# Flower Shop System - Database Setup

## Cấu trúc Database

Database PostgreSQL cho hệ thống cửa hàng hoa với đầy đủ các bảng:
- Users (Admin, Staff, Customer)
- Products & Attributes
- Orders & Order Items
- Carts & Cart Items
- Reviews, Messages, Transactions
- Vouchers, Delivery Units, TimeSheets

## Hướng dẫn khởi động Database

### 1. Yêu cầu
- Docker và Docker Compose đã cài đặt
- Port 5432 không bị chiếm dụng

### 2. Khởi động Database

```bash
# Chạy PostgreSQL container
docker-compose up -d

# Kiểm tra container đang chạy
docker ps

# Xem logs nếu cần
docker-compose logs -f postgres
```

### 3. Thông tin kết nối

- **Host**: localhost
- **Port**: 5432
- **Database**: flower_shop_system
- **Username**: flower_admin
- **Password**: flower_password_2024

### 4. Dừng Database

```bash
# Dừng container
docker-compose down

# Dừng và xóa dữ liệu (cẩn thận!)
docker-compose down -v
```

### 5. Truy cập Database

```bash
# Sử dụng psql trong container
docker exec -it flower_shop_db psql -U flower_admin -d flower_shop_system

# Hoặc sử dụng công cụ GUI như:
# - DBeaver
# - pgAdmin
# - TablePlus
```

## Cấu trúc thư mục MVC

```
src/main/java/com/example/demo/
├── controller/     # REST Controllers
├── service/        # Business Logic
├── repository/     # Data Access Layer
├── entity/         # JPA Entities
├── dto/            # Data Transfer Objects
├── config/         # Configuration Classes
├── security/       # Security Configuration
├── exception/      # Exception Handling
├── utils/          # Utility Classes
└── mapper/         # Object Mappers
```

## Dữ liệu mẫu

Database được khởi tạo với dữ liệu mẫu bao gồm:
- 8 users (1 admin, 2 staff, 5 customers)
- 15 sản phẩm hoa các loại
- Đơn hàng mẫu với các trạng thái khác nhau
- Voucher giảm giá
- Đánh giá sản phẩm
- Tin nhắn customer service

## Lưu ý cho Development Team

1. **application.properties** đã được cấu hình sẵn để kết nối database
2. JPA hibernate.ddl-auto được set là `validate` để tránh mất dữ liệu
3. Các file SQL trong `docker/init/` sẽ tự động chạy khi khởi tạo container lần đầu
4. Dữ liệu được lưu trong Docker volume nên sẽ không mất khi restart container

## Troubleshooting

### Container không start được
- Kiểm tra port 5432 có bị chiếm không: `lsof -i :5432`
- Xem logs: `docker-compose logs postgres`

### Không kết nối được database
- Đảm bảo container đang chạy: `docker ps`
- Kiểm tra network: `docker network ls`
- Thử restart: `docker-compose restart`

### Reset database
```bash
docker-compose down -v
docker-compose up -d
```
