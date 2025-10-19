package com.example.demo.service;

import com.example.demo.client.GeminiClient;
import com.example.demo.dto.AiInsightResponse;
import com.example.demo.dto.DashboardInsightsData;
import com.example.demo.dto.ReviewAiAnalysisResponse;
import com.example.demo.dto.VoucherSuggestionResponse;
import com.example.demo.dto.gemini.GeminiResponse;
import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.Voucher;
import com.example.demo.entity.enums.DiscountType;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.VoucherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service riêng biệt cho AI Insights của Admin Dashboard
 * KHÔNG can thiệp vào AiAnalyticsService (AI chat)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminAiInsightsService {

    private final GeminiClient geminiClient;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final VoucherRepository voucherRepository;
    private final ObjectMapper objectMapper;

    /**
     * Lấy AI insights cho dashboard với caching
     */
    @Cacheable(value = "adminAiInsights", key = "'dashboard'")
    public AiInsightResponse getAiInsights() {
        log.info("Generating AI insights for admin dashboard");
        
        try {
            // 1. Thu thập raw data từ repositories
            DashboardInsightsData rawData = collectRawData();
            log.debug("Collected raw data: {}", rawData);
            
            // 2. Build prompt với data thực
            String prompt = buildPrompt(rawData);
            log.debug("Built prompt for AI");
            
            // 3. Call Gemini API
            GeminiResponse response = geminiClient.generateContentWithRetry(prompt, 2);
            if (response == null || !response.isSuccessful()) {
                log.warn("Gemini API failed, returning fallback insights");
                return getFallbackInsights();
            }
            
            // 4. Parse JSON response
            String jsonResponse = response.getTextResponse();
            log.debug("AI response: {}", jsonResponse);
            
            AiInsightResponse aiResponse = objectMapper.readValue(jsonResponse, AiInsightResponse.class);
            
            // 5. Validate response
            if (aiResponse.getInsights() == null || aiResponse.getInsights().isEmpty()) {
                log.warn("AI returned empty insights, using fallback");
                return getFallbackInsights();
            }
            
            log.info("Successfully generated {} AI insights", aiResponse.getInsights().size());
            return aiResponse;
            
        } catch (Exception e) {
            log.error("Error generating AI insights", e);
            return getFallbackInsights();
        }
    }

    /**
     * Thu thập raw data từ database
     */
    private DashboardInsightsData collectRawData() {
        // Revenue data
        BigDecimal revenueYesterday = orderRepository.getYesterdayRevenue();
        BigDecimal revenueLastWeek = orderRepository.getLastWeekRevenue();
        BigDecimal revenueGrowth = BigDecimal.ZERO;
        if (revenueLastWeek.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = revenueYesterday.subtract(revenueLastWeek)
                    .divide(revenueLastWeek, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        // Top products
        List<String> topProducts = productRepository.findTopSellingProductsLast7Days().stream()
                .map(row -> (String) row[0])
                .limit(3)
                .collect(Collectors.toList());
        
        // Order data
        Long cancelledOrders = orderRepository.getCancelledOrdersLast7Days();
        Long totalOrders = orderRepository.getTotalOrdersLast7Days();
        Double cancelRate = 0.0;
        if (totalOrders > 0) {
            cancelRate = (cancelledOrders.doubleValue() / totalOrders.doubleValue()) * 100;
        }
        
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        
        // Inventory data
        List<String> lowStockProducts = productRepository.findLowStockProducts(10).stream()
                .map(Product::getName)
                .limit(3)
                .collect(Collectors.toList());
        
        List<String> highStockProducts = productRepository.findHighStockProducts(100).stream()
                .map(Product::getName)
                .limit(3)
                .collect(Collectors.toList());
        
        // Review data
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Long totalReviews = reviewRepository.countByCreatedAtAfter(weekAgo);
        Long positiveReviews = reviewRepository.countByRatingGreaterThanEqualAndCreatedAtAfter(4, weekAgo);
        Double positiveRate = 0.0;
        if (totalReviews > 0) {
            positiveRate = (positiveReviews.doubleValue() / totalReviews.doubleValue()) * 100;
        }
        
        // Get recent reviews for complaints analysis
        List<Review> recentReviews = reviewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)).getContent();
        List<String> complaints = recentReviews.stream()
                .filter(r -> r.getRating() <= 2)
                .map(Review::getComment)
                .filter(comment -> comment != null && !comment.trim().isEmpty())
                .limit(3)
                .collect(Collectors.toList());
        
        return DashboardInsightsData.builder()
                .revenueYesterday(revenueYesterday)
                .revenueLastWeek(revenueLastWeek)
                .revenueGrowth(revenueGrowth)
                .topProducts(topProducts)
                .cancelRate(cancelRate)
                .cancelReasons(List.of("Giao hàng chậm", "Sản phẩm không đúng", "Thay đổi ý định")) // Mock data
                .pendingOrders(pendingOrders)
                .totalOrdersLast7Days(totalOrders)
                .lowStockProducts(lowStockProducts)
                .highStockProducts(highStockProducts)
                .reviewCount(totalReviews)
                .positiveReviewRate(positiveRate)
                .complaints(complaints)
                .build();
    }

    /**
     * Build prompt cho AI với data thực
     */
    private String buildPrompt(DashboardInsightsData data) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là chuyên gia phân tích kinh doanh cho cửa hàng hoa trực tuyến.\n");
        prompt.append("Nhiệm vụ: Phân tích dữ liệu và tạo 3-4 insights quan trọng nhất.\n\n");
        
        prompt.append("YÊU CẦU QUAN TRỌNG: Trả về ĐÚNG format JSON sau:\n");
        prompt.append("{\n");
        prompt.append("  \"insights\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"revenue|order|inventory|review\",\n");
        prompt.append("      \"icon\": \"📈|📉|⚠️|⭐|💰|📦|🚨\",\n");
        prompt.append("      \"title\": \"Tiêu đề ngắn gọn (max 50 ký tự)\",\n");
        prompt.append("      \"message\": \"Nội dung chi tiết với số liệu cụ thể\",\n");
        prompt.append("      \"severity\": \"success|warning|danger|info\",\n");
        prompt.append("      \"actionLink\": \"/admin/products hoặc /admin/orders (nếu cần)\",\n");
        prompt.append("      \"actionText\": \"Xem chi tiết|Kiểm tra ngay (nếu cần)\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        
        prompt.append("QUY TẮC:\n");
        prompt.append("1. Tối đa 4 insights, ưu tiên vấn đề QUAN TRỌNG nhất\n");
        prompt.append("2. message PHẢI có SỐ LIỆU cụ thể: %, số tiền, tên sản phẩm\n");
        prompt.append("3. severity: success (tin tốt), warning (cảnh báo), danger (khẩn cấp), info (trung lập)\n");
        prompt.append("4. Chỉ thêm actionLink/actionText nếu có hành động cụ thể admin cần làm\n");
        prompt.append("5. Ngôn ngữ: Tiếng Việt tự nhiên, dễ hiểu, chuyên nghiệp\n\n");
        
        prompt.append("DỮ LIỆU CỬA HÀNG:\n\n");
        
        prompt.append("DOANH THU:\n");
        prompt.append("- Hôm qua: ").append(data.getRevenueYesterday()).append(" VND\n");
        prompt.append("- Tuần trước (cùng thứ): ").append(data.getRevenueLastWeek()).append(" VND\n");
        prompt.append("- Thay đổi: ").append(data.getRevenueGrowth()).append("%\n");
        prompt.append("- Top sản phẩm: ").append(String.join(", ", data.getTopProducts())).append("\n\n");
        
        prompt.append("ĐỚN HÀNG (7 ngày gần đây):\n");
        prompt.append("- Tỷ lệ hủy: ").append(String.format("%.1f", data.getCancelRate())).append("%\n");
        prompt.append("- Lý do hủy chính: ").append(String.join(", ", data.getCancelReasons())).append("\n");
        prompt.append("- Đơn chờ xử lý: ").append(data.getPendingOrders()).append("\n\n");
        
        prompt.append("TỒN KHO:\n");
        prompt.append("- Sản phẩm sắp hết (< 10): ").append(String.join(", ", data.getLowStockProducts())).append("\n");
        prompt.append("- Sản phẩm tồn cao (> 100): ").append(String.join(", ", data.getHighStockProducts())).append("\n\n");
        
        prompt.append("ĐÁNH GIÁ (7 ngày gần đây):\n");
        prompt.append("- Tổng số đánh giá: ").append(data.getReviewCount()).append("\n");
        prompt.append("- Tỷ lệ tích cực (≥4 sao): ").append(String.format("%.1f", data.getPositiveReviewRate())).append("%\n");
        prompt.append("- Nội dung phàn nàn: ").append(String.join(", ", data.getComplaints())).append("\n\n");
        
        prompt.append("Hãy phân tích và trả về JSON insights.");
        
        return prompt.toString();
    }

    /**
     * Fallback insights khi AI fail
     */
    private AiInsightResponse getFallbackInsights() {
        AiInsightResponse.InsightItem fallbackItem = AiInsightResponse.InsightItem.builder()
                .type("info")
                .icon("🤖")
                .title("Đang cập nhật phân tích")
                .message("Hệ thống AI đang xử lý dữ liệu. Vui lòng quay lại sau.")
                .severity("info")
                .build();
        
        return AiInsightResponse.builder()
                .insights(List.of(fallbackItem))
                .build();
    }

    /**
     * Analyze review using AI to get sentiment and suggested replies
     * 
     * @param reviewId ID of the review to analyze
     * @return ReviewAiAnalysisResponse with sentiment and suggested replies
     */
    @Transactional
    public ReviewAiAnalysisResponse analyzeReview(Long reviewId) {
        log.info("Analyzing review {} with AI", reviewId);
        
        try {
            // 1. Get review from database
            Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
            
            // 2. Check if sentiment already exists
            if (review.getSentiment() != null) {
                log.debug("Review {} already has sentiment: {}", reviewId, review.getSentiment());
                // If sentiment exists, we can still call AI for fresh suggestions
                // or reuse existing sentiment - for now, let's call AI for suggestions
            }
            
            // 3. Build prompt and call AI
            String prompt = buildReviewAnalysisPrompt(review.getComment(), review.getRating());
            log.debug("Built prompt for AI analysis (length: {} chars)", prompt.length());
            
            // 4. Call Gemini API
            GeminiResponse response = geminiClient.generateContentWithRetry(prompt, 2);
            if (response == null || !response.isSuccessful()) {
                log.warn("Gemini API failed for review {}, using fallback", reviewId);
                return createFallbackAnalysis(review.getRating());
            }
            
            // 5. Parse JSON response
            String jsonResponse = response.getTextResponse();
            log.debug("AI response: {}", jsonResponse);
            
            ReviewAiAnalysisResponse aiResponse = objectMapper.readValue(jsonResponse, ReviewAiAnalysisResponse.class);
            
            // 6. Validate and save sentiment if not exists
            if (review.getSentiment() == null && aiResponse.getSentiment() != null) {
                review.setSentiment(aiResponse.getSentiment());
                reviewRepository.save(review);
                log.info("Saved sentiment {} for review {}", aiResponse.getSentiment(), reviewId);
            }
            
            // 7. Set sentiment label based on sentiment
            if (aiResponse.getSentiment() != null) {
                aiResponse.setSentimentLabel(getSentimentLabel(aiResponse.getSentiment()));
            }
            
            log.info("Successfully analyzed review {} with sentiment: {}", reviewId, aiResponse.getSentiment());
            return aiResponse;
            
        } catch (Exception e) {
            log.error("Error analyzing review {}: {}", reviewId, e.getMessage(), e);
            // Return fallback based on rating
            Review review = reviewRepository.findById(reviewId).orElse(null);
            Integer rating = review != null ? review.getRating() : 3; // Default to neutral
            return createFallbackAnalysis(rating);
        }
    }
    
    /**
     * Build prompt for AI review analysis
     */
    private String buildReviewAnalysisPrompt(String comment, Integer rating) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là chuyên gia CSKH cửa hàng hoa trực tuyến.\n");
        prompt.append("Phân tích đánh giá này và trả về JSON:\n\n");
        
        prompt.append("{\n");
        prompt.append("  \"sentiment\": \"POSITIVE|NEUTRAL|NEGATIVE\",\n");
        prompt.append("  \"mainIssue\": \"Tóm tắt vấn đề chính (nếu tiêu cực/trung tính)\",\n");
        prompt.append("  \"suggestedReplies\": [\n");
        prompt.append("    {\"label\": \"Lịch sự\", \"content\": \"...\"},\n");
        prompt.append("    {\"label\": \"Kèm đền bù\", \"content\": \"...\"},\n");
        prompt.append("    {\"label\": \"Chuyên nghiệp\", \"content\": \"...\"}\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        
        prompt.append("REVIEW:\n");
        prompt.append("- Rating: ").append(rating).append("/5\n");
        prompt.append("- Comment: ").append(comment != null ? comment : "Không có bình luận").append("\n\n");
        
        prompt.append("QUY TẮC:\n");
        prompt.append("1. POSITIVE: rating 4-5 và comment tốt\n");
        prompt.append("2. NEGATIVE: rating 1-2 hoặc có từ khóa phàn nàn\n");
        prompt.append("3. NEUTRAL: còn lại\n");
        prompt.append("4. suggestedReplies: 2-3 options, mỗi cái 50-80 từ\n");
        prompt.append("5. Giọng văn: Tiếng Việt, thân thiện, chuyên nghiệp\n");
        prompt.append("6. Label phải ngắn gọn: \"Lịch sự\", \"Kèm đền bù\", \"Chuyên nghiệp\", \"Xin lỗi chân thành\"\n");
        prompt.append("7. Chỉ trả về JSON, không có text khác");
        
        return prompt.toString();
    }
    
    /**
     * Create fallback analysis when AI fails
     */
    private ReviewAiAnalysisResponse createFallbackAnalysis(Integer rating) {
        String sentiment;
        String sentimentLabel;
        
        if (rating <= 2) {
            sentiment = "NEGATIVE";
            sentimentLabel = "Tiêu cực 😡";
        } else if (rating == 3) {
            sentiment = "NEUTRAL";
            sentimentLabel = "Trung tính 😐";
        } else {
            sentiment = "POSITIVE";
            sentimentLabel = "Tích cực 😊";
        }
        
        return ReviewAiAnalysisResponse.builder()
                .sentiment(sentiment)
                .sentimentLabel(sentimentLabel)
                .mainIssue("Không thể tải phân tích AI.")
                .suggestedReplies(Collections.emptyList())
                .build();
    }
    
    /**
     * Get sentiment label with emoji
     */
    private String getSentimentLabel(String sentiment) {
        switch (sentiment) {
            case "POSITIVE":
                return "Tích cực 😊";
            case "NEUTRAL":
                return "Trung tính 😐";
            case "NEGATIVE":
                return "Tiêu cực 😡";
            default:
                return "Không xác định";
        }
    }

    /**
     * Suggest voucher configuration using AI based on business objectives
     * 
     * @param objective The business objective: NEW_CUSTOMER, INCREASE_AOV, CLEAR_INVENTORY
     * @param targetProduct Optional product name for inventory clearance
     * @return VoucherSuggestionResponse with AI-generated voucher configuration
     */
    @Transactional
    public VoucherSuggestionResponse suggestVoucher(String objective, String targetProduct) {
        log.info("Generating AI voucher suggestion for objective: {}, targetProduct: {}", objective, targetProduct);
        
        try {
            // 1. Collect real business data
            VoucherSuggestionData businessData = collectVoucherBusinessData(objective, targetProduct);
            log.debug("Collected business data: AOV={}, activeVouchers={}, inventoryProducts={}", 
                businessData.getAov(), businessData.getActiveVouchers().size(), businessData.getInventoryProducts().size());
            
            // 2. Build AI prompt with real data
            String prompt = buildVoucherSuggestionPrompt(objective, targetProduct, businessData);
            log.debug("Built AI prompt for voucher suggestion");
            
            // 3. Call Gemini API
            GeminiResponse response = geminiClient.generateContentWithRetry(prompt, 2);
            if (response == null || !response.isSuccessful()) {
                log.warn("Gemini API failed for voucher suggestion, using fallback");
                return createFallbackVoucherSuggestion(objective, businessData);
            }
            
            // 4. Parse JSON response
            String jsonResponse = response.getTextResponse();
            log.debug("AI voucher response: {}", jsonResponse);
            
            VoucherSuggestionResponse aiResponse = objectMapper.readValue(jsonResponse, VoucherSuggestionResponse.class);
            
            // 5. Validate and enhance response
            validateAndEnhanceSuggestion(aiResponse, businessData);
            
            log.info("Successfully generated AI voucher suggestion: {}", aiResponse.getCode());
            return aiResponse;
            
        } catch (Exception e) {
            log.error("Error generating voucher suggestion", e);
            return createFallbackVoucherSuggestion(objective, null);
        }
    }
    
    /**
     * Collect business data for voucher suggestion
     */
    private VoucherSuggestionData collectVoucherBusinessData(String objective, String targetProduct) {
        // Calculate AOV for last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        BigDecimal aov = orderRepository.getAverageOrderValue(thirtyDaysAgo);
        if (aov == null) aov = BigDecimal.valueOf(350000); // Default fallback
        
        // Get active vouchers for conflict checking
        List<Voucher> activeVouchers = voucherRepository.findByIsActiveTrueAndExpiryDateAfter(LocalDate.now());
        
        // Get inventory data based on objective
        List<Product> inventoryProducts = Collections.emptyList();
        if ("CLEAR_INVENTORY".equals(objective)) {
            if (targetProduct != null && !targetProduct.trim().isEmpty()) {
                // Find specific product
                inventoryProducts = productRepository.findProductsByName(targetProduct.trim());
            } else {
                // Find high stock, low sales products
                inventoryProducts = productRepository.findHighStockLowSalesProducts(thirtyDaysAgo);
            }
        }
        
        return VoucherSuggestionData.builder()
                .aov(aov)
                .activeVouchers(activeVouchers)
                .inventoryProducts(inventoryProducts)
                .build();
    }
    
    /**
     * Build AI prompt for voucher suggestion
     */
    private String buildVoucherSuggestionPrompt(String objective, String targetProduct, VoucherSuggestionData data) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là chuyên gia marketing cho cửa hàng hoa trực tuyến.\n");
        prompt.append("Nhiệm vụ: Tạo cấu hình voucher tối ưu dựa trên dữ liệu kinh doanh thực tế.\n\n");
        
        prompt.append("YÊU CẦU QUAN TRỌNG: Trả về ĐÚNG format JSON sau:\n");
        prompt.append("{\n");
        prompt.append("  \"code\": \"OBJ-CAT-MMDD-XXXX\",\n");
        prompt.append("  \"discountType\": \"PERCENTAGE|FIXED\",\n");
        prompt.append("  \"discountValue\": 20.0,\n");
        prompt.append("  \"minOrderValue\": 450000.0,\n");
        prompt.append("  \"explanation\": \"Giải thích lý do đề xuất cấu hình này\",\n");
        prompt.append("  \"warnings\": [\"Cảnh báo nếu có xung đột\"],\n");
        prompt.append("  \"dataUsed\": \"Tóm tắt dữ liệu đã sử dụng\"\n");
        prompt.append("}\n\n");
        
        prompt.append("QUY TẮC:\n");
        prompt.append("1. Code format: OBJ-CAT-MMDD-XXXX (VD: AOV-FLOWER-1019-A3B2)\n");
        prompt.append("2. minOrderValue ≈ AOV * 1.25 (để tăng AOV nhưng vẫn đạt tỷ lệ chuyển đổi tốt)\n");
        prompt.append("3. discountType: PERCENTAGE cho tăng AOV, FIXED cho thu hút KH mới, PERCENTAGE cao cho đẩy tồn\n");
        prompt.append("4. discountValue: không vượt quá 50% cho PERCENTAGE, không vượt biên lợi nhuận (~30%)\n");
        prompt.append("5. explanation: ngắn gọn, có số liệu cụ thể, lý do rõ ràng\n");
        prompt.append("6. warnings: chỉ thêm nếu có xung đột với voucher đang chạy\n");
        prompt.append("7. Ngôn ngữ: Tiếng Việt tự nhiên, chuyên nghiệp\n\n");
        
        prompt.append("DỮ LIỆU KINH DOANH:\n\n");
        prompt.append("MỤC TIÊU: ").append(objective).append("\n");
        if (targetProduct != null && !targetProduct.trim().isEmpty()) {
            prompt.append("SẢN PHẨM MỤC TIÊU: ").append(targetProduct).append("\n");
        }
        prompt.append("AOV (Giá trị đơn hàng trung bình): ").append(data.getAov()).append(" VND\n");
        
        prompt.append("VOUCHER ĐANG CHẠY:\n");
        if (data.getActiveVouchers().isEmpty()) {
            prompt.append("- Không có voucher nào đang chạy\n");
        } else {
            for (Voucher voucher : data.getActiveVouchers()) {
                prompt.append("- ").append(voucher.getCode()).append(": ")
                      .append(voucher.getDiscountType()).append(" ")
                      .append(voucher.getDiscountValue())
                      .append(voucher.getDiscountType() == DiscountType.PERCENTAGE ? "%" : " VND")
                      .append(", đơn tối thiểu ").append(voucher.getMinOrderValue()).append(" VND\n");
            }
        }
        
        if ("CLEAR_INVENTORY".equals(objective)) {
            prompt.append("SẢN PHẨM TỒN KHO:\n");
            if (data.getInventoryProducts().isEmpty()) {
                prompt.append("- Không tìm thấy sản phẩm tồn kho cao\n");
            } else {
                for (Product product : data.getInventoryProducts().stream().limit(5).collect(Collectors.toList())) {
                    prompt.append("- ").append(product.getName()).append(": tồn ").append(product.getStockQuantity()).append(" sản phẩm\n");
                }
            }
        }
        
        prompt.append("\nHãy phân tích và trả về JSON voucher suggestion.");
        
        return prompt.toString();
    }
    
    /**
     * Validate and enhance AI suggestion
     */
    private void validateAndEnhanceSuggestion(VoucherSuggestionResponse suggestion, VoucherSuggestionData data) {
        // Validate discount value
        if (suggestion.getDiscountType() == DiscountType.PERCENTAGE && 
            suggestion.getDiscountValue().compareTo(BigDecimal.valueOf(50)) > 0) {
            suggestion.setDiscountValue(BigDecimal.valueOf(30)); // Cap at 30%
        }
        
        // Check for code conflicts
        if (voucherRepository.existsByCode(suggestion.getCode())) {
            // Generate new code
            String newCode = generateUniqueVoucherCode(suggestion.getCode());
            suggestion.setCode(newCode);
        }
        
        // Check for minOrderValue conflicts with active vouchers
        List<String> warnings = suggestion.getWarnings() != null ? suggestion.getWarnings() : Collections.emptyList();
        for (Voucher activeVoucher : data.getActiveVouchers()) {
            if (suggestion.getMinOrderValue().equals(activeVoucher.getMinOrderValue())) {
                warnings.add("Có voucher khác đang chạy với cùng điều kiện đơn tối thiểu: " + activeVoucher.getCode());
            }
        }
        suggestion.setWarnings(warnings);
        
        // Set dataUsed summary
        suggestion.setDataUsed(String.format("AOV: %s VND, %d voucher đang chạy, %d sản phẩm tồn kho", 
            data.getAov(), data.getActiveVouchers().size(), data.getInventoryProducts().size()));
    }
    
    /**
     * Generate unique voucher code
     */
    private String generateUniqueVoucherCode(String baseCode) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // Last 5 digits
        return baseCode.substring(0, Math.min(baseCode.length() - 4, 8)) + "-" + timestamp;
    }
    
    /**
     * Create fallback suggestion when AI fails
     */
    private VoucherSuggestionResponse createFallbackVoucherSuggestion(String objective, VoucherSuggestionData data) {
        BigDecimal aov = data != null ? data.getAov() : BigDecimal.valueOf(350000);
        BigDecimal minOrderValue = aov.multiply(BigDecimal.valueOf(1.25));
        
        String code = generateFallbackCode(objective);
        DiscountType discountType = "INCREASE_AOV".equals(objective) ? DiscountType.PERCENTAGE : DiscountType.FIXED;
        BigDecimal discountValue = "INCREASE_AOV".equals(objective) ? BigDecimal.valueOf(20) : BigDecimal.valueOf(50000);
        
        return VoucherSuggestionResponse.builder()
                .code(code)
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderValue(minOrderValue)
                .explanation("Gợi ý mặc định: AOV hiện tại " + aov + " VND. Đề xuất voucher " + 
                    discountType + " " + discountValue + 
                    (discountType == DiscountType.PERCENTAGE ? "%" : " VND") + 
                    " cho đơn từ " + minOrderValue + " VND.")
                .warnings(Collections.singletonList("Đây là gợi ý mặc định do AI không khả dụng"))
                .dataUsed("Fallback template")
                .build();
    }
    
    /**
     * Generate fallback voucher code
     */
    private String generateFallbackCode(String objective) {
        String prefix = "NEW".equals(objective) ? "NEW" : 
                       "AOV".equals(objective) ? "AOV" : "INV";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return prefix + "-FALLBACK-" + timestamp;
    }
    
    /**
     * Data class for voucher suggestion business data
     */
    @lombok.Data
    @lombok.Builder
    private static class VoucherSuggestionData {
        private BigDecimal aov;
        private List<Voucher> activeVouchers;
        private List<Product> inventoryProducts;
    }
}
