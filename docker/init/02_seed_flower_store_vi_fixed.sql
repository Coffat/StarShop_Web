-- =============================================
-- SEED DATA (VIETNAMESE) - FLOWER STORE MANAGEMENT SYSTEM
-- Phiên bản: PostgreSQL - Updated for GHN Integration
-- Mục tiêu: Tạo dữ liệu mẫu phù hợp với schema hiện tại
-- Bao gồm: GHN fields, BCrypt passwords, shipping dimensions
-- =============================================

BEGIN;

-- ========== 1) USERS ==========
-- 5 khách hàng, 2 nhân viên, 1 admin với BCrypt passwords
INSERT INTO Users(firstname, lastname, email, password, phone, avatar, role, created_at)
VALUES
('Nguyễn', 'An', 'an.nguyen@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0901000001', NULL, 'CUSTOMER', NOW() - INTERVAL '60 days'),
('Trần', 'Bình', 'binh.tran@example.com', '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm', '0901000002', NULL, 'CUSTOMER', NOW() - INTERVAL '58 days'),
('Lê', 'Chi', 'chi.le@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0901000003', NULL, 'CUSTOMER', NOW() - INTERVAL '40 days'),
('Phạm', 'Dũng', 'dung.pham@example.com', '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm', '0901000004', NULL, 'CUSTOMER', NOW() - INTERVAL '35 days'),
('Võ', 'Hà', 'ha.vo@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0901000005', NULL, 'CUSTOMER', NOW() - INTERVAL '30 days'),
('Hoàng', 'Lan', 'lan.hoang@example.com', '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm', '0901000006', NULL, 'STAFF', NOW() - INTERVAL '90 days'),
('Đỗ', 'Minh', 'minh.do@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0901000007', NULL, 'STAFF', NOW() - INTERVAL '90 days'),
('Admin', 'Root', 'admin@example.com', '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm', '0901000008', NULL, 'ADMIN', NOW() - INTERVAL '100 days');

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
-- Thêm GHN shipping dimensions cho tất cả products
INSERT INTO Products(name, description, price, image, stock_quantity, status, weight_g, length_cm, width_cm, height_cm, created_at, updated_at)
VALUES
('Bó hồng đỏ Classic', 'Bó 12 bông hồng đỏ giấy kraft, tượng trưng cho tình yêu nồng nàn và lãng mạn. Phù hợp cho các dịp đặc biệt như sinh nhật, kỷ niệm tình yêu.', 450000, '/images/products/red-rose-bouquet.jpg', 50, 'ACTIVE', 800, 60, 25, 40, NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),

('Bó hồng phấn Sweetie', 'Hồng phấn baby kèm thiệp, mang vẻ đẹp dịu dàng và thanh lịch. Thích hợp để tặng bạn gái, vợ trong các dịp lãng mạn.', 520000, '/images/products/pink-rose-bouquet.jpg', 40, 'ACTIVE', 750, 55, 25, 38, NOW() - INTERVAL '59 days', NOW() - INTERVAL '59 days'),

('Giỏ hoa hướng dương', 'Giỏ 8 bông hướng dương tươi, mang ý nghĩa của sự lạc quan và hy vọng. Thích hợp để tặng bạn bè, đồng nghiệp.', 600000, '/images/products/sunflower-basket.jpg', 35, 'ACTIVE', 1200, 40, 30, 35, NOW() - INTERVAL '58 days', NOW() - INTERVAL '58 days'),

('Bình tulip Hà Lan', '10 tulip phối nơ, tượng trưng cho sự giàu có và thành công. Thường được chọn cho các dịp khai trương, chúc mừng.', 750000, '/images/products/tulip-vase.jpg', 30, 'ACTIVE', 1500, 25, 25, 45, NOW() - INTERVAL '57 days', NOW() - INTERVAL '57 days'),

