package com.example.demo.exception;

/**
 * Custom exception for review-related errors
 */
public class ReviewException extends RuntimeException {
    
    public ReviewException(String message) {
        super(message);
    }
    
    public ReviewException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Specific review exception types
    public static class ReviewNotAllowedException extends ReviewException {
        public ReviewNotAllowedException(String message) {
            super(message);
        }
    }
    
    public static class ReviewAlreadyExistsException extends ReviewException {
        public ReviewAlreadyExistsException(String message) {
            super(message);
        }
    }
    
    public static class ReviewNotFoundException extends ReviewException {
        public ReviewNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class UnauthorizedReviewAccessException extends ReviewException {
        public UnauthorizedReviewAccessException(String message) {
            super(message);
        }
    }
}
