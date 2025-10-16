/**
 * ACCOUNT ORDERS PAGE - JAVASCRIPT
 * Load và hiển thị orders động từ backend API
 */

// Global variables
let currentPage = 0;
let currentStatus = 'all';
const pageSize = 10;
let ordersData = []; // Store orders data globally

// Format currency VND
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

// Get status badge HTML with Tailwind styling
function getStatusBadge(status) {
    const statusMap = {
        'PENDING': { 
            class: 'bg-yellow-100 text-yellow-700 border-yellow-200', 
            icon: 'clock-history', 
            text: 'Chờ xử lý' 
        },
        'PROCESSING': { 
            class: 'bg-blue-100 text-blue-700 border-blue-200', 
            icon: 'arrow-repeat', 
            text: 'Đang xử lý' 
        },
        'SHIPPED': { 
            class: 'bg-purple-100 text-purple-700 border-purple-200', 
            icon: 'truck', 
            text: 'Đang giao' 
        },
        'COMPLETED': { 
            class: 'bg-green-100 text-green-700 border-green-200', 
            icon: 'check-circle-fill', 
            text: 'Hoàn thành' 
        },
        'CANCELLED': { 
            class: 'bg-red-100 text-red-700 border-red-200', 
            icon: 'x-circle-fill', 
            text: 'Đã hủy' 
        }
    };
    
    const statusInfo = statusMap[status] || statusMap['PENDING'];
    return `
        <span class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-sm font-semibold border ${statusInfo.class}">
            <i class="bi bi-${statusInfo.icon}"></i>
            ${statusInfo.text}
        </span>
    `;
}

// Get action buttons based on order status with Tailwind styling
function getActionButtons(order) {
    const buttons = [];
    
    // Cancel button - only for PENDING status
    if (order.status === 'PENDING') {
        buttons.push(`
            <button onclick="cancelOrder(${order.id})" 
                    class="px-4 py-2 bg-red-50 text-red-600 rounded-lg font-medium hover:bg-red-100 transition-colors border border-red-200">
                <i class="bi bi-x-circle mr-1"></i> Hủy đơn
            </button>
        `);
    }
    
    // Track button - for SHIPPED status
    if (order.status === 'SHIPPED') {
        buttons.push(`
            <button onclick="trackOrder(${order.id})" 
                    class="px-4 py-2 bg-purple-50 text-purple-600 rounded-lg font-medium hover:bg-purple-100 transition-colors border border-purple-200">
                <i class="bi bi-geo-alt mr-1"></i> Theo dõi
            </button>
        `);
    }
    
    // Review button - for COMPLETED status
    if (order.status === 'COMPLETED') {
        console.log('Adding review button for order:', order.id);
        buttons.push(`
            <button onclick="reviewOrder('${order.id}')" 
                    class="px-4 py-2 bg-gradient-to-r from-pink-500 to-purple-600 text-white rounded-lg font-medium hover:shadow-lg transition-all"
                    data-order-id="${order.id}">
                <i class="bi bi-star mr-1"></i> Đánh giá
            </button>
        `);
    }
    
    // Reorder button - for COMPLETED or CANCELLED
    if (order.status === 'COMPLETED' || order.status === 'CANCELLED') {
        buttons.push(`
            <button onclick="reorder(${order.id})" 
                    class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors border border-gray-300">
                <i class="bi bi-arrow-clockwise mr-1"></i> Mua lại
            </button>
        `);
    }
    
    return buttons.join('');
}

