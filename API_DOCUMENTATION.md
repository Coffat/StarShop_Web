# API Documentation - StarShop Flower System

## Tổng quan
Hệ thống StarShop là một ứng dụng web bán hoa tươi với các tính năng quản lý sản phẩm, giỏ hàng, đơn hàng và xác thực người dùng.

**Base URL:** `http://localhost:8080`  
**Port:** 8080  
**Database:** PostgreSQL (flower_shop_system)

## Authentication & Authorization

### JWT Token
- Tất cả API yêu cầu authentication đều sử dụng JWT token
- Token được lưu trong cookie `authToken` (httpOnly, secure)
- Token có thời hạn 24 giờ
- Header: `Authorization: Bearer <token>`

### User Roles
- `CUSTOMER`: Khách hàng thông thường
- `STAFF`: Nhân viên
- `ADMIN`: Quản trị viên

## API Endpoints

### 1. Authentication APIs (`/api/auth`)

#### 1.1 Đăng nhập
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "data": {
    "token": null,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstname": "John",
      "lastname": "Doe",
      "role": "CUSTOMER"
    }
  },
  "error": null
}
```

**Test với cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

#### 1.2 Đăng ký (Bước 1: Gửi OTP)
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "password123",
  "firstname": "John",
  "lastname": "Doe",
  "phone": "0123456789"
}
```

**Response:**
```json
{
  "data": "Mã xác thực đã được gửi đến email của bạn. Vui lòng kiểm tra và nhập mã để hoàn tất đăng ký.",
  "error": null
}
```

**Test với cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "firstname": "John",
    "lastname": "Doe",
    "phone": "0123456789"
  }'
```

#### 1.3 Xác thực đăng ký (Bước 2: Xác nhận OTP)
```http
POST /api/auth/verify-registration
Content-Type: application/json

{
  "email": "newuser@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "data": "Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.",
  "error": null
}
```

#### 1.4 Quên mật khẩu
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 1.5 Xác thực OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "data": {
    "resetToken": "uuid-token-here"
  },
  "error": null
}
```

#### 1.6 Đặt lại mật khẩu
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "uuid-token-here",
  "newPassword": "newpassword123"
}
```

#### 1.7 Làm mới token
```http
POST /api/auth/refresh
Content-Type: application/json

"current-jwt-token"
```

#### 1.8 Xác thực token
```http
GET /api/auth/validate
Authorization: Bearer <jwt-token>
```

#### 1.9 Đăng xuất
```http
POST /api/auth/logout
```

### 2. Product APIs (`/products`)

#### 2.1 Lấy danh sách sản phẩm
```http
GET /products?page=0&size=12&sort=newest&direction=desc&search=hoa&minPrice=100000&maxPrice=500000
```

**Parameters:**
- `page`: Số trang (default: 0)
- `size`: Số sản phẩm mỗi trang (default: 12, max: 50)
- `sort`: Tiêu chí sắp xếp (newest, oldest, name, price)
- `direction`: Hướng sắp xếp (asc, desc)
- `search`: Từ khóa tìm kiếm
- `minPrice`, `maxPrice`: Lọc theo giá

**Test với cURL:**
```bash
curl "http://localhost:8080/products?page=0&size=12&sort=newest&direction=desc"
```

#### 2.2 Tìm kiếm sản phẩm (AJAX)
```http
GET /products/search?q=hoa&page=0&size=12
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Hoa hồng đỏ",
      "price": 250000,
      "description": "Hoa hồng đỏ tươi",
      "stockQuantity": 50,
      "imageUrl": "/images/products/rose.jpg"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 12,
  "number": 0
}
```

#### 2.3 Chi tiết sản phẩm
```http
GET /products/{id}
```

**Test với cURL:**
```bash
curl "http://localhost:8080/products/1"
```

#### 2.4 Danh mục sản phẩm
```http
GET /products/categories
```

### 3. Cart APIs (`/api/cart`)

**Lưu ý:** Tất cả Cart APIs yêu cầu authentication với role CUSTOMER.

#### 3.1 Thêm sản phẩm vào giỏ hàng
```http
POST /api/cart/add
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

