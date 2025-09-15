-- Sample Data for Flower Shop System
-- This script populates the database with realistic sample data

-- Insert Users (Admin, Staff, and Customers)
INSERT INTO Users (firstname, lastname, email, password, phone, role) VALUES
-- Admin account
('Nguyễn', 'Admin', 'admin@flowershop.vn', '$2a$10$5uP.example', '0901234567', 'admin'),

-- Staff accounts
('Trần', 'Thị Hoa', 'hoa.tran@flowershop.vn', '$2a$10$5uP.example', '0912345678', 'staff'),
('Lê', 'Văn Nam', 'nam.le@flowershop.vn', '$2a$10$5uP.example', '0923456789', 'staff'),

-- Customer accounts
('Phạm', 'Thị Mai', 'mai.pham@gmail.com', '$2a$10$5uP.example', '0934567890', 'customer'),
('Hoàng', 'Văn Đức', 'duc.hoang@gmail.com', '$2a$10$5uP.example', '0945678901', 'customer'),
('Võ', 'Thị Lan', 'lan.vo@gmail.com', '$2a$10$5uP.example', '0956789012', 'customer'),
('Đặng', 'Văn Hùng', 'hung.dang@gmail.com', '$2a$10$5uP.example', '0967890123', 'customer'),
('Bùi', 'Thị Hương', 'huong.bui@gmail.com', '$2a$10$5uP.example', '0978901234', 'customer');

-- Insert Addresses
INSERT INTO Addresses (user_id, street, city, province, is_default) VALUES
-- Admin address
(1, '123 Nguyễn Huệ', 'Quận 1', 'TP. Hồ Chí Minh', true),

-- Staff addresses
(2, '456 Lê Lợi', 'Quận 3', 'TP. Hồ Chí Minh', true),
(3, '789 Hai Bà Trưng', 'Quận 1', 'TP. Hồ Chí Minh', true),

-- Customer addresses
(4, '321 Võ Văn Tần', 'Quận 3', 'TP. Hồ Chí Minh', true),
(4, '654 Nam Kỳ Khởi Nghĩa', 'Quận 3', 'TP. Hồ Chí Minh', false),
(5, '987 Điện Biên Phủ', 'Quận Bình Thạnh', 'TP. Hồ Chí Minh', true),
(6, '147 Nguyễn Thị Minh Khai', 'Quận 1', 'TP. Hồ Chí Minh', true),
(7, '258 Cách Mạng Tháng 8', 'Quận 10', 'TP. Hồ Chí Minh', true),
(8, '369 Lý Thường Kiệt', 'Quận 10', 'TP. Hồ Chí Minh', true);

-- Insert Products (Various flower types)
INSERT INTO Products (name, description, price, image) VALUES
-- Roses
('Hoa Hồng Đỏ', 'Bó hoa hồng đỏ tươi 20 cành, tượng trưng cho tình yêu nồng cháy', 450000, 'rose-red-20.jpg'),
('Hoa Hồng Trắng', 'Bó hoa hồng trắng tinh khôi 15 cành, thích hợp cho các dịp cưới hỏi', 380000, 'rose-white-15.jpg'),
('Hoa Hồng Vàng', 'Bó hoa hồng vàng 10 cành, biểu tượng của tình bạn', 280000, 'rose-yellow-10.jpg'),

-- Tulips
('Hoa Tulip Hà Lan', 'Bó hoa tulip nhập khẩu từ Hà Lan, 12 cành nhiều màu', 580000, 'tulip-mixed-12.jpg'),
('Hoa Tulip Đỏ', 'Bó hoa tulip đỏ rực rỡ 10 cành', 480000, 'tulip-red-10.jpg'),

-- Orchids
('Lan Hồ Điệp Trắng', 'Chậu lan hồ điệp trắng 3 cành, sang trọng', 850000, 'orchid-white-3.jpg'),
('Lan Hồ Điệp Tím', 'Chậu lan hồ điệp tím 2 cành, độc đáo', 680000, 'orchid-purple-2.jpg'),

