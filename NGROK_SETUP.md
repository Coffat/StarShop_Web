# 🌐 Ngrok Setup cho MoMo Payment

Hướng dẫn tích hợp ngrok để xử lý MoMo payment callback trong môi trường development.

## 📋 Yêu cầu

1. **Cài đặt ngrok**:
   ```bash
   brew install ngrok
   ```
   
   Hoặc tải từ: https://ngrok.com/download

2. **Đăng ký tài khoản ngrok** (tùy chọn, nhưng khuyến nghị):
   - Truy cập: https://dashboard.ngrok.com/signup
   - Lấy authtoken và chạy: `ngrok authtoken YOUR_TOKEN`

## 🚀 Cách sử dụng

### Option 1: Khởi động tất cả (Khuyến nghị)

Sử dụng script tổng hợp để start cả ngrok và Spring Boot:

```bash
chmod +x start-dev.sh
./start-dev.sh
```

Script này sẽ:
- ✅ Tự động start ngrok tunnel
- ✅ Lấy public URL và lưu vào `.ngrok-url`
- ✅ Hiển thị MoMo callback URLs
- ✅ Start Spring Boot application
- ✅ Tự động cleanup khi thoát (Ctrl+C)

### Option 2: Chạy riêng từng phần

**Bước 1: Start ngrok**
```bash
chmod +x start-ngrok.sh
./start-ngrok.sh
```

**Bước 2: Start Spring Boot**
```bash
./mvnw spring-boot:run
```

**Bước 3: Dừng ngrok khi xong**
```bash
chmod +x stop-ngrok.sh
./stop-ngrok.sh
```

## 🔧 Cách hoạt động

### 1. NgrokService
- Tự động lấy public URL từ ngrok API (`http://localhost:4040/api/tunnels`)
- Fallback đọc từ file `.ngrok-url` nếu API không khả dụng
- Cache URL trong 1 phút để giảm API calls

### 2. PaymentService
- Tự động sử dụng ngrok URL nếu có
- Fallback về localhost nếu ngrok không chạy
- Log rõ ràng URL đang được sử dụng

### 3. MoMo Callback Flow
```
User thanh toán → MoMo → Ngrok Public URL → Localhost:8080 → PaymentController
```

## 📝 URLs quan trọng

Sau khi start ngrok, bạn sẽ có:

- **Public URL**: `https://xxxx-xxx-xxx-xxx.ngrok-free.app`
- **MoMo Return URL**: `https://xxxx.ngrok-free.app/payment/momo/return`
- **MoMo Notify URL**: `https://xxxx.ngrok-free.app/payment/momo/notify`
- **Ngrok Dashboard**: `http://localhost:4040`
- **Local App**: `http://localhost:8080`

## 🐛 Troubleshooting

### Ngrok không start được

```bash
# Kiểm tra ngrok đã cài chưa
which ngrok

# Kiểm tra port 8080 có bị chiếm không
lsof -i :8080

# Kiểm tra ngrok API
curl http://localhost:4040/api/tunnels
```

### MoMo callback không hoạt động

1. **Kiểm tra ngrok đang chạy**:
   ```bash
   curl http://localhost:4040/api/tunnels
   ```

2. **Kiểm tra logs**:
   ```bash
   tail -f ngrok.log
   ```

3. **Xem Spring Boot logs** để thấy URL đang dùng:
   ```
   Using MoMo callback URLs - Return: https://xxx.ngrok-free.app/payment/momo/return
   ```

4. **Test callback URL**:
   ```bash
   curl https://your-ngrok-url.ngrok-free.app/payment/momo/return
   ```

### Ngrok URL thay đổi mỗi lần restart

- Đây là hành vi bình thường với free plan
- Mỗi lần restart, URL mới sẽ tự động được sử dụng
- Không cần cấu hình lại gì cả

## 💡 Tips

1. **Giữ ngrok chạy liên tục** trong khi develop để tránh URL thay đổi

2. **Sử dụng ngrok dashboard** (`http://localhost:4040`) để:
   - Xem real-time requests
   - Inspect request/response
   - Replay requests

3. **Ngrok Pro** (nếu cần):
   - Custom subdomain (URL cố định)
   - Không có warning page
   - Nhiều tunnels cùng lúc

4. **Alternative**: Nếu không muốn dùng ngrok, có thể dùng:
   - [Cloudflare Tunnel](https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/)
   - [LocalTunnel](https://localtunnel.github.io/www/)
   - [Serveo](https://serveo.net/)

## 📊 Monitoring

### Xem ngrok requests
```bash
# Dashboard
open http://localhost:4040

# Logs
tail -f ngrok.log
```

### Xem Spring Boot logs
```bash
# Grep MoMo related logs
./mvnw spring-boot:run | grep -i momo
```

## 🔒 Security Notes

- Ngrok free plan có warning page trước khi redirect
- Chỉ dùng cho development, KHÔNG dùng cho production
- MoMo test environment chấp nhận ngrok URLs
- Production nên dùng domain thật với SSL certificate

## 📚 Tài liệu tham khảo

- [Ngrok Documentation](https://ngrok.com/docs)
- [MoMo Payment Gateway](https://developers.momo.vn/)
- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