**Response:**
```json
{
  "data": {
    "success": true,
    "message": "Đã thêm sản phẩm vào giỏ hàng",
    "cart": {
      "userId": 1,
      "totalAmount": 500000,
      "totalQuantity": 2,
      "items": [
        {
          "productId": 1,
          "productName": "Hoa hồng đỏ",
          "quantity": 2,
          "unitPrice": 250000,
          "totalPrice": 500000
        }
      ]
    },
    "totalItems": 2
  },
  "error": null
}
```

**Test với cURL:**
```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

#### 3.2 Cập nhật số lượng sản phẩm
```http
PUT /api/cart/update
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 3
}
```

#### 3.3 Xóa sản phẩm khỏi giỏ hàng
```http
DELETE /api/cart/remove
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1
}
```

#### 3.4 Lấy giỏ hàng
```http
GET /api/cart/get
Authorization: Bearer <jwt-token>
```

#### 3.5 Xóa toàn bộ giỏ hàng
```http
DELETE /api/cart/clear
Authorization: Bearer <jwt-token>
```

#### 3.6 Đếm số sản phẩm trong giỏ hàng
```http
GET /api/cart/count
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "data": 5,
  "error": null
}
```

### 4. Order APIs (`/api/orders`)

**Lưu ý:** Tất cả Order APIs yêu cầu authentication.

#### 4.1 Tạo đơn hàng từ giỏ hàng
```http
POST /api/orders/create-from-cart
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "shippingPhone": "0123456789",
  "notes": "Giao hàng vào buổi sáng"
}
```

**Response:**
```json
{
  "data": {
    "success": true,
    "message": "Đơn hàng đã được tạo thành công",
    "order": {
      "id": 1,
      "orderNumber": "ORD-2024-001",
      "userId": 1,
      "totalAmount": 500000,
      "status": "PENDING",
      "orderDate": "2024-01-15T10:30:00",
      "items": [
        {
          "productId": 1,
          "productName": "Hoa hồng đỏ",
          "quantity": 2,
          "unitPrice": 250000,
          "totalPrice": 500000
        }
      ]
    }
  },
  "error": null
}
```

#### 4.2 Tạo đơn hàng trực tiếp
```http
POST /api/orders/create-direct
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
  "shippingPhone": "0123456789",
  "notes": "Giao hàng vào buổi sáng"
}
```

#### 4.3 Lấy danh sách đơn hàng của user
```http
GET /api/orders/list?page=0&size=10
Authorization: Bearer <jwt-token>
```

#### 4.4 Lấy chi tiết đơn hàng
```http
GET /api/orders/{orderId}
Authorization: Bearer <jwt-token>
```

#### 4.5 Hủy đơn hàng
```http
PUT /api/orders/{orderId}/cancel
Authorization: Bearer <jwt-token>
```

#### 4.6 Cập nhật trạng thái đơn hàng (Admin/Staff)
```http
PUT /api/orders/{orderId}/status?status=SHIPPED
Authorization: Bearer <jwt-token>
```

**Order Status:**
- `PENDING`: Chờ xử lý
- `CONFIRMED`: Đã xác nhận
- `SHIPPED`: Đang giao hàng
- `DELIVERED`: Đã giao hàng
- `CANCELLED`: Đã hủy

#### 4.7 Lấy đơn hàng theo trạng thái (Admin/Staff)
```http
GET /api/orders/status/{status}
Authorization: Bearer <jwt-token>
```

#### 4.8 Xử lý thanh toán cho đơn hàng
```http
POST /api/orders/{orderId}/payment?paymentMethod=COD
Authorization: Bearer <jwt-token>
```

**Parameters:**
- `orderId`: ID của đơn hàng
- `paymentMethod`: Phương thức thanh toán (COD, MOMO, BANK_TRANSFER, CREDIT_CARD)

**Response:**
```json
{
  "data": {
    "success": true,
    "message": "Đơn hàng đã được xác nhận. Bạn sẽ thanh toán khi nhận hàng.",
    "paymentDetails": {
      "method": "COD",
      "status": "PENDING",
      "amount": 500000,
      "processedAt": "2024-01-15T10:30:00",
      "note": "Thanh toán khi nhận hàng"
    }
  },
  "error": null
}
```

**Test với cURL:**
```bash
curl -X POST "http://localhost:8080/api/orders/1/payment?paymentMethod=COD" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 4.9 Tạo đơn hàng với xử lý thanh toán
```http
POST /api/orders/create-with-payment
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "orderRequest": {
    "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
    "shippingPhone": "0123456789",
    "notes": "Giao hàng vào buổi sáng"
  },
  "paymentMethod": "COD"
}
```

