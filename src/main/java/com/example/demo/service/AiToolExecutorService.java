package com.example.demo.service;

import com.example.demo.dto.AiAnalysisResult;
import com.example.demo.dto.ProductSuggestionDTO;
import com.example.demo.dto.PromotionSummaryDTO;
import com.example.demo.dto.ShippingFeeEstimate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for executing AI tool requests
 * Handles product search, shipping fee, promotions, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiToolExecutorService {

    private final ProductService productService;
    private final VoucherService voucherService;
    private final StoreConfigService storeConfigService;
    private final AiShippingService aiShippingService;

    /**
     * Execute all tool requests from AI analysis
     */
    public String executeTools(AiAnalysisResult analysis) {
        if (!analysis.hasToolRequests()) {
            return "";
        }

        StringBuilder results = new StringBuilder();
        
        for (AiAnalysisResult.ToolRequest toolRequest : analysis.getToolRequests()) {
            String toolName = toolRequest.getName();
            Map<String, Object> args = toolRequest.getArgs();
            
            log.info("Executing tool: {} with args: {}", toolName, args);
            
            try {
                String result = executeTool(toolName, args);
                if (result != null && !result.isEmpty()) {
                    results.append(result).append("\n\n");
                }
            } catch (Exception e) {
                log.error("Error executing tool: {}", toolName, e);
            }
        }
        
        return results.toString();
    }

    /**
     * Execute single tool
     */
    private String executeTool(String toolName, Map<String, Object> args) {
        switch (toolName.toLowerCase()) {
            case "product_search":
                return executeProductSearch(args);
            case "shipping_fee":
                return executeShippingFee(args);
            case "promotion_lookup":
                return executePromotionLookup();
            case "store_info":
                return executeStoreInfo();
            default:
                log.warn("Unknown tool: {}", toolName);
                return null;
        }
    }

    /**
     * Execute product search tool
     */
    private String executeProductSearch(Map<String, Object> args) {
        try {
            String query = (String) args.get("query");
            Object priceMaxObj = args.get("price_max");
            BigDecimal priceMax = null;
            
            if (priceMaxObj != null) {
                if (priceMaxObj instanceof Number) {
                    priceMax = BigDecimal.valueOf(((Number) priceMaxObj).doubleValue());
                } else if (priceMaxObj instanceof String) {
                    priceMax = new BigDecimal((String) priceMaxObj);
                }
            }
            
            log.info("Searching products: query={}, priceMax={}", query, priceMax);
            
            // Search products - get more results for AI to analyze
            List<ProductSuggestionDTO> products = searchProductsForAi(query, priceMax, 5);
            
            if (products.isEmpty()) {
                // Try broader search without the specific query
                log.info("No exact match, trying broader search");
                products = searchProductsForAi("", priceMax, 5);
            }
            
            if (products.isEmpty()) {
                return "TOOL_RESULT: Không tìm thấy sản phẩm nào trong hệ thống.";
            }
            
            // Return structured data for AI to analyze and present smartly
            StringBuilder result = new StringBuilder();
            result.append("TOOL_RESULT: Tìm thấy ").append(products.size()).append(" sản phẩm:\n\n");
            
            for (ProductSuggestionDTO product : products) {
                result.append("---\n");
                result.append("ID: ").append(product.getId()).append("\n");
                result.append("Tên: ").append(product.getName()).append("\n");
                result.append("Giá: ").append(product.getFormattedPrice()).append("\n");
                
                if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                    result.append("Mô tả: ").append(product.getDescription()).append("\n");
                }
                
                if (product.getCatalogName() != null) {
                    result.append("Danh mục: ").append(product.getCatalogName()).append("\n");
                }
                
                if (product.getImageUrl() != null) {
                    result.append("Hình ảnh: ![").append(product.getName())
                          .append("](").append(product.getFullImageUrl()).append(")\n");
                }
                
                if (product.getStockQuantity() != null) {
                    result.append("Số lượng: ").append(product.getStockQuantity()).append("\n");
                }
                
                result.append("\n");
            }
            
            result.append("HƯỚNG DẪN: Hãy phân tích danh sách trên và tư vấn 2-3 sản phẩm phù hợp NHẤT với yêu cầu '")
                  .append(query).append("'. Nếu không khớp chính xác, hãy giải thích khéo léo và gợi ý sản phẩm thay thế phù hợp.\n");
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("Error in product search tool", e);
            return "Lỗi khi tìm kiếm sản phẩm.";
        }
    }

    /**
     * Search products for AI
     */
    private List<ProductSuggestionDTO> searchProductsForAi(String query, BigDecimal maxPrice, int limit) {
        return productService.searchForAi(query, maxPrice, limit);
    }

    /**
     * Execute shipping fee tool
     */
    private String executeShippingFee(Map<String, Object> args) {
        try {
            String toLocation = (String) args.get("to_location");
            
            if (toLocation == null || toLocation.trim().isEmpty()) {
                return "Bạn cho mình biết địa chỉ giao hàng để tính phí ship nhé!";
            }
            
            log.info("Calculating shipping fee to: {}", toLocation);
            
            ShippingFeeEstimate estimate = aiShippingService.calculateShippingFeeForAi(toLocation);
            
            if (!estimate.getSuccess()) {
                return estimate.getErrorMessage();
            }
            
            return aiShippingService.formatEstimateAsText(estimate);
            
        } catch (Exception e) {
            log.error("Error in shipping fee tool", e);
            return "Không thể tính phí ship lúc này.";
        }
    }

    /**
     * Execute promotion lookup tool
     */
    private String executePromotionLookup() {
        try {
            log.info("Looking up active promotions");
            
            List<PromotionSummaryDTO> promotions = voucherService.getActivePromotionsForAi();
            
            if (promotions.isEmpty()) {
                return "Hiện tại chưa có chương trình khuyến mãi nào. Bạn theo dõi fanpage để cập nhật ưu đãi mới nhất nhé!";
            }
            
            StringBuilder result = new StringBuilder();
            result.append("**Khuyến mãi hiện tại:**\n\n");
            
            for (PromotionSummaryDTO promo : promotions) {
                result.append(String.format("🎁 **%s** - Mã: `%s`\n", 
                    promo.getName(), promo.getCode()));
                result.append(String.format("   %s\n", promo.getFormattedDiscount()));
                result.append(String.format("   %s\n", promo.getFormattedMinOrder()));
                result.append(String.format("   %s\n\n", promo.getExpiryText()));
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("Error in promotion lookup tool", e);
            return "Không thể tra cứu khuyến mãi lúc này.";
        }
    }

    /**
     * Execute store info tool
     */
    private String executeStoreInfo() {
        try {
            log.info("Getting store information");
            
            String storeInfo = storeConfigService.getStoreInfoText();
            String policies = storeConfigService.getPoliciesText();
            
            return storeInfo + "\n" + policies;
            
        } catch (Exception e) {
            log.error("Error in store info tool", e);
            return "Không thể lấy thông tin cửa hàng lúc này.";
        }
    }

    /**
     * Enhance AI reply with tool execution results
     */
    public String enhanceReplyWithToolResults(AiAnalysisResult analysis) {
        String baseReply = analysis.getReply();
        String toolResults = executeTools(analysis);
        
        if (toolResults.isEmpty()) {
            return baseReply;
        }
        
        // Combine AI reply with tool results
        return baseReply + "\n\n" + toolResults;
    }
}

