-- =====================================================
-- PHASE 2: TRIGGERS & FUNCTIONS
-- File: 04_triggers.sql
-- Mục đích: Tạo triggers tự động cập nhật Cart total và Product status
-- =====================================================

-- =====================================================
-- 1. TRIGGER: Tự động cập nhật Cart total_amount
-- =====================================================

-- Function để tính tổng tiền của Cart
CREATE OR REPLACE FUNCTION calculate_cart_total(cart_id BIGINT)
RETURNS NUMERIC(10,2) AS $$
DECLARE
    total NUMERIC(10,2) := 0;
BEGIN
    SELECT COALESCE(SUM(ci.quantity * p.price), 0)
    INTO total
    FROM cartitems ci
    JOIN products p ON ci.product_id = p.id
    WHERE ci.cart_id = cart_id;
    
    RETURN total;
END;
$$ LANGUAGE plpgsql;

-- Function để cập nhật Cart total_amount
CREATE OR REPLACE FUNCTION update_cart_total()
RETURNS TRIGGER AS $$
DECLARE
    cart_id_val BIGINT;
    new_total NUMERIC(10,2);
BEGIN
    -- Lấy cart_id từ record được thay đổi
    IF TG_OP = 'DELETE' THEN
        cart_id_val := OLD.cart_id;
    ELSE
        cart_id_val := NEW.cart_id;
    END IF;
    
    -- Tính tổng mới
    new_total := calculate_cart_total(cart_id_val);
    
    -- Cập nhật Cart
    UPDATE carts 
    SET total_amount = new_total, updated_at = CURRENT_TIMESTAMP
    WHERE id = cart_id_val;
    
    -- Trả về record phù hợp
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Tạo triggers cho CartItem
DROP TRIGGER IF EXISTS trigger_update_cart_total_insert ON cartitems;
CREATE TRIGGER trigger_update_cart_total_insert
    AFTER INSERT ON cartitems
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_total();

DROP TRIGGER IF EXISTS trigger_update_cart_total_update ON cartitems;
CREATE TRIGGER trigger_update_cart_total_update
    AFTER UPDATE ON cartitems
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_total();

DROP TRIGGER IF EXISTS trigger_update_cart_total_delete ON cartitems;
CREATE TRIGGER trigger_update_cart_total_delete
    AFTER DELETE ON cartitems
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_total();

-- =====================================================
-- 2. TRIGGER: Tự động cập nhật Product status
-- =====================================================

-- Function để cập nhật Product status dựa trên stock
CREATE OR REPLACE FUNCTION update_product_status()
RETURNS TRIGGER AS $$
DECLARE
    new_status product_status;
BEGIN
    -- Xác định status mới dựa trên stock_quantity
    IF NEW.stock_quantity IS NULL OR NEW.stock_quantity <= 0 THEN
        new_status := 'out_of_stock';
    ELSIF NEW.status = 'out_of_stock' AND NEW.stock_quantity > 0 THEN
        new_status := 'active';
    ELSE
        new_status := NEW.status; -- Giữ nguyên status hiện tại
    END IF;
    
    -- Cập nhật status nếu cần
    IF NEW.status != new_status THEN
        NEW.status := new_status;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger cho Product
DROP TRIGGER IF EXISTS trigger_update_product_status ON products;
CREATE TRIGGER trigger_update_product_status
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_product_status();

-- =====================================================
-- 3. TRIGGER: Cập nhật Cart total khi Product price thay đổi
-- =====================================================

-- Function để cập nhật tất cả Cart chứa sản phẩm có giá thay đổi
CREATE OR REPLACE FUNCTION update_carts_with_product()
RETURNS TRIGGER AS $$
DECLARE
    cart_record RECORD;
BEGIN
    -- Cập nhật tất cả Cart chứa sản phẩm này
    FOR cart_record IN 
        SELECT DISTINCT c.id 
        FROM carts c
        JOIN cartitems ci ON c.id = ci.cart_id
        WHERE ci.product_id = NEW.id
    LOOP
        UPDATE carts 
        SET total_amount = calculate_cart_total(cart_record.id), 
            updated_at = CURRENT_TIMESTAMP
        WHERE id = cart_record.id;
    END LOOP;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger cho Product price
