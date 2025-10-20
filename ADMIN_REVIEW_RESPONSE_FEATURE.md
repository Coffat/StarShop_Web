# Tính năng Phản hồi Đánh giá từ Admin

## Tổng quan
Tính năng này cho phép admin phản hồi trực tiếp các đánh giá của khách hàng, giúp tăng tương tác và xây dựng lòng tin với khách hàng.

## Các thay đổi đã thực hiện

### 1. Database Schema
- **File**: `docker/init/01_init_schema.sql` và `docker/init/09_add_admin_response_to_reviews.sql`
- **Thay đổi**: Thêm 3 trường mới vào bảng `Reviews`:
  - `admin_response` (TEXT): Nội dung phản hồi của admin
  - `admin_response_at` (TIMESTAMP): Thời gian admin phản hồi
  - `admin_response_by` (BIGINT): ID của admin đã phản hồi

### 2. Backend Changes

#### Entity
- **File**: `src/main/java/com/example/demo/entity/Review.java`
- **Thay đổi**: Thêm các trường admin response và getter/setter tương ứng

#### DTO
- **File**: `src/main/java/com/example/demo/dto/ReviewResponse.java`
- **Thay đổi**: Thêm các trường admin response vào response DTO
- **File**: `src/main/java/com/example/demo/dto/AdminReviewResponseRequest.java` (mới)
- **Mô tả**: DTO cho request phản hồi admin

#### Service
- **File**: `src/main/java/com/example/demo/service/ReviewService.java`
- **Thay đổi**: Thêm 3 methods mới:
  - `addAdminResponse()`: Thêm phản hồi admin
  - `updateAdminResponse()`: Cập nhật phản hồi admin
  - `removeAdminResponse()`: Xóa phản hồi admin

#### Controller
- **File**: `src/main/java/com/example/demo/controller/AdminReviewController.java`
- **Thay đổi**: Thêm 3 API endpoints mới:
  - `POST /admin/api/reviews/{id}/respond`: Thêm phản hồi
  - `PUT /admin/api/reviews/{id}/response`: Cập nhật phản hồi
  - `DELETE /admin/api/reviews/{id}/response`: Xóa phản hồi

### 3. Frontend Changes

#### Admin UI
- **File**: `src/main/resources/templates/admin/reviews/index.html`
- **Thay đổi**:
  - Thêm cột "Phản hồi Admin" trong bảng đánh giá
  - Thêm modal để nhập/sửa phản hồi
  - Thêm các nút thao tác: Thêm, Sửa, Xóa phản hồi
  - Hiển thị phản hồi admin với styling đặc biệt
  - Thêm validation và character counter

## API Endpoints

### 1. Thêm phản hồi admin
```
POST /admin/api/reviews/{reviewId}/respond
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "adminResponse": "Cảm ơn bạn đã đánh giá sản phẩm!"
}
```

### 2. Cập nhật phản hồi admin
```
PUT /admin/api/reviews/{reviewId}/response
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "adminResponse": "Cảm ơn bạn đã đánh giá sản phẩm! Chúng tôi sẽ cải thiện chất lượng."
}
```

### 3. Xóa phản hồi admin
```
DELETE /admin/api/reviews/{reviewId}/response
Authorization: Bearer {admin_token}
```

## Cách sử dụng

### Cho Admin:
1. Truy cập trang "Quản lý đánh giá" trong admin panel
2. Tìm đánh giá cần phản hồi
3. Click nút "Thêm phản hồi" (icon reply màu xanh)
4. Nhập nội dung phản hồi (tối đa 1000 ký tự)
5. Click "Lưu phản hồi"

### Các thao tác khác:
- **Sửa phản hồi**: Click nút "Sửa phản hồi" (icon edit màu xanh)
- **Xóa phản hồi**: Click nút "Xóa phản hồi" (icon X màu cam)

## Tính năng UI

### Hiển thị phản hồi:
- Phản hồi admin được hiển thị trong khung màu xanh nhạt
- Hiển thị tên admin đã phản hồi
- Có tooltip hiển thị toàn bộ nội dung nếu quá dài

### Validation:
- Bắt buộc nhập nội dung phản hồi
- Giới hạn tối đa 1000 ký tự
- Character counter hiển thị số ký tự đã nhập
- Màu đỏ khi vượt quá giới hạn

### Responsive:
- Modal responsive trên các thiết bị khác nhau
- Bảng có thể scroll ngang trên mobile

## Bảo mật

- Chỉ admin mới có thể phản hồi đánh giá
- Sử dụng `@PreAuthorize("hasRole('ADMIN')")` để kiểm tra quyền
- Validation input để tránh XSS và injection

## Database Migration

Để áp dụng thay đổi cho database hiện có:

1. Chạy script migration:
```sql
-- Chạy file docker/init/09_add_admin_response_to_reviews.sql
```

2. Hoặc restart container để áp dụng tất cả thay đổi:
```bash
docker-compose down
docker-compose up -d
```

## Testing

### Test API:
```bash
# Thêm phản hồi
curl -X POST "http://localhost:8080/admin/api/reviews/1/respond" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{"adminResponse": "Cảm ơn bạn đã đánh giá!"}'

# Cập nhật phản hồi
curl -X PUT "http://localhost:8080/admin/api/reviews/1/response" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{"adminResponse": "Cảm ơn bạn! Chúng tôi sẽ cải thiện."}'

# Xóa phản hồi
curl -X DELETE "http://localhost:8080/admin/api/reviews/1/response" \
  -H "Authorization: Bearer {admin_token}"
```

### Test UI:
1. Đăng nhập với tài khoản admin
2. Truy cập `/admin/reviews`
3. Test các chức năng thêm/sửa/xóa phản hồi

## Lưu ý

- Phản hồi admin sẽ được hiển thị cho tất cả người dùng xem đánh giá
- Admin có thể sửa/xóa phản hồi của mình bất kỳ lúc nào
- Phản hồi được lưu với timestamp và thông tin admin đã phản hồi
- Không có giới hạn số lượng phản hồi cho mỗi đánh giá (chỉ 1 phản hồi/đánh giá)
