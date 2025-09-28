package com.example.demo.controller;

import com.example.demo.dto.WishlistDTO;
import com.example.demo.dto.WishlistRequest;
import com.example.demo.dto.WishlistResponse;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller for managing user wishlist functionality
 * Provides both web pages and REST API endpoints
 */
@Controller
@Validated
public class WishlistController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);
    
    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Display user's wishlist page
     */
    @GetMapping("/wishlist")
    public String wishlistPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "followedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication,
            Model model) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login?returnUrl=/wishlist";
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // Setup pagination and sorting
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            // Get user's wishlist
            Page<WishlistDTO> wishlist = wishlistService.getUserWishlist(user.getId(), pageable);
            Long totalWishlist = wishlistService.getUserWishlistCount(user.getId());
            
            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Danh sách yêu thích", "/wishlist");
            
            // Add model attributes
            model.addAttribute("wishlist", wishlist);
            model.addAttribute("totalWishlist", totalWishlist);
            model.addAttribute("wishlistCount", totalWishlist); // For compatibility
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", wishlist.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sort);
            model.addAttribute("sortDirection", direction);
            model.addAttribute("hasWishlist", !wishlist.isEmpty());
            
            // SEO attributes
            model.addAttribute("pageTitle", "Danh sách yêu thích - StarShop");
            model.addAttribute("pageDescription", "Danh sách các sản phẩm bạn đã yêu thích tại StarShop");
            
            logger.info("Displaying wishlist page for user: {} with {} items", user.getEmail(), wishlist.getTotalElements());
            
            return "account/wishlist";
            
        } catch (Exception e) {
            logger.error("Error displaying wishlist page: {}", e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách yêu thích");
            return "error/500";
        }
    }
    
    /**
     * REST API: Toggle wishlist/favorite status
     */
    @PostMapping("/api/wishlist/toggle")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<WishlistResponse>> toggleWishlist(
            @Valid @RequestBody WishlistRequest request,
            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Vui lòng đăng nhập để sử dụng tính năng này"));
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Người dùng không hợp lệ"));
            }
            
            WishlistResponse response = wishlistService.toggleWishlist(user.getId(), request.getProductId());
            
            if (response.isSuccess()) {
                logger.info("User {} toggled wishlist for product {}", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error toggling wishlist: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi thay đổi trạng thái wishlist"));
        }
    }
    
    /**
     * REST API: Add product to wishlist
     */
    @PostMapping("/api/wishlist/add")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<WishlistResponse>> addToWishlist(
            @Valid @RequestBody WishlistRequest request,
            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Vui lòng đăng nhập để sử dụng tính năng này"));
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Người dùng không hợp lệ"));
            }
            
            WishlistResponse response = wishlistService.addToWishlist(user.getId(), request.getProductId());
            
            if (response.isSuccess()) {
                logger.info("User {} added product {} to wishlist", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error adding to wishlist: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi thêm vào wishlist"));
        }
    }
    
    /**
     * REST API: Remove product from wishlist
     */
    @DeleteMapping("/api/wishlist/remove")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<WishlistResponse>> removeFromWishlist(
            @Valid @RequestBody WishlistRequest request,
            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Vui lòng đăng nhập để sử dụng tính năng này"));
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Người dùng không hợp lệ"));
            }
            
            WishlistResponse response = wishlistService.removeFromWishlist(user.getId(), request.getProductId());
            
            if (response.isSuccess()) {
                logger.info("User {} removed product {} from wishlist", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error removing from wishlist: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi xóa khỏi wishlist"));
        }
    }
    
    /**
     * REST API: Get wishlist status for a product
     */
    @GetMapping("/api/wishlist/status/{productId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<WishlistResponse>> getWishlistStatus(
            @PathVariable Long productId,
            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Vui lòng đăng nhập để sử dụng tính năng này"));
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Người dùng không hợp lệ"));
            }
            
            WishlistResponse response = wishlistService.getWishlistStatus(user.getId(), productId);
            
            return ResponseEntity.ok(ResponseWrapper.success(response));
            
        } catch (Exception e) {
            logger.error("Error getting wishlist status: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy trạng thái wishlist"));
        }
    }
    
    /**
     * REST API: Get user's wishlist list
     */
    @GetMapping("/api/wishlist/list")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<List<WishlistDTO>>> getWishlistList(
            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Vui lòng đăng nhập để sử dụng tính năng này"));
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Người dùng không hợp lệ"));
            }
            
            List<WishlistDTO> wishlist = wishlistService.getUserWishlist(user.getId());
            
            return ResponseEntity.ok(ResponseWrapper.success(wishlist));
            
        } catch (Exception e) {
            logger.error("Error getting wishlist list: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy danh sách yêu thích"));
        }
    }
    
    /**
     * REST API: Clear all wishlist items
     */
    @DeleteMapping("/api/wishlist/clear")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<WishlistResponse>> clearWishlist(
            Authentication authentication) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Vui lòng đăng nhập để sử dụng tính năng này"));
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(ResponseWrapper.error("Người dùng không hợp lệ"));
            }
            
            WishlistResponse response = wishlistService.clearUserWishlist(user.getId());
            
            if (response.isSuccess()) {
                logger.info("User {} cleared all wishlist items", user.getEmail());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error clearing wishlist: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi xóa danh sách yêu thích"));
        }
    }
}
