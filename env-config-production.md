# 🔧 Production Environment Variables Configuration

## ✅ Cập Nhật Biến Môi Trường Cho Production

Các biến môi trường cần cập nhật trong `task-definition.json`:

### 🌐 URLs - Sử dụng Domain thay vì IP

```json
{
  "APP_BASE_URL": "http://starshop-hcmute.duckdns.org",
  "SWAGGER_SERVER_PROD_URL": "http://starshop-hcmute.duckdns.org",
  "CORS_ALLOWED_ORIGINS": "http://starshop-hcmute.duckdns.org"
}
```

### 💳 MoMo Payment URLs

```json
{
  "MOMO_BASE_URL": "https://test-payment.momo.vn",
  "MOMO_PARTNER_CODE": "MOMO",
  "MOMO_ACCESS_KEY": "F8BBA842ECF85",
  "MOMO_SECRET_KEY": "K951B6PE1waDMi640xX08PD3vg6EkVlz",
  "MOMO_RETURN_URL": "http://starshop-hcmute.duckdns.org/payment/momo/return",
  "MOMO_NOTIFY_URL": "http://starshop-hcmute.duckdns.org/payment/momo/notify"
}
```

### 🚚 GHN (Giao Hàng Nhanh) API

```json
{
  "GHN_BASE_URL": "https://online-gateway.ghn.vn/shiip/public-api",
  "GHN_TOKEN": "cd8acb1f-9e9c-11f0-b0e8-f2157cd9069b",
  "GHN_SHOP_ID": "6040148"
}
```

**⚠️ Lưu ý**: GHN token và shop ID có thể đã hết hạn. Nếu không hoạt động, cần:
1. Đăng nhập vào https://sso.ghn.vn/
2. Lấy token mới từ phần API Settings
3. Cập nhật lại biến môi trường

### 🔐 OAuth Configuration

#### Google OAuth
Cần cập nhật Authorized redirect URIs tại: https://console.cloud.google.com/apis/credentials

Thêm:
```
http://starshop-hcmute.duckdns.org/login/oauth2/code/google
http://starshop-hcmute.duckdns.org/oauth2/callback/google
```

#### Facebook OAuth  
Cần cập nhật Valid OAuth Redirect URIs tại: https://developers.facebook.com/apps

Thêm:
```
http://starshop-hcmute.duckdns.org/login/oauth2/code/facebook
http://starshop-hcmute.duckdns.org/oauth2/callback/facebook
```

### 📧 Email Configuration (Đã OK)

```json
{
  "SPRING_MAIL_HOST": "smtp.gmail.com",
  "SPRING_MAIL_PORT": "587",
  "SPRING_MAIL_USERNAME": "starshop.a.6868@gmail.com",
  "SPRING_MAIL_PASSWORD": "mqai nigf sweg imek",
  "SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH": "true",
  "SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE": "true",
  "SPRING_MAIL_FROM": "starshop.a.6868@gmail.com"
}
```

### 🗄️ Database Configuration (Đã OK)

```json
{
  "SPRING_DATASOURCE_URL": "jdbc:postgresql://flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com:5432/flower_shop_system",
  "SPRING_DATASOURCE_USERNAME": "flower_admin",
  "SPRING_DATASOURCE_PASSWORD": "flower_password_2024"
}
```

### 🤖 AI Configuration (Đã OK)

```json
{
  "GEMINI_API_KEY": "AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo"
}
```

### 🔒 Security Configuration (Đã OK)

```json
{
  "JWT_SECRET": "starshop_jwt_secret_key_2024_very_secure_key_for_production_use",
  "JWT_EXPIRATION": "86400000"
}
```

---

## 📝 Full Environment Variables List

```json
{
  "SPRING_PROFILES_ACTIVE": "prod",
  "SPRING_DATASOURCE_URL": "jdbc:postgresql://flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com:5432/flower_shop_system",
  "SPRING_DATASOURCE_USERNAME": "flower_admin",
  "SPRING_DATASOURCE_PASSWORD": "flower_password_2024",
  "SPRING_MAIL_HOST": "smtp.gmail.com",
  "SPRING_MAIL_PORT": "587",
  "SPRING_MAIL_USERNAME": "starshop.a.6868@gmail.com",
  "SPRING_MAIL_PASSWORD": "mqai nigf sweg imek",
  "SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH": "true",
  "SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE": "true",
  "SPRING_MAIL_FROM": "starshop.a.6868@gmail.com",
  "JWT_SECRET": "starshop_jwt_secret_key_2024_very_secure_key_for_production_use",
  "JWT_EXPIRATION": "86400000",
  "APP_BASE_URL": "http://starshop-hcmute.duckdns.org",
  "APP_UPLOAD_DIR": "uploads",
  "MOMO_BASE_URL": "https://test-payment.momo.vn",
  "MOMO_PARTNER_CODE": "MOMO",
  "MOMO_ACCESS_KEY": "F8BBA842ECF85",
  "MOMO_SECRET_KEY": "K951B6PE1waDMi640xX08PD3vg6EkVlz",
  "MOMO_RETURN_URL": "http://starshop-hcmute.duckdns.org/payment/momo/return",
  "MOMO_NOTIFY_URL": "http://starshop-hcmute.duckdns.org/payment/momo/notify",
  "GHN_BASE_URL": "https://online-gateway.ghn.vn/shiip/public-api",
  "GHN_TOKEN": "cd8acb1f-9e9c-11f0-b0e8-f2157cd9069b",
  "GHN_SHOP_ID": "6040148",
  "GEMINI_API_KEY": "AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo",
  "SPRING_JPA_HIBERNATE_DDL_AUTO": "none",
  "SPRING_JPA_SHOW_SQL": "false",
  "SPRING_JPA_OPEN_IN_VIEW": "false",
  "SWAGGER_SERVER_PROD_URL": "http://starshop-hcmute.duckdns.org",
  "SOCKJS_CLIENT_LIBRARY_URL": "https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js",
  "FACEBOOK_AVATAR_URL_TEMPLATE": "https://graph.facebook.com/{id}/picture?type=large",
  "CORS_ALLOWED_ORIGINS": "http://starshop-hcmute.duckdns.org"
}
```

---

## 🚀 Cách Áp Dụng

### Option 1: Cập nhật qua AWS Console (Dễ nhất)
1. Vào AWS ECS Console
2. Task Definitions → flower-shop-task → Create new revision
3. Scroll xuống Environment variables
4. Cập nhật các biến môi trường theo list trên
5. Create → Update service với revision mới

### Option 2: Cập nhật qua GitHub Actions (Tự động)
Thêm các biến môi trường vào GitHub Secrets, sau đó update workflow để inject vào task definition khi deploy.

---

## ✅ Checklist

- [ ] Cập nhật APP_BASE_URL với domain
- [ ] Cập nhật MOMO URLs với domain
- [ ] Cập nhật CORS_ALLOWED_ORIGINS với domain
- [ ] Cập nhật SWAGGER_SERVER_PROD_URL với domain
- [ ] Kiểm tra GHN token còn hạn không
- [ ] Cấu hình Google OAuth redirect URIs
- [ ] Cấu hình Facebook OAuth redirect URIs
- [ ] Deploy lại ứng dụng
- [ ] Test các tính năng: thanh toán, giao hàng, OAuth

---

**Tạo ngày**: 24/10/2025
