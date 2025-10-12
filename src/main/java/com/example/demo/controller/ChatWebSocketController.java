package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.service.ChatService;
import com.example.demo.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket Controller for Real-time Chat
 * Handles WebSocket STOMP messages for chat functionality
 * Following rules.mdc specifications for WebSocket with STOMP
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final WebSocketService webSocketService;

    /**
     * Handle sending chat messages via WebSocket
     * Endpoint: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO message, Principal principal) {
        try {
            log.info("Received chat message via WebSocket from user: {}, conversation: {}", 
                principal != null ? principal.getName() : "unknown", 
                message.getConversationId());
            
            // Send message through chat service
            ChatMessageDTO sentMessage = chatService.sendMessage(message);
            
            // WebSocket service will broadcast to conversation participants
            log.info("Chat message processed successfully, ID: {}", sentMessage.getId());
            
        } catch (Exception e) {
            log.error("Error processing chat message via WebSocket", e);
        }
    }

    /**
     * Handle typing indicator
     * Endpoint: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingData, Principal principal) {
        try {
            // Handle both String and Number types for conversationId and userId
            Object conversationIdObj = typingData.get("conversationId");
            Object userIdObj = typingData.get("userId");
            
            Long conversationId;
            Long userId;
            
            if (conversationIdObj instanceof String) {
                conversationId = Long.parseLong((String) conversationIdObj);
            } else if (conversationIdObj instanceof Number) {
                conversationId = ((Number) conversationIdObj).longValue();
            } else {
                log.error("Invalid conversationId type: {}", conversationIdObj.getClass());
                return;
            }
            
            if (userIdObj instanceof String) {
                userId = Long.parseLong((String) userIdObj);
            } else if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            } else {
                log.error("Invalid userId type: {}", userIdObj.getClass());
                return;
            }
            
            String userName = (String) typingData.get("userName");
            
            // Broadcast typing indicator to conversation
            webSocketService.sendTypingIndicator(conversationId.toString(), userId, userName);
            
        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
        }
    }

    /**
     * Handle user joining a conversation
     * Endpoint: /app/chat.join
     */
    @MessageMapping("/chat.join")
    public void joinConversation(@Payload Map<String, Object> joinData, 
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long conversationId = ((Number) joinData.get("conversationId")).longValue();
            Long userId = ((Number) joinData.get("userId")).longValue();
            String userName = (String) joinData.get("userName");
            
            log.info("User {} ({}) joined conversation {}", userName, userId, conversationId);
            
            // Store user info in WebSocket session
            var sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put("conversationId", conversationId);
                sessionAttributes.put("userId", userId);
                sessionAttributes.put("userName", userName);
            }
            
            // Send system message to conversation
            String systemMessage = userName + " đã tham gia cuộc hội thoại";
            chatService.sendSystemMessage(conversationId, systemMessage);
            
        } catch (Exception e) {
            log.error("Error handling user join", e);
        }
    }

    /**
     * Handle user leaving a conversation
     * Endpoint: /app/chat.leave
     */
    @MessageMapping("/chat.leave")
    public void leaveConversation(@Payload Map<String, Object> leaveData) {
        try {
            Long conversationId = ((Number) leaveData.get("conversationId")).longValue();
            Long userId = ((Number) leaveData.get("userId")).longValue();
            String userName = (String) leaveData.get("userName");
            
            log.info("User {} ({}) left conversation {}", userName, userId, conversationId);
            
            // Send system message to conversation
            String systemMessage = userName + " đã rời khỏi cuộc hội thoại";
            chatService.sendSystemMessage(conversationId, systemMessage);
            
        } catch (Exception e) {
            log.error("Error handling user leave", e);
        }
    }

    /**
     * Handle marking messages as read
     * Endpoint: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Map<String, Object> readData) {
        try {
            Long conversationId = ((Number) readData.get("conversationId")).longValue();
            Long userId = ((Number) readData.get("userId")).longValue();
            
            
            chatService.markMessagesAsRead(conversationId, userId);
            
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }
}

