// StarShop Main JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize all components
    initializeHeader();
    initializeSearch();
    initializeMobileMenu();
    initializeScrollEffects();
    initializeNewsletterForm();
    initializeTooltips();
    initializeAnimations();
    initializeCartActions();
    
    // Loading state management
    document.body.classList.add('loaded');
    
});

// Header functionality
function initializeHeader() {
    const header = document.querySelector('.header-wrapper');
    const topBar = document.querySelector('.top-bar');
    
    if (!header) return;
    
    // Sticky header on scroll
    let lastScrollTop = 0;
    let headerHeight = header.offsetHeight;
    
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        // Add/remove scrolled class
        if (scrollTop > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
        
        // Hide/show header on scroll
        if (scrollTop > headerHeight) {
            if (scrollTop > lastScrollTop) {
                // Scrolling down
                header.classList.add('header-hidden');
            } else {
                // Scrolling up
                header.classList.remove('header-hidden');
            }
        }
        
        lastScrollTop = scrollTop;
    });
    
    // Hide top bar on mobile scroll
    if (window.innerWidth <= 768 && topBar) {
        window.addEventListener('scroll', function() {
            if (window.pageYOffset > 100) {
                topBar.style.transform = 'translateY(-100%)';
            } else {
                topBar.style.transform = 'translateY(0)';
            }
        });
    }
}

// Search functionality
function initializeSearch() {
    const searchInput = document.querySelector('.search-input');
    const searchSuggestions = document.getElementById('searchSuggestions');
    
    if (!searchInput || !searchSuggestions) return;
    
    let searchTimeout;
    
    // Search suggestions
    searchInput.addEventListener('input', function() {
        const query = this.value.trim();
        
        clearTimeout(searchTimeout);
        
        if (query.length >= 2) {
            searchTimeout = setTimeout(() => {
                fetchSearchSuggestions(query);
            }, 300);
        } else {
            hideSuggestions();
        }
    });
    
    // Hide suggestions when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.search-bar')) {
            hideSuggestions();
        }
    });
    
    // Keyboard navigation for suggestions
    searchInput.addEventListener('keydown', function(e) {
        const suggestions = searchSuggestions.querySelectorAll('.suggestion-item');
        let activeIndex = Array.from(suggestions).findIndex(item => item.classList.contains('active'));
        
        switch(e.key) {
            case 'ArrowDown':
                e.preventDefault();
                activeIndex = Math.min(activeIndex + 1, suggestions.length - 1);
                updateActiveSuggestion(suggestions, activeIndex);
                break;
                
            case 'ArrowUp':
                e.preventDefault();
                activeIndex = Math.max(activeIndex - 1, -1);
                updateActiveSuggestion(suggestions, activeIndex);
                break;
                
            case 'Enter':
                const activeSuggestion = suggestions[activeIndex];
                if (activeSuggestion) {
                    e.preventDefault();
                    window.location.href = activeSuggestion.href;
                }
                break;
                
            case 'Escape':
                hideSuggestions();
                searchInput.blur();
                break;
        }
    });
    
    function fetchSearchSuggestions(query) {
        // Mock search suggestions - replace with actual API call
        const mockSuggestions = [
            { title: 'Hoa hồng đỏ', url: '/products/hoa-hong-do', category: 'Hoa tình yêu' },
            { title: 'Hoa sinh nhật', url: '/categories/sinh-nhat', category: 'Danh mục' },
            { title: 'Bukê cưới', url: '/products/buke-cuoi', category: 'Hoa cưới' },
            { title: 'Hoa tulip', url: '/products/hoa-tulip', category: 'Hoa nhập khẩu' }
        ];
        
        const filteredSuggestions = mockSuggestions.filter(item => 
            item.title.toLowerCase().includes(query.toLowerCase())
        );
        
        displaySuggestions(filteredSuggestions);
    }
    
    function displaySuggestions(suggestions) {
        if (suggestions.length === 0) {
            hideSuggestions();
            return;
        }
        
        const html = suggestions.map(suggestion => `
            <a href="${suggestion.url}" class="suggestion-item">
                <div class="suggestion-content">
                    <i class="bi bi-search suggestion-icon"></i>
                    <div class="suggestion-text">
                        <div class="suggestion-title">${suggestion.title}</div>
                        <div class="suggestion-category">${suggestion.category}</div>
                    </div>
                </div>
            </a>
        `).join('');
        
        searchSuggestions.innerHTML = html;
        searchSuggestions.classList.add('show');
    }
    
    function hideSuggestions() {
        searchSuggestions.classList.remove('show');
        searchSuggestions.innerHTML = '';
    }
    
    function updateActiveSuggestion(suggestions, activeIndex) {
        suggestions.forEach((item, index) => {
            item.classList.toggle('active', index === activeIndex);
        });
        
        if (activeIndex >= 0 && suggestions[activeIndex]) {
            suggestions[activeIndex].scrollIntoView({ block: 'nearest' });
        }
    }
}

