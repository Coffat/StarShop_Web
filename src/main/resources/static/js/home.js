// Home Page JavaScript - Simplified & Clean
document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize simplified home page features
    initializeFloatingPetals();
    initializeProductActions();
    initializeScrollAnimations();
    
});

// Floating petals - simplified
function initializeFloatingPetals() {
    const petals = document.querySelectorAll('.petal');
    
    // Add random delays to make animation more natural
    petals.forEach((petal, index) => {
        const delay = Math.random() * 15000; // 0-15 seconds
        petal.style.animationDelay = `${delay}ms`;
    });
}

// Product actions - simplified
function initializeProductActions() {
    // Quick add buttons
    const quickAddBtns = document.querySelectorAll('.btn-quick-add');
    quickAddBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.dataset.productId;
            handleQuickAdd(productId, this);
        });
    });
}

// Handle quick add to cart
function handleQuickAdd(productId, button) {
    const originalText = button.innerHTML;
    
    // Show loading state
    button.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Đang thêm...';
    button.disabled = true;
    
    // Simulate API call
    setTimeout(() => {
        // Success state
        button.innerHTML = '<i class="bi bi-check"></i> Đã thêm';
        button.style.background = 'var(--success)';
        
        // Show toast notification
        showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
        
        // Reset button after 2 seconds
        setTimeout(() => {
            button.innerHTML = originalText;
            button.style.background = '';
            button.disabled = false;
        }, 2000);
    }, 1000);
}

// Scroll animations - simplified
function initializeScrollAnimations() {
    // Intersection Observer for fade-in animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Observe elements that should animate in
    const animateElements = document.querySelectorAll('.section-header, .feature-card, .category-card, .product-card, .testimonial-card');
    animateElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';
        element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(element);
    });
}

// Utility functions
function showToast(message, type = 'success') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // Add to page
    document.body.appendChild(toast);
    
    // Show toast
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // Hide and remove toast
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

// Page performance monitoring
window.addEventListener('load', function() {
    document.body.classList.add('page-loaded');
    
    // Performance metrics
    if (window.performance && window.performance.timing) {
        const loadTime = window.performance.timing.loadEventEnd - window.performance.timing.navigationStart;
        console.log(`Home page load time: ${loadTime}ms`);
    }
});

// Error handling
window.addEventListener('error', function(e) {
    console.error('Home page JavaScript error:', e.error);
});
