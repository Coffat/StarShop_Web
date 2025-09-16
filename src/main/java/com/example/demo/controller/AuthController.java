package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.dto.UserInfoResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.WebSocketService;
import com.example.demo.service.EmailService;
import com.example.demo.service.OtpService;
import com.example.demo.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
    private final EmailService emailService;
    private final OtpService otpService;
    private final RegistrationService registrationService;

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
            BindingResult bindingResult,
            HttpServletResponse response) {
        
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
                
                // Set token in httpOnly cookie for security
                Cookie authCookie = new Cookie("authToken", token);
                authCookie.setHttpOnly(true);
                authCookie.setSecure(true); // Use HTTPS in production
                authCookie.setPath("/");
                authCookie.setMaxAge(24 * 60 * 60); // 24 hours
                response.addCookie(authCookie);
                
                // Create login response without token (for security)
                LoginResponse loginResponse = LoginResponse.of(null, userInfo);
                
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

    /**
     * User registration endpoint - Step 1: Send OTP
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<String>> register(@RequestBody RegisterRequest request) {
        log.info("Registration OTP request for email: {}", request.getEmail());
        
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Email không được để trống"));
            }
            
            if (request.getPassword() == null || request.getPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Mật khẩu phải có ít nhất 8 ký tự"));
            }
            
            if (request.getFirstname() == null || request.getFirstname().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Họ không được để trống"));
            }
            
            if (request.getLastname() == null || request.getLastname().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Tên không được để trống"));
            }
            
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Số điện thoại không được để trống"));
            }
            
            // Validate phone format (Vietnamese phone number)
            if (!request.getPhone().matches("^(\\+84|0)[3|5|7|8|9][0-9]{8}$")) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Số điện thoại không hợp lệ"));
            }
            
            // Check if user already exists
            User existingUser = authService.findUserByEmail(request.getEmail());
            if (existingUser != null) {
                log.warn("Registration failed - email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Email này đã được sử dụng"));
            }
            
            // Store pending registration data
            registrationService.storePendingRegistration(
                request.getEmail(),
                request.getPassword(),
                request.getFirstname(),
                request.getLastname(),
                request.getPhone()
            );
            
            // Generate and send OTP
            String otp = otpService.generateOtp(request.getEmail());
            emailService.sendOtpEmail(request.getEmail(), otp, request.getFirstname());
            
            log.info("Registration OTP sent successfully for email: {}", request.getEmail());
            return ResponseEntity.ok(ResponseWrapper.success("Mã xác thực đã được gửi đến email của bạn. Vui lòng kiểm tra và nhập mã để hoàn tất đăng ký."));
            
        } catch (Exception e) {
            log.error("Registration OTP error for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    /**
     * Verify OTP and complete registration - Step 2
     * POST /api/auth/verify-registration
     */
    @PostMapping("/verify-registration")
    public ResponseEntity<ResponseWrapper<String>> verifyRegistration(@RequestBody VerifyRegistrationRequest request) {
        log.info("Registration OTP verification attempt for email: {}", request.getEmail());
        
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Email không được để trống"));
            }
            
            if (request.getOtp() == null || request.getOtp().length() != 6) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Mã OTP phải có 6 số"));
            }
            
            // Verify OTP
            OtpService.OtpVerificationResult result = otpService.verifyOtp(request.getEmail(), request.getOtp());
            
            if (!result.isSuccess()) {
                log.warn("Registration OTP verification failed for email: {} - {}", request.getEmail(), result.getMessage());
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error(result.getMessage()));
            }
            
            // Get pending registration data
            RegistrationService.PendingRegistration pendingReg = registrationService.getPendingRegistration(request.getEmail());
            if (pendingReg == null) {
                log.warn("No pending registration found for email: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Không tìm thấy thông tin đăng ký hoặc đã hết hạn. Vui lòng đăng ký lại."));
            }
            
            // Create new user
            User newUser = authService.registerUser(
                pendingReg.getEmail(),
                pendingReg.getPassword(),
                pendingReg.getFirstname(),
                pendingReg.getLastname(),
                pendingReg.getPhone()
            );
            
            if (newUser != null) {
                // Remove pending registration
                registrationService.removePendingRegistration(request.getEmail());
                
                log.info("Registration completed successfully for email: {}", request.getEmail());
                
                // Send welcome email
                try {
                    emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getFirstname());
                } catch (Exception e) {
                    log.warn("Failed to send welcome email for user {}: {}", request.getEmail(), e.getMessage());
                }
                
                return ResponseEntity.ok(ResponseWrapper.success("Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ."));
            } else {
                log.error("Failed to create user for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseWrapper.error("Đăng ký thất bại. Vui lòng thử lại."));
            }
            
        } catch (Exception e) {
            log.error("Registration OTP verification error for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    /**
     * Forgot password endpoint - send OTP
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseWrapper<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        
        try {
            // Validate email
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Email không được để trống"));
            }
            
            // Check if user exists
            User user = authService.findUserByEmail(request.getEmail());
            if (user == null) {
                // For security, don't reveal if email exists or not
                log.warn("Forgot password request for non-existent email: {}", request.getEmail());
                return ResponseEntity.ok(ResponseWrapper.success("Nếu email tồn tại, mã xác thực đã được gửi."));
            }
            
            // Generate and send OTP
            String otp = otpService.generateOtp(request.getEmail());
            emailService.sendOtpEmail(request.getEmail(), otp, user.getFirstname());
            
            log.info("OTP sent successfully for email: {}", request.getEmail());
            return ResponseEntity.ok(ResponseWrapper.success("Mã xác thực đã được gửi đến email của bạn."));
            
        } catch (Exception e) {
            log.error("Forgot password error for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    /**
     * Verify OTP endpoint
     * POST /api/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseWrapper<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        log.info("OTP verification attempt for email: {}", request.getEmail());
        
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Email không được để trống"));
            }
            
            if (request.getOtp() == null || request.getOtp().length() != 6) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Mã OTP phải có 6 số"));
            }
            
            // Verify OTP
            OtpService.OtpVerificationResult result = otpService.verifyOtp(request.getEmail(), request.getOtp());
            
            if (result.isSuccess()) {
                // Generate reset token
                String resetToken = authService.generateResetToken(request.getEmail());
                log.info("OTP verification successful for email: {}", request.getEmail());
                
                VerifyOtpResponse response = new VerifyOtpResponse(resetToken);
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                log.warn("OTP verification failed for email: {} - {}", request.getEmail(), result.getMessage());
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error(result.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("OTP verification error for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    /**
     * Reset password endpoint
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseWrapper<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt for token: {}", request.getToken().substring(0, 10) + "...");
        
        try {
            // Validate input
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Token không hợp lệ"));
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Mật khẩu phải có ít nhất 8 ký tự"));
            }
            
            // Reset password using token
            boolean success = authService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
            
            if (success) {
                // Get user info for confirmation email
                String email = authService.getEmailFromResetToken(request.getToken());
                if (email != null) {
                    User user = authService.findUserByEmail(email);
                    if (user != null) {
                        try {
                            emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstname());
                        } catch (Exception e) {
                            log.warn("Failed to send password reset confirmation email: {}", e.getMessage());
                        }
                    }
                }
                
                log.info("Password reset successful");
                return ResponseEntity.ok(ResponseWrapper.success("Mật khẩu đã được đặt lại thành công!"));
            } else {
                log.warn("Password reset failed - invalid or expired token");
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Token không hợp lệ hoặc đã hết hạn"));
            }
            
        } catch (Exception e) {
            log.error("Password reset error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    // Request DTOs
    public static class RegisterRequest {
        private String email;
        private String password;
        private String firstname;
        private String lastname;
        private String phone;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstname() { return firstname; }
        public void setFirstname(String firstname) { this.firstname = firstname; }
        public String getLastname() { return lastname; }
        public void setLastname(String lastname) { this.lastname = lastname; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    public static class ForgotPasswordRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class VerifyRegistrationRequest {
        private String email;
        private String otp;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
    
    public static class VerifyOtpRequest {
        private String email;
        private String otp;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
    
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    public static class VerifyOtpResponse {
        private String resetToken;
        
        public VerifyOtpResponse(String resetToken) {
            this.resetToken = resetToken;
        }
        
        public String getResetToken() { return resetToken; }
        public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    }
}
