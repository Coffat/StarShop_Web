# 🚀 SWAGGER UI - QUICK START GUIDE

## Truy cập Swagger UI

1. **Khởi động ứng dụng:**
   ```bash
   cd /Users/vuthang/demo_web
   mvn spring-boot:run
   ```

2. **Mở Swagger UI:**
   - URL: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## 🔐 Cách sử dụng Authentication

### Bước 1: Đăng nhập để lấy token

1. Mở endpoint `🔐 Authentication > POST /api/auth/login`
2. Click **"Try it out"**
3. Nhập request body:
   ```json
   {
     "email": "customer@example.com",
     "password": "password123"
   }
   ```
4. Click **"Execute"**
5. **Copy token** từ response

### Bước 2: Authorize

1. Click nút **"Authorize"** (🔒) ở góc trên phải
2. Paste token vào field **"Value"** (KHÔNG cần thêm "Bearer")
3. Click **"Authorize"**
4. Click **"Close"**

### Bước 3: Test APIs

Giờ bạn có thể test tất cả các APIs yêu cầu authentication!

---

## 📋 APIs đã document (31/34 - 91% ✅)

### ✅ 100% Documented

**🛒 Cart APIs (6/6)**
- POST `/api/cart/add` - Thêm vào giỏ
- PUT `/api/cart/update` - Cập nhật số lượng
- DELETE `/api/cart/remove` - Xóa khỏi giỏ
- GET `/api/cart/get` - Lấy giỏ hàng
- DELETE `/api/cart/clear` - Xóa toàn bộ
- GET `/api/cart/count` - Đếm số items

**❤️ Wishlist APIs (7/7)**
- POST `/api/wishlist/add` - Thêm yêu thích
- DELETE `/api/wishlist/remove` - Xóa yêu thích
- POST `/api/wishlist/toggle` - Toggle status
- GET `/api/wishlist/status/{id}` - Kiểm tra status
- GET `/api/wishlist/list` - Danh sách
- DELETE `/api/wishlist/clear` - Xóa tất cả
- GET `/api/wishlist/count` - Đếm số items

**📦 Order APIs (9/9) - 100% ✅**
- ✅ POST `/api/orders/create-from-cart` - Tạo đơn từ giỏ
- ✅ POST `/api/orders/create-direct` - Tạo đơn trực tiếp
- ✅ GET `/api/orders/list` - Danh sách đơn hàng (pagination)
- ✅ GET `/api/orders/{id}` - Chi tiết đơn hàng
- ✅ PUT `/api/orders/{id}/cancel` - Hủy đơn hàng
- ✅ PUT `/api/orders/{id}/status` - Cập nhật status (Admin/Staff)
- ✅ GET `/api/orders/status/{status}` - Lọc theo status (Admin/Staff)
- ✅ POST `/api/orders/{id}/payment` - Xử lý thanh toán
- ✅ POST `/api/orders/create-with-payment` - Tạo + thanh toán

**🔐 Auth APIs (7/10) - 70% ✅**
- ✅ POST `/api/auth/login` - Đăng nhập
- ✅ POST `/api/auth/logout` - Đăng xuất
- ✅ POST `/api/auth/register` - Đăng ký (step 1 - send OTP)
- ✅ POST `/api/auth/verify-registration` - Đăng ký (step 2 - verify)
- ✅ POST `/api/auth/forgot-password` - Quên mật khẩu (step 1)
- ✅ POST `/api/auth/verify-otp` - Quên mật khẩu (step 2)
- ✅ POST `/api/auth/reset-password` - Quên mật khẩu (step 3)
- ⚪ POST `/api/auth/refresh` - Refresh token
- ⚪ GET `/api/auth/validate` - Validate token
- ⚪ GET `/api/auth/debug-user` - Debug

**💳 Payment APIs (2/2) - 100% ✅**
- ✅ GET `/payment/momo/return` - MoMo callback
- ✅ POST `/payment/momo/notify` - MoMo IPN

---

## 💡 Examples

### Tạo đơn hàng từ giỏ

1. Login để lấy token
2. Thêm sản phẩm vào giỏ: `POST /api/cart/add`
   ```json
   {"productId": 1, "quantity": 2}
   ```
3. Xem giỏ hàng: `GET /api/cart/get`
4. Tạo đơn: `POST /api/orders/create-from-cart`
   ```json
   {
     "addressId": 1,
     "paymentMethod": "COD",
     "notes": "Giao giờ hành chính"
   }
   ```

### Quản lý wishlist

1. Thêm yêu thích: `POST /api/wishlist/add`
   ```json
   {"productId": 5}
   ```
2. Kiểm tra status: `GET /api/wishlist/status/5`
3. Toggle: `POST /api/wishlist/toggle`
   ```json
   {"productId": 5}
   ```

---

## 📄 Tài liệu chi tiết

- **Full API Documentation:** [SWAGGER3_API_SUMMARY.md](./SWAGGER3_API_SUMMARY.md)
- **Implementation Progress:** [SWAGGER3_PROGRESS.md](./SWAGGER3_PROGRESS.md)

---

## ⚠️ Lưu ý

- Token hết hạn sau 24 giờ
- Cần có phone + address hợp lệ mới checkout được
- Một số endpoints yêu cầu role ADMIN/STAFF
- Response format: `{success, message, data}`

---

**Last Updated:** 2025-10-01 09:34:00  
**Swagger Version:** OpenAPI 3.0  
**API Coverage:** 91% (31/34 endpoints)  
**Status:** ✅ Production Ready - Expanded Coverage
