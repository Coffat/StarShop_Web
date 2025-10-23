package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
     * Returns specific error message if cannot review
     */
    public String canUserReviewOrderItemWithReason(Long userId, Long orderItemId) {
        if (userId == null || orderItemId == null) {
            log.warn("canUserReviewOrderItem: userId or orderItemId is null - userId: {}, orderItemId: {}", userId, orderItemId);
            return "Thông tin không hợp lệ";
        }

        log.info("Checking if user {} can review order item {}", userId, orderItemId);
        
        Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
        if (orderItemOpt.isEmpty()) {
            log.warn("canUserReviewOrderItem: OrderItem {} not found", orderItemId);
            return "Sản phẩm không tồn tại";
        }

        OrderItem orderItem = orderItemOpt.get();
        Order order = orderItem.getOrder();
        
        log.info("OrderItem {}: orderId={}, orderUserId={}, orderStatus={}, currentUserId={}", 
                orderItemId, order.getId(), order.getUser().getId(), order.getStatus(), userId);
        
        // Check if order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            log.warn("canUserReviewOrderItem: Order {} does not belong to user {} (belongs to user {})", 
                    order.getId(), userId, order.getUser().getId());
            return "Đơn hàng không thuộc về bạn";
        }
        
        // Check if order is completed
        if (order.getStatus() != OrderStatus.COMPLETED) {
            log.warn("canUserReviewOrderItem: Order {} status is {} (not COMPLETED)", 
                    order.getId(), order.getStatus());
            return "Đơn hàng chưa hoàn thành. Vui lòng đánh giá sau khi nhận được sản phẩm";
        }
        
        // Check if already reviewed
        boolean alreadyReviewed = reviewRepository.existsByOrderItemId(orderItemId);
        if (alreadyReviewed) {
            log.warn("canUserReviewOrderItem: OrderItem {} has already been reviewed", orderItemId);
            return "Bạn đã đánh giá sản phẩm này rồi";
        }
        
        log.info("User {} CAN review order item {}", userId, orderItemId);
        return null; // null means can review
    }
    
    /**
     * Check if user can review a specific order item (backward compatibility)
     */
    public boolean canUserReviewOrderItem(Long userId, Long orderItemId) {
        return canUserReviewOrderItemWithReason(userId, orderItemId) == null;
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
        String errorReason = canUserReviewOrderItemWithReason(userId, orderItemId);
        if (errorReason != null) {
            throw new IllegalStateException(errorReason);
        }

        // Get order item and related entities
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new IllegalArgumentException("Order item not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Force initialize lazy-loaded entities to prevent LazyInitializationException
        Product product = orderItem.getProduct();
        product.getName(); // Force load
        user.getFirstname(); // Force load

        // Create review
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        review.setOrderItem(orderItem);

        Review savedReview = reviewRepository.save(review);
        
        // Force initialize the saved review's lazy-loaded fields
        savedReview.getProduct().getName();
        savedReview.getUser().getFirstname();
        
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
     * Get all reviews with pagination and filters (Admin)
     */
    public Page<Review> getAllReviews(Pageable pageable, Integer rating, String search, String sentiment, String status, String sort) {
        log.debug("Getting all reviews with pagination: {}, rating: {}, search: {}, sentiment: {}, status: {}, sort: {}", 
                  pageable, rating, search, sentiment, status, sort);
        
        // Apply sorting to pageable
        org.springframework.data.domain.Sort sortObj;
        switch (sort != null ? sort : "newest") {
            case "oldest":
                sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt");
                break;
            case "rating-high":
                sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "rating");
                break;
            case "rating-low":
                sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "rating");
                break;
            case "newest":
            default:
                sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
                break;
        }
        
        pageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
        
        // If no filters, use the simple query
        if (rating == null && (search == null || search.trim().isEmpty()) && 
            (sentiment == null || sentiment.trim().isEmpty()) && (status == null || status.trim().isEmpty())) {
            return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        // Use filtered query
        return reviewRepository.findAllWithFilters(rating, search, sentiment, status, pageable);
    }
    
    /**
     * Get review statistics (Admin)
     */
    public java.util.Map<String, Object> getReviewStatistics() {
        log.debug("Getting review statistics");
        
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        
        // Total reviews
        long totalReviews = reviewRepository.count();
        statistics.put("totalReviews", totalReviews);
        
        // Average rating
        Double averageRating = reviewRepository.getAverageRating();
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        
        // 5 star reviews
        Long fiveStarReviews = reviewRepository.countByRating(5);
        statistics.put("fiveStarReviews", fiveStarReviews != null ? fiveStarReviews : 0L);
        
        // Today's reviews
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long todayReviews = reviewRepository.countByCreatedAtAfter(startOfDay);
        statistics.put("todayReviews", todayReviews != null ? todayReviews : 0L);
        
        return statistics;
    }

    /**
     * Add admin response to a review
     */
    @Transactional
    public Review addAdminResponse(Long reviewId, Long adminUserId, String adminResponse) {
        if (reviewId == null || adminUserId == null || adminResponse == null || adminResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid admin response parameters");
        }

        log.info("Adding admin response to review {} by admin {}", reviewId, adminUserId);

        // Get review
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Get admin user
        User adminUser = userRepository.findById(adminUserId)
            .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        // Update review with admin response
        review.setAdminResponse(adminResponse.trim());
        review.setAdminResponseAt(LocalDateTime.now());
        review.setAdminResponseBy(adminUser);

        Review savedReview = reviewRepository.save(review);
        log.info("Admin response added successfully to review {}", reviewId);
        
        return savedReview;
    }

    /**
     * Update admin response for a review
     */
    @Transactional
    public Review updateAdminResponse(Long reviewId, Long adminUserId, String adminResponse) {
        if (reviewId == null || adminUserId == null || adminResponse == null || adminResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid admin response parameters");
        }

        log.info("Updating admin response for review {} by admin {}", reviewId, adminUserId);

        // Get review
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check if review has admin response
        if (review.getAdminResponse() == null) {
            throw new IllegalStateException("Review does not have an admin response to update");
        }

        // Get admin user
        User adminUser = userRepository.findById(adminUserId)
            .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        // Update admin response
        review.setAdminResponse(adminResponse.trim());
        review.setAdminResponseAt(LocalDateTime.now());
        review.setAdminResponseBy(adminUser);

        Review savedReview = reviewRepository.save(review);
        log.info("Admin response updated successfully for review {}", reviewId);
        
        return savedReview;
    }

    /**
     * Remove admin response from a review
     */
    @Transactional
    public Review removeAdminResponse(Long reviewId, Long adminUserId) {
        if (reviewId == null || adminUserId == null) {
            throw new IllegalArgumentException("Review ID and admin user ID cannot be null");
        }

        log.info("Removing admin response from review {} by admin {}", reviewId, adminUserId);

        // Get review
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check if review has admin response
        if (review.getAdminResponse() == null) {
            throw new IllegalStateException("Review does not have an admin response to remove");
        }

        // Remove admin response
        review.setAdminResponse(null);
        review.setAdminResponseAt(null);
        review.setAdminResponseBy(null);

        Review savedReview = reviewRepository.save(review);
        log.info("Admin response removed successfully from review {}", reviewId);
        
        return savedReview;
    }
}
