-- ====================================================================================
-- Employee and User Management Enhancement
-- ====================================================================================
-- This script adds necessary enhancements for employee and customer management
-- Created: 2025-01-11
-- ====================================================================================

-- Add employee-specific fields to Users table if not exists
DO $$ 
BEGIN
    -- Add employee_code column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='employee_code') THEN
        ALTER TABLE Users ADD COLUMN employee_code VARCHAR(20) UNIQUE;
        COMMENT ON COLUMN Users.employee_code IS 'Unique employee code for staff identification';
    END IF;
    
    -- Add hire_date column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='hire_date') THEN
        ALTER TABLE Users ADD COLUMN hire_date DATE;
        COMMENT ON COLUMN Users.hire_date IS 'Date when employee was hired';
    END IF;
    
    -- Add position column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='position') THEN
        ALTER TABLE Users ADD COLUMN position VARCHAR(100);
        COMMENT ON COLUMN Users.position IS 'Employee position/job title';
    END IF;
    
    -- Add department column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='department') THEN
        ALTER TABLE Users ADD COLUMN department VARCHAR(100);
        COMMENT ON COLUMN Users.department IS 'Employee department';
    END IF;
    
    -- Add salary_per_hour column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='salary_per_hour') THEN
        ALTER TABLE Users ADD COLUMN salary_per_hour NUMERIC(10,2) DEFAULT 0.00 CHECK (salary_per_hour >= 0);
        COMMENT ON COLUMN Users.salary_per_hour IS 'Hourly wage for employee';
    END IF;
    
    -- Add is_active column for user status
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='is_active') THEN
        ALTER TABLE Users ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
        COMMENT ON COLUMN Users.is_active IS 'Whether user account is active';
    END IF;
    
    -- Add last_login column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='last_login') THEN
        ALTER TABLE Users ADD COLUMN last_login TIMESTAMP;
        COMMENT ON COLUMN Users.last_login IS 'Last login timestamp';
    END IF;
END $$;

-- Add voucher name column
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='vouchers' AND column_name='name') THEN
        ALTER TABLE Vouchers ADD COLUMN name VARCHAR(255) NOT NULL DEFAULT '';
        COMMENT ON COLUMN Vouchers.name IS 'Display name for voucher';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='vouchers' AND column_name='description') THEN
        ALTER TABLE Vouchers ADD COLUMN description TEXT;
        COMMENT ON COLUMN Vouchers.description IS 'Voucher description and terms';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='vouchers' AND column_name='max_discount_amount') THEN
        ALTER TABLE Vouchers ADD COLUMN max_discount_amount NUMERIC(10,2);
        COMMENT ON COLUMN Vouchers.max_discount_amount IS 'Maximum discount amount for percentage vouchers';
    END IF;
END $$;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_role ON Users(role);
CREATE INDEX IF NOT EXISTS idx_users_employee_code ON Users(employee_code) WHERE employee_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_is_active ON Users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_role_active ON Users(role, is_active);

-- ====================================================================================
-- FUNCTION: Auto-generate Employee Code
-- ====================================================================================
CREATE OR REPLACE FUNCTION generate_employee_code() RETURNS TRIGGER AS $$
DECLARE
    new_code VARCHAR(20);
    max_num INTEGER;
BEGIN
    -- Only generate for STAFF and ADMIN roles
    IF NEW.role IN ('STAFF', 'ADMIN') AND NEW.employee_code IS NULL THEN
        -- Get the maximum employee number
        SELECT COALESCE(MAX(CAST(SUBSTRING(employee_code FROM 4) AS INTEGER)), 0)
        INTO max_num
        FROM Users
        WHERE employee_code LIKE 'EMP%';
        
        -- Generate new code (e.g., EMP0001, EMP0002, ...)
        new_code := 'EMP' || LPAD((max_num + 1)::TEXT, 4, '0');
        NEW.employee_code := new_code;
        
        -- Set hire date if not provided
        IF NEW.hire_date IS NULL THEN
            NEW.hire_date := CURRENT_DATE;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS trg_generate_employee_code ON Users;
CREATE TRIGGER trg_generate_employee_code
    BEFORE INSERT ON Users
    FOR EACH ROW
    EXECUTE FUNCTION generate_employee_code();

-- ====================================================================================
-- FUNCTION: Update last login timestamp
-- ====================================================================================
CREATE OR REPLACE FUNCTION update_last_login(user_id_param BIGINT) RETURNS VOID AS $$
BEGIN
    UPDATE Users 
    SET last_login = CURRENT_TIMESTAMP
    WHERE id = user_id_param;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================
