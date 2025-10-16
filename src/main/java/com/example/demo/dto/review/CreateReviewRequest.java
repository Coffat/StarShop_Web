package com.example.demo.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {
    
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
    
    @NotNull(message = "Order ID không được để trống")
    private String orderId;
    
    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private Integer rating;
    
    @Size(max = 1000, message = "Bình luận không được quá 1000 ký tự")
    private String comment;
    
    // Constructor
    public CreateReviewRequest() {}
    
    public CreateReviewRequest(Long productId, String orderId, Integer rating, String comment) {
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
    }
}
