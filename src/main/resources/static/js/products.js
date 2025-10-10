/**
 * Products Page JavaScript
 * Professional interactions for product listing and detail pages
 * Following rules.mdc specifications for client-side functionality
 */

(function() {
    'use strict';

    // ================================
    // GLOBAL VARIABLES & CONFIG
    // ================================
    
    const CONFIG = {
        DEBOUNCE_DELAY: 300,
        ANIMATION_DURATION: 300,
        TOAST_DURATION: 3000,
        MAX_QUANTITY: 99,
        MIN_QUANTITY: 1
    };

    let searchTimeout = null;
    let isLoading = false;

    // ================================
    // UTILITY FUNCTIONS
    // ================================
    
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Helper function to safely set class on icon (SVG or regular element)
    function setIconClass(iconElement, className) {
        if (!iconElement) return;
        
        try {
            if (iconElement.tagName === 'SVG') {
                iconElement.setAttribute('class', className);
            } else {
                iconElement.className = className;
            }
        } catch (error) {
            console.warn('Error setting icon class:', error);
            // Fallback: try setAttribute for all elements
            try {
                iconElement.setAttribute('class', className);
            } catch (fallbackError) {
                console.error('Failed to set class even with setAttribute:', fallbackError);
            }
        }
    }
    
    // Helper function to safely get class from icon
    function getIconClass(iconElement) {
        if (!iconElement) return '';
        
        if (iconElement.tagName === 'SVG') {
            return iconElement.getAttribute('class') || '';
        } else {
            return iconElement.className || '';
        }
    }

    function showToast(message, type = 'success') {
        // Remove existing toasts
        const existingToasts = document.querySelectorAll('.toast-notification');
        existingToasts.forEach(toast => toast.remove());

        // Create new toast
        const toast = document.createElement('div');
        toast.className = `toast-notification toast-${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    ${type === 'success' ? '<path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm3.857-9.809a.75.75 0 0 0-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 1 0-1.06 1.061l2.5 2.5a.75.75 0 0 0 1.137-.089l4-5.5Z" clip-rule="evenodd" />' : '<path fill-rule="evenodd" d="M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0Zm-8-5a.75.75 0 0 1 .75.75v4.5a.75.75 0 0 1-1.5 0v-4.5A.75.75 0 0 1 10 5Zm0 10a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z" clip-rule="evenodd" />'}
                </svg>
                <span>${message}</span>
            </div>
        `;

        document.body.appendChild(toast);

        // Show toast
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // Hide toast after duration
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), CONFIG.ANIMATION_DURATION);
        }, CONFIG.TOAST_DURATION);
    }

    function setLoadingState(element, loading = true) {
        if (loading) {
            element.dataset.originalContent = element.innerHTML;
            element.innerHTML = '<svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm.75-13a.75.75 0 0 0-1.5 0v5c0 .414.336.75.75.75h4a.75.75 0 0 0 0-1.5h-3.25V5Z" clip-rule="evenodd" /></svg> Đang xử lý...';
            element.disabled = true;
        } else {
            element.innerHTML = element.dataset.originalContent || element.innerHTML;
            element.disabled = false;
            delete element.dataset.originalContent;
        }
    }

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    function validateQuantity(value) {
        const quantity = parseInt(value) || CONFIG.MIN_QUANTITY;
        return Math.min(Math.max(quantity, CONFIG.MIN_QUANTITY), CONFIG.MAX_QUANTITY);
    }

    // ================================
    // SEARCH FUNCTIONALITY
    // ================================
    
    function initializeSearch() {
        const searchForm = document.querySelector('.search-form');
        const searchInput = document.querySelector('.search-input');
        
        if (!searchForm || !searchInput) return;

        // Real-time search suggestions (placeholder for future implementation)
        const debouncedSearch = debounce((query) => {
            if (query.length < 2) return;
            
            // TODO: Implement search suggestions API call
            console.log('Search suggestions for:', query);
        }, CONFIG.DEBOUNCE_DELAY);

        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();
            debouncedSearch(query);
        });

        // Enhanced form submission
        searchForm.addEventListener('submit', (e) => {
            const query = searchInput.value.trim();
            
            if (!query) {
                e.preventDefault();
                searchInput.focus();
                showToast('Vui lòng nhập từ khóa tìm kiếm', 'error');
                return;
            }

            // Show loading state
            const submitButton = searchForm.querySelector('.btn-search');
            if (submitButton) {
                setLoadingState(submitButton);
            }
        });
    }

    // ================================
    // PRODUCT GRID FUNCTIONALITY
    // ================================
    
    function initializeProductGrid() {
        // View toggle functionality
        initializeViewToggle();
        
        // Product actions
        initializeProductActions();
        
        // Quick view functionality
        initializeQuickView();
        
        // Load initial favorite status
        loadInitialFavoriteStatus();
    }

    function initializeViewToggle() {
        const viewToggleButtons = document.querySelectorAll('.btn-view-toggle');
        const productsGrid = document.getElementById('productsGrid');
        
        if (!viewToggleButtons.length || !productsGrid) return;

        viewToggleButtons.forEach(button => {
            button.addEventListener('click', function() {
                const view = this.dataset.view;
                
                // Update active state
                viewToggleButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');
                
                // Update grid class with animation
                if (view === 'list') {
                    productsGrid.style.opacity = '1';
                    setTimeout(() => {
                        productsGrid.classList.add('products-list-view');
                        productsGrid.style.opacity = '1';
                    }, 150);
                } else {
                    productsGrid.style.opacity = '1';
                    setTimeout(() => {
                        productsGrid.classList.remove('products-list-view');
                        productsGrid.style.opacity = '1';
                    }, 150);
                }
                
                // Save preference
                localStorage.setItem('productsView', view);
                
                // Track analytics
                if (typeof gtag !== 'undefined') {
                    gtag('event', 'view_toggle', {
                        'view_type': view
                    });
                }
            });
        });
        
        // Load saved view preference
        const savedView = localStorage.getItem('productsView');
        if (savedView === 'list') {
            const listButton = document.querySelector('[data-view="list"]');
            if (listButton) listButton.click();
        }
    }

    function initializeProductActions() {
        console.log('Products.js: Initializing product actions...');
        
        // Add to cart buttons
        document.addEventListener('click', function(e) {
            if (e.target.matches('.btn-add-to-cart') || e.target.closest('.btn-add-to-cart')) {
                e.preventDefault();
                const button = e.target.matches('.btn-add-to-cart') ? e.target : e.target.closest('.btn-add-to-cart');
                handleAddToCart(button);
            }
        });

        // Wishlist buttons
        console.log('Products.js: Setting up wishlist event listeners...');
        document.addEventListener('click', function(e) {
            if (e.target.matches('.btn-wishlist') || e.target.closest('.btn-wishlist')) {
                console.log('Products.js: Wishlist button clicked!');
                e.preventDefault();
                const button = e.target.matches('.btn-wishlist') ? e.target : e.target.closest('.btn-wishlist');
                handleWishlistToggle(button);
            }
        });
    }

    function loadInitialFavoriteStatus() {
        const wishlistButtons = document.querySelectorAll('.btn-wishlist');
        
        wishlistButtons.forEach(button => {
            const productId = button.dataset.productId;
            if (productId) {
                // Get CSRF token for status check
                const csrfToken = getCsrfToken();
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                
                fetch(`/api/wishlist/status/${productId}`, {
                    method: 'GET',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        [csrfHeader]: csrfToken
                    },
                    credentials: 'same-origin' // Include cookies for authentication
                })
                .then(response => {
                    if (response.status === 401) {
                        // User not authenticated, keep default state
                        return;
                    }
                    return response.json();
                })
                .then(data => {
                    if (data && data.success && data.data && data.data.success) {
                        const icon = button.querySelector('i');
                        const isInWishlist = data.data.isFavorite || data.data.isInWishlist;
                        
                        if (isInWishlist) {
                            icon.outerHTML = '<svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path d="m9.653 16.915-.005-.003-.019-.01a20.759 20.759 0 0 1-1.162-.682 22.045 22.045 0 0 1-2.582-1.9C4.045 12.733 2 10.352 2 7.5a4.5 4.5 0 0 1 8-2.828A4.5 4.5 0 0 1 18 7.5c0 2.852-2.044 5.233-3.885 6.82a22.049 22.049 0 0 1-3.744 2.582l-.019.01-.005.003h-.002a.739.739 0 0 1-.69.001l-.002-.001Z" /></svg>';
                            button.classList.add('active');
                        } else {
                            icon.outerHTML = '<svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" /></svg>';
                            button.classList.remove('active');
                        }
                    }
                })
                .catch(error => {
                    console.log('Error loading favorite status:', error);
                });
            }
        });
    }

    function handleAddToCart(button) {
        const productId = button.dataset.productId;
        const quantity = document.getElementById('quantity')?.value || 1;
        
        if (!productId) {
            showToast('Không thể thêm sản phẩm vào giỏ hàng', 'error');
            return;
        }

        // Show loading state
        const originalHTML = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm.75-13a.75.75 0 0 0-1.5 0v5c0 .414.336.75.75.75h4a.75.75 0 0 0 0-1.5h-3.25V5Z" clip-rule="evenodd" /></svg> Đang thêm...';

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // API call to add to cart
        fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
                [csrfHeader]: csrfToken
            },
            credentials: 'same-origin',
            body: JSON.stringify({ 
                productId: parseInt(productId), 
                quantity: parseInt(quantity) 
            })
        })
        .then(response => {
            if (response.status === 401) {
                showToast('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng', 'warning');
                button.innerHTML = originalHTML;
                button.disabled = false;
                return null; // Return null instead of undefined
            }
            return response.json();
        })
        .then(data => {
            if (!data) {
                // Handle case when data is null (401 response)
                return;
            }
            
            if (data && data.success && data.data && data.data.success) {
                // Success state
                button.innerHTML = '<svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 0 1 .143 1.052l-8 10.5a.75.75 0 0 1-1.127.075l-4.5-4.5a.75.75 0 0 1 1.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 0 1 1.05-.143Z" clip-rule="evenodd" /></svg> Đã thêm!';
                button.classList.add('success');
                
                showToast(data.data.message || `Đã thêm sản phẩm vào giỏ hàng`, 'success');
                
                // Update cart count in header (realtime)
                const totalItems = data.data.totalItems;
                if (totalItems !== undefined) {
                    if (typeof updateCartCount === 'function') {
                        updateCartCount(totalItems);
                    } else if (typeof window.updateCartCount === 'function') {
                        window.updateCartCount(totalItems);
                    }
                }
                
                // Reset button after delay
                setTimeout(() => {
                    button.innerHTML = originalHTML;
                    button.classList.remove('success');
                    button.disabled = false;
                }, 1500);
                
                // Track analytics
                if (typeof gtag !== 'undefined') {
                    gtag('event', 'add_to_cart', {
                        'currency': 'VND',
                        'items': [{
                            'item_id': productId,
                            'quantity': parseInt(quantity)
                        }]
                    });
                }
            } else {
                // Error state
                button.innerHTML = originalHTML;
                button.disabled = false;
                const errorMessage = (data && data.error) || (data && data.data && data.data.message) || 'Có lỗi xảy ra khi thêm vào giỏ hàng';
                showToast(errorMessage, 'error');
            }
        })
        .catch(error => {
            console.error('Error adding to cart:', error);
            button.innerHTML = originalHTML;
            button.disabled = false;
            showToast('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng', 'error');
        });
    }

    function handleWishlistToggle(button) {
        if (!button) {
            console.error('Button is null or undefined');
            return;
        }
        
        const productId = button.dataset ? button.dataset.productId : null;
        let icon = button.querySelector('svg') || button.querySelector('i');
        
        if (!productId) {
            console.error('Product ID not found on button:', button);
            console.error('Button dataset:', button.dataset);
            console.error('Button attributes:', Array.from(button.attributes || []).map(attr => ({name: attr.name, value: attr.value})));
            return;
        }
        
        if (!icon) {
            console.error('Icon not found in button:', button);
            return;
        }

        // Disable button to prevent multiple clicks
        button.disabled = true;
        const originalContent = getIconClass(icon);
        const originalIconHTML = icon.outerHTML;
        icon.outerHTML = '<svg class="w-5 h-5 inline-block" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm.75-13a.75.75 0 0 0-1.5 0v5c0 .414.336.75.75.75h4a.75.75 0 0 0 0-1.5h-3.25V5Z" clip-rule="evenodd" /></svg>';
        
        // Re-get icon reference after replacing outerHTML
        icon = button.querySelector('svg') || button.querySelector('i');

        console.log('Wishlist toggle - Product ID:', productId);

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        
        console.log('CSRF Token:', csrfToken);
        console.log('CSRF Header:', csrfHeader);
        
        // API call to toggle wishlist - let server determine the action
        fetch('/api/wishlist/toggle', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
                [csrfHeader]: csrfToken
            },
            credentials: 'same-origin', // Include cookies for authentication
            body: JSON.stringify({ productId: productId })
        })
        .then(response => {
            console.log('API Response Status:', response.status);
            if (response.status === 401) {
                // User not authenticated
                console.log('User not authenticated');
                showToast('Vui lòng đăng nhập để sử dụng tính năng yêu thích', 'warning');
                // Revert to original state
                setIconClass(icon, originalContent);
                button.disabled = false;
                return;
            }
            if (!response.ok) {
                console.log('API Error:', response.status, response.statusText);
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Full API Response:', data);
            if (data && data.data && !data.error) {
                // Get the current wishlist status from server response
                const isInWishlist = data.data.isFavorite || data.data.isInWishlist;
                
                console.log('=== WISHLIST TOGGLE DEBUG ===');
                console.log('Server response:', data);
                console.log('isInWishlist from server:', data.data.isInWishlist);
                console.log('isFavorite (backward compatibility):', data.data.isFavorite);
                console.log('Final isInWishlist value:', isInWishlist);
                console.log('Message from server:', data.data.message);
                
                // Update UI based on server response (database truth)
                if (isInWishlist) {
                    // SVG elements need setAttribute instead of className
                    if (icon.tagName === 'SVG') {
                        setIconClass(icon, 'w-5 h-5 text-red-500');
                        icon.innerHTML = '<path d="m9.653 16.915-.005-.003-.019-.01a20.759 20.759 0 0 1-1.162-.682 22.045 22.045 0 0 1-2.582-1.9C4.045 12.733 2 10.352 2 7.5a4.5 4.5 0 0 1 8-2.828A4.5 4.5 0 0 1 18 7.5c0 2.852-2.044 5.233-3.885 6.82a22.049 22.049 0 0 1-3.744 2.582l-.019.01-.005.003h-.002a.739.739 0 0 1-.69.001l-.002-.001Z" />';
                    } else {
                        setIconClass(icon, 'bi bi-heart-fill');
                    }
                    button.classList.add('active');
                } else {
                    // SVG elements need setAttribute instead of className
                    if (icon.tagName === 'SVG') {
                        setIconClass(icon, 'w-5 h-5');
                        icon.innerHTML = '<path d="m9.653 16.915-.005-.003-.019-.10a20.759 20.759 0 0 1-1.162-.682 22.045 22.045 0 0 1-2.582-1.9C4.045 12.733 2 10.352 2 7.5a4.5 4.5 0 0 1 8-2.828A4.5 4.5 0 0 1 18 7.5c0 2.852-2.044 5.233-3.885 6.82a22.049 22.049 0 0 1-3.744 2.582l-.019.01-.005.003h-.002a.739.739 0 0 1-.69.001l-.002-.001Z" />';
                    } else {
                        setIconClass(icon, 'bi bi-heart');
                    }
                    button.classList.remove('active');
                }
                
                // Use message from server or generate fallback
                const serverMessage = data.data.message;
                const fallbackMessage = isInWishlist ? 'Đã thêm vào danh sách yêu thích' : 'Đã xóa khỏi danh sách yêu thích';
                const message = serverMessage || fallbackMessage;
                
                console.log('Server message:', serverMessage);
                console.log('Final message:', message);
                console.log('UI Updated - Icon class:', getIconClass(icon), 'Button active:', button.classList.contains('active'));
                console.log('=== END DEBUG ===');
                showToast(message, 'success');
                
                // Update wishlist count in header (realtime)
                if (data.data.userWishlistCount !== undefined) {
                    updateWishlistCount(data.data.userWishlistCount);
                } else if (data.data.favoriteCount !== undefined) {
                    // Fallback to old field name
                    updateWishlistCount(data.data.favoriteCount);
                }
                
                // Track analytics
                if (typeof gtag !== 'undefined') {
                    gtag('event', isInWishlist ? 'add_to_wishlist' : 'remove_from_wishlist', {
                        'item_id': productId
                    });
                }
            } else {
                // Revert to original state on error
                console.log('API Error Response:', data);
                setIconClass(icon, originalContent);
                const errorMessage = (data && data.error) || (data && data.data && data.data.message) || 'Có lỗi xảy ra';
                showToast(errorMessage, 'error');
            }
        })
        .catch(error => {
            // Revert to original state on error
            setIconClass(icon, originalContent);
            showToast('Có lỗi xảy ra khi thực hiện yêu cầu', 'error');
        })
        .finally(() => {
            // Re-enable button
            button.disabled = false;
        });
    }

    // Removed duplicate updateCartCount function - using the one with parameter below

    function initializeQuickView() {
        document.addEventListener('click', function(e) {
            if (e.target.matches('.btn-quick-view') || e.target.closest('.btn-quick-view')) {
                e.preventDefault();
                const button = e.target.matches('.btn-quick-view') ? e.target : e.target.closest('.btn-quick-view');
                const productId = button.dataset.productId;
                
                if (productId) {
                    openQuickView(productId);
                }
            }
        });
    }

    function openQuickView(productId) {
        const modal = document.getElementById('quickViewModal');
        const modalContent = document.getElementById('quickViewContent');
        
        if (!modal || !modalContent) return;

        // Show loading spinner
        modalContent.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Đang tải...</span>
                </div>
                <p class="mt-3 text-center">Đang tải thông tin sản phẩm...</p>
            </div>
        `;

        // Show modal
        const bootstrapModal = new bootstrap.Modal(modal);
        bootstrapModal.show();

        // TODO: Load product data via AJAX
        setTimeout(() => {
            modalContent.innerHTML = `
                <div class="quick-view-content">
                    <div class="row">
                        <div class="col-md-6">
                            <img src="/images/products/default-flower.jpg" 
                                 alt="Product" 
                                 class="img-fluid rounded">
                        </div>
                        <div class="col-md-6">
                            <h4>Tên sản phẩm</h4>
                            <p class="text-muted">Mô tả ngắn gọn về sản phẩm...</p>
                            <div class="price mb-3">
                                <span class="h4 text-primary">500.000₫</span>
                            </div>
                            <div class="d-grid gap-2">
                                <button class="btn btn-primary">Thêm vào giỏ hàng</button>
                                <a href="/products/${productId}" class="btn btn-outline-primary">Xem chi tiết</a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }, 800);
    }

    // ================================
    // PRODUCT DETAIL PAGE
    // ================================
    
    function initializeProductDetail() {
        initializeQuantityControls();
        initializeImageGallery();
        initializeProductTabs();
        initializeProductActions();
    }

    function initializeQuantityControls() {
        const quantityInput = document.getElementById('quantity');
        const decreaseBtn = document.querySelector('.btn-quantity-decrease');
        const increaseBtn = document.querySelector('.btn-quantity-increase');
        
        if (!quantityInput) return;

        // Decrease quantity
        if (decreaseBtn) {
            decreaseBtn.addEventListener('click', function() {
                const currentValue = parseInt(quantityInput.value) || CONFIG.MIN_QUANTITY;
                if (currentValue > CONFIG.MIN_QUANTITY) {
                    quantityInput.value = currentValue - 1;
                    updateQuantityDisplay();
                }
            });
        }

        // Increase quantity
        if (increaseBtn) {
            increaseBtn.addEventListener('click', function() {
                const currentValue = parseInt(quantityInput.value) || CONFIG.MIN_QUANTITY;
                if (currentValue < CONFIG.MAX_QUANTITY) {
                    quantityInput.value = currentValue + 1;
                    updateQuantityDisplay();
                }
            });
        }

        // Direct input validation
        quantityInput.addEventListener('input', function() {
            this.value = validateQuantity(this.value);
            updateQuantityDisplay();
        });

        quantityInput.addEventListener('blur', function() {
            this.value = validateQuantity(this.value);
            updateQuantityDisplay();
        });
    }

    function updateQuantityDisplay() {
        // Update any quantity-dependent displays
        const quantity = parseInt(document.getElementById('quantity')?.value) || 1;
        
        // Update total price if shown
        const priceElement = document.querySelector('.current-price');
        if (priceElement && priceElement.dataset.unitPrice) {
            const unitPrice = parseFloat(priceElement.dataset.unitPrice);
            const totalPrice = unitPrice * quantity;
            // Update display if needed
        }
    }

    function initializeImageGallery() {
        const mainImage = document.getElementById('mainProductImage');
        const thumbnails = document.querySelectorAll('.thumbnail-image');
        const zoomOverlay = document.getElementById('imageZoomOverlay');
        
        // Thumbnail click handlers
        thumbnails.forEach(thumbnail => {
            thumbnail.addEventListener('click', function() {
                if (mainImage) {
                    mainImage.src = this.src;
                    mainImage.alt = this.alt;
                    
                    // Update active thumbnail
                    document.querySelectorAll('.thumbnail-item').forEach(item => {
                        item.classList.remove('active');
                    });
                    this.closest('.thumbnail-item').classList.add('active');
                }
            });
        });

        // Image zoom functionality
        if (zoomOverlay && mainImage) {
            zoomOverlay.addEventListener('click', function() {
                openImageModal(mainImage.src, mainImage.alt);
            });
        }
    }

    function openImageModal(imageSrc, imageAlt) {
        const modal = document.createElement('div');
        modal.className = 'image-modal';
        modal.innerHTML = `
            <div class="image-modal-backdrop" onclick="this.parentElement.remove()">
                <img src="${imageSrc}" alt="${imageAlt}" class="image-modal-content">
                <button class="image-modal-close" onclick="this.parentElement.parentElement.remove()">
                    <svg class="w-6 h-6" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path d="M6.28 5.22a.75.75 0 0 0-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 1 0 1.06 1.06L10 11.06l3.72 3.72a.75.75 0 1 0 1.06-1.06L11.06 10l3.72-3.72a.75.75 0 0 0-1.06-1.06L10 8.94 6.28 5.22Z" /></svg>
                </button>
            </div>
        `;
        
        document.body.appendChild(modal);
        
        // Prevent body scrolling
        document.body.style.overflow = 'hidden';
        
        // Remove modal when clicking outside or pressing Escape
        modal.addEventListener('click', function(e) {
            if (e.target === modal || e.target.classList.contains('image-modal-backdrop')) {
                document.body.style.overflow = '';
                modal.remove();
            }
        });
        
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modal.parentElement) {
                document.body.style.overflow = '';
                modal.remove();
            }
        });
    }

    function initializeProductTabs() {
        // Enhanced tab functionality
        const tabButtons = document.querySelectorAll('.product-tabs .nav-link');
        
        tabButtons.forEach(button => {
            button.addEventListener('shown.bs.tab', function(e) {
                const targetTab = e.target.getAttribute('data-bs-target');
                
                // Track tab views
                if (typeof gtag !== 'undefined') {
                    gtag('event', 'view_product_tab', {
                        'tab_name': targetTab.replace('#', '')
                    });
                }
                
                // Lazy load content if needed
                if (targetTab === '#reviews') {
                    loadReviews();
                }
            });
        });
    }

    function loadReviews() {
        // TODO: Implement lazy loading of reviews
        console.log('Loading reviews...');
    }

    // ================================
    // SORTING & FILTERING
    // ================================
    
    function initializeSorting() {
        const sortSelect = document.getElementById('sortSelect');
        
        if (!sortSelect) return;

        sortSelect.addEventListener('change', function() {
            const value = this.value;
            changeSorting(value);
        });
    }

    function changeSorting(value) {
        const url = new URL(window.location);
        
        // Update URL parameters based on sort value
        switch(value) {
            case 'newest':
                url.searchParams.set('sort', 'newest');
                url.searchParams.delete('direction');
                break;
            case 'oldest':
                url.searchParams.set('sort', 'oldest');
                url.searchParams.delete('direction');
                break;
            case 'name':
                url.searchParams.set('sort', 'name');
                url.searchParams.set('direction', 'asc');
                break;
            case 'price-asc':
                url.searchParams.set('sort', 'price');
                url.searchParams.set('direction', 'asc');
                break;
            case 'price-desc':
                url.searchParams.set('sort', 'price');
                url.searchParams.set('direction', 'desc');
                break;
        }
        
        // Reset to first page
        url.searchParams.set('page', '0');
        
        // Show loading state
        const productsGrid = document.getElementById('productsGrid');
        if (productsGrid) {
            productsGrid.style.opacity = '0.6';
        }
        
        // Navigate to new URL
        window.location.href = url.toString();
    }

    // ================================
    // PERFORMANCE OPTIMIZATIONS
    // ================================
    
    function initializePerformanceOptimizations() {
        // Lazy loading for product images
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src || img.src;
                        img.classList.remove('lazy');
                        observer.unobserve(img);
                    }
                });
            });

            document.querySelectorAll('img[loading="lazy"]').forEach(img => {
                imageObserver.observe(img);
            });
        }

        // Preload critical resources
        const preloadLinks = [
            '/css/products.css',
            '/js/products.js'
        ];

        preloadLinks.forEach(href => {
            const link = document.createElement('link');
            link.rel = 'preload';
            link.href = href;
            link.as = href.endsWith('.css') ? 'style' : 'script';
            document.head.appendChild(link);
        });
    }

    // ================================
    // ERROR HANDLING
    // ================================
    
    function initializeErrorHandling() {
        // Global error handler for AJAX requests
        window.addEventListener('unhandledrejection', function(event) {
            console.error('Unhandled promise rejection:', event.reason);
            showToast('Đã xảy ra lỗi. Vui lòng thử lại sau.', 'error');
        });

        // Network error detection
        window.addEventListener('online', function() {
            showToast('Kết nối internet đã được khôi phục');
        });

        window.addEventListener('offline', function() {
            showToast('Mất kết nối internet', 'error');
        });
    }

    // ================================
    // INITIALIZATION
    // ================================
    
    function initialize() {
        console.log('Products.js: Initializing...');
        
        // Check if we're on a products page
        const isProductsPage = document.querySelector('.products-section') || 
                              document.querySelector('.product-detail-section');
        
        console.log('Products.js: isProductsPage =', isProductsPage);
        
        if (!isProductsPage) {
            console.log('Products.js: Not on products page, skipping initialization');
            return;
        }

        // Initialize common functionality
        initializeErrorHandling();
        initializePerformanceOptimizations();
        initializeSearch();
        initializeSorting();

        // Initialize page-specific functionality
        if (document.querySelector('.products-section')) {
            // Products listing page
        console.log('Products.js: Initializing product grid...');
        
        // Test toast notification
        setTimeout(() => {
            showToast('Products.js loaded successfully!', 'success');
        }, 1000);
        
        initializeProductGrid();
        }

        if (document.querySelector('.product-detail-section')) {
            // Product detail page
            initializeProductDetail();
        }

        console.log('Products page initialized successfully');
    }

    // ================================
    // GLOBAL FUNCTIONS (for inline handlers)
    // ================================
    
    // Make some functions globally accessible for inline event handlers
    window.changeSorting = changeSorting;
    
    // Use main.js addToCart function if available, otherwise use local handleAddToCart
    if (typeof window.addToCartFromMain === 'function') {
        window.addToCart = window.addToCartFromMain;
    } else {
        window.addToCart = function(button) {
            handleAddToCart(button);
        };
    }
    
    window.toggleWishlist = function(button) {
        handleWishlistToggle(button);
    };
    
    window.buyNow = function(button) {
        const productId = button.dataset.productId;
        const quantity = document.getElementById('quantity')?.value || 1;
        
        // TODO: Implement buy now functionality
        console.log('Buy now:', { productId, quantity });
        
        // For now, redirect to cart
        window.location.href = '/cart';
    };
    
    window.increaseQuantity = function() {
        const input = document.getElementById('quantity');
        if (input) {
            const currentValue = parseInt(input.value) || CONFIG.MIN_QUANTITY;
            if (currentValue < CONFIG.MAX_QUANTITY) {
                input.value = currentValue + 1;
                updateQuantityDisplay();
            }
        }
    };
    
    window.decreaseQuantity = function() {
        const input = document.getElementById('quantity');
        if (input) {
            const currentValue = parseInt(input.value) || CONFIG.MIN_QUANTITY;
            if (currentValue > CONFIG.MIN_QUANTITY) {
                input.value = currentValue - 1;
                updateQuantityDisplay();
            }
        }
    };

    // ================================
    // UTILITY FUNCTIONS
    // ================================
    
    function getCsrfToken() {
        // Try to get CSRF token from cookie first (Spring Security default)
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'XSRF-TOKEN') {
                return decodeURIComponent(value);
            }
        }
        
        // Fallback to meta tag
        const token = document.querySelector('meta[name="_csrf"]');
        return token ? token.getAttribute('content') : '';
    }

    function updateWishlistCount(count) {
        const wishlistCountElements = document.querySelectorAll('.wishlist-count');
        wishlistCountElements.forEach(element => {
            element.textContent = count;
            if (count > 0) {
                element.classList.remove('hidden');
                element.style.display = 'flex';
            } else {
                element.classList.add('hidden');
                element.style.display = 'none';
            }
        });
        console.log(`Updated wishlist count to: ${count}`);
    }

    // updateCartCount function is now available globally from main.js

    // ================================
    // DOM READY
    // ================================
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }

})();
