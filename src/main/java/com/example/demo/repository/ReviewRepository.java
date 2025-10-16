package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    List<Review> findByUserId(Long userId);
    
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.rating = :rating")
    List<Review> findByRating(@Param("rating") Integer rating);
    
    // New methods for star count statistics
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating = :rating")
    Long countByProductIdAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);
    
    // Check if user can review this product (has completed order with this product)
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.user.id = :userId " +
           "AND oi.product.id = :productId " +
           "AND o.status = 'COMPLETED'")
    boolean canUserReviewProduct(@Param("userId") Long userId, @Param("productId") Long productId);
    
    // Get reviews with user and product info (optimized query)
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.user " +
           "JOIN FETCH r.product " +
           "WHERE r.product.id = :productId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdWithUserAndProduct(@Param("productId") Long productId);
}
