package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.dto.ConversationDTO;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Chat Operations
 * Shared by both customers and staff for messaging
 * Following rules.mdc specifications for REST API
 */
@RestController
@RequestMapping("/api/chat")
@PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat API", description = "Chat and messaging endpoints")
public class ChatApiController extends BaseController {

    private final ChatService chatService;

    /**
     * Start a new conversation (customer only)
     */
    @Operation(summary = "Start conversation", description = "Customer starts a new conversation with support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversation started or existing conversation returned"),
        @ApiResponse(responseCode = "400", description = "Failed to start conversation")
    })
    @PostMapping("/conversations/start")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseWrapper<ConversationDTO>> startConversation(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(401).body(new ResponseWrapper<>(null, "Unauthorized"));
            }
            log.info("User {} starting conversation", userId);
            
            ConversationDTO conversation = chatService.startConversation(userId);
            return ResponseEntity.ok(new ResponseWrapper<>(conversation, "Cuộc hội thoại đã được tạo"));
            
        } catch (Exception e) {
            log.error("Error starting conversation", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, "Không thể tạo cuộc hội thoại: " + e.getMessage()));
        }
    }

    /**
     * Get user's conversations
     */
    @Operation(summary = "Get my conversations", description = "Get all conversations for current user")
    @GetMapping("/conversations/my")
    public ResponseEntity<ResponseWrapper<List<ConversationDTO>>> getMyConversations(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(401).body(new ResponseWrapper<>(null, "Unauthorized"));
            }
            log.info("Getting conversations for user {}", userId);
            
            List<ConversationDTO> conversations = chatService.getCustomerConversations(userId);
            return ResponseEntity.ok(new ResponseWrapper<>(conversations, null));
            
        } catch (Exception e) {
            log.error("Error getting user conversations", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, e.getMessage()));
        }
    }

    /**
     * Get conversation by ID
     */
    @Operation(summary = "Get conversation", description = "Get conversation details by ID")
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ResponseWrapper<ConversationDTO>> getConversation(@PathVariable Long id) {
        try {
            log.info("Getting conversation {}", id);
            
            ConversationDTO conversation = chatService.getConversation(id);
            return ResponseEntity.ok(new ResponseWrapper<>(conversation, null));
            
        } catch (Exception e) {
            log.error("Error getting conversation", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, e.getMessage()));
        }
    }

    /**
     * Get messages for a conversation
     */
    @Operation(summary = "Get conversation messages", description = "Get all messages in a conversation")
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ResponseWrapper<List<ChatMessageDTO>>> getConversationMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            String conversationId = id.toString();
            log.info("Getting messages for conversation {}", conversationId);
            
            List<ChatMessageDTO> messages = chatService.getConversationMessages(conversationId, page, size);
            return ResponseEntity.ok(new ResponseWrapper<>(messages, null));
            
        } catch (Exception e) {
            log.error("Error getting conversation messages", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, e.getMessage()));
        }
    }

    /**
     * Send a message
     */
    @Operation(summary = "Send message", description = "Send a message in a conversation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Failed to send message")
    })
    @PostMapping("/messages")
    public ResponseEntity<ResponseWrapper<ChatMessageDTO>> sendMessage(
            @RequestBody ChatMessageDTO messageDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(401).body(new ResponseWrapper<>(null, "Unauthorized"));
            }
            messageDTO.setSenderId(userId);
            
            log.info("User {} sending message in conversation {}", userId, messageDTO.getConversationId());
            
            ChatMessageDTO sentMessage = chatService.sendMessage(messageDTO);
            return ResponseEntity.ok(new ResponseWrapper<>(sentMessage, "Tin nhắn đã được gửi"));
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, "Không thể gửi tin nhắn: " + e.getMessage()));
        }
    }

    /**
     * Send first message (creates conversation if needed)
     */
    @Operation(summary = "Send first message", description = "Send first message and create conversation if needed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Failed to send message")
    })
    @PostMapping("/messages/first")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseWrapper<ChatMessageDTO>> sendFirstMessage(
            @RequestBody ChatMessageDTO messageDTO,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(401).body(new ResponseWrapper<>(null, "Unauthorized"));
            }
            messageDTO.setSenderId(userId);
            
            log.info("Customer {} sending first message", userId);
            
            ChatMessageDTO sentMessage = chatService.sendFirstMessage(messageDTO);
            return ResponseEntity.ok(new ResponseWrapper<>(sentMessage, "Tin nhắn đầu tiên đã được gửi"));
            
        } catch (Exception e) {
            log.error("Error sending first message", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, "Không thể gửi tin nhắn: " + e.getMessage()));
        }
    }

    /**
     * Mark messages as read
     */
    @Operation(summary = "Mark messages as read", description = "Mark all messages in a conversation as read")
    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<ResponseWrapper<String>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(401).body(new ResponseWrapper<>(null, "Unauthorized"));
            }
            log.info("User {} marking messages as read in conversation {}", userId, id);
            
            chatService.markMessagesAsRead(id, userId);
            return ResponseEntity.ok(new ResponseWrapper<>("OK", "Đã đánh dấu là đã đọc"));
            
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, e.getMessage()));
        }
    }

    /**
     * Get unassigned conversations (staff/admin only)
     */
    @Operation(summary = "Get unassigned conversations", description = "Get all unassigned conversations in queue")
    @GetMapping("/conversations/unassigned")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ResponseWrapper<List<ConversationDTO>>> getUnassignedConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Getting unassigned conversations");
            
            List<ConversationDTO> conversations = chatService.getUnassignedConversations(page, size);
            return ResponseEntity.ok(new ResponseWrapper<>(conversations, null));
            
        } catch (Exception e) {
            log.error("Error getting unassigned conversations", e);
            return ResponseEntity.badRequest()
                .body(new ResponseWrapper<>(null, e.getMessage()));
        }
    }
}

