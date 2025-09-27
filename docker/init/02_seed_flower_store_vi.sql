
-- =============================================
-- SEED DATA (VIETNAMESE) - FLOWER STORE MANAGEMENT SYSTEM
-- Phiên bản: PostgreSQL
-- Mục tiêu: Tạo dữ liệu mẫu "đủ nhiều" và hợp lý cho môi trường dev/demo
-- Lưu ý: Các ID được gán tường minh để tiện tham chiếu FK (BIGSERIAL vẫn cho phép chỉ định ID)
-- =============================================

BEGIN;

-- ========== 1) USERS ==========
-- 5 khách hàng (1..5), 2 nhân viên (6..7), 1 admin (8)
INSERT INTO Users(firstname, lastname, email, password, phone, avatar, role, created_at)
VALUES
('Nguyễn', 'An', 'an.nguyen@example.com', 'an12345678', '0901000001', NULL, 'customer', NOW() - INTERVAL '60 days'),
('Trần', 'Bình', 'binh.tran@example.com', 'binh123456', '0901000002', NULL, 'customer', NOW() - INTERVAL '58 days'),
('Lê', 'Chi', 'chi.le@example.com', 'chi12345678', '0901000003', NULL, 'customer', NOW() - INTERVAL '40 days'),
('Phạm', 'Dũng', 'dung.pham@example.com', 'dung123456', '0901000004', NULL, 'customer', NOW() - INTERVAL '35 days'),
('Võ', 'Hà', 'ha.vo@example.com', 'ha12345678', '0901000005', NULL, 'customer', NOW() - INTERVAL '30 days'),
('Hoàng', 'Lan', 'lan.hoang@example.com', 'lan12345678', '0901000006', NULL, 'staff', NOW() - INTERVAL '90 days'),
('Đỗ', 'Minh', 'minh.do@example.com', 'minh123456', '0901000007', NULL, 'staff', NOW() - INTERVAL '90 days'),
('Admin', 'Root', 'admin@example.com', 'admin123456', '0901000008', NULL, 'admin', NOW() - INTERVAL '100 days');

-- ========== 2) ADDRESSES ==========
-- Mỗi khách hàng 2 địa chỉ, 1 địa chỉ mặc định
INSERT INTO Addresses(id, user_id, street, city, province, is_default, created_at)
VALUES
(1, 1, '12 Nguyễn Trãi', 'Quận 5', 'TP.HCM', TRUE, NOW() - INTERVAL '55 days'),
(2, 1, '88 Lý Thường Kiệt', 'Quận 10', 'TP.HCM', FALSE, NOW() - INTERVAL '50 days'),

(3, 2, '25 Trần Hưng Đạo', 'Quận 1', 'TP.HCM', TRUE, NOW() - INTERVAL '55 days'),
(4, 2, '101 Võ Văn Kiệt', 'Quận 1', 'TP.HCM', FALSE, NOW() - INTERVAL '50 days'),

(5, 3, '7 Nguyễn Huệ', 'Quận 1', 'TP.HCM', TRUE, NOW() - INTERVAL '38 days'),
(6, 3, '45 Phan Xích Long', 'Phú Nhuận', 'TP.HCM', FALSE, NOW() - INTERVAL '36 days'),

(7, 4, '19 Hai Bà Trưng', 'Quận 3', 'TP.HCM', TRUE, NOW() - INTERVAL '33 days'),
(8, 4, '200 CMT8', 'Quận 10', 'TP.HCM', FALSE, NOW() - INTERVAL '31 days'),

(9, 5, '66 Điện Biên Phủ', 'Bình Thạnh', 'TP.HCM', TRUE, NOW() - INTERVAL '28 days'),
(10, 5, '15 Hoàng Hoa Thám', 'Tân Bình', 'TP.HCM', FALSE, NOW() - INTERVAL '27 days');