// Render single order card with Tailwind styling
function renderOrderCard(order) {
    const statusBadge = getStatusBadge(order.status);
    const actionButtons = getActionButtons(order);
    
    let itemsHTML = '';
    if (order.items && order.items.length > 0) {
        // Show max 3 items, then "+ X more"
        const displayItems = order.items.slice(0, 3);
        const remainingCount = order.items.length - 3;
        
        itemsHTML = displayItems.map(item => `
            <div class="flex items-center gap-4 p-4 bg-gray-50 rounded-xl">
                <div class="w-20 h-20 flex-shrink-0">
                    <img src="${item.productImage || '/images/placeholder.jpg'}" 
                         alt="${item.productName}"
                         class="w-full h-full object-cover rounded-lg"
                         onerror="this.src='/images/placeholder.jpg'">
                </div>
                <div class="flex-1 min-w-0">
                    <h4 class="font-semibold text-gray-900 truncate">${item.productName}</h4>
                    <p class="text-sm text-gray-600 mt-1">Số lượng: ${item.quantity} × ${formatCurrency(item.price)}</p>
                </div>
                <div class="text-right">
                    <p class="font-bold text-gray-900">${formatCurrency(item.price * item.quantity)}</p>
                </div>
            </div>
        `).join('');
        
        if (remainingCount > 0) {
            itemsHTML += `
                <div class="text-center py-2 text-sm text-gray-600">
                    <i class="bi bi-three-dots"></i> Và ${remainingCount} sản phẩm khác
                </div>
            `;
        }
    }
    
    return `
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
            <!-- Header -->
            <div class="flex items-center justify-between p-6 border-b border-gray-100">
                <div>
                    <div class="flex items-center gap-3 mb-2">
                        <span class="text-lg font-bold text-gray-900">#${order.id}</span>
                        ${statusBadge}
                    </div>
                    <p class="text-sm text-gray-600">
                        <i class="bi bi-calendar3 mr-1"></i>
                        ${formatDate(order.orderDate)}
                    </p>
                </div>
                <button onclick="viewOrderDetail(${order.id})" class="text-pink-600 hover:text-pink-700 font-medium text-sm">
                    Chi tiết <i class="bi bi-arrow-right ml-1"></i>
                </button>
            </div>
            
            <!-- Items -->
            <div class="p-6 space-y-3">
                ${itemsHTML}
            </div>
            
            <!-- Footer -->
            <div class="flex items-center justify-between p-6 bg-gray-50 border-t border-gray-100">
                <div>
                    <p class="text-sm text-gray-600 mb-1">Tổng thanh toán</p>
                    <p class="text-2xl font-bold bg-gradient-to-r from-pink-500 to-purple-600 bg-clip-text text-transparent">
                        ${formatCurrency(order.totalAmount)}
                    </p>
                </div>
                <div class="flex items-center gap-2">
                    ${actionButtons}
                </div>
            </div>
        </div>
    `;
}

// Load orders from API
async function loadOrders(status = 'all', page = 0) {
    const ordersList = document.getElementById('ordersList');
    
    // Show loading state
    ordersList.innerHTML = `
        <div class="flex items-center justify-center py-20">
            <div class="text-center">
                <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-pink-200 border-t-pink-600 mb-4"></div>
                <p class="text-gray-600">Đang tải đơn hàng...</p>
            </div>
        </div>
    `;
    
    try {
        // Build API URL
        let apiUrl = `/api/orders/list?page=${page}&size=${pageSize}`;
        if (status !== 'all') {
            apiUrl += `&status=${status}`;
        }
        
        const response = await fetch(apiUrl, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load orders');
        }
        
        const data = await response.json();
        
        // Check if we have orders
        if (!data.data || !data.data.content || data.data.content.length === 0) {
            ordersList.innerHTML = `
                <div class="text-center py-20">
                    <div class="inline-flex items-center justify-center w-24 h-24 bg-gradient-to-br from-pink-100 to-purple-100 rounded-full mb-6">
                        <i class="bi bi-inbox text-5xl bg-gradient-to-r from-pink-500 to-purple-600 bg-clip-text text-transparent"></i>
                    </div>
                    <h3 class="text-2xl font-bold text-gray-900 mb-3">Không có đơn hàng nào</h3>
                    <p class="text-gray-600">
                        ${status === 'all' 
                            ? 'Bạn chưa có đơn hàng nào trong hệ thống.' 
                            : 'Không tìm thấy đơn hàng với trạng thái này.'}
                    </p>
                </div>
            `;
            return;
        }
        
        // Store orders data globally for review functionality
        ordersData = data.data.content;
        console.log('Stored ordersData:', ordersData);
        console.log('Full API response:', data);
        console.log('Orders array:', data.data.content);
        
        // Render orders
        const ordersHtml = data.data.content.map(order => renderOrderCard(order)).join('');
        ordersList.innerHTML = ordersHtml;
        
        // Bind review button events manually (fallback for onclick)
        // bindReviewButtonEvents(); // Tạm comment để test
        
        // Update status counts in filter tabs
        updateStatusCounts();
        
    } catch (error) {
        console.error('Error loading orders:', error);
        ordersList.innerHTML = `
            <div class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
                <i class="bi bi-exclamation-triangle text-3xl text-red-600 mb-3"></i>
                <p class="text-red-700 font-medium">Có lỗi xảy ra khi tải đơn hàng.</p>
                <p class="text-red-600 text-sm mt-1">Vui lòng thử lại sau.</p>
            </div>
        `;
    }
}

