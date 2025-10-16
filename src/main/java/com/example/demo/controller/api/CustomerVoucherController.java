package com.example.demo.controller.api;

import com.example.demo.dto.VoucherDTO;
import com.example.demo.entity.enums.DiscountType;
import com.example.demo.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for customer voucher operations
 */
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Slf4j
public class CustomerVoucherController {
    
    private final VoucherService voucherService;
    
    /**
     * Get all available vouchers for customers
     * Returns only active and valid vouchers
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableVouchers() {
        try {
            log.info("Fetching available vouchers for customer");
            
            List<VoucherDTO> vouchers = voucherService.getValidVouchers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vouchers", vouchers);
            response.put("total", vouchers.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching available vouchers", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tải danh sách voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Validate and get voucher by code
     * Used when customer enters voucher code at checkout
     */
    @GetMapping("/validate/{code}")
    public ResponseEntity<Map<String, Object>> validateVoucherCode(@PathVariable String code) {
        try {
            log.info("Validating voucher code: {}", code);
            
            VoucherDTO voucher = voucherService.getVoucherByCode(code.toUpperCase());
            
            Map<String, Object> response = new HashMap<>();
            
            // Check if voucher is active
            if (!voucher.getIsActive()) {
                response.put("success", false);
                response.put("message", "Voucher này hiện không khả dụng");
                return ResponseEntity.ok(response);
            }
            
            // Check voucher status
            if (!"ACTIVE".equals(voucher.getStatus())) {
                String message = switch (voucher.getStatus()) {
                    case "EXPIRED" -> "Voucher đã hết hạn";
                    case "USED_UP" -> "Voucher đã hết lượt sử dụng";
                    case "INACTIVE" -> "Voucher hiện không khả dụng";
                    default -> "Voucher không hợp lệ";
                };
                response.put("success", false);
                response.put("message", message);
                return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("message", "Voucher hợp lệ");
            response.put("voucher", voucher);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating voucher code: {}", code, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không tìm thấy voucher này");
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Apply voucher to order
     * Calculate discount based on voucher type and order amount
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyVoucher(
            @RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            Double orderAmount = ((Number) request.get("orderAmount")).doubleValue();
            
            log.info("Applying voucher {} to order amount: {}", code, orderAmount);
            
            VoucherDTO voucher = voucherService.getValidVoucherByCode(code.toUpperCase());
            
            log.info("Voucher details: type={}, value={}, maxDiscount={}, minOrder={}", 
                voucher.getDiscountType(), voucher.getDiscountValue(), 
                voucher.getMaxDiscountAmount(), voucher.getMinOrderValue());
            
            Map<String, Object> response = new HashMap<>();
            
            // Check minimum order value
            if (voucher.getMinOrderValue() != null && orderAmount < voucher.getMinOrderValue().doubleValue()) {
                response.put("success", false);
                response.put("message", "Đơn hàng tối thiểu " + formatCurrency(voucher.getMinOrderValue().doubleValue()));
                return ResponseEntity.ok(response);
            }
            
            // Calculate discount
            double discount = 0;
            if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
                discount = orderAmount * (voucher.getDiscountValue().doubleValue() / 100.0);
                log.info("Percentage discount calculated: {} ({}% of {})", discount, voucher.getDiscountValue(), orderAmount);
                
                // Apply max discount if set
                if (voucher.getMaxDiscountAmount() != null && discount > voucher.getMaxDiscountAmount().doubleValue()) {
                    discount = voucher.getMaxDiscountAmount().doubleValue();
                    log.info("Applied max discount limit: {}", discount);
                }
            } else if (voucher.getDiscountType() == DiscountType.FIXED) {
                discount = voucher.getDiscountValue().doubleValue();
                log.info("Fixed discount applied: {}", discount);
            }
            
            // Ensure discount doesn't exceed order amount
            discount = Math.min(discount, orderAmount);
            log.info("Final discount amount: {}", discount);
            
            double finalAmount = orderAmount - discount;
            
            response.put("success", true);
            response.put("message", "Áp dụng voucher thành công");
            response.put("voucher", voucher);
            response.put("discount", discount);
            response.put("finalAmount", finalAmount);
            response.put("originalAmount", orderAmount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error applying voucher", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi áp dụng voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Format currency for display
     */
    private String formatCurrency(Double amount) {
        if (amount == null) return "0₫";
        return String.format("%,.0f₫", amount);
    }
    
}

