package com.example.demo.service;

import com.example.demo.dto.ProductAnalyticsDTO;
import com.example.demo.dto.ProductAuditLogDTO;
import com.example.demo.dto.BulkUpdateRequest;
import com.example.demo.entity.ProductAuditLog;
import com.example.demo.repository.ProductAuditLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Product Analytics and Advanced Operations
 */
@Service
@Transactional
public class ProductAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(ProductAnalyticsService.class);

    @Autowired
    private ProductAuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Get comprehensive product analytics
     */
    public List<ProductAnalyticsDTO> getProductAnalytics(LocalDate startDate, LocalDate endDate) {
        log.info("Getting product analytics from {} to {}", startDate, endDate);
        
        try {
            String sql = "SELECT * FROM get_product_analytics(?, ?)";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, startDate, endDate);
            List<ProductAnalyticsDTO> analytics = new ArrayList<>();
            
            for (Map<String, Object> row : results) {
                ProductAnalyticsDTO dto = new ProductAnalyticsDTO();
                dto.setMetricName((String) row.get("metric_name"));
                dto.setMetricValue((BigDecimal) row.get("metric_value"));
                dto.setMetricDescription((String) row.get("metric_description"));
                analytics.add(dto);
            }
            
            log.info("Retrieved {} analytics metrics", analytics.size());
            return analytics;
            
        } catch (Exception e) {
            log.error("Error getting product analytics: {}", e.getMessage(), e);
            return getBasicAnalytics(); // Fallback to basic analytics
        }
    }

    /**
     * Get basic analytics as fallback
     */
    private List<ProductAnalyticsDTO> getBasicAnalytics() {
        List<ProductAnalyticsDTO> analytics = new ArrayList<>();
        
        try {
            // Total products
            Long totalProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Long.class);
            analytics.add(new ProductAnalyticsDTO("total_products", 
                BigDecimal.valueOf(totalProducts != null ? totalProducts : 0), 
                "Total number of products"));
            
            // Active products
            Long activeProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'", Long.class);
            analytics.add(new ProductAnalyticsDTO("active_products", 
                BigDecimal.valueOf(activeProducts != null ? activeProducts : 0), 
                "Number of active products"));
            
            // Low stock products
            Long lowStockProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE stock_quantity < 10 AND stock_quantity > 0", Long.class);
            analytics.add(new ProductAnalyticsDTO("low_stock_products", 
                BigDecimal.valueOf(lowStockProducts != null ? lowStockProducts : 0), 
                "Products with stock < 10"));
            
            // Out of stock products
            Long outOfStockProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE stock_quantity = 0", Long.class);
            analytics.add(new ProductAnalyticsDTO("out_of_stock_products", 
                BigDecimal.valueOf(outOfStockProducts != null ? outOfStockProducts : 0), 
                "Products with zero stock"));
            
        } catch (Exception e) {
            log.error("Error getting basic analytics: {}", e.getMessage(), e);
        }
        
        return analytics;
    }

    /**
     * Get product audit logs with pagination
     */
    public Page<ProductAuditLogDTO> getProductAuditLogs(Pageable pageable) {
        log.info("Getting product audit logs, page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<ProductAuditLog> auditLogs = auditLogRepository.findAllOrderByChangedAtDesc(pageable);
            List<ProductAuditLogDTO> auditLogDTOs = auditLogs.getContent().stream()
                .map(this::convertToAuditLogDTO)
                .toList();
            
            return new PageImpl<>(auditLogDTOs, pageable, auditLogs.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error getting audit logs: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    /**
     * Get audit logs for specific product
     */
    public Page<ProductAuditLogDTO> getProductAuditLogs(Long productId, Pageable pageable) {
        log.info("Getting audit logs for product ID: {}", productId);
        
        try {
            Page<ProductAuditLog> auditLogs = auditLogRepository.findByProductIdOrderByChangedAtDesc(productId, pageable);
            List<ProductAuditLogDTO> auditLogDTOs = auditLogs.getContent().stream()
                .map(this::convertToAuditLogDTO)
                .toList();
            
            return new PageImpl<>(auditLogDTOs, pageable, auditLogs.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error getting audit logs for product {}: {}", productId, e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    /**
     * Get recent audit logs
     */
    public List<ProductAuditLogDTO> getRecentAuditLogs(int limit) {
        log.info("Getting {} recent audit logs", limit);
        
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            Pageable pageable = PageRequest.of(0, limit);
            List<ProductAuditLog> auditLogs = auditLogRepository.findRecentAuditLogs(since, pageable);
            
            return auditLogs.stream()
                .map(this::convertToAuditLogDTO)
                .toList();
            
        } catch (Exception e) {
            log.error("Error getting recent audit logs: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Bulk update product status
     */
    public BulkUpdateRequest.BulkUpdateResponse bulkUpdateProductStatus(
            BulkUpdateRequest.BulkStatusUpdate request) {
        log.info("Bulk updating status for {} products to {}", 
            request.getProductIds().size(), request.getNewStatus());
        
        try {
            // Convert List<Long> to PostgreSQL array format
            Long[] productIds = request.getProductIds().toArray(new Long[0]);
            
            String sql = "SELECT * FROM bulk_update_product_status(?, ?, ?)";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, 
                productIds, request.getNewStatus(), "admin"); // TODO: Get actual user
            
            if (!results.isEmpty()) {
                Map<String, Object> result = results.get(0);
                Integer updatedCount = (Integer) result.get("updated_count");
                
                // Handle failed_ids array - PostgreSQL returns it as a string array
                Object failedIdsObj = result.get("failed_ids");
                List<Long> failedIds = new ArrayList<>();
                
                if (failedIdsObj instanceof Object[]) {
                    Object[] failedArray = (Object[]) failedIdsObj;
                    for (Object id : failedArray) {
                        if (id instanceof Number) {
                            failedIds.add(((Number) id).longValue());
                        }
                    }
                }
                
                BulkUpdateRequest.BulkUpdateResponse response = 
                    new BulkUpdateRequest.BulkUpdateResponse(updatedCount, failedIds);
                response.setMessage(response.getSummary());
                
                log.info("Bulk status update completed: {} updated, {} failed", 
                    updatedCount, failedIds.size());
                
                return response;
            }
            
        } catch (Exception e) {
            log.error("Error in bulk status update: {}", e.getMessage(), e);
        }
        
        // Return error response
        BulkUpdateRequest.BulkUpdateResponse errorResponse = 
            new BulkUpdateRequest.BulkUpdateResponse(0, request.getProductIds());
        errorResponse.setMessage("Có lỗi xảy ra khi cập nhật trạng thái sản phẩm");
        return errorResponse;
    }

    /**
     * Bulk update stock quantities
     */
    public BulkUpdateRequest.BulkUpdateResponse bulkUpdateStock(
            BulkUpdateRequest.BulkStockUpdate request) {
        log.info("Bulk updating stock for {} products", request.getUpdates().size());
        
        try {
            // Convert to JSON format for PostgreSQL function
            List<Map<String, Object>> updates = new ArrayList<>();
            for (BulkUpdateRequest.StockUpdateItem item : request.getUpdates()) {
                Map<String, Object> update = new HashMap<>();
                update.put("id", item.getId());
                update.put("stock", item.getStock());
                updates.add(update);
            }
            
            String updatesJson = objectMapper.writeValueAsString(updates);
            
            String sql = "SELECT * FROM bulk_update_stock(CAST(? AS jsonb))";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, updatesJson);
            
            if (!results.isEmpty()) {
                Map<String, Object> result = results.get(0);
                Integer updatedCount = (Integer) result.get("updated_count");
                
                // Parse failed_updates JSON
                String failedUpdatesJson = (String) result.get("failed_updates");
                List<Long> failedIds = new ArrayList<>();
                
                if (failedUpdatesJson != null && !failedUpdatesJson.equals("[]")) {
                    try {
                        JsonNode failedUpdates = objectMapper.readTree(failedUpdatesJson);
                        for (JsonNode failedUpdate : failedUpdates) {
                            Long id = failedUpdate.get("id").asLong();
                            failedIds.add(id);
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing failed updates JSON: {}", e.getMessage());
                    }
                }
                
                BulkUpdateRequest.BulkUpdateResponse response = 
                    new BulkUpdateRequest.BulkUpdateResponse(updatedCount, failedIds);
                response.setMessage(response.getSummary());
                
                log.info("Bulk stock update completed: {} updated, {} failed", 
                    updatedCount, failedIds.size());
                
                return response;
            }
            
        } catch (Exception e) {
            log.error("Error in bulk stock update: {}", e.getMessage(), e);
        }
        
        // Return error response
        List<Long> allIds = request.getUpdates().stream()
            .map(BulkUpdateRequest.StockUpdateItem::getId)
            .toList();
        BulkUpdateRequest.BulkUpdateResponse errorResponse = 
            new BulkUpdateRequest.BulkUpdateResponse(0, allIds);
        errorResponse.setMessage("Có lỗi xảy ra khi cập nhật tồn kho");
        return errorResponse;
    }

    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics(LocalDate startDate) {
        log.info("Getting audit statistics from {}", startDate);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            List<Object[]> results = auditLogRepository.getAuditStatistics(startDateTime);
            
            for (Object[] row : results) {
                String action = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                Timestamp lastAction = (Timestamp) row[2];
                
                Map<String, Object> actionStats = new HashMap<>();
                actionStats.put("count", count);
                actionStats.put("lastAction", lastAction.toLocalDateTime());
                
                statistics.put(action.toLowerCase(), actionStats);
            }
            
            log.info("Retrieved audit statistics for {} action types", statistics.size());
            
        } catch (Exception e) {
            log.error("Error getting audit statistics: {}", e.getMessage(), e);
        }
        
        return statistics;
    }

    /**
     * Convert ProductAuditLog entity to ProductAuditLogDTO
     */
    private ProductAuditLogDTO convertToAuditLogDTO(ProductAuditLog auditLog) {
        ProductAuditLogDTO dto = new ProductAuditLogDTO();
        
        dto.setId(auditLog.getId());
        dto.setProductId(auditLog.getProductId());
        dto.setAction(auditLog.getAction());
        dto.setOldValues(auditLog.getOldValues());
        dto.setNewValues(auditLog.getNewValues());
        dto.setChangedBy(auditLog.getChangedBy());
        dto.setChangedAt(auditLog.getChangedAt());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setActionDescription(auditLog.getActionDisplayName());
        
        // For now, we don't have product name in the entity
        // This could be enhanced later with a join or separate query
        dto.setProductName("Sản phẩm #" + auditLog.getProductId());
        
        return dto;
    }
}
