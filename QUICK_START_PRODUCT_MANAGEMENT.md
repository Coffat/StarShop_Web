# Quick Start - Product Management System

## 🚀 Hướng dẫn khởi động nhanh

### 1. Khởi động Database (Docker)
```bash
cd /Users/vuthang/demo_web
docker-compose up -d
```

### 2. Kiểm tra Database đã ready
```bash
docker-compose ps
# Đợi PostgreSQL container status = "healthy" hoặc "running"
```

### 3. Khởi động Application
```bash
./mvnw spring-boot:run
```

Hoặc sử dụng script có sẵn:
```bash
./dev.sh
```

### 4. Truy cập Admin Panel
```
URL: http://localhost:8080/admin/products
```

**Đăng nhập với tài khoản admin:**
- Email: `admin@starshop.com`
- Password: `password` (hoặc check trong database seed file)

### 5. Test các chức năng

#### ✅ Xem danh sách sản phẩm
- Truy cập: http://localhost:8080/admin/products
- Xem thống kê: Tổng SP, Đang bán, Sắp hết, Hết hàng
- Sử dụng filter và search

#### ✅ Thêm sản phẩm mới
1. Click "Thêm sản phẩm"
2. Điền form:
   - Tên: "Test Product"
   - Giá: 100000
   - Số lượng: 10
   - Danh mục: Chọn từ dropdown
3. Upload ảnh (optional)
4. Click "Lưu sản phẩm"
5. Kiểm tra product xuất hiện trong danh sách

#### ✅ Xem chi tiết sản phẩm
1. Click icon 👁️ (eye) ở một sản phẩm
2. Modal hiện lên với đầy đủ thông tin
3. Kiểm tra không còi lỗi 500

#### ✅ Chỉnh sửa sản phẩm
1. Click icon ✏️ (edit) ở một sản phẩm
2. Modal edit hiện lên với data đã điền sẵn
3. Sửa một số trường
4. Click "Cập nhật sản phẩm"
5. Kiểm tra thay đổi đã được lưu

#### ✅ Cập nhật tồn kho nhanh
1. Click icon 📦 ở một sản phẩm
2. Nhập số lượng mới
3. Click OK
4. Kiểm tra số lượng đã thay đổi

#### ✅ Xóa sản phẩm
1. Click icon 🗑️ (trash) ở một sản phẩm test
2. Confirm xóa
3. Kiểm tra product đã biến mất

### 6. Kiểm tra Database Triggers

#### Test auto status update based on stock
```sql
-- Connect to database
psql -U postgres -d starshop

-- Update stock to 0
UPDATE products SET stock_quantity = 0 WHERE id = 1;

-- Check status should be OUT_OF_STOCK
SELECT id, name, stock_quantity, status FROM products WHERE id = 1;

-- Update stock back to > 0
UPDATE products SET stock_quantity = 10 WHERE id = 1;

-- Check status should be ACTIVE (if was OUT_OF_STOCK)
SELECT id, name, stock_quantity, status FROM products WHERE id = 1;
```

#### Test validation trigger
```sql
-- Try to insert invalid product (should fail)
INSERT INTO products (name, price, stock_quantity, created_at)
VALUES ('Invalid', -100, -5, NOW());
-- Should fail with: "Product price must be greater than 0"

-- Try to insert valid product
INSERT INTO products (name, price, stock_quantity, created_at)
VALUES ('Valid Product', 100000, 10, NOW());
-- Should succeed
```

### 7. API Testing với cURL

#### Get product detail
```bash
curl -X GET "http://localhost:8080/admin/products/api/1" \
  -H "Cookie: JSESSIONID=<your_session_id>"
```

#### Get catalogs
```bash
curl -X GET "http://localhost:8080/admin/products/api/catalogs" \
  -H "Cookie: JSESSIONID=<your_session_id>"
```

