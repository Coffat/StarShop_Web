package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User information response DTO for token validation
 * Following rules.mdc specifications for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private String role;
}
