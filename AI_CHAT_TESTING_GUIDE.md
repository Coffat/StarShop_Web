# Hướng dẫn Test AI Chat System

## Tổng quan

Hệ thống AI Chat đã được tích hợp hoàn chỉnh vào StarShop. Tài liệu này hướng dẫn cách test các tính năng AI.

## Chuẩn bị

### 1. Khởi động Database

```bash
docker-compose up -d
```

Database sẽ tự động chạy migration file `07_ai_chat_system.sql` để tạo các bảng:
- `routing_decisions`: Lưu quyết định routing của AI
- `handoff_queue`: Hàng đợi conversation cần staff xử lý
- `staff_presence`: Trạng thái online của staff
- `ai_chat_config`: Cấu hình AI

### 2. Cấu hình Gemini API Key

File `application.yml` đã được cấu hình với API key mặc định. Nếu cần thay đổi:

```yaml
gemini:
  api-key: ${GEMINI_API_KEY:YOUR_API_KEY_HERE}
```

Hoặc set biến môi trường:

```bash
export GEMINI_API_KEY=your_actual_api_key
```

### 3. Khởi động Application

```bash
./mvnw spring-boot:run
```

Hoặc:

```bash
./dev.sh
```

Application sẽ chạy tại: `http://localhost:8080`

## Các Scenario Test

### Scenario 1: AI Tư vấn Sản phẩm (Product Search)

**Mục tiêu**: Test khả năng AI gợi ý sản phẩm phù hợp

**Bước test**:

1. Truy cập trang chủ: `http://localhost:8080`
2. Click vào chat bubble ở góc phải màn hình
3. Gửi tin nhắn: "Tôi muốn mua hoa hồng trắng"
4. **Kết quả mong đợi**:
   - AI phân tích intent: `SALES`
   - AI gọi tool `product_search` với query "hoa hồng trắng"
   - AI trả về danh sách sản phẩm với:
     - Tên sản phẩm
     - Giá
     - Link xem chi tiết
     - Ảnh sản phẩm (nếu có)

**Ví dụ tin nhắn test**:
- "Tôi cần mua hoa sinh nhật"
- "Có hoa gì dưới 300k không?"
- "Hoa cưới đẹp có không?"

### Scenario 2: AI Tính Phí Ship (Shipping Fee)

**Mục tiêu**: Test khả năng AI ước tính phí ship

**Bước test**:

1. Mở chat widget
2. Gửi tin nhắn: "Phí ship đến Quận 1 bao nhiêu?"
3. **Kết quả mong đợi**:
   - AI phân tích intent: `SHIPPING`
   - AI gọi tool `shipping_fee` với location "Quận 1"
   - AI trả về:
     - Phí ship ước tính (VD: 20.000đ)
     - Thời gian giao hàng (VD: 2-4 giờ)

**Ví dụ tin nhắn test**:
- "Ship đến Thủ Đức mất bao lâu?"
- "Phí giao hàng ra Hà Nội là bao nhiêu?"
- "Giao hàng miễn phí không?"

### Scenario 3: AI Tra cứu Khuyến mãi (Promotion Lookup)

**Mục tiêu**: Test khả năng AI cung cấp thông tin khuyến mãi

**Bước test**:

1. Mở chat widget
2. Gửi tin nhắn: "Có chương trình khuyến mãi nào không?"
3. **Kết quả mong đợi**:
   - AI phân tích intent: `PROMOTION`
   - AI gọi tool `promotion_lookup`
   - AI trả về danh sách voucher:
     - Tên voucher
     - Mã code
     - Giá trị giảm giá
     - Điều kiện áp dụng
     - Ngày hết hạn

**Ví dụ tin nhắn test**:
- "Mã giảm giá nào đang có?"
- "Voucher gì áp dụng được?"
- "Có ưu đãi cho đơn 500k không?"

### Scenario 4: AI Cung cấp Thông tin Cửa hàng (Store Info)

**Mục tiêu**: Test khả năng AI trả lời về thông tin cửa hàng

**Bước test**:

1. Mở chat widget
2. Gửi tin nhắn: "Cửa hàng mở cửa lúc mấy giờ?"
3. **Kết quả mong đợi**:
   - AI phân tích intent: `STORE_INFO`
   - AI gọi tool `store_info`
   - AI trả về:
     - Giờ mở cửa: 8:00 - 22:00
     - Địa chỉ: 01 Võ Văn Ngân, TP. Thủ Đức, TP.HCM
     - Hotline (nếu có)

**Ví dụ tin nhắn test**:
- "Địa chỉ cửa hàng ở đâu?"
- "Liên hệ như thế nào?"
- "Chính sách đổi trả ra sao?"

### Scenario 5: Handoff sang Staff (PII Detection)

**Mục tiêu**: Test khả năng AI phát hiện thông tin nhạy cảm và chuyển sang staff

**Bước test**:

