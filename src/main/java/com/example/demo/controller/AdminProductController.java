package com.example.demo.controller;

import com.example.demo.dto.ResponseWrapper;
import com.example.demo.dto.ProductAnalyticsDTO;
import com.example.demo.dto.ProductAuditLogDTO;
import com.example.demo.dto.BulkUpdateRequest;
import com.example.demo.entity.Product;
import com.example.demo.entity.enums.ProductStatus;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductAnalyticsService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAnalyticsService productAnalyticsService;

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
            @RequestParam(defaultValue = "30") Integer heightCm) {
        
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
                image, weightG, lengthCm, widthCm, heightCm
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
            @RequestParam Integer heightCm) {
        
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
                image, weightG, lengthCm, widthCm, heightCm
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
    public ResponseEntity<ResponseWrapper<Product>> getProduct(@PathVariable Long productId) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (productOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseWrapper.error("Không tìm thấy sản phẩm"));
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(productOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Lỗi khi tải sản phẩm: " + e.getMessage()));
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

    // ========== ADVANCED PRODUCT MANAGEMENT ENDPOINTS ==========

    /**
     * Get Product Analytics
     */
    @GetMapping("/api/analytics")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<List<ProductAnalyticsDTO>>> getProductAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            
            List<ProductAnalyticsDTO> analytics = productAnalyticsService.getProductAnalytics(startDate, endDate);
            
            return ResponseEntity.ok(ResponseWrapper.success(analytics));
            
        } catch (Exception e) {
            log.error("Error getting product analytics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Không thể lấy dữ liệu phân tích sản phẩm"));
        }
    }

    /**
     * Get Product Audit Logs
     */
    @GetMapping("/api/audit-logs")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Page<ProductAuditLogDTO>>> getProductAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productId) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductAuditLogDTO> auditLogs;
            
            if (productId != null) {
                auditLogs = productAnalyticsService.getProductAuditLogs(productId, pageable);
            } else {
                auditLogs = productAnalyticsService.getProductAuditLogs(pageable);
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(auditLogs));
            
        } catch (Exception e) {
            log.error("Error getting audit logs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Không thể lấy lịch sử thay đổi sản phẩm"));
        }
    }

    /**
     * Bulk Update Product Status
     */
    @PutMapping("/api/bulk-status")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<BulkUpdateRequest.BulkUpdateResponse>> bulkUpdateStatus(
            @RequestBody BulkUpdateRequest.BulkStatusUpdate request) {
        
        try {
            // Validate request
            if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Danh sách sản phẩm không được để trống"));
            }
            
            if (request.getNewStatus() == null || request.getNewStatus().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Trạng thái mới không được để trống"));
            }
            
            // Validate status
            try {
                ProductStatus.valueOf(request.getNewStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Trạng thái không hợp lệ: " + request.getNewStatus()));
            }
            
            BulkUpdateRequest.BulkUpdateResponse response = 
                productAnalyticsService.bulkUpdateProductStatus(request);
            
            log.info("Bulk status update completed: {}", response.getSummary());
            
            return ResponseEntity.ok(ResponseWrapper.success(response));
            
        } catch (Exception e) {
            log.error("Error in bulk status update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra khi cập nhật trạng thái sản phẩm"));
        }
    }

    /**
     * Bulk Update Stock
     */
    @PutMapping("/api/bulk-stock")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<BulkUpdateRequest.BulkUpdateResponse>> bulkUpdateStock(
            @RequestBody BulkUpdateRequest.BulkStockUpdate request) {
        
        try {
            // Validate request
            if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseWrapper.error("Danh sách cập nhật không được để trống"));
            }
            
            // Validate each update
            for (BulkUpdateRequest.StockUpdateItem item : request.getUpdates()) {
                if (item.getId() == null) {
                    return ResponseEntity.badRequest()
                            .body(ResponseWrapper.error("ID sản phẩm không được để trống"));
                }
                if (item.getStock() == null || item.getStock() < 0) {
                    return ResponseEntity.badRequest()
                            .body(ResponseWrapper.error("Số lượng tồn kho phải >= 0"));
                }
            }
            
            BulkUpdateRequest.BulkUpdateResponse response = 
                productAnalyticsService.bulkUpdateStock(request);
            
            log.info("Bulk stock update completed: {}", response.getSummary());
            
            return ResponseEntity.ok(ResponseWrapper.success(response));
            
        } catch (Exception e) {
            log.error("Error in bulk stock update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Có lỗi xảy ra khi cập nhật tồn kho"));
        }
    }

    /**
     * Get Recent Activity
     */
    @GetMapping("/api/recent-activity")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<List<ProductAuditLogDTO>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<ProductAuditLogDTO> recentLogs = productAnalyticsService.getRecentAuditLogs(limit);
            
            return ResponseEntity.ok(ResponseWrapper.success(recentLogs));
            
        } catch (Exception e) {
            log.error("Error getting recent activity: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Không thể lấy hoạt động gần đây"));
        }
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