('Lan hồ điệp 3 cành', 'Chậu lan sang trọng, thể hiện sự quý phái và đẳng cấp. Thường được chọn cho các sự kiện quan trọng và trang trí văn phòng.', 1200000, '/images/products/orchid-pot.jpg', 20, 'ACTIVE', 2000, 30, 30, 50, NOW() - INTERVAL '56 days', NOW() - INTERVAL '56 days'),

('Bó cẩm chướng Pastel', 'Tone pastel nhẹ nhàng, thể hiện tình cảm chân thành và sự biết ơn. Phù hợp để tặng mẹ, bà trong các dịp đặc biệt.', 390000, '/images/products/pastel-carnation.jpg', 60, 'ACTIVE', 600, 50, 20, 35, NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),

('Bó baby trắng', 'Baby trắng full bó, tượng trưng cho sự trong sạch và thuần khiết. Thường được dùng làm hoa phụ để tôn lên vẻ đẹp của hoa chính.', 350000, '/images/products/baby-breath-white.jpg', 70, 'ACTIVE', 400, 45, 20, 30, NOW() - INTERVAL '54 days', NOW() - INTERVAL '54 days'),

('Bó mix hoa theo mùa', 'Mix nhiều loài theo mùa, tạo nên sự đa dạng và phong phú. Thích hợp cho những người yêu thích sự đa dạng trong hoa.', 480000, '/images/products/seasonal-mix.jpg', 45, 'ACTIVE', 900, 55, 25, 40, NOW() - INTERVAL '53 days', NOW() - INTERVAL '53 days'),

('Giỏ hoa trái cây', 'Hoa + trái cây nhập, kết hợp giữa vẻ đẹp và dinh dưỡng. Thích hợp cho các dịp thăm bệnh, chúc sức khỏe.', 980000, '/images/products/fruit-flower-basket.jpg', 15, 'ACTIVE', 2500, 45, 35, 40, NOW() - INTERVAL '52 days', NOW() - INTERVAL '52 days'),

('Hoa cưới cô dâu', 'Bó cưới tone trắng kem, tượng trưng cho sự trong trắng và hạnh phúc. Được thiết kế đặc biệt cho ngày cưới.', 850000, '/images/products/wedding-bouquet.jpg', 12, 'ACTIVE', 1000, 50, 30, 45, NOW() - INTERVAL '51 days', NOW() - INTERVAL '51 days'),

('Vòng hoa chia buồn', 'Trang trọng, lịch sự, thể hiện sự tôn trọng và chia sẻ nỗi buồn. Thường được sử dụng trong các buổi lễ tang.', 1500000, '/images/products/condolence-wreath.jpg', 10, 'ACTIVE', 3000, 80, 80, 20, NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),

('Hoa mừng khai trương', 'Kệ hoa chúc mừng, mang ý nghĩa thịnh vượng và thành công. Thích hợp cho các dịp khai trương cửa hàng, công ty.', 1800000, '/images/products/grand-opening-arrangement.jpg', 8, 'ACTIVE', 5000, 100, 60, 150, NOW() - INTERVAL '49 days', NOW() - INTERVAL '49 days'),

('Hoa Lavender', 'Hoa lavender tím nhạt với hương thơm dịu nhẹ, có tác dụng thư giãn tinh thần. Thích hợp để trang trí phòng ngủ.', 320000, '/images/products/lavender-bouquet.jpg', 25, 'ACTIVE', 300, 40, 15, 25, NOW() - INTERVAL '48 days', NOW() - INTERVAL '48 days'),

('Hoa Peony Hồng', 'Hoa peony hồng đậm đà, tượng trưng cho sự thịnh vượng và may mắn. Thường được chọn cho các dịp cưới hỏi.', 680000, '/images/products/pink-peony.jpg', 18, 'ACTIVE', 700, 50, 25, 35, NOW() - INTERVAL '47 days', NOW() - INTERVAL '47 days'),

('Hoa Gerbera Cam', 'Hoa gerbera cam rực rỡ, mang năng lượng tích cực và niềm vui. Thích hợp để tặng trong các dịp vui mừng.', 420000, '/images/products/orange-gerbera.jpg', 35, 'ACTIVE', 500, 45, 20, 30, NOW() - INTERVAL '46 days', NOW() - INTERVAL '46 days');

