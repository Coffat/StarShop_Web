# 📚 Tổng Hợp Files Deployment

## 📄 Danh Sách Files

### 1. **AWS_DEPLOYMENT_GUIDE.md** ⭐ QUAN TRỌNG NHẤT
   - **Mục đích**: Hướng dẫn chi tiết đầy đủ về deployment và maintenance
   - **Nội dung**:
     - Thông tin hệ thống
     - Kiến trúc triển khai
     - Hướng dẫn cập nhật code
     - Quản lý database
     - Quản lý biến môi trường
     - Monitoring và logs
     - Troubleshooting
     - Backup và restore
   - **Khi nào đọc**: Khi cần hiểu chi tiết về hệ thống hoặc gặp vấn đề

### 2. **DEPLOYMENT_README.md**
   - **Mục đích**: Quick reference guide
   - **Nội dung**: Các lệnh thường dùng, troubleshooting nhanh
   - **Khi nào đọc**: Khi cần tra cứu nhanh một lệnh

### 3. **DEPLOYMENT_CHECKLIST.md**
   - **Mục đích**: Checklist để đảm bảo không bỏ sót bước nào
   - **Nội dung**: Các bước cần làm trước, trong và sau deployment
   - **Khi nào dùng**: Mỗi lần deploy

### 4. **deploy.sh** ⭐ SCRIPT CHÍNH
   - **Mục đích**: Script tự động deploy
   - **Chức năng**:
     - Build Docker image
     - Push lên ECR
     - Update ECS service
     - Cập nhật DuckDNS
   - **Cách dùng**: `./deploy.sh`

### 5. **update-duckdns.sh**
   - **Mục đích**: Cập nhật domain khi IP thay đổi
   - **Chức năng**:
     - Lấy Public IP hiện tại của ECS
     - Cập nhật lên DuckDNS
   - **Cách dùng**: `./update-duckdns.sh`

### 6. **backup-db.sh**
   - **Mục đích**: Backup database
   - **Chức năng**:
     - Backup PostgreSQL database
     - Tự động xóa backup cũ > 7 ngày
   - **Cách dùng**: `./backup-db.sh`

### 7. **task-definition.json**
   - **Mục đích**: Cấu hình ECS Task
   - **Nội dung**: Biến môi trường, CPU, Memory, Image, etc.
   - **Khi nào sửa**: Khi cần thêm/sửa biến môi trường hoặc thay đổi resources

### 8. **.gitignore.deployment**
   - **Mục đích**: Danh sách files không nên commit lên git
   - **Cách dùng**: Copy nội dung vào `.gitignore` chính

## 🚀 Quy Trình Deploy Chuẩn

### Lần đầu tiên:
1. Đọc `AWS_DEPLOYMENT_GUIDE.md` để hiểu hệ thống
2. Kiểm tra `task-definition.json` có đúng không
3. Chạy `./deploy.sh`

### Các lần sau:
1. Sửa code
2. Test local
3. Mở `DEPLOYMENT_CHECKLIST.md` và làm theo
4. Chạy `./backup-db.sh` (nếu có thay đổi database)
5. Chạy `./deploy.sh`
6. Verify deployment thành công

## 📖 Kịch Bản Sử Dụng

### Kịch bản 1: Sửa bug trong code
```bash
# 1. Sửa code
# 2. Test local
# 3. Deploy
./deploy.sh
```

### Kịch bản 2: Thêm feature mới cần biến môi trường mới
```bash
# 1. Sửa code
# 2. Thêm biến vào task-definition.json
# 3. Deploy
./deploy.sh
```

### Kịch bản 3: Thay đổi database schema
```bash
# 1. Backup database
./backup-db.sh

# 2. Chạy migration
psql -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
     -U flower_admin \
     -d flower_shop_system \
     -f migration.sql

# 3. Deploy code mới
./deploy.sh
```

### Kịch bản 4: IP thay đổi
```bash
# Chỉ cần chạy
./update-duckdns.sh
```

### Kịch bản 5: Có lỗi sau khi deploy
```bash
# 1. Xem logs
aws logs tail /ecs/flower-shop-task --since 10m --region ap-southeast-2

# 2. Nếu cần rollback
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition flower-shop-task:PREVIOUS_REVISION \
  --force-new-deployment \
  --region ap-southeast-2
```

## 🔍 Tra Cứu Nhanh

### Xem logs:
```bash
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2
```

### Xem trạng thái:
```bash
aws ecs describe-services --cluster flower-shop-cluster --services flower-shop-service --region ap-southeast-2
```

### Lấy Public IP:
```bash
./update-duckdns.sh
```

### Backup database:
```bash
./backup-db.sh
```

### Deploy:
```bash
./deploy.sh
```

## 📞 Khi Cần Giúp Đỡ

1. **Đọc troubleshooting** trong `AWS_DEPLOYMENT_GUIDE.md`
2. **Xem logs** để tìm lỗi cụ thể
3. **Check AWS Console** để xem trạng thái resources
4. **Google error message** cụ thể

## ⚠️ Lưu Ý Quan Trọng

- ⚠️ **KHÔNG** commit `task-definition.json` lên git public (có passwords)
- ⚠️ **KHÔNG** commit `update-duckdns.sh` lên git public (có token)
- ✅ **LUÔN** backup database trước khi thay đổi schema
- ✅ **LUÔN** test trên local trước khi deploy
- ✅ **LUÔN** theo checklist khi deploy

## 📊 Maintenance Schedule

### Hàng ngày:
- Kiểm tra logs có error không
- Monitor performance

### Hàng tuần:
- Backup database: `./backup-db.sh`
- Review logs và performance

### Hàng tháng:
- Review và update dependencies
- Security audit
- Clean up old backups

---

**Tạo ngày**: 22/10/2025  
**Version**: 1.0
