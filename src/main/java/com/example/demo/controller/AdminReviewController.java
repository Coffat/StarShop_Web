package com.example.demo.controller;

import com.example.demo.dto.AdminReviewResponseRequest;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.dto.ReviewAiAnalysisResponse;
import com.example.demo.dto.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminAiInsightsService;
import com.example.demo.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Admin REST API Controller for Review Management
 * Following rules.mdc specifications for REST API
 */
@RestController
@RequestMapping("/admin/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "👑 Admin Reviews", description = "Admin APIs - Manage product reviews")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final AdminAiInsightsService adminAiInsightsService;

    /**
     * Get all reviews with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Lấy danh sách đánh giá",
        description = "Lấy danh sách tất cả đánh giá với phân trang (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<ResponseWrapper<Page<Review>>> getAllReviews(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số đánh giá mỗi trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Lọc theo rating (1-5)", example = "5")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "Tìm kiếm theo tên sản phẩm hoặc người dùng", example = "hoa hồng")
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        try {
            log.info("Admin {} getting all reviews with pagination: page={}, size={}, rating={}, search={}", 
                    authentication.getName(), page, size, rating, search);
            
            Pageable pageable = PageRequest.of(page, size);
            
            // Get all reviews with pagination
            Page<Review> reviewsPage = reviewService.getAllReviews(pageable);
            
            return ResponseEntity.ok(ResponseWrapper.success(reviewsPage));
            
        } catch (Exception e) {
            log.error("Error getting all reviews: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể lấy danh sách đánh giá: " + e.getMessage()));
        }
    }

    /**
     * Delete a review (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Xóa đánh giá",
        description = "Xóa đánh giá bất kỳ (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xóa đánh giá thành công"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Đánh giá không tồn tại")
    })
    public ResponseEntity<ResponseWrapper<String>> deleteReview(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            log.info("Admin {} deleting review {}", authentication.getName(), id);
            
            // Admin can delete any review, so we pass a dummy user ID and isAdmin = true
            reviewService.deleteReview(id, 0L, true);
            
            log.info("Review {} deleted successfully by admin {}", id, authentication.getName());
            return ResponseEntity.ok(ResponseWrapper.success("Đánh giá đã được xóa thành công"));
            
        } catch (Exception e) {
            log.error("Error deleting review {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể xóa đánh giá: " + e.getMessage()));
        }
    }

    /**
     * Get review statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Thống kê đánh giá",
        description = "Lấy thống kê tổng quan về đánh giá (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<ResponseWrapper<java.util.Map<String, Object>>> getReviewStatistics(
            Authentication authentication) {
        
        try {
            log.info("Admin {} getting review statistics", authentication.getName());
            
            // For now, return basic statistics
            // In the future, we can add more detailed statistics
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalReviews", 0L); // This would need to be implemented
            statistics.put("averageRating", 0.0); // This would need to be implemented
            statistics.put("ratingDistribution", new java.util.HashMap<>()); // This would need to be implemented
            
            return ResponseEntity.ok(ResponseWrapper.success(statistics));
            
        } catch (Exception e) {
            log.error("Error getting review statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể lấy thống kê: " + e.getMessage()));
        }
    }

    /**
     * Add admin response to a review
     */
    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Phản hồi đánh giá",
        description = "Admin phản hồi đánh giá của khách hàng"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phản hồi thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Đánh giá không tồn tại")
    })
    public ResponseEntity<ResponseWrapper<ReviewResponse>> addAdminResponse(
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewResponseRequest request,
            Authentication authentication) {
        
        try {
            log.info("Admin {} adding response to review {}", authentication.getName(), id);
            
            User adminUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
            
            Review review = reviewService.addAdminResponse(id, adminUser.getId(), request.getAdminResponse());
            ReviewResponse response = new ReviewResponse(review, false);
            
            log.info("Admin response added successfully to review {}", id);
            return ResponseEntity.ok(ResponseWrapper.success(response, "Phản hồi đã được thêm thành công"));
            
        } catch (Exception e) {
            log.error("Error adding admin response to review {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể thêm phản hồi: " + e.getMessage()));
        }
    }

    /**
     * Update admin response for a review
     */
    @PutMapping("/{id}/response")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Cập nhật phản hồi đánh giá",
        description = "Admin cập nhật phản hồi đánh giá của khách hàng"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật phản hồi thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Đánh giá không tồn tại")
    })
    public ResponseEntity<ResponseWrapper<ReviewResponse>> updateAdminResponse(
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewResponseRequest request,
            Authentication authentication) {
        
        try {
            log.info("Admin {} updating response for review {}", authentication.getName(), id);
            
            User adminUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
            
            Review review = reviewService.updateAdminResponse(id, adminUser.getId(), request.getAdminResponse());
            ReviewResponse response = new ReviewResponse(review, false);
            
            log.info("Admin response updated successfully for review {}", id);
            return ResponseEntity.ok(ResponseWrapper.success(response, "Phản hồi đã được cập nhật thành công"));
            
        } catch (Exception e) {
            log.error("Error updating admin response for review {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể cập nhật phản hồi: " + e.getMessage()));
        }
    }

    /**
     * Remove admin response from a review
     */
    @DeleteMapping("/{id}/response")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Xóa phản hồi đánh giá",
        description = "Admin xóa phản hồi đánh giá của khách hàng"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xóa phản hồi thành công"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Đánh giá không tồn tại")
    })
    public ResponseEntity<ResponseWrapper<ReviewResponse>> removeAdminResponse(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            log.info("Admin {} removing response from review {}", authentication.getName(), id);
            
            User adminUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
            
            Review review = reviewService.removeAdminResponse(id, adminUser.getId());
            ReviewResponse response = new ReviewResponse(review, false);
            
            log.info("Admin response removed successfully from review {}", id);
            return ResponseEntity.ok(ResponseWrapper.success(response, "Phản hồi đã được xóa thành công"));
            
        } catch (Exception e) {
            log.error("Error removing admin response from review {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể xóa phản hồi: " + e.getMessage()));
        }
    }

    /**
     * Analyze review with AI to get sentiment and suggested replies
     */
    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Phân tích đánh giá với AI",
        description = "Sử dụng AI để phân tích sentiment và gợi ý câu trả lời cho đánh giá"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phân tích thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
        @ApiResponse(responseCode = "404", description = "Đánh giá không tồn tại")
    })
    public ResponseEntity<ResponseWrapper<ReviewAiAnalysisResponse>> analyzeReview(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            log.info("Admin {} analyzing review {} with AI", authentication.getName(), id);
            
            ReviewAiAnalysisResponse analysis = adminAiInsightsService.analyzeReview(id);
            
            log.info("AI analysis completed for review {}", id);
            return ResponseEntity.ok(ResponseWrapper.success(analysis, "Phân tích AI hoàn thành"));
            
        } catch (Exception e) {
            log.error("Error analyzing review {} with AI: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Không thể phân tích đánh giá: " + e.getMessage()));
        }
    }
}