// Mobile menu functionality
function initializeMobileMenu() {
    const mobileDropdownToggles = document.querySelectorAll('.mobile-dropdown-toggle');
    
    mobileDropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            
            const submenu = this.nextElementSibling;
            const icon = this.querySelector('.bi-chevron-down');
            
            if (submenu) {
                submenu.classList.toggle('show');
                icon.classList.toggle('rotate');
                this.classList.toggle('active');
            }
        });
    });
    
    // Close mobile menu when clicking on links
    const mobileNavLinks = document.querySelectorAll('.mobile-nav-link:not(.mobile-dropdown-toggle)');
    const offcanvas = document.getElementById('mobileMenu');
    
    mobileNavLinks.forEach(link => {
        link.addEventListener('click', function() {
            if (offcanvas) {
                const bsOffcanvas = bootstrap.Offcanvas.getInstance(offcanvas);
                if (bsOffcanvas) {
                    bsOffcanvas.hide();
                }
            }
        });
    });
}

// Scroll effects
function initializeScrollEffects() {
    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Parallax effect for hero sections
    const heroSections = document.querySelectorAll('.hero-section');
    
    if (heroSections.length > 0) {
        window.addEventListener('scroll', function() {
            const scrolled = window.pageYOffset;
            
            heroSections.forEach(hero => {
                const rate = scrolled * -0.5;
                hero.style.transform = `translateY(${rate}px)`;
            });
        });
    }
    
    // Fade in animation on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    document.querySelectorAll('.animate-on-scroll').forEach(el => {
        observer.observe(el);
    });
}

// Newsletter form
function initializeNewsletterForm() {
    const newsletterForms = document.querySelectorAll('.newsletter-form');
    
    newsletterForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = this.querySelector('input[name="email"]').value;
            const button = this.querySelector('.btn-newsletter');
            const originalHTML = button.innerHTML;
            
            // Validate email
            if (!validateEmail(email)) {
                showToast('Vui lòng nhập địa chỉ email hợp lệ', 'error');
                return;
            }
            
            // Show loading state
            button.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i>';
            button.disabled = true;
            
            // Simulate API call
            setTimeout(() => {
                // Success
                showToast('Cảm ơn bạn đã đăng ký nhận tin!', 'success');
                this.reset();
                
                // Reset button
                button.innerHTML = originalHTML;
                button.disabled = false;
            }, 1500);
        });
    });
}

// Initialize tooltips
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Initialize animations
function initializeAnimations() {
    // Heartbeat animation for logo
    const heartbeatElements = document.querySelectorAll('.heartbeat');
    
    heartbeatElements.forEach(element => {
        // Random delay for natural effect
        const delay = Math.random() * 2000;
        setTimeout(() => {
            element.style.animationDelay = `${delay}ms`;
        }, 100);
    });
    
    // Floating petals animation
    const petals = document.querySelectorAll('.petal');
    
    petals.forEach((petal, index) => {
        // Random properties for each petal
        const duration = 10 + Math.random() * 10; // 10-20s
        const delay = Math.random() * 5; // 0-5s delay
        const xOffset = Math.random() * 100; // Random horizontal position
        
        petal.style.animationDuration = `${duration}s`;
        petal.style.animationDelay = `${delay}s`;
        petal.style.left = `${xOffset}%`;
    });
}

// Cart actions
function initializeCartActions() {
    // Add to cart buttons
    document.addEventListener('click', function(e) {
        if (e.target.matches('.btn-add-to-cart') || e.target.closest('.btn-add-to-cart')) {
            e.preventDefault();
            
            const button = e.target.closest('.btn-add-to-cart');
            const productId = button.dataset.productId;
            
            addToCart(productId, button);
        }
        
        // Wishlist buttons
        if (e.target.matches('.btn-wishlist') || e.target.closest('.btn-wishlist')) {
            e.preventDefault();
            
            const button = e.target.closest('.btn-wishlist');
            const productId = button.dataset.productId;
            
            toggleWishlist(productId, button);
        }
    });
}

