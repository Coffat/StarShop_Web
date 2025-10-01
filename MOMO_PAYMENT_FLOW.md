# Luồng Thanh Toán MoMo - Đã Hoàn Thiện

## Tổng Quan
Đã hoàn thiện luồng thanh toán MoMo với điều hướng đúng và xử lý trạng thái đơn hàng khi thanh toán thành công/thất bại.

## Các Thay Đổi Đã Thực Hiện

### 1. ✅ Tạo Trang Kết Quả Thanh Toán
**File**: `/src/main/resources/templates/orders/payment-result.html`

**Tính năng**:
- Hiển thị kết quả thanh toán (thành công/thất bại)
- Hiển thị thông tin đơn hàng: mã đơn, trạng thái, tổng tiền, phương thức thanh toán
- Hiển thị mã giao dịch MoMo (nếu có)
- Các nút hành động:
  - **Thành công**: Xem đơn hàng, Đơn hàng của tôi, Về trang chủ
  - **Thất bại**: Thử lại thanh toán, Đơn hàng của tôi, Về trang chủ
- Responsive design với Bootstrap 5
- Icon và màu sắc phù hợp với trạng thái

### 2. ✅ Sửa PaymentController - Redirect thay vì JSON
**File**: `/src/main/java/com/example/demo/controller/PaymentController.java`

**Thay đổi chính**:
```java
// TRƯỚC: Trả về JSON ResponseEntity
@ResponseBody
public ResponseEntity<ResponseWrapper<Map<String, Object>>> momoReturn(...)

// SAU: Redirect về trang kết quả
public String momoReturn(HttpServletRequest request, Model model)
```

**Logic xử lý**:

#### Thanh Toán Thành Công (resultCode = "0"):
1. Cập nhật trạng thái đơn hàng: `PENDING` → `PROCESSING`
2. Lưu vào database
3. Set model attributes:
   - `success = true`
   - `message = "Thanh toán thành công!"`
   - `order` (OrderDTO)
   - `transactionId` (MoMo transaction ID)
4. Return `"orders/payment-result"`

#### Thanh Toán Thất Bại (resultCode != "0"):
1. Gọi `orderService.cancelOrderByPaymentFailure(orderId)`
2. Reload order để lấy trạng thái mới
3. Set model attributes:
   - `success = false`
   - `message = "Thanh toán thất bại: [lý do]"`
   - `order` (OrderDTO với status = CANCELLED)
   - `transactionId`
4. Return `"orders/payment-result"`

#### Xử Lý Lỗi:
- **Chữ ký không hợp lệ**: Hiển thị lỗi bảo mật
- **Không tìm thấy đơn hàng**: Hiển thị lỗi và hướng dẫn liên hệ hỗ trợ

### 3. ✅ Thêm Method Hủy Đơn Do Thanh Toán Thất Bại
**File**: `/src/main/java/com/example/demo/service/OrderService.java`

**Method mới**: `cancelOrderByPaymentFailure(Long orderId)`

**Chức năng**:
1. Kiểm tra đơn hàng tồn tại
2. Chỉ hủy nếu đơn hàng ở trạng thái `PENDING` (chờ thanh toán)
3. Cập nhật trạng thái: `PENDING` → `CANCELLED`
4. **Hoàn trả stock** cho từng sản phẩm trong đơn hàng:
   ```java
   product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity())
   ```
5. Lưu vào database
6. Log chi tiết cho tracking

**Đặc điểm**:
- Không kiểm tra quyền sở hữu (vì được gọi từ payment gateway)
- Void return type (không cần response)
- Xử lý exception an toàn với logging

### 4. ✅ SecurityConfig - Đã Có Sẵn
**File**: `/src/main/java/com/example/demo/config/SecurityConfig.java`

**Cấu hình hiện tại** (line 88):
```java
.requestMatchers("/payment/momo/**").permitAll()
```
- Cho phép truy cập callback endpoint mà không cần authentication
- CSRF đã được ignore cho `/api/payment/**` (line 52)

## Luồng Hoạt Động Chi Tiết

### A. Luồng Thanh Toán Thành Công
```
1. User checkout → Chọn MoMo
2. Frontend gọi: POST /api/orders/create-with-payment
3. Backend tạo đơn hàng với status = PENDING
4. PaymentService.processMoMoPayment():
   - Gọi MoMo API tạo payment request
   - Trả về payUrl
5. Frontend redirect user đến payUrl (MoMo app/web)
6. User thanh toán trên MoMo
7. MoMo callback: GET /payment/momo/return?resultCode=0&...
8. PaymentController.momoReturn():
   - Verify signature
   - Update order: PENDING → PROCESSING
   - Redirect: /orders/payment-result
9. User thấy trang thành công với thông tin đơn hàng
```

