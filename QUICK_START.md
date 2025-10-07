# 🚀 Quick Start - Development với Ngrok

## Khởi động nhanh (1 lệnh)

```bash
./start-dev.sh
```

Xong! Script sẽ tự động:
- ✅ Start ngrok tunnel
- ✅ Lấy public URL
- ✅ Start Spring Boot
- ✅ Hiển thị tất cả URLs cần thiết

## Dừng

Nhấn `Ctrl+C` - tất cả sẽ tự động cleanup

## Hoặc chạy riêng

```bash
# Terminal 1: Start ngrok
./start-ngrok.sh

# Terminal 2: Start app
./mvnw spring-boot:run

# Khi xong: Stop ngrok
./stop-ngrok.sh
```

## URLs sau khi start

- 🌍 **Public**: `https://xxxx.ngrok-free.app`
- 🏠 **Local**: `http://localhost:8080`
- 📊 **Ngrok Dashboard**: `http://localhost:4040`

## Test MoMo Payment

1. Truy cập: `https://xxxx.ngrok-free.app` (hoặc localhost)
2. Thêm sản phẩm vào giỏ hàng
3. Checkout → Chọn MoMo
4. Thanh toán → MoMo sẽ callback về ngrok URL
5. Đơn hàng tự động cập nhật trạng thái ✅

## Troubleshooting

```bash
# Kiểm tra ngrok
curl http://localhost:4040/api/tunnels

# Xem logs
tail -f ngrok.log

# Stop tất cả
./stop-ngrok.sh
pkill -f "spring-boot"
```

Chi tiết: Xem [NGROK_SETUP.md](NGROK_SETUP.md)
