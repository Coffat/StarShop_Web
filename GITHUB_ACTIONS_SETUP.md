# 🚀 Hướng Dẫn Setup GitHub Actions để Deploy Tự Động

## ✅ Đã Hoàn Thành
- ✅ Tạo workflow file: `.github/workflows/deploy-to-aws.yml`

## 📝 Các Bước Tiếp Theo

### 1. Thêm AWS Credentials vào GitHub Secrets

1. Truy cập: https://github.com/Coffat/demo_web/settings/secrets/actions

2. Click **"New repository secret"** và thêm 2 secrets:

   **Secret 1:**
   - Name: `AWS_ACCESS_KEY_ID`
   - Value: (AWS Access Key ID của bạn)

   **Secret 2:**
   - Name: `AWS_SECRET_ACCESS_KEY`
   - Value: (AWS Secret Access Key của bạn)

### 2. Lấy AWS Credentials

Nếu chưa có, chạy lệnh sau để lấy credentials:

```bash
cat ~/.aws/credentials
```

Hoặc tạo IAM user mới cho GitHub Actions với quyền:
- `AmazonEC2ContainerRegistryFullAccess`
- `AmazonECS_FullAccess`

### 3. Push Code lên GitHub

```bash
git add .github/workflows/deploy-to-aws.yml
git add GITHUB_ACTIONS_SETUP.md
git commit -m "Add GitHub Actions workflow for auto deployment"
git push origin vien-deploy
```

### 4. Kiểm Tra Deployment

Sau khi push, workflow sẽ tự động chạy:
1. Truy cập: https://github.com/Coffat/demo_web/actions
2. Xem tiến trình build và deploy
3. Khi xong, ứng dụng sẽ tự động cập nhật tại: http://starshop-hcmute.duckdns.org

## 🔄 Quy Trình Tự Động

Từ giờ, mỗi khi bạn push code lên nhánh `vien-deploy`:

```
Push code → GitHub Actions → Build Docker (AMD64) → Push to ECR → Deploy to ECS → Done!
```

## 📊 Workflow Làm Gì?

1. **Checkout code** từ GitHub
2. **Build Docker image** trên Ubuntu (AMD64) - không cần build local nữa!
3. **Push image** lên AWS ECR
4. **Deploy** lên ECS Fargate
5. **Đợi** service stable
6. **Thông báo** kết quả

## ⚡ Lợi Ích

- ✅ Build trên cloud (AMD64) - không cần máy local
- ✅ Tự động deploy khi push code
- ✅ Không cần chạy script thủ công
- ✅ Có logs chi tiết trên GitHub
- ✅ Rollback dễ dàng nếu có lỗi

## 🆘 Troubleshooting

### Workflow không chạy?
- Kiểm tra đã thêm AWS secrets chưa
- Kiểm tra branch name có đúng là `vien-deploy` không

### Build fail?
- Xem logs tại: https://github.com/Coffat/demo_web/actions
- Kiểm tra Dockerfile có lỗi không

### Deploy fail?
- Kiểm tra AWS credentials có đủ quyền không
- Kiểm tra ECS service name có đúng không

## 📞 Thông Tin

- **Workflow file**: `.github/workflows/deploy-to-aws.yml`
- **Trigger**: Push to `vien-deploy` branch
- **Build platform**: Ubuntu (AMD64)
- **Deploy target**: AWS ECS Fargate
- **Region**: ap-southeast-2 (Sydney)

---

**Tạo ngày**: 24/10/2025