1. Mở chat widget
2. Gửi tin nhắn: "Tôi muốn hỏi về đơn hàng của tôi, SĐT 0912345678"
3. **Kết quả mong đợi**:
   - AI phát hiện PII (số điện thoại)
   - AI set `need_handoff = true`
   - Conversation được thêm vào `handoff_queue`
   - Khách hàng nhận tin nhắn hệ thống:
     > "Cảm ơn bạn! Mình đã chuyển yêu cầu đến nhân viên tư vấn. Vui lòng đợi trong giây lát nhé! ⏳"

**Ví dụ tin nhắn test** (sẽ trigger handoff):
- "Đơn hàng #12345 của tôi đến đâu rồi?"
- "Tôi muốn thanh toán bằng thẻ"
- "Email của tôi là user@example.com"

### Scenario 6: Handoff sang Staff (Low Confidence)

**Mục tiêu**: Test khả năng AI tự đánh giá confidence và handoff khi không tự tin

**Bước test**:

1. Mở chat widget
2. Gửi tin nhắn phức tạp: "Tôi muốn đặt hoa theo yêu cầu riêng, có thể không?"
3. **Kết quả mong đợi**:
   - AI phân tích và nhận thấy confidence < 0.65
   - AI set `suggest_handoff = true`
   - Conversation được thêm vào handoff queue
   - Khách hàng nhận tin nhắn handoff

**Ví dụ tin nhắn test**:
- "Tôi cần tư vấn kỹ về thiết kế hoa cưới"
- "Có thể giao hàng vào 3h sáng không?"
- "Tôi muốn khiếu nại về chất lượng"

### Scenario 7: Staff Console - Xem Handoff Queue

**Mục tiêu**: Test giao diện staff nhận và xử lý handoff

**Bước test**:

1. Đăng nhập với tài khoản staff/admin
2. Truy cập: `http://localhost:8080/staff/chat`
3. Kiểm tra tab "Hàng đợi AI" (nếu có)
4. **Kết quả mong đợi**:
   - Hiển thị danh sách conversation chờ xử lý
   - Mỗi conversation hiển thị:
     - Customer name
     - AI intent phân tích được
     - Confidence score
     - Handoff reason
     - Thời gian chờ
   - Nút "Nhận xử lý" để staff claim conversation

### Scenario 8: Chitchat - AI trả lời tự nhiên

**Mục tiêu**: Test khả năng AI trò chuyện tự nhiên

**Bước test**:

1. Mở chat widget
2. Gửi tin nhắn: "Chào bạn!"
3. **Kết quả mong đợi**:
   - AI phân tích intent: `CHITCHAT`
   - AI trả lời thân thiện, tự nhiên
   - Không gọi tool nào

**Ví dụ tin nhắn test**:
- "Hello"
- "Cảm ơn bạn nhé"
- "Bye bye"

## Kiểm tra Database

### 1. Xem Routing Decisions

```sql
SELECT 
    id,
    conversation_id,
    intent,
    confidence,
    need_handoff,
    handoff_reason,
    created_at
FROM routing_decisions
ORDER BY created_at DESC
LIMIT 10;
```

**Kết quả mong đợi**:
- Mỗi tin nhắn có 1 routing decision
- Intent được phân loại đúng
- Confidence score hợp lý (0.0 - 1.0)
- `need_handoff` = true khi có PII hoặc confidence thấp

### 2. Xem Handoff Queue

```sql
SELECT 
    hq.id,
    hq.conversation_id,
    hq.handoff_reason,
    hq.priority,
    hq.enqueued_at,
    hq.assigned_at,
    hq.resolved_at,
    c.customer_name
FROM handoff_queue hq
JOIN conversations c ON hq.conversation_id = c.id
WHERE hq.resolved_at IS NULL
ORDER BY hq.priority DESC, hq.enqueued_at ASC;
```

**Kết quả mong đợi**:
- Conversation có PII hoặc low confidence nằm trong queue
- Priority cao hơn cho PII_DETECTED, ORDER_SUPPORT
- Chưa resolved (resolved_at = NULL)

### 3. Xem Staff Presence

```sql
SELECT 
    sp.staff_id,
    u.full_name,
    sp.status,
    sp.current_workload,
    sp.last_seen_at
FROM staff_presence sp
JOIN users u ON sp.staff_id = u.id
ORDER BY sp.last_seen_at DESC;
```

**Kết quả mong đợi**:
- Staff online có status = 'ONLINE'
- Workload được cập nhật khi nhận conversation
- last_seen_at được cập nhật định kỳ

## Kiểm tra Logs

### 1. Xem AI Logs

```bash
tail -f app.log | grep "AI"
```

**Logs quan trọng**:
- `AI analysis completed`: Gemini đã phân tích xong
- `AI routing decision`: Quyết định routing
- `AI tool execution`: Thực thi tool
- `AI response generated`: Tạo response

### 2. Xem Routing Logs

