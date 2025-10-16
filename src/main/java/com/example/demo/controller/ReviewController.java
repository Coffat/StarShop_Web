package com.example.demo.controller;

import com.example.demo.dto.ResponseWrapper;
import com.example.demo.dto.review.*;
import com.example.demo.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * Review Controller for product reviews
 * Following REST API best practices
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "⭐ Reviews", description = "Product review APIs - Create, read, update, delete reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Tạo review mới
     * POST /api/reviews
     */
    @Operation(
        summary = "Tạo đánh giá mới",
        description = "Tạo đánh giá cho sản phẩm đã mua. Chỉ có thể đánh giá khi đơn hàng đã hoàn thành."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tạo đánh giá thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền đánh giá sản phẩm này")
    })
    @PostMapping
    public ResponseEntity<ResponseWrapper<ReviewResponse>> createReview(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin đánh giá",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateReviewRequest.class))
            )
            @Valid @RequestBody CreateReviewRequest request,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Creating review for product {} by user {}", request.getProductId(), authentication.getName());
        
        try {
            // Check validation errors
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Validation failed: " + errorMessage));
            }
            
            ReviewResponse review = reviewService.createReview(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseWrapper.success(review));
            
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật review
     * PUT /api/reviews/{id}
     */
    @Operation(
        summary = "Cập nhật đánh giá",
        description = "Cập nhật đánh giá của mình. Chỉ có thể cập nhật đánh giá do mình tạo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền chỉnh sửa đánh giá này"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ReviewResponse>> updateReview(
            @Parameter(description = "ID của đánh giá", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin cập nhật",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateReviewRequest.class))
            )
            @Valid @RequestBody UpdateReviewRequest request,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Updating review {} by user {}", id, authentication.getName());
        
        try {
            // Check validation errors
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Validation failed: " + errorMessage));
            }
            
            ReviewResponse review = reviewService.updateReview(id, request, authentication.getName());
            return ResponseEntity.ok(ResponseWrapper.success(review));
            
        } catch (Exception e) {
            log.error("Error updating review {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    /**
     * Xóa review
     * DELETE /api/reviews/{id}
     */
    @Operation(
        summary = "Xóa đánh giá",
        description = "Xóa đánh giá của mình. Chỉ có thể xóa đánh giá do mình tạo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền xóa đánh giá này"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<String>> deleteReview(
            @Parameter(description = "ID của đánh giá", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Deleting review {} by user {}", id, authentication.getName());
        
        try {
            reviewService.deleteReview(id, authentication.getName());
            return ResponseEntity.ok(ResponseWrapper.success("Xóa đánh giá thành công"));
            
        } catch (Exception e) {
            log.error("Error deleting review {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    /**
     * Lấy reviews của sản phẩm
     * GET /api/reviews/product/{productId}
     */
    @Operation(
        summary = "Lấy đánh giá của sản phẩm",
        description = "Lấy danh sách đánh giá của sản phẩm với phân trang."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseWrapper<Page<ReviewResponse>>> getProductReviews(
            @Parameter(description = "ID của sản phẩm", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Getting reviews for product {} - page: {}, size: {}", productId, page, size);
        
        try {
            String currentUserEmail = authentication != null ? authentication.getName() : null;
            Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, page, size, currentUserEmail);
            return ResponseEntity.ok(ResponseWrapper.success(reviews));
            
        } catch (Exception e) {
            log.error("Error getting product reviews: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    /**
     * Lấy reviews của user hiện tại
     * GET /api/reviews/my
     */
    @Operation(
        summary = "Lấy đánh giá của tôi",
        description = "Lấy danh sách đánh giá do người dùng hiện tại tạo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/my")
    public ResponseEntity<ResponseWrapper<Page<ReviewResponse>>> getMyReviews(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Getting reviews for user {} - page: {}, size: {}", authentication.getName(), page, size);
        
        try {
            Page<ReviewResponse> reviews = reviewService.getUserReviews(authentication.getName(), page, size);
            return ResponseEntity.ok(ResponseWrapper.success(reviews));
            
        } catch (Exception e) {
            log.error("Error getting user reviews: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    /**
     * Kiểm tra user đã review sản phẩm chưa
     * GET /api/reviews/check/{productId}
     */
    @Operation(
        summary = "Kiểm tra đã đánh giá chưa",
        description = "Kiểm tra người dùng hiện tại đã đánh giá sản phẩm này chưa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kiểm tra thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/check/{productId}")
    public ResponseEntity<ResponseWrapper<Boolean>> checkUserReviewed(
            @Parameter(description = "ID của sản phẩm", required = true)
            @PathVariable Long productId,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Checking if user {} reviewed product {}", authentication.getName(), productId);
        
        try {
            boolean hasReviewed = reviewService.hasUserReviewedProduct(productId, authentication.getName());
            return ResponseEntity.ok(ResponseWrapper.success(hasReviewed));
            
        } catch (Exception e) {
            log.error("Error checking user review status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    /**
     * Lấy thống kê đánh giá của sản phẩm
     * GET /api/reviews/summary/{productId}
     */
    @Operation(
        summary = "Thống kê đánh giá sản phẩm",
        description = "Lấy thống kê tổng quan về đánh giá của sản phẩm (số sao trung bình, phân bố sao, v.v.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    @GetMapping("/summary/{productId}")
    public ResponseEntity<ResponseWrapper<ReviewSummaryDTO>> getProductReviewSummary(
            @Parameter(description = "ID của sản phẩm", required = true)
            @PathVariable Long productId) {
        
        log.info("Getting review summary for product {}", productId);
        
        try {
            ReviewSummaryDTO summary = reviewService.getProductReviewSummary(productId);
            return ResponseEntity.ok(ResponseWrapper.success(summary));
            
        } catch (Exception e) {
            log.error("Error getting product review summary: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }
}
