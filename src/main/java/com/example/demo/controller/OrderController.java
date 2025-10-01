package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.ResponseWrapper;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.entity.enums.PaymentMethod;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for managing user orders functionality
 * Provides both web pages and REST API endpoints
 */
@Controller
@Tag(name = "📦 Orders", description = "Order management APIs - Create, view, cancel orders")
public class OrderController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Display user's orders page
     */
    @GetMapping("/orders")
    public String ordersPage(Authentication authentication, Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login";
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // Get user's orders with pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
            Page<OrderDTO> orders = orderService.getUserOrders(user.getId(), pageable);
            
            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Đơn hàng của tôi", "/orders");
            
            // Add model attributes
            model.addAttribute("orders", orders);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orders.getTotalPages());
            model.addAttribute("totalElements", orders.getTotalElements());
            
            return "orders/index";
            
        } catch (Exception e) {
            logger.error("Error displaying orders page: {}", e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng");
            return "error/500";
        }
    }
    
    /**
     * Display order detail page
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetailPage(@PathVariable Long orderId, Authentication authentication, Model model) {
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login";
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // Get order details
            OrderResponse orderResponse = orderService.getOrder(orderId, user.getId());
            
            if (!orderResponse.isSuccess()) {
                model.addAttribute("error", orderResponse.getMessage());
                return "error/404";
            }
            
            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Đơn hàng của tôi", "/orders");
            addBreadcrumb(model, "Chi tiết đơn hàng #" + orderId, "/orders/" + orderId);
            
            // Add model attributes
            model.addAttribute("order", orderResponse.getOrder());
            
            return "orders/detail";
            
        } catch (Exception e) {
            logger.error("Error displaying order detail page: {}", e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải chi tiết đơn hàng");
            return "error/500";
        }
    }
    
    /**
     * Display checkout page
     */
    @GetMapping("/checkout")
    public String checkoutPage(Authentication authentication, Model model) {
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login";
            }
            
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return "redirect:/auth/login";
            }
            
            // Validate user profile before checkout
            String validationError = validateUserProfileForCheckout(user);
            if (validationError != null) {
                model.addAttribute("error", validationError);
                model.addAttribute("requireProfileUpdate", true);
                model.addAttribute("user", user);
                
                // Add breadcrumbs
                addBreadcrumb(model, "Trang chủ", "/");
                addBreadcrumb(model, "Giỏ hàng", "/cart");
                addBreadcrumb(model, "Thanh toán", "/checkout");
                
                // Add page metadata
                model.addAttribute("pageTitle", "Cập nhật thông tin - StarShop");
                model.addAttribute("pageDescription", "Vui lòng cập nhật thông tin tài khoản để tiếp tục");
                
                return "orders/checkout";
            }
            
            // Add breadcrumbs
            addBreadcrumb(model, "Trang chủ", "/");
            addBreadcrumb(model, "Giỏ hàng", "/cart");
            addBreadcrumb(model, "Thanh toán", "/checkout");
            
            // Add user info for checkout form
            model.addAttribute("user", user);
            
            // Add payment methods
            Map<String, Object> paymentMethods = paymentService.getAvailablePaymentMethods();
            model.addAttribute("paymentMethods", paymentMethods);
            
            // Add page metadata
            model.addAttribute("pageTitle", "Thanh toán - StarShop");
            model.addAttribute("pageDescription", "Hoàn tất đơn hàng của bạn tại StarShop");
            
            return "orders/checkout";
            
        } catch (Exception e) {
            logger.error("Error displaying checkout page: {}", e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải trang thanh toán");
            return "error/500";
        }
    }
    
    /**
     * REST API: Create order from cart
     */
    @Operation(
        summary = "Tạo đơn hàng từ giỏ hàng",
        description = "Tạo đơn hàng mới từ các sản phẩm trong giỏ hàng. Giỏ hàng sẽ được xóa sau khi tạo đơn thành công.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tạo đơn thành công"),
        @ApiResponse(responseCode = "400", description = "Thông tin không hợp lệ hoặc giỏ hàng trống"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PostMapping("/api/orders/create-from-cart")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderResponse>> createOrderFromCart(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin đơn hàng (địa chỉ, payment method, voucher, notes)",
                required = true
            )
            @Valid @RequestBody OrderRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Create order from cart
            OrderResponse orderResponse = orderService.createOrderFromCart(user.getId(), request);
            
            if (orderResponse.isSuccess()) {
                return ResponseEntity.ok(ResponseWrapper.success(orderResponse));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(orderResponse.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error creating order from cart: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tạo đơn hàng"));
        }
    }
    
    /**
     * REST API: Create order directly
     */
    @Operation(
        summary = "Tạo đơn hàng trực tiếp",
        description = "Tạo đơn hàng mới không qua giỏ hàng. Phải cung cấp danh sách sản phẩm (items) trong request.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tạo đơn thành công"),
        @ApiResponse(responseCode = "400", description = "Thông tin không hợp lệ hoặc sản phẩm hết hàng"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PostMapping("/api/orders/create-direct")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderResponse>> createOrderDirect(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Thông tin đơn hàng với danh sách items, address, payment method",
                required = true
            )
            @Valid @RequestBody OrderRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Create order directly
            OrderResponse orderResponse = orderService.createOrderDirect(user.getId(), request);
            
            if (orderResponse.isSuccess()) {
                return ResponseEntity.ok(ResponseWrapper.success(orderResponse));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(orderResponse.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error creating direct order: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tạo đơn hàng"));
        }
    }
    
    /**
     * REST API: Get user's orders
     */
    @Operation(
        summary = "Lấy danh sách đơn hàng",
        description = "Lấy danh sách tất cả đơn hàng của người dùng hiện tại với phân trang. Sắp xếp theo ngày tạo mới nhất.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @GetMapping("/api/orders/list")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Page<OrderDTO>>> getUserOrders(
            @Parameter(description = "Trang số (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Get user's orders
            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
            Page<OrderDTO> orders = orderService.getUserOrders(user.getId(), pageable);
            
            return ResponseEntity.ok(ResponseWrapper.success(orders));
            
        } catch (Exception e) {
            logger.error("Error getting user orders: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy danh sách đơn hàng"));
        }
    }
    
    /**
     * REST API: Get order details
     */
    @Operation(
        summary = "Lấy chi tiết đơn hàng",
        description = "Lấy thông tin chi tiết của một đơn hàng theo ID. Chỉ lấy được đơn hàng của chính mình.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "400", description = "Không tìm thấy đơn hàng hoặc không có quyền truy cập"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderResponse>> getOrder(
            @Parameter(description = "ID đơn hàng", required = true)
            @PathVariable Long orderId,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Get order details
            OrderResponse orderResponse = orderService.getOrder(orderId, user.getId());
            
            if (orderResponse.isSuccess()) {
                return ResponseEntity.ok(ResponseWrapper.success(orderResponse));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(orderResponse.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error getting order {}: {}", orderId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy thông tin đơn hàng"));
        }
    }
    
    /**
     * REST API: Cancel order
     */
    @Operation(
        summary = "Hủy đơn hàng",
        description = "Hủy đơn hàng theo ID. Chỉ được hủy đơn ở trạng thái PENDING hoặc PROCESSING.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hủy thành công"),
        @ApiResponse(responseCode = "400", description = "Không thể hủy đơn hàng (sai trạng thái hoặc không có quyền)"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PutMapping("/api/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderResponse>> cancelOrder(
            @Parameter(description = "ID đơn hàng cần hủy", required = true)
            @PathVariable Long orderId,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Cancel order
            OrderResponse orderResponse = orderService.cancelOrder(orderId, user.getId());
            
            if (orderResponse.isSuccess()) {
                return ResponseEntity.ok(ResponseWrapper.success(orderResponse));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(orderResponse.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi hủy đơn hàng"));
        }
    }
    
    /**
     * REST API: Update order status (Admin/Staff only)
     */
    @Operation(
        summary = "Cập nhật trạng thái đơn hàng (Admin/Staff)",
        description = "Cập nhật trạng thái đơn hàng. Chỉ Admin và Staff có quyền sử dụng. Trạng thái: PENDING, PROCESSING, SHIPPING, DELIVERED, CANCELLED.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền (yêu cầu Admin/Staff)")
    })
    @PutMapping("/api/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<OrderResponse>> updateOrderStatus(
            @Parameter(description = "ID đơn hàng", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "Trạng thái mới", required = true, example = "PROCESSING")
            @RequestParam OrderStatus status,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Check if user has admin/staff role
            if (!user.getRole().name().equals("ADMIN") && !user.getRole().name().equals("STAFF")) {
                return ResponseEntity.status(403)
                    .body(ResponseWrapper.error("Bạn không có quyền thực hiện thao tác này"));
            }
            
            // Update order status
            OrderResponse orderResponse = orderService.updateOrderStatus(orderId, status);
            
            if (orderResponse.isSuccess()) {
                return ResponseEntity.ok(ResponseWrapper.success(orderResponse));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(orderResponse.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error updating order {} status: {}", orderId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi cập nhật trạng thái đơn hàng"));
        }
    }
    
    /**
     * REST API: Get orders by status (Admin/Staff only)
     */
    @Operation(
        summary = "Lấy đơn hàng theo trạng thái (Admin/Staff)",
        description = "Lấy tất cả đơn hàng theo trạng thái cụ thể. Chỉ Admin và Staff có quyền. Dùng để quản lý và theo dõi đơn hàng.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "403", description = "Không có quyền (yêu cầu Admin/Staff)")
    })
    @GetMapping("/api/orders/status/{status}")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<List<OrderDTO>>> getOrdersByStatus(
            @Parameter(description = "Trạng thái đơn hàng", required = true, example = "PENDING")
            @PathVariable OrderStatus status,
            @Parameter(hidden = true) Authentication authentication) {
        
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
            
            // Check if user has admin/staff role
            if (!user.getRole().name().equals("ADMIN") && !user.getRole().name().equals("STAFF")) {
                return ResponseEntity.status(403)
                    .body(ResponseWrapper.error("Bạn không có quyền thực hiện thao tác này"));
            }
            
            // Get orders by status
            List<OrderDTO> orders = orderService.getOrdersByStatus(status);
            
            return ResponseEntity.ok(ResponseWrapper.success(orders));
            
        } catch (Exception e) {
            logger.error("Error getting orders by status {}: {}", status, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy danh sách đơn hàng"));
        }
    }
    
    /**
     * REST API: Process payment for an order
     */
    @Operation(
        summary = "Xử lý thanh toán cho đơn hàng",
        description = "Khởi tạo quá trình thanh toán cho đơn hàng. Hỗ trợ COD và MoMo. Nếu là MoMo sẽ trả về payment URL.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thanh toán thành công hoặc lấy payment URL"),
        @ApiResponse(responseCode = "400", description = "Đơn hàng không hợp lệ hoặc lỗi thanh toán"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PostMapping("/api/orders/{orderId}/payment")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<PaymentService.PaymentResult>> processPayment(
            @PathVariable Long orderId,
            @RequestParam PaymentMethod paymentMethod,
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
            
            // Get order entity
            Order order = orderService.getOrderEntity(orderId, user.getId());
            if (order == null) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Đơn hàng không tồn tại hoặc bạn không có quyền truy cập"));
            }
            
            // Process payment
            PaymentService.PaymentResult paymentResult = paymentService.processPayment(order, paymentMethod);
            
            if (paymentResult.isSuccess()) {
                logger.info("Payment processed successfully for order {} by user {}", orderId, user.getEmail());
                return ResponseEntity.ok(ResponseWrapper.success(paymentResult));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(paymentResult.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi xử lý thanh toán"));
        }
    }
    
    /**
     * REST API: Get available payment methods
     */
    @GetMapping("/api/payment/methods")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getPaymentMethods() {
        
        try {
            Map<String, Object> paymentMethods = paymentService.getAvailablePaymentMethods();
            return ResponseEntity.ok(ResponseWrapper.success(paymentMethods));
            
        } catch (Exception e) {
            logger.error("Error getting payment methods: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi lấy danh sách phương thức thanh toán"));
        }
    }
    
    /**
     * REST API: Create order with payment processing
     */
    @PostMapping("/api/orders/create-with-payment")
    @ResponseBody
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> createOrderWithPayment(
            @Valid @RequestBody OrderWithPaymentRequest request,
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
            
            // Validate user profile before creating order
            String validationError = validateUserProfileForCheckout(user);
            if (validationError != null) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(validationError));
            }
            
            // Create order first
            Order order;
            try {
                order = orderService.createOrderFromCartEntity(user.getId(), request.getOrderRequest());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(e.getMessage()));
            }
            
            // Process payment
            PaymentService.PaymentResult paymentResult = paymentService.processPayment(order, request.getPaymentMethod());
            
            Map<String, Object> result = new HashMap<>();
            result.put("order", OrderDTO.fromOrder(order));
            result.put("payment", paymentResult);
            
            if (paymentResult.isSuccess()) {
                logger.info("Order created and payment processed successfully for user {}", user.getEmail());
                return ResponseEntity.ok(ResponseWrapper.success(result));
            } else {
                // Order created but payment failed
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error(paymentResult.getMessage()));
            }
            
        } catch (Exception e) {
            logger.error("Error creating order with payment: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ResponseWrapper.error("Có lỗi xảy ra khi tạo đơn hàng và xử lý thanh toán"));
        }
    }
    
    // Request DTO for order with payment
    public static class OrderWithPaymentRequest {
        private OrderRequest orderRequest;
        private PaymentMethod paymentMethod;
        
        public OrderRequest getOrderRequest() {
            return orderRequest;
        }
        
        public void setOrderRequest(OrderRequest orderRequest) {
            this.orderRequest = orderRequest;
        }
        
        public PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }
    
    /**
     * Validate user profile for checkout
     * @param user User to validate
     * @return Error message if validation fails, null if valid
     */
    private String validateUserProfileForCheckout(User user) {
        // Check phone number
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return "Vui lòng cập nhật số điện thoại trong tài khoản của bạn để tiếp tục thanh toán.";
        }
        
        // Validate Vietnamese phone number (10 digits, starts with 0)
        String phone = user.getPhone().trim().replaceAll("\\s+", "");
        if (!phone.matches("^0[0-9]{9}$")) {
            return "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam gồm 10 chữ số (bắt đầu bằng số 0).";
        }
        
        // Check if user has at least one address
        if (user.getAddresses() == null || user.getAddresses().isEmpty()) {
            return "Vui lòng thêm địa chỉ giao hàng trong tài khoản của bạn để tiếp tục thanh toán.";
        }
        
        // Check if user has a default address or at least one valid address
        boolean hasValidAddress = user.getAddresses().stream()
            .anyMatch(addr -> addr.getStreet() != null && !addr.getStreet().trim().isEmpty());
        
        if (!hasValidAddress) {
            return "Vui lòng cập nhật địa chỉ giao hàng trong tài khoản của bạn.";
        }
        
        return null; // All validations passed
    }
}
