# 🚀 Hướng Dẫn Deploy và Maintain Ứng Dụng Flower Shop trên AWS ECS

## 📋 Mục Lục
- [Thông Tin Hệ Thống](#thông-tin-hệ-thống)
- [Kiến Trúc Triển Khai](#kiến-trúc-triển-khai)
- [Cập Nhật Code Backend](#cập-nhật-code-backend)
- [Quản Lý Database](#quản-lý-database)
- [Quản Lý Biến Môi Trường](#quản-lý-biến-môi-trường)
- [Monitoring và Logs](#monitoring-và-logs)
- [Troubleshooting](#troubleshooting)
- [Backup và Restore](#backup-và-restore)

---

## 🌐 Thông Tin Hệ Thống

### **Thông tin truy cập:**
- **Domain**: http://starshop-hcmute.duckdns.org
- **Public IP hiện tại**: 16.176.203.154
- **Port**: 80 (HTTP)
- **AWS Region**: ap-southeast-2 (Sydney)
- **AWS Account ID**: 486918185988

### **Tài nguyên AWS:**

| Tài nguyên | Tên | ID/ARN |
|------------|-----|--------|
| ECS Cluster | flower-shop-cluster | - |
| ECS Service | flower-shop-service | - |
| Task Definition | flower-shop-task | Revision: 7 |
| ECR Repository | flower_shop_app | 486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app |
| RDS Database | flower-shop-db-instance | flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com |
| Security Group (ECS) | flower-shop-sg | sg-09b68a2f85c567cf6 |
| Security Group (RDS) | - | sg-028b57fab2c0669a0 |
| CloudWatch Log Group | /ecs/flower-shop-task | - |

### **Database:**
- **Engine**: PostgreSQL 17.4
- **Endpoint**: flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com:5432
- **Database Name**: flower_shop_system
- **Username**: flower_admin
- **Password**: flower_password_2024

### **DuckDNS:**
- **Domain**: starshop-hcmute.duckdns.org
- **Token**: 31b32e5b-c7a7-41f6-8474-0a1b46f57526

---

## 🏗️ Kiến Trúc Triển Khai

```
Internet
    ↓
DuckDNS (starshop-hcmute.duckdns.org)
    ↓
Public IP (16.176.203.154:80)
    ↓
Security Group (sg-09b68a2f85c567cf6)
    ↓
ECS Fargate (ARM64)
    ├── Container: flower-shop-container
    ├── Image: ECR (flower_shop_app:latest)
    └── Logs: CloudWatch (/ecs/flower-shop-task)
    ↓
Security Group (sg-028b57fab2c0669a0)
    ↓
RDS PostgreSQL (flower-shop-db-instance)
```

---

## 🔄 Cập Nhật Code Backend

### **Quy trình cập nhật khi sửa code:**

#### **Bước 1: Sửa code và test local**
```bash
# Test ứng dụng trên local trước
./mvnw spring-boot:run

# Hoặc build và chạy với Docker
docker build -t flower_shop_app .
docker run -p 8080:8080 flower_shop_app
```

#### **Bước 2: Build Docker Image mới**
```bash
# Build image cho ARM64 (vì ECS đang dùng ARM64)
docker buildx build --platform linux/arm64 -t flower_shop_app .

# Hoặc nếu muốn build cho cả AMD64 và ARM64
docker buildx build --platform linux/amd64,linux/arm64 -t flower_shop_app .
```

#### **Bước 3: Tag và Push lên ECR**
```bash
# 1. Đăng nhập vào ECR
aws ecr get-login-password --region ap-southeast-2 | \
  docker login --username AWS --password-stdin \
  486918185988.dkr.ecr.ap-southeast-2.amazonaws.com

# 2. Tag image
docker tag flower_shop_app:latest \
  486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app:latest

# 3. Push lên ECR
docker push 486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app:latest
```

#### **Bước 4: Deploy lên ECS**
```bash
# Force new deployment để ECS pull image mới
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --force-new-deployment \
  --region ap-southeast-2
```

#### **Bước 5: Kiểm tra deployment**
```bash
# Xem trạng thái service
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].{Status:status,Running:runningCount,Pending:pendingCount}' \
  --output table

# Đợi khoảng 2-3 phút để container mới khởi động
sleep 120

# Xem logs
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2
```

#### **Bước 6: Cập nhật DuckDNS (nếu IP thay đổi)**
```bash
# Chạy script tự động cập nhật
./update-duckdns.sh
```

#### **Bước 7: Verify deployment**
```bash
# Test HTTP
curl -I http://starshop-hcmute.duckdns.org

# Hoặc mở trình duyệt
open http://starshop-hcmute.duckdns.org
```

---

## 🔄 Quy Trình Cập Nhật Nhanh (Script)

Tạo file `deploy.sh` để tự động hóa:

```bash
#!/bin/bash

echo "🚀 Bắt đầu deploy..."

# 1. Build image
echo "📦 Building Docker image..."
docker buildx build --platform linux/arm64 -t flower_shop_app .

# 2. Login ECR
echo "🔐 Logging into ECR..."
aws ecr get-login-password --region ap-southeast-2 | \
  docker login --username AWS --password-stdin \
  486918185988.dkr.ecr.ap-southeast-2.amazonaws.com

# 3. Tag image
echo "🏷️  Tagging image..."
docker tag flower_shop_app:latest \
  486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app:latest

# 4. Push to ECR
echo "⬆️  Pushing to ECR..."
docker push 486918185988.dkr.ecr.ap-southeast-2.amazonaws.com/flower_shop_app:latest

# 5. Update ECS service
echo "🔄 Updating ECS service..."
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --force-new-deployment \
  --region ap-southeast-2 \
  --query 'service.serviceName' \
  --output text

# 6. Wait for deployment
echo "⏳ Waiting for deployment (2 minutes)..."
sleep 120

# 7. Check status
echo "✅ Checking deployment status..."
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].{Status:status,Running:runningCount}' \
  --output table

# 8. Update DuckDNS
echo "🦆 Updating DuckDNS..."
./update-duckdns.sh

echo "🎉 Deployment completed!"
echo "🌐 Visit: http://starshop-hcmute.duckdns.org"
```

Sử dụng:
```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 🗄️ Quản Lý Database

### **Kết nối vào Database:**

```bash
# Sử dụng psql
psql -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
     -U flower_admin \
     -d flower_shop_system \
     -p 5432

# Nhập password: flower_password_2024
```

### **Chạy Migration/Schema Changes:**

```bash
# 1. Kết nối vào database
psql -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
     -U flower_admin \
     -d flower_shop_system

# 2. Chạy SQL file
\i /path/to/migration.sql

# Hoặc
psql -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
     -U flower_admin \
     -d flower_shop_system \
     -f migration.sql
```

### **Backup Database:**

```bash
# Backup toàn bộ database
pg_dump -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
        -U flower_admin \
        -d flower_shop_system \
        -F c \
        -f backup_$(date +%Y%m%d_%H%M%S).dump

# Backup chỉ schema
pg_dump -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
        -U flower_admin \
        -d flower_shop_system \
        --schema-only \
        -f schema_backup.sql

# Backup chỉ data
pg_dump -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
        -U flower_admin \
        -d flower_shop_system \
        --data-only \
        -f data_backup.sql
```

### **Restore Database:**

```bash
# Restore từ dump file
pg_restore -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
           -U flower_admin \
           -d flower_shop_system \
           -c \
           backup_20241022.dump

# Restore từ SQL file
psql -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
     -U flower_admin \
     -d flower_shop_system \
     -f backup.sql
```

---

## ⚙️ Quản Lý Biến Môi Trường

### **Danh sách biến môi trường hiện tại:**

File: `task-definition.json`

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
  "APP_BASE_URL": "http://16.176.203.154:8080",
  "APP_UPLOAD_DIR": "uploads",
  "MOMO_BASE_URL": "https://test-payment.momo.vn",
  "MOMO_PARTNER_CODE": "MOMO",
  "MOMO_ACCESS_KEY": "F8BBA842ECF85",
  "MOMO_SECRET_KEY": "K951B6PE1waDMi640xX08PD3vg6EkVlz",
  "MOMO_RETURN_URL": "http://16.176.203.154:8080/payment/momo/return",
  "MOMO_NOTIFY_URL": "http://16.176.203.154:8080/payment/momo/notify",
  "GHN_BASE_URL": "https://online-gateway.ghn.vn/shiip/public-api",
  "GHN_TOKEN": "cd8acb1f-9e9c-11f0-b0e8-f2157cd9069b",
  "GHN_SHOP_ID": "6040148",
  "GEMINI_API_KEY": "AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo",
  "SPRING_JPA_HIBERNATE_DDL_AUTO": "none",
  "SPRING_JPA_SHOW_SQL": "false",
  "SPRING_JPA_OPEN_IN_VIEW": "false",
  "SWAGGER_SERVER_PROD_URL": "http://16.176.203.154:8080",
  "SOCKJS_CLIENT_LIBRARY_URL": "https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js",
  "FACEBOOK_AVATAR_URL_TEMPLATE": "https://graph.facebook.com/{id}/picture?type=large",
  "CORS_ALLOWED_ORIGINS": "http://16.176.203.154:8080"
}
```

### **Cập nhật biến môi trường:**

#### **Cách 1: Sửa file task-definition.json**

```bash
# 1. Sửa file task-definition.json
nano task-definition.json

# 2. Đăng ký Task Definition mới
aws ecs register-task-definition \
  --cli-input-json file://task-definition.json \
  --region ap-southeast-2

# 3. Cập nhật service với revision mới (ví dụ revision 8)
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition flower-shop-task:8 \
  --force-new-deployment \
  --region ap-southeast-2
```

#### **Cách 2: Sử dụng AWS Console**

1. Vào **ECS** → **Task Definitions** → **flower-shop-task**
2. Chọn revision mới nhất → **Create new revision**
3. Scroll xuống **Environment variables**
4. Thêm/Sửa/Xóa biến môi trường
5. Click **Create**
6. Vào **Services** → **flower-shop-service** → **Update**
7. Chọn **Revision** mới vừa tạo
8. Click **Update**

### **Thêm biến môi trường mới:**

Nếu code cần thêm biến môi trường mới:

1. Thêm vào `task-definition.json`:
```json
{
  "name": "NEW_VARIABLE_NAME",
  "value": "new_value"
}
```

2. Đăng ký và deploy:
```bash
aws ecs register-task-definition --cli-input-json file://task-definition.json --region ap-southeast-2
aws ecs update-service --cluster flower-shop-cluster --service flower-shop-service --task-definition flower-shop-task:NEW_REVISION --force-new-deployment --region ap-southeast-2
```

---

## 📊 Monitoring và Logs

### **Xem Logs:**

```bash
# Xem logs realtime
aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2

# Xem logs 10 phút gần nhất
aws logs tail /ecs/flower-shop-task --since 10m --region ap-southeast-2

# Xem logs 1 giờ gần nhất
aws logs tail /ecs/flower-shop-task --since 1h --region ap-southeast-2

# Lọc logs có chứa "ERROR"
aws logs tail /ecs/flower-shop-task --since 30m --region ap-southeast-2 | grep ERROR

# Lọc logs có chứa "Exception"
aws logs tail /ecs/flower-shop-task --since 1h --region ap-southeast-2 | grep -i exception
```

### **Xem trạng thái Service:**

```bash
# Trạng thái tổng quan
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].{Status:status,DesiredCount:desiredCount,RunningCount:runningCount,PendingCount:pendingCount}' \
  --output table

# Xem events gần nhất
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].events[0:5]' \
  --output table

# Xem thông tin tasks
aws ecs list-tasks \
  --cluster flower-shop-cluster \
  --service-name flower-shop-service \
  --region ap-southeast-2

# Xem chi tiết task
aws ecs describe-tasks \
  --cluster flower-shop-cluster \
  --tasks TASK_ARN \
  --region ap-southeast-2
```

### **Lấy Public IP hiện tại:**

```bash
# Sử dụng script
./update-duckdns.sh

# Hoặc thủ công
TASK_ARN=$(aws ecs list-tasks --cluster flower-shop-cluster --service-name flower-shop-service --region ap-southeast-2 --query 'taskArns[0]' --output text | awk -F'/' '{print $NF}')
ENI_ID=$(aws ecs describe-tasks --cluster flower-shop-cluster --tasks $TASK_ARN --region ap-southeast-2 --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text)
aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --region ap-southeast-2 --query 'NetworkInterfaces[0].Association.PublicIp' --output text
```

---

## 🔧 Troubleshooting

### **Container không khởi động:**

```bash
# 1. Xem logs để tìm lỗi
aws logs tail /ecs/flower-shop-task --since 10m --region ap-southeast-2 | tail -100

# 2. Kiểm tra events
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].events[0:10]'

# 3. Kiểm tra task definition
aws ecs describe-task-definition \
  --task-definition flower-shop-task \
  --region ap-southeast-2

# 4. Kiểm tra security groups
aws ec2 describe-security-groups \
  --group-ids sg-09b68a2f85c567cf6 \
  --region ap-southeast-2
```

### **Không kết nối được database:**

```bash
# 1. Kiểm tra security group của RDS
aws ec2 describe-security-groups \
  --group-ids sg-028b57fab2c0669a0 \
  --region ap-southeast-2

# 2. Kiểm tra RDS có đang chạy không
aws rds describe-db-instances \
  --db-instance-identifier flower-shop-db-instance \
  --region ap-southeast-2 \
  --query 'DBInstances[0].{Status:DBInstanceStatus,Endpoint:Endpoint.Address}'

# 3. Test kết nối từ local
psql -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
     -U flower_admin \
     -d flower_shop_system
```

### **Ứng dụng chạy chậm:**

```bash
# 1. Kiểm tra CPU và Memory usage
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2

# 2. Xem logs để tìm bottleneck
aws logs tail /ecs/flower-shop-task --since 30m --region ap-southeast-2 | grep -i "slow\|timeout"

# 3. Tăng CPU/Memory trong task definition
# Sửa file task-definition.json:
# "cpu": "2048",  # từ 1024 lên 2048
# "memory": "4096"  # từ 2048 lên 4096
```

### **Domain không truy cập được:**

```bash
# 1. Kiểm tra DNS
nslookup starshop-hcmute.duckdns.org

# 2. Kiểm tra IP có đúng không
dig starshop-hcmute.duckdns.org

# 3. Cập nhật DuckDNS
./update-duckdns.sh

# 4. Test HTTP
curl -I http://starshop-hcmute.duckdns.org

# 5. Kiểm tra security group có mở port 80 không
aws ec2 describe-security-groups \
  --group-ids sg-09b68a2f85c567cf6 \
  --region ap-southeast-2 \
  --query 'SecurityGroups[0].IpPermissions[?FromPort==`80`]'
```

### **Lỗi "Could not resolve placeholder":**

Thiếu biến môi trường. Thêm vào `task-definition.json` và deploy lại.

```bash
# 1. Xem lỗi cụ thể
aws logs tail /ecs/flower-shop-task --since 5m --region ap-southeast-2 | grep "Could not resolve placeholder"

# 2. Thêm biến vào task-definition.json
# 3. Deploy lại
```

---

## 💾 Backup và Restore

### **Backup Database định kỳ:**

Tạo script `backup-db.sh`:

```bash
#!/bin/bash

BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/flower_shop_backup_$TIMESTAMP.dump"

mkdir -p $BACKUP_DIR

echo "🗄️  Backing up database..."

pg_dump -h flower-shop-db-instance.c5gc6e8m62lz.ap-southeast-2.rds.amazonaws.com \
        -U flower_admin \
        -d flower_shop_system \
        -F c \
        -f $BACKUP_FILE

if [ $? -eq 0 ]; then
    echo "✅ Backup successful: $BACKUP_FILE"
    
    # Xóa backup cũ hơn 7 ngày
    find $BACKUP_DIR -name "*.dump" -mtime +7 -delete
    echo "🧹 Cleaned up old backups"
else
    echo "❌ Backup failed"
    exit 1
fi
```

Chạy định kỳ với cron:
```bash
# Mở crontab
crontab -e

# Thêm dòng này để backup mỗi ngày lúc 2h sáng
0 2 * * * /path/to/backup-db.sh
```

### **Backup Docker Image:**

```bash
# Save image to tar file
docker save flower_shop_app:latest -o flower_shop_app_backup.tar

# Load image from tar file
docker load -i flower_shop_app_backup.tar
```

### **Backup Task Definition:**

```bash
# Export task definition
aws ecs describe-task-definition \
  --task-definition flower-shop-task \
  --region ap-southeast-2 \
  > task-definition-backup.json
```

---

## 📝 Checklist Trước Khi Deploy

- [ ] Code đã được test kỹ trên local
- [ ] Database migration (nếu có) đã được review
- [ ] Biến môi trường mới (nếu có) đã được thêm vào task-definition.json
- [ ] Đã backup database
- [ ] Đã thông báo cho team về downtime (nếu có)
- [ ] Đã chuẩn bị rollback plan

---

## 🔄 Rollback

Nếu deployment có vấn đề, rollback về version cũ:

```bash
# 1. Xem danh sách task definition revisions
aws ecs list-task-definitions \
  --family-prefix flower-shop-task \
  --region ap-southeast-2

# 2. Rollback về revision trước (ví dụ revision 6)
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition flower-shop-task:6 \
  --force-new-deployment \
  --region ap-southeast-2

# 3. Kiểm tra
aws ecs describe-services \
  --cluster flower-shop-cluster \
  --services flower-shop-service \
  --region ap-southeast-2 \
  --query 'services[0].taskDefinition'
```

---

## 📞 Liên Hệ và Hỗ Trợ

- **AWS Account ID**: 486918185988
- **Region**: ap-southeast-2
- **User**: vien

---

## 📚 Tài Liệu Tham Khảo

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [Docker Documentation](https://docs.docker.com/)
- [DuckDNS Documentation](https://www.duckdns.org/spec.jsp)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Cập nhật lần cuối**: 22/10/2025  
**Version**: 1.0
