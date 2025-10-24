# Nâng cấp AI Insights - Phân tích Chuyên nghiệp

## Vấn đề cũ
AI chỉ **mô tả số liệu** thô, không có giá trị thực tế:
- ❌ "Doanh thu tăng 15%" (Vậy thì sao?)
- ❌ "Có 3 sản phẩm sắp hết" (Sản phẩm nào? Ảnh hưởng gì?)
- ❌ "Tỷ lệ hủy cao" (Cao bao nhiêu? Tại sao? Làm gì?)

## Nâng cấp mới

### 1. Cấu trúc Insight 3 phần
Mỗi insight bây giờ có:
- **PHÁT HIỆN**: Số liệu + so sánh benchmark
- **NGUYÊN NHÂN**: Giải thích tại sao
- **HÀNH ĐỘNG**: Gợi ý cụ thể làm gì

**Ví dụ insight tốt:**
```
Title: "Nguy cơ hết hàng sản phẩm bán chạy"
Message: "Hoa Hồng Đỏ (top 1 doanh thu) chỉ còn 8 bông, dự kiến hết trong 2 ngày. 
Tuần trước bán 45 bông/ngày. Nên nhập gấp 200 bông để đáp ứng nhu cầu cuối tuần."
Action: "Nhập hàng ngay" → /admin/products
```

### 2. Benchmark ngành hoa tươi
AI bây giờ so sánh với chuẩn ngành:
- Tỷ lệ hủy: <5% (tốt), 5-8% (TB), >8% (kém)
- Review tích cực: >90% (xuất sắc), 80-90% (tốt), <80% (cần cải thiện)
- Tồn kho: 2-4 tuần (tối ưu), <1 tuần (thiếu), >6 tuần (ứ đọng)
- AOV: 300-500k (thấp), 500-800k (tốt), >800k (cao)

### 3. Câu hỏi phân tích thông minh
AI được hướng dẫn trả lời:
1. Doanh thu tăng/giảm do ĐÂU?
2. Sản phẩm nào BỊ BỎ QUA nhưng có tiềm năng?
3. Nguy cơ MẤT KHÁCH nào?
4. Cơ hội TĂNG DOANH THU nào?
5. Vấn đề VẬN HÀNH cần xử lý GẤP?

### 4. Ưu tiên theo tác động
Insights được sắp xếp theo mức độ ảnh hưởng:
1. **Doanh thu** (mất tiền/cơ hội kiếm tiền)
2. **Tồn kho** (hết hàng bán chạy/ứ đọng vốn)
3. **Đơn hàng** (tỷ lệ hủy/chờ xử lý lâu)
4. **Review** (trải nghiệm khách hàng)

## So sánh trước/sau

### Trước (mô tả số liệu)
```json
{
  "title": "Doanh thu tăng",
  "message": "Doanh thu 24h gần nhất là 3,360,000 VND, tăng 15% so với trước đó.",
  "severity": "success"
}
```

### Sau (phân tích + hành động)
```json
{
  "title": "Cơ hội tăng 30% doanh thu cuối tuần",
  "message": "Doanh thu 24h tăng 15% nhờ Hoa Hồng Đỏ bán chạy (45 bông/ngày). 
  Nhưng chỉ còn 8 bông tồn kho, dự kiến hết trong 2 ngày. 
  Nếu nhập thêm 200 bông trước cuối tuần, có thể tăng thêm 1M doanh thu.",
  "severity": "success",
  "actionLink": "/admin/products",
  "actionText": "Nhập hàng ngay"
}
```

## Cách test

### 1. Clear cache AI
```bash
# Trong browser console (F12)
fetch('/admin/api/ai-insights/clear-cache', {
  method: 'POST',
  credentials: 'include'
}).then(r => r.json()).then(console.log);
```

### 2. Refresh dashboard
- Truy cập: `http://localhost:8080/admin/dashboard`
- Phần "AI Phân Tích" sẽ tự động load
- Xem insights mới có cấu trúc 3 phần: Phát hiện - Nguyên nhân - Hành động

### 3. Kiểm tra chất lượng
Insights tốt phải có:
- ✅ Số liệu cụ thể (%, số tiền, tên sản phẩm)
- ✅ So sánh với benchmark hoặc kỳ trước
- ✅ Giải thích nguyên nhân
- ✅ Gợi ý hành động cụ thể
- ✅ Link đến trang liên quan (nếu có)

## File đã sửa

- ✅ `src/main/java/com/example/demo/service/AdminAiInsightsService.java`
  - Nâng cấp prompt AI với nguyên tắc phân tích chuyên nghiệp
  - Thêm benchmark ngành hoa tươi
  - Thêm câu hỏi phân tích thông minh
  - Thêm ví dụ insight tốt/tệ để AI học

## Lợi ích

### Cho Admin:
- 📊 Hiểu rõ **TẠI SAO** số liệu thay đổi
- 🎯 Biết **LÀM GÌ** ngay lập tức
- 💰 Phát hiện **CƠ HỘI** tăng doanh thu
- ⚠️ Cảnh báo **RỦI RO** mất khách/tiền

### Cho Business:
- Tăng hiệu quả ra quyết định
- Giảm thời gian phân tích thủ công
- Phát hiện vấn đề sớm hơn
- Tối ưu tồn kho & doanh thu

## Ví dụ insights thực tế (sau nâng cấp)

### 1. Inventory Alert
```
🚨 "Nguy cơ hết hàng sản phẩm bán chạy"
Hoa Hồng Đỏ (top 1 doanh thu, 1.2M/tuần) chỉ còn 8 bông. 
Tốc độ bán 45 bông/ngày → hết trong 2 ngày. 
Cuối tuần là peak demand (+80%). 
→ Nhập gấp 200 bông để tránh mất 1.5M doanh thu.
```

### 2. Revenue Opportunity
```
💰 "Cơ hội tăng 25% doanh thu từ combo"
Khách mua Hoa Hồng thường mua thêm Hoa Baby (70% correlation). 
Nhưng chỉ 15% được suggest combo. 
→ Tạo combo "Hồng + Baby" giảm 10% có thể tăng 800k/tuần.
```

### 3. Customer Risk
```
⚠️ "Tỷ lệ hủy đơn cao hơn ngành 60%"
Tỷ lệ hủy 8.5% (ngành 5%). Nguyên nhân: giao chậm (45%), 
sản phẩm không đúng (30%). 
→ Đổi sang GHN Express, tăng QC ảnh trước giao.
```

### 4. Review Issue
```
😡 "3 khách phàn nàn về chất lượng hoa"
Review 1-2 sao tăng 40% tuần này. Chủ yếu về "hoa héo", "không tươi". 
Có thể do nhà cung cấp mới hoặc bảo quản kém. 
→ Kiểm tra kho lạnh, đổi nhà cung cấp nếu cần.
```

---
**Kết luận**: AI bây giờ là **Business Analyst thực sự**, không chỉ là tool hiển thị số liệu!
