
⸻
API key của tôi: AIzaSyB4tCAr_Y5VqAGzpVF01_lc6fEOCbyd9Oo
🌸 STARSHOP AI CHAT SYSTEM 

🎯 1. Mục tiêu hệ thống
	•	Tạo AI Chat tư vấn bán hàng thông minh, hoạt động trực tiếp trên website StarShop.
	•	AI có thể:
	•	Gợi ý và hiển thị ảnh sản phẩm thực tế từ DB (Products.image_url).
	•	Tính phí ship ước tính bằng API GHN/OpenMap.
	•	Trả lời câu hỏi về khuyến mãi, giờ làm việc, cửa hàng, vận chuyển.
	•	Giao tiếp lịch sự, thân thiện, tự nhiên, nhiệt tình.
	•	Staff (nhân viên) chỉ tham gia khi:
	•	Câu hỏi liên quan đơn hàng, thanh toán, thông tin cá nhân, hoặc
	•	AI không tự tin với câu trả lời (confidence < 0.65).

⸻

🧩 2. Kiến trúc tổng quan

[Web Chat Widget]
     │ (WebSocket / REST)
     ▼
[Spring Boot Backend]
 ├─ Conversations
 ├─ Messages
 ├─ Routing Engine (intent, confidence, PII)
 ├─ Tool Executor (product_search, shipping_fee, promotion_lookup)
 ├─ Handoff Queue (staff chờ xử lý)
 ├─ Staff Presence (online, workload)
 ├─ Users (Customer & Staff)
 └─ External Integrations:
      - Gemini API (LLM)
      - GHN/OpenMap API (phí ship)
      - Internal APIs: Products, Promotions


⸻

💬 3. Trạng thái hội thoại

Trạng thái logic	DB (bảng Conversations)	Ý nghĩa
ai	OPEN (assigned_staff_id IS NULL)	AI đang tư vấn
handoff pending	OPEN + có bản ghi handoff_queue	Đang chờ nhân viên nhận
staff active	ASSIGNED	Nhân viên đã tiếp nhận chat
closed	CLOSED	Cuộc chat kết thúc


⸻

⚙️ 4. Quy tắc phân loại tin nhắn

Nhóm AI xử lý được

Intent	Ví dụ	Hành động
sales	“Hoa hồng trắng khoảng 300k có không?”	Gọi product_search() → gửi ảnh + giá + link
shipping	“Ship về Cần Thơ bao nhiêu?”	Gọi shipping_fee() GHN API → trả phí ước tính
promotion	“Hôm nay có giảm giá gì không?”	Gọi promotion_lookup()
store_info	“Shop mở cửa mấy giờ?”	Trả từ cấu hình
chitchat	“Cảm ơn nhé, bạn dễ thương ghê!”	AI trả lời thân thiện

Nhóm chuyển staff

Intent	Ví dụ	Lý do
order_support	“Đơn #12345 chưa giao?”	Cần tra hệ thống
payment	“Momo bị trừ tiền rồi”	Nhạy cảm
personal_info	“Địa chỉ mình là 45 Lý Thường Kiệt”	Có PII
low_confidence	AI không chắc chắn	confidence < 0.65


⸻

🧠 5. JSON structured output từ Gemini

{
  "intent": "sales|shipping|promotion|order_support|payment|store_info|chitchat|other",
  "confidence": 0.0,
  "reply": "string (markdown allowed, có thể kèm ![image](url))",
  "suggest_handoff": false,
  "need_handoff": false,
  "tool_requests": [
    { "name": "product_search", "args": { "query": "hoa hồng trắng", "price_max": 300000 } }
  ],
  "product_suggestions": [
    { "name": "Hoa Hồng Trắng 15 Bông", "price": 195000, "image_url": "https://..." },
    { "name": "Bó Hồng Trắng Mix", "price": 210000, "image_url": "https://..." }
  ]
}


⸻

🛠️ 6. Tools mà AI được phép gọi (thông qua backend)

Tên tool	Chức năng	Nguồn dữ liệu
product_search(query, filters)	Tìm sản phẩm & trả về ảnh, giá, link	Bảng Products
shipping_fee(from,to,weight)	Tính phí giao hàng ước tính	GHN API / OpenMap
promotion_lookup()	Truy xuất khuyến mãi hiện tại	Bảng Promotions
store_info()	Trả địa chỉ, giờ mở cửa, hotline	Config cố định


⸻

💾 7. Bảng dữ liệu mở rộng (tương thích DB hiện tại)

CREATE TABLE IF NOT EXISTS routing_decisions (
  id BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT REFERENCES Conversations(id) ON DELETE CASCADE,
  intent TEXT NOT NULL,
  confidence NUMERIC(4,3),
  need_handoff BOOLEAN NOT NULL,
  reasons TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS handoff_queue (
  conversation_id BIGINT PRIMARY KEY REFERENCES Conversations(id) ON DELETE CASCADE,
  priority INT DEFAULT 0,
  tags TEXT[] DEFAULT '{}',
  enqueued_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS staff_presence (
  staff_id BIGINT PRIMARY KEY REFERENCES Users(id) ON DELETE CASCADE,
  online BOOLEAN DEFAULT FALSE,
  workload INT DEFAULT 0,
  last_seen_at TIMESTAMPTZ DEFAULT now()
);


⸻

🚦 8. Quy tắc ngưỡng confidence

Ngưỡng	Hành động
≥ 0.80	AI trả lời tự động
0.65–0.79	AI trả lời + hỏi khách có muốn gặp nhân viên
< 0.65	Handoff nhân viên


⸻

🌼 9. Ví dụ hội thoại có ảnh

User: “Cho mình bó hoa hồng trắng khoảng 300k, giao ở HCM nhé.”
AI:

“Mình gợi ý vài mẫu nè 🌸
1️⃣ Hoa Hồng Trắng 15 Bông – 295.000đ

2️⃣ Hộp Hồng Trắng Mix – 310.000đ

Phí ship nội thành HCM tầm 22.000đ.
Bạn muốn mình giữ hàng không?”

⸻

User: “Đơn #12459 của mình sao chưa tới?”
AI:

“Mình xin phép chuyển bạn cho nhân viên để kiểm tra đơn hàng nhé 💬.”
→ backend: need_handoff=true, thêm handoff_queue.

⸻

👩‍💼 10. Staff Console
	•	Hiển thị danh sách handoff_queue: thời gian chờ, intent, priority.
	•	Nhân viên “claim” cuộc chat → Conversations.status='ASSIGNED'.
	•	Có thể gửi ảnh, file, hoặc kết thúc chat (status='CLOSED').

⸻

🔐 11. Bảo mật & quy tắc
	•	Ẩn dữ liệu cá nhân khi log (09******45, ***@gmail.com).
	•	Giới hạn 60 tin nhắn / 5 phút / IP.
	•	Xoá hoặc ẩn danh hội thoại sau 90 ngày.
	•	Staff chỉ xem chat mình phụ trách.

⸻

📈 12. Thống kê & tối ưu
	•	Log intent + confidence → bảng routing_decisions.
	•	Theo dõi AI containment rate, handoff rate, satisfaction.
	•	Cải thiện prompt dựa trên hội thoại thực tế.

⸻

🚀 13. Lộ trình phát triển

Giai đoạn	Mục tiêu
Phase 1	Chat AI + gợi ý sản phẩm (ảnh, giá) + tính phí ship
Phase 2	Handoff staff + staff console
Phase 3	Thêm khuyến mãi & thống kê hành vi
Phase 4	Phân ca staff tự động + dashboard


⸻


