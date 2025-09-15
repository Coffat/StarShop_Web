package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.dto.UserInfoResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.WebSocketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Authentication Controller for login endpoints
 * Following rules.mdc specifications for RESTful API design
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final WebSocketService webSocketService;

    /**
     * User login endpoint
     * POST /api/auth/login
     * @param loginRequest Login credentials
     * @param bindingResult Validation results
     * @return JWT token if successful, error message if failed
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult bindingResult) {
        
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                
                log.warn("Login validation failed for email {}: {}", loginRequest.getEmail(), errorMessage);
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Validation failed: " + errorMessage));
            }
            
            // Authenticate user
            String token = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            
            if (token != null) {
                log.info("Login successful for email: {}", loginRequest.getEmail());
                
                // Get user information
                User user = authService.findUserByEmail(loginRequest.getEmail());
                if (user == null) {
                    log.error("User not found after successful authentication for email: {}", loginRequest.getEmail());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ResponseWrapper.error("An error occurred during login"));
                }
                
                // Send welcome message via WebSocket
                try {
                    webSocketService.sendWelcomeMessage(user.getId(), user.getFullName());
                } catch (Exception e) {
                    log.warn("Failed to send welcome message for user {}: {}", loginRequest.getEmail(), e.getMessage());
                    // Don't fail the login if WebSocket message fails
                }
                
                // Create user info response
                UserInfoResponse userInfo = new UserInfoResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getRole().toString()
                );
                
                // Create login response
                LoginResponse loginResponse = LoginResponse.of(token, userInfo);
                
                return ResponseEntity.ok(ResponseWrapper.<LoginResponse>success(loginResponse));
            } else {
                log.warn("Login failed for email: {} - Invalid credentials", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseWrapper.error("Invalid email or password"));
            }
            
        } catch (Exception e) {
            log.error("Login error for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            
            // Log error in JSON format as per rules.mdc
            log.error("{{ \"error\": \"{}\", \"timestamp\": \"{}\" }}", 
                     e.getMessage(), LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("An error occurred during login. Please try again."));
        }
    }

    /**
     * Token refresh endpoint
     * POST /api/auth/refresh
     * @param currentToken Current JWT token
     * @return New JWT token if successful
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseWrapper<String>> refreshToken(@RequestBody String currentToken) {
        
        log.info("Token refresh attempt");
        
        try {
            String newToken = authService.refreshToken(currentToken);
            
            if (newToken != null) {
                log.info("Token refresh successful");
                return ResponseEntity.ok(ResponseWrapper.success(newToken));
            } else {
                log.warn("Token refresh failed - Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseWrapper.error("Invalid or expired token"));
            }
            
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage(), e);
            
            // Log error in JSON format as per rules.mdc
            log.error("{{ \"error\": \"{}\", \"timestamp\": \"{}\" }}", 
                     e.getMessage(), LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("An error occurred during token refresh"));
        }
    }

    /**
     * Token validation endpoint
     * GET /api/auth/validate
     * @param token JWT token from Authorization header
     * @return User information if token is valid
     */
    @GetMapping("/validate")
    public ResponseEntity<ResponseWrapper<Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        log.debug("Token validation attempt");
        
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseWrapper.error("Missing or invalid Authorization header"));
            }
            
            // Extract token from "Bearer " prefix
            String jwtToken = token.substring(7);
            
            var user = authService.validateTokenAndGetUser(jwtToken);
            
            if (user != null) {
                log.debug("Token validation successful for user: {}", user.getEmail());
                
                // Return user info without sensitive data
                UserInfoResponse userInfo = new UserInfoResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getRole().name()
                );
                
                return ResponseEntity.ok(ResponseWrapper.success(userInfo));
            } else {
                log.warn("Token validation failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ResponseWrapper.error("Invalid or expired token"));
            }
            
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("An error occurred during token validation"));
        }
    }

    /**
     * Logout endpoint (client-side token removal)
     * POST /api/auth/logout
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<String>> logout() {
        log.info("Logout request received");
        
        // Since JWT is stateless, logout is handled client-side by removing the token
        // This endpoint can be used for logging purposes or future token blacklisting
        
        return ResponseEntity.ok(ResponseWrapper.success("Logged out successfully"));
    }
}
