
package com.example.demo.service;

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
}
