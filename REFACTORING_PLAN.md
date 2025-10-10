# KẾ HOẠCH REFACTORING: ProductAttributes → Catalogs

## 📊 TÓM TẮT THAY ĐỔI

### Database
- ❌ Xóa: `ProductAttributes`, `AttributeValues` tables
- ✅ Thêm: `Catalogs` table  
- 🔧 Sửa: `Products` table (thêm `catalog_id`)

### Backend
- ❌ Xóa: `ProductAttribute`, `AttributeValue` entities + repositories
- ✅ Thêm: `Catalog` entity + repository + service
- 🔧 Sửa: `Product` entity, `ProductService`, `ProductRepository`

### Frontend
- 🔧 Sửa: Product listing, detail, admin forms - đổi từ attributes sang catalog

---

## 🗂️ CHI TIẾT FILES CẦN SỬA

### 1. `docker/init/01_init_schema.sql`

```sql
-- XÓA (lines 79-103): ProductAttributes & AttributeValues tables

-- THÊM (sau line 78):
CREATE TABLE Catalogs (
    id BIGSERIAL PRIMARY KEY,
    value VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT NULL
);
CREATE INDEX idx_catalogs_value ON Catalogs(value);

-- SỬA Products table (thêm vào line 70):
    catalog_id BIGINT,
    FOREIGN KEY (catalog_id) REFERENCES Catalogs(id) ON DELETE SET NULL
CREATE INDEX idx_products_catalog_id ON Products(catalog_id);
```

### 2. `docker/init/02_seed_flower_store_vi_fixed.sql`

```sql
-- XÓA: sections 3) PRODUCT ATTRIBUTES và 6) ATTRIBUTE VALUES

-- THÊM (sau DeliveryUnits):
INSERT INTO Catalogs(value, created_at) VALUES
('Tình yêu', NOW()), ('Khai trương  ', NOW()), ('Hoa cưới', NOW()),
('Đám tang', NOW());

-- SỬA Products INSERT: thêm catalog_id cho mỗi product
-- Mapping: Product 1→1, Product 2→2, Product 3→3, etc.
```

### 3. `docker/init/04_product_management_enhancements.sql`

```sql
-- SỬA product_summary view (line 407):
CREATE OR REPLACE VIEW product_summary AS
SELECT p.id, p.name, c.value as catalog_name, ...
FROM products p
LEFT JOIN catalogs c ON p.catalog_id = c.id;
```

---

## 🔄 THỰC HIỆN THEO THỨ TỰ

1. ✅ **Database First** - Sửa 3 files SQL
---

## 📋 CATALOG MAPPING

| Product | ID | Catalog |
|---------|----|----|
| Bó hồng đỏ Classic | 1 | Tình yêu |
| Bó hồng phấn Sweetie | 2 | Lãng mạn |
| Giỏ hoa hướng dương | 3 | Chúc mừng |
| Bình tulip Hà Lan | 4 | Khai trương |
| Lan hồ điệp 3 cành | 5 | Sang trọng |
| Bó cẩm chướng Pastel | 2 | Lãng mạn |
| Hoa cưới | 6 | Hoa cưới |
| Vòng hoa chia buồn | 7 | Chia buồn |
| Hoa mừng khai trương | 4 | Khai trương |
| Hoa Lavender | 8 | Trang trí |
| Hoa Peony Hồng | 2 | Lãng mạn |
| Hoa Gerbera Cam | 3 | Chúc mừng |
