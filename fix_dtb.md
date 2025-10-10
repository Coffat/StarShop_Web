Đây là danh sách tất cả những điểm bạn cần chỉnh sửa trong các file mã nguồn của mình sau khi áp dụng thay đổi trên.

### 1. File `01_init_schema.sql` (File Định nghĩa Schema)

Đây là nơi cần thay đổi cấu trúc chính.

- **Xóa bỏ hoàn toàn:**
    
    - Toàn bộ định nghĩa `CREATE TABLE ProductAttributes`.
        
    - Toàn bộ định nghĩa `CREATE TABLE AttributeValues`.
        
    - Các `COMMENT` và `INDEX` liên quan đến hai bảng này.
        
- **Thêm mới:**
    
    - Thêm định nghĩa `CREATE TABLE Catalogs` như trong kịch bản SQL ở trên.
        
- **Chỉnh sửa:**
    
    - Trong định nghĩa `CREATE TABLE Products`, thêm cột `catalog_id` và ràng buộc khóa ngoại của nó. Cách tốt nhất là thêm trực tiếp vào câu lệnh `CREATE TABLE` để giữ file sạch sẽ, thay vì dùng `ALTER TABLE`.
        
    
    SQL
    
    ```
    -- Ví dụ chỉnh sửa trong CREATE TABLE Products
    CREATE TABLE Products (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT DEFAULT NULL,
        price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
        -- ... các cột khác ...
        height_cm INT DEFAULT 30 CHECK (height_cm > 0),
        catalog_id BIGINT, -- CỘT MỚI
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP,
        CHECK (price >= 0),
        CHECK (stock_quantity >= 0),
        FOREIGN KEY (catalog_id) REFERENCES Catalogs(id) ON DELETE SET NULL -- RÀNG BUỘC MỚI
    );
    ```
    
### 2. File `02_seed_flower_store_vi_fixed.sql` (File Dữ liệu Mẫu)

File này cần được cập nhật cẩn thận để phù hợp với cấu trúc mới.

- **Xóa bỏ hoàn toàn:**
    
    - Xóa toàn bộ section `========== 3) PRODUCT ATTRIBUTES ==========`.
        
    - Xóa toàn bộ section `========== 6) ATTRIBUTE VALUES ==========`.
        
- **Thêm mới:**
    
    - Bạn cần thêm một section mới để chèn dữ liệu cho bảng `Catalogs`. Dựa trên dữ liệu cũ từ thuộc tính "Chủ đề", ta có thể tạo các danh mục sau:
        
    
    SQL
    
    ```
    -- ========== NEW SECTION: Catalogs ==========
    INSERT INTO Catalogs(value, created_at)
    VALUES
    ('Tình yêu', NOW()),       -- id: 1
    ('Lãng mạn', NOW()),      -- id: 2
    ('Chúc mừng', NOW()),     -- id: 3
    ('Khai trương', NOW()),    -- id: 4
    ('Sang trọng', NOW()),    -- id: 5
    ('Hoa cưới', NOW()),        -- id: 6
    ('Chia buồn', NOW()),      -- id: 7
    ('Trang trí', NOW());      -- id: 8
    ```
    
