-- =====================================================
-- PHASE 2: FUNCTIONS & PROCEDURES
-- File: 05_functions.sql
-- Mục đích: Tạo functions tính toán discount và procedures xử lý đơn hàng
-- =====================================================

-- =====================================================
-- 1. FUNCTIONS: Tính toán Discount
-- =====================================================

-- Function tính discount dựa trên Voucher
CREATE OR REPLACE FUNCTION calculate_voucher_discount(
    voucher_code VARCHAR(50),
    order_amount NUMERIC(10,2),
    user_id BIGINT
) RETURNS NUMERIC(10,2) AS $$
DECLARE
    voucher_record RECORD;
    discount_amount NUMERIC(10,2) := 0;
BEGIN
    -- Lấy thông tin voucher
    SELECT * INTO voucher_record
    FROM vouchers 
    WHERE code = voucher_code 
    AND is_active = true
    AND expiry_date > CURRENT_DATE
    AND (max_uses IS NULL OR uses < max_uses);
    
    -- Kiểm tra voucher có tồn tại và hợp lệ
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Kiểm tra minimum order amount
    IF order_amount < voucher_record.minimum_order_amount THEN
        RETURN 0;
    END IF;
    
    -- Tính discount amount
    IF voucher_record.discount_type = 'percentage' THEN
        discount_amount := order_amount * (voucher_record.discount_value / 100);
        -- Giới hạn maximum discount
        IF voucher_record.maximum_discount IS NOT NULL AND discount_amount > voucher_record.maximum_discount THEN
            discount_amount := voucher_record.maximum_discount;
        END IF;
    ELSIF voucher_record.discount_type = 'fixed' THEN
        discount_amount := voucher_record.discount_value;
    END IF;
    
    -- Đảm bảo discount không vượt quá order amount
    IF discount_amount > order_amount THEN
        discount_amount := order_amount;
    END IF;
    
    RETURN discount_amount;
END;
$$ LANGUAGE plpgsql;

-- Function tính discount dựa trên số lượng sản phẩm
CREATE OR REPLACE FUNCTION calculate_quantity_discount(
    product_id BIGINT,
    quantity INTEGER
) RETURNS NUMERIC(10,2) AS $$
DECLARE
    product_record RECORD;
    discount_amount NUMERIC(10,2) := 0;
BEGIN
    -- Lấy thông tin sản phẩm
    SELECT * INTO product_record
    FROM products 
    WHERE id = product_id;
    
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Áp dụng discount theo số lượng
    IF quantity >= 10 THEN
        discount_amount := product_record.price * quantity * 0.15; -- 15% discount cho >= 10 sản phẩm
    ELSIF quantity >= 5 THEN
        discount_amount := product_record.price * quantity * 0.10; -- 10% discount cho >= 5 sản phẩm
    ELSIF quantity >= 3 THEN
        discount_amount := product_record.price * quantity * 0.05; -- 5% discount cho >= 3 sản phẩm
    END IF;
    
    RETURN discount_amount;
END;
$$ LANGUAGE plpgsql;

-- Function tính tổng discount cho đơn hàng
CREATE OR REPLACE FUNCTION calculate_total_discount(
    order_id BIGINT,
    voucher_code VARCHAR(50) DEFAULT NULL
) RETURNS NUMERIC(10,2) AS $$
DECLARE
    order_record RECORD;
    order_item RECORD;
    total_amount NUMERIC(10,2) := 0;
    voucher_discount NUMERIC(10,2) := 0;
    quantity_discount NUMERIC(10,2) := 0;
    total_discount NUMERIC(10,2) := 0;
BEGIN
    -- Lấy thông tin đơn hàng
    SELECT * INTO order_record
    FROM orders 
    WHERE id = order_id;
    
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Tính tổng tiền đơn hàng
    SELECT COALESCE(SUM(oi.quantity * oi.price), 0)
    INTO total_amount
    FROM orderitems oi
    WHERE oi.order_id = order_id;
    
    -- Tính voucher discount
    IF voucher_code IS NOT NULL THEN
        voucher_discount := calculate_voucher_discount(voucher_code, total_amount, order_record.user_id);
    END IF;
    
    -- Tính quantity discount cho từng sản phẩm
    FOR order_item IN 
        SELECT oi.product_id, oi.quantity
        FROM orderitems oi
        WHERE oi.order_id = order_id
    LOOP
        quantity_discount := quantity_discount + calculate_quantity_discount(order_item.product_id, order_item.quantity);
    END LOOP;
    
    -- Tổng discount (không được vượt quá 50% tổng đơn hàng)
    total_discount := voucher_discount + quantity_discount;
    IF total_discount > total_amount * 0.5 THEN
        total_discount := total_amount * 0.5;
    END IF;
    
    RETURN total_discount;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 2. FUNCTIONS: Xử lý đơn hàng
-- =====================================================

-- Function kiểm tra stock availability
CREATE OR REPLACE FUNCTION check_stock_availability(
    order_id BIGINT
) RETURNS BOOLEAN AS $$
DECLARE
    order_item RECORD;
    available_stock INTEGER;
