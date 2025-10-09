package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller to help diagnose authentication and authorization issues
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UserRepository userRepository;

    @GetMapping("/auth")
    public Map<String, Object> debugAuth() {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        result.put("hasAuthentication", auth != null);
        result.put("isAuthenticated", auth != null ? auth.isAuthenticated() : false);
        result.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
        result.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        result.put("name", auth != null ? auth.getName() : "null");
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                User user = userRepository.findByEmail(auth.getName()).orElse(null);
                if (user != null) {
                    result.put("userFound", true);
                    result.put("userRole", user.getRole().toString());
                    result.put("userRoleEnum", user.getRole().name());
                    result.put("userEmail", user.getEmail());
                    result.put("userFullName", user.getFullName());
                } else {
                    result.put("userFound", false);
                }
            } catch (Exception e) {
                result.put("userLookupError", e.getMessage());
            }
        }
        
        return result;
    }

    @GetMapping("/admin-test")
    public Map<String, Object> adminTest() {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            boolean hasAdminRole = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            result.put("hasAdminRole", hasAdminRole);
            result.put("allAuthorities", auth.getAuthorities().toString());
            
            if (hasAdminRole) {
                result.put("message", "You have ADMIN role - should be able to access /admin/**");
            } else {
                result.put("message", "You do NOT have ADMIN role - cannot access /admin/**");
            }
        } else {
            result.put("message", "Not authenticated");
        }
        
        return result;
    }
}