### B. Luồng Thanh Toán Thất Bại
```
1-6. [Giống luồng thành công]
7. MoMo callback: GET /payment/momo/return?resultCode=1001&...
8. PaymentController.momoReturn():
   - Verify signature
   - Gọi orderService.cancelOrderByPaymentFailure()
     → Update order: PENDING → CANCELLED
     → Restore stock cho tất cả sản phẩm
   - Redirect: /orders/payment-result
9. User thấy trang thất bại với:
   - Thông báo lỗi
   - Thông tin đơn hàng (status = CANCELLED)
   - Nút "Thử lại thanh toán"
```

## Database Schema - Phù Hợp

### Order Status Enum (PostgreSQL)
```sql
CREATE TYPE order_status AS ENUM ('pending', 'processing', 'shipped', 'completed', 'cancelled');
```

### Java Enum
```java
public enum OrderStatus {
    PENDING("pending"),       // Chờ thanh toán
    PROCESSING("processing"), // Đã thanh toán, đang xử lý
    SHIPPED("shipped"),       // Đang giao hàng
    COMPLETED("completed"),   // Hoàn thành
    CANCELLED("cancelled");   // Đã hủy (do user hoặc payment failure)
}
```

## API Endpoints

### Payment Callback (Public)
```
GET /payment/momo/return
- Query params: orderId, resultCode, signature, transId, message, ...
- Response: HTML page (redirect to payment-result)
- Authentication: Not required (permitAll)
```

### Payment Result Page (Public)
```
GET /orders/payment-result (rendered by controller)
- Model attributes:
  - success: boolean
  - message: String
  - order: OrderDTO
  - transactionId: String (optional)
  - pageTitle: String
```

## Testing Checklist

### ✅ Thanh Toán Thành Công
- [ ] Đơn hàng chuyển từ PENDING → PROCESSING
- [ ] Hiển thị trang success với thông tin đầy đủ
- [ ] Có nút "Xem đơn hàng" hoạt động
- [ ] Stock không bị hoàn trả

### ✅ Thanh Toán Thất Bại
- [ ] Đơn hàng chuyển từ PENDING → CANCELLED
- [ ] Stock được hoàn trả đúng số lượng
- [ ] Hiển thị trang failure với lý do lỗi
- [ ] Có nút "Thử lại thanh toán" redirect về /checkout

### ✅ Edge Cases
- [ ] Signature không hợp lệ → Hiển thị lỗi bảo mật
- [ ] Order không tồn tại → Hiển thị lỗi và hướng dẫn
- [ ] Order không ở trạng thái PENDING → Log warning, không crash

## Lợi Ích

### 1. User Experience
- ✅ Thông báo rõ ràng về kết quả thanh toán
- ✅ Không thấy JSON response trên màn hình
- ✅ Có các hành động tiếp theo rõ ràng
- ✅ Có thể thử lại thanh toán nếu thất bại

### 2. Business Logic
- ✅ Tự động hủy đơn khi thanh toán thất bại
- ✅ Hoàn trả stock để sản phẩm có thể bán lại
- ✅ Tracking đầy đủ với logs
- ✅ Không để đơn hàng "treo" ở trạng thái PENDING

### 3. Technical
- ✅ Code clean và maintainable
- ✅ Tách biệt logic hủy đơn (user vs payment failure)
- ✅ Transaction safety với @Transactional
- ✅ Error handling đầy đủ

## Notes

### MoMo Result Codes
- `0`: Thành công
- `9000`: Giao dịch được xác nhận thành công
- `1001`: Giao dịch thất bại do tài khoản không đủ số dư
- `1002`: Giao dịch thất bại do thẻ/tài khoản bị giới hạn
- `1004`: Giao dịch thất bại do vượt quá số tiền thanh toán
- `1005`: Giao dịch thất bại do url hoặc QR code đã hết hạn
- `1006`: Giao dịch thất bại do người dùng đã hủy
- `1017`: Giao dịch thất bại do người dùng từ chối xác nhận thanh toán
- `9999`: Giao dịch thất bại

### Security
- Signature verification để đảm bảo callback từ MoMo
- CSRF ignored cho callback endpoint
- Public access cho callback (MoMo server gọi)

### Future Improvements
- [ ] Thêm email notification khi thanh toán thành công/thất bại
- [ ] Thêm retry mechanism cho MoMo API calls
- [ ] Thêm webhook logging table để track tất cả callbacks
- [ ] Thêm admin dashboard để xem payment statistics
