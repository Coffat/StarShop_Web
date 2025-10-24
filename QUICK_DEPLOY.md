# 🚀 Quick Deploy Guide

## Deployment đã được fix! Bây giờ chỉ cần 2 bước:

---

## ⚡ Cách 1: Deploy Tự Động (Khuyến nghị)

### Bước 1: Push code lên GitHub
```bash
git add .
git commit -m "Fix: Add complete environment variables for production"
git push origin vien-deploy
```

### Bước 2: Đợi GitHub Actions
- Vào tab **Actions** trên GitHub
- Xem workflow **Deploy to AWS ECS** đang chạy
- Sau 5-10 phút, deployment hoàn tất
- Truy cập: **http://starshop-hcmute.duckdns.org**

---

## 🛠️ Cách 2: Deploy Manual (Nếu cần)

### Bước 1: Update task definition
```bash
./update-task-definition.sh
```

### Bước 2: Kiểm tra
```bash
curl http://starshop-hcmute.duckdns.org/actuator/health
```

---

## ✅ Kiểm Tra Nhanh

### 1. Health Check
```bash
curl http://starshop-hcmute.duckdns.org/actuator/health
```
✅ Kết quả: `{"status":"UP"}`

### 2. Products API
```bash
curl http://starshop-hcmute.duckdns.org/api/products
```
✅ Kết quả: Danh sách sản phẩm (không lỗi 500)

### 3. Product Detail Page
- Mở: http://starshop-hcmute.duckdns.org/product/1
- ✅ Reviews hiển thị
- ✅ Không có lỗi 500

---

## 🔍 Xem Logs (Nếu có lỗi)

```bash
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2
```

---

## 📝 Những Gì Đã Fix

### ✅ Đã thêm 54 biến môi trường:
- Database connection (RDS)
- JWT authentication
- Email service (SMTP)
- MoMo payment gateway
- GHN shipping API
- Gemini AI
- CORS configuration
- OAuth (Google/Facebook)
- Swagger/OpenAPI
- WebSocket

### ✅ Files đã tạo/cập nhật:
- `.env.production` - Tất cả biến môi trường
- `task-definition.json` - ECS task config với env vars
- `.github/workflows/deploy-to-aws.yml` - Auto deploy workflow
- `Dockerfile` - Hỗ trợ environment variables
- `update-task-definition.sh` - Script deploy manual
- `DEPLOYMENT_FIX_GUIDE.md` - Hướng dẫn chi tiết

---

## ⚠️ Lưu Ý

### 1. OAuth Configuration
Cần cấu hình redirect URIs:

**Google:** https://console.cloud.google.com/apis/credentials
- Thêm: `http://starshop-hcmute.duckdns.org/login/oauth2/code/google`

**Facebook:** https://developers.facebook.com/apps
- Thêm: `http://starshop-hcmute.duckdns.org/login/oauth2/code/facebook`

### 2. GHN Token
- Có thể hết hạn
- Kiểm tra tại: https://sso.ghn.vn/
- Nếu hết hạn, cập nhật trong `.env.production` và `task-definition.json`

### 3. MoMo Test Environment
- Hiện đang dùng test environment
- Khi go-live, cần chuyển sang production

---

## 🐛 Troubleshooting

### Lỗi 500 Internal Server Error
```bash
# Xem logs
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2

# Kiểm tra service status
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2
```

### Reviews không load
- Kiểm tra database connection trong logs
- Verify `SPRING_DATASOURCE_*` variables trong task definition

### Payment không hoạt động
- Kiểm tra `MOMO_*` variables
- Verify return URL và notify URL

---

## 📞 Need Help?

Xem file `DEPLOYMENT_FIX_GUIDE.md` để biết chi tiết đầy đủ.

---

**Status:** ✅ Ready to Deploy
**Last Updated:** 2025-10-24
