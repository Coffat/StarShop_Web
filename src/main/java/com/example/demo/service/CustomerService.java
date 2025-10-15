package com.example.demo.service;

import com.example.demo.dto.CreateCustomerRequest;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.UpdateCustomerRequest;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Get all customers (CUSTOMER role only)
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        List<User> customers = userRepository.findByRole(UserRole.CUSTOMER);
        return customers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get customers with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getCustomers(Pageable pageable) {
        // Get all users and filter for customers only
        List<User> allUsers = userRepository.findAll();
        List<User> customers = allUsers.stream()
            .filter(user -> user.getRole() == UserRole.CUSTOMER)
            .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), customers.size());
        
        List<CustomerDTO> customerDTOs = customers.subList(start, end).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new PageImpl<>(customerDTOs, pageable, customers.size());
    }
    
    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Người dùng không phải là khách hàng");
        }
        
        return convertToDTO(user);
    }
    
    /**
     * Search customers by keyword
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<User> users = userRepository.searchUsers(keyword);
        return users.stream()
            .filter(user -> user.getRole() == UserRole.CUSTOMER)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Create new customer
     */
    public CustomerDTO createCustomer(CreateCustomerRequest request) {
        log.info("Creating new customer with email: {}", request.getEmail());
        
        // Validate email and phone uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
        }
        
        User customer = new User();
        customer.setFirstname(request.getFirstname());
        customer.setLastname(request.getLastname());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole(UserRole.CUSTOMER);
        customer.setAvatar(request.getAvatar());
        customer.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        User savedCustomer = userRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        
        return convertToDTO(savedCustomer);
    }
    
    /**
     * Update customer
     */
    public CustomerDTO updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        
        User customer = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Người dùng không phải là khách hàng");
        }
        
        // Update fields if provided
        if (request.getFirstname() != null) {
            customer.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            customer.setLastname(request.getLastname());
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
            }
            customer.setPhone(request.getPhone());
        }
        if (request.getPassword() != null) {
            customer.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getAvatar() != null) {
            customer.setAvatar(request.getAvatar());
        }
        if (request.getIsActive() != null) {
            customer.setIsActive(request.getIsActive());
        }
        
        User updatedCustomer = userRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", id);
        
        return convertToDTO(updatedCustomer);
    }
    
    /**
     * Delete customer
     */
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        
        User customer = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Người dùng không phải là khách hàng");
        }
        
        userRepository.delete(customer);
        log.info("Customer deleted successfully with ID: {}", id);
    }
    
    /**
     * Toggle customer active status
     */
    public CustomerDTO toggleCustomerStatus(Long id) {
        log.info("Toggling status for customer with ID: {}", id);
        
        User customer = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Người dùng không phải là khách hàng");
        }
        
        customer.setIsActive(!customer.getIsActive());
        User updatedCustomer = userRepository.save(customer);
        log.info("Customer status toggled successfully with ID: {}", id);
        
        return convertToDTO(updatedCustomer);
    }
    
    /**
     * Get customer statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerStats() {
        List<User> allCustomers = userRepository.findByRole(UserRole.CUSTOMER);
        
        long total = allCustomers.size();
        long loyal = 0;
        java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
        
        try {
            loyal = allCustomers.stream()
                .filter(c -> {
                    try {
                        return c.getOrders() != null && c.getOrders().size() > 10;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
            
            totalSpent = allCustomers.stream()
                .flatMap(c -> {
                    try {
                        return c.getOrders() != null ? c.getOrders().stream() : java.util.stream.Stream.empty();
                    } catch (Exception e) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .map(order -> order.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        } catch (Exception e) {
            log.warn("Could not calculate customer statistics: {}", e.getMessage());
        }
        
        // Count new customers this month
        java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        
        long newThisMonth = allCustomers.stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startOfMonth))
            .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("loyal", loyal);
        stats.put("totalSpent", totalSpent);
        stats.put("newThisMonth", newThisMonth);
        
        return stats;
    }
    
    /**
     * Convert User entity to CustomerDTO
     */
    private CustomerDTO convertToDTO(User user) {
        // Calculate total spent - safe null handling
        java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
        java.time.LocalDateTime lastOrderDate = null;
        long totalOrders = 0;
        long totalReviews = 0;
        
        try {
            if (user.getOrders() != null) {
                totalOrders = user.getOrders().size();
                totalSpent = user.getOrders().stream()
                    .map(order -> order.getTotalAmount())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                
                lastOrderDate = user.getOrders().stream()
                    .map(order -> order.getOrderDate())
                    .max(java.time.LocalDateTime::compareTo)
                    .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not load order data for customer {}: {}", user.getId(), e.getMessage());
        }
        
        try {
            if (user.getReviews() != null) {
                totalReviews = user.getReviews().size();
            }
        } catch (Exception e) {
            log.warn("Could not load review data for customer {}: {}", user.getId(), e.getMessage());
        }
        
        return CustomerDTO.builder()
            .id(user.getId())
            .firstname(user.getFirstname())
            .lastname(user.getLastname())
            .email(user.getEmail())
            .phone(user.getPhone())
            .avatar(user.getAvatar())
            .isActive(user.getIsActive())
            .lastLogin(user.getLastLogin())
            .createdAt(user.getCreatedAt())
            .totalOrders(totalOrders)
            .totalSpent(totalSpent)
            .lastOrderDate(lastOrderDate)
            .totalReviews(totalReviews)
            .build();
    }
}

