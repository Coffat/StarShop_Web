package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Custom OAuth2 User Service for Google and Facebook login
 * Following rules.mdc specifications for OAuth2 integration
 */
@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception e) {
            log.error("Error processing OAuth2 user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("OAuth2 user processing failed");
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oauth2User, registrationId);
        String name = extractName(oauth2User, registrationId);
        
        log.info("Processing OAuth2 user from {}: email={}, name={}", registrationId, email, name);
        
        if (email == null || email.trim().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> createNewOAuth2User(email, name, registrationId));
        
        // Update user information if needed
        updateUserFromOAuth2(user, oauth2User, registrationId);
        
        return oauth2User;
    }

    private String extractEmail(OAuth2User oauth2User, String registrationId) {
        switch (registrationId) {
            case "google":
                return oauth2User.getAttribute("email");
            case "facebook":
                return oauth2User.getAttribute("email");
            default:
                return oauth2User.getAttribute("email");
        }
    }

    private String extractName(OAuth2User oauth2User, String registrationId) {
        switch (registrationId) {
            case "google":
                return oauth2User.getAttribute("name");
            case "facebook":
                return oauth2User.getAttribute("name");
            default:
                return oauth2User.getAttribute("name");
        }
    }

    private User createNewOAuth2User(String email, String name, String provider) {
        log.info("Creating new OAuth2 user: email={}, provider={}", email, provider);
        
        User user = new User();
        user.setEmail(email.toLowerCase());
        
        // Parse name into first and last name
        if (name != null && !name.trim().isEmpty()) {
            String[] nameParts = name.trim().split("\\s+", 2);
            user.setFirstname(nameParts[0]);
            user.setLastname(nameParts.length > 1 ? nameParts[1] : "");
        } else {
            user.setFirstname("User");
            user.setLastname("");
        }
        
        // Generate random password for OAuth2 users (they won't use it for login)
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        
        // Generate a unique temporary phone number (will need to be updated by user)
        String uniquePhone = generateUniquePhone();
        user.setPhone(uniquePhone);
        
        user.setRole(UserRole.CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());
        
        try {
            user = userRepository.save(user);
            log.info("Successfully created OAuth2 user with ID: {}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("Error creating OAuth2 user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to create user account");
        }
    }

    private void updateUserFromOAuth2(User user, OAuth2User oauth2User, String registrationId) {
        boolean updated = false;
        
        // Update avatar if available
        String avatarUrl = extractAvatarUrl(oauth2User, registrationId);
        if (avatarUrl != null && !avatarUrl.equals(user.getAvatar())) {
            user.setAvatar(avatarUrl);
            updated = true;
        }
        
        // Update name if it's different
        String name = extractName(oauth2User, registrationId);
        if (name != null && !name.trim().isEmpty()) {
            String[] nameParts = name.trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            if (!firstName.equals(user.getFirstname()) || !lastName.equals(user.getLastname())) {
                user.setFirstname(firstName);
                user.setLastname(lastName);
                updated = true;
            }
        }
        
        if (updated) {
            try {
                userRepository.save(user);
                log.info("Updated OAuth2 user information for: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error updating OAuth2 user: {}", e.getMessage());
            }
        }
    }

    private String extractAvatarUrl(OAuth2User oauth2User, String registrationId) {
        switch (registrationId) {
            case "google":
                return oauth2User.getAttribute("picture");
            case "facebook":
                // Facebook graph API format for profile picture
                String facebookId = oauth2User.getAttribute("id");
                return facebookId != null ? "https://graph.facebook.com/" + facebookId + "/picture?type=large" : null;
            default:
                return null;
        }
    }

    /**
     * Generate a unique temporary phone number for OAuth2 users
     * Format: 09XXXXXXXX (10 digits Vietnamese mobile format)
     */
    private String generateUniquePhone() {
        // Use current timestamp to ensure uniqueness
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        
        // Take last 8 digits and prepend with "09" for Vietnamese mobile format
        String phone = "09" + timestampStr.substring(timestampStr.length() - 8);
        
        log.info("Generated unique phone for OAuth2 user: {}", phone);
        return phone;
    }
}
