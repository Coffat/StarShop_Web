package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response wrapper for API responses
 * Following rules.mdc JSON response format: { "data": [], "error": null }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWrapper<T> {
    
    private T data;
    private String error;
    private String message;
    
    /**
     * Create successful response with data
     * @param data Response data
     * @return ResponseWrapper with data and null error
     */
    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(data, null, null);
    }
    
    /**
     * Create successful response with data and message
     * @param data Response data
     * @param message Success message
     * @return ResponseWrapper with data and message
     */
    public static <T> ResponseWrapper<T> success(T data, String message) {
        return new ResponseWrapper<>(data, null, message);
    }
    
    /**
     * Create error response with message
     * @param error Error message
     * @return ResponseWrapper with null data and error message
     */
    public static <T> ResponseWrapper<T> error(String error) {
        return new ResponseWrapper<>(null, error, null);
    }
}
