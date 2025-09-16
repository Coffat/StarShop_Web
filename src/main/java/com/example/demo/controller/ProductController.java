
package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller for handling product-related web requests
 * Following rules.mdc specifications for presentation tier
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController extends BaseController {

    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    /**
     * Display products listing page
     * @param page Page number (default: 0)
     * @param size Page size (default: 12)
     * @param sort Sort criteria (default: newest)
     * @param direction Sort direction (default: desc)
     * @param search Search keyword
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     * @param model Spring Model
     * @return Products listing template
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model) {
        
        try {
            log.info("Products listing request - page: {}, size: {}, sort: {}, search: '{}'", 
                    page, size, sort, search);

            // Create pageable with bounds checking
            int validPage = Math.max(0, page);
            int validSize = Math.min(Math.max(1, size), 50); // Limit max size to 50
            Pageable pageable = PageRequest.of(validPage, validSize);

            Page<Product> productsPage;
            
            // Apply search if provided
            if (search != null && !search.trim().isEmpty()) {
                productsPage = productService.searchProducts(search, pageable);
                model.addAttribute("searchQuery", search);
                log.info("Search performed for: '{}', found {} products", search, productsPage.getTotalElements());
            } else {
                // Apply sorting
                productsPage = productService.getProductsSorted(sort, direction, pageable);
            }

            // Apply price filter if provided (for display purposes, actual filtering would need custom query)
            if (minPrice != null || maxPrice != null) {
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);
            }

            // Add model attributes
            model.addAttribute("productsPage", productsPage);
            model.addAttribute("products", productsPage.getContent());
            model.addAttribute("currentPage", validPage);
            model.addAttribute("totalPages", productsPage.getTotalPages());
            model.addAttribute("totalElements", productsPage.getTotalElements());
            model.addAttribute("pageSize", validSize);
            model.addAttribute("sortBy", sort);
            model.addAttribute("sortDirection", direction);
            
            // Pagination helper attributes
            model.addAttribute("hasPrevious", productsPage.hasPrevious());
            model.addAttribute("hasNext", productsPage.hasNext());
            model.addAttribute("isFirst", productsPage.isFirst());
            model.addAttribute("isLast", productsPage.isLast());

            // Add breadcrumb
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Sản phẩm", "/products");

            // Set page metadata
            model.addAttribute("pageTitle", search != null && !search.trim().isEmpty() ? 
                    "Tìm kiếm: " + search : "Tất cả sản phẩm");
            model.addAttribute("pageDescription", "Khám phá bộ sưu tập hoa tươi đa dạng tại StarShop. " +
                    "Hoa sinh nhật, hoa tình yêu, hoa cưới và nhiều loại hoa khác với chất lượng tốt nhất.");

            log.info("Products listing completed - showing {} products on page {}/{}", 
                    productsPage.getContent().size(), validPage + 1, productsPage.getTotalPages());

            return "products/index";

        } catch (Exception e) {
            log.error("Error loading products listing: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải danh sách sản phẩm. Vui lòng thử lại sau.");
            return "error/500";
        }
    }

    /**
     * Display single product detail page
     * @param id Product ID
     * @param model Spring Model
     * @return Product detail template
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            log.info("Product detail request for ID: {}", id);

            // Validate product ID
            if (id == null || id <= 0) {
                log.warn("Invalid product ID: {}", id);
                model.addAttribute("errorMessage", "Sản phẩm không tồn tại.");
                return "error/404";
            }

            // Get product with rating information
            ProductService.ProductWithRating productWithRating = productService.getProductWithRating(id);
            if (productWithRating == null || productWithRating.getProduct() == null) {
                log.warn("Product not found with ID: {}", id);
                model.addAttribute("errorMessage", "Sản phẩm không tồn tại.");
                return "error/404";
            }

            Product product = productWithRating.getProduct();
            
            // Get product reviews with pagination
            Pageable reviewPageable = PageRequest.of(0, 10);
            Page<Review> reviewsPage = reviewRepository.findByProductId(id, reviewPageable);

            // Get related products (same category or similar)
            List<Product> relatedProducts = productService.getLatestProducts(4);
            
            // Remove current product from related products
            relatedProducts.removeIf(p -> p.getId().equals(id));
            if (relatedProducts.size() > 3) {
                relatedProducts = relatedProducts.subList(0, 3);
            }

            // Add model attributes
            model.addAttribute("product", product);
            model.addAttribute("averageRating", productWithRating.getAverageRating());
            model.addAttribute("reviewCount", productWithRating.getReviewCount());
            model.addAttribute("reviews", reviewsPage.getContent());
            model.addAttribute("reviewsPage", reviewsPage);
            model.addAttribute("relatedProducts", relatedProducts);

            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Sản phẩm", "/products");
            addBreadcrumb(model, product.getName(), "/products/" + id);

            // Set page metadata
            model.addAttribute("pageTitle", product.getName());
            model.addAttribute("pageDescription", product.getDescription() != null ? 
                    product.getDescription().substring(0, Math.min(product.getDescription().length(), 160)) : 
                    "Chi tiết sản phẩm " + product.getName() + " tại StarShop");

            log.info("Product detail loaded successfully for: {} (ID: {})", product.getName(), id);

            return "products/detail";

        } catch (Exception e) {
            log.error("Error loading product detail for ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải thông tin sản phẩm. Vui lòng thử lại sau.");
            return "error/500";
        }
    }

    /**
     * Search products endpoint for AJAX requests
     * @param q Search query
     * @param page Page number
     * @param size Page size
     * @return JSON response with products
     */
    @GetMapping("/search")
    @ResponseBody
    public Page<Product> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        try {
            log.info("AJAX search request - query: '{}', page: {}, size: {}", q, page, size);
            
            int validPage = Math.max(0, page);
            int validSize = Math.min(Math.max(1, size), 50);
            Pageable pageable = PageRequest.of(validPage, validSize);
            
            if (q != null && !q.trim().isEmpty()) {
                return productService.searchProducts(q.trim(), pageable);
            } else {
                return productService.findAll(pageable);
            }
            
        } catch (Exception e) {
            log.error("Error in AJAX search: {}", e.getMessage(), e);
            // Return empty page on error
            return Page.empty();
        }
    }

    /**
     * Get product categories (placeholder for future implementation)
     * @param model Spring Model
     * @return Categories page
     */
    @GetMapping("/categories")
    public String categories(Model model) {
        try {
            log.info("Categories page request");

            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Sản phẩm", "/products");
            addBreadcrumb(model, "Danh mục", "/products/categories");

            // Set page metadata
            model.addAttribute("pageTitle", "Danh mục sản phẩm");
            model.addAttribute("pageDescription", "Khám phá các danh mục hoa tươi đa dạng tại StarShop");

            // For now, redirect to main products page
            // TODO: Implement proper category system when Category entity is added
            return "redirect:/products";

        } catch (Exception e) {
            log.error("Error loading categories: {}", e.getMessage(), e);
            return "redirect:/products";
        }
    }
}
