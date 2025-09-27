package com.example.demo.controller;

import com.example.demo.dto.FavoriteRequest;
import com.example.demo.dto.FavoriteResponse;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST API Controller for wishlist functionality
 * Provides backward compatibility for /api/wishlist endpoints
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    
    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Toggle favorite status (backward compatibility endpoint)
     */
    @PostMapping("/toggle")
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
                logger.info("User {} toggled favorite for product {} via wishlist API", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error toggling favorite via wishlist API: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi thay đổi trạng thái yêu thích"));
        }
    }
    
    /**
     * Add product to wishlist (backward compatibility endpoint)
     */
    @PostMapping("/add")
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> addToWishlist(
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
                logger.info("User {} added product {} to wishlist via API", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error adding to wishlist via API: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi thêm vào danh sách yêu thích"));
        }
    }
    
    /**
     * Remove product from wishlist (backward compatibility endpoint)
     */
    @DeleteMapping("/remove")
    public ResponseEntity<ResponseWrapper<FavoriteResponse>> removeFromWishlist(
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
                logger.info("User {} removed product {} from wishlist via API", user.getEmail(), request.getProductId());
                return ResponseEntity.ok(ResponseWrapper.success(response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error removing from wishlist via API: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi xóa khỏi danh sách yêu thích"));
        }
    }
}
