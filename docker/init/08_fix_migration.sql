-- =====================================================
-- FIX MIGRATION: Sửa lỗi contains null values
-- File: 08_fix_migration.sql
-- Mục đích: Sửa lỗi khi thêm cột NOT NULL vào bảng có dữ liệu
-- =====================================================

-- =====================================================
-- 1. SỬA LỖI CARTS.TOTAL_AMOUNT
-- =====================================================

-- Kiểm tra xem cột total_amount đã tồn tại chưa
DO $$
BEGIN
    -- Nếu cột chưa tồn tại, thêm cột với giá trị mặc định
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'carts' AND column_name = 'total_amount'
    ) THEN
        ALTER TABLE carts ADD COLUMN total_amount NUMERIC(10,2) DEFAULT 0.00;
        RAISE NOTICE 'Added total_amount column to carts table';
    ELSE
        RAISE NOTICE 'total_amount column already exists in carts table';
    END IF;
END $$;

-- Cập nhật giá trị NULL thành 0.00
UPDATE carts SET total_amount = 0.00 WHERE total_amount IS NULL;

-- =====================================================
-- 2. SỬA LỖI PRODUCTS.STATUS
-- =====================================================

-- Kiểm tra xem cột status đã tồn tại chưa
DO $$
BEGIN
    -- Nếu cột chưa tồn tại, thêm cột với giá trị mặc định
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'products' AND column_name = 'status'
    ) THEN
        ALTER TABLE products ADD COLUMN status product_status DEFAULT 'active';
        RAISE NOTICE 'Added status column to products table';
    ELSE
        RAISE NOTICE 'status column already exists in products table';
    END IF;
END $$;

-- Cập nhật giá trị NULL thành 'active'
UPDATE products SET status = 'active' WHERE status IS NULL;

-- Cập nhật status dựa trên stock_quantity
UPDATE products SET status = 'out_of_stock' WHERE stock_quantity <= 0 AND status = 'active';

-- =====================================================
-- 3. SỬA LỖI PRODUCTATTRIBUTES.CREATED_AT
-- =====================================================

-- Kiểm tra xem cột created_at đã tồn tại chưa
DO $$
BEGIN
    -- Nếu cột chưa tồn tại, thêm cột với giá trị mặc định
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'productattributes' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE productattributes ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE 'Added created_at column to productattributes table';
    ELSE
        RAISE NOTICE 'created_at column already exists in productattributes table';
    END IF;
END $$;

-- Kiểm tra xem cột updated_at đã tồn tại chưa
DO $$
BEGIN
    -- Nếu cột chưa tồn tại, thêm cột với giá trị mặc định
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'productattributes' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE productattributes ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        RAISE NOTICE 'Added updated_at column to productattributes table';
    ELSE
        RAISE NOTICE 'updated_at column already exists in productattributes table';
    END IF;
END $$;

-- Cập nhật giá trị NULL thành CURRENT_TIMESTAMP
UPDATE productattributes SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE productattributes SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;

-- =====================================================
-- 4. KIỂM TRA VÀ CẬP NHẬT CÁC CỘT KHÁC
-- =====================================================

-- Kiểm tra và cập nhật cột stock_quantity trong products
UPDATE products SET stock_quantity = 0 WHERE stock_quantity IS NULL;

-- Kiểm tra và cập nhật cột is_active trong vouchers
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'vouchers' AND column_name = 'is_active'
    ) THEN
        UPDATE vouchers SET is_active = true WHERE is_active IS NULL;
        RAISE NOTICE 'Updated is_active column in vouchers table';
    END IF;
END $$;

-- Kiểm tra và cập nhật cột is_active trong deliveryunits
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'deliveryunits' AND column_name = 'is_active'
    ) THEN
        UPDATE deliveryunits SET is_active = true WHERE is_active IS NULL;
        RAISE NOTICE 'Updated is_active column in deliveryunits table';
    END IF;
END $$;

-- =====================================================
-- 5. VERIFICATION: Kiểm tra kết quả
-- =====================================================

-- Kiểm tra carts table
SELECT 
    'carts' as table_name,
    COUNT(*) as total_rows,
    COUNT(total_amount) as non_null_total_amount,
    COUNT(*) - COUNT(total_amount) as null_total_amount
FROM carts;

-- Kiểm tra products table
SELECT 
    'products' as table_name,
    COUNT(*) as total_rows,
    COUNT(status) as non_null_status,
    COUNT(*) - COUNT(status) as null_status,
    COUNT(stock_quantity) as non_null_stock,
    COUNT(*) - COUNT(stock_quantity) as null_stock
FROM products;

-- Kiểm tra productattributes table
SELECT 
    'productattributes' as table_name,
    COUNT(*) as total_rows,
    COUNT(created_at) as non_null_created_at,
    COUNT(*) - COUNT(created_at) as null_created_at,
    COUNT(updated_at) as non_null_updated_at,
    COUNT(*) - COUNT(updated_at) as null_updated_at
FROM productattributes;

-- =====================================================
-- 6. TẠO INDEXES NẾU CHƯA CÓ
-- =====================================================

-- Index cho products.status
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

-- Index cho carts.total_amount
CREATE INDEX IF NOT EXISTS idx_carts_total_amount ON carts(total_amount);

-- =====================================================
-- 7. CẬP NHẬT CONSTRAINTS
-- =====================================================

-- Thêm constraint cho carts.total_amount
DO $$
BEGIN
    -- Kiểm tra xem constraint đã tồn tại chưa
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'carts_total_amount_check'
    ) THEN
        ALTER TABLE carts ADD CONSTRAINT carts_total_amount_check CHECK (total_amount >= 0);
        RAISE NOTICE 'Added total_amount check constraint to carts table';
    END IF;
END $$;

-- =====================================================
-- 8. FINAL VERIFICATION
-- =====================================================

-- Kiểm tra tất cả các cột quan trọng
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name IN ('carts', 'products', 'productattributes')
AND column_name IN ('total_amount', 'status', 'stock_quantity', 'created_at', 'updated_at', 'is_active')
ORDER BY table_name, column_name;

-- Kiểm tra dữ liệu mẫu
SELECT 'Carts with total_amount:' as info, COUNT(*) as count FROM carts WHERE total_amount IS NOT NULL
UNION ALL
SELECT 'Products with status:', COUNT(*) FROM products WHERE status IS NOT NULL
UNION ALL
SELECT 'Products with stock_quantity:', COUNT(*) FROM products WHERE stock_quantity IS NOT NULL
UNION ALL
SELECT 'ProductAttributes with created_at:', COUNT(*) FROM productattributes WHERE created_at IS NOT NULL;

RAISE NOTICE 'Migration completed successfully! All NULL values have been updated with default values.';

COMMIT;
