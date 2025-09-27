package com.example.demo.service;

import com.example.demo.dto.FavoriteDTO;
import com.example.demo.dto.FavoriteResponse;
import com.example.demo.entity.Follow;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.FollowRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing user favorites (Follow entity)
 * Handles business logic for favorite products functionality
 */
@Service
@Transactional
public class FavoriteService {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Add a product to user's favorites
     */
    public FavoriteResponse addToFavorites(Long userId, Long productId) {
        try {
            logger.info("Adding product {} to favorites for user {}", productId, userId);
            
            // Check if already exists
            if (followRepository.existsByUserIdAndProductId(userId, productId)) {
                return FavoriteResponse.error("Sản phẩm đã có trong danh sách yêu thích");
            }
            
            // Validate user exists
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return FavoriteResponse.error("Người dùng không tồn tại");
            }
            
            // Validate product exists
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return FavoriteResponse.error("Sản phẩm không tồn tại");
            }
            
            // Create new follow
            Follow follow = new Follow(userOpt.get(), productOpt.get());
            followRepository.save(follow);
            
            Long favoriteCount = followRepository.countFollowersByProductId(productId);
            
            logger.info("Successfully added product {} to favorites for user {}", productId, userId);
            return FavoriteResponse.success("Đã thêm vào danh sách yêu thích", true, favoriteCount);
            
        } catch (Exception e) {
            logger.error("Error adding product {} to favorites for user {}: {}", productId, userId, e.getMessage());
            return FavoriteResponse.error("Có lỗi xảy ra khi thêm vào danh sách yêu thích");
        }
    }
    
    /**
     * Remove a product from user's favorites
     */
    public FavoriteResponse removeFromFavorites(Long userId, Long productId) {
        try {
            logger.info("Removing product {} from favorites for user {}", productId, userId);
            
            // Check if exists
            if (!followRepository.existsByUserIdAndProductId(userId, productId)) {
                return FavoriteResponse.error("Sản phẩm không có trong danh sách yêu thích");
            }
            
            // Remove from favorites
            followRepository.deleteByUserIdAndProductId(userId, productId);
            
            Long favoriteCount = followRepository.countFollowersByProductId(productId);
            
            logger.info("Successfully removed product {} from favorites for user {}", productId, userId);
            return FavoriteResponse.success("Đã xóa khỏi danh sách yêu thích", false, favoriteCount);
            
        } catch (Exception e) {
            logger.error("Error removing product {} from favorites for user {}: {}", productId, userId, e.getMessage());
            return FavoriteResponse.error("Có lỗi xảy ra khi xóa khỏi danh sách yêu thích");
        }
    }
    
    /**
     * Toggle favorite status of a product for user
     */
    public FavoriteResponse toggleFavorite(Long userId, Long productId) {
        boolean isFavorite = followRepository.existsByUserIdAndProductId(userId, productId);
        
        if (isFavorite) {
            return removeFromFavorites(userId, productId);
        } else {
            return addToFavorites(userId, productId);
        }
    }
    
    /**
     * Check if a product is in user's favorites
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        return followRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    /**
     * Get user's favorite products with pagination
     */
    @Transactional(readOnly = true)
    public Page<FavoriteDTO> getUserFavorites(Long userId, Pageable pageable) {
        try {
            logger.info("Getting favorites for user {} with pagination", userId);
            
            List<Follow> follows = followRepository.findByUserId(userId);
            
            List<FavoriteDTO> favoriteDTOs = follows.stream()
                .map(follow -> FavoriteDTO.fromProduct(
                    follow.getProduct(), 
                    userId, 
                    follow.getId(), 
                    follow.getFollowedAt()
                ))
                .collect(Collectors.toList());
            
            // Apply pagination manually since we're doing complex mapping
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), favoriteDTOs.size());
            
            List<FavoriteDTO> pageContent = favoriteDTOs.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, favoriteDTOs.size());
            
        } catch (Exception e) {
            logger.error("Error getting favorites for user {}: {}", userId, e.getMessage());
            return Page.empty(pageable);
        }
    }
    
    /**
     * Get user's favorite products without pagination
     */
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        try {
            logger.info("Getting all favorites for user {}", userId);
            
            List<Follow> follows = followRepository.findByUserId(userId);
            
            return follows.stream()
                .map(follow -> FavoriteDTO.fromProduct(
                    follow.getProduct(), 
                    userId, 
                    follow.getId(), 
                    follow.getFollowedAt()
                ))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting favorites for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get favorite count for a product
     */
    @Transactional(readOnly = true)
    public Long getFavoriteCount(Long productId) {
        return followRepository.countFollowersByProductId(productId);
    }
    
    /**
     * Get user's favorite count
     */
    @Transactional(readOnly = true)
    public Long getUserFavoriteCount(Long userId) {
        return followRepository.countByUserId(userId);
    }
    
    /**
     * Get favorite status and count for a product
     */
    @Transactional(readOnly = true)
    public FavoriteResponse getFavoriteStatus(Long userId, Long productId) {
        try {
            boolean isFavorite = followRepository.existsByUserIdAndProductId(userId, productId);
            Long favoriteCount = followRepository.countFollowersByProductId(productId);
            
            return FavoriteResponse.success("Status retrieved successfully", isFavorite, favoriteCount);
            
        } catch (Exception e) {
            logger.error("Error getting favorite status for product {} and user {}: {}", productId, userId, e.getMessage());
            return FavoriteResponse.error("Có lỗi xảy ra khi lấy trạng thái yêu thích");
        }
    }
    
    /**
     * Clear all favorites for a user
     */
    public FavoriteResponse clearUserFavorites(Long userId) {
        try {
            logger.info("Clearing all favorites for user {}", userId);
            
            List<Follow> follows = followRepository.findByUserId(userId);
            followRepository.deleteAll(follows);
            
            logger.info("Successfully cleared all favorites for user {}", userId);
            return FavoriteResponse.success("Đã xóa tất cả sản phẩm yêu thích");
            
        } catch (Exception e) {
            logger.error("Error clearing favorites for user {}: {}", userId, e.getMessage());
            return FavoriteResponse.error("Có lỗi xảy ra khi xóa danh sách yêu thích");
        }
    }
}
