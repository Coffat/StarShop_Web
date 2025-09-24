-- Flower Shop System Database Schema
-- Version: 1.0
-- Database: PostgreSQL 15

-- Drop existing types if re-running (optional)
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS order_status CASCADE;
DROP TYPE IF EXISTS discount_type CASCADE;
DROP TYPE IF EXISTS payment_method CASCADE;
DROP TYPE IF EXISTS transaction_type CASCADE;
DROP TYPE IF EXISTS transaction_status CASCADE;
DROP TYPE IF EXISTS product_status CASCADE;

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('customer', 'staff', 'admin');
CREATE TYPE order_status AS ENUM ('pending', 'processing', 'shipped', 'completed', 'cancelled');
CREATE TYPE discount_type AS ENUM ('percentage', 'fixed');
CREATE TYPE payment_method AS ENUM ('cash_on_delivery', 'credit_card', 'bank_transfer', 'wallet');
CREATE TYPE transaction_type AS ENUM ('payment', 'refund');
CREATE TYPE transaction_status AS ENUM ('success', 'failed');
CREATE TYPE product_status AS ENUM ('active', 'inactive', 'out_of_stock', 'discontinued');

-- Table: Users
CREATE TABLE Users (
    id BIGSERIAL PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    cover VARCHAR(255) DEFAULT NULL,
    role user_role NOT NULL DEFAULT 'customer',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CHECK (LENGTH(password) >= 8)
);

COMMENT ON TABLE Users IS 'Stores user information including customers, staff, and admins';
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_users_phone ON Users(phone);

-- Table: Addresses
CREATE TABLE Addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

COMMENT ON TABLE Addresses IS 'User delivery addresses';
CREATE INDEX idx_addresses_user_id ON Addresses(user_id);

-- Table: Products
CREATE TABLE Products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    image VARCHAR(255) DEFAULT NULL,
    stock_quantity INTEGER DEFAULT 0,
    status product_status DEFAULT 'active', -- Tạm thời cho phép NULL
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CHECK (price >= 0),
    CHECK (stock_quantity >= 0)
);

COMMENT ON TABLE Products IS 'Flower products catalog';
CREATE INDEX idx_products_name ON Products(name);

-- Note: Timestamp management is now handled by JPA Auditing
-- Removed database triggers and constraints for updated_at columns

-- Table: ProductAttributes
CREATE TABLE ProductAttributes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Thêm cột created_at
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ProductAttributes IS 'Product attribute types (e.g., color, size)';
CREATE INDEX idx_productattributes_name ON ProductAttributes(name);

-- Table: AttributeValues
CREATE TABLE AttributeValues (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (attribute_id) REFERENCES ProductAttributes(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    UNIQUE (attribute_id, product_id, value)
);

COMMENT ON TABLE AttributeValues IS 'Specific values for product attributes';
CREATE UNIQUE INDEX idx_attributevalues_unique ON AttributeValues(attribute_id, product_id, value);

-- Table: DeliveryUnits (Moved up to resolve foreign key dependency)
CREATE TABLE DeliveryUnits (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    fee NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    estimated_time VARCHAR(50) DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CHECK (fee >= 0)
);

COMMENT ON TABLE DeliveryUnits IS 'Delivery service providers';

-- Table: Vouchers (Moved up to resolve foreign key dependency)
CREATE TABLE Vouchers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_value NUMERIC(10,2) NOT NULL,
    discount_type discount_type NOT NULL,
    expiry_date DATE NOT NULL,
    min_order_value NUMERIC(10,2) DEFAULT 0.00,
    max_uses INTEGER DEFAULT NULL,
    uses INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CHECK (discount_value > 0),
    CHECK (max_uses IS NULL OR uses <= max_uses)
);

COMMENT ON TABLE Vouchers IS 'Discount vouchers';
CREATE INDEX idx_vouchers_code ON Vouchers(code);

-- Table: Orders
CREATE TABLE Orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    status order_status NOT NULL DEFAULT 'pending',
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_unit_id BIGINT DEFAULT NULL,
    voucher_id BIGINT DEFAULT NULL,
    address_id BIGINT NOT NULL,
    payment_method payment_method NOT NULL DEFAULT 'cash_on_delivery',
    notes TEXT DEFAULT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (delivery_unit_id) REFERENCES DeliveryUnits(id),
    FOREIGN KEY (voucher_id) REFERENCES Vouchers(id),
    FOREIGN KEY (address_id) REFERENCES Addresses(id),
    CHECK (total_amount >= 0)
);

COMMENT ON TABLE Orders IS 'Customer orders with status and payment info';
CREATE INDEX idx_orders_user_id ON Orders(user_id);
CREATE INDEX idx_orders_status ON Orders(status);
CREATE INDEX idx_orders_user_status ON Orders(user_id, status);

