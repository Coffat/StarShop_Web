# 🔧 Hướng Dẫn Fix Deployment - Environment Variables

## 📋 Tổng Quan

Document này giải thích các vấn đề deployment đã được fix và cách sử dụng.

---

## ❌ Vấn Đề Trước Đây

### 1. **Thiếu hoàn toàn biến môi trường**
- GitHub Actions chỉ build và push Docker image
- Không có biến môi trường nào được truyền vào ECS Task
- Application chạy với profile `dev` thay vì `prod`
- Gây ra lỗi 500, không load được reviews, product details, v.v.

### 2. **Các tính năng bị ảnh hưởng**
- ❌ Database connection (dùng local config thay vì RDS)
- ❌ JWT authentication (thiếu secret key)
- ❌ Email service (không có SMTP config)
- ❌ MoMo payment (thiếu API keys)
- ❌ GHN shipping (thiếu token và shop ID)
- ❌ Gemini AI (thiếu API key)
- ❌ CORS (không cho phép domain production)
- ❌ OAuth (Google/Facebook login không hoạt động)

---

## ✅ Giải Pháp Đã Implement

### 1. **File `.env.production`**
- Chứa **tất cả 50+ biến môi trường** cần thiết
- Bao gồm: Database, JWT, Email, MoMo, GHN, Gemini AI, CORS, OAuth
- File này **KHÔNG bị gitignore** nữa (theo yêu cầu)

### 2. **File `task-definition.json`**
- AWS ECS Task Definition với đầy đủ environment variables
- Cấu hình:
  - CPU: 512 (0.5 vCPU)
  - Memory: 1024 MB (1 GB)
  - Network: awsvpc
  - Platform: Fargate
  - Health check endpoint: `/actuator/health`
  - Logging: CloudWatch Logs

### 3. **GitHub Actions Workflow Updated**
- Tự động lấy AWS Account ID
- Update task definition với image mới
- Register task definition revision mới
- Deploy lên ECS với environment variables đầy đủ

### 4. **Dockerfile Updated**
- Hỗ trợ nhận environment variables từ ECS
- Tự động chạy với profile `prod`
- Tối ưu startup time

---

## 🚀 Cách Deploy

### Option 1: Deploy qua GitHub Actions (Khuyến nghị)

1. **Commit và push code lên branch `vien-deploy`:**
```bash
git add .
git commit -m "Fix: Add complete environment variables for production"
git push origin vien-deploy
```

2. **GitHub Actions sẽ tự động:**
   - Build Docker image (ARM64 cho Fargate)
   - Push lên ECR
   - Update task definition với biến môi trường
   - Deploy lên ECS
   - Update DuckDNS với IP mới

3. **Kiểm tra deployment:**
   - Vào GitHub Actions tab để xem progress
   - Sau khi deploy xong, truy cập: http://starshop-hcmute.duckdns.org

### Option 2: Deploy Manual qua AWS Console

1. **Vào AWS ECS Console**
2. **Task Definitions → flower-shop-task → Create new revision**
3. **Copy nội dung từ `task-definition.json`**
4. **Thay `YOUR_AWS_ACCOUNT_ID` bằng AWS Account ID thực tế**
5. **Create → Update service với revision mới**

---

## 🔍 Kiểm Tra Sau Khi Deploy

### 1. **Health Check**
```bash
curl http://starshop-hcmute.duckdns.org/actuator/health
```
Expected: `{"status":"UP"}`

### 2. **Database Connection**
```bash
curl http://starshop-hcmute.duckdns.org/api/products
```
Expected: Danh sách sản phẩm (không lỗi 500)

### 3. **Product Details Page**
- Truy cập: http://starshop-hcmute.duckdns.org/product/{id}
- Kiểm tra: Reviews hiển thị đúng, không lỗi

### 4. **Authentication**
- Login với user/password
- Kiểm tra JWT token được generate

### 5. **Payment (MoMo)**
- Tạo đơn hàng
- Chọn thanh toán MoMo
- Kiểm tra redirect đến MoMo gateway

### 6. **Shipping (GHN)**
- Tính phí ship
- Kiểm tra provinces/districts/wards load được

### 7. **AI Features**
- Chat với AI
- Kiểm tra product recommendations

---

## 📝 Danh Sách Biến Môi Trường

### Core Application (5 biến)
- `SPRING_PROFILES_ACTIVE=prod`
- `APP_BASE_URL=http://starshop-hcmute.duckdns.org`
- `APP_UPLOAD_DIR=uploads`
- `SERVER_PORT=8080`
- `CORS_ALLOWED_ORIGINS=http://starshop-hcmute.duckdns.org`

### Database (5 biến)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=none`

### Security (2 biến)
- `JWT_SECRET`
- `JWT_EXPIRATION=86400000`

