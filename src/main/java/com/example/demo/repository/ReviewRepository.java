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
    
    List<Review> findByUserId(Long userId);
    
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.rating = :rating")
    List<Review> findByRating(@Param("rating") Integer rating);
    
    /**
     * Find review by order item ID
     */
    Optional<Review> findByOrderItemId(Long orderItemId);
    
    /**
     * Check if review exists for order item
     */
    boolean existsByOrderItemId(Long orderItemId);
    
    /**
     * Find reviews by user ordered by creation date descending
     */
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find all reviews ordered by creation date descending (for admin)
     */
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
