-- ====================================================================================
-- Add Admin Response Fields to Reviews Table
-- ====================================================================================
-- This script adds admin response functionality to existing reviews table
-- Created: 2025-01-19
-- ====================================================================================

-- Add admin response fields to Reviews table
DO $$ 
BEGIN
    -- Add admin_response column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='reviews' AND column_name='admin_response') THEN
        ALTER TABLE Reviews ADD COLUMN admin_response TEXT DEFAULT NULL;
        COMMENT ON COLUMN Reviews.admin_response IS 'Admin response to customer review';
    END IF;
    
    -- Add admin_response_at column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='reviews' AND column_name='admin_response_at') THEN
        ALTER TABLE Reviews ADD COLUMN admin_response_at TIMESTAMP DEFAULT NULL;
        COMMENT ON COLUMN Reviews.admin_response_at IS 'Timestamp when admin responded';
    END IF;
    
    -- Add admin_response_by column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='reviews' AND column_name='admin_response_by') THEN
        ALTER TABLE Reviews ADD COLUMN admin_response_by BIGINT DEFAULT NULL;
        COMMENT ON COLUMN Reviews.admin_response_by IS 'Admin user who responded';
    END IF;
END $$;

-- Add foreign key constraint for admin_response_by
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'fk_reviews_admin_response_by') THEN
        ALTER TABLE Reviews ADD CONSTRAINT fk_reviews_admin_response_by 
        FOREIGN KEY (admin_response_by) REFERENCES Users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_reviews_admin_response_by ON Reviews(admin_response_by);
CREATE INDEX IF NOT EXISTS idx_reviews_admin_response_at ON Reviews(admin_response_at);

COMMENT ON TABLE Reviews IS 'Product reviews and ratings with admin response support';
