// StarShop Main JavaScript
document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize AOS (Animate On Scroll) first
    initializeAOS();
    
    // Initialize all components
    initializeHeader();
    initializeSearch();
    initializeSearchToggle();
    initializeMobileMenu();
    initializeScrollEffects();
    initializeNewsletterForm();
    initializeTooltips();
    initializeAnimations();
    initializeCartActions();
    
    // Loading state management
    document.body.classList.add('loaded');
    
});

// AOS (Animate On Scroll) initialization
function initializeAOS() {
    // Check if AOS is available
    if (typeof AOS !== 'undefined') {
        // Detect device capabilities
        const isMobile = window.innerWidth < 768;
        const isTablet = window.innerWidth >= 768 && window.innerWidth < 1024;
        const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        
        // Skip AOS if user prefers reduced motion
        if (prefersReducedMotion) {
            console.log('AOS disabled: User prefers reduced motion');
            return;
        }
        
        AOS.init({
            // Global settings
            duration: isMobile ? 600 : 800,   // Shorter duration on mobile
            easing: 'ease-out-cubic',         // Smooth easing
            once: false,                       // Animation happens only once
            mirror: false,                    // Don't mirror animation on scroll up
            anchorPlacement: 'top-bottom',    // When element triggers animation
            
            // Performance settings
            disable: isMobile,                // Disable on mobile for better performance
            startEvent: 'DOMContentLoaded',   // Event to start AOS
            initClassName: 'aos-init',        // Class added after initialization
            animatedClassName: 'aos-animate', // Class added on animation
            useClassNames: false,             // Use data-aos-* as class names
            disableMutationObserver: isMobile, // Disable on mobile for performance
            debounceDelay: isMobile ? 100 : 50, // Longer debounce on mobile
            throttleDelay: isMobile ? 150 : 99, // Longer throttle on mobile
            
            // Responsive settings
            offset: isMobile ? 80 : 120,      // Smaller offset on mobile
            delay: 0,                         // Delay before animation starts
            anchor: null,                     // Anchor element for offset
            placement: 'top-bottom',          // Placement of element
        });
        
        // Only add event listeners if AOS is enabled
        if (!isMobile) {
            // Refresh AOS when new content is loaded
            window.addEventListener('load', function() {
                AOS.refresh();
            });
            
            // Refresh AOS on window resize with throttling
            let resizeTimeout;
            window.addEventListener('resize', function() {
                clearTimeout(resizeTimeout);
                resizeTimeout = setTimeout(function() {
                    // Re-initialize AOS with new settings if screen size changes significantly
                    const newIsMobile = window.innerWidth < 768;
                    if (newIsMobile !== isMobile) {
                        AOS.refresh();
                    }
                }, 250);
            });
            
            // Handle reduced motion preference changes
            window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', function(e) {
                if (e.matches) {
                    AOS.refresh();
                }
            });
        }
        
        console.log(`AOS initialized successfully - Mobile: ${isMobile}, Tablet: ${isTablet}, Reduced Motion: ${prefersReducedMotion}`);
    } else {
        console.warn('AOS library not loaded');
    }
}


// Simple Header functionality - No effects
function initializeHeader() {
    const header = document.querySelector('.header-wrapper');
    
    if (!header) return;
    
    // Fix dropdown positioning issues
    fixDropdownPositioning();
    
    console.log('Simple header initialized - no scroll effects');
}

