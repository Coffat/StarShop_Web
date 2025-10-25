// Language Switcher for StarShop
// Handles language switching functionality with localStorage persistence

class LanguageSwitcher {
    constructor() {
        this.currentLanguage = localStorage.getItem('starshop-language') || 'vi';
        this.translations = {};
        this.init();
    }

    init() {
        this.loadTranslations();
        this.updateLanguageDisplay();
        this.bindEvents();
        this.applyLanguage();
    }

    // Load translation data
    loadTranslations() {
        this.translations = {
            vi: {
                // Header
                'search-placeholder': '🌸 Tìm hoa nhanh - Hoa sinh nhật, tình yêu, cưới...',
                'login': 'Đăng nhập',
                'register': 'Đăng ký',
                'profile': 'Thông tin cá nhân',
                'orders': 'Đơn hàng của tôi',
                'wishlist': 'Danh sách yêu thích',
                'settings': 'Cài đặt',
                'logout': 'Đăng xuất',
                'customer-role': 'Khách hàng',
                
                // Navigation
                'home': 'Trang chủ',
                'categories': 'Danh mục hoa',
                'products': 'Sản phẩm',
                'gifts': 'Quà tặng',
                'occasions': 'Dịp đặc biệt',
                'blog': 'Blog',
                'contact': 'Liên hệ',
                
                // Categories
                'birthday-flowers': 'Hoa sinh nhật',
                'love-flowers': 'Hoa tình yêu',
                'wedding-flowers': 'Hoa cưới',
                'opening-flowers': 'Hoa khai trương',
                'sympathy-flowers': 'Hoa chia buồn',
                'mother-flowers': 'Hoa tặng mẹ',
                
                // Home page
                'hero-title': 'Mang Hoa Đến Niềm Vui Của Bạn',
                'hero-subtitle': 'Khám phá bộ sưu tập hoa tươi cao cấp được tuyển chọn kỹ lưỡng, mang đến những khoảnh khắc đặc biệt nhất trong cuộc sống',
                'explore-products': 'Khám phá sản phẩm',
                'view-catalog': 'Xem danh mục',
                
                // Why StarShop
                'why-starshop': 'Tại sao chọn StarShop?',
                'fresh-flowers': 'Hoa tươi 100%',
                'fresh-desc': 'Cam kết hoa tươi nhập khẩu trực tiếp',
                'fast-delivery': 'Giao hàng nhanh',
                'fast-desc': 'Giao hàng trong 2 giờ nội thành HCM',
                'professional-design': 'Thiết kế chuyên nghiệp',
                'design-desc': 'Đội ngũ florist chuyên nghiệp',
                'support-247': 'Hỗ trợ 24/7',
                'support-desc': 'Tư vấn và hỗ trợ mọi lúc',
                
                // Products
                'featured-products': 'Sản phẩm nổi bật',
                'quick-add': 'Thêm nhanh',
                'adding': 'Đang thêm...',
                'added': 'Đã thêm',
                'add-to-cart-success': 'Đã thêm sản phẩm vào giỏ hàng!',
                
                // Cart page
                'cart-title': 'Giỏ hàng của bạn',
                'cart-subtitle': 'sản phẩm trong giỏ hàng',
                'select-all': 'Chọn tất cả sản phẩm',
                'cart-summary': 'Tổng cộng giỏ hàng',
                'unit-price': 'Đơn giá',
                'quantity': 'Số lượng',
                'subtotal': 'Thành tiền',
                'total-quantity': 'Tổng số lượng',
                'temporary-total': 'Tạm tính',
                'final-total': 'Tổng tiền',
                'selected-items': 'sản phẩm được chọn',
                'proceed-checkout': 'Tiến hành thanh toán',
                'continue-shopping': 'Tiếp tục mua sắm',
                'clear-cart': 'Xóa tất cả',
                'shipping-note': 'Phí vận chuyển sẽ được tính ở bước thanh toán',
                'empty-cart-title': 'Giỏ hàng trống',
                'empty-cart-desc': 'Bạn chưa có sản phẩm nào trong giỏ hàng. Hãy khám phá các sản phẩm tuyệt vời của chúng tôi!',
                'explore-products': 'Khám phá sản phẩm',
                
                // Footer
                'language': 'Ngôn ngữ',
                'vietnamese': 'Tiếng Việt',
                'english': 'English'
            },
            en: {
                // Header
                'search-placeholder': '🌸 Search flowers quickly - Birthday, love, wedding...',
                'login': 'Login',
                'register': 'Register',
                'profile': 'Profile',
                'orders': 'My Orders',
                'wishlist': 'Wishlist',
                'settings': 'Settings',
                'logout': 'Logout',
                'customer-role': 'Customer',
                
                // Navigation
                'home': 'Home',
                'categories': 'Categories',
                'products': 'Products',
                'gifts': 'Gifts',
                'occasions': 'Occasions',
                'blog': 'Blog',
                'contact': 'Contact',
                
                // Categories
                'birthday-flowers': 'Birthday Flowers',
                'love-flowers': 'Love Flowers',
                'wedding-flowers': 'Wedding Flowers',
                'opening-flowers': 'Opening Flowers',
                'sympathy-flowers': 'Sympathy Flowers',
                'mother-flowers': 'Mother Flowers',
                
                // Home page
                'hero-title': 'Bringing Flowers to Your Joy',
                'hero-subtitle': 'Discover our premium collection of fresh flowers carefully curated to bring the most special moments in life',
                'explore-products': 'Explore Products',
                'view-catalog': 'View Catalog',
                
                // Why StarShop
                'why-starshop': 'Why Choose StarShop?',
                'fresh-flowers': '100% Fresh Flowers',
                'fresh-desc': 'Direct import fresh flowers guarantee',
                'fast-delivery': 'Fast Delivery',
                'fast-desc': '2-hour delivery in Ho Chi Minh City',
                'professional-design': 'Professional Design',
                'design-desc': 'Professional florist team',
                'support-247': '24/7 Support',
                'support-desc': 'Consultation and support anytime',
                
                // Products
                'featured-products': 'Featured Products',
                'quick-add': 'Quick Add',
                'adding': 'Adding...',
                'added': 'Added',
                'add-to-cart-success': 'Product added to cart!',
                
                // Cart page
                'cart-title': 'Your Cart',
                'cart-subtitle': 'items in cart',
                'select-all': 'Select all products',
                'cart-summary': 'Cart Summary',
                'unit-price': 'Unit Price',
                'quantity': 'Quantity',
                'subtotal': 'Subtotal',
                'total-quantity': 'Total Quantity',
                'temporary-total': 'Subtotal',
                'final-total': 'Total',
                'selected-items': 'items selected',
                'proceed-checkout': 'Proceed to Checkout',
                'continue-shopping': 'Continue Shopping',
                'clear-cart': 'Clear All',
                'shipping-note': 'Shipping fee will be calculated at checkout',
                'empty-cart-title': 'Empty Cart',
                'empty-cart-desc': 'You have no items in your cart. Explore our amazing products!',
                'explore-products': 'Explore Products',
                
                // Footer
                'language': 'Language',
                'vietnamese': 'Vietnamese',
                'english': 'English'
            }
        };
    }

