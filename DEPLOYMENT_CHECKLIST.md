# ✅ Deployment Checklist

## 📋 Trước khi Deploy

### Code Changes
- [ ] Code đã được test kỹ trên local
- [ ] Đã chạy unit tests và integration tests
- [ ] Đã review code changes
- [ ] Đã update documentation (nếu cần)

### Database
- [ ] Đã backup database hiện tại
  ```bash
  ./backup-db.sh
  ```
- [ ] Database migration scripts đã được review (nếu có)
- [ ] Đã test migration trên database test (nếu có)

### Configuration
- [ ] Biến môi trường mới đã được thêm vào `task-definition.json`
- [ ] Các API keys/secrets vẫn còn valid
- [ ] URLs và endpoints đã được cập nhật đúng

### Communication
- [ ] Đã thông báo cho team về deployment
- [ ] Đã schedule deployment time (nếu cần)
- [ ] Đã chuẩn bị rollback plan

## 🚀 Trong khi Deploy

- [ ] Chạy deployment script
  ```bash
  ./deploy.sh
  ```
- [ ] Monitor logs trong quá trình deploy
  ```bash
  aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2
  ```
- [ ] Kiểm tra service status
  ```bash
  aws ecs describe-services --cluster flower-shop-cluster --services flower-shop-service --region ap-southeast-2
  ```

## ✅ Sau khi Deploy

### Verification
- [ ] Container đã chạy thành công (RunningCount = 1)
- [ ] Không có error trong logs
- [ ] Domain vẫn truy cập được: http://starshop-hcmute.duckdns.org
- [ ] Test các chức năng chính:
  - [ ] Login/Register
  - [ ] Xem sản phẩm
  - [ ] Thêm vào giỏ hàng
  - [ ] Checkout
  - [ ] Payment (nếu có)

### Performance
- [ ] Response time bình thường
- [ ] Không có memory leak
- [ ] CPU usage ổn định

### Database
- [ ] Database connection hoạt động bình thường
- [ ] Queries chạy nhanh
- [ ] Không có deadlock

### Monitoring
- [ ] Setup CloudWatch alarms (nếu chưa có)
- [ ] Kiểm tra logs không có warning/error bất thường
- [ ] Monitor trong 30 phút đầu

## 🔄 Nếu có vấn đề - Rollback

- [ ] Xác định vấn đề cụ thể
- [ ] Quyết định rollback hay fix forward
- [ ] Nếu rollback:
  ```bash
  # Rollback về revision trước
  aws ecs update-service \
    --cluster flower-shop-cluster \
    --service flower-shop-service \
    --task-definition flower-shop-task:PREVIOUS_REVISION \
    --force-new-deployment \
    --region ap-southeast-2
  ```
- [ ] Restore database (nếu cần)
  ```bash
  pg_restore -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
             -U flower_admin \
             -d flower_shop_system \
             -c \
             backups/flower_shop_backup_TIMESTAMP.dump
  ```
- [ ] Verify rollback thành công
- [ ] Thông báo cho team

## 📝 Post-Deployment Tasks

- [ ] Update deployment log/notes
- [ ] Document any issues encountered
- [ ] Update version number (nếu có)
- [ ] Notify team deployment completed
- [ ] Schedule next backup
- [ ] Review and improve deployment process (nếu cần)

## 🔐 Security Checklist

- [ ] Không commit sensitive data (passwords, tokens) lên git
- [ ] API keys vẫn còn valid và secure
- [ ] Security groups được cấu hình đúng
- [ ] Database password đủ mạnh
- [ ] HTTPS enabled (nếu có SSL certificate)

## 📊 Metrics to Monitor

### Sau 1 giờ:
- [ ] Error rate < 1%
- [ ] Response time < 2s
- [ ] CPU usage < 70%
- [ ] Memory usage < 80%

### Sau 24 giờ:
- [ ] Không có crash/restart
- [ ] Database performance ổn định
- [ ] Không có customer complaints
- [ ] Logs clean (không có critical errors)

---

**Deployment Date**: _____________  
**Deployed By**: _____________  
**Version/Revision**: _____________  
**Notes**: _____________
