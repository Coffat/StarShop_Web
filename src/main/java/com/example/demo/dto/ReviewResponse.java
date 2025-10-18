package com.example.demo.dto;

import com.example.demo.entity.Review;

import java.time.LocalDateTime;

/**
 * DTO for review responses
 */
public class ReviewResponse {
    
    private Long id;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canEdit;
    private boolean isVerified; // Has orderItem (verified purchase)
    private String productName;
    private Long productId;
    
    // Constructors
    public ReviewResponse() {}
    
    public ReviewResponse(Review review, boolean canEdit) {
        this.id = review.getId();
        this.userName = review.getUser().getFirstname() + " " + review.getUser().getLastname();
        this.userAvatar = review.getUser().getAvatar();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.canEdit = canEdit;
        this.isVerified = review.getOrderItem() != null;
        this.productName = review.getProduct().getName();
        this.productId = review.getProduct().getId();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserAvatar() {
        return userAvatar;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isCanEdit() {
        return canEdit;
    }
    
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
