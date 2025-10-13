package com.example.demo.service;

import com.example.demo.client.GeminiClient;
import com.example.demo.dto.AiAnalysisResult;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.RoutingDecision;
import com.example.demo.entity.enums.HandoffReason;
import com.example.demo.entity.enums.IntentType;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.RoutingDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for AI chat functionality
 * Handles message analysis, response generation, and tool execution
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final GeminiClient geminiClient;
    private final AiPromptService promptService;
    private final StoreConfigService storeConfigService;
    private final PiiDetectionService piiDetectionService;
    private final RoutingDecisionRepository routingDecisionRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WebSocketService webSocketService;

    /**
     * Analyze customer message using AI
     */
    @Transactional
    public AiAnalysisResult analyzeMessage(Long conversationId, String customerMessage) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Analyzing message for conversation {}", conversationId);
            
            // Check for PII first
            if (piiDetectionService.containsPII(customerMessage)) {
                log.warn("⚠️ PII DETECTED in conversation {}: {}", conversationId, 
                    piiDetectionService.getPIITypes(customerMessage));
                
                // Send urgent notification to ALL staff
                try {
                    Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
                    if (conversation != null && conversation.getCustomer() != null) {
                        String customerName = conversation.getCustomer().getFullName();
                        String piiTypes = piiDetectionService.getPIITypes(customerMessage);
                        
                        // Send broadcast notification to all staff
                        webSocketService.sendBroadcast(
                            String.format("🚨 PHÁT HIỆN THÔNG TIN CÁ NHÂN từ khách hàng %s (Loại: %s)", 
                                customerName, piiTypes),
                            "pii_alert"
                        );
                        
                        log.info("PII alert sent to all staff for conversation {}", conversationId);
                    }
                } catch (Exception e) {
                    log.error("Error sending PII notification", e);
                }
                
                return createHandoffResult(IntentType.OTHER, 0.5, 
                    "Mình xin phép chuyển bạn cho nhân viên để xử lý thông tin cá nhân nhé 💬",
                    HandoffReason.PII_DETECTED);
            }

            // Get conversation history
            String conversationHistory = getConversationHistory(conversationId);
            
            // Generate prompts
            String systemPrompt = promptService.generateSystemPrompt();
            String userMessage = promptService.generateUserMessage(customerMessage, conversationHistory);
            
            // Call Gemini API
            AiAnalysisResult result = geminiClient.analyzeMessage(systemPrompt, userMessage);
            
            if (result == null) {
                log.error("Failed to get AI analysis, forcing handoff");
                return createHandoffResult(IntentType.OTHER, 0.0,
                    "Xin lỗi bạn, mình đang gặp chút vấn đề kỹ thuật. Để mình chuyển cho nhân viên hỗ trợ bạn nhé 💬",
                    HandoffReason.AI_ERROR);
            }

            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Save routing decision
            saveRoutingDecision(conversationId, result, processingTime);
            
            log.info("AI analysis completed - Intent: {}, Confidence: {}, Handoff: {}", 
                result.getIntent(), result.getConfidence(), result.getNeedHandoff());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error analyzing message", e);
            return createHandoffResult(IntentType.OTHER, 0.0,
                "Xin lỗi bạn, hệ thống đang bận. Để mình chuyển cho nhân viên hỗ trợ bạn nhé 💬",
                HandoffReason.AI_ERROR);
        }
    }
    
    /**
     * Generate final AI response after tool execution
     * This creates a context-aware, detailed response based on actual product data
     */
    public String generateFinalResponse(Long conversationId, String customerMessage, 
                                       String toolResults, AiAnalysisResult initialAnalysis) {
        try {
            log.info("Generating final AI response with tool results for conversation {}", conversationId);
            
            // Get conversation history
            String conversationHistory = getConversationHistory(conversationId);
            
            // Build enhanced prompt for final response
            StringBuilder finalPrompt = new StringBuilder();
            finalPrompt.append("BẠN LÀ: Hoa AI - chuyên viên tư vấn hoa chuyên nghiệp\n\n");
            
            finalPrompt.append("LỊCH SỬ HỘI THOẠI:\n");
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                finalPrompt.append(conversationHistory).append("\n\n");
            }
            
            finalPrompt.append("KHÁCH HÀNG VỪA HỎI: ").append(customerMessage).append("\n\n");
            
            finalPrompt.append("KẾT QUẢ TÌM KIẾM SẢN PHẨM:\n");
            finalPrompt.append(toolResults).append("\n\n");
            
            finalPrompt.append("NHIỆM VỤ:\n");
            finalPrompt.append("1. Phân tích KỸ mục đích/ngữ cảnh của khách (ví dụ: tặng mẹ, tặng người yêu, sinh nhật...)\n");
            finalPrompt.append("2. Dựa trên danh sách sản phẩm TÌM THẤY, tư vấn 2-3 sản phẩm PHÙ HỢP NHẤT\n");
            finalPrompt.append("3. Giải thích NGẮN GỌN nhưng ẤM ÁP tại sao sản phẩm phù hợp\n");
            finalPrompt.append("4. Hiển thị ảnh: ![Tên](url) và giá\n");
            finalPrompt.append("5. Dùng emoji phù hợp (💕 🌸 ✨) để thân thiện\n\n");
            
            finalPrompt.append("QUY TẮC TƯ VẤN:\n");
            finalPrompt.append("- Nếu sản phẩm KHỚP đúng yêu cầu → tư vấn nhiệt tình\n");
            finalPrompt.append("- Nếu sản phẩm TƯƠNG TỰ (hoa khác nhưng phù hợp mục đích) → giải thích khéo: \"Hiện shop chưa có [X], nhưng [Y] cũng rất phù hợp để [mục đích] vì [lý do]\"\n");
            finalPrompt.append("- Nếu giá CAO HƠN yêu cầu → gợi ý điều chỉnh hoặc sản phẩm rẻ hơn\n");
            finalPrompt.append("- KHÔNG bao giờ nói \"không có\" rồi dừng → luôn tìm cách tư vấn\n");
            finalPrompt.append("- Gọi khách là \"bạn\", tự xưng là \"mình\"\n\n");
            
            finalPrompt.append("VÍ DỤ TƯ VẤN TỐT (NGẮN GỌN):\n");
            finalPrompt.append("\"Tặng mẹ thì hoa hồng phấn rất phù hợp nè bạn! 💕\n\n");
            finalPrompt.append("* **Bó hồng phấn Sweetie** - 520,000đ\n");
            finalPrompt.append("![Bó hồng phấn](url)\n");
            finalPrompt.append("Hồng phấn tượng trưng cho tình thương dịu dàng - hoàn hảo để tặng mẹ ạ!\n\n");
            finalPrompt.append("Bạn muốn mình tư vấn thêm không ạ? 😊\"\n\n");
            
            finalPrompt.append("CHÚ Ý QUAN TRỌNG: \n");
            finalPrompt.append("- ⚠️ KHÔNG BAO GIỜ trả về JSON format (không có {}, [], \"key\":\"value\")\n");
            finalPrompt.append("- ⚠️ KHÔNG copy nguyên phần 'TOOL_RESULT' hay 'HƯỚNG DẪN'\n");
            finalPrompt.append("- Chỉ viết TEXT thuần túy với markdown (*, **, ![]())\n");
            finalPrompt.append("- Response phải NGẮN GỌN (2-4 câu + bullet list sản phẩm)\n\n");
            
            finalPrompt.append("BẮT ĐẦU TƯ VẤN (TEXT ONLY, NO JSON):\n");
            
            // Call Gemini to generate final response
            String systemPrompt = "Tư vấn hoa NGẮN GỌN, ẤM ÁP. Chỉ trả TEXT với markdown (* list, ** bold, ![](url)). KHÔNG BAO GIỜ trả JSON!";
            
            com.example.demo.dto.gemini.GeminiResponse response = geminiClient.generateContentWithSystemPrompt(
                systemPrompt, 
                finalPrompt.toString()
            );
            
            if (response != null && response.isSuccessful()) {
                String finalResponse = response.getTextResponse();
                log.info("Generated final response: {} chars", finalResponse.length());
                
                // Clean up JSON wrapper if AI returned JSON format
                finalResponse = cleanJsonWrapper(finalResponse);
                
                // Clean up any TOOL_RESULT prefix if AI accidentally included it
                finalResponse = finalResponse.replaceAll("(?i)TOOL_RESULT:\\s*", "");
                finalResponse = finalResponse.replaceAll("(?i)HƯỚNG DẪN:.*", "");
                
                return finalResponse.trim();
            } else {
                log.warn("Failed to generate final response, using fallback");
                // Fallback: clean tool results and show simple message
                String cleanToolResults = toolResults
                    .replaceAll("(?i)TOOL_RESULT:\\s*Tìm thấy \\d+ sản phẩm:\\s*", "")
                    .replaceAll("(?i)HƯỚNG DẪN:.*", "")
                    .trim();
                
                return initialAnalysis.getReply() + "\n\n" + 
                    "Mình tìm thấy một số sản phẩm cho bạn nè:\n\n" + cleanToolResults;
            }
            
        } catch (Exception e) {
            log.error("Error generating final response", e);
            // Fallback: clean tool results
            String cleanToolResults = toolResults
                .replaceAll("(?i)TOOL_RESULT:\\s*Tìm thấy \\d+ sản phẩm:\\s*", "")
                .replaceAll("(?i)HƯỚNG DẪN:.*", "")
                .trim();
            
            return initialAnalysis.getReply() + "\n\n" + 
                "Mình tìm thấy một số sản phẩm cho bạn nè:\n\n" + cleanToolResults;
        }
    }
    
    /**
     * Clean JSON wrapper if AI accidentally returned JSON format
     * Extract content from: {"messages":[{"content":"..."}]} or similar
     */
    private String cleanJsonWrapper(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        // Check if response starts with JSON
        if (cleaned.startsWith("{") || cleaned.startsWith("[")) {
            try {
                // Try to extract content from JSON structure
                // Pattern 1: {"messages":[{"role":"assistant","content":"..."}]}
                if (cleaned.contains("\"content\"")) {
                    int contentStart = cleaned.indexOf("\"content\"");
                    if (contentStart > 0) {
                        contentStart = cleaned.indexOf(":", contentStart) + 1;
                        contentStart = cleaned.indexOf("\"", contentStart) + 1;
                        int contentEnd = cleaned.lastIndexOf("\"");
                        
                        if (contentStart > 0 && contentEnd > contentStart) {
                            String extracted = cleaned.substring(contentStart, contentEnd);
                            // Unescape JSON string
                            extracted = extracted
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                                .replace("\\/", "/");
                            
                            log.info("Extracted content from JSON wrapper");
                            return extracted;
                        }
                    }
                }
                
                // Pattern 2: {"reply":"..."}
                if (cleaned.contains("\"reply\"")) {
                    int replyStart = cleaned.indexOf("\"reply\"");
                    if (replyStart > 0) {
                        replyStart = cleaned.indexOf(":", replyStart) + 1;
                        replyStart = cleaned.indexOf("\"", replyStart) + 1;
                        int replyEnd = cleaned.indexOf("\"", replyStart);
                        
                        if (replyStart > 0 && replyEnd > replyStart) {
                            String extracted = cleaned.substring(replyStart, replyEnd);
                            extracted = extracted
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                            
                            log.info("Extracted reply from JSON");
                            return extracted;
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("Failed to parse JSON wrapper, returning as-is", e);
            }
        }
        
        // If not JSON or failed to parse, return as-is
        return cleaned;
    }

    /**
     * Get conversation history for context
     */
    private String getConversationHistory(Long conversationId) {
        try {
            List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
            
            if (messages.isEmpty()) {
                return "";
            }

            // Get last N messages
            int maxMessages = storeConfigService.getConfigAsInt("ai.max_conversation_history", 10);
            int start = Math.max(0, messages.size() - maxMessages);
            List<Message> recentMessages = messages.subList(start, messages.size());
            
            List<String> history = new ArrayList<>();
            for (Message msg : recentMessages) {
                String sender = msg.getSender().getFullName();
                String content = msg.getContent();
                history.add(sender + ": " + content);
            }
            
            return String.join("\n", history);
            
        } catch (Exception e) {
            log.error("Error getting conversation history", e);
            return "";
        }
    }

    /**
     * Save routing decision to database
     */
    @Transactional
    public void saveRoutingDecision(Long conversationId, AiAnalysisResult result, long processingTimeMs) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

            RoutingDecision decision = new RoutingDecision();
            decision.setConversation(conversation);
            decision.setIntent(result.getIntentType());
            decision.setConfidence(result.getConfidenceAsBigDecimal());
            decision.setNeedHandoff(result.requiresHandoff());
            decision.setSuggestHandoff(result.suggestsHandoff());
            decision.setAiResponse(result.getReply());
            decision.setProcessingTimeMs((int) processingTimeMs);
            
            // Set handoff reason if needed
            if (result.requiresHandoff()) {
                decision.setHandoffReason(determineHandoffReason(result));
            }
            
            // Set tools used
            if (result.hasToolRequests()) {
                List<String> toolNames = new ArrayList<>();
                for (AiAnalysisResult.ToolRequest tool : result.getToolRequests()) {
                    toolNames.add(tool.getName());
                }
                decision.setToolsUsed(toolNames.toArray(new String[0]));
            }
            
            routingDecisionRepository.save(decision);
            log.debug("Routing decision saved for conversation {}", conversationId);
            
        } catch (Exception e) {
            log.error("Error saving routing decision", e);
        }
    }

    /**
     * Determine handoff reason from AI result
     */
    private HandoffReason determineHandoffReason(AiAnalysisResult result) {
        IntentType intent = result.getIntentType();
        
        if (result.getConfidence() != null && result.getConfidence() < 0.65) {
            return HandoffReason.LOW_CONFIDENCE;
        }
        
        switch (intent) {
            case ORDER_SUPPORT:
                return HandoffReason.ORDER_INQUIRY;
            case PAYMENT:
                return HandoffReason.PAYMENT_ISSUE;
            default:
                return HandoffReason.COMPLEX_QUERY;
        }
    }

    /**
     * Create handoff result when AI cannot handle
     */
    private AiAnalysisResult createHandoffResult(IntentType intent, double confidence, 
                                                  String reply, HandoffReason reason) {
        AiAnalysisResult result = new AiAnalysisResult();
        result.setIntent(intent.name());
        result.setConfidence(confidence);
        result.setReply(reply);
        result.setNeedHandoff(true);
        result.setSuggestHandoff(false);
        result.setToolRequests(new ArrayList<>());
        result.setProductSuggestions(new ArrayList<>());
        return result;
    }

    /**
     * Check if AI should handle the message based on confidence
     */
    public boolean shouldAiHandle(AiAnalysisResult result) {
        if (result == null) {
            return false;
        }

        // Check if handoff is required
        if (result.requiresHandoff()) {
            return false;
        }

        // Check confidence threshold
        Map<String, Double> thresholds = storeConfigService.getConfidenceThresholds();
        double autoThreshold = thresholds.getOrDefault("auto", 0.80);
        
        return result.isConfidentEnough(autoThreshold);
    }

    /**
     * Check if AI suggests handoff
     */
    public boolean shouldSuggestHandoff(AiAnalysisResult result) {
        if (result == null || result.requiresHandoff()) {
            return false;
        }

        Map<String, Double> thresholds = storeConfigService.getConfidenceThresholds();
        double suggestThreshold = thresholds.getOrDefault("suggest", 0.65);
        double autoThreshold = thresholds.getOrDefault("auto", 0.80);
        
        return result.getConfidence() >= suggestThreshold && 
               result.getConfidence() < autoThreshold;
    }

    /**
     * Format AI reply with handoff suggestion if needed
     */
    public String formatReplyWithHandoffSuggestion(AiAnalysisResult result) {
        String reply = result.getReply();
        
        if (shouldSuggestHandoff(result)) {
            reply += "\n\n_Bạn có muốn mình chuyển cho nhân viên để được hỗ trợ tốt hơn không?_ 💬";
        }
        
        return reply;
    }
}