-- ========== 3) DELIVERY UNITS ==========
INSERT INTO DeliveryUnits(id, name, fee, estimated_time, is_active, created_at)
VALUES
(1, 'Giao Nhanh 2H', 25000, '2-4 giờ nội thành', TRUE, NOW() - INTERVAL '80 days'),
(2, 'GHN', 30000, '1-2 ngày', TRUE, NOW() - INTERVAL '80 days'),
(3, 'Viettel Post', 35000, '2-3 ngày', TRUE, NOW() - INTERVAL '80 days');

-- ========== 4) PRODUCT ATTRIBUTES ==========
INSERT INTO ProductAttributes(id, name, created_at)
VALUES
(1, 'Màu sắc', NOW() - INTERVAL '70 days'),
(2, 'Kích thước', NOW() - INTERVAL '70 days'),
(3, 'Chủ đề', NOW() - INTERVAL '70 days');

-- ========== 5) PRODUCTS ==========
INSERT INTO Products(id, name, description, price, image, stock_quantity, status, created_at)
VALUES
(1,  'Bó hồng đỏ Classic', 'Bó 12 bông hồng đỏ giấy kraft', 450000, NULL, 50, 'active', NOW() - INTERVAL '60 days'),
(2,  'Bó hồng phấn Sweetie', 'Hồng phấn baby kèm thiệp', 520000, NULL, 40, 'active', NOW() - INTERVAL '59 days'),
(3,  'Giỏ hoa hướng dương', 'Giỏ 8 bông hướng dương tươi', 600000, NULL, 35, 'active', NOW() - INTERVAL '58 days'),
(4,  'Bình tulip Hà Lan', '10 tulip phối nơ', 750000, NULL, 30, 'active', NOW() - INTERVAL '57 days'),
(5,  'Lan hồ điệp 3 cành', 'Chậu lan sang trọng', 1200000, NULL, 20, 'active', NOW() - INTERVAL '56 days'),
(6,  'Bó cẩm chướng Pastel', 'Tone pastel nhẹ nhàng', 390000, NULL, 60, 'active', NOW() - INTERVAL '55 days'),
(7,  'Bó baby trắng', 'Baby trắng full bó', 350000, NULL, 70, 'active', NOW() - INTERVAL '54 days'),
(8,  'Bó mix hoa theo mùa', 'Mix nhiều loài theo mùa', 480000, NULL, 45, 'active', NOW() - INTERVAL '53 days'),
(9,  'Giỏ hoa trái cây', 'Hoa + trái cây nhập', 980000, NULL, 15, 'active', NOW() - INTERVAL '52 days'),
(10, 'Hoa cưới cô dâu', 'Bó cưới tone trắng kem', 850000, NULL, 12, 'active', NOW() - INTERVAL '51 days'),
(11, 'Vòng hoa chia buồn', 'Trang trọng, lịch sự', 1500000, NULL, 10, 'active', NOW() - INTERVAL '50 days'),
(12, 'Hoa mừng khai trương', 'Kệ hoa chúc mừng', 1800000, NULL, 8, 'active', NOW() - INTERVAL '49 days');

-- ========== 6) ATTRIBUTE VALUES (gắn với sản phẩm) ==========
-- Màu sắc
INSERT INTO AttributeValues(id, attribute_id, product_id, value, created_at) VALUES
(1, 1, 1, 'Đỏ', NOW() - INTERVAL '60 days'),
(2, 1, 2, 'Hồng phấn', NOW() - INTERVAL '59 days'),
(3, 1, 3, 'Vàng', NOW() - INTERVAL '58 days'),
(4, 1, 6, 'Pastel', NOW() - INTERVAL '55 days'),
(5, 1, 7, 'Trắng', NOW() - INTERVAL '54 days'),
(6, 1, 8, 'Đa sắc', NOW() - INTERVAL '53 days'),
(7, 1, 10, 'Trắng kem', NOW() - INTERVAL '51 days');

