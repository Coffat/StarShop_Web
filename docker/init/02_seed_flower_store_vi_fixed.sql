-- =============================================
-- SEED DATA (VIETNAMESE) - FLOWER STORE MANAGEMENT SYSTEM
-- Phiên bản: PostgreSQL - Fixed Version 3
-- Mục tiêu: Tạo dữ liệu mẫu "đủ nhiều" và hợp lý cho môi trường dev/demo
-- Lưu ý: Sử dụng cách tiếp cận đơn giản hơn để tránh lỗi subquery
-- =============================================

BEGIN;

-- ========== 1) USERS ==========
-- 5 khách hàng, 2 nhân viên, 1 admin
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

-- ========== 2) DELIVERY UNITS ==========
INSERT INTO DeliveryUnits(name, fee, estimated_time, is_active, created_at)
VALUES
('Giao Nhanh 2H', 25000, '2-4 giờ nội thành', TRUE, NOW() - INTERVAL '80 days'),
('GHN', 30000, '1-2 ngày', TRUE, NOW() - INTERVAL '80 days'),
('Viettel Post', 35000, '2-3 ngày', TRUE, NOW() - INTERVAL '80 days');

-- ========== 3) PRODUCT ATTRIBUTES ==========
INSERT INTO ProductAttributes(name, created_at)
VALUES
('Màu sắc', NOW() - INTERVAL '70 days'),
('Kích thước', NOW() - INTERVAL '70 days'),
('Chủ đề', NOW() - INTERVAL '70 days');

-- ========== 4) PRODUCTS ==========
INSERT INTO Products(name, description, price, image, stock_quantity, status, created_at, updated_at)
VALUES
('Bó hồng đỏ Classic', 'Bó 12 bông hồng đỏ giấy kraft, tượng trưng cho tình yêu nồng nàn và lãng mạn. Phù hợp cho các dịp đặc biệt như sinh nhật, kỷ niệm tình yêu.', 450000, '/images/products/red-rose-bouquet.jpg', 50, 'active', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),

('Bó hồng phấn Sweetie', 'Hồng phấn baby kèm thiệp, mang vẻ đẹp dịu dàng và thanh lịch. Thích hợp để tặng bạn gái, vợ trong các dịp lãng mạn.', 520000, '/images/products/pink-rose-bouquet.jpg', 40, 'active', NOW() - INTERVAL '59 days', NOW() - INTERVAL '59 days'),

('Giỏ hoa hướng dương', 'Giỏ 8 bông hướng dương tươi, mang ý nghĩa của sự lạc quan và hy vọng. Thích hợp để tặng bạn bè, đồng nghiệp.', 600000, '/images/products/sunflower-basket.jpg', 35, 'active', NOW() - INTERVAL '58 days', NOW() - INTERVAL '58 days'),

('Bình tulip Hà Lan', '10 tulip phối nơ, tượng trưng cho sự giàu có và thành công. Thường được chọn cho các dịp khai trương, chúc mừng.', 750000, '/images/products/tulip-vase.jpg', 30, 'active', NOW() - INTERVAL '57 days', NOW() - INTERVAL '57 days'),

('Lan hồ điệp 3 cành', 'Chậu lan sang trọng, thể hiện sự quý phái và đẳng cấp. Thường được chọn cho các sự kiện quan trọng và trang trí văn phòng.', 1200000, '/images/products/orchid-pot.jpg', 20, 'active', NOW() - INTERVAL '56 days', NOW() - INTERVAL '56 days'),

('Bó cẩm chướng Pastel', 'Tone pastel nhẹ nhàng, thể hiện tình cảm chân thành và sự biết ơn. Phù hợp để tặng mẹ, bà trong các dịp đặc biệt.', 390000, '/images/products/pastel-carnation.jpg', 60, 'active', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),