-- FUNCTION: Get employee statistics
-- ====================================================================================
CREATE OR REPLACE FUNCTION get_employee_stats()
RETURNS TABLE(
    total_employees BIGINT,
    active_employees BIGINT,
    inactive_employees BIGINT,
    staff_count BIGINT,
    admin_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) FILTER (WHERE role IN ('STAFF', 'ADMIN')) as total_employees,
        COUNT(*) FILTER (WHERE role IN ('STAFF', 'ADMIN') AND is_active = TRUE) as active_employees,
        COUNT(*) FILTER (WHERE role IN ('STAFF', 'ADMIN') AND is_active = FALSE) as inactive_employees,
        COUNT(*) FILTER (WHERE role = 'STAFF') as staff_count,
        COUNT(*) FILTER (WHERE role = 'ADMIN') as admin_count
    FROM Users;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================
-- FUNCTION: Get customer statistics
-- ====================================================================================
CREATE OR REPLACE FUNCTION get_customer_stats()
RETURNS TABLE(
    total_customers BIGINT,
    active_customers BIGINT,
    inactive_customers BIGINT,
    customers_with_orders BIGINT,
    new_customers_this_month BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) FILTER (WHERE role = 'CUSTOMER') as total_customers,
        COUNT(*) FILTER (WHERE role = 'CUSTOMER' AND is_active = TRUE) as active_customers,
        COUNT(*) FILTER (WHERE role = 'CUSTOMER' AND is_active = FALSE) as inactive_customers,
        COUNT(DISTINCT o.user_id) as customers_with_orders,
        COUNT(*) FILTER (WHERE role = 'CUSTOMER' AND created_at >= DATE_TRUNC('month', CURRENT_DATE)) as new_customers_this_month
    FROM Users u
    LEFT JOIN Orders o ON u.id = o.user_id AND u.role = 'CUSTOMER';
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================
-- FUNCTION: Get voucher statistics
-- ====================================================================================
CREATE OR REPLACE FUNCTION get_voucher_stats()
RETURNS TABLE(
    total_vouchers BIGINT,
    active_vouchers BIGINT,
    expired_vouchers BIGINT,
    used_up_vouchers BIGINT,
    total_uses BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_vouchers,
        COUNT(*) FILTER (WHERE is_active = TRUE AND expiry_date >= CURRENT_DATE 
                        AND (max_uses IS NULL OR uses < max_uses)) as active_vouchers,
        COUNT(*) FILTER (WHERE expiry_date < CURRENT_DATE) as expired_vouchers,
        COUNT(*) FILTER (WHERE max_uses IS NOT NULL AND uses >= max_uses) as used_up_vouchers,
        COALESCE(SUM(uses), 0) as total_uses
    FROM Vouchers;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================
-- FUNCTION: Update voucher usage
-- ====================================================================================
CREATE OR REPLACE FUNCTION increment_voucher_usage() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.voucher_id IS NOT NULL THEN
        UPDATE Vouchers 
        SET uses = uses + 1
        WHERE id = NEW.voucher_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS trg_increment_voucher_usage ON Orders;
CREATE TRIGGER trg_increment_voucher_usage
    AFTER INSERT ON Orders
    FOR EACH ROW
    WHEN (NEW.voucher_id IS NOT NULL)
    EXECUTE FUNCTION increment_voucher_usage();

-- ====================================================================================
-- FUNCTION: Calculate employee monthly salary
-- ====================================================================================
CREATE OR REPLACE FUNCTION calculate_employee_monthly_hours(
    employee_id BIGINT,
    month_date DATE
) RETURNS NUMERIC AS $$
DECLARE
    total_hours NUMERIC;
BEGIN
    SELECT COALESCE(SUM(hours_worked), 0)
    INTO total_hours
    FROM TimeSheets
    WHERE staff_id = employee_id
    AND DATE_TRUNC('month', date) = DATE_TRUNC('month', month_date);
    
    RETURN total_hours;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================
-- PROCEDURE: Generate monthly salaries for all employees
-- ====================================================================================
CREATE OR REPLACE PROCEDURE generate_monthly_salaries(month_date DATE)
LANGUAGE plpgsql
AS $$
DECLARE
    emp_record RECORD;
    work_hours NUMERIC;
BEGIN
    -- Loop through all active employees
    FOR emp_record IN 
        SELECT id, salary_per_hour 
        FROM Users 
        WHERE role IN ('STAFF', 'ADMIN') 
        AND is_active = TRUE
        AND salary_per_hour > 0
    LOOP
        -- Calculate total hours worked
        work_hours := calculate_employee_monthly_hours(emp_record.id, month_date);
        
        -- Insert or update salary record
        INSERT INTO Salaries (user_id, base_salary, working_time, month_year, created_at)
        VALUES (emp_record.id, emp_record.salary_per_hour, work_hours, 
                DATE_TRUNC('month', month_date)::DATE, CURRENT_TIMESTAMP)
        ON CONFLICT (user_id, month_year) 
        DO UPDATE SET 
            base_salary = emp_record.salary_per_hour,
            working_time = work_hours,
            updated_at = CURRENT_TIMESTAMP;
    END LOOP;
    
    RAISE NOTICE 'Monthly salaries generated for %', TO_CHAR(month_date, 'YYYY-MM');
END;
$$;

-- ====================================================================================
-- VIEW: Employee summary with work statistics
-- ====================================================================================
CREATE OR REPLACE VIEW employee_summary AS
SELECT 
    u.id,
    u.employee_code,
    u.firstname,
    u.lastname,
    u.email,
    u.phone,
    u.role,
    u.position,
    u.department,
    u.salary_per_hour,
    u.hire_date,
    u.is_active,
    u.last_login,
    u.created_at,
    COUNT(DISTINCT ts.id) as total_shifts,
    COALESCE(SUM(ts.hours_worked), 0) as total_hours_worked,
    COUNT(DISTINCT DATE_TRUNC('month', ts.date)) as months_worked
FROM Users u
LEFT JOIN TimeSheets ts ON u.id = ts.staff_id
WHERE u.role IN ('STAFF', 'ADMIN')
GROUP BY u.id;

COMMENT ON VIEW employee_summary IS 'Consolidated view of employee information with work statistics';

-- ====================================================================================
-- VIEW: Customer summary with order statistics
-- ====================================================================================
CREATE OR REPLACE VIEW customer_summary AS
SELECT 
    u.id,
    u.firstname,
    u.lastname,
    u.email,
    u.phone,
    u.is_active,
    u.last_login,
    u.created_at,
    COUNT(DISTINCT o.id) as total_orders,
    COALESCE(SUM(o.total_amount), 0) as total_spent,
    MAX(o.order_date) as last_order_date,
    COUNT(DISTINCT r.id) as total_reviews
FROM Users u
LEFT JOIN Orders o ON u.id = o.user_id
LEFT JOIN Reviews r ON u.id = r.user_id
WHERE u.role = 'CUSTOMER'
GROUP BY u.id;

COMMENT ON VIEW customer_summary IS 'Consolidated view of customer information with purchase history';

-- ====================================================================================
-- VIEW: Voucher summary with usage statistics
-- ====================================================================================
CREATE OR REPLACE VIEW voucher_summary AS
SELECT 
    v.id,
    v.code,
    v.name,
    v.description,
    v.discount_type,
    v.discount_value,
    v.max_discount_amount,
    v.min_order_value,
    v.expiry_date,
    v.max_uses,
    v.uses,
    v.is_active,
    v.created_at,
    CASE 
        WHEN v.expiry_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN v.max_uses IS NOT NULL AND v.uses >= v.max_uses THEN 'USED_UP'
        WHEN v.is_active = FALSE THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END as status,
    CASE 
        WHEN v.max_uses IS NULL THEN NULL
        ELSE ROUND((v.uses::NUMERIC / v.max_uses::NUMERIC) * 100, 2)
    END as usage_percentage,
    COUNT(DISTINCT o.id) as times_used_in_orders,
    COALESCE(SUM(o.total_amount), 0) as total_order_value
FROM Vouchers v
LEFT JOIN Orders o ON v.id = o.voucher_id
GROUP BY v.id;

COMMENT ON VIEW voucher_summary IS 'Consolidated view of voucher information with usage statistics';

-- ====================================================================================
-- Sample Data Updates (Optional - for testing)
-- ====================================================================================

-- Update existing users to set is_active to TRUE
UPDATE Users SET is_active = TRUE WHERE is_active IS NULL;

-- Example: Create a sample employee (uncomment to use)
-- INSERT INTO Users (firstname, lastname, email, password, phone, role, position, department, salary_per_hour, created_at)
-- VALUES ('Nguyễn', 'Văn A', 'nvana@starshop.vn', '$2a$10$...', '0901234567', 'STAFF', 
--         'Nhân viên bán hàng', 'Kinh doanh', 50000, CURRENT_TIMESTAMP);

-- ====================================================================================
-- Grant necessary permissions
-- ====================================================================================

-- If you have specific database users, grant them access
-- GRANT SELECT, INSERT, UPDATE ON Users, Vouchers, TimeSheets, Salaries TO your_app_user;
-- GRANT EXECUTE ON FUNCTION generate_employee_code() TO your_app_user;
-- GRANT EXECUTE ON FUNCTION update_last_login(BIGINT) TO your_app_user;
-- GRANT EXECUTE ON PROCEDURE generate_monthly_salaries(DATE) TO your_app_user;

-- ====================================================================================
-- End of Script
-- ====================================================================================