BEGIN
    -- Kiểm tra từng sản phẩm trong đơn hàng
    FOR order_item IN 
        SELECT oi.product_id, oi.quantity, p.name as product_name
        FROM orderitems oi
        JOIN products p ON oi.product_id = p.id
        WHERE oi.order_id = order_id
    LOOP
        -- Lấy stock hiện tại
        SELECT stock_quantity INTO available_stock
        FROM products 
        WHERE id = order_item.product_id;
        
        -- Kiểm tra stock
        IF available_stock IS NULL OR available_stock < order_item.quantity THEN
            RAISE NOTICE 'Không đủ stock cho sản phẩm: % (cần: %, có: %)', 
                order_item.product_name, order_item.quantity, COALESCE(available_stock, 0);
            RETURN FALSE;
        END IF;
    END LOOP;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function tính phí vận chuyển
CREATE OR REPLACE FUNCTION calculate_shipping_fee(
    delivery_unit_id BIGINT,
    order_amount NUMERIC(10,2),
    distance_km INTEGER DEFAULT 0
) RETURNS NUMERIC(10,2) AS $$
DECLARE
    delivery_record RECORD;
    shipping_fee NUMERIC(10,2) := 0;
BEGIN
    -- Lấy thông tin đơn vị vận chuyển
    SELECT * INTO delivery_record
    FROM deliveryunits 
    WHERE id = delivery_unit_id AND is_active = true;
    
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Tính phí vận chuyển
    IF delivery_record.fee_type = 'fixed' THEN
        shipping_fee := delivery_record.fee_value;
    ELSIF delivery_record.fee_type = 'percentage' THEN
        shipping_fee := order_amount * (delivery_record.fee_value / 100);
    ELSIF delivery_record.fee_type = 'distance' THEN
        shipping_fee := delivery_record.fee_value * distance_km;
    END IF;
    
    -- Miễn phí vận chuyển nếu đơn hàng >= 500,000 VND
    IF order_amount >= 500000 THEN
        shipping_fee := 0;
    END IF;
    
    RETURN shipping_fee;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 3. PROCEDURES: Xử lý đơn hàng
-- =====================================================

-- Procedure xử lý đơn hàng hoàn chỉnh
CREATE OR REPLACE PROCEDURE process_order(
    p_order_id BIGINT,
    p_voucher_code VARCHAR(50) DEFAULT NULL,
    p_delivery_unit_id BIGINT DEFAULT NULL,
    p_distance_km INTEGER DEFAULT 0
) AS $$
DECLARE
    order_record RECORD;
    total_amount NUMERIC(10,2) := 0;
    discount_amount NUMERIC(10,2) := 0;
    shipping_fee NUMERIC(10,2) := 0;
    final_amount NUMERIC(10,2) := 0;
    stock_available BOOLEAN;
BEGIN
    -- Lấy thông tin đơn hàng
    SELECT * INTO order_record
    FROM orders 
    WHERE id = p_order_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Đơn hàng không tồn tại: %', p_order_id;
    END IF;
    
    -- Kiểm tra stock availability
    stock_available := check_stock_availability(p_order_id);
    IF NOT stock_available THEN
        RAISE EXCEPTION 'Không đủ stock cho đơn hàng: %', p_order_id;
    END IF;
    
    -- Tính tổng tiền đơn hàng
    SELECT COALESCE(SUM(oi.quantity * oi.price), 0)
    INTO total_amount
    FROM orderitems oi
    WHERE oi.order_id = p_order_id;
    
    -- Tính discount
    discount_amount := calculate_total_discount(p_order_id, p_voucher_code);
    
    -- Tính phí vận chuyển
    IF p_delivery_unit_id IS NOT NULL THEN
        shipping_fee := calculate_shipping_fee(p_delivery_unit_id, total_amount, p_distance_km);
    END IF;
    
    -- Tính tổng cuối cùng
    final_amount := total_amount - discount_amount + shipping_fee;
    
    -- Cập nhật đơn hàng
    UPDATE orders 
    SET 
        total_amount = total_amount,
        discount_amount = discount_amount,
        shipping_fee = shipping_fee,
        final_amount = final_amount,
        status = 'confirmed',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_order_id;
    
    -- Cập nhật voucher usage nếu có
    IF p_voucher_code IS NOT NULL THEN
        UPDATE vouchers 
        SET uses = uses + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE code = p_voucher_code;
    END IF;
    
    -- Cập nhật stock (đã được trigger xử lý)
    -- Tạo transaction record
    INSERT INTO transactions (
        order_id, 
        amount, 
        transaction_type, 
        status, 
        transaction_reference,
        notes
    ) VALUES (
        p_order_id, 
        final_amount, 
        'payment', 
        'pending',
        'TXN-' || p_order_id || '-' || EXTRACT(EPOCH FROM NOW())::BIGINT,
        'Thanh toán cho đơn hàng #' || p_order_id
    );
    
    RAISE NOTICE 'Đơn hàng % đã được xử lý thành công. Tổng tiền: %, Giảm giá: %, Phí vận chuyển: %, Thành tiền: %', 
        p_order_id, total_amount, discount_amount, shipping_fee, final_amount;
        