('Bó baby trắng', 'Baby trắng full bó, tượng trưng cho sự trong sạch và thuần khiết. Thường được dùng làm hoa phụ để tôn lên vẻ đẹp của hoa chính.', 350000, '/images/products/baby-breath-white.jpg', 70, 'active', NOW() - INTERVAL '54 days', NOW() - INTERVAL '54 days'),

('Bó mix hoa theo mùa', 'Mix nhiều loài theo mùa, tạo nên sự đa dạng và phong phú. Thích hợp cho những người yêu thích sự đa dạng trong hoa.', 480000, '/images/products/seasonal-mix.jpg', 45, 'active', NOW() - INTERVAL '53 days', NOW() - INTERVAL '53 days'),

('Giỏ hoa trái cây', 'Hoa + trái cây nhập, kết hợp giữa vẻ đẹp và dinh dưỡng. Thích hợp cho các dịp thăm bệnh, chúc sức khỏe.', 980000, '/images/products/fruit-flower-basket.jpg', 15, 'active', NOW() - INTERVAL '52 days', NOW() - INTERVAL '52 days'),

('Hoa cưới cô dâu', 'Bó cưới tone trắng kem, tượng trưng cho sự trong trắng và hạnh phúc. Được thiết kế đặc biệt cho ngày cưới.', 850000, '/images/products/wedding-bouquet.jpg', 12, 'active', NOW() - INTERVAL '51 days', NOW() - INTERVAL '51 days'),

('Vòng hoa chia buồn', 'Trang trọng, lịch sự, thể hiện sự tôn trọng và chia sẻ nỗi buồn. Thường được sử dụng trong các buổi lễ tang.', 1500000, '/images/products/condolence-wreath.jpg', 10, 'active', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),

('Hoa mừng khai trương', 'Kệ hoa chúc mừng, mang ý nghĩa thịnh vượng và thành công. Thích hợp cho các dịp khai trương cửa hàng, công ty.', 1800000, '/images/products/grand-opening-arrangement.jpg', 8, 'active', NOW() - INTERVAL '49 days', NOW() - INTERVAL '49 days'),

('Hoa Lavender', 'Hoa lavender tím nhạt với hương thơm dịu nhẹ, có tác dụng thư giãn tinh thần. Thích hợp để trang trí phòng ngủ.', 320000, '/images/products/lavender-bouquet.jpg', 25, 'active', NOW() - INTERVAL '48 days', NOW() - INTERVAL '48 days'),

('Hoa Peony Hồng', 'Hoa peony hồng đậm đà, tượng trưng cho sự thịnh vượng và may mắn. Thường được chọn cho các dịp cưới hỏi.', 680000, '/images/products/pink-peony.jpg', 18, 'active', NOW() - INTERVAL '47 days', NOW() - INTERVAL '47 days'),

('Hoa Gerbera Cam', 'Hoa gerbera cam rực rỡ, mang năng lượng tích cực và niềm vui. Thích hợp để tặng trong các dịp vui mừng.', 420000, '/images/products/orange-gerbera.jpg', 35, 'active', NOW() - INTERVAL '46 days', NOW() - INTERVAL '46 days');

-- ========== 5) VOUCHERS ==========
INSERT INTO Vouchers(code, discount_type, discount_value, min_order_value, max_uses, uses, expiry_date, is_active, created_at)
VALUES
('WELCOME10', 'percentage', 10, 500000, 100, 15, (NOW() + INTERVAL '30 days')::date, TRUE, NOW() - INTERVAL '30 days'),
('NEWUSER20', 'percentage', 20, 300000, 50, 8, (NOW() + INTERVAL '25 days')::date, TRUE, NOW() - INTERVAL '25 days'),
('SAVE50K', 'fixed', 50000, 800000, 200, 25, (NOW() + INTERVAL '20 days')::date, TRUE, NOW() - INTERVAL '20 days'),
('VIP15', 'percentage', 15, 1000000, 30, 5, (NOW() + INTERVAL '15 days')::date, TRUE, NOW() - INTERVAL '15 days');

COMMIT;
