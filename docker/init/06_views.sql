-- =====================================================
-- PHASE 2: VIEWS & MATERIALIZED VIEWS
-- File: 06_views.sql
-- Mục đích: Tạo views để tối ưu hóa truy vấn dữ liệu
-- =====================================================

-- =====================================================
-- 1. VIEWS: Cart và User Summary
-- =====================================================

-- View: Cart Summary với thông tin chi tiết
CREATE OR REPLACE VIEW cart_summary AS
SELECT 
    c.id as cart_id,
    c.user_id,
    CONCAT(u.first_name, ' ', u.last_name) as user_name,
    u.email as user_email,
    c.total_amount,
    COUNT(ci.id) as item_count,
    SUM(ci.quantity) as total_quantity,
    c.created_at as cart_created_at,
    c.updated_at as cart_updated_at,
    -- Thông tin sản phẩm trong giỏ
    STRING_AGG(
        CONCAT(p.name, ' (x', ci.quantity, ')'), 
        ', ' ORDER BY p.name
    ) as products_in_cart
FROM carts c
JOIN users u ON c.user_id = u.id
LEFT JOIN cartitems ci ON c.id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.id
GROUP BY c.id, c.user_id, u.first_name, u.last_name, u.email, 
         c.total_amount, c.created_at, c.updated_at;

-- View: User Profile Summary
CREATE OR REPLACE VIEW user_profile_summary AS
SELECT 
    u.id as user_id,
    CONCAT(u.first_name, ' ', u.last_name) as full_name,
    u.email,
    u.phone,
    u.avatar,
    u.role,
    u.created_at as member_since,
    -- Thống kê đơn hàng
    COUNT(DISTINCT o.id) as total_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.id END) as completed_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.id END) as pending_orders,
    COALESCE(SUM(CASE WHEN o.status = 'delivered' THEN o.final_amount END), 0) as total_spent,
    -- Thống kê giỏ hàng
    COALESCE(c.total_amount, 0) as cart_total,
    COALESCE(c.item_count, 0) as cart_items,
    -- Thống kê wishlist
    COUNT(DISTINCT f.id) as wishlist_count,
    -- Thống kê reviews
    COUNT(DISTINCT r.id) as review_count,
    COALESCE(AVG(r.rating), 0) as average_rating_given
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
LEFT JOIN cart_summary c ON u.id = c.user_id
LEFT JOIN follows f ON u.id = f.user_id
LEFT JOIN reviews r ON u.id = r.user_id
GROUP BY u.id, u.first_name, u.last_name, u.email, u.phone, u.avatar, 
         u.role, u.created_at, c.total_amount, c.item_count;

-- =====================================================
-- 2. VIEWS: Product và Order Summary
-- =====================================================

-- View: Product Summary với thống kê
CREATE OR REPLACE VIEW product_summary AS
SELECT 
    p.id as product_id,
    p.name,
    p.description,
    p.price,
    p.stock_quantity,
    p.status,
    p.image,
    p.created_at as product_created_at,
    -- Thống kê bán hàng
    COUNT(DISTINCT oi.order_id) as total_orders,
    COALESCE(SUM(oi.quantity), 0) as total_sold,
    COALESCE(SUM(oi.quantity * oi.price), 0) as total_revenue,
    -- Thống kê reviews
    COUNT(DISTINCT r.id) as review_count,
    COALESCE(AVG(r.rating), 0) as average_rating,
    -- Thống kê wishlist
    COUNT(DISTINCT f.id) as wishlist_count,
    -- Thống kê cart
    COUNT(DISTINCT ci.cart_id) as in_cart_count,
    SUM(ci.quantity) as cart_quantity
FROM products p
LEFT JOIN orderitems oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'delivered'
LEFT JOIN reviews r ON p.id = r.product_id
LEFT JOIN follows f ON p.id = f.product_id
LEFT JOIN cartitems ci ON p.id = ci.product_id
GROUP BY p.id, p.name, p.description, p.price, p.stock_quantity, 
         p.status, p.image, p.created_at;

