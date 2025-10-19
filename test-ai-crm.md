# Test Plan: AI Customer Segmentation & CRM

## 🧪 Testing Checklist

### 1. Database Migration ✅
- [x] Schema đã được cập nhật với 2 cột mới:
  - `customer_segment VARCHAR(20)`
  - `customer_segment_updated_at TIMESTAMP`
- [x] Index đã được tạo: `idx_users_customer_segment`

### 2. Backend Components ✅
- [x] **User Entity**: Thêm 2 trường mới với getters/setters
- [x] **UserRepository**: Thêm 3 queries cho segmentation
- [x] **OrderRepository**: Thêm 2 queries cho order dates
- [x] **DTOs**: MarketingCampaignRequest & MarketingCampaignResponse
- [x] **CustomerSegmentationService**: Scheduled task + manual trigger
- [x] **AiPromptService**: generateMarketingEmail method
- [x] **EmailService**: sendMarketingEmail method
- [x] **AdminMarketingController**: 3 API endpoints

### 3. Frontend UI ✅
- [x] **AI Segmentation Filters**: 3 nút lọc (VIP, NEW, AT_RISK)
- [x] **Campaign Button**: Hiện khi chọn segment
- [x] **Campaign Modal**: Form nhập thông tin campaign
- [x] **JavaScript**: Alpine.js functions cho tương tác

## 🚀 Manual Testing Steps

### Step 1: Database Setup
```bash
# Restart application để load schema mới
./mvnw spring-boot:run
```

### Step 2: Trigger Manual Segmentation
```bash
# Gọi API để chạy phân khúc thủ công
curl -X POST http://localhost:8080/admin/api/marketing/trigger-segmentation
```

### Step 3: Check Segment Stats
```bash
# Kiểm tra thống kê phân khúc
curl http://localhost:8080/admin/api/marketing/segment-stats
```

### Step 4: Test UI
1. Truy cập: `http://localhost:8080/admin/users`
2. Kiểm tra các nút lọc AI hiển thị số lượng
3. Click vào 1 segment (VD: AT_RISK)
4. Kiểm tra nút "Gửi chiến dịch" xuất hiện
5. Click nút và test modal

### Step 5: Test Campaign
1. Chọn segment "AT_RISK"
2. Click "Gửi chiến dịch"
3. Điền form:
   - Tên: "Nhớ Bạn"
   - Voucher: "COMEBACK15"
   - Giá trị: 15
   - Loại: PERCENTAGE
4. Click "Gửi chiến dịch"
5. Kiểm tra email được gửi

## 🔍 Expected Results

### Segmentation Logic
- **VIP**: total_spent > 5,000,000 VND AND orders_count ≥ 3
- **NEW**: Đăng ký ≤ 30 ngày OR first_order ≤ 30 ngày
- **AT_RISK**: Không mua ≥ 90 ngày AND đã từng mua

### API Responses
```json
// GET /admin/api/marketing/segment-stats
{
  "success": true,
  "stats": {
    "vip": 5,
    "new": 12,
    "atRisk": 8
  }
}

// POST /admin/api/marketing/send-campaign
{
  "success": true,
  "message": "Đã gửi 8 email thành công",
  "recipientCount": 8,
  "emailsSent": 8,
  "emailsFailed": 0,
  "voucherCode": "COMEBACK15"
}
```

## 🐛 Troubleshooting

### Common Issues
1. **Segmentation không chạy**: Kiểm tra scheduled task config
2. **UI không hiển thị**: Kiểm tra Alpine.js và JavaScript errors
3. **Email không gửi**: Kiểm tra EmailService config
4. **API 404**: Kiểm tra controller mapping

### Debug Commands
```bash
# Check logs
tail -f logs/app.log

# Check database
psql -d flower_store -c "SELECT customer_segment, COUNT(*) FROM Users WHERE role='CUSTOMER' GROUP BY customer_segment;"

# Test API manually
curl -X POST http://localhost:8080/admin/api/marketing/send-campaign \
  -H "Content-Type: application/json" \
  -d '{"segment":"AT_RISK","campaignName":"Test","voucherCode":"TEST10","discountValue":10,"discountType":"PERCENTAGE"}'
```

## ✅ Success Criteria
- [ ] Segmentation chạy tự động mỗi đêm
- [ ] UI hiển thị đúng số lượng từng segment
- [ ] Campaign gửi email thành công
- [ ] Voucher được tạo và áp dụng
- [ ] AI-generated email content có chất lượng

## 📝 Notes
- Scheduled task chạy lúc 00:00 Asia/Ho_Chi_Minh
- Email template sử dụng placeholders: {{name}}, {{voucher}}, {{expiry}}
- Voucher có hạn 30 ngày từ ngày tạo
- Campaign chỉ gửi cho khách hàng có email hợp lệ