-- Table: OrderItems
CREATE TABLE OrderItems (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    price NUMERIC(10,2) NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(id),
    CHECK (quantity > 0)
);

COMMENT ON TABLE OrderItems IS 'Items within an order';
CREATE INDEX idx_orderitems_order_id ON OrderItems(order_id);

-- Table: Carts
CREATE TABLE Carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    total_amount NUMERIC(10,2) DEFAULT 0.00, -- Tạm thời cho phép NULL
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CHECK (total_amount >= 0)
);

COMMENT ON TABLE Carts IS 'User shopping carts';

-- Table: CartItems
CREATE TABLE CartItems (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES Carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(id),
    CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);

COMMENT ON TABLE CartItems IS 'Items in user carts';
CREATE UNIQUE INDEX idx_cartitems_unique ON CartItems(cart_id, product_id);

-- Table: Reviews
CREATE TABLE Reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating SMALLINT NOT NULL,
    comment TEXT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    CHECK (rating BETWEEN 1 AND 5)
);

COMMENT ON TABLE Reviews IS 'Product reviews and ratings';
CREATE INDEX idx_reviews_product_id ON Reviews(product_id);

-- Table: Follows
CREATE TABLE Follows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    followed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

COMMENT ON TABLE Follows IS 'User follows on products';
CREATE UNIQUE INDEX idx_follows_unique ON Follows(user_id, product_id);

-- Table: Messages
CREATE TABLE Messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES Users(id),
    FOREIGN KEY (receiver_id) REFERENCES Users(id)
);

COMMENT ON TABLE Messages IS 'User-to-user messaging';
CREATE INDEX idx_messages_conversation ON Messages(sender_id, receiver_id, sent_at);
CREATE INDEX idx_messages_sent_at ON Messages(sent_at);

-- Table: Transactions
CREATE TABLE Transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT DEFAULT NULL,
    amount NUMERIC(10,2) NOT NULL,
    type transaction_type NOT NULL,
    status transaction_status NOT NULL DEFAULT 'failed',
    transaction_reference VARCHAR(255) DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (order_id) REFERENCES Orders(id),
    CHECK (amount > 0)
);

COMMENT ON TABLE Transactions IS 'Payment/refund transactions';
CREATE INDEX idx_transactions_order_id ON Transactions(order_id);
CREATE INDEX idx_transactions_created_at ON Transactions(created_at);

-- Table: TimeSheets
CREATE TABLE TimeSheets (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP DEFAULT NULL,
    date DATE NOT NULL,
    hours_worked NUMERIC(5,2) DEFAULT 0.00,
    notes TEXT DEFAULT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES Users(id),
    CHECK (hours_worked >= 0),
    CHECK (check_out IS NULL OR check_out > check_in)
);

COMMENT ON TABLE TimeSheets IS 'Staff work hours tracking';
CREATE INDEX idx_timesheets_staff_date ON TimeSheets(staff_id, date);

-- Additional performance indexes
CREATE INDEX idx_follows_product_id ON Follows(product_id);
CREATE INDEX idx_products_stock_quantity ON Products(stock_quantity);
CREATE INDEX idx_vouchers_is_active ON Vouchers(is_active);
CREATE INDEX idx_deliveryunits_is_active ON DeliveryUnits(is_active);
CREATE INDEX idx_transactions_transaction_reference ON Transactions(transaction_reference);

-- ========================================
-- SAMPLE DATA INSERTION
-- ========================================

-- Insert sample users
INSERT INTO Users (firstname, lastname, email, password, phone, role, created_at) VALUES
('Admin', 'User', 'admin@flowershop.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456789', 'admin', CURRENT_TIMESTAMP),
('Bob', 'Wilson', 'bob.wilson@flowershop.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456790', 'staff', CURRENT_TIMESTAMP),
('Alice', 'Johnson', 'alice.johnson@flowershop.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456791', 'staff', CURRENT_TIMESTAMP),
('John', 'Doe', 'john.doe@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456792', 'customer', CURRENT_TIMESTAMP),
('Jane', 'Smith', 'jane.smith@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456793', 'customer', CURRENT_TIMESTAMP),
('Mike', 'Brown', 'mike.brown@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456794', 'customer', CURRENT_TIMESTAMP);

-- Insert sample addresses
INSERT INTO Addresses (user_id, street, city, province, is_default, created_at) VALUES
(4, '123 Main Street', 'Ho Chi Minh City', 'Ho Chi Minh', true, CURRENT_TIMESTAMP),
(4, '456 Oak Avenue', 'Ho Chi Minh City', 'Ho Chi Minh', false, CURRENT_TIMESTAMP),
(5, '789 Pine Road', 'Hanoi', 'Hanoi', true, CURRENT_TIMESTAMP),
(6, '321 Elm Street', 'Da Nang', 'Da Nang', true, CURRENT_TIMESTAMP);

