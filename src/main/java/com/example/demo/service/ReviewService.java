package com.example.demo.service;

import com.example.demo.dto.review.*;
import com.example.demo.entity.*;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.exception.ReviewException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRateLimitService rateLimitService;

    /**
     * Tạo review mới
     */
    public ReviewResponse createReview(CreateReviewRequest request, String userEmail) {
        log.info("Creating review for product {} by user {}", request.getProductId(), userEmail);
        
        // Check rate limiting
        if (!rateLimitService.canCreateReview(userEmail)) {
            long remainingMinutes = rateLimitService.getRemainingTimeMinutes(userEmail);
            int remainingDaily = rateLimitService.getRemainingDailyReviews(userEmail);
            
            if (remainingMinutes > 0) {
                throw new ReviewException.ReviewNotAllowedException(
                    "Vui lòng đợi " + remainingMinutes + " phút trước khi đánh giá tiếp");
            } else {
                throw new ReviewException.ReviewNotAllowedException(
                    "Bạn đã đạt giới hạn " + (10 - remainingDaily) + " đánh giá trong ngày");
            }
        }
        
        // Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ReviewException("Không tìm thấy người dùng"));
        
        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ReviewException("Không tìm thấy sản phẩm"));
        
        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ReviewException("Không tìm thấy đơn hàng"));
        
        // Business rules validation
        validateReviewCreation(user, product, order, request);
        
        // Find order item
        OrderItem orderItem = orderItemRepository.findByOrderIdAndProductId(request.getOrderId(), product.getId())
                .orElseThrow(() -> new ReviewException("Sản phẩm không có trong đơn hàng này"));
        
        // Check if already reviewed
        if (reviewRepository.findByUserIdAndProductId(user.getId(), product.getId()).isPresent()) {
            throw new ReviewException.ReviewAlreadyExistsException("Bạn đã đánh giá sản phẩm này rồi");
        }
        
        // Create review
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setOrderItem(orderItem);
        
        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with ID: {}", savedReview.getId());
        
        // Record review creation for rate limiting
        rateLimitService.recordReviewCreation(userEmail);
        
        return ReviewResponse.fromEntity(savedReview, true);
    }

    /**
     * Cập nhật review
     */
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, String userEmail) {
        log.info("Updating review {} by user {}", reviewId, userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        // Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa đánh giá này");
        }
        
        // Update fields
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Review updated successfully: {}", reviewId);
        
        return ReviewResponse.fromEntity(updatedReview, true);
    }

    /**
     * Xóa review
     */
    public void deleteReview(Long reviewId, String userEmail) {
        log.info("Deleting review {} by user {}", reviewId, userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        // Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }
        
        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", reviewId);
    }

    /**
     * Lấy reviews của sản phẩm với pagination
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, int page, int size, String currentUserEmail) {
        log.info("Getting reviews for product {} - page: {}, size: {}", productId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewsPage = reviewRepository.findByProductId(productId, pageable);
        
        // Get current user ID for edit permission check
        Long currentUserId = null;
        if (currentUserEmail != null) {
            Optional<User> currentUser = userRepository.findByEmail(currentUserEmail);
            if (currentUser.isPresent()) {
                currentUserId = currentUser.get().getId();
            }
        }
        
        final Long finalCurrentUserId = currentUserId;
        return reviewsPage.map(review -> ReviewResponse.fromEntity(review, 
                finalCurrentUserId != null && review.getUser().getId().equals(finalCurrentUserId)));
    }

    /**
     * Lấy reviews của user
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(String userEmail, int page, int size) {
        log.info("Getting reviews for user {} - page: {}, size: {}", userEmail, page, size);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewsPage = reviewRepository.findByUserId(user.getId(), pageable);
        
        return reviewsPage.map(review -> ReviewResponse.fromEntity(review, true));
    }

    /**
     * Kiểm tra user đã review sản phẩm chưa
     */
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return reviewRepository.findByUserIdAndProductId(user.getId(), productId).isPresent();
    }

    /**
     * Lấy review summary của sản phẩm
     */
    @Transactional(readOnly = true)
    public ReviewSummaryDTO getProductReviewSummary(Long productId) {
        log.info("Getting review summary for product {}", productId);
        
        Long totalReviews = reviewRepository.countReviewsByProductId(productId);
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        
        // Get star counts (you'll need to add these methods to ReviewRepository)
        Long fiveStarCount = reviewRepository.countByProductIdAndRating(productId, 5);
        Long fourStarCount = reviewRepository.countByProductIdAndRating(productId, 4);
        Long threeStarCount = reviewRepository.countByProductIdAndRating(productId, 3);
        Long twoStarCount = reviewRepository.countByProductIdAndRating(productId, 2);
        Long oneStarCount = reviewRepository.countByProductIdAndRating(productId, 1);
        
        return ReviewSummaryDTO.builder()
                .productId(productId)
                .totalReviews(totalReviews != null ? totalReviews : 0)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .fiveStarCount(fiveStarCount != null ? fiveStarCount : 0)
                .fourStarCount(fourStarCount != null ? fourStarCount : 0)
                .threeStarCount(threeStarCount != null ? threeStarCount : 0)
                .twoStarCount(twoStarCount != null ? twoStarCount : 0)
                .oneStarCount(oneStarCount != null ? oneStarCount : 0)
                .build();
    }

    /**
     * Validate business rules for review creation
     */
    private void validateReviewCreation(User user, Product product, Order order, CreateReviewRequest request) {
        // Check if order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ReviewException.UnauthorizedReviewAccessException("Đơn hàng không thuộc về bạn");
        }
        
        // Check if order is completed
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ReviewException.ReviewNotAllowedException("Chỉ có thể đánh giá khi đơn hàng đã hoàn thành");
        }
        
        // Check if product is in the order
        boolean productInOrder = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
        
        if (!productInOrder) {
            throw new ReviewException.ReviewNotAllowedException("Sản phẩm không có trong đơn hàng này");
        }
        
        // Additional validation: Check if enough time has passed since order completion
        // (Optional: prevent immediate reviews to ensure genuine experience)
        
        // Validate rating range (additional check beyond @Valid)
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ReviewException("Đánh giá phải từ 1 đến 5 sao");
        }
        
        // Validate comment length if provided
        if (request.getComment() != null && request.getComment().length() > 1000) {
            throw new ReviewException("Bình luận không được quá 1000 ký tự");
        }
    }
}
