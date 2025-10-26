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
                'fresh-daily': 'Hoa tươi mỗi ngày',
                'hero-title-part1': 'Mang',
                'hero-title-part2': 'hoa đến',
                'hero-title-part3': 'niềm vui của bạn',
                'hero-description': 'Khám phá bộ sưu tập hoa tươi tuyệt đẹp tại StarShop. Chúng tôi mang đến những bó hoa tinh tế, được thiết kế đặc biệt cho mọi khoảnh khắc đáng nhớ của bạn.',
                'happy-customers': 'Khách hàng hài lòng',
                'unique-designs': 'Mẫu hoa độc đáo',
                'customer-support': 'Hỗ trợ khách hàng',
                'explore-now': 'Khám phá ngay',
                'view-categories': 'Xem danh mục',
                'rating': '4.9',
                'rating-desc': 'Đánh giá',
                'why-starshop-desc': 'Chúng tôi cam kết mang đến trải nghiệm tuyệt vời nhất',
                'featured-categories': 'Danh mục nổi bật',
                'featured-categories-desc': 'Tìm kiếm bó hoa hoàn hảo cho mọi dịp đặc biệt',
                'explore-collection': 'Khám phá bộ sưu tập',
                'explore': 'Khám phá',
                'view-all-categories': 'Xem tất cả danh mục',
                'featured-products-desc': 'Những bó hoa được yêu thích nhất tại StarShop',
                'out-of-stock': 'HẾT HÀNG',
                'out-of-stock-badge': 'Hết hàng',
                'low-stock': 'Sắp hết',
                'featured': 'Nổi bật',
                'remaining': 'Còn',
                'view-all-products': 'Xem tất cả sản phẩm',
                'special-offer': 'Ưu đãi đặc biệt',
                'discount-20': 'Giảm 20%',
                'first-order': 'cho đơn hàng đầu tiên',
                'promo-desc': 'Chào mừng bạn đến với StarShop! Nhận ngay ưu đãi hấp dẫn khi mua sắm lần đầu.',
                'shop-now': 'Mua sắm ngay',
                'free-shipping': 'Miễn phí giao hàng',
                'hcm-area': 'Nội thành TPHCM',
                'delivery-2h': 'Giao hàng 2 giờ',
                'fast-reliable': 'Nhanh chóng & tin cậy',
                'fresh-flowers-100': 'Hoa tươi 100%',
                'quality-guaranteed': 'Đảm bảo chất lượng',
                'five-star-rating': 'Đánh giá 5 sao',
                'from-customers': 'Từ khách hàng',
                'customer-reviews': 'Đánh giá từ khách hàng',
                'what-customers-say': 'Khách hàng nói gì về chúng tôi',
                'testimonials-desc': 'Hàng nghìn khách hàng đã tin tưởng và hài lòng với dịch vụ của StarShop',
                
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
                
                // Footer
                'footer-description': 'Nơi tình yêu nở hoa - Chúng tôi mang đến những bó hoa tươi tuyệt đẹp cho mọi dịp đặc biệt trong cuộc sống của bạn. Chất lượng cao, giao hàng nhanh chóng.',
                'follow-us': 'Theo dõi chúng tôi:',
                'footer-products': 'Sản phẩm',
                'useful-links': 'Liên kết hữu ích',
                'your-account': 'Tài khoản của bạn',
                'shipping-policy': 'Chính sách giao hàng',
                'returns-refunds': 'Đổi trả & hoàn tiền',
                'flower-care': 'Hướng dẫn chăm sóc hoa',
                'faq': 'Câu hỏi thường gặp',
                'contact-connect': 'Liên hệ & Kết nối',
                'business-hours': 'Giờ làm việc:',
                'weekdays': 'Thứ 2 - Thứ 6',
                'weekend': 'Thứ 7 - Chủ nhật',
                'copyright': 'Bản quyền thuộc về',
                'privacy-policy': 'Chính sách bảo mật',
                'terms-of-use': 'Điều khoản sử dụng',
                
                // Products
                'featured-products': 'Sản phẩm nổi bật',
                'quick-add': 'Thêm nhanh',
                'adding': 'Đang thêm...',
                'added': 'Đã thêm',
                'add-to-cart': 'Thêm',
                'add-to-cart-success': 'Đã thêm sản phẩm vào giỏ hàng!',
                'add-to-cart-error': 'Không thể thêm sản phẩm vào giỏ hàng',
                
                // Categories page
                'categories-title': 'Khám Phá Thế Giới Hoa',
                'categories-description': 'Từ những đóa hồng lãng mạn đến cúc họa mi tinh khôi, hãy tìm nguồn cảm hứng cho mọi khoảnh khắc đặc biệt.',
                'no-categories-found': 'Hiện tại chưa có danh mục sản phẩm nào được tìm thấy.',
                
                // Products page
                'search-results': 'Tìm kiếm',
                'all-products': 'Tất cả sản phẩm',
                'found': 'Tìm thấy',
                'products-match': 'sản phẩm phù hợp',
                'has': 'Có',
                'products-in-category': 'sản phẩm trong danh mục này',
                'fresh-flowers-at-starshop': 'sản phẩm hoa tươi đẹp tại StarShop',
                'category': 'Danh mục',
                'all-categories': 'Tất cả danh mục',
                'search-by-name': 'Tìm kiếm theo tên',
                'enter-product-name': 'Nhập tên sản phẩm...',
                'sort': 'Sắp xếp',
                'newest': 'Mới nhất',
                'oldest': 'Cũ nhất',
                'name-az': 'Tên A-Z',
                'price-low-high': 'Giá thấp → cao',
                'price-high-low': 'Giá cao → thấp',
                'filter': 'Lọc',
                'clear-filter': 'Xóa bộ lọc',
                'showing': 'Hiển thị',
                'of-total': 'trong tổng số',
                'products': 'sản phẩm',
                'no-products-found': 'Không tìm thấy sản phẩm nào',
                'no-match-keyword': 'Không có sản phẩm nào phù hợp với từ khóa',
                'try-different-keyword': 'Hãy thử tìm kiếm với từ khóa khác',
                'no-products-yet': 'Hiện tại chưa có sản phẩm nào. Vui lòng quay lại sau.',
                'add-to-wishlist': 'Thêm vào yêu thích',
                'new': 'Mới',
                'reviews': 'đánh giá',
                'add-to-cart-btn': 'Thêm vào giỏ',
                'login-to-buy': 'Đăng nhập để mua',
                'product-pagination': 'Phân trang sản phẩm',
                'previous-page': 'Trang trước',
                'next-page': 'Trang sau',
                'page': 'Trang',
                
                // Product Detail page
                'product-not-found-title': '404 - Không tìm thấy sản phẩm!',
                'product-not-found-desc': 'Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.',
                'back-to-products': 'Về trang danh sách sản phẩm',
                'product-description': 'Mô tả sản phẩm',
                'buy-now': 'Mua ngay',
                'free-shipping': 'Giao hàng miễn phí',
                'free-shipping-desc': 'Miễn phí giao hàng nội thành TP.HCM',
                'fast-delivery': 'Giao hàng nhanh',
                'delivery-2-4h': 'Giao trong 2-4 giờ',
                'fresh-flowers-100': 'Hoa tươi 100%',
                'fresh-quality-guarantee': 'Cam kết hoa tươi, chất lượng cao',
                'detailed-info': 'Thông tin chi tiết',
                'learn-more-product': 'Tìm hiểu thêm về sản phẩm và cách chăm sóc',
                'detailed-description': 'Mô tả chi tiết',
                'reviews-tab': 'Đánh giá',
                'care-guide': 'Hướng dẫn chăm sóc',
                'full-product-info': 'Thông tin đầy đủ về sản phẩm',
                'customer-reviews': 'Đánh giá từ khách hàng',
                'customer-experience': 'Chia sẻ trải nghiệm của khách hàng về sản phẩm',
                
                // Vouchers page
                'vouchers-title': 'Mã Giảm Giá Đặc Biệt',
                'vouchers-subtitle': 'Chọn mã ưu đãi phù hợp nhất cho đơn hàng của bạn',
                'loading-vouchers': 'Đang tải danh sách ưu đãi...',
                'error-loading-data': 'Lỗi tải dữ liệu',
                'cannot-connect-api': 'Không thể kết nối tới API Voucher',
                'try-again': 'Thử lại',
                'discount-percent': 'Giảm %',
                'discount-amount': 'Giảm tiền',
                'times': 'lần',
                'unlimited': 'Không giới hạn',
                'code': 'Mã',
                'min-order': 'Đơn tối thiểu',
                'max-discount': 'Giảm tối đa',
                'expiry-date': 'HSD',
                'copy-code': 'Sao chép mã',
                'copied': 'Đã sao chép!',
                'no-vouchers': 'Hiện tại chưa có mã giảm giá nào',
                'check-back-later': 'Vui lòng quay lại sau hoặc liên hệ hỗ trợ',
                
                // Blog page
                'blog-title': 'Blog StarShop',
                'blog-subtitle': 'Khám phá nghệ thuật cắm hoa, chăm sóc hoa tươi và xu hướng trang trí mới nhất',
                'search-articles': 'Tìm kiếm bài viết...',
                'search': 'Tìm',
                'all': 'Tất cả',
                'flower-care': 'Chăm sóc hoa',
                'flower-arrangement': 'Nghệ thuật cắm hoa',
                'trends': 'Xu hướng',
                'special-occasions': 'Dịp đặc biệt',
                'tips': 'Mẹo hay',
                'featured': 'Nổi bật',
                'read-more': 'Đọc thêm',
                'art': 'Nghệ thuật',
                'min-read': 'phút đọc',
                'view-more': 'Xem thêm',
                'care': 'Chăm sóc',
                'knowledge': 'Kiến thức',
                'newsletter-title': 'Đăng ký nhận bản tin',
                'newsletter-desc': 'Nhận những bài viết mới nhất, mẹo chăm sóc hoa và ưu đãi đặc biệt qua email',
                'subscribe': 'Đăng ký',
                
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
                'nav-home': 'Home',
                'nav-categories': 'Flower Categories',
                'nav-products': 'Products',
                'nav-voucher': 'Voucher',
                'nav-blog': 'Blog',
                'no-categories': 'No categories available',
                'search': 'Search',
                'search-placeholder': 'Enter flower name, occasion, or keyword...',
                'search-flowers': 'Search flowers...',
                'close-search': 'Close search',
                'login': 'Login',
                'register': 'Register',
                'profile': 'Profile',
                'orders': 'My Orders',
                'logout': 'Logout',
                'cart': 'Cart',
                
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
                'fresh-daily': 'Fresh Daily Flowers',
                'hero-title-part1': 'Bringing',
                'hero-title-part2': 'flowers to',
                'hero-title-part3': 'your joy',
                'hero-description': 'Discover our beautiful collection of fresh flowers at StarShop. We bring you exquisite bouquets, specially designed for every memorable moment in your life.',
                'happy-customers': 'Happy Customers',
                'unique-designs': 'Unique Designs',
                'customer-support': 'Customer Support',
                'explore-now': 'Explore Now',
                'view-categories': 'View Categories',
                'rating': '4.9',
                'rating-desc': 'Rating',
                'why-starshop-desc': 'We are committed to bringing you the best experience',
                'featured-categories': 'Featured Categories',
                'featured-categories-desc': 'Find the perfect bouquet for every special occasion',
                'explore-collection': 'Explore Collection',
                'explore': 'Explore',
                'view-all-categories': 'View All Categories',
                'featured-products-desc': 'Most loved bouquets at StarShop',
                'out-of-stock': 'OUT OF STOCK',
                'out-of-stock-badge': 'Out of Stock',
                'low-stock': 'Low Stock',
                'featured': 'Featured',
                'remaining': 'Remaining',
                'view-all-products': 'View All Products',
                'special-offer': 'Special Offer',
                'discount-20': '20% Off',
                'first-order': 'for your first order',
                'promo-desc': 'Welcome to StarShop! Get amazing deals on your first purchase.',
                'shop-now': 'Shop Now',
                'free-shipping': 'Free Shipping',
                'hcm-area': 'Ho Chi Minh City Area',
                'delivery-2h': '2-Hour Delivery',
                'fast-reliable': 'Fast & Reliable',
                'fresh-flowers-100': '100% Fresh Flowers',
                'quality-guaranteed': 'Quality Guaranteed',
                'five-star-rating': '5-Star Rating',
                'from-customers': 'From Customers',
                'customer-reviews': 'Customer Reviews',
                'what-customers-say': 'What Our Customers Say',
                'testimonials-desc': 'Thousands of customers have trusted and been satisfied with StarShop services',
                
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
                
                // Footer
                'footer-description': 'Where love blooms - We bring you beautiful fresh flowers for every special occasion in your life. High quality, fast delivery.',
                'follow-us': 'Follow us:',
                'footer-products': 'Products',
                'useful-links': 'Useful Links',
                'your-account': 'Your Account',
                'shipping-policy': 'Shipping Policy',
                'returns-refunds': 'Returns & Refunds',
                'flower-care': 'Flower Care Guide',
                'faq': 'FAQ',
                'contact-connect': 'Contact & Connect',
                'business-hours': 'Business Hours:',
                'weekdays': 'Mon - Fri',
                'weekend': 'Sat - Sun',
                'copyright': 'Copyright belongs to',
                'privacy-policy': 'Privacy Policy',
                'terms-of-use': 'Terms of Use',
                
                // Products
                'featured-products': 'Featured Products',
                'quick-add': 'Quick Add',
                'adding': 'Adding...',
                'added': 'Added',
                'add-to-cart': 'Add',
                'add-to-cart-success': 'Product added to cart!',
                'add-to-cart-error': 'Cannot add product to cart',
                
                // Categories page
                'categories-title': 'Discover the World of Flowers',
                'categories-description': 'From romantic roses to delicate daisies, find inspiration for every special moment.',
                'no-categories-found': 'No product categories found at the moment.',
                
                // Products page
                'search-results': 'Search Results',
                'all-products': 'All Products',
                'found': 'Found',
                'products-match': 'matching products',
                'has': 'There are',
                'products-in-category': 'products in this category',
                'fresh-flowers-at-starshop': 'beautiful fresh flower products at StarShop',
                'category': 'Category',
                'all-categories': 'All Categories',
                'search-by-name': 'Search by name',
                'enter-product-name': 'Enter product name...',
                'sort': 'Sort',
                'newest': 'Newest',
                'oldest': 'Oldest',
                'name-az': 'Name A-Z',
                'price-low-high': 'Price Low → High',
                'price-high-low': 'Price High → Low',
                'filter': 'Filter',
                'clear-filter': 'Clear Filter',
                'showing': 'Showing',
                'of-total': 'of',
                'products': 'products',
                'no-products-found': 'No products found',
                'no-match-keyword': 'No products match the keyword',
                'try-different-keyword': 'Try searching with a different keyword',
                'no-products-yet': 'No products available at the moment. Please check back later.',
                'add-to-wishlist': 'Add to Wishlist',
                'new': 'New',
                'reviews': 'reviews',
                'add-to-cart-btn': 'Add to Cart',
                'login-to-buy': 'Login to Buy',
                'product-pagination': 'Product Pagination',
                'previous-page': 'Previous Page',
                'next-page': 'Next Page',
                'page': 'Page',
                
                // Product Detail page
                'product-not-found-title': '404 - Product Not Found!',
                'product-not-found-desc': 'The product you are looking for does not exist or has been deleted.',
                'back-to-products': 'Back to Product List',
                'product-description': 'Product Description',
                'buy-now': 'Buy Now',
                'free-shipping': 'Free Shipping',
                'free-shipping-desc': 'Free shipping in Ho Chi Minh City',
                'fast-delivery': 'Fast Delivery',
                'delivery-2-4h': 'Delivery in 2-4 hours',
                'fresh-flowers-100': '100% Fresh Flowers',
                'fresh-quality-guarantee': 'Guaranteed fresh, high quality flowers',
                'detailed-info': 'Detailed Information',
                'learn-more-product': 'Learn more about the product and care instructions',
                'detailed-description': 'Detailed Description',
                'reviews-tab': 'Reviews',
                'care-guide': 'Care Guide',
                'full-product-info': 'Complete product information',
                'customer-reviews': 'Customer Reviews',
                'customer-experience': 'Share customer experiences about the product',
                
                // Vouchers page
                'vouchers-title': 'Special Discount Codes',
                'vouchers-subtitle': 'Choose the best deal for your order',
                'loading-vouchers': 'Loading vouchers...',
                'error-loading-data': 'Error loading data',
                'cannot-connect-api': 'Cannot connect to Voucher API',
                'try-again': 'Try Again',
                'discount-percent': 'Discount %',
                'discount-amount': 'Discount',
                'times': 'times',
                'unlimited': 'Unlimited',
                'code': 'Code',
                'min-order': 'Min Order',
                'max-discount': 'Max Discount',
                'expiry-date': 'Expiry',
                'copy-code': 'Copy Code',
                'copied': 'Copied!',
                'no-vouchers': 'No vouchers available at the moment',
                'check-back-later': 'Please check back later or contact support',
                
                // Blog page
                'blog-title': 'StarShop Blog',
                'blog-subtitle': 'Discover the art of flower arrangement, fresh flower care and the latest decoration trends',
                'search-articles': 'Search articles...',
                'search': 'Search',
                'all': 'All',
                'flower-care': 'Flower Care',
                'flower-arrangement': 'Flower Arrangement',
                'trends': 'Trends',
                'special-occasions': 'Special Occasions',
                'tips': 'Tips',
                'featured': 'Featured',
                'read-more': 'Read More',
                'art': 'Art',
                'min-read': 'min read',
                'view-more': 'View More',
                'care': 'Care',
                'knowledge': 'Knowledge',
                'newsletter-title': 'Subscribe to Newsletter',
                'newsletter-desc': 'Get the latest articles, flower care tips and special offers via email',
                'subscribe': 'Subscribe',
                
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
        // Update language display button (VI/EN)
        const languageDisplays = document.querySelectorAll('.language-display');
        languageDisplays.forEach(display => {
            display.textContent = this.currentLanguage.toUpperCase();
        });

        // Update active state in dropdown
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

        // Update elements with data-translate-placeholder attribute
        document.querySelectorAll('[data-translate-placeholder]').forEach(element => {
            const key = element.dataset.translatePlaceholder;
            if (translations[key]) {
                element.placeholder = translations[key];
            }
        });

        // Update elements with data-translate-title attribute
        document.querySelectorAll('[data-translate-title]').forEach(element => {
            const key = element.dataset.translateTitle;
            if (translations[key]) {
                element.title = translations[key];
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
