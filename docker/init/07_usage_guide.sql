-- =====================================================
-- PHASE 2: USAGE GUIDE & EXAMPLES
-- File: 07_usage_guide.sql
-- Mục đích: Hướng dẫn sử dụng triggers, functions, views và procedures
-- =====================================================

-- =====================================================
-- 1. HƯỚNG DẪN SỬ DỤNG TRIGGERS
-- =====================================================

/*
=== TRIGGERS TỰ ĐỘNG ===

1. Cart Total Update Triggers:
   - Tự động cập nhật total_amount khi CartItem thay đổi
   - Trigger: trigger_update_cart_total_insert/update/delete
   - Function: update_cart_total()

2. Product Status Update Trigger:
   - Tự động cập nhật status khi stock_quantity thay đổi
   - Trigger: trigger_update_product_status
   - Function: update_product_status()

3. Cart Update on Price Change Trigger:
   - Tự động cập nhật Cart total khi Product price thay đổi
   - Trigger: trigger_update_carts_on_price_change
   - Function: update_carts_with_product()

4. Stock Update on Order Trigger:
   - Tự động giảm stock khi OrderItem được tạo
   - Trigger: trigger_update_stock_on_order
   - Function: update_product_stock_on_order()

5. Stock Restore on Cancel Trigger:
   - Tự động hoàn lại stock khi Order bị hủy
   - Trigger: trigger_restore_stock_on_cancel
   - Function: restore_product_stock_on_cancel()
*/

-- =====================================================
-- 2. HƯỚNG DẪN SỬ DỤNG FUNCTIONS
-- =====================================================

/*
=== DISCOUNT FUNCTIONS ===

1. calculate_voucher_discount(voucher_code, order_amount, user_id):
   - Tính discount dựa trên voucher
   - Trả về: Số tiền được giảm
   - Ví dụ: SELECT calculate_voucher_discount('WELCOME10', 1000000, 1);

2. calculate_quantity_discount(product_id, quantity):
   - Tính discount dựa trên số lượng sản phẩm
   - Trả về: Số tiền được giảm
   - Ví dụ: SELECT calculate_quantity_discount(1, 5);

3. calculate_total_discount(order_id, voucher_code):
   - Tính tổng discount cho đơn hàng
   - Trả về: Tổng số tiền được giảm
   - Ví dụ: SELECT calculate_total_discount(1, 'WELCOME10');

=== ORDER FUNCTIONS ===

4. check_stock_availability(order_id):
   - Kiểm tra stock có đủ cho đơn hàng không
   - Trả về: TRUE/FALSE
   - Ví dụ: SELECT check_stock_availability(1);

5. calculate_shipping_fee(delivery_unit_id, order_amount, distance_km):
   - Tính phí vận chuyển
   - Trả về: Số tiền phí vận chuyển
   - Ví dụ: SELECT calculate_shipping_fee(1, 300000, 10);

=== UTILITY FUNCTIONS ===

6. get_order_summary(order_id):
   - Lấy thông tin tổng hợp đơn hàng
   - Trả về: Bảng với thông tin chi tiết
   - Ví dụ: SELECT * FROM get_order_summary(1);

7. get_top_selling_products(limit):
   - Lấy top sản phẩm bán chạy
   - Trả về: Bảng với thông tin sản phẩm
   - Ví dụ: SELECT * FROM get_top_selling_products(10);
*/

-- =====================================================
-- 3. HƯỚNG DẪN SỬ DỤNG PROCEDURES
-- =====================================================

/*
=== ORDER PROCEDURES ===

1. process_order(order_id, voucher_code, delivery_unit_id, distance_km):
   - Xử lý đơn hàng hoàn chỉnh
   - Cập nhật stock, tính discount, tạo transaction
   - Ví dụ: CALL process_order(1, 'WELCOME10', 1, 10);

2. cancel_order(order_id, reason):
   - Hủy đơn hàng và hoàn lại stock
   - Tạo transaction hoàn tiền
   - Ví dụ: CALL cancel_order(1, 'Khách hàng yêu cầu hủy');
*/