-- ========== 5) ADDRESSES ==========
-- Thêm addresses cho users với GHN fields (mix OLD và NEW mode)
INSERT INTO Addresses(user_id, street, city, province, is_default, province_id, district_id, ward_code, address_detail, province_name, district_name, ward_name, address_mode, created_at, updated_at)
VALUES
-- User 1: Nguyễn An (OLD mode - HCM)
(1, '123 Nguyễn Văn Cừ, Phường 4, Quận 5', 'Thành phố Hồ Chí Minh', 'TP. Hồ Chí Minh', TRUE, 202, 1450, '21211', '123 Nguyễn Văn Cừ', 'Thành phố Hồ Chí Minh', 'Quận 5', 'Phường 04', 'OLD', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),

-- User 2: Trần Bình (NEW mode - HN)
(2, '456 Hoàng Hoa Thám, Phường Liễu Giai', 'Hà Nội', 'Hà Nội', TRUE, 201, NULL, '1A0208', '456 Hoàng Hoa Thám', 'Hà Nội', NULL, 'Phường Liễu Giai', 'NEW', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),

-- User 3: Lê Chi (OLD mode - HCM)
(3, '789 Lê Văn Sỹ, Phường 1, Quận Tân Bình', 'Thành phố Hồ Chí Minh', 'TP. Hồ Chí Minh', TRUE, 202, 1463, '21301', '789 Lê Văn Sỹ', 'Thành phố Hồ Chí Minh', 'Quận Tân Bình', 'Phường 01', 'OLD', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days'),

-- User 4: Phạm Dũng (NEW mode - DN)
(4, '321 Trần Phú, Phường Hải Châu I', 'Đà Nẵng', 'Đà Nẵng', TRUE, 291, NULL, '90107', '321 Trần Phú', 'Đà Nẵng', NULL, 'Phường Hải Châu I', 'NEW', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),

-- User 5: Võ Hà (OLD mode - HCM) + thêm address phụ
(5, '654 Võ Văn Tần, Phường 6, Quận 3', 'Thành phố Hồ Chí Minh', 'TP. Hồ Chí Minh', TRUE, 202, 1442, '21006', '654 Võ Văn Tần', 'Thành phố Hồ Chí Minh', 'Quận 3', 'Phường 06', 'OLD', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
(5, '987 Nguyễn Thị Minh Khai, Phường Đa Kao, Quận 1', 'Thành phố Hồ Chí Minh', 'TP. Hồ Chí Minh', FALSE, 202, 1440, '21012', '987 Nguyễn Thị Minh Khai', 'Thành phố Hồ Chí Minh', 'Quận 1', 'Phường Đa Kao', 'OLD', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days');

-- ========== 6) ATTRIBUTE VALUES ==========
-- Thêm attribute values cho products
INSERT INTO AttributeValues(attribute_id, product_id, value, created_at, updated_at)
VALUES
-- Màu sắc cho các products
(1, 1, 'Đỏ', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
(1, 2, 'Hồng', NOW() - INTERVAL '59 days', NOW() - INTERVAL '59 days'),
(1, 3, 'Vàng', NOW() - INTERVAL '58 days', NOW() - INTERVAL '58 days'),
(1, 4, 'Đa màu', NOW() - INTERVAL '57 days', NOW() - INTERVAL '57 days'),
(1, 5, 'Trắng', NOW() - INTERVAL '56 days', NOW() - INTERVAL '56 days'),
-- Kích thước
(2, 1, 'Trung bình', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
(2, 2, 'Trung bình', NOW() - INTERVAL '59 days', NOW() - INTERVAL '59 days'),
(2, 3, 'Lớn', NOW() - INTERVAL '58 days', NOW() - INTERVAL '58 days'),
(2, 4, 'Trung bình', NOW() - INTERVAL '57 days', NOW() - INTERVAL '57 days'),
(2, 5, 'Lớn', NOW() - INTERVAL '56 days', NOW() - INTERVAL '56 days'),
-- Chủ đề
(3, 1, 'Tình yêu', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
(3, 2, 'Lãng mạn', NOW() - INTERVAL '59 days', NOW() - INTERVAL '59 days'),
(3, 3, 'Chúc mừng', NOW() - INTERVAL '58 days', NOW() - INTERVAL '58 days'),
(3, 4, 'Khai trương', NOW() - INTERVAL '57 days', NOW() - INTERVAL '57 days'),
(3, 5, 'Sang trọng', NOW() - INTERVAL '56 days', NOW() - INTERVAL '56 days');

-- ========== 7) CARTS ==========
-- Tạo carts cho customers
INSERT INTO Carts(user_id, total_amount, total_quantity, created_at, updated_at)
VALUES
(1, 0, 0, NOW() - INTERVAL '60 days', NOW() - INTERVAL '1 day'),
(2, 0, 0, NOW() - INTERVAL '58 days', NOW() - INTERVAL '2 days'),
(3, 0, 0, NOW() - INTERVAL '40 days', NOW() - INTERVAL '3 days'),
(4, 0, 0, NOW() - INTERVAL '35 days', NOW() - INTERVAL '4 days'),
(5, 0, 0, NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days');

-- ========== 8) CART ITEMS ==========
-- Thêm một số items vào carts để demo
INSERT INTO CartItems(cart_id, product_id, quantity, created_at, updated_at)
VALUES
(1, 1, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(1, 3, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(2, 2, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(3, 5, 1, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(3, 7, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

-- Update cart totals after adding items
UPDATE Carts SET 
    total_amount = (
        SELECT COALESCE(SUM(ci.quantity * p.price), 0)
        FROM CartItems ci
        JOIN Products p ON ci.product_id = p.id
        WHERE ci.cart_id = Carts.id
    ),
    total_quantity = (
        SELECT COALESCE(SUM(ci.quantity), 0)
        FROM CartItems ci
        WHERE ci.cart_id = Carts.id
    ),
    updated_at = NOW()
WHERE id IN (1, 2, 3, 4, 5);

-- ========== 9) FOLLOWS (WISHLIST) ==========
-- Thêm wishlist data cho users
INSERT INTO Follows(user_id, product_id, followed_at)
VALUES
(1, 1, NOW() - INTERVAL '10 days'),
(1, 5, NOW() - INTERVAL '8 days'),
(1, 10, NOW() - INTERVAL '5 days'),
(2, 2, NOW() - INTERVAL '12 days'),
(2, 4, NOW() - INTERVAL '7 days'),
(3, 3, NOW() - INTERVAL '15 days'),
(3, 6, NOW() - INTERVAL '9 days'),
(3, 8, NOW() - INTERVAL '3 days'),
(4, 7, NOW() - INTERVAL '6 days'),
(4, 9, NOW() - INTERVAL '4 days'),
(5, 11, NOW() - INTERVAL '2 days'),
(5, 12, NOW() - INTERVAL '1 day');

-- ========== 10) VOUCHERS ==========
INSERT INTO Vouchers(code, discount_type, discount_value, min_order_value, max_uses, uses, expiry_date, is_active, created_at)
VALUES
('WELCOME10', 'PERCENTAGE', 10, 500000, 100, 15, (NOW() + INTERVAL '30 days')::date, TRUE, NOW() - INTERVAL '30 days'),
('NEWUSER20', 'PERCENTAGE', 20, 300000, 50, 8, (NOW() + INTERVAL '25 days')::date, TRUE, NOW() - INTERVAL '25 days'),
('SAVE50K', 'FIXED', 50000, 800000, 200, 25, (NOW() + INTERVAL '20 days')::date, TRUE, NOW() - INTERVAL '20 days'),
('VIP15', 'PERCENTAGE', 15, 1000000, 30, 5, (NOW() + INTERVAL '15 days')::date, TRUE, NOW() - INTERVAL '15 days');

COMMIT;
