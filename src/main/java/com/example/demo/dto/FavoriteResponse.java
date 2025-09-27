package com.example.demo.dto;

/**
 * Response DTO for favorite operations
 */
public class FavoriteResponse {
    
    private boolean success;
    private String message;
    private boolean isFavorite;
    private Long favoriteCount;
    
    // Constructors
    public FavoriteResponse() {
    }
    
    public FavoriteResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public FavoriteResponse(boolean success, String message, boolean isFavorite) {
        this.success = success;
        this.message = message;
        this.isFavorite = isFavorite;
    }
    
    public FavoriteResponse(boolean success, String message, boolean isFavorite, Long favoriteCount) {
        this.success = success;
        this.message = message;
        this.isFavorite = isFavorite;
        this.favoriteCount = favoriteCount;
    }
    
    // Static factory methods
    public static FavoriteResponse success(String message) {
        return new FavoriteResponse(true, message);
    }
    
    public static FavoriteResponse success(String message, boolean isFavorite) {
        return new FavoriteResponse(true, message, isFavorite);
    }
    
    public static FavoriteResponse success(String message, boolean isFavorite, Long favoriteCount) {
        return new FavoriteResponse(true, message, isFavorite, favoriteCount);
    }
    
    public static FavoriteResponse error(String message) {
        return new FavoriteResponse(false, message);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    public Long getFavoriteCount() {
        return favoriteCount;
    }
    
    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
