package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Authentication Service for user login and JWT token generation
 * Following rules.mdc specifications for business logic tier
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Authenticate user with email and password
     * @param email User email
     * @param password Plain text password
     * @return JWT token if authentication successful, null otherwise
     */
    public String authenticateUser(String email, String password) {
        log.info("Authentication attempt for email: {}", email);
        
        try {
            // Validate input parameters
            if (email == null || email.trim().isEmpty()) {
                log.warn("Authentication failed: email is null or empty");
                return null;
            }
            
            if (password == null || password.trim().isEmpty()) {
                log.warn("Authentication failed: password is null or empty for email: {}", email);
                return null;
            }
            
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(email.trim().toLowerCase());
            if (userOptional.isEmpty()) {
                log.warn("Authentication failed: user not found for email: {}", email);
                return null;
            }
            
            User user = userOptional.get();
            
            // Validate password using BCrypt
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Authentication failed: invalid password for email: {}", email);
                return null;
            }
            
            // Generate JWT token with user claims
            String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
            
            log.info("Authentication successful for user: {} with role: {}", email, user.getRole());
            return token;
            
        } catch (Exception e) {
            log.error("Authentication error for email {}: {}", email, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Find user by email
     * @param email User email
     * @return User entity if found, null otherwise
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            return userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        } catch (Exception e) {
            log.error("Error finding user by email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Find user by ID
     * @param userId User ID
     * @return User entity if found, null otherwise
     */
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            log.error("Error finding user by ID {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Validate user credentials without generating token
     * @param email User email
     * @param password Plain text password
     * @return true if credentials are valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateCredentials(String email, String password) {
        log.debug("Validating credentials for email: {}", email);
        
        try {
            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return false;
            }
            
            Optional<User> userOptional = userRepository.findByEmail(email.trim().toLowerCase());
            if (userOptional.isEmpty()) {
                return false;
            }
            
            User user = userOptional.get();
            return passwordEncoder.matches(password, user.getPassword());
            
        } catch (Exception e) {
            log.error("Error validating credentials for email {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user exists by email
     * @param email User email
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean userExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            return userRepository.existsByEmail(email.trim().toLowerCase());
        } catch (Exception e) {
            log.error("Error checking user existence for email {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Refresh JWT token for existing user
     * @param currentToken Current JWT token
     * @return New JWT token if valid, null otherwise
     */
    public String refreshToken(String currentToken) {
        try {
            if (currentToken == null || currentToken.trim().isEmpty()) {
                return null;
            }
            
            // Validate current token
            if (!jwtService.validateToken(currentToken)) {
                log.warn("Token refresh failed: invalid current token");
                return null;
            }
            
            // Extract user information from current token
            String email = jwtService.extractEmail(currentToken);
            
            // Find user to get current role (in case it was updated)
            User user = findUserByEmail(email);
            if (user == null) {
                log.warn("Token refresh failed: user not found for email: {}", email);
                return null;
            }
            
            // Generate new token
            String newToken = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
            
            log.info("Token refreshed successfully for user: {}", email);
            return newToken;
            
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate JWT token and return user information
     * @param token JWT token
     * @return User entity if token is valid, null otherwise
     */
    @Transactional(readOnly = true)
    public User validateTokenAndGetUser(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }
            
            // Validate token
            if (!jwtService.validateToken(token)) {
                return null;
            }
            
            // Extract email from token
            String email = jwtService.extractEmail(token);
            
            // Find and return user
            return findUserByEmail(email);
            
        } catch (Exception e) {
            log.error("Error validating token and getting user: {}", e.getMessage());
            return null;
        }
    }
}
