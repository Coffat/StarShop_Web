package com.example.demo.dto.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSummaryDTO {
    
    private Long productId;
    private Long totalReviews;
    private Double averageRating;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
    
    // Helper method to get star percentage
    public double getStarPercentage(int stars) {
        if (totalReviews == 0) return 0.0;
        
        long count = switch (stars) {
            case 5 -> fiveStarCount != null ? fiveStarCount : 0;
            case 4 -> fourStarCount != null ? fourStarCount : 0;
            case 3 -> threeStarCount != null ? threeStarCount : 0;
            case 2 -> twoStarCount != null ? twoStarCount : 0;
            case 1 -> oneStarCount != null ? oneStarCount : 0;
            default -> 0;
        };
        
        return (double) count / totalReviews * 100;
    }
    
    // Helper method to get formatted average rating
    public String getFormattedAverageRating() {
        if (averageRating == null || averageRating == 0) {
            return "0.0";
        }
        return String.format("%.1f", averageRating);
    }
}
