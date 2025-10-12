# 💬 Hướng Dẫn Test Chat System

## ✅ Đã Hoàn Thành

### 1. **Backend Components**
- ✅ Database schema: `Conversations` table và cập nhật `Messages` table
- ✅ Entity classes: `Conversation`, enums (`ConversationStatus`, `ConversationPriority`, `MessageType`)
- ✅ Repositories: `ConversationRepository`, `MessageRepository` với các query methods mới
- ✅ Services: `ChatService`, `StaffService`, `WebSocketService`
- ✅ Controllers:
  - `ChatApiController` - REST API cho chat
  - `ChatWebSocketController` - WebSocket handler
  - `StaffController` - Web pages cho staff
  - `StaffApiController` - REST API cho staff operations

### 2. **Frontend Components**
- ✅ Chat bubble widget cho customer (`fragments/chat-bubble.html`)
- ✅ Chat widget JavaScript (`/js/chat-widget.js`)
- ✅ Staff dashboard với chat interface
- ✅ CSS styling cho chat (`/css/chat.css`)

### 3. **Authentication & Authorization**
- ✅ Endpoint `/api/auth/me` để lấy thông tin user hiện tại
- ✅ Security config cho staff routes
- ✅ WebSocket authentication

---

## 🧪 Cách Test Chat System

### **Bước 1: Chuẩn Bị Tài Khoản**

Database đã có sẵn các tài khoản test:

**Tài khoản Customer:**
```
Email: an.nguyen@example.com
Password: 12345678
Role: CUSTOMER
```

**Tài khoản Staff:**
```
Email: lan.hoang@example.com
Password: 12345678
Role: STAFF
```

### **Bước 2: Test Chat Widget (Customer)**

1. **Mở trình duyệt và truy cập:** http://localhost:8080/

2. **Đăng nhập với tài khoản customer:**
   - Click "Đăng nhập" trên header
   - Email: `an.nguyen@example.com`
   - Password: `12345678`

3. **Kiểm tra chat bubble:**
   - Sau khi đăng nhập, bạn sẽ thấy một nút chat bubble 💬 ở góc dưới bên phải màn hình
   - Click vào nút để mở chat widget

4. **Gửi tin nhắn:**
   - Nhập tin nhắn vào ô input
   - Nhấn Enter hoặc click nút gửi
   - Tin nhắn sẽ được lưu vào database

5. **Kiểm tra trong database:**
   ```bash
   docker exec flower_shop_db psql -U flower_admin -d flower_shop_system -c "SELECT * FROM conversations ORDER BY created_at DESC LIMIT 5;"
   docker exec flower_shop_db psql -U flower_admin -d flower_shop_system -c "SELECT * FROM messages ORDER BY sent_at DESC LIMIT 10;"
   ```

### **Bước 3: Test Staff Dashboard**

1. **Mở tab/cửa sổ mới (hoặc trình duyệt ẩn danh):**

2. **Đăng nhập với tài khoản staff:**
   - Truy cập: http://localhost:8080/login
   - Email: `lan.hoang@example.com`
   - Password: `12345678`

3. **Truy cập Staff Dashboard:**
   - Sau khi đăng nhập, bạn sẽ được redirect tự động đến: http://localhost:8080/staff/dashboard

4. **Kiểm tra Dashboard:**
   - Xem danh sách conversations
   - Xem unassigned conversations (conversations chưa có staff)
   - Click vào conversation để xem chi tiết

5. **Truy cập Staff Chat:**
   - Click menu "Chat" hoặc truy cập: http://localhost:8080/staff/chat
   - Xem danh sách conversations
   - Click vào conversation để chat với customer

6. **Assign conversation:**
   - Click nút "Nhận xử lý" để assign conversation cho mình
   - Sau khi assign, có thể gửi tin nhắn reply cho customer

### **Bước 4: Test Real-time Chat**

1. **Mở 2 cửa sổ trình duyệt:**
   - Cửa sổ 1: Customer (đã đăng nhập)
   - Cửa sổ 2: Staff (đã đăng nhập)

2. **Customer gửi tin nhắn:**
   - Mở chat widget
   - Gửi tin nhắn: "Xin chào, tôi cần tư vấn sản phẩm"

3. **Staff nhận tin nhắn:**
   - Trên staff dashboard, conversation mới sẽ xuất hiện trong "Unassigned Conversations"
   - Click "Nhận xử lý" để assign
   - Gửi reply: "Chào bạn! Tôi có thể giúp gì cho bạn?"