-- Lilies
('Hoa Ly Trắng', 'Bó hoa ly trắng thơm ngát 5 cành', 320000, 'lily-white-5.jpg'),
('Hoa Ly Hồng', 'Bó hoa ly hồng dịu dàng 7 cành', 420000, 'lily-pink-7.jpg'),

-- Mixed bouquets
('Bó Hoa Sinh Nhật', 'Bó hoa hỗn hợp nhiều màu sắc cho sinh nhật', 550000, 'birthday-mixed.jpg'),
('Bó Hoa Khai Trương', 'Bó hoa chúc mừng khai trương, gồm hồng và ly', 780000, 'opening-mixed.jpg'),
('Giỏ Hoa Tang Lễ', 'Giỏ hoa tang lễ trang nghiêm', 650000, 'funeral-basket.jpg'),

-- Special arrangements
('Hoa Cưới Cầm Tay', 'Bó hoa cưới cầm tay cho cô dâu', 980000, 'wedding-bouquet.jpg'),
('Hộp Hoa Hồng', 'Hộp hoa hồng cao cấp 30 bông', 1200000, 'rose-box-30.jpg'),
('Bình Hoa Để Bàn', 'Bình hoa trang trí để bàn', 450000, 'table-vase.jpg');

-- Insert Product Attributes
INSERT INTO ProductAttributes (name) VALUES
('Màu sắc'),
('Kích thước'),
('Kiểu dáng'),
('Xuất xứ');

-- Insert Attribute Values
INSERT INTO AttributeValues (attribute_id, product_id, value) VALUES
-- Colors for roses
(1, 1, 'Đỏ'),
(1, 2, 'Trắng'),
(1, 3, 'Vàng'),

-- Sizes
(2, 1, 'Lớn'),
(2, 2, 'Trung bình'),
(2, 3, 'Nhỏ'),

-- Styles
(3, 10, 'Hiện đại'),
(3, 11, 'Cổ điển'),
(3, 13, 'Sang trọng'),

-- Origins
(4, 4, 'Hà Lan'),
(4, 5, 'Hà Lan'),
(4, 6, 'Đà Lạt'),
(4, 7, 'Đà Lạt');

-- Insert Delivery Units
INSERT INTO DeliveryUnits (name, fee, estimated_time) VALUES
('Giao hàng nhanh', 35000, '2-4 giờ'),
('Giao hàng tiết kiệm', 20000, '1-2 ngày'),
('Giao hàng đặc biệt', 50000, '1-2 giờ'),
('Nhận tại cửa hàng', 0, 'Ngay lập tức');

-- Insert Vouchers
INSERT INTO Vouchers (code, discount_value, discount_type, expiry_date, min_order_value, max_uses) VALUES
('WELCOME10', 10, 'percentage', '2025-12-31', 200000, 1000),
('NEWYEAR2025', 100000, 'fixed', '2025-02-28', 500000, 500),
('VIP20', 20, 'percentage', '2025-12-31', 1000000, 100),
('FREESHIP', 35000, 'fixed', '2025-06-30', 300000, 2000),
('BIRTHDAY15', 15, 'percentage', '2025-12-31', 0, NULL);

-- Insert Carts for customers
INSERT INTO Carts (user_id) VALUES
(4), (5), (6), (7), (8);

-- Insert Cart Items (some customers have items in cart)
INSERT INTO CartItems (cart_id, product_id, quantity) VALUES
(1, 1, 1),  -- Customer 4 has red roses in cart
(1, 10, 1), -- and birthday bouquet
(2, 6, 1),  -- Customer 5 has white orchid
(3, 13, 2); -- Customer 6 has 2 wedding bouquets

-- Insert Orders
INSERT INTO Orders (user_id, total_amount, status, delivery_unit_id, voucher_id, address_id, payment_method) VALUES
-- Completed orders
(4, 815000, 'completed', 1, 1, 4, 'credit_card'), -- Used WELCOME10 voucher
(5, 680000, 'completed', 2, NULL, 6, 'bank_transfer'),
(6, 1960000, 'completed', 3, NULL, 7, 'credit_card'),

