package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.dto.ProductDetailDTO;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product Service for handling product business logic
 * Following rules.mdc specifications for business tier
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Find all products with pagination
     * @param pageable Pagination information
     * @return Page of products
     */
    public Page<Product> findAll(Pageable pageable) {
        log.info("Fetching products with pagination: page {}, size {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable);
    }

    /**
     * Find product by ID
     * @param id Product ID
     * @return Product if found, null otherwise
     */
    public Product findById(Long id) {
        if (id == null) {
            log.warn("Product ID is null");
            return null;
        }
        
        log.info("Fetching product with ID: {}", id);
        Optional<Product> product = productRepository.findById(id);
        
        if (product.isEmpty()) {
            log.warn("Product not found with ID: {}", id);
            return null;
        }
        
        return product.get();
    }

    /**
     * Search products by keyword with pagination
     * @param keyword Search keyword
     * @param pageable Pagination information
     * @return Page of matching products
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.info("Empty search keyword, returning all products");
            return findAll(pageable);
        }
        
        String cleanKeyword = keyword.trim();
        log.info("Searching products with keyword: '{}', page: {}, size: {}", 
                cleanKeyword, pageable.getPageNumber(), pageable.getPageSize());
        
        return productRepository.searchProducts(cleanKeyword, pageable);
    }

    /**
     * Find products by name containing keyword with pagination
     * @param name Name keyword
     * @param pageable Pagination information
     * @return Page of matching products
     */
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return findAll(pageable);
        }
        
        log.info("Finding products by name containing: '{}'", name.trim());
        return productRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    /**
     * Find products by price range
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of products in price range
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null) minPrice = BigDecimal.ZERO;
        if (maxPrice == null) maxPrice = new BigDecimal("999999999");
        
        log.info("Finding products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Get latest products
     * @param limit Maximum number of products to return
     * @return List of latest products
     */
    public List<Product> getLatestProducts(int limit) {
        log.info("Fetching {} latest products", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findLatestProducts(pageable);
    }

    /**
     * Get best selling products
     * @param limit Maximum number of products to return
     * @return List of best selling products
     */
    public List<Product> getBestSellingProducts(int limit) {
        log.info("Fetching {} best selling products", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellingProducts(pageable);
    }

    /**
     * Get featured products for homepage
     * @return List of featured products
     */
    public List<Product> getFeaturedProducts() {
        log.info("Fetching featured products");
        // Return latest products as featured for now
        return getLatestProducts(8);
    }

    /**
     * Get products with reviews loaded
     * @return List of products with reviews
     */
    public List<Product> getProductsWithReviews() {
        log.info("Fetching products with reviews");
        return productRepository.findAllWithReviews();
    }

    /**
     * Find products by attribute value
     * @param attributeId Attribute ID
     * @param value Attribute value
     * @return List of matching products
     */
    public List<Product> findByAttributeValue(Long attributeId, String value) {
        if (attributeId == null || value == null || value.trim().isEmpty()) {
            log.warn("Invalid attribute search parameters: attributeId={}, value={}", attributeId, value);
            return List.of();
        }
        
        log.info("Finding products by attribute {} with value: {}", attributeId, value);
        return productRepository.findByAttributeValue(attributeId, value.trim());
    }

    /**
     * Get product with average rating and review count using optimized query
     * @param productId Product ID
     * @return ProductDetailDTO with rating information
     */
    public ProductDetailDTO getProductWithRating(Long productId) {
        if (productId == null) {
            log.warn("Product ID is null");
            return null;
        }
        
        log.info("Fetching product detail with rating for ID: {}", productId);
        return productRepository.findProductDetailById(productId).orElse(null);
    }

    /**
     * Get product with average rating and review count (legacy method for backward compatibility)
     * @param productId Product ID
     * @return ProductWithRating with rating information
     */
    public ProductWithRating getProductWithRatingLegacy(Long productId) {
        Product product = findById(productId);
        if (product == null) {
            return null;
        }
        
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        Long reviewCount = reviewRepository.countReviewsByProductId(productId);
        
        return new ProductWithRating(product, 
                averageRating != null ? averageRating : 0.0, 
                reviewCount != null ? reviewCount : 0L);
    }

    /**
     * Get products sorted by different criteria
     * @param sortBy Sort criteria (name, price, rating, newest)
     * @param direction Sort direction (asc, desc)
     * @param pageable Pagination information
     * @return Sorted page of products
     */
    public Page<Product> getProductsSorted(String sortBy, String direction, Pageable pageable) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Sort sort;
        switch (sortBy != null ? sortBy.toLowerCase() : "newest") {
            case "name":
                sort = Sort.by(sortDirection, "name");
                break;
            case "price":
                sort = Sort.by(sortDirection, "price");
                break;
            case "oldest":
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "newest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                sort
        );
        
        log.info("Getting products sorted by: {} {}", sortBy, direction);
        return productRepository.findAll(sortedPageable);
    }

    /**
     * Inner class for product with rating information
     */
    public static class ProductWithRating {
        private final Product product;
        private final Double averageRating;
        private final Long reviewCount;

        public ProductWithRating(Product product, Double averageRating, Long reviewCount) {
            this.product = product;
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        public Product getProduct() { return product; }
        public Double getAverageRating() { return averageRating; }
        public Long getReviewCount() { return reviewCount; }
    }
}
