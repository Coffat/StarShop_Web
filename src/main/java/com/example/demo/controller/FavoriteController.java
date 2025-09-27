package com.example.demo.controller;

import com.example.demo.dto.FavoriteDTO;
import com.example.demo.dto.FavoriteRequest;
import com.example.demo.dto.FavoriteResponse;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FavoriteService;
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
 * Controller for managing user favorites/wishlist functionality
 * Provides both web pages and REST API endpoints
 */
@Controller
@RequestMapping("/favorites")
@Validated
public class FavoriteController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Display user's favorites page
     */
    @GetMapping
    public String favoritesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "followedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication,
            Model model) {
        
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login?returnUrl=/favorites";
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // Setup pagination and sorting
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            // Get user's favorites
            Page<FavoriteDTO> favorites = favoriteService.getUserFavorites(user.getId(), pageable);
            Long totalFavorites = favoriteService.getUserFavoriteCount(user.getId());
            
            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Sản phẩm yêu thích", "/favorites");
            
            // Add model attributes
            model.addAttribute("favorites", favorites);
            model.addAttribute("totalFavorites", totalFavorites);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", favorites.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sort);
            model.addAttribute("sortDirection", direction);
            model.addAttribute("hasFavorites", !favorites.isEmpty());
            
            // SEO attributes
            model.addAttribute("pageTitle", "Sản phẩm yêu thích - StarShop");
            model.addAttribute("pageDescription", "Danh sách các sản phẩm bạn đã yêu thích tại StarShop");
            
            logger.info("Displaying favorites page for user: {} with {} items", user.getEmail(), favorites.getTotalElements());
            
            return "favorites/index";
            
        } catch (Exception e) {
            logger.error("Error displaying favorites page: {}", e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách yêu thích");
            return "error/500";
        }
    }
    
    /**
     * REST API: Add product to favorites
     */
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> addToFavorites(
            @Valid @RequestBody FavoriteRequest request,
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
            
            FavoriteResponse response = favoriteService.addToFavorites(user.getId(), request.getProductId());
            
            if (response.isSuccess()) {
                logger.info("User {} added product {} to favorites", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error adding to favorites: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi thêm vào danh sách yêu thích"));
        }
    }
    
    /**
     * REST API: Remove product from favorites
     */
    @DeleteMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> removeFromFavorites(
            @Valid @RequestBody FavoriteRequest request,
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
            
            FavoriteResponse response = favoriteService.removeFromFavorites(user.getId(), request.getProductId());
            
            if (response.isSuccess()) {
                logger.info("User {} removed product {} from favorites", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error removing from favorites: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi xóa khỏi danh sách yêu thích"));
        }
    }
    
    /**
     * REST API: Toggle favorite status
     */
    @PostMapping("/api/toggle")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> toggleFavorite(
            @Valid @RequestBody FavoriteRequest request,
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
            
            FavoriteResponse response = favoriteService.toggleFavorite(user.getId(), request.getProductId());
            
            if (response.isSuccess()) {
                logger.info("User {} toggled favorite for product {}", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error toggling favorite: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi thay đổi trạng thái yêu thích"));
        }
    }
    
    /**
     * REST API: Get favorite status for a product
     */
    @GetMapping("/api/status/{productId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> getFavoriteStatus(
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
            
            FavoriteResponse response = favoriteService.getFavoriteStatus(user.getId(), productId);
            
            return ResponseEntity.ok(ResponseWrapper.success(response));
            
        } catch (Exception e) {
            logger.error("Error getting favorite status: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy trạng thái yêu thích"));
        }
    }
    
    /**
     * REST API: Get user's favorites list
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<List<FavoriteDTO>>> getFavoritesList(
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
            
            List<FavoriteDTO> favorites = favoriteService.getUserFavorites(user.getId());
            
            return ResponseEntity.ok(ResponseWrapper.success(favorites));
            
        } catch (Exception e) {
            logger.error("Error getting favorites list: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy danh sách yêu thích"));
        }
    }
    
    /**
     * REST API: Clear all favorites
     */
    @DeleteMapping("/api/clear")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> clearFavorites(
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
            
            FavoriteResponse response = favoriteService.clearUserFavorites(user.getId());
            
            if (response.isSuccess()) {
                logger.info("User {} cleared all favorites", user.getEmail());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error clearing favorites: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi xóa danh sách yêu thích"));
        }
    }
}
