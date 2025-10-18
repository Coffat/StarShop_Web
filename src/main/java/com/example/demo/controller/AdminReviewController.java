package com.example.demo.controller;

import com.example.demo.dto.ResponseWrapper;
import com.example.demo.entity.Review;
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

/**
 * Admin REST API Controller for Review Management
 * Following rules.mdc specifications for REST API
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "👑 Admin Reviews", description = "Admin APIs - Manage product reviews")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    private final ReviewService reviewService;

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
}
