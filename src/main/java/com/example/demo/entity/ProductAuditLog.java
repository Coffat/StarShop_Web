package com.example.demo.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity for Product Audit Log
 * Tracks all changes made to products for auditing purposes
 */
@Entity
@Table(name = "product_audit_log")
public class ProductAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "action", nullable = false, length = 50)
    private String action; // INSERT, UPDATE, DELETE
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private JsonNode oldValues;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private JsonNode newValues;
    
    @Column(name = "changed_by", length = 100)
    private String changedBy;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    // Constructors
    public ProductAuditLog() {
        this.changedAt = LocalDateTime.now();
    }
    
    public ProductAuditLog(Long productId, String action, JsonNode oldValues, JsonNode newValues, String changedBy) {
        this();
        this.productId = productId;
        this.action = action;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.changedBy = changedBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public JsonNode getOldValues() {
        return oldValues;
    }
    
    public void setOldValues(JsonNode oldValues) {
        this.oldValues = oldValues;
    }
    
    public JsonNode getNewValues() {
        return newValues;
    }
    
    public void setNewValues(JsonNode newValues) {
        this.newValues = newValues;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    // Helper methods
    public String getActionDisplayName() {
        switch (action) {
            case "INSERT": return "Tạo mới";
            case "UPDATE": return "Cập nhật";
            case "DELETE": return "Xóa";
            default: return action;
        }
    }
    
    public String getActionBadgeClass() {
        switch (action) {
            case "INSERT": return "bg-green-100 text-green-800";
            case "UPDATE": return "bg-blue-100 text-blue-800";
            case "DELETE": return "bg-red-100 text-red-800";
            default: return "bg-gray-100 text-gray-800";
        }
    }
    
    public String getActionIcon() {
        switch (action) {
            case "INSERT": return "fas fa-plus";
            case "UPDATE": return "fas fa-edit";
            case "DELETE": return "fas fa-trash";
            default: return "fas fa-question";
        }
    }
    
    @Override
    public String toString() {
        return "ProductAuditLog{" +
                "id=" + id +
                ", productId=" + productId +
                ", action='" + action + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