// Utility functions
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function showToast(message, type = 'info') {
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        toastContainer.style.zIndex = '1080';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast
    const toastId = 'toast-' + Date.now();
    const toastHTML = `
        <div id="${toastId}" class="toast toast-${type}" role="alert">
            <div class="toast-header">
                <i class="bi ${getToastIcon(type)} me-2"></i>
                <strong class="me-auto">StarShop</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    
    // Show toast
    const toast = new bootstrap.Toast(document.getElementById(toastId));
    toast.show();
    
    // Remove toast element after it's hidden
    document.getElementById(toastId).addEventListener('hidden.bs.toast', function() {
        this.remove();
    });
}

function getToastIcon(type) {
    const icons = {
        success: 'bi-check-circle-fill text-success',
        error: 'bi-exclamation-circle-fill text-danger',
        warning: 'bi-exclamation-triangle-fill text-warning',
        info: 'bi-info-circle-fill text-info'
    };
    return icons[type] || icons.info;
}

function addToCart(productId, button) {
    const originalHTML = button.innerHTML;
    
    // Show loading state
    button.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i> Đang thêm...';
    button.disabled = true;
    
    // Simulate API call
    fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({ productId: productId, quantity: 1 })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('Đã thêm sản phẩm vào giỏ hàng', 'success');
            updateCartCount(data.cartCount);
            
            // Success state
            button.innerHTML = '<i class="bi bi-check"></i> Đã thêm';
            button.classList.add('btn-success');
            
            // Reset after 2 seconds
            setTimeout(() => {
                button.innerHTML = originalHTML;
                button.classList.remove('btn-success');
                button.disabled = false;
            }, 2000);
        } else {
            throw new Error(data.message || 'Có lỗi xảy ra');
        }
    })
    .catch(error => {
        showToast(error.message || 'Không thể thêm sản phẩm vào giỏ hàng', 'error');
        
        // Reset button
        button.innerHTML = originalHTML;
        button.disabled = false;
    });
}

function toggleWishlist(productId, button) {
    const icon = button.querySelector('i');
    const isInWishlist = icon.classList.contains('bi-heart-fill');
    
    // Optimistic update
    if (isInWishlist) {
        icon.classList.remove('bi-heart-fill');
        icon.classList.add('bi-heart');
        button.classList.remove('active');
    } else {
        icon.classList.remove('bi-heart');
        icon.classList.add('bi-heart-fill');
        button.classList.add('active');
    }
    
    // API call
    fetch('/api/wishlist/toggle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({ productId: productId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateWishlistCount(data.wishlistCount);
            showToast(data.inWishlist ? 'Đã thêm vào danh sách yêu thích' : 'Đã xóa khỏi danh sách yêu thích', 'success');
        } else {
            // Revert optimistic update
            if (isInWishlist) {
                icon.classList.remove('bi-heart');
                icon.classList.add('bi-heart-fill');
                button.classList.add('active');
            } else {
                icon.classList.remove('bi-heart-fill');
                icon.classList.add('bi-heart');
                button.classList.remove('active');
            }
            throw new Error(data.message);
        }
    })
    .catch(error => {
        showToast(error.message || 'Có lỗi xảy ra', 'error');
    });
}

function updateCartCount(count) {
    const cartCountElements = document.querySelectorAll('.cart-count');
    cartCountElements.forEach(element => {
        if (count > 0) {
            element.textContent = count;
            element.style.display = 'block';
        } else {
            element.style.display = 'none';
        }
    });
}

function updateWishlistCount(count) {
    const wishlistCountElements = document.querySelectorAll('.wishlist-count');
    wishlistCountElements.forEach(element => {
        if (count > 0) {
            element.textContent = count;
            element.style.display = 'block';
        } else {
            element.style.display = 'none';
        }
    });
}

// CSS animations
const style = document.createElement('style');
style.textContent = `
    .spin {
        animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
    
    .animate-on-scroll {
        opacity: 0;
        transform: translateY(30px);
        transition: all 0.6s ease-out;
    }
    
    .animate-on-scroll.animate-in {
        opacity: 1;
        transform: translateY(0);
    }
    
    .header-wrapper.scrolled {
        box-shadow: 0 2px 20px rgba(0,0,0,0.1);
        backdrop-filter: blur(10px);
    }
    
    .header-wrapper.header-hidden {
        transform: translateY(-100%);
    }
    
    .mobile-submenu {
        max-height: 0;
        overflow: hidden;
        transition: max-height 0.3s ease;
        padding-left: 1rem;
    }
    
    .mobile-submenu.show {
        max-height: 300px;
    }
    
    .mobile-dropdown-toggle .bi-chevron-down {
        transition: transform 0.3s ease;
    }
    
    .mobile-dropdown-toggle .bi-chevron-down.rotate {
        transform: rotate(180deg);
    }
    
    .suggestion-item {
        display: block;
        padding: 0.75rem 1rem;
        text-decoration: none;
        color: var(--text-primary);
        border-bottom: 1px solid var(--border-light);
        transition: background-color 0.2s ease;
    }
    
    .suggestion-item:hover,
    .suggestion-item.active {
        background-color: var(--bg-light);
        color: var(--primary);
    }
    
    .suggestion-content {
        display: flex;
        align-items: center;
    }
    
    .suggestion-icon {
        margin-right: 0.75rem;
        color: var(--text-secondary);
    }
    
    .suggestion-title {
        font-weight: 500;
        margin-bottom: 0.25rem;
    }
    
    .suggestion-category {
        font-size: 0.8rem;
        color: var(--text-secondary);
    }
    
    .search-suggestions {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: white;
        border: 1px solid var(--border);
        border-top: none;
        border-radius: 0 0 8px 8px;
        box-shadow: var(--shadow-lg);
        z-index: 1000;
        max-height: 300px;
        overflow-y: auto;
        display: none;
    }
    
    .search-suggestions.show {
        display: block;
    }
    
    .toast-success .toast-header {
        background-color: var(--success-light);
        border-bottom: 1px solid var(--success);
    }
    
    .toast-error .toast-header {
        background-color: var(--danger-light);
        border-bottom: 1px solid var(--danger);
    }
    
    .toast-warning .toast-header {
        background-color: var(--warning-light);
        border-bottom: 1px solid var(--warning);
    }
    
    .toast-info .toast-header {
        background-color: var(--info-light);
        border-bottom: 1px solid var(--info);
    }
`;

document.head.appendChild(style);