    // Update language display in switcher
    updateLanguageDisplay() {
        const languageTexts = document.querySelectorAll('.language-text');
        const languageButtons = document.querySelectorAll('button span.text-sm.font-medium.text-gray-700');
        
        // Update button text (VI/EN)
        languageButtons.forEach(button => {
            if (button.textContent === 'VI' || button.textContent === 'EN') {
                button.textContent = this.currentLanguage.toUpperCase();
            }
        });
        
        languageTexts.forEach(text => {
            text.textContent = this.currentLanguage.toUpperCase();
        });

        // Update active state
        document.querySelectorAll('.language-option').forEach(option => {
            option.classList.remove('active');
            if (option.dataset.lang === this.currentLanguage) {
                option.classList.add('active');
            }
        });

        document.querySelectorAll('.mobile-language-option').forEach(option => {
            option.classList.remove('active');
            if (option.dataset.lang === this.currentLanguage) {
                option.classList.add('active');
            }
        });
    }

    // Bind click events
    bindEvents() {
        // Desktop language options
        document.querySelectorAll('.language-option').forEach(option => {
            option.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchLanguage(option.dataset.lang);
            });
        });

        // Mobile language options
        document.querySelectorAll('.mobile-language-option').forEach(option => {
            option.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchLanguage(option.dataset.lang);
            });
        });
    }

    // Switch language
    switchLanguage(language) {
        if (language === this.currentLanguage) return;
        
        this.currentLanguage = language;
        localStorage.setItem('starshop-language', language);
        
        this.updateLanguageDisplay();
        this.applyLanguage();
        
        // Show notification
        this.showLanguageChangeNotification();
    }

    // Apply language to page elements
    applyLanguage() {
        const translations = this.translations[this.currentLanguage];
        
        // Update elements with data-translate attribute
        document.querySelectorAll('[data-translate]').forEach(element => {
            const key = element.dataset.translate;
            if (translations[key]) {
                if (element.tagName === 'INPUT' && element.type === 'text') {
                    element.placeholder = translations[key];
                } else {
                    element.textContent = translations[key];
                }
            }
        });

        // Update specific elements by ID or class
        this.updateSpecificElements(translations);
    }

    // Update specific elements that need special handling
    updateSpecificElements(translations) {
        // Search input placeholder
        const searchInputs = document.querySelectorAll('.search-input-hero, .search-input');
        searchInputs.forEach(input => {
            input.placeholder = translations['search-placeholder'];
        });

        // User role text
        const userRoles = document.querySelectorAll('.user-role');
        userRoles.forEach(role => {
            role.textContent = translations['customer-role'];
        });

        // Update page title
        if (document.title.includes('StarShop')) {
            document.title = this.currentLanguage === 'vi' ? 
                'StarShop - Cửa hàng hoa tươi' : 
                'StarShop - Fresh Flower Shop';
        }
    }

    // Show language change notification
    showLanguageChangeNotification() {
        const message = this.currentLanguage === 'vi' ? 
            'Đã chuyển sang Tiếng Việt' : 
            'Switched to English';
        
        // Use existing toast function if available
        if (typeof showToast === 'function') {
            showToast(message, 'success');
        } else {
            // Fallback notification
            const notification = document.createElement('div');
            notification.className = 'language-notification';
            notification.textContent = message;
            notification.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: var(--primary);
                color: white;
                padding: 12px 20px;
                border-radius: 8px;
                z-index: 9999;
                font-size: 14px;
                font-weight: 500;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                transform: translateX(100%);
                transition: transform 0.3s ease;
            `;
            
            document.body.appendChild(notification);
            
            // Animate in
            setTimeout(() => {
                notification.style.transform = 'translateX(0)';
            }, 100);
            
            // Remove after 3 seconds
            setTimeout(() => {
                notification.style.transform = 'translateX(100%)';
                setTimeout(() => {
                    document.body.removeChild(notification);
                }, 300);
            }, 3000);
        }
    }

    // Get current language
    getCurrentLanguage() {
        return this.currentLanguage;
    }

    // Get translation for a key
    translate(key) {
        return this.translations[this.currentLanguage][key] || key;
    }
}

// Initialize language switcher when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.languageSwitcher = new LanguageSwitcher();
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LanguageSwitcher;
}
