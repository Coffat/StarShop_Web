package com.example.demo.controller;

import com.example.demo.dto.CreateVoucherRequest;
import com.example.demo.dto.MarketingCampaignRequest;
import com.example.demo.dto.VoucherDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.DiscountType;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AiPromptService;
import com.example.demo.service.CustomerSegmentationService;
import com.example.demo.service.EmailService;
import com.example.demo.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "📢 Admin Marketing", description = "Marketing campaigns & customer segmentation APIs")
@RestController
@RequestMapping("/admin/api/marketing")
@RequiredArgsConstructor
@Slf4j
public class AdminMarketingController {
    
    private final UserRepository userRepository;
    private final VoucherService voucherService;
    private final AiPromptService aiPromptService;
    private final EmailService emailService;
    private final CustomerSegmentationService segmentationService;
    
    /**
     * Gửi chiến dịch email marketing cho 1 phân khúc khách hàng
     */
    @Operation(
        summary = "Send marketing campaign",
        description = "Send email marketing campaign to customer segment with voucher"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Campaign sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid campaign data"),
        @ApiResponse(responseCode = "500", description = "Error sending campaign")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/send-campaign")
    public ResponseEntity<Map<String, Object>> sendCampaign(
            @RequestBody MarketingCampaignRequest request) {
        
        try {
            log.info("📧 Bắt đầu chiến dịch marketing cho segment: {}", request.getSegment());
            
            // 1. Lấy danh sách khách hàng theo segment
            List<User> customers = userRepository.findByCustomerSegment(request.getSegment());
            
            if (customers.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không có khách hàng nào trong phân khúc này"
                ));
            }
            
            // 2. Tạo voucher chung cho campaign
            CreateVoucherRequest voucherReq = new CreateVoucherRequest();
            voucherReq.setCode(request.getVoucherCode());
            voucherReq.setName("Chiến dịch " + request.getCampaignName());
            voucherReq.setDiscountType(DiscountType.valueOf(request.getDiscountType()));
            voucherReq.setDiscountValue(request.getDiscountValue());
            voucherReq.setExpiryDate(LocalDate.now().plusDays(30));
            voucherReq.setIsActive(true);
            
            VoucherDTO voucher = voucherService.createVoucher(voucherReq);
            log.info("✅ Voucher created: {}", voucher.getCode());
            
            // 3. Gửi email cho từng khách
            int successCount = 0;
            int failCount = 0;
            
            for (User customer : customers) {
                try {
                    // Generate email content với AI
                    Map<String, String> emailContent = aiPromptService.generateMarketingEmail(
                        request.getSegment(),
                        customer.getFirstname(),
                        voucher.getCode(),
                        voucher.getExpiryDate().toString()
                    );
                    
                    // Thay thế placeholders
                    String body = emailContent.get("body")
                        .replace("{{name}}", customer.getFirstname())
                        .replace("{{voucher}}", voucher.getCode())
                        .replace("{{expiry}}", voucher.getExpiryDate().toString());
                    
                    emailService.sendMarketingEmail(
                        customer.getEmail(),
                        customer.getFirstname(),
                        emailContent.get("subject"),
                        body
                    );
                    
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("Failed to send to {}: {}", customer.getEmail(), e.getMessage());
                    failCount++;
                }
            }
            
            log.info("✅ Campaign completed: {} success, {} failed", successCount, failCount);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Đã gửi %d email thành công", successCount),
                "recipientCount", customers.size(),
                "emailsSent", successCount,
                "emailsFailed", failCount,
                "voucherCode", voucher.getCode()
            ));
            
        } catch (Exception e) {
            log.error("Error sending campaign", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi khi gửi chiến dịch: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy thống kê số lượng khách hàng theo từng phân khúc
     */
    @Operation(
        summary = "Get customer segment statistics",
        description = "Retrieve statistics of customer count by segment (VIP, NEW, AT_RISK)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Segment statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Error retrieving statistics")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/segment-stats")
    public ResponseEntity<Map<String, Object>> getSegmentStats() {
        try {
            Long vipCount = userRepository.countByCustomerSegment("VIP");
            Long newCount = userRepository.countByCustomerSegment("NEW");
            Long atRiskCount = userRepository.countByCustomerSegment("AT_RISK");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", Map.of(
                    "vip", vipCount != null ? vipCount : 0,
                    "new", newCount != null ? newCount : 0,
                    "atRisk", atRiskCount != null ? atRiskCount : 0
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching segment stats", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Manual trigger để admin chạy phân khúc ngay (không cần đợi đến đêm)
     */
    @Operation(
        summary = "Trigger customer segmentation",
        description = "Manually trigger customer segmentation process (normally runs at night)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Segmentation triggered successfully"),
        @ApiResponse(responseCode = "500", description = "Error triggering segmentation")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/trigger-segmentation")
    public ResponseEntity<Map<String, Object>> triggerSegmentation() {
        try {
            log.info("🔄 Manual segmentation triggered by admin");
            int updatedCount = segmentationService.manualSegment();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Đã phân khúc %d khách hàng", updatedCount)
            ));
        } catch (Exception e) {
            log.error("Error triggering segmentation", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            ));
        }
    }
}
