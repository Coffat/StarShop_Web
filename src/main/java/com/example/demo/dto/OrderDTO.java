package com.example.demo.dto;

import com.example.demo.entity.Order;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.entity.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Order entity
 * Represents customer order information
 */
public class OrderDTO {
    
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private Long deliveryUnitId;
    private String deliveryUnitName;
    private BigDecimal deliveryFee;
    private Long voucherId;
    private String voucherCode;
    private BigDecimal discountAmount;
    private Long addressId;
    private String addressDetails;
    private PaymentMethod paymentMethod;
    private String notes;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public OrderDTO() {
    }
    
    public OrderDTO(Long id, Long userId, String userFullName, String userEmail,
                    BigDecimal totalAmount, OrderStatus status, LocalDateTime orderDate,
                    Long deliveryUnitId, String deliveryUnitName, BigDecimal deliveryFee,
                    Long voucherId, String voucherCode, BigDecimal discountAmount,
                    Long addressId, String addressDetails, PaymentMethod paymentMethod,
                    String notes, List<OrderItemDTO> orderItems,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryUnitId = deliveryUnitId;
        this.deliveryUnitName = deliveryUnitName;
        this.deliveryFee = deliveryFee;
        this.voucherId = voucherId;
        this.voucherCode = voucherCode;
        this.discountAmount = discountAmount;
        this.addressId = addressId;
        this.addressDetails = addressDetails;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Static factory method to create from Order entity
    public static OrderDTO fromOrder(Order order) {
        if (order == null) {
            return null;
        }
        
        List<OrderItemDTO> itemDTOs = order.getOrderItems() != null ?
            order.getOrderItems().stream()
                .map(OrderItemDTO::fromOrderItem)
                .collect(Collectors.toList()) :
            new java.util.ArrayList<>();
        
        return new OrderDTO(
            order.getId(),
            order.getUser() != null ? order.getUser().getId() : null,
            order.getUser() != null ? order.getUser().getFullName() : null,
            order.getUser() != null ? order.getUser().getEmail() : null,
            order.getTotalAmount(),
            order.getStatus(),
            order.getOrderDate(),
            order.getDeliveryUnit() != null ? order.getDeliveryUnit().getId() : null,
            order.getDeliveryUnit() != null ? order.getDeliveryUnit().getName() : null,
            order.getDeliveryUnit() != null ? order.getDeliveryUnit().getFee() : BigDecimal.ZERO,
            order.getVoucher() != null ? order.getVoucher().getId() : null,
            order.getVoucher() != null ? order.getVoucher().getCode() : null,
            order.getVoucher() != null ? order.getVoucher().calculateDiscount(order.getTotalAmount()) : BigDecimal.ZERO,
            order.getAddress() != null ? order.getAddress().getId() : null,
            order.getAddress() != null ? order.getAddress().getFullAddress() : null,
            order.getPaymentMethod(),
            order.getNotes(),
            itemDTOs,
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
    
    // Helper methods
    public String getStatusDisplayName() {
        if (status == null) return "Không xác định";
        
        switch (status) {
            case PENDING: return "Chờ xử lý";
            case PROCESSING: return "Đang xử lý";
            case SHIPPED: return "Đang giao hàng";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
    
    public String getPaymentMethodDisplayName() {
        if (paymentMethod == null) return "Không xác định";
        
        switch (paymentMethod) {
            case COD: return "Thanh toán khi nhận hàng";
            case MOMO: return "Ví MoMo";
            case BANK_TRANSFER: return "Chuyển khoản ngân hàng";
            case CREDIT_CARD: return "Thẻ tín dụng";
            default: return paymentMethod.name();
        }
    }
    
    public BigDecimal getSubtotal() {
        return orderItems != null ?
            orderItems.stream()
                .map(OrderItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add) :
            BigDecimal.ZERO;
    }
    
    public int getTotalItems() {
        return orderItems != null ?
            orderItems.stream()
                .mapToInt(OrderItemDTO::getQuantity)
                .sum() : 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserFullName() {
        return userFullName;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public Long getDeliveryUnitId() {
        return deliveryUnitId;
    }
    
    public void setDeliveryUnitId(Long deliveryUnitId) {
        this.deliveryUnitId = deliveryUnitId;
    }
    
    public String getDeliveryUnitName() {
        return deliveryUnitName;
    }
    
    public void setDeliveryUnitName(String deliveryUnitName) {
        this.deliveryUnitName = deliveryUnitName;
    }
    
    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }
    
    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
    
    public Long getVoucherId() {
        return voucherId;
    }
    
    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }
    
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public Long getAddressId() {
        return addressId;
    }
    
    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
    
    public String getAddressDetails() {
        return addressDetails;
    }
    
    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
