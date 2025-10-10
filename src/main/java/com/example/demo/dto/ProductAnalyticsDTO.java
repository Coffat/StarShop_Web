package com.example.demo.dto;

import java.math.BigDecimal;

/**
 * DTO for Product Analytics Data
 */
public class ProductAnalyticsDTO {
    private String metricName;
    private BigDecimal metricValue;
    private String metricDescription;

    // Constructors
    public ProductAnalyticsDTO() {}

    public ProductAnalyticsDTO(String metricName, BigDecimal metricValue, String metricDescription) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricDescription = metricDescription;
    }

    // Getters and Setters
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    public String getMetricDescription() {
        return metricDescription;
    }

    public void setMetricDescription(String metricDescription) {
        this.metricDescription = metricDescription;
    }

    // Helper methods for display
    public String getFormattedValue() {
        if (metricValue == null) return "0";
        
        // Format based on metric type
        if (metricName.contains("price") || metricName.contains("value")) {
            return String.format("%,.0f VND", metricValue);
        } else {
            return String.format("%,.0f", metricValue);
        }
    }

    public String getDisplayName() {
        switch (metricName) {
            case "total_products": return "Tổng sản phẩm";
            case "active_products": return "Đang bán";
            case "low_stock_products": return "Sắp hết hàng";
            case "out_of_stock_products": return "Hết hàng";
            case "total_stock_value": return "Giá trị tồn kho";
            case "avg_product_price": return "Giá trung bình";
            case "products_created_period": return "Sản phẩm mới";
            default: return metricName;
        }
    }

    @Override
    public String toString() {
        return "ProductAnalyticsDTO{" +
                "metricName='" + metricName + '\'' +
                ", metricValue=" + metricValue +
                ", metricDescription='" + metricDescription + '\'' +
                '}';
    }
}
