package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.dto.FavoriteDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.FollowRepository;
import com.example.demo.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Account Controller for user account management pages
 * Following rules.mdc specifications for MVC pattern
 */
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FollowRepository followRepository;
    private final FavoriteService favoriteService;

    /**
     * Serve account information page
     * Only accessible to authenticated users
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String accountProfile(Authentication authentication, Model model) {
        log.info("Account profile page accessed by user: {}", authentication.getName());
        
        // Use optimized query to get user profile with statistics in one query
        UserProfileDTO userProfile = userRepository.findUserProfileByEmail(authentication.getName()).orElse(null);
        if (userProfile != null) {
            User user = userProfile.getUser();
            
            // User basic info
            model.addAttribute("userObject", user);
            model.addAttribute("userFullName", user.getFullName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPhone", user.getPhone());
            model.addAttribute("userRole", user.getRole().getDisplayName());
            model.addAttribute("memberSince", user.getCreatedAt() != null ? 
                user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy")) : "2024");
            
            // Get primary address - already fetched with JOIN FETCH
            if (!user.getAddresses().isEmpty()) {
                model.addAttribute("userAddress", user.getAddresses().get(0).getFullAddress());
            } else {
                model.addAttribute("userAddress", "");
            }
            
            // Use pre-calculated statistics from DTO
            model.addAttribute("ordersCount", userProfile.getOrdersCount());
            model.addAttribute("wishlistCount", userProfile.getWishlistCount());
            model.addAttribute("cartCount", userProfile.getCartItemsCount());
        }
        
        // Authentication info
        model.addAttribute("isUserAuthenticated", authentication.isAuthenticated());
        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("pageTitle", "Thông tin tài khoản");
        return "account/profile";
    }

    /**
     * Serve account settings page
     * Only accessible to authenticated users
     */
    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String accountSettings(Authentication authentication, Model model) {
        log.info("Account settings page accessed by user: {}", authentication.getName());
        
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("userObject", user);
            
            // Get user statistics for sidebar
            Long ordersCount = orderRepository.countOrdersByUser(user.getId());
            Long wishlistCount = followRepository.countByUserId(user.getId());
            
            model.addAttribute("ordersCount", ordersCount);
            model.addAttribute("wishlistCount", wishlistCount);
        }
        
        model.addAttribute("isUserAuthenticated", authentication.isAuthenticated());
        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("pageTitle", "Cài đặt tài khoản");
        return "account/settings";
    }

    /**
     * Serve order history page
     * Only accessible to authenticated users
     */
    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String accountOrders(Authentication authentication, Model model) {
        log.info("Account orders page accessed by user: {}", authentication.getName());
        
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("userObject", user);
            
            // Get user's orders and statistics
            Long ordersCount = orderRepository.countOrdersByUser(user.getId());
            Long wishlistCount = followRepository.countByUserId(user.getId());
            
            model.addAttribute("ordersCount", ordersCount);
            model.addAttribute("wishlistCount", wishlistCount);
            
            // TODO: Add actual orders data from database
            // List<Order> userOrders = orderRepository.findByUserId(user.getId(), PageRequest.of(0, 10));
            // model.addAttribute("orders", userOrders);
        }
        
        model.addAttribute("isUserAuthenticated", authentication.isAuthenticated());
        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("pageTitle", "Lịch sử đơn hàng");
        return "account/orders";
    }

    /**
     * Serve wishlist page
     * Only accessible to authenticated users
     */
    @GetMapping("/wishlist")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String accountWishlist(Authentication authentication, Model model) {
        log.info("Account wishlist page accessed by user: {}", authentication.getName());
        
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("userObject", user);
            
            // Get user's wishlist and statistics using FavoriteService
            Long ordersCount = orderRepository.countOrdersByUser(user.getId());
            Long wishlistCount = favoriteService.getUserFavoriteCount(user.getId());
            List<FavoriteDTO> favoritesList = favoriteService.getUserFavorites(user.getId());
            
            model.addAttribute("ordersCount", ordersCount);
            model.addAttribute("wishlistCount", wishlistCount.intValue());
            model.addAttribute("totalFavorites", wishlistCount);
            model.addAttribute("favoritesList", favoritesList);
        }
        
        model.addAttribute("isUserAuthenticated", authentication.isAuthenticated());
        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("pageTitle", "Danh sách yêu thích");
        return "account/wishlist";
    }
}
