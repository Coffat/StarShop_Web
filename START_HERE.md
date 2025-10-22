# 🎯 BẮT ĐẦU TỪ ĐÂY

## 👋 Chào mừng đến với hệ thống Flower Shop trên AWS!

Nếu bạn là người mới hoặc quay lại sau một thời gian, hãy đọc file này trước.

---

## 🌐 Thông Tin Nhanh

**Ứng dụng đang chạy tại**: http://starshop-hcmute.duckdns.org

**Trạng thái**: ✅ Đang hoạt động trên AWS ECS

---

## 📚 Đọc File Nào Trước?

### 1️⃣ **Lần đầu tiên** - Đọc theo thứ tự:

1. **START_HERE.md** (file này) - Tổng quan
2. **DEPLOYMENT_FILES_SUMMARY.md** - Hiểu các files có gì
3. **DEPLOYMENT_README.md** - Quick reference
4. **AWS_DEPLOYMENT_GUIDE.md** - Hướng dẫn chi tiết đầy đủ

### 2️⃣ **Khi cần deploy** - Làm theo:

1. **DEPLOYMENT_CHECKLIST.md** - Checklist từng bước
2. Chạy `./deploy.sh`

### 3️⃣ **Khi gặp vấn đề** - Tham khảo:

1. **AWS_DEPLOYMENT_GUIDE.md** → Phần "Troubleshooting"
2. **DEPLOYMENT_README.md** → Phần "Troubleshooting"

---

## 🚀 Quick Actions

### Deploy ứng dụng mới:
```bash
./deploy.sh
```

### Xem logs:
```bash
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2
```

### Backup database:
```bash
./backup-db.sh
```

### Cập nhật domain (khi IP thay đổi):
```bash
./update-duckdns.sh
```

---

## 📁 Cấu Trúc Files Deployment

```
demo_web/
├── START_HERE.md                    ← BẮT ĐẦU TỪ ĐÂY
├── DEPLOYMENT_FILES_SUMMARY.md      ← Tổng hợp tất cả files
├── DEPLOYMENT_README.md             ← Quick reference
├── DEPLOYMENT_CHECKLIST.md          ← Checklist khi deploy
├── AWS_DEPLOYMENT_GUIDE.md          ← Hướng dẫn chi tiết đầy đủ ⭐
│
├── deploy.sh                        ← Script deploy chính ⭐
├── update-duckdns.sh                ← Script cập nhật domain
├── backup-db.sh                     ← Script backup database
│
├── task-definition.json             ← Cấu hình ECS Task
├── Dockerfile                       ← Docker configuration
│
└── .gitignore.deployment            ← Files không commit
```

---

## 🎓 Học Nhanh - 5 Phút

### Hệ thống hoạt động như thế nào?

```
User truy cập
    ↓
starshop-hcmute.duckdns.org (Domain miễn phí)
    ↓
16.176.203.154:80 (Public IP)
    ↓
AWS ECS Fargate (Container chạy ứng dụng)
    ↓
AWS RDS PostgreSQL (Database)
```

### Khi nào cần làm gì?

| Tình huống | Làm gì |
|------------|--------|
| Sửa code backend | `./deploy.sh` |
| Thêm biến môi trường | Sửa `task-definition.json` → `./deploy.sh` |
| Database thay đổi | `./backup-db.sh` → chạy migration → `./deploy.sh` |
| IP thay đổi | `./update-duckdns.sh` |
| Xem logs | `aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2` |
| Có lỗi | Xem logs → Đọc Troubleshooting trong `AWS_DEPLOYMENT_GUIDE.md` |

---

## ⚡ Deploy Lần Đầu

Nếu đây là lần đầu bạn deploy:

```bash
# 1. Đảm bảo AWS CLI đã được cấu hình
aws configure list

# 2. Đảm bảo Docker đang chạy
docker ps

# 3. Kiểm tra task-definition.json có đúng không
cat task-definition.json

# 4. Deploy!
./deploy.sh

# 5. Kiểm tra
curl -I http://starshop-hcmute.duckdns.org
```

---

## 🆘 Cần Giúp Đỡ Ngay?

### Container không chạy?
```bash
# Xem logs
aws logs tail /ecs/flower-shop-task --since 10m --region ap-southeast-2
```

### Domain không truy cập được?
```bash
# Cập nhật DuckDNS
./update-duckdns.sh

# Test
curl -I http://starshop-hcmute.duckdns.org
```

### Muốn rollback?
```bash
# Xem các revisions
aws ecs list-task-definitions --family-prefix flower-shop-task --region ap-southeast-2

# Rollback về revision cũ (ví dụ revision 6)
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition flower-shop-task:6 \
  --force-new-deployment \
  --region ap-southeast-2
```

---

## 📊 Thông Tin Hệ Thống

| Thông tin | Giá trị |
|-----------|---------|
| **Domain** | http://starshop-hcmute.duckdns.org |
| **AWS Region** | ap-southeast-2 (Sydney) |
| **AWS Account** | 486918185988 |
| **ECS Cluster** | flower-shop-cluster |
| **ECS Service** | flower-shop-service |
| **Database** | flower-shop-db-instance |
| **Platform** | ECS Fargate ARM64 |

---

## ✅ Checklist Nhanh

Trước khi deploy, đảm bảo:
- [ ] Code đã test trên local
- [ ] Đã backup database (nếu có thay đổi schema)
- [ ] Biến môi trường mới đã được thêm (nếu có)
- [ ] Đã đọc `DEPLOYMENT_CHECKLIST.md`

---

## 📞 Liên Hệ

- **AWS Console**: https://console.aws.amazon.com/
- **Region**: ap-southeast-2
- **User**: vien

---

## 🎉 Bắt Đầu Thôi!

1. Đọc `DEPLOYMENT_FILES_SUMMARY.md` để hiểu các files
2. Đọc `AWS_DEPLOYMENT_GUIDE.md` để hiểu chi tiết
3. Thử deploy: `./deploy.sh`

**Chúc bạn deploy thành công! 🚀**

---

*Tạo ngày: 22/10/2025*  
*Cập nhật lần cuối: 22/10/2025*
