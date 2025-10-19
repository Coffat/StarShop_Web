/**
 * ACCOUNT ORDERS PAGE - JAVASCRIPT
 * Load và hiển thị orders động từ backend API
 */

// Global variables
let currentPage = 0;
let currentStatus = 'all';
const pageSize = 10;

// Review modal variables
let currentReviewData = {
    productId: null,
    orderItemId: null,
    productName: '',
    productImage: ''
};
let selectedRating = 0;

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
        buttons.push(`
            <button onclick="reviewOrder(${order.id})" 
                    class="px-4 py-2 bg-gradient-to-r from-pink-500 to-purple-600 text-white rounded-lg font-medium hover:shadow-lg transition-all">
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
                    ${order.status === 'COMPLETED' ? `
                        <button onclick="openReviewModal(${item.productId}, ${item.id}, '${item.productName}', '${item.productImage || '/images/placeholder.jpg'}')" 
                                class="mt-2 px-3 py-1.5 bg-gradient-to-r from-pink-500 to-purple-600 text-white text-xs font-medium rounded-lg hover:shadow-md transition-all">
                            <i class="bi bi-star mr-1"></i> Đánh giá
                        </button>
                    ` : ''}
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
        
        // Render orders
        const ordersHtml = data.data.content.map(order => renderOrderCard(order)).join('');
        ordersList.innerHTML = ordersHtml;
        
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

// Track order
function trackOrder(orderId) {
    // TODO: Implement order tracking
    showToast('Tính năng theo dõi đơn hàng đang được phát triển', 'info');
}

// Review order
function reviewOrder(orderId) {
    // Fetch order details to get concrete order items, then open the review modal
    fetch(`/api/orders/${orderId}`)
        .then(async (res) => {
            const body = await res.json().catch(() => ({}));
            if (!res.ok) {
                const msg = (body && (body.message || body.error)) || 'Không thể lấy chi tiết đơn hàng';
                throw new Error(msg);
            }
            return body;
        })
        .then((payload) => {
            // ResponseWrapper shape: { data: { ...order... } } or nested { data: { data: { ... } } }
            const wrapped = payload && payload.data ? payload.data : payload;
            const orderData = wrapped && wrapped.data ? wrapped.data : wrapped;

            // Try multiple shapes to find items
            const items =
                (orderData && orderData.items) ||
                (orderData && orderData.orderItems) ||
                (orderData && orderData.order && (orderData.order.items || orderData.order.orderItems)) ||
                [];

            if (!items || items.length === 0) {
                showToast('Không tìm thấy sản phẩm để đánh giá trong đơn này', 'info');
                return;
            }

            // Pick first item to review (UI also allows per-item review buttons)
            const it = items[0];
            const orderItemId = it.id || it.orderItemId;
            const productId = it.productId || (it.product && it.product.id);
            const productName = it.productName || (it.product && it.product.name) || 'Sản phẩm';
            const productImage = it.productImage || (it.product && it.product.image) || '/images/placeholder.jpg';

            if (!orderItemId || !productId) {
                showToast('Không đủ dữ liệu để mở form đánh giá', 'error');
                return;
            }

            openReviewModal(productId, orderItemId, productName, productImage);
        })
        .catch((err) => {
            console.error('reviewOrder error:', err);
            showToast(err.message || 'Có lỗi xảy ra khi mở form đánh giá', 'error');
        });
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

// ==================== REVIEW MODAL FUNCTIONS ====================

// Open review modal
function openReviewModal(productId, orderItemId, productName, productImage) {
    currentReviewData = {
        productId: productId,
        orderItemId: orderItemId,
        productName: productName,
        productImage: productImage
    };
    
    // Reset form
    selectedRating = 0;
    document.getElementById('reviewProductName').textContent = productName;
    document.getElementById('reviewProductImage').src = productImage;
    document.getElementById('reviewProductImage').alt = productName;
    document.getElementById('reviewComment').value = '';
    document.getElementById('commentCount').textContent = '0/1000';
    
    // Reset stars
    const starButtons = document.querySelectorAll('.star-btn');
    starButtons.forEach(btn => {
        btn.classList.remove('text-yellow-400');
        btn.classList.add('text-gray-300');
        btn.innerHTML = '<i class="bi bi-star"></i>';
    });
    
    document.getElementById('ratingText').textContent = 'Chọn số sao để đánh giá';
    document.getElementById('submitReviewBtn').disabled = true;
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('reviewModal'));
    modal.show();
}

// Handle star rating selection
function selectRating(rating) {
    selectedRating = rating;
    
    const starButtons = document.querySelectorAll('.star-btn');
    const ratingTexts = [
        '', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'
    ];
    
    starButtons.forEach((btn, index) => {
        if (index < rating) {
            btn.classList.remove('text-gray-300');
            btn.classList.add('text-yellow-400');
            btn.innerHTML = '<i class="bi bi-star-fill"></i>';
        } else {
            btn.classList.remove('text-yellow-400');
            btn.classList.add('text-gray-300');
            btn.innerHTML = '<i class="bi bi-star"></i>';
        }
    });
    
    document.getElementById('ratingText').textContent = ratingTexts[rating];
    document.getElementById('submitReviewBtn').disabled = false;
}

// Submit review
async function submitReview() {
    if (selectedRating === 0) {
        showToast('Vui lòng chọn số sao đánh giá', 'error');
        return;
    }
    
    const comment = document.getElementById('reviewComment').value.trim();
    
    try {
        const response = await fetch('/api/reviews', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                orderItemId: currentReviewData.orderItemId,
                rating: selectedRating,
                comment: comment
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showToast('Đánh giá đã được gửi thành công!', 'success');
            
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('reviewModal'));
            modal.hide();
            
            // Reload orders to update UI
            loadOrders(currentStatus, currentPage);
        } else {
            showToast(data.message || 'Có lỗi xảy ra khi gửi đánh giá', 'error');
        }
    } catch (error) {
        console.error('Error submitting review:', error);
        showToast('Có lỗi xảy ra khi gửi đánh giá', 'error');
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Setup filter tabs
    setupFilterTabs();
    
    // Setup star rating buttons
    const starButtons = document.querySelectorAll('.star-btn');
    starButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const rating = parseInt(this.dataset.rating);
            selectRating(rating);
        });
        
        btn.addEventListener('mouseenter', function() {
            const rating = parseInt(this.dataset.rating);
            const starButtons = document.querySelectorAll('.star-btn');
            
            starButtons.forEach((starBtn, index) => {
                if (index < rating) {
                    starBtn.classList.remove('text-gray-300');
                    starBtn.classList.add('text-yellow-400');
                } else {
                    starBtn.classList.remove('text-yellow-400');
                    starBtn.classList.add('text-gray-300');
                }
            });
        });
    });
    
    // Setup comment character counter
    const commentTextarea = document.getElementById('reviewComment');
    if (commentTextarea) {
        commentTextarea.addEventListener('input', function() {
            const count = this.value.length;
            document.getElementById('commentCount').textContent = `${count}/1000`;
        });
    }
    
    // Load initial orders
    const ordersCount = parseInt(document.querySelector('[data-orders-count]')?.getAttribute('data-orders-count') || '0');
    
    if (ordersCount > 0) {
        loadOrders('all', 0);
    }
});