DROP TRIGGER IF EXISTS trigger_update_carts_on_price_change ON products;
CREATE TRIGGER trigger_update_carts_on_price_change
    AFTER UPDATE OF price ON products
    FOR EACH ROW
    WHEN (OLD.price != NEW.price)
    EXECUTE FUNCTION update_carts_with_product();

-- =====================================================
-- 4. TRIGGER: Cập nhật stock khi OrderItem được tạo
-- =====================================================

-- Function để giảm stock khi có OrderItem mới
CREATE OR REPLACE FUNCTION update_product_stock_on_order()
RETURNS TRIGGER AS $$
BEGIN
    -- Giảm stock của sản phẩm
    UPDATE products 
    SET stock_quantity = stock_quantity - NEW.quantity,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.product_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger cho OrderItem
DROP TRIGGER IF EXISTS trigger_update_stock_on_order ON orderitems;
CREATE TRIGGER trigger_update_stock_on_order
    AFTER INSERT ON orderitems
    FOR EACH ROW
    EXECUTE FUNCTION update_product_stock_on_order();

-- =====================================================
-- 5. TRIGGER: Cập nhật stock khi Order bị hủy
-- =====================================================

-- Function để hoàn lại stock khi Order bị hủy
CREATE OR REPLACE FUNCTION restore_product_stock_on_cancel()
RETURNS TRIGGER AS $$
DECLARE
    order_item RECORD;
BEGIN
    -- Chỉ xử lý khi order status thay đổi thành 'cancelled'
    IF NEW.status = 'cancelled' AND OLD.status != 'cancelled' THEN
        -- Hoàn lại stock cho tất cả OrderItem
        FOR order_item IN 
            SELECT product_id, quantity 
            FROM orderitems 
            WHERE order_id = NEW.id
        LOOP
            UPDATE products 
            SET stock_quantity = stock_quantity + order_item.quantity,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = order_item.product_id;
        END LOOP;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger cho Order status
DROP TRIGGER IF EXISTS trigger_restore_stock_on_cancel ON orders;
CREATE TRIGGER trigger_restore_stock_on_cancel
    AFTER UPDATE OF status ON orders
    FOR EACH ROW
    EXECUTE FUNCTION restore_product_stock_on_cancel();

-- =====================================================
-- VERIFICATION: Kiểm tra triggers đã được tạo
-- =====================================================

-- Kiểm tra các function đã được tạo
SELECT 
    routine_name as function_name,
    routine_type,
    data_type as return_type
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name IN (
    'calculate_cart_total',
    'update_cart_total', 
    'update_product_status',
    'update_carts_with_product',
    'update_product_stock_on_order',
    'restore_product_stock_on_cancel'
)
ORDER BY routine_name;

-- Kiểm tra các trigger đã được tạo
SELECT 
    trigger_name,
    event_object_table as table_name,
    action_timing,
    event_manipulation as event
FROM information_schema.triggers 
WHERE trigger_schema = 'public'
AND trigger_name LIKE 'trigger_%'
ORDER BY event_object_table, trigger_name;

-- =====================================================
-- TEST TRIGGERS: Kiểm tra hoạt động
-- =====================================================

-- Test 1: Thêm CartItem và kiểm tra Cart total
-- INSERT INTO cartitems (cart_id, product_id, quantity) VALUES (1, 1, 2);
-- SELECT id, total_amount FROM carts WHERE id = 1;

-- Test 2: Cập nhật Product stock và kiểm tra status
-- UPDATE products SET stock_quantity = 0 WHERE id = 1;
-- SELECT id, name, stock_quantity, status FROM products WHERE id = 1;

-- Test 3: Cập nhật Product price và kiểm tra Cart total
-- UPDATE products SET price = 150000 WHERE id = 1;
-- SELECT id, total_amount FROM carts WHERE id = 1;

COMMIT;
