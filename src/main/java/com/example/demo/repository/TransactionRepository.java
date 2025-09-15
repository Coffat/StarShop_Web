package com.example.demo.repository;

import com.example.demo.entity.Transaction;
import com.example.demo.entity.enums.TransactionStatus;
import com.example.demo.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    
    List<Transaction> findByOrderId(Long orderId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    Page<Transaction> findByTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.status = :status")
    BigDecimal getTotalAmountByTypeAndStatus(@Param("type") TransactionType type,
                                             @Param("status") TransactionStatus status);
}
