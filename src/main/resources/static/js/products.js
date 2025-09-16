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

    function showToast(message, type = 'success') {
        // Remove existing toasts
        const existingToasts = document.querySelectorAll('.toast-notification');
        existingToasts.forEach(toast => toast.remove());

        // Create new toast
        const toast = document.createElement('div');
        toast.className = `toast-notification toast-${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
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
            element.innerHTML = '<i class="bi bi-hourglass"></i> Đang xử lý...';
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
                    productsGrid.style.opacity = '0.7';
                    setTimeout(() => {
                        productsGrid.classList.add('products-list-view');
                        productsGrid.style.opacity = '1';
                    }, 150);
                } else {
                    productsGrid.style.opacity = '0.7';
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
        // Add to cart buttons
        document.addEventListener('click', function(e) {
            if (e.target.matches('.btn-add-to-cart') || e.target.closest('.btn-add-to-cart')) {
                e.preventDefault();
                const button = e.target.matches('.btn-add-to-cart') ? e.target : e.target.closest('.btn-add-to-cart');
                handleAddToCart(button);
            }
        });

        // Wishlist buttons
        document.addEventListener('click', function(e) {
            if (e.target.matches('.btn-wishlist') || e.target.closest('.btn-wishlist')) {
                e.preventDefault();
                const button = e.target.matches('.btn-wishlist') ? e.target : e.target.closest('.btn-wishlist');
                handleWishlistToggle(button);
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
        setLoadingState(button);

        // Simulate API call (replace with actual implementation)
        setTimeout(() => {
            // Success state
            button.innerHTML = '<i class="bi bi-check"></i> Đã thêm!';
            button.classList.add('success');
            
            showToast(`Đã thêm sản phẩm vào giỏ hàng (${quantity} sản phẩm)`);
            
            // Update cart count in header
            updateCartCount();
            
            // Reset button after delay
            setTimeout(() => {
                setLoadingState(button, false);
                button.classList.remove('success');
            }, 1500);
            
            // Track analytics
            if (typeof gtag !== 'undefined') {
                gtag('event', 'add_to_cart', {
                    'currency': 'VND',
                    'value': 0, // Replace with actual product price
                    'items': [{
                        'item_id': productId,
                        'quantity': parseInt(quantity)
                    }]
                });
            }
        }, 1000);
    }

    function handleWishlistToggle(button) {
        const productId = button.dataset.productId;
        const icon = button.querySelector('i');
        
        if (!productId || !icon) return;

        const isAdding = icon.classList.contains('bi-heart');
        
        // Optimistic UI update
        if (isAdding) {
            icon.className = 'bi bi-heart-fill';
            button.classList.add('active');
            showToast('Đã thêm vào danh sách yêu thích');
        } else {
            icon.className = 'bi bi-heart';
            button.classList.remove('active');
            showToast('Đã xóa khỏi danh sách yêu thích');
        }

        // TODO: Implement actual wishlist API call
        console.log(`${isAdding ? 'Adding to' : 'Removing from'} wishlist:`, productId);
        
        // Track analytics
        if (typeof gtag !== 'undefined') {
            gtag('event', isAdding ? 'add_to_wishlist' : 'remove_from_wishlist', {
                'item_id': productId
            });
        }
    }

    function updateCartCount() {
        const cartCountElements = document.querySelectorAll('.cart-count');
        cartCountElements.forEach(element => {
            const currentCount = parseInt(element.textContent) || 0;
            element.textContent = currentCount + 1;
            
            // Add animation
            element.style.transform = 'scale(1.2)';
            setTimeout(() => {
                element.style.transform = 'scale(1)';
            }, 200);
        });
    }

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
                    <i class="bi bi-x-lg"></i>
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
        // Check if we're on a products page
        const isProductsPage = document.querySelector('.products-section') || 
                              document.querySelector('.product-detail-section');
        
        if (!isProductsPage) return;

        // Initialize common functionality
        initializeErrorHandling();
        initializePerformanceOptimizations();
        initializeSearch();
        initializeSorting();

        // Initialize page-specific functionality
        if (document.querySelector('.products-section')) {
            // Products listing page
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
    
    window.addToCart = function(button) {
        handleAddToCart(button);
    };
    
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
    // DOM READY
    // ================================
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }

})();
