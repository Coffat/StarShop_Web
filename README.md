# StarShop – Cửa hàng Hoa trực tuyến

> Dự án thương mại điện tử cho cửa hàng hoa với đầy đủ tính năng: danh mục – sản phẩm – giỏ hàng – đơn hàng – thanh toán MoMo (test), vận chuyển GHN, đăng nhập OAuth2, chat realtime (WebSocket) và trợ lý AI (Gemini). Triển khai nhanh bằng Docker + PostgreSQL, giao diện Thymeleaf.

---

## Nhóm thực hiện
- Vũ Toàn Thắng
- Nguyễn Nhật Huy
- Đặng Ngọc Tài
- Phan Quốc Viễn

---

## Badges

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)
![Build](https://img.shields.io/badge/Build-Maven-red)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Mục lục
- [Giới thiệu nhanh](#giới-thiệu-nhanh)
- [Kiến trúc và công nghệ](#kiến-trúc-và-công-nghệ)
- [Tính năng chính](#tính-năng-chính)
- [Ảnh chụp màn hình](#ảnh-chụp-màn-hình)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Thiết lập môi trường](#thiết-lập-môi-trường)
- [Chạy bằng Docker](#chạy-bằng-docker)
- [Chạy development](#chạy-development)
- [Cấu hình môi trường](#cấu-hình-môi-trường)
- [Tích hợp GHN (vận chuyển)](#tích-hợp-ghn-vận-chuyển)
- [Thanh toán MoMo (test) + SSE](#thanh-toán-momo-test--sse)
- [Chat realtime + AI Gemini](#chat-realtime--ai-gemini)
- [Tài liệu API (Swagger)](#tài-liệu-api-swagger)
- [Bảo mật & lưu ý triển khai](#bảo-mật--lưu-ý-triển-khai)
- [Khắc phục sự cố](#khắc-phục-sự-cố)
- [Giấy phép](#giấy-phép)
- [English summary](#english-summary)

---

## Giới thiệu nhanh
StarShop là ứng dụng web quản lý – bán hàng cho cửa hàng hoa. Ứng dụng tập trung vào trải nghiệm đặt hàng mượt mà (catalog, giỏ hàng, thanh toán), quản trị rõ ràng (dashboard admin/staff), giao tiếp khách hàng realtime (chat + AI hỗ trợ), và tích hợp các dịch vụ Việt Nam (GHN, MoMo) để phù hợp thực tế.

---

## Kiến trúc và công nghệ
- Backend: Spring Boot MVC, Service, Repository; WebSocket; SSE
- View: Thymeleaf + static assets (`src/main/resources/templates`, `static`)
- Database: PostgreSQL (Docker) – schema init tại `docker/init/*.sql`
- Security: Spring Security, OAuth2 Client (Google/Facebook), JWT cho API
- Tài liệu API: springdoc OpenAPI 3 (Swagger UI)
- AI: Google Gemini (model `gemini-2.5-flash`)
- Payment: MoMo test environment
- Shipping: GHN (địa chỉ 2/3 cấp, tính phí)

Thành phần chính (trích từ mã nguồn):
- `config/`: `SecurityConfig`, `WebSocketConfig`, `SwaggerConfig`, `MoMoProperties`, `GhnProperties`, `GeminiProperties`, `RestTemplateConfig`, `RateLimitConfig`
- `controller/`: `AuthController`, `PaymentController`, `ShippingController`, `LocationController`, `ChatWebSocketController`, `ChatApiController`, `SseController`, các controller Admin/Staff, Catalog, Orders, Products…
- `client/`: `GhnClient`, `GeminiClient`
- `entity/`: `User`, `Product`, `Order`, `OrderItem`, `Cart`, `Conversation`, `Message`, `HandoffQueue`, `Voucher`…
- `service/`, `repository/`: nghiệp vụ và truy cập dữ liệu

Phiên bản/stack:
- Java 17, Spring Boot 3.5.5, PostgreSQL 15
- Maven Wrapper (`./mvnw`)

---

## Tính năng chính

### Xác thực & Bảo mật
- Đăng nhập/Đăng ký với email & password
- OAuth2 (Google, Facebook)
- JWT Authentication cho API
- Quên mật khẩu với OTP qua email
- Spring Security với role-based access (CUSTOMER, STAFF, ADMIN)

### Quản lý Sản phẩm & Danh mục
- Danh mục sản phẩm (Catalogs)
- CRUD sản phẩm với hình ảnh
- Tìm kiếm và lọc sản phẩm
- Đánh giá & Review sản phẩm với AI analysis
- Wishlist (Danh sách yêu thích)
- Theo dõi sản phẩm (Follow)
- AI-powered product description generation

### Giỏ hàng & Đặt hàng
- Quản lý giỏ hàng real-time
- Checkout với nhiều địa chỉ giao hàng
- Hệ thống voucher/mã giảm giá
- Theo dõi đơn hàng
- Lịch sử đơn hàng

### Vận chuyển GHN
- Tích hợp API Giao Hàng Nhanh
- Quản lý địa chỉ 2 cấp và 3 cấp
- Tính phí vận chuyển tự động
- API lấy tỉnh/quận/phường

### Thanh toán
- COD (Thanh toán khi nhận hàng)
- MoMo (test environment)
- Cập nhật trạng thái real-time qua SSE
- Webhook callback cho MoMo

### Chat & AI Assistant
- WebSocket real-time chat
- AI Chatbot powered by Google Gemini 2.5-flash
- Hỗ trợ khách hàng tự động
- Chuyển tiếp đến nhân viên (handoff queue)
- Lưu lịch sử hội thoại
- PII detection (bảo vệ thông tin cá nhân)

### Dashboard Admin
- **Dashboard Analytics:**
  - Thống kê tổng quan (doanh thu, đơn hàng, khách hàng, sản phẩm)
  - Biểu đồ tương quan (correlation chart) - 7/30/90 ngày
  - Xu hướng doanh thu (revenue trend)
  - Biểu đồ trạng thái đơn hàng
  - AI Insights & recommendations (lazy-loaded)
  
- **Quản lý Đơn hàng:**
  - Xem, tìm kiếm, lọc đơn hàng
  - Cập nhật trạng thái đơn hàng
  - Xem chi tiết đơn hàng
  - Export Excel
  
- **Quản lý Sản phẩm:**
  - CRUD sản phẩm đầy đủ
  - Upload hình ảnh
  - Quản lý tồn kho
  - Cập nhật trạng thái sản phẩm
  - AI product description generator
  - Export Excel
  
- **Quản lý Danh mục (Catalogs):**
  - CRUD danh mục sản phẩm
  - Upload hình ảnh danh mục
  
- **Quản lý Khách hàng (Users):**
  - Xem danh sách khách hàng
  - Phân khúc khách hàng tự động (VIP, NEW, AT_RISK)
  - Lọc theo segment, ngày tham gia
  - Xem địa chỉ giao hàng
  - Export Excel
  
- **Quản lý Nhân viên (Employees):**
  - CRUD nhân viên
  - Quản lý mã nhân viên (employee_code)
  - Thông tin chức vụ, phòng ban
  - Lương theo giờ (salary_per_hour)
  - Trạng thái hoạt động
  
- **Quản lý Lương & Chấm công (Payroll):**
  - Xem tổng hợp chấm công theo tháng
  - Tính lương tự động dựa trên timesheet
  - Trạng thái lương (PENDING, PAID, OVERDUE)
  - Scheduler tự động tính lương cuối tháng
  - Export Excel
  
- **Quản lý Voucher:**
  - CRUD voucher/mã giảm giá
  - Loại giảm giá (PERCENTAGE, FIXED)
  - Điều kiện áp dụng
  - Thời hạn sử dụng
  - AI-powered voucher suggestions
  
- **Quản lý Đánh giá (Reviews):**
  - Xem tất cả đánh giá sản phẩm
  - AI analysis cho sentiment analysis
  - Phản hồi đánh giá
  - Lọc theo rating, sản phẩm
  
- **Marketing Campaigns:**
  - Tạo và quản lý chiến dịch marketing
  - Gửi email hàng loạt
  - Segmentation targeting (VIP, NEW, AT_RISK)
  - Email templates
  
- **Quản lý Nội dung (Content):**
  - Quản lý banner, hình ảnh
  - Nội dung tĩnh
  
- **Tài chính (Finance):**
  - Báo cáo doanh thu chi tiết
  - Thống kê theo thời gian
  
- **Cài đặt Hệ thống (Settings):**
  - Cấu hình chung
  - Cache management
  - System health check

### Dashboard Staff
- Xử lý đơn hàng được assigned
- Chat với khách hàng (realtime)
- Chấm công (Timesheet check-in/check-out)
- Xem bảng lương cá nhân
- Xử lý và phản hồi review
- Theo dõi hiệu suất cá nhân

### Tính năng khác
- Email notifications (SMTP)
- Export Excel (Apache POI) cho nhiều modules
- Swagger/OpenAPI documentation
- Rate limiting
- Caching (location data, shipping fees, product recommendations)
- Tự động tính lương theo giờ (Salary Scheduler)
- Customer segmentation AI (VIP, NEW, AT_RISK)
- AI monitoring service
- Session management
- Spring Boot Actuator (health checks)

---

## Ảnh chụp màn hình

> Một số ảnh chụp thực tế từ ứng dụng chạy cục bộ.

![Trang chủ](assets/home.png)

![Trang sản phẩm](assets/products.png)

![Đăng nhập](assets/login.png)

![Hồ sơ tài khoản](assets/profile.png)

![Giỏ hàng](assets/cart.png)

![Swagger UI](assets/swagger.png)


---

## Cấu trúc thư mục
Các đường dẫn đáng chú ý:

```text
src/main/java/com/example/demo/
  client/                    # GHN, Gemini HTTP clients
  config/                    # Security/WebSocket/Swagger/Props configs
  controller/                # Web + API controllers (Auth, Orders, Products, ...)
  dto/                       # DTOs (orders, shipping, chat, ...)
  entity/                    # Entities (User, Product, Order, Conversation, ...)
  repository/                # Spring Data repositories
  security/                  # JWT filter, etc.
  service/                   # Business services
  scheduler/                 # Salary scheduler
  util/                      # Utilities

src/main/resources/
  application.yml            # Cấu hình chính (ghi đè bằng ENV khi deploy)
  templates/                 # Thymeleaf views
    admin/                  # Admin dashboard pages
    staff/                  # Staff dashboard pages
    customer/               # Customer pages
    layouts/                # Layout templates
  static/                    # CSS, JS, images

docker/
  init/*.sql                 # Khởi tạo & seed PostgreSQL

docs/
  GHN_INTEGRATION.md         # Tài liệu tích hợp GHN chi tiết

docker-compose.yml           # PostgreSQL (port 5432), mount init SQL
```

---

## Thiết lập môi trường
Yêu cầu:
- JDK 17
- Docker & Docker Compose (để chạy PostgreSQL nhanh)
- Maven Wrapper (`./mvnw`) đã có sẵn
- VS Code Dev Tunnels (để nhận callback MoMo từ internet)

---

## Chạy bằng Docker
Khởi tạo PostgreSQL với schema/seed tự động:
```bash
docker compose up -d
```
- DB URL: `jdbc:postgresql://localhost:5432/flower_shop_system`
- User/Pass: `flower_admin` / `flower_password_2024` (cấu hình sẵn trong compose)

---

## Chạy development
1) Thiết lập VS Code Dev Tunnel (để có public URL cho callback MoMo):
```bash
./dev-vscode.sh set-url
# Nhập URL VS Code tunnel của bạn (ví dụ: https://abc123-8080.app.github.dev)
```

2) Chạy ứng dụng Spring Boot:
```bash
./dev-vscode.sh start
# hoặc: ./mvnw spring-boot:run
```

Ứng dụng chạy tại: `http://localhost:8080`

---

## Cấu hình môi trường
Tất cả giá trị nhạy cảm phải đặt qua biến môi trường khi triển khai. Không dùng hard-code/secrets mặc định của `application.yml` cho production.

Các ENV tiêu biểu:
```bash
# GHN
export GHN_TOKEN=your_ghn_token
export GHN_SHOP_ID=your_shop_id
export GHN_FROM_PROVINCE_ID=202
export GHN_FROM_DISTRICT_ID=3695
export GHN_FROM_WARD_CODE=90745

# MoMo (VS Code Dev Tunnel)
export VSCODE_FORWARD_URL=https://your-vscode-tunnel-url.example

# Gemini
export GEMINI_API_KEY=your_gemini_api_key
```

Kiểm tra và điều chỉnh tại `src/main/resources/application.yml`.

---

## Tích hợp GHN (vận chuyển)
- Tài liệu chi tiết: `docs/GHN_INTEGRATION.md`
- Controller liên quan: `LocationController`, `ShippingController`, `AddressController`
- Địa chỉ hỗ trợ 2 chế độ:
  - OLD (3 cấp): province_id, district_id, ward_code, address_detail
  - NEW (2 cấp): province_id, ward_code, address_detail (district_id tùy chọn)
- Endpoint tham khảo:
  - `GET /api/locations/provinces|districts|wards`
  - `POST /api/shipping/fee` – tính phí GHN

Thiết lập khuyến nghị: set `GHN_TOKEN`, `GHN_SHOP_ID` bằng ENV.

---

## Thanh toán MoMo (test) + SSE
- Cấu hình trong `application-dev.properties` (`momo.*`) và script `dev-vscode.sh` hỗ trợ in `return`/`notify` URLs
- Controller: `PaymentController`, SSE tại `SseController`
- Luồng phát triển:
  1. Thiết lập VS Code Dev Tunnel để có `https` public URL
  2. Chạy ứng dụng; `VSCODE_FORWARD_URL` sẽ dùng làm `notify-url`
  3. Thực hiện thanh toán, theo dõi trạng thái qua SSE

Endpoints tham khảo:
- `GET /payment/momo/return`
- `POST /payment/momo/notify`
- `GET /sse/orders/{orderId}` (SSE cập nhật trạng thái)

---

## Chat realtime + AI Gemini
- WebSocket: cấu hình tại `WebSocketConfig`, controller `ChatWebSocketController`
- API chat/AI: `ChatApiController`, client gọi Gemini: `GeminiClient`
- Mô hình hội thoại: `Conversation`, `Message`, `HandoffQueue` (chuyển giao nhân viên khi cần)

---

## Tài liệu API (Swagger)
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Bảo mật & lưu ý triển khai
- Bắt buộc: di chuyển toàn bộ secrets (OAuth, GHN, Gemini, JWT, SMTP…) ra ENV/Secret Manager khi deploy
- Bật HTTPS ở reverse proxy (Nginx/Caddy) và cấu hình cookie `secure`, `same-site`
- Kiểm tra roles/permissions tại `SecurityConfig`; OAuth2 login: Google/Facebook
- Tắt `devtools` và logging chi tiết trong production

---

## Khắc phục sự cố
- DB không kết nối: kiểm tra `docker compose ps`, cổng 5432, biến môi trường JDBC
- GHN 401/403: sai `GHN_TOKEN` hoặc `GHN_SHOP_ID`
- MoMo notify không gọi được: kiểm tra `VSCODE_FORWARD_URL` đã được set, VS Code tunnel còn hoạt động
- SSE không stream: đảm bảo endpoint `/sse/orders/{orderId}` và network không chặn
- OAuth2: kiểm tra callback URL trong Google/Facebook Console

---

## Giấy phép
[MIT License](LICENSE)

---

## English summary
StarShop is a Spring Boot e-commerce app for a flower shop. It features OAuth2 login, JWT-secured APIs, catalog/cart/orders, GHN shipping fee calculation, MoMo (test) payment with SSE updates, real-time chat with staff and an AI assistant powered by Gemini. PostgreSQL via Docker, Thymeleaf UI, OpenAPI/Swagger for docs. Configure all secrets via environment variables in production.

---

## License Details

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


**Copyright © 2025 StarShop Team**
- Vũ Toàn Thắng
- Nguyễn Nhật Huy  
- Đặng Ngọc Tài
- Phan Quốc Viễn

---


