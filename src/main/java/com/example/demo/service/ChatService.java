package com.example.demo.service;

import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.dto.ConversationDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.ConversationStatus;
import com.example.demo.entity.enums.MessageType;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing chat conversations and messages
 * Following rules.mdc specifications for business logic tier
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    /**
     * Start a new conversation for a customer
     * If customer already has an open conversation, return that instead
     */
    public ConversationDTO startConversation(Long customerId) {
        log.info("Starting conversation for customer ID: {}", customerId);
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Only allow customers to start conversations
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new RuntimeException("Only customers can start conversations");
        }
        
        // Check if customer already has an open or assigned conversation
        Optional<Conversation> existingConv = conversationRepository
            .findByCustomerIdAndStatusIn(customerId, 
                Arrays.asList(ConversationStatus.OPEN, ConversationStatus.ASSIGNED));
        
        if (existingConv.isPresent()) {
            log.info("Found existing conversation ID: {}", existingConv.get().getId());
            return convertToDTO(existingConv.get());
        }
        
        // Create new conversation
        Conversation conversation = new Conversation(customer);
        conversation = conversationRepository.save(conversation);
        
        log.info("Created new conversation ID: {}", conversation.getId());
        
        // Notify staff about new conversation
        webSocketService.sendNotification(null, 
            "Cuộc hội thoại mới từ " + customer.getFullName(), 
            "new_conversation");
        
        return convertToDTO(conversation);
    }

    /**
     * Assign conversation to a specific staff member
     */
    public ConversationDTO assignConversation(Long conversationId, Long staffId) {
        log.info("Assigning conversation {} to staff {}", conversationId, staffId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        User staff = userRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        if (staff.getRole() != UserRole.STAFF && staff.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("User is not a staff member");
        }
        
        conversation.assignToStaff(staff);
        conversation = conversationRepository.save(conversation);
        
        // Send system message
        String systemMessage = "Cuộc hội thoại đã được chuyển đến " + staff.getFullName();
        sendSystemMessage(conversation.getId(), systemMessage);
        
        // Notify staff
        webSocketService.sendNotification(staffId, 
            "Bạn đã được giao cuộc hội thoại với " + conversation.getCustomer().getFullName(), 
            "conversation_assigned");
        
        return convertToDTO(conversation);
    }

    /**
     * Auto-assign conversation to available staff with least workload
     */
    public ConversationDTO autoAssignConversation(Long conversationId) {
        log.info("Auto-assigning conversation {}", conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Find staff with least active conversations
        List<User> staffList = userRepository.findByRole(UserRole.STAFF);
        
        if (staffList.isEmpty()) {
            log.warn("No staff available for auto-assignment");
            return convertToDTO(conversation);
        }
        
        User selectedStaff = null;
        Long minWorkload = Long.MAX_VALUE;
        
        for (User staff : staffList) {
            if (staff.getIsActive()) {
                Long workload = conversationRepository.getStaffActiveConversationCount(staff.getId());
                if (workload < minWorkload) {
                    minWorkload = workload;
                    selectedStaff = staff;
                }
            }
        }
        
        if (selectedStaff != null) {
            return assignConversation(conversationId, selectedStaff.getId());
        }
        
        return convertToDTO(conversation);
    }

    /**
     * Send a message in a conversation
     */
    public ChatMessageDTO sendMessage(ChatMessageDTO messageDTO) {
        log.info("Sending message in conversation: {}", messageDTO.getConversationId());
        
        User sender = userRepository.findById(messageDTO.getSenderId())
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        // Determine receiver based on conversation
        User receiver = null;
        if (messageDTO.getReceiverId() != null) {
            receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElse(null);
        } else {
            // If no receiver specified, find the other participant in the conversation
            Conversation conversation = conversationRepository.findById(messageDTO.getConversationId())
                .orElse(null);
            if (conversation != null) {
                if (sender.getId().equals(conversation.getCustomer().getId())) {
                    // Customer is sending, receiver is assigned staff (if any)
                    receiver = conversation.getAssignedStaff();
                } else {
                    // Staff is sending, receiver is customer
                    receiver = conversation.getCustomer();
                }
            }
        }
        
        // Create message entity
        Message message = new Message(sender, receiver, messageDTO.getContent());
        message.setConversationId(messageDTO.getConversationId());
        message.setMessageType(messageDTO.getMessageType() != null ? 
            messageDTO.getMessageType() : MessageType.TEXT);
        message.setIsAiGenerated(messageDTO.getIsAiGenerated() != null ? 
            messageDTO.getIsAiGenerated() : false);
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation last message time
        conversationRepository.findById(messageDTO.getConversationId()).ifPresent(conv -> {
            conv.setLastMessageAt(savedMessage.getSentAt());
            conversationRepository.save(conv);
        });
        
        // Convert to DTO and send via WebSocket
        ChatMessageDTO resultDTO = convertMessageToDTO(savedMessage);
        webSocketService.sendChatMessage(resultDTO);
        
        // Send conversation update to staff
        webSocketService.sendConversationUpdate(messageDTO.getConversationId(), "new_message", resultDTO);
        
        // Send general chat update (like Messenger/Zalo)
        webSocketService.sendChatUpdate("message_sent", resultDTO);
        
        log.info("Message sent successfully, ID: {}", savedMessage.getId());
        return resultDTO;
    }

    /**
     * Send first message and create conversation if needed
     * This method handles the case where a customer sends their first message
     */
    public ChatMessageDTO sendFirstMessage(ChatMessageDTO messageDTO) {
        log.info("Sending first message for customer: {}", messageDTO.getSenderId());
        
        User sender = userRepository.findById(messageDTO.getSenderId())
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        // Only customers can send first messages
        if (sender.getRole() != UserRole.CUSTOMER) {
            throw new RuntimeException("Only customers can send first messages");
        }
        
        // Check if customer already has an open conversation
        Optional<Conversation> existingConv = conversationRepository
            .findByCustomerIdAndStatusIn(sender.getId(), 
                Arrays.asList(ConversationStatus.OPEN, ConversationStatus.ASSIGNED));
        
        Conversation conversation;
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
            log.info("Using existing conversation ID: {}", conversation.getId());
        } else {
            // Create new conversation
            conversation = new Conversation(sender);
            conversation = conversationRepository.save(conversation);
            log.info("Created new conversation ID: {}", conversation.getId());
            
            // Notify staff about new conversation
            webSocketService.sendNotification(null, 
                "Cuộc hội thoại mới từ " + sender.getFullName(), 
                "new_conversation");
        }
        
        // Create message entity - receiver is assigned staff (if any)
        Message message = new Message(sender, conversation.getAssignedStaff(), messageDTO.getContent());
        message.setConversationId(conversation.getId());
        message.setMessageType(messageDTO.getMessageType() != null ? 
            messageDTO.getMessageType() : MessageType.TEXT);
        message.setIsAiGenerated(messageDTO.getIsAiGenerated() != null ? 
            messageDTO.getIsAiGenerated() : false);
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation last message time
        conversation.setLastMessageAt(savedMessage.getSentAt());
        conversationRepository.save(conversation);
        
        // Convert to DTO and send via WebSocket
        ChatMessageDTO resultDTO = convertMessageToDTO(savedMessage);
        webSocketService.sendChatMessage(resultDTO);
        
        // Send conversation update to staff
        webSocketService.sendConversationUpdate(conversation.getId(), "new_message", resultDTO);
        
        // Send general chat update (like Messenger/Zalo)
        webSocketService.sendChatUpdate("message_sent", resultDTO);
        
        log.info("First message sent successfully, ID: {}", savedMessage.getId());
        return resultDTO;
    }

    /**
     * Send a system message (not from user)
     */
    public void sendSystemMessage(Long conversationId, String content) {
        ChatMessageDTO systemMessage = ChatMessageDTO.createSystemMessage(conversationId, content);
        webSocketService.sendChatMessage(systemMessage);
    }

    /**
     * Close a conversation
     */
    public ConversationDTO closeConversation(Long conversationId) {
        log.info("Closing conversation {}", conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        conversation.close();
        conversation = conversationRepository.save(conversation);
        
        // Send system message
        sendSystemMessage(conversationId, "Cuộc hội thoại đã được đóng");
        
        return convertToDTO(conversation);
    }

    /**
     * Reopen a closed conversation
     */
    public ConversationDTO reopenConversation(Long conversationId) {
        log.info("Reopening conversation {}", conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        conversation.reopen();
        conversation = conversationRepository.save(conversation);
        
        // Send system message
        sendSystemMessage(conversationId, "Cuộc hội thoại đã được mở lại");
        
        return convertToDTO(conversation);
    }

    /**
     * Get all conversations for a staff member
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getStaffConversations(Long staffId, int page, int size) {
        log.info("Getting conversations for staff {}", staffId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Conversation> conversations = conversationRepository
            .findByAssignedStaffIdAndStatusInOrderByLastMessageAtDesc(
                staffId, 
                Arrays.asList(ConversationStatus.ASSIGNED, ConversationStatus.OPEN),
                pageable);
        
        return conversations.stream()
            .map(conv -> convertToStaffDTO(conv, staffId))
            .collect(Collectors.toList());
    }

    /**
     * Get unassigned conversations (queue)
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUnassignedConversations(int page, int size) {
        log.info("Getting unassigned conversations");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Conversation> conversations = conversationRepository
            .findUnassignedConversations(pageable);
        
        return conversations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get messages for a conversation
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationMessages(String conversationId, int page, int size) {
        log.info("Getting messages for conversation {}", conversationId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository
            .findByConversationIdOrderBySentAtDesc(Long.parseLong(conversationId), pageable);
        
        return messages.stream()
            .map(this::convertMessageToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Mark messages as read in a conversation
     */
    public void markMessagesAsRead(Long conversationId, Long userId) {
        log.info("Marking messages as read for conversation {} and user {}", conversationId, userId);
        messageRepository.markConversationMessagesAsRead(conversationId, userId);
    }

    /**
     * Get conversation by ID
     */
    @Transactional(readOnly = true)
    public ConversationDTO getConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return convertToDTO(conversation);
    }

    /**
     * Get customer's conversations
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getCustomerConversations(Long customerId) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conversation> conversations = conversationRepository
            .findByCustomerIdOrderByLastMessageAtDesc(customerId, pageable);
        
        return conversations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Search staff conversations
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> searchStaffConversations(Long staffId, String searchTerm, int page, int size) {
        log.info("Searching conversations for staff {} with term: {}", staffId, searchTerm);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Conversation> conversations = conversationRepository
            .searchStaffConversations(staffId, searchTerm, 
                Arrays.asList(ConversationStatus.ASSIGNED, ConversationStatus.OPEN),
                pageable);
        
        return conversations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Helper methods

    private ConversationDTO convertToStaffDTO(Conversation conversation, Long staffId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setStatus(conversation.getStatus());
        dto.setPriority(conversation.getPriority());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setClosedAt(conversation.getClosedAt());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setNotes(conversation.getNotes());
        
        // Customer info
        User customer = conversation.getCustomer();
        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getFullName());
        dto.setCustomerEmail(customer.getEmail());
        dto.setCustomerAvatar(customer.getAvatar());
        dto.setCustomerPhone(customer.getPhone());
        
        // Staff info
        if (conversation.getAssignedStaff() != null) {
            User staff = conversation.getAssignedStaff();
            dto.setAssignedStaffId(staff.getId());
            dto.setAssignedStaffName(staff.getFullName());
            dto.setAssignedStaffCode(staff.getEmployeeCode());
            dto.setAssignedStaffAvatar(staff.getAvatar());
        }
        
        // Last message info
        List<Message> lastMessages = messageRepository.findLastMessageByConversationId(conversation.getId(), PageRequest.of(0, 1));
        if (!lastMessages.isEmpty()) {
            Message lastMessage = lastMessages.get(0);
            dto.setLastMessageContent(lastMessage.getContent());
            dto.setLastMessageSenderId(lastMessage.getSender().getId());
        }
        
        // Unread count for staff - count unread messages from customer to staff
        Long unreadCount = messageRepository.countUnreadByConversationIdAndReceiverId(conversation.getId(), staffId);
        dto.setUnreadCount(unreadCount);
        
        return dto;
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setStatus(conversation.getStatus());
        dto.setPriority(conversation.getPriority());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setClosedAt(conversation.getClosedAt());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setNotes(conversation.getNotes());
        
        // Customer info
        User customer = conversation.getCustomer();
        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getFullName());
        dto.setCustomerEmail(customer.getEmail());
        dto.setCustomerAvatar(customer.getAvatar());
        dto.setCustomerPhone(customer.getPhone());
        
        // Staff info
        if (conversation.getAssignedStaff() != null) {
            User staff = conversation.getAssignedStaff();
            dto.setAssignedStaffId(staff.getId());
            dto.setAssignedStaffName(staff.getFullName());
            dto.setAssignedStaffCode(staff.getEmployeeCode());
            dto.setAssignedStaffAvatar(staff.getAvatar());
        }
        
        // Last message info
        List<Message> lastMessages = messageRepository.findLastMessageByConversationId(conversation.getId(), PageRequest.of(0, 1));
        if (!lastMessages.isEmpty()) {
            Message lastMessage = lastMessages.get(0);
            dto.setLastMessageContent(lastMessage.getContent());
            dto.setLastMessageSenderId(lastMessage.getSender().getId());
        }
        
        // Unread count - count unread messages in this conversation for the customer
        Long unreadCount = messageRepository.countUnreadByConversationIdAndReceiverId(conversation.getId(), customer.getId());
        dto.setUnreadCount(unreadCount);
        
        return dto;
    }

    private ChatMessageDTO convertMessageToDTO(Message message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setSenderAvatar(message.getSender().getAvatar());
        
        // Handle null receiver (for customer messages when no staff assigned)
        if (message.getReceiver() != null) {
            dto.setReceiverId(message.getReceiver().getId());
            dto.setReceiverName(message.getReceiver().getFullName());
        }
        
        dto.setConversationId(message.getConversationId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setIsRead(message.getIsRead());
        dto.setIsAiGenerated(message.getIsAiGenerated());
        dto.setSentAt(message.getSentAt());
        return dto;
    }
}

