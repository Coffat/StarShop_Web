-- =====================================================
-- Migration: Add image column to Catalogs table
-- Purpose: Support catalog/category images
-- Date: 2025-10-15
-- =====================================================

-- Add image column to Catalogs table
ALTER TABLE Catalogs 
ADD COLUMN image VARCHAR(255) DEFAULT NULL;

-- Add comment for documentation
COMMENT ON COLUMN Catalogs.image IS 'Catalog/category image path or URL';

-- Add index for performance (optional, useful if filtering by image existence)
CREATE INDEX idx_catalogs_image ON Catalogs(image) WHERE image IS NOT NULL;

-- =====================================================
-- Optional: Update existing catalogs with sample images
-- Uncomment and modify paths as needed
-- =====================================================

-- UPDATE Catalogs SET image = '/images/catalogs/tinh-yeu.jpg' WHERE value = 'Tình yêu';
-- UPDATE Catalogs SET image = '/images/catalogs/khai-truong.jpg' WHERE value = 'Khai trương';
-- UPDATE Catalogs SET image = '/images/catalogs/hoa-cuoi.jpg' WHERE value = 'Hoa cưới';
-- UPDATE Catalogs SET image = '/images/catalogs/dam-tang.jpg' WHERE value = 'Đám tang';
-- UPDATE Catalogs SET image = '/images/catalogs/chuc-mung.jpg' WHERE value = 'Chúc mừng';

-- Grant permissions to application user
GRANT SELECT, INSERT, UPDATE ON Catalogs TO flower_admin;

-- Verification query (for testing)
-- SELECT id, value, image, created_at FROM Catalogs ORDER BY id;