-- View: Order Summary với thông tin chi tiết
CREATE OR REPLACE VIEW order_summary AS
SELECT 
    o.id as order_id,
    o.user_id,
    CONCAT(u.first_name, ' ', u.last_name) as customer_name,
    u.email as customer_email,
    u.phone as customer_phone,
    o.status,
    o.order_date,
    o.total_amount,
    o.discount_amount,
    o.shipping_fee,
    o.final_amount,
    o.notes,
    -- Thống kê sản phẩm
    COUNT(oi.id) as item_count,
    SUM(oi.quantity) as total_quantity,
    -- Thông tin địa chỉ
    a.full_address,
    a.phone as delivery_phone,
    -- Thông tin vận chuyển
    du.name as delivery_unit_name,
    du.fee_type as delivery_fee_type,
    du.fee_value as delivery_fee_value
FROM orders o
JOIN users u ON o.user_id = u.id
LEFT JOIN orderitems oi ON o.id = oi.order_id
LEFT JOIN addresses a ON o.address_id = a.id
LEFT JOIN deliveryunits du ON o.delivery_unit_id = du.id
GROUP BY o.id, o.user_id, u.first_name, u.last_name, u.email, u.phone,
         o.status, o.order_date, o.total_amount, o.discount_amount, 
         o.shipping_fee, o.final_amount, o.notes, a.full_address, 
         a.phone, du.name, du.fee_type, du.fee_value;

-- =====================================================
-- 3. VIEWS: Business Analytics
-- =====================================================

-- View: Daily Sales Summary
CREATE OR REPLACE VIEW daily_sales_summary AS
SELECT 
    DATE(o.order_date) as sale_date,
    COUNT(DISTINCT o.id) as total_orders,
    COUNT(DISTINCT o.user_id) as unique_customers,
    SUM(o.final_amount) as total_revenue,
    AVG(o.final_amount) as average_order_value,
    SUM(o.discount_amount) as total_discounts,
    SUM(o.shipping_fee) as total_shipping_fees,
    -- Thống kê theo trạng thái
    COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.id END) as delivered_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'pending' THEN o.id END) as pending_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'cancelled' THEN o.id END) as cancelled_orders
FROM orders o
GROUP BY DATE(o.order_date)
ORDER BY sale_date DESC;

-- View: Product Performance
CREATE OR REPLACE VIEW product_performance AS
SELECT 
    p.id as product_id,
    p.name,
    p.price,
    p.status,
    -- Thống kê bán hàng 30 ngày gần nhất
    COUNT(DISTINCT CASE 
        WHEN o.order_date >= CURRENT_DATE - INTERVAL '30 days' 
        AND o.status = 'delivered' 
        THEN oi.order_id 
    END) as orders_last_30_days,
    COALESCE(SUM(CASE 
        WHEN o.order_date >= CURRENT_DATE - INTERVAL '30 days' 
        AND o.status = 'delivered' 
        THEN oi.quantity 
    END), 0) as sold_last_30_days,
    COALESCE(SUM(CASE 
        WHEN o.order_date >= CURRENT_DATE - INTERVAL '30 days' 
        AND o.status = 'delivered' 
        THEN oi.quantity * oi.price 
    END), 0) as revenue_last_30_days,
    -- Thống kê tổng
    COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN oi.order_id END) as total_orders,
    COALESCE(SUM(CASE WHEN o.status = 'delivered' THEN oi.quantity END), 0) as total_sold,
    COALESCE(SUM(CASE WHEN o.status = 'delivered' THEN oi.quantity * oi.price END), 0) as total_revenue,
    -- Thống kê reviews
    COUNT(DISTINCT r.id) as review_count,
    COALESCE(AVG(r.rating), 0) as average_rating,
    -- Thống kê wishlist
    COUNT(DISTINCT f.id) as wishlist_count
FROM products p
LEFT JOIN orderitems oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id
LEFT JOIN reviews r ON p.id = r.product_id
LEFT JOIN follows f ON p.id = f.product_id
GROUP BY p.id, p.name, p.price, p.status
ORDER BY revenue_last_30_days DESC;

-- =====================================================
-- 4. MATERIALIZED VIEWS: Performance Optimization
-- =====================================================