**Response:**
```json
{
  "data": {
    "order": {
      "id": 1,
      "orderNumber": "ORD-2024-001",
      "userId": 1,
      "totalAmount": 500000,
      "status": "CONFIRMED",
      "paymentMethod": "COD",
      "orderDate": "2024-01-15T10:30:00"
    },
    "payment": {
      "success": true,
      "message": "Đơn hàng đã được xác nhận. Bạn sẽ thanh toán khi nhận hàng.",
      "paymentDetails": {
        "method": "COD",
        "status": "PENDING",
        "amount": 500000
      }
    }
  },
  "error": null
}
```

**Test với cURL:**
```bash
curl -X POST http://localhost:8080/api/orders/create-with-payment \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderRequest": {
      "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
      "shippingPhone": "0123456789",
      "notes": "Giao hàng vào buổi sáng"
    },
    "paymentMethod": "COD"
  }'
```

### 5. Payment APIs (`/api/payment`)

#### 5.1 Lấy danh sách phương thức thanh toán
```http
GET /api/payment/methods
```

**Response:**
```json
{
  "data": {
    "COD": {
      "displayName": "Thanh toán khi nhận hàng",
      "englishName": "Cash on Delivery",
      "available": true,
      "statusMessage": "Sẵn sàng"
    },
    "MOMO": {
      "displayName": "Ví điện tử MoMo",
      "englishName": "MoMo Wallet",
      "available": false,
      "statusMessage": "Đang phát triển"
    },
    "BANK_TRANSFER": {
      "displayName": "Chuyển khoản ngân hàng",
      "englishName": "Bank Transfer",
      "available": false,
      "statusMessage": "Chưa hỗ trợ"
    },
    "CREDIT_CARD": {
      "displayName": "Thẻ tín dụng",
      "englishName": "Credit Card",
      "available": false,
      "statusMessage": "Chưa hỗ trợ"
    }
  },
  "error": null
}
```

**Test với cURL:**
```bash
curl http://localhost:8080/api/payment/methods
```

### 6. Wishlist APIs (`/api/wishlist`)

**Lưu ý:** Tất cả Wishlist APIs yêu cầu authentication với role CUSTOMER.

#### 6.1 Thêm vào wishlist
```http
POST /api/wishlist/add
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1
}
```

#### 6.2 Xóa khỏi wishlist
```http
DELETE /api/wishlist/remove
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1
}
```

#### 6.3 Toggle wishlist (thêm/xóa)
```http
POST /api/wishlist/toggle
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": 1
}
```

#### 6.4 Kiểm tra trạng thái wishlist
```http
GET /api/wishlist/status/{productId}
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "data": {
    "success": true,
    "message": "Sản phẩm đã có trong wishlist",
    "isInWishlist": true
  },
  "error": null
}
```

#### 6.5 Lấy danh sách wishlist
```http
GET /api/wishlist/list
Authorization: Bearer <jwt-token>
```

#### 6.6 Xóa toàn bộ wishlist
```http
DELETE /api/wishlist/clear
Authorization: Bearer <jwt-token>
```

### 7. System APIs

#### 7.1 Health Check
```http
GET /api/health
```

**Response:**
```json
{
  "status": "UP",
  "database": "connected"
}
```

#### 7.2 System Info
```http
GET /api/info
```

**Response:**
```json
{
  "application": "Flower Shop System",
  "description": "API for flower shop management",
  "developer": "Your Team"
}
```

## Web Pages (Non-API)

### Public Pages
- `GET /` - Trang chủ
- `GET /products` - Danh sách sản phẩm
- `GET /products/{id}` - Chi tiết sản phẩm
- `GET /products/categories` - Danh mục sản phẩm
- `GET /categories` - Danh mục sản phẩm (redirect)
- `GET /login` - Trang đăng nhập
- `GET /register` - Trang đăng ký
- `GET /forgot-password` - Trang quên mật khẩu
- `GET /reset-password` - Trang đặt lại mật khẩu