4. **Customer nhận reply:**
   - Tin nhắn từ staff sẽ xuất hiện real-time trong chat widget
   - Nếu chat widget đang đóng, badge sẽ hiển thị số tin nhắn chưa đọc

---

## 🔍 Debug & Troubleshooting

### **Kiểm tra WebSocket Connection**

Mở Developer Console (F12) và xem logs:

```javascript
// Logs từ chat-widget.js
Chat Widget Connected: ...
New conversation created: 123
Message sent successfully
```

### **Kiểm tra API Endpoints**

**1. Lấy thông tin user hiện tại:**
```bash
curl -s http://localhost:8080/api/auth/me \
  -H "Cookie: JSESSIONID=your-session-id"
```

**2. Lấy danh sách conversations:**
```bash
curl -s http://localhost:8080/api/chat/my-conversations \
  -H "Cookie: JSESSIONID=your-session-id"
```

**3. Gửi tin nhắn:**
```bash
curl -X POST http://localhost:8080/api/chat/conversations/1/messages \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "content": "Test message",
    "messageType": "TEXT"
  }'
```

### **Kiểm tra Database**

**1. Xem conversations:**
```sql
SELECT 
  c.id,
  c.customer_id,
  c.staff_id,
  c.status,
  c.priority,
  c.subject,
  c.last_message_at,
  u1.email as customer_email,
  u2.email as staff_email
FROM conversations c
LEFT JOIN users u1 ON c.customer_id = u1.id
LEFT JOIN users u2 ON c.staff_id = u2.id
ORDER BY c.last_message_at DESC;
```

**2. Xem messages:**
```sql
SELECT 
  m.id,
  m.conversation_id,
  m.sender_id,
  m.receiver_id,
  m.content,
  m.message_type,
  m.is_read,
  m.sent_at,
  u1.email as sender_email,
  u2.email as receiver_email
FROM messages m
LEFT JOIN users u1 ON m.sender_id = u1.id
LEFT JOIN users u2 ON m.receiver_id = u2.id
WHERE m.conversation_id IS NOT NULL
ORDER BY m.sent_at DESC
LIMIT 20;
```

### **Common Issues**

**1. Chat bubble không hiển thị:**
- Kiểm tra xem user đã đăng nhập chưa
- Mở Console và xem logs
- Kiểm tra file `/js/chat-widget.js` đã được load chưa

**2. Tin nhắn không gửi được:**
- Kiểm tra WebSocket connection
- Kiểm tra CSRF token
- Xem logs trong Console và server logs

**3. Staff không thấy conversations:**
- Kiểm tra role của user (phải là STAFF hoặc ADMIN)
- Kiểm tra database có conversations chưa
- Xem logs trong `app.log`

---

## 📊 Database Schema

### **Conversations Table**
```sql
CREATE TABLE Conversations (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    staff_id BIGINT DEFAULT NULL,
    status conversation_status NOT NULL DEFAULT 'OPEN',
    priority conversation_priority NOT NULL DEFAULT 'NORMAL',
    subject VARCHAR(255) DEFAULT NULL,
    last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Users(id),
    FOREIGN KEY (staff_id) REFERENCES Users(id)
);
```

### **Messages Table (Updated)**
```sql
ALTER TABLE Messages ADD COLUMN conversation_id BIGINT DEFAULT NULL;
ALTER TABLE Messages ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT';
ALTER TABLE Messages ADD COLUMN is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE Messages ADD COLUMN attachments TEXT DEFAULT NULL;
```

---

## 🚀 Next Steps (Future Enhancements)

1. **AI Integration:**
   - Tích hợp Gemini API để AI hỗ trợ trả lời tự động
   - Thêm field `is_ai_generated` để đánh dấu tin nhắn từ AI

2. **File Attachments:**
   - Upload và gửi hình ảnh, file
   - Sử dụng field `attachments` (JSON string)

3. **Typing Indicators:**
   - Hiển thị "Staff đang nhập..." khi staff đang gõ tin nhắn

4. **Push Notifications:**
   - Thông báo khi có tin nhắn mới (browser notification)

5. **Chat Analytics:**
   - Thống kê số lượng conversations
   - Thời gian phản hồi trung bình
   - Customer satisfaction rating

---

## 📝 Notes

- WebSocket endpoint: `ws://localhost:8080/ws`
- STOMP topics:
  - `/topic/chat/{conversationId}` - Conversation-specific messages
  - `/user/queue/chat` - Personal queue for user
- Chat widget chỉ hiển thị khi user đã đăng nhập
- Staff có thể xem toàn bộ lịch sử chat của conversation
- Conversations có thể được assign/unassign giữa các staff members

---

**Happy Testing! 🎉**