-- =====================================================
-- 4. HƯỚNG DẪN SỬ DỤNG VIEWS
-- =====================================================

/*
=== SUMMARY VIEWS ===

1. cart_summary:
   - Thông tin tổng hợp giỏ hàng
   - Ví dụ: SELECT * FROM cart_summary WHERE user_id = 1;

2. user_profile_summary:
   - Thông tin tổng hợp người dùng
   - Ví dụ: SELECT * FROM user_profile_summary WHERE user_id = 1;

3. product_summary:
   - Thông tin tổng hợp sản phẩm
   - Ví dụ: SELECT * FROM product_summary WHERE product_id = 1;

4. order_summary:
   - Thông tin tổng hợp đơn hàng
   - Ví dụ: SELECT * FROM order_summary WHERE order_id = 1;

=== ANALYTICS VIEWS ===

5. daily_sales_summary:
   - Thống kê bán hàng theo ngày
   - Ví dụ: SELECT * FROM daily_sales_summary ORDER BY sale_date DESC LIMIT 7;

6. product_performance:
   - Hiệu suất sản phẩm
   - Ví dụ: SELECT * FROM product_performance ORDER BY revenue_last_30_days DESC LIMIT 10;

=== MATERIALIZED VIEWS ===

7. mv_top_selling_products:
   - Top sản phẩm bán chạy (cập nhật hàng ngày)
   - Ví dụ: SELECT * FROM mv_top_selling_products LIMIT 10;

8. mv_monthly_sales:
   - Thống kê bán hàng theo tháng
   - Ví dụ: SELECT * FROM mv_monthly_sales ORDER BY month DESC LIMIT 12;
*/

-- =====================================================
-- 5. VÍ DỤ THỰC TẾ
-- =====================================================

-- === VÍ DỤ 1: Xử lý đơn hàng mới ===
/*
-- Bước 1: Tạo đơn hàng
INSERT INTO orders (user_id, status, order_date, total_amount, final_amount) 
VALUES (1, 'pending', CURRENT_TIMESTAMP, 500000, 500000);

-- Bước 2: Thêm sản phẩm vào đơn hàng
INSERT INTO orderitems (order_id, product_id, quantity, price) 
VALUES (1, 1, 2, 250000);

-- Bước 3: Xử lý đơn hàng với voucher
CALL process_order(1, 'WELCOME10', 1, 5);

-- Bước 4: Kiểm tra kết quả
SELECT * FROM get_order_summary(1);
SELECT * FROM order_summary WHERE order_id = 1;
*/

-- === VÍ DỤ 2: Quản lý giỏ hàng ===
/*
-- Bước 1: Thêm sản phẩm vào giỏ hàng
INSERT INTO cartitems (cart_id, product_id, quantity) 
VALUES (1, 1, 3);

-- Bước 2: Kiểm tra tổng tiền giỏ hàng (tự động cập nhật bởi trigger)
SELECT * FROM cart_summary WHERE cart_id = 1;

-- Bước 3: Cập nhật số lượng
UPDATE cartitems SET quantity = 5 WHERE cart_id = 1 AND product_id = 1;

-- Bước 4: Kiểm tra lại tổng tiền
SELECT * FROM cart_summary WHERE cart_id = 1;
*/

-- === VÍ DỤ 3: Quản lý sản phẩm ===
/*
-- Bước 1: Cập nhật stock sản phẩm
UPDATE products SET stock_quantity = 0 WHERE id = 1;

-- Bước 2: Kiểm tra status (tự động cập nhật bởi trigger)
SELECT id, name, stock_quantity, status FROM products WHERE id = 1;

-- Bước 3: Cập nhật lại stock
UPDATE products SET stock_quantity = 10 WHERE id = 1;

-- Bước 4: Kiểm tra status
SELECT id, name, stock_quantity, status FROM products WHERE id = 1;
*/