- **Chỉnh sửa:**
    
    - Trong `INSERT INTO Products`, bạn phải thêm cột `catalog_id` cho mỗi sản phẩm. Dựa trên dữ liệu cũ và tên sản phẩm, tôi đề xuất ánh xạ như sau:
        
    
    SQL
    
    ```
    -- Ví dụ chỉnh sửa câu lệnh INSERT cho Products
    INSERT INTO Products(name, description, price, image, stock_quantity, status, weight_g, length_cm, width_cm, height_cm, catalog_id, created_at, updated_at)
    VALUES
    ('Bó hồng đỏ Classic', '...', 450000, '...', 50, 'ACTIVE', 800, 60, 25, 40, 1, NOW(), NOW()), -- Tình yêu
    ('Bó hồng phấn Sweetie', '...', 520000, '...', 40, 'ACTIVE', 750, 55, 25, 38, 2, NOW(), NOW()), -- Lãng mạn
    ('Giỏ hoa hướng dương', '...', 600000, '...', 35, 'ACTIVE', 1200, 40, 30, 35, 3, NOW(), NOW()), -- Chúc mừng
    ('Bình tulip Hà Lan', '...', 750000, '...', 30, 'ACTIVE', 1500, 25, 25, 45, 4, NOW(), NOW()), -- Khai trương
    ('Lan hồ điệp 3 cành', '...', 1200000, '...', 20, 'ACTIVE', 2000, 30, 30, 50, 5, NOW(), NOW()), -- Sang trọng
    ('Bó cẩm chướng Pastel', '...', 390000, '...', 60, 'ACTIVE', 600, 50, 20, 35, 2, NOW(), NOW()), -- Lãng mạn
    ('Hoa cưới', '...', 850000, '...', 12, 'ACTIVE', 1000, 50, 30, 45, 6, NOW(), NOW()), -- Hoa cưới
    ('Vòng hoa chia buồn', '...', 1500000, '...', 10, 'ACTIVE', 3000, 80, 80, 20, 7, NOW(), NOW()), -- Chia buồn
    ('Hoa mừng khai trương', '...', 1800000, '...', 8, 'ACTIVE', 5000, 100, 60, 150, 4, NOW(), NOW()), -- Khai trương
    ('Hoa Lavender', '...', 320000, '...', 25, 'ACTIVE', 300, 40, 15, 25, 8, NOW(), NOW()), -- Trang trí
    ('Hoa Peony Hồng', '...', 680000, '...', 18, 'ACTIVE', 700, 50, 25, 35, 2, NOW(), NOW()), -- Lãng mạn
    ('Hoa Gerbera Cam', '...', 420000, '...', 35, 'ACTIVE', 500, 45, 20, 30, 3, NOW(), NOW()); -- Chúc mừng
    ```


### 3. File `04_product_management_enhancements.sql`

File này chứa các logic nâng cao, một số view có thể được cải thiện.

- **Các Function và Trigger:** Hầu hết các hàm (`calculate_product_statistics`, `log_product_changes`, `validate_product_data`, v.v.) và các trigger tương ứng sẽ **không bị lỗi** vì chúng chủ yếu thao tác trên các cột của bảng `Products` mà không đụng đến thuộc tính. Dữ liệu JSON trong bảng `product_audit_log` sẽ tự động thay đổi để phản ánh cấu trúc mới (có `catalog_id`).
    
- **View Cần Cải Thiện:**
    
    - Bạn nên cập nhật `CREATE OR REPLACE VIEW product_summary` để thêm thông tin tên danh mục, giúp việc báo cáo dễ dàng hơn.
        
    
    SQL
    
    ```
    -- Đề xuất cải thiện cho view product_summary
    CREATE OR REPLACE VIEW product_summary AS
    SELECT
        p.id,
        p.name,
        c.value as catalog_name, -- THÊM TÊN DANH MỤC
        p.price,
        p.stock_quantity,
        p.status,
        p.created_at,
        p.updated_at,
        (p.price * p.stock_quantity) as stock_value,
        CASE
            WHEN p.stock_quantity = 0 THEN 'Out of Stock'
            WHEN p.stock_quantity < 10 THEN 'Low Stock'
            WHEN p.stock_quantity < 50 THEN 'Medium Stock'
            ELSE 'High Stock'
        END as stock_level,
        CASE
            WHEN p.status = 'ACTIVE' AND p.stock_quantity > 0 THEN 'Available'
            WHEN p.status = 'ACTIVE' AND p.stock_quantity = 0 THEN 'Out of Stock'
            ELSE 'Unavailable'
        END as availability_status
    FROM products p
    LEFT JOIN catalogs c ON p.catalog_id = c.id; -- THÊM JOIN
    ```