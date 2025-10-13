package com.example.demo.service;

import com.example.demo.dto.ShippingFeeEstimate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for AI shipping fee calculations
 * Simplified shipping fee estimation for AI chat
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiShippingService {

    private final StoreConfigService storeConfigService;

    /**
     * Calculate shipping fee for AI (simplified)
     * Returns estimated fee based on location keywords
     */
    public ShippingFeeEstimate calculateShippingFeeForAi(String toLocation) {
        log.info("AI shipping fee calculation for: {}", toLocation);
        
        if (toLocation == null || toLocation.trim().isEmpty()) {
            return ShippingFeeEstimate.error("Vui lòng cung cấp địa chỉ giao hàng");
        }

        String location = toLocation.toLowerCase().trim();
        
        try {
            // Simple location-based estimation
            BigDecimal fee;
            String estimatedTime;
            
            // Check for Ho Chi Minh City (inner city)
            if (location.contains("hcm") || location.contains("hồ chí minh") || 
                location.contains("sài gòn") || location.contains("saigon") ||
                location.contains("thủ đức") || location.contains("thu duc") ||
                location.contains("quận") || location.contains("quan")) {
                
                // Inner city HCM
                fee = new BigDecimal("20000");
                estimatedTime = "2-4 giờ";
                
            } else if (location.contains("hà nội") || location.contains("ha noi") || 
                       location.contains("hanoi")) {
                
                // Hanoi
                fee = new BigDecimal("45000");
                estimatedTime = "1-2 ngày";
                
            } else if (location.contains("đà nẵng") || location.contains("da nang") ||
                       location.contains("danang")) {
                
                // Da Nang
                fee = new BigDecimal("40000");
                estimatedTime = "1-2 ngày";
                
            } else if (location.contains("cần thơ") || location.contains("can tho")) {
                
                // Can Tho
                fee = new BigDecimal("35000");
                estimatedTime = "1 ngày";
                
            } else if (location.contains("biên hòa") || location.contains("bien hoa") ||
                       location.contains("đồng nai") || location.contains("dong nai") ||
                       location.contains("bình dương") || location.contains("binh duong")) {
                
                // Nearby provinces
                fee = new BigDecimal("25000");
                estimatedTime = "4-6 giờ";
                
            } else {
                
                // Other locations - general estimate
                fee = new BigDecimal("50000");
                estimatedTime = "2-3 ngày";
            }
            
            ShippingFeeEstimate estimate = ShippingFeeEstimate.success(fee, estimatedTime);
            estimate.setToLocation(toLocation);
            estimate.setFromLocation("TP. Thủ Đức, TP.HCM");
            
            log.info("Estimated shipping fee: {} for location: {}", fee, toLocation);
            
            return estimate;
            
        } catch (Exception e) {
            log.error("Error calculating shipping fee for AI", e);
            return ShippingFeeEstimate.error("Không thể tính phí ship lúc này");
        }
    }

    /**
     * Get shipping policy text
     */
    public String getShippingPolicy() {
        String policy = storeConfigService.getConfig("policy.shipping");
        if (policy != null) {
            return policy;
        }
        return "Miễn phí ship đơn từ 500k trong bán kính 5km. Giao hàng trong 2-4 giờ. Hỗ trợ giao toàn TP.HCM.";
    }

    /**
     * Format shipping fee estimate as text
     */
    public String formatEstimateAsText(ShippingFeeEstimate estimate) {
        if (!estimate.getSuccess()) {
            return estimate.getErrorMessage();
        }
        
        return String.format("Phí ship đến %s: **%s** (thời gian: %s)", 
            estimate.getToLocation(),
            estimate.getFormattedFee(),
            estimate.getEstimatedTime());
    }
}