-- Processing order
(7, 320000, 'processing', 1, NULL, 8, 'cash_on_delivery'),

-- Pending order
(8, 1200000, 'pending', 1, NULL, 9, 'wallet'),

-- Shipped order
(4, 550000, 'shipped', 2, NULL, 5, 'credit_card');

-- Insert Order Items
INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES
-- Order 1 items
(1, 1, 1, 450000),
(1, 10, 1, 550000),

-- Order 2 items
(2, 6, 1, 850000),

-- Order 3 items
(3, 13, 2, 980000),

-- Order 4 items
(4, 8, 1, 320000),

-- Order 5 items
(5, 14, 1, 1200000),

-- Order 6 items
(6, 10, 1, 550000);

-- Insert Reviews
INSERT INTO Reviews (user_id, product_id, rating, comment) VALUES
(4, 1, 5, 'Hoa rất tươi và đẹp, giao hàng nhanh chóng!'),
(4, 10, 4, 'Bó hoa đẹp nhưng hơi nhỏ so với hình ảnh'),
(5, 6, 5, 'Lan rất đẹp và tươi lâu, đóng gói cẩn thận'),
(6, 13, 5, 'Hoàn hảo cho ngày cưới của tôi! Cảm ơn shop'),
(7, 8, 4, 'Hoa ly thơm ngát, màu sắc tươi sáng'),
(8, 14, 5, 'Hộp hoa sang trọng, người nhận rất thích!');

-- Insert Follows
INSERT INTO Follows (user_id, product_id) VALUES
(4, 1), (4, 2), (4, 10),
(5, 6), (5, 7),
(6, 13), (6, 14),
(7, 8), (7, 9),
(8, 14), (8, 15);

-- Insert Messages (Customer service conversations)
INSERT INTO Messages (sender_id, receiver_id, content, is_read) VALUES
(4, 2, 'Cho em hỏi hoa hồng đỏ còn hàng không ạ?', true),
(2, 4, 'Dạ còn hàng ạ. Anh chị cần đặt bao nhiêu bó ạ?', true),
(4, 2, 'Em cần 2 bó, giao trong ngày được không?', true),
(2, 4, 'Dạ được ạ. Anh chị vui lòng đặt hàng trên website nhé!', false),

(5, 3, 'Shop có nhận thiết kế hoa theo yêu cầu không?', true),
(3, 5, 'Dạ có ạ. Chị có thể mô tả yêu cầu cụ thể được không ạ?', false);

-- Insert Transactions
INSERT INTO Transactions (user_id, order_id, amount, type, status) VALUES
(4, 1, 815000, 'payment', 'success'),
(5, 2, 680000, 'payment', 'success'),
(6, 3, 1960000, 'payment', 'success'),
(4, 6, 550000, 'payment', 'success');

-- Insert TimeSheets (for staff)
INSERT INTO TimeSheets (staff_id, check_in, check_out, date, hours_worked) VALUES
-- Staff 2 timesheets
(2, '2025-09-10 08:00:00', '2025-09-10 17:30:00', '2025-09-10', 9.5),
(2, '2025-09-11 08:15:00', '2025-09-11 17:45:00', '2025-09-11', 9.5),
(2, '2025-09-12 08:00:00', '2025-09-12 17:00:00', '2025-09-12', 9.0),

-- Staff 3 timesheets
(3, '2025-09-10 09:00:00', '2025-09-10 18:00:00', '2025-09-10', 9.0),
(3, '2025-09-11 09:00:00', '2025-09-11 18:30:00', '2025-09-11', 9.5),
(3, '2025-09-12 09:00:00', NULL, '2025-09-12', 0.0); -- Currently working

-- Display summary
SELECT 'Database populated with sample data!' as message;
SELECT 'Users: ' || COUNT(*) FROM Users;
SELECT 'Products: ' || COUNT(*) FROM Products;
SELECT 'Orders: ' || COUNT(*) FROM Orders;