// Fix dropdown positioning issues
function fixDropdownPositioning() {
    // Fix Bootstrap dropdown positioning for header dropdowns
    document.addEventListener('shown.bs.dropdown', function(event) {
        const dropdown = event.target.closest('.dropdown');
        if (dropdown && dropdown.closest('.header-wrapper')) {
            const menu = dropdown.querySelector('.dropdown-menu');
            if (menu) {
                // Add a class to force our CSS overrides
                menu.classList.add('header-dropdown-fixed');
                
                // Force reflow to ensure positioning works
                menu.offsetHeight;
                
                // Double-check positioning after a short delay
                setTimeout(() => {
                    if (menu.classList.contains('show')) {
                        menu.style.transform = 'none';
                        menu.style.position = 'absolute';
                        menu.style.top = '100%';
                        menu.style.right = '0';
                        menu.style.left = 'auto';
                        menu.style.zIndex = '1060';
                        menu.style.overflow = 'visible';
                    }
                }, 10);
            }
        }
    });
    
    // Also fix on hide to reset any Bootstrap positioning
    document.addEventListener('hidden.bs.dropdown', function(event) {
        const dropdown = event.target.closest('.dropdown');
        if (dropdown && dropdown.closest('.header-wrapper')) {
            const menu = dropdown.querySelector('.dropdown-menu');
            if (menu) {
                // Remove our fix class
                menu.classList.remove('header-dropdown-fixed');
            }
        }
    });
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
    .then(response => {
        if (response.status === 401) {
            // User not authenticated
            showToast('Vui lòng đăng nhập để sử dụng tính năng yêu thích', 'warning');
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
            return;
        }
        return response.json();
    })
    .then(data => {
        if (data && data.success) {
            updateWishlistCount(data.data.favoriteCount);
            showToast(data.data.isFavorite ? 'Đã thêm vào danh sách yêu thích' : 'Đã xóa khỏi danh sách yêu thích', 'success');
        } else if (data && !data.success) {
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
            showToast(data.message || 'Có lỗi xảy ra', 'error');
        }
    })
    .catch(error => {
        showToast('Có lỗi xảy ra khi thực hiện yêu cầu', 'error');
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

function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
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

// Search Toggle Functionality
function initializeSearchToggle() {
    const searchToggleBtn = document.getElementById('searchToggleBtn') || document.querySelector('.search-toggle-btn');
    const searchCollapse = document.getElementById('searchCollapse');
    
    console.log('Initializing search toggle:', { searchToggleBtn, searchCollapse });
    
    if (!searchToggleBtn || !searchCollapse) {
        console.error('Search elements not found during initialization');
        return;
    }
    
    // Add event listener as backup to onclick
    searchToggleBtn.addEventListener('click', function(e) {
        e.preventDefault();
        console.log('Search toggle clicked via event listener');
        toggleSearch();
    });
    
    // Close search when clicking outside
    document.addEventListener('click', function(e) {
        const searchBox = document.getElementById('searchCollapse');
        const toggleBtn = document.getElementById('searchToggleBtn');
        
        if (searchBox && toggleBtn && searchBox.classList.contains('show')) {
            // Check if click is outside both search box and toggle button
            if (!searchBox.contains(e.target) && !toggleBtn.contains(e.target)) {
                console.log('👆 Clicked outside - closing search');
                hideSearch();
            }
        }
    });
    
    // Close search on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            const searchBox = document.getElementById('searchCollapse');
            if (searchBox && searchBox.classList.contains('show')) {
                console.log('⌨️ Escape pressed - closing search');
                hideSearch();
            }
        }
    });
}

// Enhanced Search Toggle Function
function toggleSearch() {
    console.log('🔍 === SEARCH TOGGLE ===');
    
    const searchBox = document.getElementById('searchCollapse');
    const toggleBtn = document.getElementById('searchToggleBtn');
    
    if (!searchBox) {
        console.error('❌ Search box not found!');
        return;
    }
    
    const isVisible = searchBox.classList.contains('show');
    console.log('Current state:', isVisible ? 'VISIBLE' : 'HIDDEN');
    
    if (isVisible) {
        // Hide search
        hideSearch();
    } else {
        // Show search
        showSearch();
    }
}

// Show Search Function
function showSearch() {
    console.log('🔓 SHOWING search...');
    
    const searchBox = document.getElementById('searchCollapse');
    const toggleBtn = document.getElementById('searchToggleBtn');
    
    if (searchBox) {
        searchBox.classList.add('show');
        
        // Focus on input after animation
        setTimeout(() => {
            const input = searchBox.querySelector('.search-input');
            if (input) {
                input.focus();
                input.select(); // Select all text if any
                console.log('🎯 Input focused and selected');
            }
        }, 300);
    }
    
    if (toggleBtn) {
        toggleBtn.classList.add('active');
    }
    
    console.log('✅ Search box is now VISIBLE');
}

// Hide Search Function
function hideSearch() {
    console.log('🔒 HIDING search...');
    
    const searchBox = document.getElementById('searchCollapse');
    const toggleBtn = document.getElementById('searchToggleBtn');
    
    if (searchBox) {
        searchBox.classList.remove('show');
        
        // Blur input
        const input = searchBox.querySelector('.search-input');
        if (input) {
            input.blur();
        }
    }
    
    if (toggleBtn) {
        toggleBtn.classList.remove('active');
    }
    
    console.log('✅ Search box is now HIDDEN');
}

// Close search function - specifically for X button
function closeSearch() {
    console.log('❌ CLOSE button clicked');
    hideSearch();
}

// Test function - call this from browser console to test
function testSearchToggle() {
    console.log('=== Testing Search Toggle ===');
    const searchCollapse = document.getElementById('searchCollapse');
    const searchToggleBtn = document.getElementById('searchToggleBtn');
    
    console.log('Elements found:', {
        searchCollapse: !!searchCollapse,
        searchToggleBtn: !!searchToggleBtn,
        searchCollapseClasses: searchCollapse ? searchCollapse.className : 'not found',
        searchToggleBtnClasses: searchToggleBtn ? searchToggleBtn.className : 'not found'
    });
    
    if (searchCollapse) {
        console.log('Manually adding show class...');
        searchCollapse.classList.add('show');
        console.log('Classes after manual add:', searchCollapse.className);
    }
}
