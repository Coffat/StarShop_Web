package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.Address;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.FollowRepository;
import com.example.demo.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

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
    private final AddressRepository addressRepository;

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
            
            // Get default address - already fetched with JOIN FETCH
            if (!user.getAddresses().isEmpty()) {
                // Try to find default address first
                Address defaultAddress = user.getAddresses().stream()
                    .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                    .findFirst()
                    .orElse(user.getAddresses().get(0)); // Fallback to first address
                
                model.addAttribute("userAddress", defaultAddress.getFullAddress());
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
     * Update user profile information
     * Only accessible to authenticated users
     */
    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public String updateProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("street") String street,
            @RequestParam("city") String city,
            @RequestParam("province") String province,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        log.info("Profile update requested by user: {}", authentication.getName());
        
        try {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng");
                return "redirect:/account/profile";
            }
            
            // Update user information
            user.setFirstname(firstName.trim());
            user.setLastname(lastName.trim());
            user.setPhone(phone.trim());
            
            // Save user
            userRepository.save(user);
            
            // Handle address update
            if (street != null && !street.trim().isEmpty()) {
                log.info("Address update requested - Street: {}, City: {}, Province: {}", street.trim(), city, province);
                
                // Check if user already has a default address
                Address existingAddress = addressRepository.findDefaultAddressByUserId(user.getId()).orElse(null);
                
                if (existingAddress != null) {
                    // Update existing address
                    existingAddress.setStreet(street.trim());
                    existingAddress.setCity(city != null ? city.trim() : "Thành phố Hồ Chí Minh");
                    existingAddress.setProvince(province != null ? province.trim() : "TP. Hồ Chí Minh");
                    addressRepository.save(existingAddress);
                    log.info("Updated existing address for user: {}", user.getEmail());
                } else {
                    // Create new default address
                    Address newAddress = new Address();
                    newAddress.setStreet(street.trim());
                    newAddress.setCity(city != null ? city.trim() : "Thành phố Hồ Chí Minh");
                    newAddress.setProvince(province != null ? province.trim() : "TP. Hồ Chí Minh");
                    newAddress.setIsDefault(true);
                    newAddress.setUser(user);
                    addressRepository.save(newAddress);
                    log.info("Created new default address for user: {}", user.getEmail());
                }
            }
            
            log.info("Profile updated successfully for user: {}", authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
            
        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin. Vui lòng thử lại.");
        }
        
        return "redirect:/account/profile";
    }

    // Wishlist functionality moved to WishlistController (/wishlist)
}
