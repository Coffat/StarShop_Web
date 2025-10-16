package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/vouchers")
@RequiredArgsConstructor
@Slf4j
public class AdminVoucherController {
    
    private final VoucherService voucherService;
    
    /**
     * Get all vouchers with filters
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String search
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Apply filters
            Page<VoucherDTO> voucherPage;
            if (search != null && !search.trim().isEmpty()) {
                // Search by keyword (code, name)
                voucherPage = voucherService.searchVouchers(search.trim(), pageable);
            } else {
                // Get all vouchers with optional filters
                voucherPage = voucherService.getVouchersWithFilters(
                    pageable, type, status, fromDate
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("vouchers", voucherPage.getContent());
            response.put("currentPage", voucherPage.getNumber());
            response.put("totalItems", voucherPage.getTotalElements());
            response.put("totalPages", voucherPage.getTotalPages());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting vouchers", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get voucher by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVoucherById(@PathVariable Long id) {
        try {
            VoucherDTO voucher = voucherService.getVoucherById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("voucher", voucher);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting voucher with ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    /**
     * Get valid vouchers
     */
    @GetMapping("/valid")
    public ResponseEntity<Map<String, Object>> getValidVouchers() {
        try {
            List<VoucherDTO> vouchers = voucherService.getValidVouchers();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vouchers", vouchers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting valid vouchers", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách voucher hợp lệ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Create new voucher
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        try {
            VoucherDTO voucher = voucherService.createVoucher(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo voucher thành công");
            response.put("voucher", voucher);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating voucher", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error creating voucher", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tạo voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update voucher
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVoucherRequest request
    ) {
        try {
            VoucherDTO voucher = voucherService.updateVoucher(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật voucher thành công");
            response.put("voucher", voucher);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating voucher", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error updating voucher with ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete voucher
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVoucher(@PathVariable Long id) {
        try {
            voucherService.deleteVoucher(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa voucher thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting voucher with ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Toggle voucher status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleVoucherStatus(@PathVariable Long id) {
        try {
            VoucherDTO voucher = voucherService.toggleVoucherStatus(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái voucher thành công");
            response.put("voucher", voucher);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling voucher status with ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Validate voucher
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(
            @RequestParam String code,
            @RequestParam java.math.BigDecimal orderAmount
    ) {
        try {
            boolean isValid = voucherService.validateVoucher(code, orderAmount);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", isValid);
            if (isValid) {
                VoucherDTO voucher = voucherService.getVoucherByCode(code);
                response.put("voucher", voucher);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating voucher", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("valid", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}