-- Kích thước
INSERT INTO AttributeValues(id, attribute_id, product_id, value, created_at) VALUES
(21, 2, 1, 'Vừa', NOW() - INTERVAL '60 days'),
(22, 2, 2, 'Vừa', NOW() - INTERVAL '59 days'),
(23, 2, 3, 'Lớn', NOW() - INTERVAL '58 days'),
(24, 2, 4, 'Vừa', NOW() - INTERVAL '57 days'),
(25, 2, 5, 'Lớn', NOW() - INTERVAL '56 days'),
(26, 2, 6, 'Nhỏ', NOW() - INTERVAL '55 days'),
(27, 2, 7, 'Vừa', NOW() - INTERVAL '54 days'),
(28, 2, 8, 'Vừa', NOW() - INTERVAL '53 days'),
(29, 2, 9, 'Lớn', NOW() - INTERVAL '52 days'),
(30, 2, 10, 'Nhỏ', NOW() - INTERVAL '51 days');

-- Chủ đề
INSERT INTO AttributeValues(id, attribute_id, product_id, value, created_at) VALUES
(41, 3, 1, 'Tình yêu', NOW() - INTERVAL '60 days'),
(42, 3, 3, 'Năng lượng', NOW() - INTERVAL '58 days'),
(43, 3, 5, 'Sang trọng', NOW() - INTERVAL '56 days'),
(44, 3, 9, 'Chăm sóc', NOW() - INTERVAL '52 days'),
(45, 3, 12, 'Khai trương', NOW() - INTERVAL '49 days');

-- ========== 7) VOUCHERS ==========
INSERT INTO Vouchers(id, code, discount_value, discount_type, expiry_date, min_order_value, max_uses, uses, is_active, created_at)
VALUES
(1, 'CHAOHE10', 10, 'percentage', CURRENT_DATE + INTERVAL '60 days', 300000, 200, 35, TRUE, NOW() - INTERVAL '40 days'),
(2, 'FREESHIP25K', 25000, 'fixed', CURRENT_DATE + INTERVAL '30 days', 200000, 500, 120, TRUE, NOW() - INTERVAL '35 days'),
(3, 'VIP100K', 100000, 'fixed', CURRENT_DATE + INTERVAL '90 days', 700000, 50, 5, TRUE, NOW() - INTERVAL '20 days');

-- ========== 8) CARTS & CART ITEMS ==========
INSERT INTO Carts(id, user_id, total_amount, total_quantity, created_at)
VALUES
(1, 1, 930000, 2, NOW() - INTERVAL '5 days'),
(2, 2, 350000, 1, NOW() - INTERVAL '4 days'),
(3, 3, 1200000, 1, NOW() - INTERVAL '3 days'),
(4, 4, 870000, 2, NOW() - INTERVAL '2 days'),
(5, 5, 480000, 1, NOW() - INTERVAL '1 day');

INSERT INTO CartItems(id, cart_id, product_id, quantity, created_at)
VALUES
(1, 1, 1, 1, NOW() - INTERVAL '5 days'),
(2, 1, 8, 1, NOW() - INTERVAL '5 days'),
(3, 2, 7, 1, NOW() - INTERVAL '4 days'),
(4, 3, 5, 1, NOW() - INTERVAL '3 days'),
(5, 4, 6, 1, NOW() - INTERVAL '2 days'),
(6, 4, 10, 1, NOW() - INTERVAL '2 days'),
(7, 5, 8, 1, NOW() - INTERVAL '1 day');