END;
$$ LANGUAGE plpgsql;

-- Procedure hủy đơn hàng
CREATE OR REPLACE PROCEDURE cancel_order(
    p_order_id BIGINT,
    p_reason TEXT DEFAULT NULL
) AS $$
DECLARE
    order_record RECORD;
BEGIN
    -- Lấy thông tin đơn hàng
    SELECT * INTO order_record
    FROM orders 
    WHERE id = p_order_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Đơn hàng không tồn tại: %', p_order_id;
    END IF;
    
    -- Kiểm tra trạng thái đơn hàng
    IF order_record.status IN ('delivered', 'cancelled') THEN
        RAISE EXCEPTION 'Không thể hủy đơn hàng với trạng thái: %', order_record.status;
    END IF;
    
    -- Cập nhật trạng thái đơn hàng
    UPDATE orders 
    SET 
        status = 'cancelled',
        notes = COALESCE(notes, '') || ' | Hủy: ' || COALESCE(p_reason, 'Không có lý do'),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_order_id;
    
    -- Tạo transaction record cho việc hoàn tiền
    INSERT INTO transactions (
        order_id, 
        amount, 
        transaction_type, 
        status, 
        transaction_reference,
        notes
    ) VALUES (
        p_order_id, 
        -order_record.final_amount, 
        'refund', 
        'pending',
        'REF-' || p_order_id || '-' || EXTRACT(EPOCH FROM NOW())::BIGINT,
        'Hoàn tiền cho đơn hàng bị hủy #' || p_order_id || '. Lý do: ' || COALESCE(p_reason, 'Không có lý do')
    );
    
    RAISE NOTICE 'Đơn hàng % đã được hủy thành công', p_order_id;
    
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 4. FUNCTIONS: Utility Functions
-- =====================================================

-- Function lấy thông tin đơn hàng chi tiết
CREATE OR REPLACE FUNCTION get_order_summary(p_order_id BIGINT)
RETURNS TABLE (
    order_id BIGINT,
    user_name VARCHAR(255),
    total_amount NUMERIC(10,2),
    discount_amount NUMERIC(10,2),
    shipping_fee NUMERIC(10,2),
    final_amount NUMERIC(10,2),
    status VARCHAR(50),
    order_date TIMESTAMP,
    item_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        o.id,
        CONCAT(u.first_name, ' ', u.last_name) as user_name,
        o.total_amount,
        o.discount_amount,
        o.shipping_fee,
        o.final_amount,
        o.status::VARCHAR,
        o.order_date,
        COUNT(oi.id) as item_count
    FROM orders o
    JOIN users u ON o.user_id = u.id
    LEFT JOIN orderitems oi ON o.id = oi.order_id
    WHERE o.id = p_order_id
    GROUP BY o.id, u.first_name, u.last_name, o.total_amount, o.discount_amount, 
             o.shipping_fee, o.final_amount, o.status, o.order_date;
END;
$$ LANGUAGE plpgsql;

-- Function lấy top sản phẩm bán chạy
CREATE OR REPLACE FUNCTION get_top_selling_products(p_limit INTEGER DEFAULT 10)
RETURNS TABLE (
    product_id BIGINT,
    product_name VARCHAR(255),
    total_sold BIGINT,
    total_revenue NUMERIC(10,2),
    average_rating NUMERIC(3,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        COALESCE(SUM(oi.quantity), 0) as total_sold,
        COALESCE(SUM(oi.quantity * oi.price), 0) as total_revenue,
        COALESCE(AVG(r.rating), 0) as average_rating
    FROM products p
    LEFT JOIN orderitems oi ON p.id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'delivered'
    LEFT JOIN reviews r ON p.id = r.product_id
    GROUP BY p.id, p.name
    ORDER BY total_sold DESC, total_revenue DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VERIFICATION: Kiểm tra functions và procedures
-- =====================================================

-- Kiểm tra các function đã được tạo
SELECT 
    routine_name as function_name,
    routine_type,
    data_type as return_type
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name IN (
    'calculate_voucher_discount',
    'calculate_quantity_discount',
    'calculate_total_discount',
    'check_stock_availability',
    'calculate_shipping_fee',
    'get_order_summary',
    'get_top_selling_products'
)
ORDER BY routine_name;

-- Kiểm tra các procedure đã được tạo
SELECT 
    routine_name as procedure_name,
    routine_type
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name IN (
    'process_order',
    'cancel_order'
)
ORDER BY routine_name;

-- =====================================================
-- TEST FUNCTIONS: Kiểm tra hoạt động
-- =====================================================

-- Test 1: Tính voucher discount
-- SELECT calculate_voucher_discount('WELCOME10', 1000000, 1);

-- Test 2: Tính quantity discount
-- SELECT calculate_quantity_discount(1, 5);

-- Test 3: Tính shipping fee
-- SELECT calculate_shipping_fee(1, 300000, 10);

-- Test 4: Lấy order summary
-- SELECT * FROM get_order_summary(1);

-- Test 5: Lấy top selling products
-- SELECT * FROM get_top_selling_products(5);

COMMIT;
