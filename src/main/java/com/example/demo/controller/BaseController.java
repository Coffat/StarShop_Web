package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Base controller to add common model attributes to all views
 */
@ControllerAdvice
public class BaseController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Add current path to all views for navigation highlighting
     */
    @ModelAttribute("currentPath")
    public String addCurrentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
    
    /**
     * Add authentication status to all views
     */
    @ModelAttribute("isUserAuthenticated")
    public boolean addAuthenticationStatus(Authentication authentication) {
        return authentication != null && 
               authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }
    
    /**
     * Add username to all views
     */
    @ModelAttribute("currentUser")
    public String addUsername(Authentication authentication) {
        if (authentication != null && 
            authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * Add full user object to all views
     */
    @ModelAttribute("userObject")
    @Transactional(readOnly = true)
    public User addUserObject(Authentication authentication) {
        System.out.println("=== BaseController Debug ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null ? authentication.isAuthenticated() : "null"));
        System.out.println("Principal name: " + (authentication != null ? authentication.getName() : "null"));
        
        if (authentication != null && 
            authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            System.out.println("Looking up user by email: " + authentication.getName());
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            System.out.println("Found user: " + (user != null ? user.getFullName() : "null"));
            return user;
        }
        System.out.println("No authenticated user found");
        return null;
    }
    
    /**
     * Add cart count to all views
     */
    @ModelAttribute("cartCount")
    @Transactional(readOnly = true)
    public int addCartCount(Authentication authentication) {
        try {
            if (authentication != null && 
                authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                User user = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (user != null && user.getCart() != null) {
                    return user.getCart().getTotalItems();
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting cart count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Add wishlist count to all views
     */
    @ModelAttribute("wishlistCount")
    @Transactional(readOnly = true)
    public int addWishlistCount(Authentication authentication) {
        try {
            if (authentication != null && 
                authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                User user = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (user != null && user.getFollows() != null) {
                    return user.getFollows().size();
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting wishlist count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Add orders count to all views
     */
    @ModelAttribute("ordersCount")
    @Transactional(readOnly = true)
    public int addOrdersCount(Authentication authentication) {
        try {
            if (authentication != null && 
                authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                User user = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (user != null && user.getOrders() != null) {
                    return user.getOrders().size();
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting orders count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Breadcrumb item class
     */
    public static class BreadcrumbItem {
        private String title;
        private String url;
        
        public BreadcrumbItem(String title, String url) {
            this.title = title;
            this.url = url;
        }
        
        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }
    
    /**
     * Add breadcrumb item to model
     */
    protected void addBreadcrumb(Model model, String title, String url) {
        @SuppressWarnings("unchecked")
        List<BreadcrumbItem> breadcrumbs = (List<BreadcrumbItem>) model.asMap().get("breadcrumbs");
        if (breadcrumbs == null) {
            breadcrumbs = new ArrayList<>();
            model.addAttribute("breadcrumbs", breadcrumbs);
        }
        breadcrumbs.add(new BreadcrumbItem(title, url));
    }
}
