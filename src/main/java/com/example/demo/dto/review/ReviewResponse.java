package com.example.demo.dto.review;

import com.example.demo.entity.Review;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canEdit; // Có thể chỉnh sửa không (chỉ owner)
    
    // Static factory method
    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .userAvatar(review.getUser().getAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .canEdit(false) // Will be set by service based on current user
                .build();
    }
    
    // Factory method with edit permission
    public static ReviewResponse fromEntity(Review review, boolean canEdit) {
        ReviewResponse response = fromEntity(review);
        response.setCanEdit(canEdit);
        return response;
    }
}