-- Insert sample product attributes
INSERT INTO ProductAttributes (name, created_at, updated_at) VALUES
('Color', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Size', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fragrance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Occasion', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Season', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample products with stock quantities and status
INSERT INTO Products (name, description, price, image, stock_quantity, status, created_at) VALUES
('Red Roses Bouquet', 'Beautiful red roses arranged in an elegant bouquet', 299000, '/images/products/red-roses.jpg', 50, 'active', CURRENT_TIMESTAMP),
('White Lilies', 'Pure white lilies perfect for any occasion', 199000, '/images/products/white-lilies.jpg', 30, 'active', CURRENT_TIMESTAMP),
('Sunflower Arrangement', 'Bright and cheerful sunflowers', 249000, '/images/products/sunflowers.jpg', 25, 'active', CURRENT_TIMESTAMP),
('Tulip Mix', 'Colorful mix of tulips in various colors', 179000, '/images/products/tulips.jpg', 40, 'active', CURRENT_TIMESTAMP),
('Orchid Plant', 'Exotic orchid plant in decorative pot', 399000, '/images/products/orchid.jpg', 15, 'active', CURRENT_TIMESTAMP),
('Carnation Bouquet', 'Sweet carnations in pink and white', 159000, '/images/products/carnations.jpg', 35, 'active', CURRENT_TIMESTAMP),
('Mixed Spring Flowers', 'Fresh spring flowers in a beautiful arrangement', 229000, '/images/products/spring-mix.jpg', 20, 'active', CURRENT_TIMESTAMP),
('Romantic Rose Box', 'Premium roses in an elegant gift box', 499000, '/images/products/rose-box.jpg', 10, 'active', CURRENT_TIMESTAMP),
('Baby Breath Bouquet', 'Delicate baby breath flowers', 129000, '/images/products/baby-breath.jpg', 45, 'active', CURRENT_TIMESTAMP),
('Wedding Bouquet', 'Elegant white and cream wedding bouquet', 599000, '/images/products/wedding-bouquet.jpg', 0, 'out_of_stock', CURRENT_TIMESTAMP);

-- Insert sample attribute values
INSERT INTO AttributeValues (attribute_id, product_id, value, created_at) VALUES
(1, 1, 'Red', CURRENT_TIMESTAMP),
(2, 1, 'Large', CURRENT_TIMESTAMP),
(3, 1, 'Rose', CURRENT_TIMESTAMP),
(4, 1, 'Romance', CURRENT_TIMESTAMP),
(1, 2, 'White', CURRENT_TIMESTAMP),
(2, 2, 'Medium', CURRENT_TIMESTAMP),
(3, 2, 'Lily', CURRENT_TIMESTAMP),
(4, 2, 'Sympathy', CURRENT_TIMESTAMP),
(1, 3, 'Yellow', CURRENT_TIMESTAMP),
(2, 3, 'Large', CURRENT_TIMESTAMP),
(3, 3, 'Sunflower', CURRENT_TIMESTAMP),
(4, 3, 'Celebration', CURRENT_TIMESTAMP);

-- Insert sample delivery units with is_active status
INSERT INTO DeliveryUnits (name, fee, estimated_time, is_active, created_at) VALUES
('Standard Delivery', 30000, '2-3 business days', true, CURRENT_TIMESTAMP),
('Express Delivery', 50000, '1 business day', true, CURRENT_TIMESTAMP),
('Same Day Delivery', 80000, 'Same day', true, CURRENT_TIMESTAMP),
('Premium Delivery', 100000, '2-4 hours', false, CURRENT_TIMESTAMP);

-- Insert sample vouchers with is_active status
INSERT INTO Vouchers (code, discount_value, discount_type, expiry_date, min_order_value, max_uses, uses, is_active, created_at) VALUES
('WELCOME10', 10, 'percentage', '2024-12-31', 200000, 100, 5, true, CURRENT_TIMESTAMP),
('SAVE50K', 50000, 'fixed', '2024-12-31', 300000, 50, 2, true, CURRENT_TIMESTAMP),
('NEWUSER', 15, 'percentage', '2024-12-31', 150000, 200, 10, true, CURRENT_TIMESTAMP),
('EXPIRED20', 20, 'percentage', '2023-12-31', 100000, 10, 0, false, CURRENT_TIMESTAMP);

-- Insert sample carts with total amounts
INSERT INTO Carts (user_id, total_amount, created_at) VALUES
(4, 847000, CURRENT_TIMESTAMP),
(5, 199000, CURRENT_TIMESTAMP),
(6, 537000, CURRENT_TIMESTAMP);

-- Insert sample cart items
INSERT INTO CartItems (cart_id, product_id, quantity, created_at) VALUES
(1, 1, 2, CURRENT_TIMESTAMP),
(1, 3, 1, CURRENT_TIMESTAMP),
(2, 2, 1, CURRENT_TIMESTAMP),
(3, 4, 3, CURRENT_TIMESTAMP);

-- Insert sample orders (to ensure foreign key consistency)
INSERT INTO Orders (user_id, total_amount, status, order_date, delivery_unit_id, voucher_id, address_id, payment_method, notes) VALUES
(4, 628000, 'completed', CURRENT_TIMESTAMP - INTERVAL '5 days', 1, 1, 1, 'credit_card', 'Please deliver after 2 PM'),
(5, 199000, 'shipped', CURRENT_TIMESTAMP - INTERVAL '3 days', 2, NULL, 3, 'bank_transfer', 'Handle with care'),
(6, 537000, 'processing', CURRENT_TIMESTAMP - INTERVAL '1 day', 1, 2, 4, 'cash_on_delivery', 'Call before delivery'),
(4, 299000, 'pending', CURRENT_TIMESTAMP, 1, NULL, 1, 'credit_card', NULL);

-- Insert sample order items (with valid order_id references)
INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES
(1, 1, 2, 299000),
(1, 3, 1, 249000),
(2, 2, 1, 199000),
(3, 4, 3, 179000),
(4, 1, 1, 299000);

-- Insert sample transactions (with valid order_id references)
INSERT INTO Transactions (user_id, order_id, amount, type, status, transaction_reference, notes, created_at) VALUES
(4, 1, 628000, 'payment', 'success', 'TXN_001_2024', 'Payment successful', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(5, 2, 199000, 'payment', 'success', 'TXN_002_2024', 'Bank transfer completed', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(6, 3, 537000, 'payment', 'success', 'TXN_003_2024', 'Cash on delivery', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(4, 4, 299000, 'payment', 'failed', 'TXN_004_2024', 'Payment failed - insufficient funds', CURRENT_TIMESTAMP);

-- Insert sample reviews
INSERT INTO Reviews (user_id, product_id, rating, comment, created_at) VALUES
(4, 1, 5, 'Beautiful roses, exactly as described!', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(5, 2, 4, 'Lovely lilies, very fresh', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(6, 3, 5, 'Amazing sunflowers, bright and cheerful', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(4, 4, 3, 'Good quality tulips, but delivery was late', CURRENT_TIMESTAMP - INTERVAL '3 days');

-- Insert sample follows (wishlist)
INSERT INTO Follows (user_id, product_id, followed_at) VALUES
(4, 5, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(4, 8, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(5, 1, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(6, 6, CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Insert sample messages
INSERT INTO Messages (sender_id, receiver_id, content, sent_at, is_read) VALUES
(4, 2, 'Hi, I have a question about my order', CURRENT_TIMESTAMP - INTERVAL '2 hours', false),
(2, 4, 'Hello! How can I help you?', CURRENT_TIMESTAMP - INTERVAL '1 hour', true),
(5, 2, 'When will my order be delivered?', CURRENT_TIMESTAMP - INTERVAL '30 minutes', false);

-- Insert sample timesheets
INSERT INTO TimeSheets (staff_id, check_in, check_out, date, hours_worked, notes) VALUES
(2, CURRENT_TIMESTAMP - INTERVAL '8 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_DATE, 7.0, 'Regular work day'),
(3, CURRENT_TIMESTAMP - INTERVAL '7 hours', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_DATE, 6.5, 'Half day shift'),
(2, CURRENT_TIMESTAMP - INTERVAL '1 day' - INTERVAL '8 hours', CURRENT_TIMESTAMP - INTERVAL '1 day' - INTERVAL '1 hour', CURRENT_DATE - INTERVAL '1 day', 7.0, 'Previous day work');

-- ========================================
-- DATA INTEGRITY VERIFICATION
-- ========================================

-- Verify no orphaned data exists
DO $$
BEGIN
    -- Check for orphaned order items
    IF EXISTS (SELECT 1 FROM orderitems oi LEFT JOIN orders o ON oi.order_id = o.id WHERE o.id IS NULL) THEN
        RAISE EXCEPTION 'Found orphaned order items - data integrity check failed';
    END IF;
    
    -- Check for orphaned transactions
    IF EXISTS (SELECT 1 FROM transactions t LEFT JOIN orders o ON t.order_id = o.id WHERE o.id IS NULL AND t.order_id IS NOT NULL) THEN
        RAISE EXCEPTION 'Found orphaned transactions - data integrity check failed';
    END IF;
    
    -- Check for orphaned cart items
    IF EXISTS (SELECT 1 FROM cartitems ci LEFT JOIN carts c ON ci.cart_id = c.id WHERE c.id IS NULL) THEN
        RAISE EXCEPTION 'Found orphaned cart items - data integrity check failed';
    END IF;
    
    RAISE NOTICE 'Data integrity check passed - no orphaned data found';
END $$;
