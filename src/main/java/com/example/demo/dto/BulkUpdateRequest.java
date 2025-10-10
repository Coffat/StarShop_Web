package com.example.demo.dto;

import java.util.List;

/**
 * DTO for Bulk Update Operations
 */
public class BulkUpdateRequest {
    
    /**
     * Bulk Status Update Request
     */
    public static class BulkStatusUpdate {
        private List<Long> productIds;
        private String newStatus;
        private String reason;

        // Constructors
        public BulkStatusUpdate() {}

        public BulkStatusUpdate(List<Long> productIds, String newStatus, String reason) {
            this.productIds = productIds;
            this.newStatus = newStatus;
            this.reason = reason;
        }

        // Getters and Setters
        public List<Long> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<Long> productIds) {
            this.productIds = productIds;
        }

        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * Bulk Stock Update Request
     */
    public static class BulkStockUpdate {
        private List<StockUpdateItem> updates;

        // Constructors
        public BulkStockUpdate() {}

        public BulkStockUpdate(List<StockUpdateItem> updates) {
            this.updates = updates;
        }

        // Getters and Setters
        public List<StockUpdateItem> getUpdates() {
            return updates;
        }

        public void setUpdates(List<StockUpdateItem> updates) {
            this.updates = updates;
        }
    }

    /**
     * Stock Update Item
     */
    public static class StockUpdateItem {
        private Long id;
        private Integer stock;
        private String reason;

        // Constructors
        public StockUpdateItem() {}

        public StockUpdateItem(Long id, Integer stock) {
            this.id = id;
            this.stock = stock;
        }

        public StockUpdateItem(Long id, Integer stock, String reason) {
            this.id = id;
            this.stock = stock;
            this.reason = reason;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * Bulk Update Response
     */
    public static class BulkUpdateResponse {
        private Integer updatedCount;
        private List<Long> failedIds;
        private List<String> errors;
        private String message;

        // Constructors
        public BulkUpdateResponse() {}

        public BulkUpdateResponse(Integer updatedCount, List<Long> failedIds) {
            this.updatedCount = updatedCount;
            this.failedIds = failedIds;
        }

        public BulkUpdateResponse(Integer updatedCount, List<Long> failedIds, String message) {
            this.updatedCount = updatedCount;
            this.failedIds = failedIds;
            this.message = message;
        }

        // Getters and Setters
        public Integer getUpdatedCount() {
            return updatedCount;
        }

        public void setUpdatedCount(Integer updatedCount) {
            this.updatedCount = updatedCount;
        }

        public List<Long> getFailedIds() {
            return failedIds;
        }

        public void setFailedIds(List<Long> failedIds) {
            this.failedIds = failedIds;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        // Helper methods
        public boolean hasFailures() {
            return failedIds != null && !failedIds.isEmpty();
        }

        public String getSummary() {
            if (hasFailures()) {
                return String.format("Cập nhật thành công %d/%d sản phẩm", 
                    updatedCount, updatedCount + failedIds.size());
            } else {
                return String.format("Cập nhật thành công %d sản phẩm", updatedCount);
            }
        }
    }
}
