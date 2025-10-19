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
            <svg class="w-4 h-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path d="M10 2a8 8 0 1 0 0 16 8 8 0 0 0 0-16zm0 14a6 6 0 1 1 0-12 6 6 0 0 1 0 12z"/>
            </svg>
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
                <svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>
                </svg> Hủy đơn
            </button>
        `);
    }
    
    // Track button - for SHIPPED status
    if (order.status === 'SHIPPED') {
        buttons.push(`
            <button onclick="trackOrder(${order.id})" 
                    class="px-4 py-2 bg-purple-50 text-purple-600 rounded-lg font-medium hover:bg-purple-100 transition-colors border border-purple-200">
                <svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
                </svg> Theo dõi
            </button>
        `);
    }
    
    // Review button - for COMPLETED status
    if (order.status === 'COMPLETED') {
        buttons.push(`
            <button onclick="reviewOrder(${order.id})" 
                    class="px-4 py-2 bg-gradient-to-r from-pink-500 to-purple-600 text-white rounded-lg font-medium hover:shadow-lg transition-all">
                <svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10.868 2.884c-.321-.772-1.415-.772-1.736 0l-1.83 4.401-4.753.381c-.833.067-1.171 1.107-.536 1.651l3.62 3.102-1.106 4.637c-.194.813.691 1.456 1.405 1.02L10 15.591l4.069 2.485c.713.436 1.598-.207 1.404-1.02l-1.106-4.637 3.62-3.102c.635-.544.297-1.584-.536-1.65l-4.752-.382-1.831-4.401Z"/>
                </svg> Đánh giá
            </button>
        `);
    }
    
    // Reorder button - for COMPLETED or CANCELLED
    if (order.status === 'COMPLETED' || order.status === 'CANCELLED') {
        buttons.push(`
            <button onclick="reorder(${order.id})" 
                    class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition-colors border border-gray-300">
                <svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M15.312 11.424a5.5 5.5 0 01-9.201 2.466l-.312-.311h2.433a.75.75 0 000-1.5H3.989a.75.75 0 00-.75.75v4.242a.75.75 0 001.5 0v-2.43l.31.31a7 7 0 0011.712-3.138.75.75 0 00-1.449-.39zm1.23-3.723a.75.75 0 00.219-.53V2.929a.75.75 0 00-1.5 0V5.36l-.31-.31A7 7 0 003.239 8.188a.75.75 0 101.448.389A5.5 5.5 0 0113.89 6.11l.311.31h-2.432a.75.75 0 000 1.5h4.243a.75.75 0 00.53-.219z" clip-rule="evenodd"/>
                </svg> Mua lại
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
                            <svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10.868 2.884c-.321-.772-1.415-.772-1.736 0l-1.83 4.401-4.753.381c-.833.067-1.171 1.107-.536 1.651l3.62 3.102-1.106 4.637c-.194.813.691 1.456 1.405 1.02L10 15.591l4.069 2.485c.713.436 1.598-.207 1.404-1.02l-1.106-4.637 3.62-3.102c.635-.544.297-1.584-.536-1.65l-4.752-.382-1.831-4.401Z"/>
                </svg> Đánh giá
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
                    <svg class="w-4 h-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M10 3a1.5 1.5 0 110 3 1.5 1.5 0 010-3zM10 8.5a1.5 1.5 0 110 3 1.5 1.5 0 010-3zM11.5 15.5a1.5 1.5 0 10-3 0 1.5 1.5 0 003 0z"/>
                    </svg> Và ${remainingCount} sản phẩm khác
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
                        <svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5.75 2a.75.75 0 01.75.75V4h7V2.75a.75.75 0 011.5 0V4h.25A2.75 2.75 0 0118 6.75v8.5A2.75 2.75 0 0115.25 18H4.75A2.75 2.75 0 012 15.25v-8.5A2.75 2.75 0 014.75 4H5V2.75A.75.75 0 015.75 2zm-1 5.5c-.69 0-1.25.56-1.25 1.25v6.5c0 .69.56 1.25 1.25 1.25h10.5c.69 0 1.25-.56 1.25-1.25v-6.5c0-.69-.56-1.25-1.25-1.25H4.75z" clip-rule="evenodd"/>
                        </svg>
                        ${formatDate(order.orderDate)}
                    </p>
                </div>
                <button onclick="viewOrderDetail(${order.id})" class="text-pink-600 hover:text-pink-700 font-medium text-sm">
                    Chi tiết <svg class="w-4 h-4 ml-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clip-rule="evenodd"/>
                    </svg>
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
    console.log('Loading orders with status:', status, 'page:', page);
    const ordersList = document.getElementById('ordersList');
    
    if (!ordersList) {
        console.error('ordersList element not found');
        return;
    }
    
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
        
        console.log('Fetching orders from:', apiUrl);
        
        const response = await fetch(apiUrl, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('Response status:', response.status);
        
        if (!response.ok) {
            throw new Error(`Failed to load orders: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('Orders data received:', data);
        
        // Check if we have orders
        if (!data.data || !data.data.content || data.data.content.length === 0) {
            ordersList.innerHTML = `
                <div class="text-center py-20">
                    <div class="inline-flex items-center justify-center w-24 h-24 bg-gradient-to-br from-pink-100 to-purple-100 rounded-full mb-6">
                        <svg class="w-12 h-12 text-pink-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M1 11.27c0-.246.033-.492.099-.73l1.523-5.521A2.75 2.75 0 015.273 3h9.454a2.75 2.75 0 012.651 2.019l1.523 5.52c.066.239.099.485.099.732V15a2 2 0 01-2 2H3a2 2 0 01-2-2v-3.73zM15 16a1 1 0 100-2 1 1 0 000 2zm-8-1a1 1 0 11-2 0 1 1 0 012 0z" clip-rule="evenodd"/>
                        </svg>
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
        console.error('Error details:', error.message);
        ordersList.innerHTML = `
            <div class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
                <svg class="w-8 h-8 text-red-600 mb-3" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8.485 2.495c.673-1.167 2.357-1.167 3.03 0l6.28 10.875c.673 1.167-.17 2.625-1.516 2.625H3.72c-1.347 0-2.189-1.458-1.515-2.625L8.485 2.495zM10 5a.75.75 0 01.75.75v3.5a.75.75 0 01-1.5 0v-3.5A.75.75 0 0110 5zm0 9a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
                </svg>
                <p class="text-red-700 font-medium">Có lỗi xảy ra khi tải đơn hàng.</p>
                <p class="text-red-600 text-sm mt-1">Vui lòng thử lại sau.</p>
                <p class="text-red-500 text-xs mt-2">Lỗi: ${error.message}</p>
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
        <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
            ${type === 'success' ? 
                '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.236 4.53L7.53 10.53a.75.75 0 00-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd"/>' :
                type === 'error' ?
                '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd"/>' :
                '<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.01a.75.75 0 100-1.5H9zm1 3a.75.75 0 000 1.5h.01a.75.75 0 100-1.5H10z" clip-rule="evenodd"/>'
            }
        </svg>
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
        // Keep the SVG structure, just change color
    });
    
    document.getElementById('ratingText').textContent = 'Chọn số sao để đánh giá';
    document.getElementById('submitReviewBtn').disabled = true;
    
    // Show modal with Tailwind classes
    const modal = document.getElementById('reviewModal');
    const modalDialog = document.getElementById('modalDialog');
    const modalBackdrop = document.getElementById('modalBackdrop');
    
    modal.classList.remove('hidden');
    
    // Trigger animation after a small delay
    setTimeout(() => {
        modalDialog.classList.remove('scale-95', 'opacity-0');
        modalDialog.classList.add('scale-100', 'opacity-100');
        modalBackdrop.classList.add('opacity-100');
    }, 10);
    
    // Add event listeners for close buttons
    document.getElementById('closeModalBtn').onclick = closeReviewModal;
    document.getElementById('cancelReviewBtn').onclick = closeReviewModal;
    document.getElementById('modalBackdrop').onclick = closeReviewModal;
    
    // Add event listeners for star buttons
    const starButtonsForModal = document.querySelectorAll('.star-btn');
    starButtonsForModal.forEach((btn, index) => {
        btn.onclick = () => selectRating(index + 1);
    });
}

// Close review modal
function closeReviewModal() {
    const modal = document.getElementById('reviewModal');
    const modalDialog = document.getElementById('modalDialog');
    const modalBackdrop = document.getElementById('modalBackdrop');
    
    // Start fade out animation
    modalDialog.classList.remove('scale-100', 'opacity-100');
    modalDialog.classList.add('scale-95', 'opacity-0');
    modalBackdrop.classList.remove('opacity-100');
    
    // Hide modal after animation
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
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
        } else {
            btn.classList.remove('text-yellow-400');
            btn.classList.add('text-gray-300');
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
            closeReviewModal();
            
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
    console.log('DOM Content Loaded - Initializing orders page');
    
    // Setup filter tabs
    setupFilterTabs();
    
    // Star rating buttons will be set up when modal opens
    
    // Setup comment character counter
    const commentTextarea = document.getElementById('reviewComment');
    if (commentTextarea) {
        commentTextarea.addEventListener('input', function() {
            const count = this.value.length;
            document.getElementById('commentCount').textContent = `${count}/1000`;
        });
    }
    
    // Load initial orders
    console.log('Loading initial orders...');
    loadOrders('all', 0);
});
