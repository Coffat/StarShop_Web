package com.example.demo.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

/**
 * DTO for Product Audit Log
 */
public class ProductAuditLogDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String action;
    private JsonNode oldValues;
    private JsonNode newValues;
    private String changedBy;
    private LocalDateTime changedAt;
    private String ipAddress;
    private String userAgent;
    private String actionDescription;

    // Constructors
    public ProductAuditLogDTO() {}

    public ProductAuditLogDTO(Long id, Long productId, String productName, String action, 
                             JsonNode oldValues, JsonNode newValues, String changedBy, 
                             LocalDateTime changedAt, String actionDescription) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.action = action;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.actionDescription = actionDescription;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    // Helper methods
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

    public String getDisplayAction() {
        switch (action) {
            case "INSERT": return "Tạo mới";
            case "UPDATE": return "Cập nhật";
            case "DELETE": return "Xóa";
            default: return action;
        }
    }

    @Override
    public String toString() {
        return "ProductAuditLogDTO{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", action='" + action + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
