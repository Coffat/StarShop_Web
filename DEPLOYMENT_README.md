# 🚀 Quick Start - Deploy & Maintain

## 📋 Files quan trọng

- `AWS_DEPLOYMENT_GUIDE.md` - Hướng dẫn chi tiết đầy đủ
- `deploy.sh` - Script tự động deploy
- `update-duckdns.sh` - Script cập nhật domain
- `backup-db.sh` - Script backup database
- `task-definition.json` - Cấu hình ECS Task

## 🔄 Deploy ứng dụng mới

```bash
# Cách 1: Sử dụng script tự động (Khuyến nghị)
./deploy.sh

# Cách 2: Thủ công từng bước
# 1. Build image
docker buildx build --platform linux/arm64 -t flower_shop_app .

# 2. Login ECR
aws ecr get-login-password --region ap-southeast-2 | \
  docker login --username AWS --password-stdin \
  486918185988.dkr.ecr.ap-southeast-2.amazonaws.com

# 3. Tag & Push
docker tag flower_shop_app:latest \
  486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app:latest
docker push 486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app:latest

# 4. Deploy
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --force-new-deployment \
  --region ap-southeast-2
```

## 🌐 Truy cập ứng dụng

**Domain**: http://starshop-hcmute.duckdns.org

## 📊 Xem logs

```bash
# Logs realtime
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2

# Logs 10 phút gần nhất
aws logs tail /ecs/flower-shop-task --since 10m --region ap-southeast-2
```

## 🗄️ Backup database

```bash
./backup-db.sh
```

## 🔧 Cập nhật biến môi trường

1. Sửa file `task-definition.json`
2. Đăng ký Task Definition mới:
```bash
aws ecs register-task-definition \
  --cli-input-json file://task-definition.json \
  --region ap-southeast-2
```
3. Cập nhật service với revision mới:
```bash
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition flower-shop-task:NEW_REVISION \
  --force-new-deployment \
  --region ap-southeast-2
```

## 🆘 Troubleshooting

### Container không chạy?
```bash
# Xem logs
aws logs tail /ecs/flower-shop-task --since 10m --region ap-southeast-2

# Xem events
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].events[0:5]'
```

### Domain không truy cập được?
```bash
# Cập nhật DuckDNS
./update-duckdns.sh

# Kiểm tra DNS
nslookup starshop-hcmute.duckdns.org
```

### Rollback về version cũ?
```bash
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition flower-shop-task:OLD_REVISION \
  --force-new-deployment \
  --region ap-southeast-2
```

## 📚 Xem thêm

Đọc file `AWS_DEPLOYMENT_GUIDE.md` để biết chi tiết đầy đủ về:
- Kiến trúc hệ thống
- Quản lý database
- Monitoring và logs
- Backup và restore
- Troubleshooting chi tiết

## 🔑 Thông tin quan trọng

- **AWS Account**: 486918185988
- **Region**: ap-southeast-2
- **Domain**: starshop-hcmute.duckdns.org
- **Database**: flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com
