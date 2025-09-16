package com.example.demo.security;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.Cookie;
import org.springframework.lang.NonNull;

/**
 * JWT Authentication Filter extending OncePerRequestFilter
 * Following rules.mdc specifications for JWT security
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        final String userEmail;
        
        // Try to get JWT from Authorization header first
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            log.debug("JWT token found in Authorization header");
        } else {
            // Try to get JWT from cookie
            jwt = getJwtFromCookie(request);
            if (jwt != null) {
                log.debug("JWT token found in cookie");
            }
        }
        
        // Skip filter if no JWT token found
        if (jwt == null) {
            log.debug("No JWT token found, continuing without authentication for path: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract email from JWT token
            userEmail = jwtService.extractEmail(jwt);
            log.debug("Extracted email from JWT: {}", userEmail);
            
            // If token is valid and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("User email found and no existing authentication, validating token...");
                
                // Validate token
                if (jwtService.validateToken(jwt)) {
                    log.debug("JWT token is valid, setting up authentication...");
                    
                    // Extract user role from token
                    var userRole = jwtService.extractRole(jwt);
                    var userId = jwtService.extractUserId(jwt);
                    
                    log.debug("Extracted user details - ID: {}, Role: {}", userId, userRole);
                    
                    // Create authority based on user role
                    List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + userRole.name().toUpperCase())
                    );
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);
                    
                    // Set additional details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Add user ID to authentication details for easy access
                    request.setAttribute("userId", userId);
                    request.setAttribute("userRole", userRole);
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("JWT authentication successful for user: {} with role: {} and authorities: {}", 
                        userEmail, userRole, authorities);
                    
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                    // Clear any existing authentication
                    SecurityContextHolder.clearContext();
                }
            } else if (userEmail == null) {
                log.warn("Could not extract email from JWT token");
            } else {
                log.debug("User already authenticated: {}", 
                    SecurityContextHolder.getContext().getAuthentication().getName());
            }
            
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from authToken cookie
     */
    private String getJwtFromCookie(HttpServletRequest request) {
        log.debug("Checking cookies for JWT token...");
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                log.debug("Found cookie: {} = {}", cookie.getName(), 
                    cookie.getValue().length() > 20 ? cookie.getValue().substring(0, 20) + "..." : cookie.getValue());
                if ("authToken".equals(cookie.getName())) {
                    log.debug("Found authToken cookie with value length: {}", cookie.getValue().length());
                    return cookie.getValue();
                }
            }
        } else {
            log.debug("No cookies found in request");
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT filter only for truly public endpoints and static resources
        return path.equals("/health") ||
               path.equals("/info") ||
               path.equals("/error") ||
               path.startsWith("/api/auth/") ||
               path.startsWith("/public/") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/login") ||
               path.startsWith("/register") ||
               path.startsWith("/forgot-password") ||
               path.startsWith("/reset-password") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/ws") ||
               path.startsWith("/actuator/");
    }
}
