
package com.example.demo.service;

import com.example.demo.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebSocket Service for sending real-time messages
 * Following rules.mdc specifications for WebSocket messaging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send welcome message to user after successful login
     * @param userId User ID
     * @param userName User's full name
     */
    public void sendWelcomeMessage(Long userId, String userName) {
        if (userId == null) {
            log.warn("Cannot send welcome message: userId is null");
            return;
        }
        
        try {
            String welcomeMessage = String.format(
                "🌸 Welcome back to StarShop, %s! Happy shopping! 🌹", 
                userName != null ? userName : "Dear Customer"
            );
            
            // Send to user-specific topic as per rules.mdc: /topic/messages/{userId}
            String destination = "/topic/messages/" + userId;
            
            WelcomeMessagePayload payload = new WelcomeMessagePayload(
                "welcome",
                welcomeMessage,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("Welcome message sent to user {} at destination: {}", userId, destination);
            
        } catch (Exception e) {
            log.error("Error sending welcome message to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send order status update message
     * @param userId User ID
     * @param orderId Order ID
     * @param status New order status
     */
    public void sendOrderStatusUpdate(Long userId, Long orderId, String status) {
        if (userId == null || orderId == null) {
            log.warn("Cannot send order update: userId or orderId is null");
            return;
        }
        
        try {
            String message = String.format(
                "📦 Your order #%d status has been updated to: %s", 
                orderId, status
            );
            
            OrderUpdatePayload payload = new OrderUpdatePayload(
                "order_update",
                message,
                orderId,
                status,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            // Send to user-specific topic
            String destination = "/topic/messages/" + userId;
            messagingTemplate.convertAndSend(destination, payload);
            
            // Also send to general orders topic
            messagingTemplate.convertAndSend("/topic/orders", payload);
            
            log.info("Order status update sent to user {} for order {}: {}", userId, orderId, status);
            
        } catch (Exception e) {
            log.error("Error sending order status update to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send general notification to user
     * @param userId User ID
     * @param message Notification message
     * @param type Message type
     */
    public void sendNotification(Long userId, String message, String type) {
        if (userId == null || message == null) {
            log.warn("Cannot send notification: userId or message is null");
            return;
        }
        
        try {
            NotificationPayload payload = new NotificationPayload(
                type != null ? type : "notification",
                message,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            String destination = "/topic/messages/" + userId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("Notification sent to user {}: {}", userId, message);
            
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send broadcast message to all users
     * @param message Broadcast message
     * @param type Message type
     */
    public void sendBroadcast(String message, String type) {
        if (message == null) {
            log.warn("Cannot send broadcast: message is null");
            return;
        }
        
        try {
            NotificationPayload payload = new NotificationPayload(
                type != null ? type : "broadcast",
                message,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            messagingTemplate.convertAndSend("/topic/notifications", payload);
            
            log.info("Broadcast message sent: {}", message);
            
        } catch (Exception e) {
            log.error("Error sending broadcast message: {}", e.getMessage(), e);
        }
    }

    /**
     * Send conversation update to staff
     * @param conversationId Conversation ID
     * @param updateType Type of update (new_message, status_change, etc.)
     * @param data Update data
     */
    public void sendConversationUpdate(Long conversationId, String updateType, Object data) {
        if (conversationId == null) {
            log.warn("Cannot send conversation update: conversationId is null");
            return;
        }
        
        try {
            ConversationUpdatePayload payload = new ConversationUpdatePayload(
                conversationId,
                updateType,
                data,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            messagingTemplate.convertAndSend("/topic/conversation-updates", payload);
            
            log.info("Conversation update sent for conversation {}: {}", conversationId, updateType);
            
        } catch (Exception e) {
            log.error("Error sending conversation update: {}", e.getMessage(), e);
        }
    }

    /**
     * Send general chat update (like Messenger/Zalo)
     * @param updateType Type of update
     * @param data Update data
     */
    public void sendChatUpdate(String updateType, Object data) {
        if (updateType == null) {
            log.warn("Cannot send chat update: updateType is null");
            return;
        }
        
        try {
            ChatUpdatePayload payload = new ChatUpdatePayload(
                updateType,
                data,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            messagingTemplate.convertAndSend("/topic/chat-updates", payload);
            
            log.info("Chat update sent: {}", updateType);
            
        } catch (Exception e) {
            log.error("Error sending chat update: {}", e.getMessage(), e);
        }
    }

    /**
     * Send chat message to conversation participants
     * @param chatMessage Chat message DTO
     */
    public void sendChatMessage(ChatMessageDTO chatMessage) {
        if (chatMessage == null || chatMessage.getConversationId() == null) {
            log.warn("Cannot send chat message: message or conversationId is null");
            return;
        }
        
        try {
            // Send to conversation topic (both participants subscribed)
            String conversationTopic = "/topic/chat/" + chatMessage.getConversationId();
            messagingTemplate.convertAndSend(conversationTopic, chatMessage);
            
            // Send to staff topic for real-time updates
            messagingTemplate.convertAndSend("/topic/chat/staff", chatMessage);
            
            // Also send to receiver's personal queue
            if (chatMessage.getReceiverId() != null) {
                String userQueue = "/user/queue/chat";
                messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(), 
                    "/queue/chat", 
                    chatMessage
                );
            }
            
            log.info("Chat message sent to conversation {}: {}", 
                chatMessage.getConversationId(), chatMessage.getContent());
            
        } catch (Exception e) {
            log.error("Error sending chat message: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify staff about new message
     * @param staffId Staff ID
     * @param chatMessage Chat message
     */
    public void notifyStaffNewMessage(Long staffId, ChatMessageDTO chatMessage) {
        if (staffId == null) {
            log.warn("Cannot notify staff: staffId is null");
            return;
        }
        
        try {
            String notification = String.format(
                "Tin nhắn mới từ %s trong cuộc hội thoại #%s",
                chatMessage.getSenderName(),
                chatMessage.getConversationId()
            );
            
            NotificationPayload payload = new NotificationPayload(
                "new_message",
                notification,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            String destination = "/topic/messages/" + staffId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("New message notification sent to staff {}", staffId);
            
        } catch (Exception e) {
            log.error("Error notifying staff {}: {}", staffId, e.getMessage(), e);
        }
    }

    /**
     * Broadcast to all staff members
     * @param message Broadcast message
     */
    public void broadcastToStaff(String message) {
        if (message == null) {
            log.warn("Cannot broadcast to staff: message is null");
            return;
        }
        
        try {
            NotificationPayload payload = new NotificationPayload(
                "staff_broadcast",
                message,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            messagingTemplate.convertAndSend("/topic/staff/notifications", payload);
            
            log.info("Broadcast message sent to all staff: {}", message);
            
        } catch (Exception e) {
            log.error("Error broadcasting to staff: {}", e.getMessage(), e);
        }
    }

    /**
     * Send typing indicator
     * @param conversationId Conversation ID
     * @param userId User ID who is typing
     * @param userName User name who is typing
     */
    public void sendTypingIndicator(String conversationId, Long userId, String userName) {
        if (conversationId == null || userId == null) {
            return;
        }
        
        try {
            TypingIndicatorPayload payload = new TypingIndicatorPayload(
                userId,
                userName,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            String destination = "/topic/chat/" + conversationId + "/typing";
            messagingTemplate.convertAndSend(destination, payload);
            
        } catch (Exception e) {
            log.error("Error sending typing indicator: {}", e.getMessage(), e);
        }
    }

    // Message payload classes
    public static class WelcomeMessagePayload {
        public final String type;
        public final String message;
        public final String timestamp;
        
        public WelcomeMessagePayload(String type, String message, String timestamp) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    public static class OrderUpdatePayload {
        public final String type;
        public final String message;
        public final Long orderId;
        public final String status;
        public final String timestamp;
        
        public OrderUpdatePayload(String type, String message, Long orderId, String status, String timestamp) {
            this.type = type;
            this.message = message;
            this.orderId = orderId;
            this.status = status;
            this.timestamp = timestamp;
        }
    }
    
    public static class NotificationPayload {
        public final String type;
        public final String message;
        public final String timestamp;
        
        public NotificationPayload(String type, String message, String timestamp) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    public static class TypingIndicatorPayload {
        public final Long userId;
        public final String userName;
        public final String timestamp;
        
        public TypingIndicatorPayload(Long userId, String userName, String timestamp) {
            this.userId = userId;
            this.userName = userName;
            this.timestamp = timestamp;
        }
    }
    
    public static class ConversationUpdatePayload {
        public final Long conversationId;
        public final String updateType;
        public final Object data;
        public final String timestamp;
        
        public ConversationUpdatePayload(Long conversationId, String updateType, Object data, String timestamp) {
            this.conversationId = conversationId;
            this.updateType = updateType;
            this.data = data;
            this.timestamp = timestamp;
        }
    }
    
    public static class ChatUpdatePayload {
        public final String type;
        public final Object data;
        public final String timestamp;
        
        public ChatUpdatePayload(String type, Object data, String timestamp) {
            this.type = type;
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
