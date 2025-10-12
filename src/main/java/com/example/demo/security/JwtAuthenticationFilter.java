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
        } else {
            // Try to get JWT from cookie
            jwt = getJwtFromCookie(request);
        }
        
        // Skip filter if no JWT token found
        if (jwt == null) {
            
            // Check if there's a session-based authentication
            String sessionToken = (String) request.getSession().getAttribute("authToken");
            String sessionEmail = (String) request.getSession().getAttribute("userEmail");
            
            if (sessionToken != null && sessionEmail != null) {
                try {
                    if (jwtService.validateToken(sessionToken)) {
                        var userRole = jwtService.extractRole(sessionToken);
                        var userId = jwtService.extractUserId(sessionToken);
                        
                        List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + userRole.name().toUpperCase())
                        );
                        
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(sessionEmail, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        request.setAttribute("userId", userId);
                        request.setAttribute("userRole", userRole);
                        
                    // Also set session attributes for Thymeleaf access
                    request.getSession().setAttribute("userId", userId);
                    request.getSession().setAttribute("userRole", userRole.name());
                    log.info("Set session attributes - userId: {}, userRole: {}", userId, userRole.name());
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Session-based JWT authentication successful for user: {}", sessionEmail);
                    }
                } catch (Exception e) {
                    log.warn("Session token validation failed: {}", e.getMessage());
                }
            }
            
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract email from JWT token
            userEmail = jwtService.extractEmail(jwt);
            
            // If token is valid and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Validate token
                if (jwtService.validateToken(jwt)) {
                    
                    // Extract user role from token
                    var userRole = jwtService.extractRole(jwt);
                    var userId = jwtService.extractUserId(jwt);
                    
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
                    
                    // Also set session attributes for Thymeleaf access
                    request.getSession().setAttribute("userId", userId);
                    request.getSession().setAttribute("userRole", userRole.name());
                    log.info("Set session attributes from JWT - userId: {}, userRole: {}", userId, userRole.name());
                    
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
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
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
