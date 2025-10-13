# AI Chat System - Quick Start

## 🚀 Khởi động nhanh

### 1. Start Database
```bash
docker-compose up -d
```

### 2. Start Application
```bash
./mvnw spring-boot:run
```

### 3. Test AI Chat
1. Mở trình duyệt: `http://localhost:8080`
2. Click vào chat bubble góc phải
3. Gửi tin nhắn: **"Tôi muốn mua hoa hồng trắng"**

## 💬 Test Commands

### Tìm sản phẩm
```
Tôi muốn mua hoa hồng trắng
Có hoa sinh nhật dưới 300k không?
```

### Tính phí ship
```
Phí ship đến Quận 1 bao nhiêu?
Giao hàng đến Thủ Đức mất bao lâu?
```

### Xem khuyến mãi
```
Có chương trình khuyến mãi nào không?
Mã giảm giá gì đang có?
```

### Thông tin cửa hàng
```
Cửa hàng mở cửa lúc mấy giờ?
Địa chỉ cửa hàng ở đâu?
```

### Test Handoff (chuyển sang staff)
```
Tôi muốn hỏi về đơn hàng, SĐT 0912345678
Đơn hàng #12345 đến đâu rồi?
```

## 📊 Kiểm tra Database

```sql
-- Xem routing decisions
SELECT * FROM routing_decisions ORDER BY created_at DESC LIMIT 10;

-- Xem handoff queue
SELECT * FROM handoff_queue WHERE resolved_at IS NULL;

-- Xem staff presence
SELECT * FROM staff_presence;
```

## 📖 Tài liệu chi tiết

- **Testing Guide**: `AI_CHAT_TESTING_GUIDE.md`
- **Implementation Summary**: `AI_CHAT_IMPLEMENTATION_SUMMARY.md`
- **Integration Plan**: `ai-chat-system-integration.plan.md`

## 🔧 Configuration

API Key được cấu hình trong `application.yml`:
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo}
```

Nếu cần thay đổi:
```bash
export GEMINI_API_KEY=your_key_here
```

## ✅ Features

- ✅ AI phân tích intent tự động
- ✅ Tìm sản phẩm với ảnh và giá
- ✅ Tính phí ship theo địa điểm
- ✅ Tra cứu khuyến mãi
- ✅ Thông tin cửa hàng
- ✅ Phát hiện PII và handoff
- ✅ Staff queue management
- ✅ Real-time WebSocket notifications

## 🐛 Troubleshooting

### Lỗi Gemini API
```bash
# Kiểm tra logs
tail -f app.log | grep "Gemini"
```

### Lỗi Database
```bash
# Reset database
docker-compose down -v
docker-compose up -d
```

### Xem tất cả logs
```bash
tail -f app.log
```

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Database đã chạy chưa: `docker ps`
2. Migration đã chạy chưa: Check `routing_decisions` table
3. Gemini API key có hợp lệ không
4. Logs có error gì không: `tail -f app.log | grep ERROR`

Happy testing! 🎉

