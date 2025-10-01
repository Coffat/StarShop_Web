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

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/h2-console/**", "/ws/**", "/api/auth/**", "/logout", "/api/wishlist/**", "/api/favorite/**", "/api/cart/**", "/api/orders/**", "/api/payment/**", "/swagger-ui/**", "/v3/api-docs/**")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // Configure fixation first so we don't need to call back with .and()
                .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                .invalidSessionUrl("/login?expired")
                // Configure concurrency last; avoid deprecated .and()
                .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                    .sessionRegistry(sessionRegistry())
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints as per rules.mdc
                .requestMatchers("/", "/health", "/info", "/error").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/forgot-password", "/api/auth/verify-otp", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/health", "/api/info").permitAll()
                .requestMatchers("/public/**").permitAll()
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
                
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // Payment callbacks
                .requestMatchers("/payment/momo/**").permitAll()
                
                // WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                
                // Account pages - require authentication
                .requestMatchers("/account/**").authenticated()
                
                // Order pages - require authentication
                .requestMatchers("/orders", "/orders/**", "/checkout").authenticated()
                
                // Products pages
                .requestMatchers("/products", "/products/**").permitAll()
                
                // Categories page
                .requestMatchers("/categories").permitAll()
                
                // Protected API endpoints as per rules.mdc
                .requestMatchers("/api/users/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/products/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/payment/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                .requestMatchers("/api/reviews/**").hasRole("CUSTOMER")
                .requestMatchers("/api/wishlist/**").hasRole("CUSTOMER")
                .requestMatchers("/api/favorite/**").hasRole("CUSTOMER")
                .requestMatchers("/api/messages/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
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
                        System.err.println("Error in authentication entry point: " + e.getMessage());
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
        return new com.example.demo.config.CustomOAuth2UserService(userRepository, passwordEncoder());
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, 
                org.springframework.security.core.Authentication authentication) -> {
            try {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String email = oauth2User.getAttribute("email");
                
                System.out.println("OAuth2 Success - Email: " + email);
                System.out.println("OAuth2 User Attributes: " + oauth2User.getAttributes());
                
                if (email == null || email.isEmpty()) {
                    System.out.println("No email found, redirecting to error");
                    response.sendRedirect("/login?error=oauth2_no_email");
                    return;
                }
                
                // Find user (should exist after CustomOAuth2UserService processing)
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    System.out.println("User not found after OAuth2 processing, redirecting to error");
                    response.sendRedirect("/login?error=oauth2_user_not_found");
                    return;
                }
                
                System.out.println("User found: " + user.getEmail());
                
                // Generate JWT token
                String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
                System.out.println("JWT token generated: " + token.substring(0, 20) + "...");
                
                // Set authentication in session
                request.getSession().setAttribute("authToken", token);
                request.getSession().setAttribute("userEmail", user.getEmail());
                request.getSession().setAttribute("userRole", user.getRole().name());
                request.getSession().setAttribute("userId", user.getId());
                
                // Redirect to home page
                response.sendRedirect("/?oauth2=success");
                
            } catch (Exception e) {
                System.out.println("OAuth2 error: " + e.getMessage());
                e.printStackTrace();
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
