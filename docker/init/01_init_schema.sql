-- Create ENUM types
CREATE TYPE user_role AS ENUM ('customer', 'staff', 'admin');
CREATE TYPE order_status AS ENUM ('pending', 'processing', 'shipped', 'completed', 'cancelled');
CREATE TYPE discount_type AS ENUM ('percentage', 'fixed');
CREATE TYPE payment_method AS ENUM ('cash_on_delivery', 'credit_card', 'bank_transfer', 'wallet');
CREATE TYPE transaction_type AS ENUM ('payment', 'refund');
CREATE TYPE transaction_status AS ENUM ('success', 'failed');
CREATE TYPE product_status AS ENUM ('active', 'inactive', 'out_of_stock', 'discontinued');
CREATE TYPE salary_status AS ENUM ('pending', 'paid', 'overdue');

-- Table: Users
CREATE TABLE Users (
    id BIGSERIAL PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
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
    status product_status DEFAULT 'active',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CHECK (price >= 0),
    CHECK (stock_quantity >= 0)
);
COMMENT ON TABLE Products IS 'Flower products catalog';
CREATE INDEX idx_products_name ON Products(name);

-- Table: ProductAttributes
CREATE TABLE ProductAttributes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
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

-- Table: DeliveryUnits
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

-- Table: Vouchers
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
    total_amount NUMERIC(10,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    total_quantity INTEGER DEFAULT 0 CHECK (total_quantity >= 0),
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
    order_item_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    FOREIGN KEY (order_item_id) REFERENCES OrderItems(id) ON DELETE CASCADE,
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
    FOREIGN KEY (staff_id) REFERENCES Users(id),
    CHECK (hours_worked >= 0),
    CHECK (check_out IS NULL OR check_out > check_in)
);
COMMENT ON TABLE TimeSheets IS 'Staff work hours tracking';
CREATE INDEX idx_timesheets_staff_date ON TimeSheets(staff_id, date);

-- Table: Salaries
CREATE TABLE Salaries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    base_salary NUMERIC(10,2) NOT NULL CHECK (base_salary >= 0),
    working_time NUMERIC(5,2) NOT NULL DEFAULT 0.00 CHECK (working_time >= 0),
    total_salary NUMERIC(10,2) GENERATED ALWAYS AS (base_salary * working_time) STORED,
    month_year DATE NOT NULL,
    status salary_status NOT NULL DEFAULT 'pending',
    notes TEXT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    UNIQUE (user_id, month_year)
);
COMMENT ON TABLE Salaries IS 'Simplified staff salary records, based on hourly rate and total work hours';
CREATE INDEX idx_salaries_user_id ON Salaries(user_id);
CREATE INDEX idx_salaries_month_year ON Salaries(month_year);

-- Additional performance indexes
CREATE INDEX idx_follows_product_id ON Follows(product_id);
CREATE INDEX idx_products_stock_quantity ON Products(stock_quantity);
CREATE INDEX idx_vouchers_is_active ON Vouchers(is_active);
CREATE INDEX idx_deliveryunits_is_active ON DeliveryUnits(is_active);
CREATE INDEX idx_transactions_transaction_reference ON Transactions(transaction_reference);