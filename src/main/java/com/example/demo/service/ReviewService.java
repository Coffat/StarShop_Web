package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing product reviews
 * Following rules.mdc specifications for business logic tier
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    /**
     * Check if user can review a product (must have completed order with that product)
     */
    public boolean canUserReviewProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }

        log.debug("Checking if user {} can review product {}", userId, productId);
        
        // Check if user has any completed order items for this product
        List<OrderItem> completedOrderItems = orderItemRepository.findCompletedOrderItemsByUserAndProduct(userId, productId, OrderStatus.COMPLETED);
        boolean canReview = !completedOrderItems.isEmpty();
        
        log.debug("User {} can review product {}: {}", userId, productId, canReview);
        return canReview;
    }

    /**
     * Check if user can review a specific order item
     */
    public boolean canUserReviewOrderItem(Long userId, Long orderItemId) {
        if (userId == null || orderItemId == null) {
            return false;
        }

        log.debug("Checking if user {} can review order item {}", userId, orderItemId);
        
        Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
        if (orderItemOpt.isEmpty()) {
            return false;
        }

        OrderItem orderItem = orderItemOpt.get();
        Order order = orderItem.getOrder();
        
        // Check if order belongs to user and is completed
        boolean canReview = order.getUser().getId().equals(userId) && 
                           order.getStatus() == OrderStatus.COMPLETED;
        
        // Check if already reviewed
        if (canReview) {
            boolean alreadyReviewed = reviewRepository.existsByOrderItemId(orderItemId);
            canReview = !alreadyReviewed;
        }
        
        log.debug("User {} can review order item {}: {}", userId, orderItemId, canReview);
        return canReview;
    }

    /**
     * Create a new review
     */
    @Transactional
    public Review createReview(Long userId, Long orderItemId, Integer rating, String comment) {
        if (userId == null || orderItemId == null || rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid review parameters");
        }

        log.info("Creating review for user {} and order item {}", userId, orderItemId);

        // Validate user can review this order item
        if (!canUserReviewOrderItem(userId, orderItemId)) {
            throw new IllegalStateException("User cannot review this order item");
        }

        // Get order item and related entities
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new IllegalArgumentException("Order item not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create review
        Review review = new Review();
        review.setUser(user);
        review.setProduct(orderItem.getProduct());
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        review.setOrderItem(orderItem);

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with ID: {}", savedReview.getId());
        
        return savedReview;
    }

    /**
     * Update an existing review
     */
    @Transactional
    public Review updateReview(Long reviewId, Long userId, Integer rating, String comment) {
        if (reviewId == null || userId == null || rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid review parameters");
        }

        log.info("Updating review {} for user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("User can only update their own reviews");
        }

        // Update review
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);

        Review savedReview = reviewRepository.save(review);
        log.info("Review {} updated successfully", reviewId);
        
        return savedReview;
    }

    /**
     * Delete a review (user can delete their own, admin can delete any)
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID cannot be null");
        }

        log.info("Deleting review {} by user {} (admin: {})", reviewId, userId, isAdmin);

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check permissions
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("User can only delete their own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review {} deleted successfully", reviewId);
    }

    /**
     * Get reviews for a product with pagination
     */
    public Page<Review> getReviewsByProduct(Long productId, Pageable pageable) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        log.debug("Getting reviews for product {} with pagination", productId);
        return reviewRepository.findByProductId(productId, pageable);
    }

    /**
     * Get reviews by user
     */
    public List<Review> getReviewsByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        log.debug("Getting reviews for user {}", userId);
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get user's review for a specific product
     */
    public Optional<Review> getUserReviewForProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Optional.empty();
        }

        log.debug("Getting user {} review for product {}", userId, productId);
        return reviewRepository.findByUserIdAndProductId(userId, productId);
    }

    /**
     * Get order items that user can review
     */
    public List<OrderItem> getReviewableOrderItems(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        log.debug("Getting reviewable order items for user {}", userId);
        return orderItemRepository.findCompletedOrderItemsByUser(userId, OrderStatus.COMPLETED);
    }

    /**
     * Get average rating for a product
     */
    public Double getAverageRating(Long productId) {
        if (productId == null) {
            return 0.0;
        }

        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        return averageRating != null ? averageRating : 0.0;
    }

    /**
     * Get review count for a product
     */
    public Long getReviewCount(Long productId) {
        if (productId == null) {
            return 0L;
        }

        Long count = reviewRepository.countReviewsByProductId(productId);
        return count != null ? count : 0L;
    }

    /**
     * Get all reviews with pagination (Admin)
     */
    public Page<Review> getAllReviews(Pageable pageable) {
        log.debug("Getting all reviews with pagination: {}", pageable);
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
