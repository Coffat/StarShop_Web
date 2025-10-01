package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entity.*;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing orders
 * Handles business logic for order functionality
 */
@Service
@Transactional
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private DeliveryUnitRepository deliveryUnitRepository;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private CartService cartService;
    
    /**
     * Create order from user's cart
     */
    public OrderResponse createOrderFromCart(Long userId, OrderRequest request) {
        try {
            logger.info("Creating order from cart for user: {}", userId);
            
            // Validate user
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return OrderResponse.error("Người dùng không tồn tại");
            }
            
            User user = userOpt.get();
            
            // Get user's cart
            Cart cart = cartService.getOrCreateCart(userId);
            if (cart.getCartItems().isEmpty()) {
                return OrderResponse.error("Giỏ hàng trống");
            }
            
            // Validate address
            Optional<Address> addressOpt = addressRepository.findById(request.getAddressId());
            if (addressOpt.isEmpty()) {
                return OrderResponse.error("Địa chỉ không hợp lệ");
            }
            
            Address address = addressOpt.get();
            if (!address.getUser().getId().equals(userId)) {
                return OrderResponse.error("Địa chỉ không thuộc về người dùng này");
            }
            
            // Create order
            Order order = new Order(user, address, request.getPaymentMethod());
            order.setNotes(request.getNotes());
            
            // Set delivery unit if provided
            if (request.getDeliveryUnitId() != null) {
                Optional<DeliveryUnit> deliveryUnitOpt = deliveryUnitRepository.findById(request.getDeliveryUnitId());
                if (deliveryUnitOpt.isPresent()) {
                    order.setDeliveryUnit(deliveryUnitOpt.get());
                }
            }
            
            // Set voucher if provided
            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                Optional<Voucher> voucherOpt = voucherRepository.findByCode(request.getVoucherCode().trim());
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    if (voucher.isValid()) {
                        order.setVoucher(voucher);
                    } else {
                        return OrderResponse.error("Voucher không hợp lệ hoặc đã hết hạn");
                    }
                }
            }
            
            // Create order items from cart items
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = cartItem.getProduct();
                
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    return OrderResponse.error("Sản phẩm '" + product.getName() + "' không đủ hàng trong kho");
                }
                
                // Create order item with current product price
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(product.getPrice());
                order.addOrderItem(orderItem);
                
                // Update product stock
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }
            
            // Calculate total amount
            order.calculateTotalAmount();
            
            // Save order
            order = orderRepository.save(order);
            
            // Clear user's cart
            cartService.clearCart(userId);
            
            // Generate order number
            String orderNumber = generateOrderNumber(order.getId());
            
            logger.info("Successfully created order {} for user {}", order.getId(), userId);
            return OrderResponse.success("Đặt hàng thành công", OrderDTO.fromOrder(order), orderNumber);
            
        } catch (Exception e) {
            logger.error("Error creating order from cart for user {}: {}", userId, e.getMessage());
            return OrderResponse.error("Có lỗi xảy ra khi đặt hàng");
        }
    }
    
    /**
     * Create order directly (bypass cart)
     */
    public OrderResponse createOrderDirect(Long userId, OrderRequest request) {
        try {
            logger.info("Creating direct order for user: {}", userId);
            
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return OrderResponse.error("Danh sách sản phẩm không được để trống");
            }
            
            // Validate user
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return OrderResponse.error("Người dùng không tồn tại");
            }
            
            User user = userOpt.get();
            
            // Validate address
            Optional<Address> addressOpt = addressRepository.findById(request.getAddressId());
            if (addressOpt.isEmpty()) {
                return OrderResponse.error("Địa chỉ không hợp lệ");
            }
            
            Address address = addressOpt.get();
            if (!address.getUser().getId().equals(userId)) {
                return OrderResponse.error("Địa chỉ không thuộc về người dùng này");
            }
            
            // Create order
            Order order = new Order(user, address, request.getPaymentMethod());
            order.setNotes(request.getNotes());
            
            // Set delivery unit if provided
            if (request.getDeliveryUnitId() != null) {
                Optional<DeliveryUnit> deliveryUnitOpt = deliveryUnitRepository.findById(request.getDeliveryUnitId());
                if (deliveryUnitOpt.isPresent()) {
                    order.setDeliveryUnit(deliveryUnitOpt.get());
                }
            }
            
            // Set voucher if provided
            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                Optional<Voucher> voucherOpt = voucherRepository.findByCode(request.getVoucherCode().trim());
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    if (voucher.isValid()) {
                        order.setVoucher(voucher);
                    } else {
                        return OrderResponse.error("Voucher không hợp lệ hoặc đã hết hạn");
                    }
                }
            }
            
            // Create order items
            for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
                if (productOpt.isEmpty()) {
                    return OrderResponse.error("Sản phẩm không tồn tại");
                }
                
                Product product = productOpt.get();
                
                // Check stock availability
                if (product.getStockQuantity() < itemRequest.getQuantity()) {
                    return OrderResponse.error("Sản phẩm '" + product.getName() + "' không đủ hàng trong kho");
                }
                
                // Create order item with current product price
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());
                order.addOrderItem(orderItem);
                
                // Update product stock
                product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
                productRepository.save(product);
            }
            
            // Calculate total amount
            order.calculateTotalAmount();
            
            // Save order
            order = orderRepository.save(order);
            
            // Generate order number
            String orderNumber = generateOrderNumber(order.getId());
            
            logger.info("Successfully created direct order {} for user {}", order.getId(), userId);
            return OrderResponse.success("Đặt hàng thành công", OrderDTO.fromOrder(order), orderNumber);
            
        } catch (Exception e) {
            logger.error("Error creating direct order for user {}: {}", userId, e.getMessage());
            return OrderResponse.error("Có lỗi xảy ra khi đặt hàng");
        }
    }
    
    /**
     * Get user's orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        try {
            Page<Order> orders = orderRepository.findByUserId(userId, pageable);
            return orders.map(OrderDTO::fromOrder);
        } catch (Exception e) {
            logger.error("Error getting orders for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi lấy danh sách đơn hàng");
        }
    }
    
    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = Optional.ofNullable(orderRepository.findOrderWithItems(orderId));
            if (orderOpt.isEmpty()) {
                return OrderResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            
            // Check if order belongs to user
            if (!order.getUser().getId().equals(userId)) {
                return OrderResponse.error("Bạn không có quyền xem đơn hàng này");
            }
            
            return OrderResponse.success("Lấy thông tin đơn hàng thành công", OrderDTO.fromOrder(order));
            
        } catch (Exception e) {
            logger.error("Error getting order {} for user {}: {}", orderId, userId, e.getMessage());
            return OrderResponse.error("Có lỗi xảy ra khi lấy thông tin đơn hàng");
        }
    }
    
    /**
     * Create order from cart and return Order entity (for internal use)
     */
    public Order createOrderFromCartEntity(Long userId, OrderRequest request) {
        try {
            logger.info("Creating order from cart for user: {}", userId);
            
            // Validate user
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Người dùng không tồn tại");
            }
            
            User user = userOpt.get();
            
            // Get user's cart
            Cart cart = cartService.getOrCreateCart(userId);
            if (cart.getCartItems().isEmpty()) {
                throw new RuntimeException("Giỏ hàng trống");
            }
            
            // Create temporary address for shipping info
            Address tempAddress = new Address();
            tempAddress.setUser(user);
            tempAddress.setStreet(request.getShippingAddress());
            tempAddress.setCity("TP.HCM"); // Default city
            tempAddress.setProvince("TP.HCM"); // Default province
            tempAddress.setIsDefault(false);
            
            // Create order
            Order order = new Order(user, tempAddress, request.getPaymentMethod());
            order.setNotes(request.getNotes());
            
            // Create order items from cart items
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = cartItem.getProduct();
                
                // Check stock availability
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ hàng trong kho");
                }
                
                // Create order item with current product price
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(product.getPrice());
                order.addOrderItem(orderItem);
                
                // Update product stock
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }
            
            // Calculate total amount
            order.calculateTotalAmount();
            
            // Save order
            Order savedOrder = orderRepository.save(order);
            
            // Clear user's cart
            cartService.clearCart(userId);
            
            logger.info("Order created successfully with ID: {}", savedOrder.getId());
            return savedOrder;
            
        } catch (Exception e) {
            logger.error("Error creating order from cart for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi tạo đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Get order entity by ID and user ID (for internal use)
     */
    @Transactional(readOnly = true)
    public Order getOrderEntity(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = Optional.ofNullable(orderRepository.findOrderWithItems(orderId));
            if (orderOpt.isEmpty()) {
                return null;
            }
            
            Order order = orderOpt.get();
            
            // Check if order belongs to user
            if (!order.getUser().getId().equals(userId)) {
                return null;
            }
            
            return order;
            
        } catch (Exception e) {
            logger.error("Error getting order entity {} for user {}: {}", orderId, userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Cancel order
     */
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return OrderResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            
            // Check if order belongs to user
            if (!order.getUser().getId().equals(userId)) {
                return OrderResponse.error("Bạn không có quyền hủy đơn hàng này");
            }
            
            // Check if order can be cancelled
            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
                return OrderResponse.error("Không thể hủy đơn hàng ở trạng thái hiện tại");
            }
            
            // Update order status
            order.setStatus(OrderStatus.CANCELLED);
            
            // Restore product stock
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            }
            
            orderRepository.save(order);
            
            logger.info("Successfully cancelled order {} for user {}", orderId, userId);
            return OrderResponse.success("Hủy đơn hàng thành công", OrderDTO.fromOrder(order));
            
        } catch (Exception e) {
            logger.error("Error cancelling order {} for user {}: {}", orderId, userId, e.getMessage());
            return OrderResponse.error("Có lỗi xảy ra khi hủy đơn hàng");
        }
    }
    
    /**
     * Cancel order due to payment failure
     * This method is used by payment gateway callbacks to cancel orders and restore stock
     * when payment fails. It bypasses user ownership checks since it's triggered by payment system.
     */
    public void cancelOrderByPaymentFailure(Long orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                logger.error("Order {} not found for payment failure cancellation", orderId);
                return;
            }
            
            Order order = orderOpt.get();
            
            // Only cancel if order is in PENDING status (waiting for payment)
            if (order.getStatus() != OrderStatus.PENDING) {
                logger.warn("Order {} is not in PENDING status, current status: {}", orderId, order.getStatus());
                return;
            }
            
            // Update order status to CANCELLED
            order.setStatus(OrderStatus.CANCELLED);
            
            // Restore product stock
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productRepository.save(product);
                logger.info("Restored {} units of product {} (ID: {}) due to payment failure", 
                    orderItem.getQuantity(), product.getName(), product.getId());
            }
            
            orderRepository.save(order);
            logger.info("Successfully cancelled order {} due to payment failure and restored stock", orderId);
            
        } catch (Exception e) {
            logger.error("Error cancelling order {} due to payment failure: {}", orderId, e.getMessage(), e);
        }
    }
    
    /**
     * Update order status (for admin/staff)
     */
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return OrderResponse.error("Đơn hàng không tồn tại");
            }
            
            Order order = orderOpt.get();
            OrderStatus oldStatus = order.getStatus();
            
            // Validate status transition
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                return OrderResponse.error("Không thể chuyển từ trạng thái " + oldStatus + " sang " + newStatus);
            }
            
            order.setStatus(newStatus);
            orderRepository.save(order);
            
            logger.info("Updated order {} status from {} to {}", orderId, oldStatus, newStatus);
            return OrderResponse.success("Cập nhật trạng thái đơn hàng thành công", OrderDTO.fromOrder(order));
            
        } catch (Exception e) {
            logger.error("Error updating order {} status: {}", orderId, e.getMessage());
            return OrderResponse.error("Có lỗi xảy ra khi cập nhật trạng thái đơn hàng");
        }
    }
    
    /**
     * Get orders by status (for admin/staff)
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        try {
            List<Order> orders = orderRepository.findByStatus(status);
            return orders.stream()
                .map(OrderDTO::fromOrder)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting orders by status {}: {}", status, e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi lấy danh sách đơn hàng");
        }
    }
    
    /**
     * Generate order number
     */
    private String generateOrderNumber(Long orderId) {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD" + dateStr + String.format("%06d", orderId);
    }
    
    /**
     * Validate status transition
     */
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case PENDING:
                return to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.COMPLETED;
            case COMPLETED:
            case CANCELLED:
                return false; // Final states
            default:
                return false;
        }
    }
}