// Update status counts in filter tabs and quick stats
async function updateStatusCounts() {
    try {
        const response = await fetch('/api/orders/list?page=0&size=1000');
        if (!response.ok) return;
        
        const data = await response.json();
        const orders = data.data.content || [];
        
        // Count by status
        const counts = {
            'all': orders.length,
            'PENDING': 0,
            'PROCESSING': 0,
            'SHIPPED': 0,
            'COMPLETED': 0,
            'CANCELLED': 0
        };
        
        orders.forEach(order => {
            if (counts[order.status] !== undefined) {
                counts[order.status]++;
            }
        });
        
        // Update quick stats cards
        const pendingCount = document.getElementById('pendingCount');
        const shippedCount = document.getElementById('shippedCount');
        const completedCount = document.getElementById('completedCount');
        
        if (pendingCount) pendingCount.textContent = counts['PENDING'];
        if (shippedCount) shippedCount.textContent = counts['SHIPPED'];
        if (completedCount) completedCount.textContent = counts['COMPLETED'];
        
    } catch (error) {
        console.error('Error updating status counts:', error);
    }
}

// Filter tabs click handler with Tailwind styling
function setupFilterTabs() {
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            // Remove active styles from all tabs
            document.querySelectorAll('.filter-tab').forEach(t => {
                t.classList.remove('bg-gradient-to-r', 'from-pink-500', 'to-purple-600', 'text-white', 'shadow-md');
                t.classList.add('text-gray-700', 'hover:bg-gray-100');
            });
            
            // Add active styles to clicked tab
            this.classList.remove('text-gray-700', 'hover:bg-gray-100');
            this.classList.add('bg-gradient-to-r', 'from-pink-500', 'to-purple-600', 'text-white', 'shadow-md');
            
            // Get status and load orders
            const status = this.getAttribute('data-status');
            currentStatus = status;
            currentPage = 0;
            loadOrders(status, currentPage);
        });
    });
}

// View order detail
function viewOrderDetail(orderId) {
    // Support both button click and direct ID
    const id = typeof orderId === 'number' ? orderId : orderId;
    window.location.href = `/orders/${id}`;
}