-- === VÍ DỤ 4: Phân tích kinh doanh ===
/*
-- Top 10 sản phẩm bán chạy
SELECT * FROM mv_top_selling_products LIMIT 10;

-- Thống kê bán hàng 7 ngày gần nhất
SELECT * FROM daily_sales_summary ORDER BY sale_date DESC LIMIT 7;

-- Hiệu suất sản phẩm
SELECT * FROM product_performance ORDER BY revenue_last_30_days DESC LIMIT 10;

-- Thống kê người dùng
SELECT * FROM user_profile_summary ORDER BY total_spent DESC LIMIT 10;
*/

-- =====================================================
-- 6. MAINTENANCE & OPTIMIZATION
-- =====================================================

-- === REFRESH MATERIALIZED VIEWS ===
/*
-- Refresh tất cả materialized views
SELECT refresh_all_materialized_views();

-- Refresh từng view riêng lẻ
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_top_selling_products;
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_monthly_sales;
*/

-- === MONITORING TRIGGERS ===
/*
-- Kiểm tra triggers đang hoạt động
SELECT 
    trigger_name,
    event_object_table,
    action_timing,
    event_manipulation
FROM information_schema.triggers 
WHERE trigger_schema = 'public'
ORDER BY event_object_table, trigger_name;

-- Kiểm tra functions
SELECT 
    routine_name,
    routine_type,
    data_type
FROM information_schema.routines 
WHERE routine_schema = 'public'
ORDER BY routine_name;
*/

-- === PERFORMANCE MONITORING ===
/*
-- Kiểm tra kích thước materialized views
SELECT 
    schemaname,
    matviewname,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||matviewname)) as size
FROM pg_matviews 
WHERE schemaname = 'public';

-- Kiểm tra index usage
SELECT 
    indexname,
    tablename,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
*/

-- =====================================================
-- 7. TROUBLESHOOTING
-- =====================================================

-- === KIỂM TRA LỖI TRIGGERS ===
/*
-- Kiểm tra log lỗi triggers
SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public';

-- Kiểm tra functions có lỗi không
SELECT 
    routine_name,
    routine_definition
FROM information_schema.routines 
WHERE routine_schema = 'public'
AND routine_definition LIKE '%ERROR%';
*/

-- === KIỂM TRA DỮ LIỆU ===
/*
-- Kiểm tra tính nhất quán dữ liệu
SELECT 
    c.id,
    c.total_amount as cart_total,
    COALESCE(SUM(ci.quantity * p.price), 0) as calculated_total
FROM carts c
LEFT JOIN cartitems ci ON c.id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.id
GROUP BY c.id, c.total_amount
HAVING c.total_amount != COALESCE(SUM(ci.quantity * p.price), 0);

-- Kiểm tra stock consistency
SELECT 
    p.id,
    p.name,
    p.stock_quantity,
    p.status,
    CASE 
        WHEN p.stock_quantity <= 0 AND p.status != 'out_of_stock' THEN 'INCONSISTENT'
        WHEN p.stock_quantity > 0 AND p.status = 'out_of_stock' THEN 'INCONSISTENT'
        ELSE 'OK'
    END as status_check
FROM products p;
*/

-- =====================================================
-- 8. BACKUP & RESTORE
-- =====================================================

-- === BACKUP FUNCTIONS & TRIGGERS ===
/*
-- Backup tất cả functions
pg_dump -h localhost -U username -d database_name --schema-only --no-owner --no-privileges -t 'public.*' > functions_backup.sql

-- Backup materialized views
pg_dump -h localhost -U username -d database_name --data-only --no-owner --no-privileges -t 'public.mv_*' > materialized_views_backup.sql
*/

-- === RESTORE ===
/*
-- Restore functions
psql -h localhost -U username -d database_name < functions_backup.sql

-- Restore materialized views
psql -h localhost -U username -d database_name < materialized_views_backup.sql
*/

COMMIT;
