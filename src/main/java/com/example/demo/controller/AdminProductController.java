package com.example.demo.controller;

import com.example.demo.dto.AdminProductDTO;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.entity.Catalog;
import com.example.demo.entity.Product;
import com.example.demo.entity.enums.ProductStatus;
import com.example.demo.repository.CatalogRepository;
import com.example.demo.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CatalogRepository catalogRepository;

    /**
     * Admin Products Management Page
     */
    @GetMapping({"", "/"})
    public String productsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {
        
        // Create sort object
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get products with filters
        Page<Product> productsPage;
        if (search != null && !search.trim().isEmpty()) {
            productsPage = productService.searchProducts(search.trim(), pageable);
        } else if (status != null && !status.isEmpty()) {
            try {
                ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
                productsPage = productService.getProductsByStatus(productStatus, pageable);
            } catch (IllegalArgumentException e) {
                productsPage = productService.getAllProducts(pageable);
            }
        } else {
            productsPage = productService.getAllProducts(pageable);
        }
        
        // Get statistics for dashboard cards
        Map<String, Object> statistics = productService.getProductStatistics();
        
        // Add attributes to model
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("productStatuses", ProductStatus.values());
        
        // Breadcrumbs
        model.addAttribute("pageTitle", "Quản lý Sản phẩm");
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Sản phẩm", "/admin/products"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        // Set page title and content template for admin layout
        model.addAttribute("pageTitle", "Quản lý Sản phẩm");
        model.addAttribute("contentTemplate", "admin/products/index");
        model.addAttribute("currentPath", "/admin/products");
        
        return "layouts/admin";
    }

    // Product Detail Page removed - now using modal dialog instead

    // ==================== REST API ENDPOINTS ====================

    /**
     * Get products list for AJAX pagination
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Page<Product>>> getProductsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Product> productsPage;
            if (search != null && !search.trim().isEmpty()) {
                productsPage = productService.searchProducts(search.trim(), pageable);
            } else if (status != null && !status.isEmpty()) {
                try {
                    ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
                    productsPage = productService.getProductsByStatus(productStatus, pageable);
                } catch (IllegalArgumentException e) {
                    productsPage = productService.getAllProducts(pageable);
                }
            } else {
                productsPage = productService.getAllProducts(pageable);
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(productsPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi tải danh sách sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Get product statistics
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getProductStatistics() {
        try {
            Map<String, Object> statistics = productService.getProductStatistics();
            return ResponseEntity.ok(ResponseWrapper.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi tải thống kê: " + e.getMessage()));
        }
    }

    /**
     * Create new product
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Product>> createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "0") Integer stockQuantity,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(defaultValue = "500") Integer weightG,
            @RequestParam(defaultValue = "20") Integer lengthCm,
            @RequestParam(defaultValue = "20") Integer widthCm,
            @RequestParam(defaultValue = "30") Integer heightCm,
            @RequestParam(required = false) Long catalogId) {
        
        try {
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Tên sản phẩm không được để trống"));
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Giá sản phẩm không hợp lệ"));
            }
            
            // Create product
            Product product = productService.createProduct(
                name.trim(), description, price, stockQuantity, 
                ProductStatus.valueOf(status.toUpperCase()),
                image, weightG, lengthCm, widthCm, heightCm, catalogId
            );
            
            return ResponseEntity.ok(ResponseWrapper.success(product, "Tạo sản phẩm thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi tạo sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Update product
     */
    @PutMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Product>> updateProduct(
            @PathVariable Long productId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer stockQuantity,
            @RequestParam String status,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam Integer weightG,
            @RequestParam Integer lengthCm,
            @RequestParam Integer widthCm,
            @RequestParam Integer heightCm,
            @RequestParam(required = false) Long catalogId) {
        
        try {
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Tên sản phẩm không được để trống"));
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Giá sản phẩm không hợp lệ"));
            }
            
            // Update product
            Product product = productService.updateProduct(
                productId, name.trim(), description, price, stockQuantity,
                ProductStatus.valueOf(status.toUpperCase()),
                image, weightG, lengthCm, widthCm, heightCm, catalogId
            );
            
            return ResponseEntity.ok(ResponseWrapper.success(product, "Cập nhật sản phẩm thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi cập nhật sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Update product status
     */
    @PutMapping("/api/{productId}/status")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Product>> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam String status) {
        
        try {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            Product product = productService.updateProductStatus(productId, productStatus);
            
            return ResponseEntity.ok(ResponseWrapper.success(product, "Cập nhật trạng thái thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Trạng thái không hợp lệ"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi cập nhật trạng thái: " + e.getMessage()));
        }
    }

    /**
     * Update product stock
     */
    @PutMapping("/api/{productId}/stock")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Product>> updateProductStock(
            @PathVariable Long productId,
            @RequestParam Integer stockQuantity) {
        
        try {
            if (stockQuantity < 0) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Số lượng tồn kho không được âm"));
            }
            
            Product product = productService.updateProductStock(productId, stockQuantity);
            
            return ResponseEntity.ok(ResponseWrapper.success(product, "Cập nhật tồn kho thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi cập nhật tồn kho: " + e.getMessage()));
        }
    }

    /**
     * Delete product
     */
    @DeleteMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Void>> deleteProduct(@PathVariable Long productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.ok(ResponseWrapper.success(null, "Sản phẩm đã được xóa thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<AdminProductDTO>> getProduct(@PathVariable Long productId) {
        try {
            Map<String, Object> result = productService.getProductByIdWithRating(productId);
            
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseWrapper.error("Không tìm thấy sản phẩm"));
            }
            
            Product product = (Product) result.get("product");
            Double averageRating = (Double) result.get("averageRating");
            Long reviewCount = (Long) result.get("reviewCount");
            
            AdminProductDTO dto = AdminProductDTO.fromEntityWithRating(product, averageRating, reviewCount);
            
            return ResponseEntity.ok(ResponseWrapper.success(dto));
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi tải sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Get all catalogs for dropdown
     */
    @GetMapping("/api/catalogs")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<List<Catalog>>> getCatalogs() {
        try {
            List<Catalog> catalogs = catalogRepository.findAll();
            return ResponseEntity.ok(ResponseWrapper.success(catalogs));
        } catch (Exception e) {
            log.error("Error fetching catalogs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi tải danh mục: " + e.getMessage()));
        }
    }

    // Helper class for breadcrumbs
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
     * Export Products (placeholder for future implementation)
     */
    @GetMapping("/api/export")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<String>> exportProducts(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        try {
            // TODO: Implement actual export functionality
            // This is a placeholder that returns a success message
            
            log.info("Export request - format: {}, status: {}, search: {}", format, status, search);
            
            String message = String.format("Xuất dữ liệu sản phẩm định dạng %s đã được lên lịch. " +
                "Bạn sẽ nhận được thông báo khi hoàn thành.", format.toUpperCase());
            
            return ResponseEntity.ok(ResponseWrapper.success(message));
            
        } catch (Exception e) {
            log.error("Error in export request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra khi xuất dữ liệu"));
        }
    }
}
