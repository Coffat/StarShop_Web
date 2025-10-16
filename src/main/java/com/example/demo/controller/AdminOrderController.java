package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import com.example.demo.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Admin Order Management Controller
 */
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController extends BaseController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;

    /**
     * Admin Orders Management Page
     */
    @GetMapping({"", "/"})
    public String ordersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {
        

        try {
            // Create pageable with sorting
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Get orders based on filters
            Page<OrderDTO> orders;
            if (status != null && !status.isEmpty()) {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getOrdersByStatus(orderStatus, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                orders = orderService.searchOrders(search.trim(), pageable);
            } else {
                orders = orderService.getAllOrders(pageable);
            }

            // Add model attributes
            model.addAttribute("pageTitle", "Quản lý Đơn hàng");
            model.addAttribute("contentTemplate", "admin/orders/index");
            model.addAttribute("orders", orders);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orders.getTotalPages());
            model.addAttribute("totalElements", orders.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSearch", search);
            
            // Order status options for filter
            model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values()));
            
            // Statistics for dashboard cards
            Map<String, Object> orderStats = orderService.getOrderStatistics();
            model.addAttribute("orderStats", orderStats);

            // Breadcrumbs
            List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
            breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
            breadcrumbs.add(new BreadcrumbItem("Quản lý Đơn hàng", "/admin/orders"));
            model.addAttribute("breadcrumbs", breadcrumbs);

            return "layouts/admin";

        } catch (Exception e) {
            log.error("Error loading admin orders page: {}", e.getMessage(), e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng");
            return "layouts/admin";
        }
    }

    /**
     * Order Detail Page
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable String orderId, Model model) {
        // Dialog UI is used; keep direct link as redirect to orders list
        return "redirect:/admin/orders";
    }

    // ==================== REST API ENDPOINTS ====================

    /**
     * API: Update order status
     */
    @PutMapping("/api/{orderId}/status")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderDTO>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        

        try {
            String statusStr = request.get("status");
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Trạng thái đơn hàng không được để trống"));
            }

            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus);

            return ResponseEntity.ok(ResponseWrapper.success(updatedOrder, "Cập nhật trạng thái đơn hàng thành công"));

        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}", request.get("status"));
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Trạng thái đơn hàng không hợp lệ"));
        } catch (Exception e) {
            log.error("Error updating order status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi cập nhật trạng thái đơn hàng"));
        }
    }

    /**
     * API: Get order detail (for dialog)
     */
    @GetMapping("/api/{orderId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderDTO>> getOrderDetailApi(@PathVariable String orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseWrapper.error("Không tìm thấy đơn hàng"));
            }
            return ResponseEntity.ok(ResponseWrapper.success(order, "Lấy chi tiết đơn hàng thành công"));
        } catch (Exception e) {
            log.error("Error getting order detail: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tải chi tiết đơn hàng"));
        }
    }

    /**
     * API: Get orders with filters (AJAX)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Page<OrderDTO>>> getOrdersApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<OrderDTO> orders;
            if (status != null && !status.isEmpty()) {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getOrdersByStatus(orderStatus, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                orders = orderService.searchOrders(search.trim(), pageable);
            } else {
                orders = orderService.getAllOrders(pageable);
            }

            return ResponseEntity.ok(ResponseWrapper.success(orders, "Lấy danh sách đơn hàng thành công"));

        } catch (Exception e) {
            log.error("Error getting orders API: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tải danh sách đơn hàng"));
        }
    }

    /**
     * API: Get order statistics
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getOrderStatistics() {
        try {
            Map<String, Object> stats = orderService.getOrderStatistics();
            return ResponseEntity.ok(ResponseWrapper.success(stats, "Lấy thống kê đơn hàng thành công"));
        } catch (Exception e) {
            log.error("Error getting order statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tải thống kê đơn hàng"));
        }
    }

    /**
     * Admin cancel order with reason
     */
    @PutMapping("/api/{orderId}/cancel")
    public ResponseEntity<ResponseWrapper<OrderDTO>> cancelOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        
        
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Lý do hủy không được để trống"));
            }
            
            OrderDTO cancelledOrder = orderService.adminCancelOrder(orderId, reason);
            return ResponseEntity.ok(ResponseWrapper.success(cancelledOrder, "Hủy đơn hàng thành công"));
            
        } catch (Exception e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Có lỗi xảy ra khi hủy đơn hàng"));
        }
    }

    /**
     * Create new order (Admin function)
     */
    @PostMapping("/api/create")
    public ResponseEntity<ResponseWrapper<OrderDTO>> createOrder(@RequestBody CreateOrderRequest request) {
        
        try {
            // Validate request
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("ID khách hàng không được để trống"));
            }
            
            if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Đơn hàng phải có ít nhất một sản phẩm"));
            }
            
            // Create order using OrderService
            OrderDTO createdOrder = orderService.createOrderDirect(request);
            
            return ResponseEntity.ok(ResponseWrapper.success(createdOrder, "Tạo đơn hàng thành công"));
            
        } catch (Exception e) {
            log.error("Error creating order for user {}: {}", request.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tạo đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * DTO for create order request
     */
    public static class CreateOrderRequest {
        private Long userId;
        private List<OrderItemRequest> orderItems;
        private String paymentMethod;
        private String notes;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public List<OrderItemRequest> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    /**
     * DTO for order item request
     */
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private java.math.BigDecimal price;
        
        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
    }

    /**
     * Search customers for order creation
     */
    @GetMapping("/api/search-customers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ResponseWrapper<java.util.List<CustomerSearchResult>>> searchCustomers(
            @RequestParam("q") String query) {
        
        
        try {
            if (query == null || query.trim().length() < 1) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Query phải có ít nhất 1 ký tự"));
            }
            
            // Search customers using UserRepository or UserService
            java.util.List<CustomerSearchResult> customers = searchCustomersFromDatabase(query.trim());
            
            return ResponseEntity.ok(ResponseWrapper.success(customers, "Tìm kiếm thành công"));
            
        } catch (Exception e) {
            log.error("Error searching customers with query {}: {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tìm kiếm khách hàng"));
        }
    }

    /**
     * Search customers from database
     */
    private java.util.List<CustomerSearchResult> searchCustomersFromDatabase(String query) {
        try {
            // Search users by name or email with addresses fetched
            java.util.List<User> users = userRepository.searchUsersWithAddresses(query);
            
            // Limit results to prevent too many results
            if (users.size() > 10) {
                users = users.subList(0, 10);
            }
            
            java.util.List<CustomerSearchResult> results = new java.util.ArrayList<>();
            
            for (User user : users) {
                // Check if user has default address
                boolean hasDefaultAddress = user.getAddresses() != null && 
                    user.getAddresses().stream().anyMatch(addr -> addr.getIsDefault() != null && addr.getIsDefault());
                
                String fullName = user.getFullName();
                if (fullName == null || fullName.trim().isEmpty()) {
                    fullName = user.getFirstname() + " " + user.getLastname();
                }
                
                results.add(new CustomerSearchResult(
                    user.getId(),
                    fullName.trim(),
                    user.getEmail(),
                    hasDefaultAddress
                ));
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("Error searching customers from database: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * DTO for customer search results
     */
    public static class CustomerSearchResult {
        private Long id;
        private String fullName;
        private String email;
        private boolean hasDefaultAddress;
        
        public CustomerSearchResult(Long id, String fullName, String email, boolean hasDefaultAddress) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.hasDefaultAddress = hasDefaultAddress;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public boolean isHasDefaultAddress() { return hasDefaultAddress; }
        public void setHasDefaultAddress(boolean hasDefaultAddress) { this.hasDefaultAddress = hasDefaultAddress; }
    }
}