-- ========== 9) ORDERS ==========
-- Tạo đơn hàng với nhiều trạng thái, có/không dùng voucher, đơn vị giao hàng, địa chỉ
INSERT INTO Orders(id, user_id, total_amount, status, order_date, delivery_unit_id, voucher_id, address_id, payment_method, notes)
VALUES
(1, 1, 450000, 'completed', NOW() - INTERVAL '25 days', 1, NULL, 1, 'cash_on_delivery', 'Giao giờ trưa'),
(2, 1, 970000, 'completed', NOW() - INTERVAL '20 days', 2, 1, 2, 'bank_transfer', 'Chúc mừng sinh nhật'),
(3, 2, 350000, 'processing', NOW() - INTERVAL '15 days', 2, 2, 3, 'wallet', NULL),
(4, 2, 1500000, 'shipped', NOW() - INTERVAL '10 days', 3, 3, 4, 'credit_card', 'Giao sáng sớm'),
(5, 3, 1200000, 'completed', NOW() - INTERVAL '12 days', 1, NULL, 5, 'cash_on_delivery', NULL),
(6, 3, 390000, 'cancelled', NOW() - INTERVAL '9 days', 2, NULL, 6, 'wallet', 'Khách hủy'),
(7, 4, 1230000, 'completed', NOW() - INTERVAL '7 days', 2, 2, 7, 'credit_card', 'Lễ kỷ niệm'),
(8, 4, 850000, 'pending', NOW() - INTERVAL '3 days', 1, NULL, 8, 'cash_on_delivery', NULL),
(9, 5, 1800000, 'processing', NOW() - INTERVAL '2 days', 3, 3, 9, 'bank_transfer', 'Khai trương'),
(10,5, 480000, 'completed', NOW() - INTERVAL '1 day', 1, 1, 10, 'cash_on_delivery', 'Giao nhanh');

-- ========== 10) ORDER ITEMS ==========
-- Tổng tiền được tính hợp lý theo quantity * price (đã trừ voucher ở total_amount nếu có).
INSERT INTO OrderItems(id, order_id, product_id, quantity, price)
VALUES
-- O1: 1 x (1:450k)
(1, 1, 1, 1, 450000),

-- O2: (1 x 1:450k) + (1 x 8:480k) = 930k -> voucher CHAOHE10 (-10%) ~ 837k, cộng ship 30k ~ 867k, làm tròn + note -> ở total_amount đặt 970k (bao gồm gói thiệp, phụ phí); minh họa tình huống phí phát sinh.
(2, 2, 1, 1, 450000),
(3, 2, 8, 1, 480000),

-- O3: (1 x 7:350k) = 350k - voucher 25k => tổng khoảng 325k + phí -> 350k minh họa
(4, 3, 7, 1, 350000),

-- O4: (1 x 11:1.5tr) = 1.5tr - 100k voucher VIP100K + ship -> ~1.45-1.49tr; total đặt 1.5tr
(5, 4, 11, 1, 1500000),

-- O5: (1 x 5:1.2tr) = 1.2tr (COD)
(6, 5, 5, 1, 1200000),

-- O6: (1 x 6:390k) = 390k (bị hủy)
(7, 6, 6, 1, 390000),

-- O7: (1 x 6:390k) + (1 x 10:850k) = 1.24tr - freeship25k + phụ phí => 1.23tr
(8, 7, 6, 1, 390000),
(9, 7, 10, 1, 850000),

-- O8: (1 x 10:850k) = 850k (pending)
(10, 8, 10, 1, 850000),

-- O9: (1 x 12:1.8tr) = 1.8tr - 100k + ship => ~1.75-1.8tr (processing)
(11, 9, 12, 1, 1800000),

-- O10: (1 x 8:480k) = 480k - 10% + ship nhanh => ~480k (completed)
(12, 10, 8, 1, 480000);

-- ========== 11) REVIEWS (chỉ cho đơn đã hoàn tất; ràng buộc order_item_id UNIQUE) ==========
INSERT INTO Reviews(id, user_id, product_id, rating, comment, created_at, order_item_id)
VALUES
(1, 1, 1, 5, 'Hoa đẹp, giao đúng giờ!', NOW() - INTERVAL '24 days', 1),
(2, 1, 8, 4, 'Bó hoa xinh, giấy gói ổn.', NOW() - INTERVAL '19 days', 3),
(3, 3, 5, 5, 'Lan rất tươi, trang trọng.', NOW() - INTERVAL '11 days', 6),
(4, 4, 6, 4, 'Màu pastel dễ thương.', NOW() - INTERVAL '6 days', 8),
(5, 4, 10, 5, 'Bó cưới tinh tế, rất ưng.', NOW() - INTERVAL '6 days', 9),
(6, 5, 8, 5, 'Giao nhanh, hoa tươi.', NOW() - INTERVAL '20 hours', 12);