### Email (7 biến)
- `SPRING_MAIL_HOST=smtp.gmail.com`
- `SPRING_MAIL_PORT=587`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true`
- `SPRING_MAIL_FROM`

### MoMo Payment (9 biến)
- `MOMO_BASE_URL`
- `MOMO_PARTNER_CODE`
- `MOMO_ACCESS_KEY`
- `MOMO_SECRET_KEY`
- `MOMO_RETURN_URL`
- `MOMO_NOTIFY_URL`
- `MOMO_REQUEST_TYPE`
- `MOMO_ENDPOINT_CREATE`
- `MOMO_ENDPOINT_QUERY`

### GHN Shipping (11 biến)
- `GHN_BASE_URL`
- `GHN_TOKEN`
- `GHN_SHOP_ID`
- `GHN_SERVICE_TYPE_ID_DEFAULT`
- `GHN_FROM_PROVINCE_ID`
- `GHN_FROM_DISTRICT_ID`
- `GHN_FROM_WARD_CODE`
- `GHN_ENDPOINTS_PROVINCES`
- `GHN_ENDPOINTS_DISTRICTS`
- `GHN_ENDPOINTS_WARDS`
- `GHN_ENDPOINTS_CALCULATE_FEE`

### Gemini AI (7 biến)
- `GEMINI_API_KEY`
- `GEMINI_BASE_URL`
- `GEMINI_MODEL`
- `GEMINI_TEMPERATURE`
- `GEMINI_TOP_P`
- `GEMINI_MAX_TOKENS`
- `GEMINI_TIMEOUT_SECONDS`

### Swagger/OpenAPI (5 biến)
- `SWAGGER_SERVER_PROD_URL`
- `SPRINGDOC_API_DOCS_PATH`
- `SPRINGDOC_API_DOCS_ENABLED`
- `SPRINGDOC_SWAGGER_UI_PATH`
- `SPRINGDOC_SWAGGER_UI_ENABLED`

### Other (3 biến)
- `SOCKJS_CLIENT_LIBRARY_URL`
- `FACEBOOK_AVATAR_URL_TEMPLATE`
- `SERVER_COMPRESSION_ENABLED=true`

**Tổng cộng: 54 biến môi trường**

---

## ⚠️ Lưu Ý Quan Trọng

### 1. **OAuth Configuration**
Cần cấu hình redirect URIs trên:

**Google OAuth Console:**
- https://console.cloud.google.com/apis/credentials
- Thêm: `http://starshop-hcmute.duckdns.org/login/oauth2/code/google`
- Thêm: `http://starshop-hcmute.duckdns.org/oauth2/callback/google`

**Facebook Developers:**
- https://developers.facebook.com/apps
- Thêm: `http://starshop-hcmute.duckdns.org/login/oauth2/code/facebook`
- Thêm: `http://starshop-hcmute.duckdns.org/oauth2/callback/facebook`

### 2. **GHN Token**
- Token có thể hết hạn
- Kiểm tra tại: https://sso.ghn.vn/
- Nếu hết hạn, cập nhật trong `.env.production` và `task-definition.json`

### 3. **MoMo Environment**
- Hiện đang dùng **test environment**
- Khi go-live, cần chuyển sang production:
  - `MOMO_BASE_URL=https://payment.momo.vn`
  - Cập nhật partner code, access key, secret key từ MoMo production

### 4. **Database Password**
- Nên rotate định kỳ
- Sau khi đổi password, cập nhật trong task definition

### 5. **Gemini API Key**
- Kiểm tra quota và billing
- Monitor usage để tránh vượt quota

---

## 🐛 Troubleshooting

### Lỗi: Application không start
**Kiểm tra:**
1. CloudWatch Logs: `/ecs/flower-shop-task`
2. Xem có lỗi connection database không
3. Kiểm tra security group cho phép kết nối đến RDS

### Lỗi: 500 Internal Server Error
**Kiểm tra:**
1. Logs trong CloudWatch
2. Biến môi trường có được set đúng không
3. Database có accessible từ ECS task không

### Lỗi: Reviews không load
**Nguyên nhân:** Database connection hoặc JPA configuration
**Fix:** Kiểm tra `SPRING_DATASOURCE_*` variables

### Lỗi: Payment không hoạt động
**Nguyên nhân:** MoMo credentials không đúng hoặc return URL sai
**Fix:** Kiểm tra `MOMO_*` variables và return URL

### Lỗi: Shipping fee không tính được
**Nguyên nhân:** GHN token hết hạn hoặc FROM location không đúng
**Fix:** 
1. Lấy token mới từ GHN
2. Kiểm tra `GHN_FROM_*` variables

---

## 📊 Monitoring

### CloudWatch Logs
```bash
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2
```

### ECS Service Status
```bash
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2
```

### Task Status
```bash
aws ecs list-tasks \
  --cluster flower-shop-cluster \
  --service-name flower-shop-service \
  --region ap-southeast-2
```

---

## 🔐 Security Best Practices

### Khuyến nghị cho tương lai:

1. **Sử dụng AWS Secrets Manager**
   - Lưu sensitive data (passwords, API keys)
   - Tự động rotate secrets
   - Audit access logs

2. **Sử dụng Parameter Store**
   - Lưu non-sensitive config
   - Version control
   - Free tier available

3. **Enable HTTPS**
   - Sử dụng AWS Certificate Manager
   - Setup Application Load Balancer
   - Force HTTPS redirect

4. **Environment Separation**
   - Tách biệt dev/staging/prod
   - Sử dụng different AWS accounts
   - Implement proper IAM roles

---

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra CloudWatch Logs
2. Xem GitHub Actions logs
3. Kiểm tra ECS service events
4. Review task definition environment variables

---

**Last Updated:** 2025-10-24
**Version:** 1.0
**Status:** ✅ Production Ready
