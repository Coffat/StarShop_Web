package com.example.demo.repository;

import com.example.demo.entity.ProductAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Product Audit Log operations
 * Note: This uses native queries since we're working with PostgreSQL-specific features
 */
@Repository
public interface ProductAuditLogRepository extends JpaRepository<ProductAuditLog, Long> {

    /**
     * Get audit logs ordered by changed date
     */
    @Query("SELECT pal FROM ProductAuditLog pal ORDER BY pal.changedAt DESC")
    Page<ProductAuditLog> findAllOrderByChangedAtDesc(Pageable pageable);

    /**
     * Get audit logs for specific product
     */
    @Query("SELECT pal FROM ProductAuditLog pal WHERE pal.productId = :productId ORDER BY pal.changedAt DESC")
    Page<ProductAuditLog> findByProductIdOrderByChangedAtDesc(@Param("productId") Long productId, Pageable pageable);

    /**
     * Get audit logs by date range
     */
    @Query("SELECT pal FROM ProductAuditLog pal WHERE pal.changedAt BETWEEN :startDate AND :endDate ORDER BY pal.changedAt DESC")
    Page<ProductAuditLog> findByChangedAtBetweenOrderByChangedAtDesc(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable);

    /**
     * Get audit logs by action type
     */
    @Query("SELECT pal FROM ProductAuditLog pal WHERE pal.action = :action ORDER BY pal.changedAt DESC")
    Page<ProductAuditLog> findByActionOrderByChangedAtDesc(@Param("action") String action, Pageable pageable);

    /**
     * Get recent audit logs (last 24 hours)
     */
    @Query("SELECT pal FROM ProductAuditLog pal WHERE pal.changedAt >= :since ORDER BY pal.changedAt DESC")
    List<ProductAuditLog> findRecentAuditLogs(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Count audit logs by action type
     */
    long countByAction(String action);

    /**
     * Get audit statistics - using native query for aggregation
     */
    @Query(value = """
        SELECT 
            action,
            COUNT(*) as count,
            MAX(changed_at) as last_action
        FROM product_audit_log 
        WHERE changed_at >= :startDate
        GROUP BY action
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Object[]> getAuditStatistics(@Param("startDate") LocalDateTime startDate);
}