### Protected Pages (Yêu cầu authentication)
- `GET /cart` - Trang giỏ hàng
- `GET /orders` - Danh sách đơn hàng
- `GET /orders/{id}` - Chi tiết đơn hàng
- `GET /checkout` - Trang thanh toán (với form thanh toán COD/MoMo)
- `GET /wishlist` - Danh sách yêu thích
- `GET /account/profile` - Thông tin tài khoản
- `GET /account/settings` - Cài đặt tài khoản
- `GET /account/orders` - Lịch sử đơn hàng

## OAuth2 Integration

### Google OAuth2
- **Client ID:** `45091665731-js8rgkgu5c662khuebpcieikh47eps6t.apps.googleusercontent.com`
- **Redirect URI:** `{baseUrl}/login/oauth2/code/google`

### Facebook OAuth2
- **Client ID:** `1220413069770726`
- **Redirect URI:** `{baseUrl}/login/oauth2/code/facebook`

**OAuth2 Endpoints:**
- `GET /oauth2/authorization/google` - Đăng nhập Google
- `GET /oauth2/authorization/facebook` - Đăng nhập Facebook

## Error Handling

### Standard Error Response Format
```json
{
  "data": null,
  "error": "Error message in Vietnamese"
}
```

### HTTP Status Codes
- `200` - Success
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `500` - Internal Server Error

## Testing với cURL

### 1. Test đăng nhập và lấy token
```bash
# Đăng nhập
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' \
  -c cookies.txt

# Token sẽ được lưu trong cookie, sử dụng cho các request tiếp theo
```

### 2. Test thêm sản phẩm vào giỏ hàng
```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

### 3. Test lấy danh sách sản phẩm
```bash
curl "http://localhost:8080/products?page=0&size=10"
```

### 4. Test health check
```bash
curl http://localhost:8080/api/health
```

## Database Schema

### Main Tables
- `users` - Thông tin người dùng
- `products` - Sản phẩm
- `carts` - Giỏ hàng
- `cart_items` - Chi tiết giỏ hàng
- `orders` - Đơn hàng (có trường payment_method)
- `order_items` - Chi tiết đơn hàng
- `follows` - Wishlist (sản phẩm yêu thích)
- `reviews` - Đánh giá sản phẩm

### Payment Methods
- `COD` - Thanh toán khi nhận hàng (đã implement)
- `MOMO` - Ví điện tử MoMo (đang phát triển)
- `BANK_TRANSFER` - Chuyển khoản ngân hàng (chưa hỗ trợ)
- `CREDIT_CARD` - Thẻ tín dụng (chưa hỗ trợ)

### Database Connection
- **URL:** `jdbc:postgresql://localhost:5432/flower_shop_system`
- **Username:** `flower_admin`
- **Password:** `flower_password_2024`

## Security Features

1. **JWT Authentication** - Token-based authentication
2. **Password Encryption** - BCrypt password hashing
3. **CSRF Protection** - CSRF token validation
4. **Session Management** - Secure session handling
5. **OAuth2 Integration** - Google & Facebook login
6. **Role-based Access Control** - Different permissions for different roles
7. **Input Validation** - Request validation with Bean Validation
8. **SQL Injection Protection** - JPA/Hibernate ORM

## Email Service

### SMTP Configuration
- **Host:** `smtp.gmail.com`
- **Port:** `587`
- **Username:** `starshop.a.6868@gmail.com`
- **Security:** TLS/STARTTLS

### Email Templates
- OTP verification emails
- Welcome emails
- Password reset confirmation emails

## WebSocket Support

### WebSocket Endpoint
- `GET /ws` - WebSocket connection endpoint

### Features
- Real-time welcome messages
- Order status updates
- Live notifications

## Development Notes

### Logging
- **Level:** DEBUG for application, INFO for root
- **Format:** JSON format for errors as per rules.mdc
- **Location:** `app.log`

### File Upload
- **Max file size:** 10MB
- **Max request size:** 10MB

### Timezone
- **Default:** Asia/Ho_Chi_Minh

### Session Configuration
- **Cookie name:** JSESSIONID
- **Max age:** 3600 seconds
- **Same-site:** Lax
- **Secure:** false (development), true (production)

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích mã nguồn hiện tại. Một số tính năng có thể chưa được implement đầy đủ hoặc có thể thay đổi trong quá trình phát triển.
