package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security Configuration with JWT and OAuth2 support
 * Following rules.mdc specifications for Spring Security 6.3.4
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final com.example.demo.service.CustomUserDetailsService customUserDetailsService;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/h2-console/**", "/ws/**", "/api/auth/**", "/logout", "/api/wishlist/**", "/api/favorite/**", "/api/cart/**", "/api/orders/**", "/api/payment/**", "/api/locations/**", "/api/addresses/**", "/api/shipping/**", "/admin/orders/api/**", "/admin/products/api/**", "/admin/api/**", "/sse/**", "/swagger-ui/**", "/v3/api-docs/**")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // Configure fixation first so we don't need to call back with .and()
                .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                // Configure concurrency last; avoid deprecated .and()
                .maximumSessions(1)
                    .sessionRegistry(sessionRegistry())
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints as per rules.mdc
                .requestMatchers("/", "/health", "/info", "/error").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/verify-registration", "/api/auth/forgot-password", "/api/auth/verify-otp", "/api/auth/reset-password").permitAll()
                // Payment callbacks - MUST BE FIRST to avoid authentication
                .requestMatchers("/payment/momo/**").permitAll()
                
                // Public pages
                .requestMatchers("/", "/home", "/about", "/contact", "/help/**").permitAll()
                
                // H2 Console (for development)
                .requestMatchers("/h2-console/**").permitAll()
                
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/oauth2/authorization/**").permitAll()
                
                // Static resources
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/register", "/forgot-password", "/reset-password", "/logout").permitAll()
                
                // Swagger UI and API Docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                
                // WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                
                // Location APIs - public access for address forms
                .requestMatchers("/api/locations/**").permitAll()
                
                // Debug endpoints - for troubleshooting (remove in production)
                .requestMatchers("/debug/**").permitAll()
                
                // Account pages - require authentication
                .requestMatchers("/account/**").authenticated()
                // Order pages - require authentication
                .requestMatchers("/orders", "/orders/**", "/checkout").authenticated()
                // Admin pages - require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Products pages
                .requestMatchers("/products", "/products/**").permitAll()
                
                // Categories page
                .requestMatchers("/categories").permitAll()
                
                // Protected API endpoints as per rules.mdc
                .requestMatchers("/api/users/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/products/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/payment/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/addresses/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/shipping/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/sse/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/cart/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/wishlist/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/reviews/**").hasRole("CUSTOMER")
                .requestMatchers("/api/favorite/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/messages/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/orders/api/**").hasRole("ADMIN")
                .requestMatchers("/admin/products/api/**").hasRole("ADMIN")
                .requestMatchers("/admin/api/**").hasRole("ADMIN")
                
                // Admin page routes
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .userDetailsService(customUserDetailsService)
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
            )
            .rememberMe(remember -> remember
                .key("unique-and-secret")
                .tokenValiditySeconds(86400)
                .userDetailsService(username -> {
                    // Simple UserDetails implementation for remember-me
                    User user = userRepository.findByEmail(username).orElse(null);
                    if (user != null) {
                        return org.springframework.security.core.userdetails.User.builder()
                            .username(user.getEmail())
                            .password(user.getPassword())
                            .authorities("ROLE_" + user.getRole().name())
                            .build();
                    }
                    return null;
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oauth2SuccessHandler())
                .failureHandler(oauth2FailureHandler())
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService())
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("authToken", "JSESSIONID", "SPRING_SECURITY_REMEMBER_ME_COOKIE")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    try {
                        // Check if this is an API request
                        if (request.getRequestURI().startsWith("/api/")) {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập để sử dụng tính năng này\"}");
                        } else {
                            // Handle authentication entry point - redirect to login for web pages
                            response.sendRedirect("/login?error=authentication_required");
                        }
                    } catch (IOException e) {
                        // Log error and send basic error response
                        response.setStatus(500);
                    }
                })
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // For H2 console
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public com.example.demo.config.CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(userRepository);
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, 
                org.springframework.security.core.Authentication authentication) -> {
            try {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String email = oauth2User.getAttribute("email");
                
                if (email == null || email.isEmpty()) {
                    response.sendRedirect("/login?error=oauth2_no_email");
                    return;
                }
                
                // Find user (should exist after CustomOAuth2UserService processing)
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    response.sendRedirect("/login?error=oauth2_user_not_found");
                    return;
                }
                
                // Generate JWT token
                String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
                
                // Create JWT cookie (same as in AuthController)
                jakarta.servlet.http.Cookie authCookie = new jakarta.servlet.http.Cookie("authToken", token);
                authCookie.setHttpOnly(true);
                authCookie.setSecure(false); // Set to false for localhost development
                authCookie.setPath("/");
                authCookie.setMaxAge(24 * 60 * 60); // 24 hours
                response.addCookie(authCookie);
                
                // Set authentication in session
                request.getSession().setAttribute("authToken", token);
                request.getSession().setAttribute("userEmail", user.getEmail());
                request.getSession().setAttribute("userRole", user.getRole().name());
                request.getSession().setAttribute("userId", user.getId());
                
                // Redirect to home page
                response.sendRedirect("/?oauth2=success");
                
            } catch (Exception e) {
                response.sendRedirect("/login?error=oauth2_processing");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, 
                org.springframework.security.core.AuthenticationException exception) -> {
            try {
                response.sendRedirect("/login?error=oauth2_failed");
            } catch (IOException e) {
                // Log error
                response.sendRedirect("/login?error=oauth2");
            }
        };
    }
}