// Cancel order
async function cancelOrder(orderId) {
    if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/orders/${orderId}/cancel`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to cancel order');
        }
        
        // Show success message
        showToast('Đơn hàng đã được hủy thành công', 'success');
        
        // Reload orders
        loadOrders(currentStatus, currentPage);
        
    } catch (error) {
        console.error('Error canceling order:', error);
        showToast('Có lỗi xảy ra khi hủy đơn hàng', 'error');
    }
}

// Bind review button events manually
function bindReviewButtonEvents() {
    console.log('Binding review button events...');
    const reviewButtons = document.querySelectorAll('button[data-order-id]');
    console.log('Found review buttons:', reviewButtons.length);
    
    reviewButtons.forEach(button => {
        const orderId = button.getAttribute('data-order-id');
        console.log('Binding event for order:', orderId);
        
        // Add new listener
        button.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Review button clicked for order:', orderId);
            reviewOrder(orderId);
        });
    });
}

// Track order
function trackOrder(orderId) {
    // TODO: Implement order tracking
    showToast('Tính năng theo dõi đơn hàng đang được phát triển', 'info');
}

// Review order
function reviewOrder(orderId) {
    try {
        console.log('Review order called with ID:', orderId);
        console.log('Available orders data:', ordersData);
        
        // Check if openReviewModal function exists
        if (typeof openReviewModal === 'undefined') {
            alert('openReviewModal function not found. Make sure reviews.js is loaded.');
            return;
        }
        
        // Find the order data
        const orderData = ordersData.find(order => order.id === orderId || order.id == orderId);
        console.log('Found order data:', orderData);
        
        if (!orderData) {
            alert('Order not found with ID: ' + orderId);
            return;
        }
        
        // Check if order has items
        console.log('Order items:', orderData.items);
        console.log('Order orderItems:', orderData.orderItems);
        console.log('Order items type:', typeof orderData.items);
        console.log('Order orderItems type:', typeof orderData.orderItems);
        console.log('Order items length:', orderData.items ? orderData.items.length : 'undefined');
        console.log('Order orderItems length:', orderData.orderItems ? orderData.orderItems.length : 'undefined');
        
        if (orderData.items) {
            orderData.items.forEach((item, index) => {
                console.log(`Item ${index}:`, item);
                console.log(`Item ${index} productId:`, item.productId);
                console.log(`Item ${index} productName:`, item.productName);
            });
        }
        
        if (orderData.orderItems) {
            orderData.orderItems.forEach((item, index) => {
                console.log(`OrderItem ${index}:`, item);
                console.log(`OrderItem ${index} productId:`, item.productId);
                console.log(`OrderItem ${index} productName:`, item.productName);
            });
        }
        
        if (!orderData.orderItems || orderData.orderItems.length === 0) {
            alert('Order has no orderItems. Order data: ' + JSON.stringify(orderData));
            return;
        }
        
        // If order has multiple items, show selection modal
        if (orderData.orderItems.length > 1) {
            showProductSelectionModal(orderData);
        } else {
            // Single product, open review modal directly
            const item = orderData.orderItems[0];
            console.log('Opening review modal for item:', item);
            openReviewModal(item.productId, orderId, item.productName);
        }
        
    } catch (error) {
        alert('ERROR in reviewOrder: ' + error.message);
        console.error('Error in reviewOrder:', error);
    }
}

// Show product selection modal for multi-item orders
function showProductSelectionModal(orderData) {
    const modalHTML = `
        <div id="productSelectionModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
            <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full max-h-[80vh] overflow-hidden">
                <div class="bg-gradient-to-r from-pink-500 to-purple-600 px-6 py-4 text-white">
                    <div class="flex items-center justify-between">
                        <h3 class="text-lg font-bold">Chọn sản phẩm để đánh giá</h3>
                        <button onclick="closeProductSelectionModal()" class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center hover:bg-white/30 transition-colors">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                            </svg>
                        </button>
                    </div>
                </div>
                <div class="p-6 max-h-96 overflow-y-auto">
                    <div class="space-y-3">
                        ${orderData.items.map(item => `
                            <div class="flex items-center gap-4 p-4 border border-gray-200 rounded-xl hover:border-pink-300 cursor-pointer transition-colors"
                                 onclick="selectProductForReview(${item.productId}, '${orderData.id}', '${item.productName.replace(/'/g, "\\'")}')">
                                <img src="${item.productImage || '/images/default-product.jpg'}" 
                                     alt="${item.productName}" 
                                     class="w-16 h-16 object-cover rounded-lg">
                                <div class="flex-1">
                                    <h4 class="font-medium text-gray-900">${item.productName}</h4>
                                    <p class="text-sm text-gray-500">Số lượng: ${item.quantity}</p>
                                    <p class="text-sm font-medium text-pink-600">${formatCurrency(item.price)}</p>
                                </div>
                                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                                </svg>
                            </div>
                        `).join('')}
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// Close product selection modal
function closeProductSelectionModal() {
    const modal = document.getElementById('productSelectionModal');
    if (modal) {
        modal.remove();
    }
}

// Select product for review
function selectProductForReview(productId, orderId, productName) {
    closeProductSelectionModal();
    openReviewModal(productId, orderId, productName);
}

// Refresh review buttons after successful review
function refreshOrderReviewButtons() {
    // Reload orders to update review button states
    loadOrders(currentPage);
}

// Reorder
function reorder(orderId) {
    // TODO: Implement reorder
    showToast('Tính năng mua lại đang được phát triển', 'info');
}

// Show toast notification
function showToast(message, type = 'info') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} position-fixed top-0 end-0 m-3`;
    toast.style.zIndex = '9999';
    toast.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'error' ? 'x-circle' : 'info-circle'}"></i>
        ${message}
    `;
    
    document.body.appendChild(toast);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Setup filter tabs
    setupFilterTabs();
    
    // Load initial orders
    const ordersCount = parseInt(document.querySelector('[data-orders-count]')?.getAttribute('data-orders-count') || '0');
    
    if (ordersCount > 0) {
        loadOrders('all', 0);
    }
});