-- ========== 12) FOLLOWS ==========
INSERT INTO Follows(id, user_id, product_id, followed_at)
VALUES
(1, 1, 5, NOW() - INTERVAL '30 days'),
(2, 1, 10, NOW() - INTERVAL '22 days'),
(3, 2, 7, NOW() - INTERVAL '18 days'),
(4, 3, 5, NOW() - INTERVAL '16 days'),
(5, 3, 12, NOW() - INTERVAL '15 days'),
(6, 4, 1, NOW() - INTERVAL '8 days'),
(7, 5, 9, NOW() - INTERVAL '5 days');

-- ========== 13) MESSAGES (giữa KH & nhân viên) ==========
INSERT INTO Messages(id, sender_id, receiver_id, content, sent_at, is_read)
VALUES
(1, 1, 6, 'Shop còn tulip trắng không ạ?', NOW() - INTERVAL '7 days', TRUE),
(2, 6, 1, 'Dạ còn anh nhé, 10 bông/bó.', NOW() - INTERVAL '7 days' + INTERVAL '10 minutes', TRUE),
(3, 2, 7, 'Mình cần hoa trong ngày, tư vấn giúp.', NOW() - INTERVAL '2 days', FALSE),
(4, 7, 2, 'Dạ em gợi ý bó hồng đỏ Classic ạ.', NOW() - INTERVAL '2 days' + INTERVAL '15 minutes', FALSE);

-- ========== 14) TRANSACTIONS (thanh toán/hoàn tiền) ==========
INSERT INTO Transactions(id, user_id, order_id, amount, type, status, transaction_reference, notes, created_at)
VALUES
(1, 1, 1, 450000, 'payment', 'success', 'COD#O1', 'Thanh toán khi nhận', NOW() - INTERVAL '25 days'),
(2, 1, 2, 970000, 'payment', 'success', 'BANK#O2', 'Chuyển khoản', NOW() - INTERVAL '20 days'),
(3, 2, 3, 350000, 'payment', 'success', 'WALLET#O3', 'Ví điện tử', NOW() - INTERVAL '15 days'),
(4, 2, 4, 1500000, 'payment', 'success', 'CARD#O4', 'Thẻ tín dụng', NOW() - INTERVAL '10 days'),
(5, 3, 5, 1200000, 'payment', 'success', 'COD#O5', NULL, NOW() - INTERVAL '12 days'),
(6, 3, 6, 390000, 'refund',  'success', 'REFUND#O6', 'Hoàn do khách hủy', NOW() - INTERVAL '8 days'),
(7, 4, 7, 1230000, 'payment', 'success', 'CARD#O7', NULL, NOW() - INTERVAL '7 days'),
(8, 4, 8, 850000, 'payment', 'failed',  'COD#O8', 'Chưa nhận hàng', NOW() - INTERVAL '3 days'),
(9, 5, 9, 1800000, 'payment', 'success', 'BANK#O9', NULL, NOW() - INTERVAL '2 days'),
(10,5,10, 480000, 'payment', 'success', 'COD#O10', 'Thanh toán khi nhận', NOW() - INTERVAL '1 day');

