-- Fix product status column type
-- Change from product_status ENUM to VARCHAR to avoid Hibernate casting issues

-- Step 1: Alter column to VARCHAR with explicit cast
ALTER TABLE products 
ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- Step 2: Keep the default value
ALTER TABLE products 
ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- Step 3: Add check constraint to ensure valid values
ALTER TABLE products
ADD CONSTRAINT products_status_check 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED'));

-- Note: We keep the product_status ENUM type in database for potential future use
-- but the products table now uses VARCHAR with constraint