-- Materialized View: Top Selling Products (cập nhật hàng ngày)
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_top_selling_products AS
SELECT 
    p.id as product_id,
    p.name,
    p.price,
    p.status,
    COUNT(DISTINCT oi.order_id) as total_orders,
    COALESCE(SUM(oi.quantity), 0) as total_sold,
    COALESCE(SUM(oi.quantity * oi.price), 0) as total_revenue,
    COUNT(DISTINCT r.id) as review_count,
    COALESCE(AVG(r.rating), 0) as average_rating,
    COUNT(DISTINCT f.id) as wishlist_count,
    CURRENT_DATE as last_updated
FROM products p
LEFT JOIN orderitems oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'delivered'
LEFT JOIN reviews r ON p.id = r.product_id
LEFT JOIN follows f ON p.id = f.product_id
GROUP BY p.id, p.name, p.price, p.status
ORDER BY total_sold DESC, total_revenue DESC;

-- Tạo index cho materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_top_selling_products_id 
ON mv_top_selling_products (product_id);

CREATE INDEX IF NOT EXISTS idx_mv_top_selling_products_sold 
ON mv_top_selling_products (total_sold DESC);

CREATE INDEX IF NOT EXISTS idx_mv_top_selling_products_revenue 
ON mv_top_selling_products (total_revenue DESC);

-- Materialized View: Monthly Sales Summary
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_monthly_sales AS
SELECT 
    DATE_TRUNC('month', o.order_date) as month,
    COUNT(DISTINCT o.id) as total_orders,
    COUNT(DISTINCT o.user_id) as unique_customers,
    SUM(o.final_amount) as total_revenue,
    AVG(o.final_amount) as average_order_value,
    SUM(o.discount_amount) as total_discounts,
    SUM(o.shipping_fee) as total_shipping_fees,
    COUNT(DISTINCT CASE WHEN o.status = 'delivered' THEN o.id END) as delivered_orders,
    COUNT(DISTINCT CASE WHEN o.status = 'cancelled' THEN o.id END) as cancelled_orders,
    CURRENT_DATE as last_updated
FROM orders o
GROUP BY DATE_TRUNC('month', o.order_date)
ORDER BY month DESC;

-- Tạo index cho materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_monthly_sales_month 
ON mv_monthly_sales (month);

-- =====================================================
-- 5. FUNCTIONS: Refresh Materialized Views
-- =====================================================

-- Function để refresh tất cả materialized views
CREATE OR REPLACE FUNCTION refresh_all_materialized_views()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_top_selling_products;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_monthly_sales;
    
    RAISE NOTICE 'Tất cả materialized views đã được refresh thành công';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VERIFICATION: Kiểm tra views đã được tạo
-- =====================================================

-- Kiểm tra các view đã được tạo
SELECT 
    table_name as view_name,
    table_type
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_type IN ('VIEW', 'MATERIALIZED VIEW')
ORDER BY table_type, table_name;

-- Kiểm tra các function đã được tạo
SELECT 
    routine_name as function_name,
    routine_type
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name = 'refresh_all_materialized_views';

-- =====================================================
-- TEST VIEWS: Kiểm tra hoạt động
-- =====================================================

-- Test 1: Cart Summary
-- SELECT * FROM cart_summary WHERE user_id = 1;

-- Test 2: User Profile Summary
-- SELECT * FROM user_profile_summary WHERE user_id = 1;

-- Test 3: Product Summary
-- SELECT * FROM product_summary WHERE product_id = 1;

-- Test 4: Order Summary
-- SELECT * FROM order_summary WHERE order_id = 1;

-- Test 5: Daily Sales Summary
-- SELECT * FROM daily_sales_summary ORDER BY sale_date DESC LIMIT 7;

-- Test 6: Product Performance
-- SELECT * FROM product_performance ORDER BY revenue_last_30_days DESC LIMIT 10;

-- Test 7: Materialized Views
-- SELECT * FROM mv_top_selling_products LIMIT 10;
-- SELECT * FROM mv_monthly_sales ORDER BY month DESC LIMIT 12;

COMMIT;