-- ========== 15) TIMESHEETS (giờ làm của nhân viên) ==========
-- Cho tháng 09/2025, nhân viên 6 & 7
INSERT INTO TimeSheets(id, staff_id, check_in, check_out, date, hours_worked, notes)
VALUES
-- Staff 6 (Lan): 10 ca làm ~ 78 giờ
(1, 6, '2025-09-02 08:30', '2025-09-02 17:30', '2025-09-02', 8.0, 'Ca sáng'),
(2, 6, '2025-09-04 09:00', '2025-09-04 18:00', '2025-09-04', 8.0, 'Ca hành chính'),
(3, 6, '2025-09-06 08:30', '2025-09-06 19:00', '2025-09-06', 9.5, 'Thêm giờ'),
(4, 6, '2025-09-09 09:00', '2025-09-09 18:00', '2025-09-09', 8.0, NULL),
(5, 6, '2025-09-11 13:00', '2025-09-11 21:00', '2025-09-11', 8.0, 'Ca chiều'),
(6, 6, '2025-09-13 08:30', '2025-09-13 17:30', '2025-09-13', 8.0, NULL),
(7, 6, '2025-09-16 08:30', '2025-09-16 18:30', '2025-09-16', 9.0, 'Đơn nhiều'),
(8, 6, '2025-09-19 09:00', '2025-09-19 18:00', '2025-09-19', 8.0, NULL),
(9, 6, '2025-09-22 08:30', '2025-09-22 17:30', '2025-09-22', 8.0, NULL),
(10,6, '2025-09-25 09:00', '2025-09-25 19:00', '2025-09-25', 9.0, 'Giao kệ khai trương'),

-- Staff 7 (Minh): 9 ca làm ~ 71.5 giờ
(11, 7, '2025-09-01 09:00', '2025-09-01 18:00', '2025-09-01', 8.0, NULL),
(12, 7, '2025-09-03 08:00', '2025-09-03 17:00', '2025-09-03', 8.0, NULL),
(13, 7, '2025-09-05 13:00', '2025-09-05 22:00', '2025-09-05', 9.0, 'Ca tối'),
(14, 7, '2025-09-08 09:00', '2025-09-08 18:30', '2025-09-08', 9.5, 'OT xử lý tồn'),
(15, 7, '2025-09-12 08:30', '2025-09-12 17:30', '2025-09-12', 8.0, NULL),
(16, 7, '2025-09-15 09:00', '2025-09-15 18:00', '2025-09-15', 8.0, NULL),
(17, 7, '2025-09-18 08:30', '2025-09-18 17:00', '2025-09-18', 7.5, 'Đi công việc'),
(18, 7, '2025-09-21 09:00', '2025-09-21 18:00', '2025-09-21', 8.0, NULL),
(19, 7, '2025-09-24 09:00', '2025-09-24 17:30', '2025-09-24', 7.5, NULL);

-- ========== 16) SALARIES (tính lương theo giờ) ==========
-- base_salary theo giờ (VNĐ/giờ); working_time là tổng giờ làm trong tháng
-- Nhân viên 6: 78.5 giờ * 45,000 => total_salary tự sinh (Generated Column)
-- Nhân viên 7: 71.5 giờ * 42,000 => total_salary tự sinh
INSERT INTO Salaries(id, user_id, base_salary, working_time, month_year, status, notes, created_at)
VALUES
(1, 6, 45000, 78.5, '2025-09-01', 'paid', 'Đã chuyển khoản ngày 2025-09-26', NOW() - INTERVAL '1 day'),
(2, 7, 42000, 71.5, '2025-09-01', 'pending', 'Chờ xác nhận kế toán', NOW() - INTERVAL '12 hours');

-- ========== 17) CẬP NHẬT TỒN KHO (mô phỏng bán ra) ==========
-- Trừ tồn theo các đơn không bị hủy (completed, shipped, processing, pending)
UPDATE Products SET stock_quantity = stock_quantity - 1 WHERE id IN (1,5,6,7,8,10,11,12);
-- Với đơn có nhiều item
UPDATE Products SET stock_quantity = stock_quantity - 1 WHERE id IN (6,10);

COMMIT;

-- =============================================
-- GỢI Ý CHẠY:
-- psql -U <user> -d <db_name> -f seed_flower_store_vi.sql
-- =============================================
