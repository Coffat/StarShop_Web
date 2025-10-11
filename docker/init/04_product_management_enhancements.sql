-- =============================================
-- PRODUCT MANAGEMENT ENHANCEMENTS (SIMPLIFIED)
-- Basic Triggers & Functions for Product CRUD
-- =============================================

-- Drop existing triggers first to avoid conflicts
DROP TRIGGER IF EXISTS trigger_validate_product ON products;
DROP TRIGGER IF EXISTS trigger_update_product_status ON products;

BEGIN;

-- ========== 1) FUNCTIONS ==========

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

-- ========== 2) TRIGGERS ==========

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

COMMIT;