```bash
tail -f app.log | grep "Routing"
```

**Logs quan trọng**:
- `Routing message`: Bắt đầu routing
- `PII detected`: Phát hiện thông tin cá nhân
- `Handoff needed`: Cần chuyển staff
- `AI can handle`: AI xử lý được

### 3. Xem Handoff Logs

```bash
tail -f app.log | grep "Handoff"
```

**Logs quan trọng**:
- `Adding to handoff queue`: Thêm vào queue
- `Handoff notification sent`: Thông báo staff
- `Staff assigned to conversation`: Gán staff

## Test Performance

### 1. Response Time

**Mục tiêu**: Đảm bảo AI response trong < 3 giây

**Cách test**:
1. Gửi tin nhắn
2. Đo thời gian từ khi gửi đến khi nhận response
3. **Kết quả mong đợi**: < 3 giây

### 2. Concurrent Users

**Mục tiêu**: Test với nhiều user đồng thời

**Cách test**:
1. Mở 5-10 tab browser
2. Mỗi tab mở chat widget và gửi tin nhắn
3. **Kết quả mong đợi**:
   - Tất cả nhận response
   - Không bị lỗi
   - Response time vẫn < 5 giây

### 3. Gemini API Rate Limit

**Mục tiêu**: Test xử lý khi Gemini API lỗi

**Cách test**:
1. Tạm thời set API key sai trong `application.yml`
2. Gửi tin nhắn
3. **Kết quả mong đợi**:
   - Conversation tự động handoff sang staff
   - Khách hàng nhận tin nhắn lỗi graceful
   - Không crash application

## Checklist Test

### Tính năng AI

- [ ] AI phân tích intent đúng (SALES, SHIPPING, PROMOTION, etc.)
- [ ] AI gọi tool product_search và trả về sản phẩm
- [ ] AI gọi tool shipping_fee và trả về phí ship
- [ ] AI gọi tool promotion_lookup và trả về voucher
- [ ] AI gọi tool store_info và trả về thông tin cửa hàng
- [ ] AI trả lời chitchat tự nhiên

### Handoff System

- [ ] AI phát hiện PII (số điện thoại, email) và handoff
- [ ] AI handoff khi confidence thấp
- [ ] AI handoff khi gặp ORDER_SUPPORT, PAYMENT
- [ ] Conversation được thêm vào handoff_queue
- [ ] Staff nhận notification về handoff mới
- [ ] Staff có thể claim conversation từ queue

### Database

- [ ] routing_decisions được tạo cho mỗi tin nhắn
- [ ] handoff_queue được tạo khi cần handoff
- [ ] staff_presence được cập nhật khi staff online
- [ ] ai_chat_config có dữ liệu store info

### UI/UX

- [ ] Chat widget hiển thị tin nhắn AI đúng format
- [ ] Hiển thị ảnh sản phẩm (nếu có)
- [ ] Hiển thị link sản phẩm clickable
- [ ] Tin nhắn handoff hiển thị cho customer
- [ ] Staff console hiển thị AI queue

### Error Handling

- [ ] Xử lý khi Gemini API lỗi (fallback to handoff)
- [ ] Xử lý khi tool execution lỗi
- [ ] Xử lý khi không có staff online
- [ ] Xử lý khi database lỗi

## Troubleshooting

### Lỗi: "Gemini API error"

**Nguyên nhân**: API key không hợp lệ hoặc hết quota

**Giải pháp**:
1. Kiểm tra API key trong `application.yml`
2. Kiểm tra quota tại: https://makersuite.google.com/app/apikey
3. Thử API key khác

### Lỗi: "No routing decision found"

**Nguyên nhân**: Database migration chưa chạy

**Giải pháp**:
1. Kiểm tra file `07_ai_chat_system.sql` đã được chạy chưa
2. Chạy lại migration: `docker-compose down -v && docker-compose up -d`

### Lỗi: "PII detection not working"

**Nguyên nhân**: Regex pattern không match

**Giải pháp**:
1. Kiểm tra `PiiDetectionService.java`
2. Test regex pattern với dữ liệu thực tế
3. Cập nhật pattern nếu cần

### AI không trả lời

**Nguyên nhân**: Có thể do nhiều lý do

**Giải pháp**:
1. Kiểm tra logs: `tail -f app.log | grep "ERROR"`
2. Kiểm tra Gemini API response
3. Kiểm tra system prompt có đúng không
4. Test với tin nhắn đơn giản: "Hello"

## Kết luận

Hệ thống AI Chat đã được tích hợp hoàn chỉnh với các tính năng:

✅ AI phân tích intent và confidence
✅ AI gọi tools (product search, shipping fee, promotion, store info)
✅ AI phát hiện PII và handoff
✅ Handoff system với queue và staff assignment
✅ Analytics và monitoring
✅ Security (PII masking, rate limiting config)

Hãy test kỹ các scenario trên và báo cáo lỗi nếu có!

