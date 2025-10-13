

# 1) Giữ 2-phase, nhưng bỏ ép “20 words”

* Trong `generateSystemPrompt()` đang có: *“reply … (max 20 words)”* → **bỏ**.
  Thay bằng: “**reply ngắn gọn 1–2 câu, tự nhiên; có thể chào hoặc hỏi lại**”.
* Mục tiêu: phase route chỉ cần **nhịp chào/ack** chứ không “cụt ngủn”.

---

# 2) Thông số đề xuất cho **gemini-2.5-flash** (theo tình huống)

| Tình huống                   |  temperature | top_p | max_output_tokens | timeout | Ghi chú                                                       |
| ---------------------------- | -----------: | ----: | ----------------: | ------: | ------------------------------------------------------------- |
| Greeting/Closing             |  **0.3–0.4** |   0.8 |           **128** |     10s | 1–2 câu là đủ, bật stream để “nhanh cảm giác”                 |
| Hỏi ngắn/FAQ                 |      **0.4** |  0.85 |           **256** |     12s | Trả thẳng trọng tâm + hỏi lại 1 ý                             |
| Chit-chat                    |      **0.5** |   0.9 |           **384** |     15s | Tự nhiên, ấm áp, không lê thê                                 |
| **Tư vấn sản phẩm (có ảnh)** | **0.65–0.7** |   0.9 |      **768–1024** | **20s** | 2–3 gợi ý, tên **đậm**, **giá**, `![ảnh](url)`, 1 câu **CTA** |
| Handoff (PII/đơn hàng)       |      **0.2** |   0.7 |           **128** |     10s | Xin phép chuyển người, trấn an ngắn gọn                       |

> Vì bạn không muốn JSON, hãy **chọn profile theo tình huống** trước khi gọi API. Đừng dùng 1 bộ tham số cho mọi case — đó là lý do 2.5-flash nghe “không khéo”.

---

# 3) Prompt – thêm “độ dài linh hoạt” thay vì ép cố định

Trong `generateFinalResponsePrompt()` thêm khối này (giữ tiếng Việt, đúng brand):

* “**Độ dài linh hoạt**:
  – Chào/hỏi lại: *~10–25 từ*.
  – Trả lời thông tin chung: *~30–60 từ*.
  – **Tư vấn sản phẩm**: *~100–150 từ*, có **2–3 gợi ý** (tên **đậm**, **giá**, **ảnh**), kết thúc bằng **CTA**.
  – Trò chuyện thường: *~60–90 từ*.”
* “Tối đa **2 emoji** khi phù hợp; tránh rập khuôn; ưu tiên rõ ràng & ấm áp.”

> Lý do: 2.5-flash rất “ngoan” với hướng dẫn độ dài tương đối. “~N từ” + `max_output_tokens` theo-case cho chất lượng ổn mà vẫn nhanh.

---

# 4) Những chỗ nên tinh chỉnh ngay (để “khéo” hơn)

1. **Mở đầu**: luôn 1 câu chào + 1 câu đặt bối cảnh (“mình gợi ý theo ngân sách/tone bạn thích”).
2. **Tư vấn**: đúng 2–3 mẫu, **đừng** list 5–7 mẫu (chậm + rối).
3. **Ảnh**: chỉ tư vấn mới chèn ảnh; **thumbnail nhẹ** (600–800px) để không làm trễ.
4. **CTA**: mỗi câu trả lời **kết thúc** bằng 1 câu hỏi rõ ràng (Yes/No hoặc A/B).
5. **Handoff**: câu chuẩn ngắn gọn — “Mình xin phép chuyển bạn cho nhân viên để kiểm tra chính xác nhé. Bạn chờ mình một chút ạ 💬”.

---

# 5) Hiệu năng: “nhanh nhưng đủ dữ liệu”

* **Song song** gọi `product_search` và `shipping_fee`; đặt **time-box 700–900ms** mỗi API.
* Nếu ship chưa về kịp: trả “Phí ship dự kiến … (mình xác nhận lại ngay khi có số)”.
* **Cache nóng**: top SP phổ biến 2–5 phút; bảng giá ship bậc thang 10–30 phút.
* **Streaming** văn bản trước, ảnh lazy-load sau → cảm giác rất nhanh.

---

# 6) Cài đặt an toàn & ổn định cho 2.5-flash

* **retry** 2–3 lần (exponential backoff ngắn: 0.4s, 0.8s, 1.6s).
* **timeout** tổng cho call tư vấn (phase cuối) **~20s** vì có tools; greeting/FAQ để **10–12s**.
* **safety**: giữ chặn PII trong pipeline của bạn (handoff ngay khi #mã đơn, SĐT, email, địa chỉ).

---

# 7) “Checklist tự chấm điểm” sau mỗi response (rule nhẹ, không JSON)

* Độ dài có nằm trong khoảng mục tiêu của case?
* **Tư vấn** có **ảnh** + **giá** + **CTA** chưa?
* Có lặp cụm mở đầu 2–3 lần liên tiếp không? Nếu có, đổi câu chào.

---

## Kết luận nhanh

* **Giữ gemini-2.5-flash** là hợp lý.
* **Bỏ ép 20 words**, thêm “độ dài linh hoạt” theo case.
* **Chọn temperature / max_tokens / timeout** theo **tình huống** như bảng #2.
* Bật **song song + stream + cache** để “nhanh mà khéo”.

