package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Voucher;
import com.example.demo.repository.VoucherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VoucherService {
    
    private final VoucherRepository voucherRepository;
    
    /**
     * Get all vouchers
     */
    @Transactional(readOnly = true)
    public List<VoucherDTO> getAllVouchers() {
        return voucherRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get vouchers with pagination
     */
    @Transactional(readOnly = true)
    public Page<VoucherDTO> getVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Get voucher by ID
     */
    @Transactional(readOnly = true)
    public VoucherDTO getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher với ID: " + id));
        return convertToDTO(voucher);
    }
    
    /**
     * Get voucher by code
     */
    @Transactional(readOnly = true)
    public VoucherDTO getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher với mã: " + code));
        return convertToDTO(voucher);
    }
    
    /**
     * Get all valid vouchers
     */
    @Transactional(readOnly = true)
    public List<VoucherDTO> getValidVouchers() {
        return voucherRepository.findValidVouchers(LocalDate.now()).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Create new voucher
     */
    public VoucherDTO createVoucher(CreateVoucherRequest request) {
        log.info("Creating new voucher with code: {}", request.getCode());
        
        // Validate code uniqueness
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại");
        }
        
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setExpiryDate(request.getExpiryDate());
        voucher.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : java.math.BigDecimal.ZERO);
        voucher.setMaxUses(request.getMaxUses());
        voucher.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        Voucher savedVoucher = voucherRepository.save(voucher);
        log.info("Voucher created successfully with ID: {}", savedVoucher.getId());
        
        return convertToDTO(savedVoucher);
    }
    
    /**
     * Update voucher
     */
    public VoucherDTO updateVoucher(Long id, UpdateVoucherRequest request) {
        log.info("Updating voucher with ID: {}", id);
        
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher với ID: " + id));
        
        // Update fields if provided
        if (request.getName() != null) {
            voucher.setName(request.getName());
        }
        if (request.getDescription() != null) {
            voucher.setDescription(request.getDescription());
        }
        if (request.getDiscountType() != null) {
            voucher.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            voucher.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMaxDiscountAmount() != null) {
            voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getExpiryDate() != null) {
            voucher.setExpiryDate(request.getExpiryDate());
        }
        if (request.getMinOrderValue() != null) {
            voucher.setMinOrderValue(request.getMinOrderValue());
        }
        if (request.getMaxUses() != null) {
            voucher.setMaxUses(request.getMaxUses());
        }
        if (request.getIsActive() != null) {
            voucher.setIsActive(request.getIsActive());
        }
        
        Voucher updatedVoucher = voucherRepository.save(voucher);
        log.info("Voucher updated successfully with ID: {}", id);
        
        return convertToDTO(updatedVoucher);
    }
    
    /**
     * Delete voucher
     */
    public void deleteVoucher(Long id) {
        log.info("Deleting voucher with ID: {}", id);
        
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher với ID: " + id));
        
        voucherRepository.delete(voucher);
        log.info("Voucher deleted successfully with ID: {}", id);
    }
    
    /**
     * Toggle voucher active status
     */
    public VoucherDTO toggleVoucherStatus(Long id) {
        log.info("Toggling status for voucher with ID: {}", id);
        
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher với ID: " + id));
        
        voucher.setIsActive(!voucher.getIsActive());
        
        Voucher updatedVoucher = voucherRepository.save(voucher);
        log.info("Voucher status toggled successfully with ID: {}", id);
        
        return convertToDTO(updatedVoucher);
    }
    
    /**
     * Validate voucher for an order
     */
    @Transactional(readOnly = true)
    public boolean validateVoucher(String code, java.math.BigDecimal orderAmount) {
        Voucher voucher = voucherRepository.findValidVoucherByCode(code, LocalDate.now())
            .orElse(null);
        
        if (voucher == null) {
            return false;
        }
        
        return voucher.canApplyToOrder(orderAmount);
    }
    
    /**
     * Convert Voucher entity to VoucherDTO
     */
    private VoucherDTO convertToDTO(Voucher voucher) {
        String status;
        if (voucher.getExpiryDate().isBefore(LocalDate.now())) {
            status = "EXPIRED";
        } else if (voucher.getMaxUses() != null && voucher.getUses() >= voucher.getMaxUses()) {
            status = "USED_UP";
        } else if (voucher.getIsActive() != null && !voucher.getIsActive()) {
            status = "INACTIVE";
        } else {
            status = "ACTIVE";
        }
        
        Double usagePercentage = null;
        if (voucher.getMaxUses() != null && voucher.getMaxUses() > 0) {
            usagePercentage = (voucher.getUses().doubleValue() / voucher.getMaxUses().doubleValue()) * 100;
        }
        
        // Count times used in orders (could be optimized with a query)
        Long timesUsedInOrders = (long) voucher.getOrders().size();
        java.math.BigDecimal totalOrderValue = voucher.getOrders().stream()
            .map(order -> order.getTotalAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        return VoucherDTO.builder()
            .id(voucher.getId())
            .code(voucher.getCode())
            .name(voucher.getName())
            .description(voucher.getDescription())
            .discountType(voucher.getDiscountType())
            .discountValue(voucher.getDiscountValue())
            .maxDiscountAmount(voucher.getMaxDiscountAmount())
            .minOrderValue(voucher.getMinOrderValue())
            .expiryDate(voucher.getExpiryDate())
            .maxUses(voucher.getMaxUses())
            .uses(voucher.getUses())
            .isActive(voucher.getIsActive())
            .createdAt(voucher.getCreatedAt())
            .status(status)
            .usagePercentage(usagePercentage)
            .timesUsedInOrders(timesUsedInOrders)
            .totalOrderValue(totalOrderValue)
            .build();
    }
}

