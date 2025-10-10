-- =============================================
-- PRODUCT MANAGEMENT ENHANCEMENTS
-- Triggers, Functions & Procedures for Product CRUD Operations
-- =============================================

-- Drop existing triggers first to avoid conflicts
DROP TRIGGER IF EXISTS trigger_validate_product ON products;
DROP TRIGGER IF EXISTS trigger_update_product_status ON products;
DROP TRIGGER IF EXISTS trigger_log_product_changes ON products;
DROP TRIGGER IF EXISTS trigger_update_product_statistics ON products;

BEGIN;

-- ========== 1) AUDIT TABLE FOR PRODUCT CHANGES ==========
CREATE TABLE IF NOT EXISTS product_audit_log (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    action VARCHAR(50) NOT NULL, -- INSERT, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_product_audit_product_id ON product_audit_log(product_id);
CREATE INDEX IF NOT EXISTS idx_product_audit_changed_at ON product_audit_log(changed_at);

-- ========== 2) PRODUCT STATISTICS TABLE ==========
CREATE TABLE IF NOT EXISTS product_statistics (
    id BIGSERIAL PRIMARY KEY,
    total_products INTEGER DEFAULT 0,
    active_products INTEGER DEFAULT 0,
    inactive_products INTEGER DEFAULT 0,
    out_of_stock_products INTEGER DEFAULT 0,
    low_stock_products INTEGER DEFAULT 0,
    discontinued_products INTEGER DEFAULT 0,
    total_stock_value DECIMAL(15,2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT NOW()
);
-- Insert initial record
INSERT INTO product_statistics (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

-- ========== 3) FUNCTIONS ==========

-- Function to calculate product statistics (for trigger)
CREATE OR REPLACE FUNCTION calculate_product_statistics()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE product_statistics SET
        total_products = (SELECT COUNT(*) FROM products),
        active_products = (SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'),
        inactive_products = (SELECT COUNT(*) FROM products WHERE status = 'INACTIVE'),
        out_of_stock_products = (SELECT COUNT(*) FROM products WHERE status = 'OUT_OF_STOCK'),
        discontinued_products = (SELECT COUNT(*) FROM products WHERE status = 'DISCONTINUED'),
        low_stock_products = (SELECT COUNT(*) FROM products WHERE stock_quantity < 10),
        last_updated = NOW()
    WHERE id = 1;
    
    -- Insert if no record exists
    INSERT INTO product_statistics (id, total_products, active_products, inactive_products, 
                                  out_of_stock_products, discontinued_products, low_stock_products, last_updated)
    SELECT 1, 
           (SELECT COUNT(*) FROM products),
           (SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'),
           (SELECT COUNT(*) FROM products WHERE status = 'INACTIVE'),
           (SELECT COUNT(*) FROM products WHERE status = 'OUT_OF_STOCK'),
           (SELECT COUNT(*) FROM products WHERE status = 'DISCONTINUED'),
           (SELECT COUNT(*) FROM products WHERE stock_quantity < 10),
           NOW()
    WHERE NOT EXISTS (SELECT 1 FROM product_statistics WHERE id = 1);
    
    RETURN NULL; -- For AFTER trigger
END;
$$ LANGUAGE plpgsql;

-- Separate function for manual statistics calculation
CREATE OR REPLACE FUNCTION refresh_product_statistics()
RETURNS VOID AS $$
BEGIN
    UPDATE product_statistics SET
        total_products = (SELECT COUNT(*) FROM products),
        active_products = (SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'),
        inactive_products = (SELECT COUNT(*) FROM products WHERE status = 'INACTIVE'),
        out_of_stock_products = (SELECT COUNT(*) FROM products WHERE status = 'OUT_OF_STOCK'),
        discontinued_products = (SELECT COUNT(*) FROM products WHERE status = 'DISCONTINUED'),
        low_stock_products = (SELECT COUNT(*) FROM products WHERE stock_quantity < 10),
        last_updated = NOW()
    WHERE id = 1;
    
    -- Insert if no record exists
    INSERT INTO product_statistics (id, total_products, active_products, inactive_products, 
                                  out_of_stock_products, discontinued_products, low_stock_products, last_updated)
    SELECT 1, 
           (SELECT COUNT(*) FROM products),
           (SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'),
           (SELECT COUNT(*) FROM products WHERE status = 'INACTIVE'),
           (SELECT COUNT(*) FROM products WHERE status = 'OUT_OF_STOCK'),
           (SELECT COUNT(*) FROM products WHERE status = 'DISCONTINUED'),
           (SELECT COUNT(*) FROM products WHERE stock_quantity < 10),
           NOW()
    WHERE NOT EXISTS (SELECT 1 FROM product_statistics WHERE id = 1);
END;
$$ LANGUAGE plpgsql;

-- Function to update product status based on stock
CREATE OR REPLACE FUNCTION update_product_status_by_stock()
RETURNS TRIGGER AS $$
BEGIN
    -- Auto update status to OUT_OF_STOCK if stock is 0
    IF NEW.stock_quantity = 0 AND NEW.status != 'DISCONTINUED' THEN
        NEW.status = 'OUT_OF_STOCK';
    -- Auto update status to ACTIVE if stock > 0 and currently OUT_OF_STOCK
    ELSIF NEW.stock_quantity > 0 AND OLD.status = 'OUT_OF_STOCK' THEN
        NEW.status = 'ACTIVE';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Function to log product changes
CREATE OR REPLACE FUNCTION log_product_changes()
RETURNS TRIGGER AS $$
DECLARE
    action_type VARCHAR(10);
    old_data JSONB;
    new_data JSONB;
BEGIN
    -- Determine action type
    IF TG_OP = 'INSERT' THEN
        action_type = 'INSERT';
        old_data = NULL;
        new_data = row_to_json(NEW)::jsonb;
    ELSIF TG_OP = 'UPDATE' THEN
        action_type = 'UPDATE';
        old_data = row_to_json(OLD)::jsonb;
        new_data = row_to_json(NEW)::jsonb;
    ELSIF TG_OP = 'DELETE' THEN
        action_type = 'DELETE';
        old_data = row_to_json(OLD)::jsonb;
        new_data = NULL;
    END IF;

    -- Insert audit log
    INSERT INTO product_audit_log (
        product_id, 
        action, 
        old_values, 
        new_values, 
        changed_by,
        changed_at
    ) VALUES (
        COALESCE(NEW.id, OLD.id),
        action_type,
        old_data,
        new_data,
        current_user,
        NOW()
    );

    -- Return appropriate record
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to validate product data
CREATE OR REPLACE FUNCTION validate_product_data()
RETURNS TRIGGER AS $$
BEGIN
    -- Validate price
    IF NEW.price <= 0 THEN
        RAISE EXCEPTION 'Product price must be greater than 0';
    END IF;
    
    -- Validate stock quantity
    IF NEW.stock_quantity < 0 THEN
        RAISE EXCEPTION 'Stock quantity cannot be negative';
    END IF;
    
    -- Validate name
    IF LENGTH(TRIM(NEW.name)) < 3 THEN
        RAISE EXCEPTION 'Product name must be at least 3 characters long';
    END IF;
    
    -- Validate shipping dimensions
    IF NEW.weight_g IS NOT NULL AND NEW.weight_g <= 0 THEN
        RAISE EXCEPTION 'Weight must be greater than 0';
    END IF;
    
    IF NEW.length_cm IS NOT NULL AND NEW.length_cm <= 0 THEN
        RAISE EXCEPTION 'Length must be greater than 0';
    END IF;
    
    IF NEW.width_cm IS NOT NULL AND NEW.width_cm <= 0 THEN
        RAISE EXCEPTION 'Width must be greater than 0';
    END IF;
    
    IF NEW.height_cm IS NOT NULL AND NEW.height_cm <= 0 THEN
        RAISE EXCEPTION 'Height must be greater than 0';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========== 4) TRIGGERS ==========

-- Trigger for product validation (before insert/update)
CREATE TRIGGER trigger_validate_product
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION validate_product_data();

-- Trigger for auto status update based on stock (before insert/update)
CREATE TRIGGER trigger_update_product_status
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_product_status_by_stock();

-- Trigger for audit logging (after insert/update/delete)
CREATE TRIGGER trigger_log_product_changes
    AFTER INSERT OR UPDATE OR DELETE ON products
    FOR EACH ROW
    EXECUTE FUNCTION log_product_changes();

-- Trigger for statistics update (after insert/update/delete)
CREATE TRIGGER trigger_update_product_statistics
    AFTER INSERT OR UPDATE OR DELETE ON products
    FOR EACH STATEMENT
    EXECUTE FUNCTION calculate_product_statistics();

-- ========== 5) STORED PROCEDURES ==========

-- Procedure to bulk update product status
CREATE OR REPLACE FUNCTION bulk_update_product_status(
    p_product_ids BIGINT[],
    p_new_status VARCHAR(20),
    p_updated_by VARCHAR(255) DEFAULT current_user
)
RETURNS TABLE(updated_count INTEGER, failed_ids BIGINT[]) AS $$
DECLARE
    updated_rows INTEGER := 0;
    failed_list BIGINT[] := ARRAY[]::BIGINT[];
    product_id BIGINT;
BEGIN
    -- Validate status
    IF p_new_status NOT IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED') THEN
        RAISE EXCEPTION 'Invalid product status: %', p_new_status;
    END IF;
    
    -- Update each product
    FOREACH product_id IN ARRAY p_product_ids
    LOOP
        BEGIN
            UPDATE products 
            SET status = p_new_status::product_status, 
                updated_at = NOW()
            WHERE id = product_id;
            
            IF FOUND THEN
                updated_rows := updated_rows + 1;
            ELSE
                failed_list := array_append(failed_list, product_id);
            END IF;
        EXCEPTION WHEN OTHERS THEN
            failed_list := array_append(failed_list, product_id);
        END;
    END LOOP;
    
    RETURN QUERY SELECT updated_rows, failed_list;
END;
$$ LANGUAGE plpgsql;

-- Procedure to bulk update stock quantities
CREATE OR REPLACE FUNCTION bulk_update_stock(
    p_updates JSONB -- Format: [{"id": 1, "stock": 50}, {"id": 2, "stock": 30}]
)
RETURNS TABLE(updated_count INTEGER, failed_updates JSONB) AS $$
DECLARE
    updated_rows INTEGER := 0;
    failed_list JSONB := '[]'::jsonb;
    update_item JSONB;
    product_id BIGINT;
    new_stock INTEGER;
BEGIN
    -- Process each update
    FOR update_item IN SELECT * FROM jsonb_array_elements(p_updates)
    LOOP
        BEGIN
            product_id := (update_item->>'id')::BIGINT;
            new_stock := (update_item->>'stock')::INTEGER;
            
            -- Validate stock
            IF new_stock < 0 THEN
                failed_list := failed_list || jsonb_build_object(
                    'id', product_id, 
                    'error', 'Stock quantity cannot be negative'
                );
                CONTINUE;
            END IF;
            
            UPDATE products 
            SET stock_quantity = new_stock,
                updated_at = NOW()
            WHERE id = product_id;
            
            IF FOUND THEN
                updated_rows := updated_rows + 1;
            ELSE
                failed_list := failed_list || jsonb_build_object(
                    'id', product_id, 
                    'error', 'Product not found'
                );
            END IF;
        EXCEPTION WHEN OTHERS THEN
            failed_list := failed_list || jsonb_build_object(
                'id', product_id, 
                'error', SQLERRM
            );
        END;
    END LOOP;
    
    RETURN QUERY SELECT updated_rows, failed_list;
END;
$$ LANGUAGE plpgsql;

-- Procedure to get product analytics
CREATE OR REPLACE FUNCTION get_product_analytics(
    p_start_date DATE DEFAULT CURRENT_DATE - INTERVAL '30 days',
    p_end_date DATE DEFAULT CURRENT_DATE
)
RETURNS TABLE(
    metric_name VARCHAR(50),
    metric_value DECIMAL(15,2),
    metric_description TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'total_products'::VARCHAR(50) as metric_name,
        COUNT(*)::DECIMAL(15,2) as metric_value,
        'Total number of products'::TEXT as metric_description
    FROM products
    
    UNION ALL
    
    SELECT 
        'active_products'::VARCHAR(50),
        COUNT(*)::DECIMAL(15,2),
        'Number of active products'::TEXT
    FROM products WHERE status = 'ACTIVE'
    
    UNION ALL
    
    SELECT 
        'low_stock_products'::VARCHAR(50),
        COUNT(*)::DECIMAL(15,2),
        'Products with stock < 10'::TEXT
    FROM products WHERE stock_quantity < 10 AND stock_quantity > 0
    
    UNION ALL
    
    SELECT 
        'out_of_stock_products'::VARCHAR(50),
        COUNT(*)::DECIMAL(15,2),
        'Products with zero stock'::TEXT
    FROM products WHERE stock_quantity = 0
    
    UNION ALL
    
    SELECT 
        'total_stock_value'::VARCHAR(50),
        COALESCE(SUM(price * stock_quantity), 0)::DECIMAL(15,2),
        'Total value of all stock'::TEXT
    FROM products WHERE status = 'ACTIVE'
    
    UNION ALL
    
    SELECT 
        'avg_product_price'::VARCHAR(50),
        COALESCE(AVG(price), 0)::DECIMAL(15,2),
        'Average product price'::TEXT
    FROM products WHERE status = 'ACTIVE'
    
    UNION ALL
    
    SELECT 
        'products_created_period'::VARCHAR(50),
        COUNT(*)::DECIMAL(15,2),
        'Products created in date range'::TEXT
    FROM products 
    WHERE created_at::DATE BETWEEN p_start_date AND p_end_date;
END;
$$ LANGUAGE plpgsql;

-- ========== 6) VIEWS FOR REPORTING ==========

-- View for product summary with calculated fields
CREATE OR REPLACE VIEW product_summary AS
SELECT 
    p.id,
    p.name,
    c.value as catalog_name,
    p.price,
    p.stock_quantity,
    p.status,
    p.created_at,
    p.updated_at,
    (p.price * p.stock_quantity) as stock_value,
    CASE 
        WHEN p.stock_quantity = 0 THEN 'Out of Stock'
        WHEN p.stock_quantity < 10 THEN 'Low Stock'
        WHEN p.stock_quantity < 50 THEN 'Medium Stock'
        ELSE 'High Stock'
    END as stock_level,
    CASE 
        WHEN p.status = 'ACTIVE' AND p.stock_quantity > 0 THEN 'Available'
        WHEN p.status = 'ACTIVE' AND p.stock_quantity = 0 THEN 'Out of Stock'
        ELSE 'Unavailable'
    END as availability_status
FROM products p
LEFT JOIN catalogs c ON p.catalog_id = c.id;

-- View for audit trail with user-friendly format
CREATE OR REPLACE VIEW product_audit_summary AS
SELECT 
    pal.id,
    pal.product_id,
    p.name as product_name,
    pal.action,
    pal.changed_by,
    pal.changed_at,
    CASE 
        WHEN pal.action = 'INSERT' THEN 'Product created'
        WHEN pal.action = 'UPDATE' THEN 'Product updated'
        WHEN pal.action = 'DELETE' THEN 'Product deleted'
    END as action_description,
    pal.old_values,
    pal.new_values
FROM product_audit_log pal
LEFT JOIN products p ON pal.product_id = p.id
ORDER BY pal.changed_at DESC;

-- ========== 7) INITIAL DATA CALCULATION ==========

-- Calculate initial statistics
SELECT refresh_product_statistics();

COMMIT;

-- ========== 8) GRANT PERMISSIONS ==========
GRANT SELECT, INSERT, UPDATE, DELETE ON product_audit_log TO flower_admin;
GRANT SELECT, UPDATE ON product_statistics TO flower_admin;
GRANT SELECT ON product_summary TO flower_admin;
GRANT SELECT ON product_audit_summary TO flower_admin;
GRANT USAGE ON SEQUENCE product_audit_log_id_seq TO flower_admin;

-- Grant execute permissions on functions
GRANT EXECUTE ON FUNCTION calculate_product_statistics() TO flower_admin;
GRANT EXECUTE ON FUNCTION refresh_product_statistics() TO flower_admin;
GRANT EXECUTE ON FUNCTION bulk_update_product_status(BIGINT[], VARCHAR, VARCHAR) TO flower_admin;
GRANT EXECUTE ON FUNCTION bulk_update_stock(JSONB) TO flower_admin;
GRANT EXECUTE ON FUNCTION get_product_analytics(DATE, DATE) TO flower_admin;

COMMIT;