#### Create product
```bash
curl -X POST "http://localhost:8080/admin/products/api/create" \
  -H "Cookie: JSESSIONID=<your_session_id>" \
  -F "name=Test Product" \
  -F "description=Test description" \
  -F "price=100000" \
  -F "stockQuantity=10" \
  -F "status=ACTIVE" \
  -F "weightG=500" \
  -F "lengthCm=20" \
  -F "widthCm=20" \
  -F "heightCm=30"
```

#### Update product
```bash
curl -X PUT "http://localhost:8080/admin/products/api/1" \
  -H "Cookie: JSESSIONID=<your_session_id>" \
  -F "name=Updated Product" \
  -F "price=150000" \
  -F "stockQuantity=20" \
  -F "status=ACTIVE"
```

#### Update stock
```bash
curl -X PUT "http://localhost:8080/admin/products/api/1/stock" \
  -H "Cookie: JSESSIONID=<your_session_id>" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "stockQuantity=50"
```

#### Delete product
```bash
curl -X DELETE "http://localhost:8080/admin/products/api/1" \
  -H "Cookie: JSESSIONID=<your_session_id>"
```

## 🐛 Troubleshooting

### Lỗi 500 khi xem chi tiết sản phẩm
**Đã fix!** Nếu vẫn gặp:
1. Restart application
2. Clear browser cache
3. Check console logs

### Modal không mở
1. Kiểm tra console browser có error JS không
2. Đảm bảo Tailwind CSS CDN loaded
3. Check Font Awesome CDN

### Image upload không work
1. Kiểm tra quyền ghi folder uploads:
```bash
mkdir -p uploads
chmod 755 uploads
```

2. Check `application.yml`:
```yaml
app:
  upload:
    dir: uploads
```

3. Increase max file size nếu cần:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

### Không lấy được catalogs
1. Check database có data không:
```sql
SELECT * FROM catalogs;
```

2. Nếu rỗng, seed data:
```sql
INSERT INTO catalogs (value, created_at) VALUES
('Tình yêu', NOW()),
('Khai trương', NOW()),
('Hoa cười', NOW()),
('Đám tang', NOW());
```

### CSS không hiển thị đúng
1. Check Tailwind CDN trong `layouts/admin.html`:
```html
<script src="https://cdn.tailwindcss.com"></script>
```

2. Hard refresh browser: `Ctrl+Shift+R` (Windows) hoặc `Cmd+Shift+R` (Mac)

## 📊 Database Schema Reference

### Products Table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    image VARCHAR(255),
    stock_quantity INTEGER DEFAULT 0,
    status product_status DEFAULT 'ACTIVE',
    weight_g INT DEFAULT 500,
    length_cm INT DEFAULT 20,
    width_cm INT DEFAULT 20,
    height_cm INT DEFAULT 30,
    catalog_id BIGINT REFERENCES catalogs(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Catalogs Table
```sql
CREATE TABLE catalogs (
    id BIGSERIAL PRIMARY KEY,
    value VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## 🎯 Next Steps

1. **Customize**: Điều chỉnh UI theo brand của bạn
2. **Extend**: Thêm các features như variants, multiple images
3. **Optimize**: Add caching, image optimization
4. **Deploy**: Deploy lên production server

## 📞 Support

Nếu gặp vấn đề, check:
1. `PRODUCT_MANAGEMENT_IMPROVEMENTS.md` - Chi tiết technical
2. Application logs: `app.log`
3. Database logs: `docker logs <postgres_container_id>`

## ✅ Verification Checklist

- [ ] Database running
- [ ] Application started
- [ ] Can login to admin
- [ ] Can view products list
- [ ] Can view product detail (no 500 error)
- [ ] Can create new product
- [ ] Can edit existing product
- [ ] Can update stock
- [ ] Can delete product
- [ ] Catalogs dropdown loads
- [ ] Image upload works
- [ ] All statistics cards show correct data
- [ ] Filters and search work
- [ ] Pagination works
- [ ] Mobile responsive

## 🎉 Hoàn thành!

Hệ thống quản lý sản phẩm đã sẵn sàng sử dụng!

