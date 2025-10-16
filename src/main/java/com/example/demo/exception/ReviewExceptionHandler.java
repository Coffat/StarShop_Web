package com.example.demo.exception;

import com.example.demo.dto.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Review-related exceptions
 */
@RestControllerAdvice
@Slf4j
public class ReviewExceptionHandler {

    @ExceptionHandler(ReviewException.ReviewNotAllowedException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleReviewNotAllowed(ReviewException.ReviewNotAllowedException e) {
        log.warn("Review not allowed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseWrapper.error(e.getMessage()));
    }

    @ExceptionHandler(ReviewException.ReviewAlreadyExistsException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleReviewAlreadyExists(ReviewException.ReviewAlreadyExistsException e) {
        log.warn("Review already exists: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseWrapper.error(e.getMessage()));
    }

    @ExceptionHandler(ReviewException.ReviewNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleReviewNotFound(ReviewException.ReviewNotFoundException e) {
        log.warn("Review not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(e.getMessage()));
    }

    @ExceptionHandler(ReviewException.UnauthorizedReviewAccessException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleUnauthorizedReviewAccess(ReviewException.UnauthorizedReviewAccessException e) {
        log.warn("Unauthorized review access: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseWrapper.error(e.getMessage()));
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleGenericReviewException(ReviewException e) {
        log.error("Review exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
    }
}
