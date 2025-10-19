package com.example.demo.service;

import com.example.demo.client.GeminiClient;
import com.example.demo.dto.AiInsightResponse;
import com.example.demo.dto.DashboardInsightsData;
import com.example.demo.dto.gemini.GeminiResponse;
import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
}
