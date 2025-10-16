# StarShop – Nền tảng cửa hàng hoa (Spring Boot)

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
![License](https://img.shields.io/badge/License-MIT-lightgrey)

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
- Đăng nhập/Đăng ký: OAuth2 Google/Facebook, phiên đăng nhập, JWT cho API
- Danh mục – Sản phẩm: duyệt, chi tiết, gợi ý, quản trị sản phẩm
- Giỏ hàng – Đặt hàng – Voucher – Wishlist
- Vận chuyển GHN: địa chỉ 2/3 cấp, tính phí vận chuyển
- Thanh toán MoMo (test): callback qua ngrok, cập nhật realtime bằng SSE
- Chat realtime: WebSocket khách hàng – nhân viên, handoff hàng đợi; Trợ lý AI (Gemini) hỗ trợ trả lời và chuyển giao
- Dashboard Admin/Staff: theo vai trò
- Tài liệu API: `/swagger-ui.html`, OpenAPI `/v3/api-docs`

---

## Ảnh chụp màn hình

> Một số ảnh chụp thực tế từ ứng dụng chạy cục bộ.

![Trang chủ](assets/home.png)

![Trang sản phẩm](assets/products.png)

![Đăng nhập](assets/login.png)

![Hồ sơ tài khoản](assets/profile.png)

![Giỏ hàng](assets/cart.png)

![Swagger UI](assets/swagger.png)

> Swagger UI truy cập tại: `http://localhost:8080/swagger-ui.html`

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

src/main/resources/
  application.yml            # Cấu hình chính (ghi đè bằng ENV khi deploy)
  templates/                 # Thymeleaf views
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
- Ngrok (tùy chọn, để nhận callback MoMo từ internet)

Cài ngrok trên macOS:
```bash
brew install ngrok
```

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
1) Chạy ngrok trước (để có public URL cho callback MoMo):
```bash
./dev.sh ngrok
```
Lưu ý: script sẽ ghi public URL vào file `.ngrok-url` và export biến `NGROK_URL` khi bạn chạy app.

2) Chạy ứng dụng Spring Boot:
```bash
./mvnw spring-boot:run
# hoặc
./dev.sh app
# hoặc full: ./dev.sh start (ngrok + app)
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

# MoMo
export NGROK_URL=https://your-ngrok-url.example

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
- Cấu hình trong `application.yml` (`momo.*`) và script `dev.sh` hỗ trợ in `return`/`notify` URLs
- Controller: `PaymentController`, SSE tại `SseController`
- Luồng phát triển:
  1. Chạy ngrok để có `https` public URL
  2. Chạy ứng dụng; `NGROK_URL` sẽ dùng làm `notify-url`
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
- MoMo notify không gọi được: kiểm tra `NGROK_URL` còn sống, dashboard ngrok `http://localhost:4040`
- SSE không stream: đảm bảo endpoint `/sse/orders/{orderId}` và network không chặn
- OAuth2: kiểm tra callback URL trong Google/Facebook Console

---

## Giấy phép
MIT (hoặc cập nhật theo yêu cầu dự án của bạn).

---

## English summary
StarShop is a Spring Boot e-commerce app for a flower shop. It features OAuth2 login, JWT-secured APIs, catalog/cart/orders, GHN shipping fee calculation, MoMo (test) payment with SSE updates, real-time chat with staff and an AI assistant powered by Gemini. PostgreSQL via Docker, Thymeleaf UI, OpenAPI/Swagger for docs. Configure all secrets via environment variables in production.

---

## Gợi ý ảnh/chụp màn hình (tùy chọn)
Bạn có thể thêm thư mục `assets/` chứa các ảnh chụp:
- `assets/home.png` – Trang chủ
- `assets/admin-dashboard.png` – Dashboard admin
- `assets/staff.png` – Màn hình nhân viên
- `assets/chat.png` – Widget chat
- `assets/checkout.png` – Luồng checkout (MoMo)

Sau đó chèn vào README như:

```markdown
![Home](assets/home.png)
```

> Nếu cần, có thể dùng ngrok để truy cập từ trình duyệt và chụp ảnh màn hình UI, sau đó commit vào `assets/`. 
