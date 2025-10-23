# 🎯 KẾ HOẠCH MIGRATION: CHUẨN HÓA THÔNG BÁO VỚI SWEETALERT2

**Ngày tạo:** 23/10/2025  
**Mục tiêu:** Thay thế TẤT CẢ alert(), confirm() bằng SweetAlert2 + Xóa console.log debug

---

## 📊 PHÂN TÍCH HIỆN TRẠNG

### 1. ✅ ĐÃ DÙNG SWEETALERT2 (Giữ nguyên):
- `main.js` - showToast() function (SweetAlert2)
- `staff-dashboard.js` - Swal.fire() cho check-out confirmation
- `account-orders.js` - Swal.fire() cho cancel order
- `admin/vouchers/index.html` - Swal.fire() cho confirmations

### 2. ❌ ĐANG DÙNG alert() (CẦN THAY THẾ):

| File | Số lượng | Loại | Priority |
|------|----------|------|----------|
| `staff/timesheet/index.html` | 7 | Success/Error | 🔴 Cao |
| `staff/profile/index.html` | 1 | Validation | 🔴 Cao |
| `staff/orders/index.html` | 2 | Error | 🔴 Cao |
| `staff/orders/detail.html` | 2 | Error | 🔴 Cao |
| `staff/dashboard/index.html` | 2 | Error | 🔴 Cao |
| `staff/chat/index.html` | 9 | Success/Error | 🔴 Cao |
| `reset-password.html` | 1 | Validation | 🟡 TB |
| `register.html` | 1 | Success | 🟡 TB |
| `forgot-password.html` | 1 | Success | 🟡 TB |
| `account/wishlist.html` | 2 | Success | 🟡 TB |
| `admin/users/index.html` | 1 | Info | 🟢 Thấp |
| `cart/index.html` | 0 | - | - |

**Tổng: ~29 alert() cần thay thế**

### 3. ❌ ĐANG DÙNG confirm() (CẦN THAY THẾ):

| File | Số lượng | Mục đích | Priority |
|------|----------|----------|----------|
| `staff/timesheet/index.html` | 1 | Check-out | 🔴 Cao |
| `staff/chat/index.html` | 2 | Close/Return to AI | 🔴 Cao |
| `layouts/staff.html` | 1 | Logout | 🔴 Cao |
| `layouts/admin.html` | 1 | Logout | 🔴 Cao |
| `cart/index.html` | 2 | Remove/Clear | 🟡 TB |
| `admin/users/index.html` | 3 | Delete/Toggle | 🟡 TB |
| `admin/reviews/index.html` | 1 | Delete | 🟡 TB |
| `admin/products/index.html` | 1 | Delete | 🟡 TB |
| `admin/employees/index.html` | 2 | Delete/Toggle | 🟡 TB |
| `account/wishlist.html` | 2 | Remove/Clear | 🟡 TB |
| `static/js/staff-notifications.js` | 1 | Clear all | 🟢 Thấp |
| `static/js/profile-modals.js` | 1 | Delete address | 🟢 Thấp |
| `static/js/products.js` | 1 | Delete review | 🟢 Thấp |

**Tổng: ~19 confirm() cần thay thế**

### 4. 🗑️ CONSOLE.LOG DEBUG (CẦN XÓA):

| File | Số lượng | Loại | Action |
|------|----------|------|--------|
| `staff/chat/index.html` | ~80+ | Debug logs | ❌ Xóa hết |
| `vouchers.html` | 2 | Debug logs | ❌ Xóa |
| `staff/timesheet/index.html` | 4 | Error logs | ✅ Giữ lại |
| `staff/dashboard/index.html` | 2 | Error logs | ✅ Giữ lại |
| `main.js` | 1 | Warning | ✅ Giữ lại |
| `heroicons.js` | 1 | Warning | ✅ Giữ lại |

**Quy tắc:**
- ❌ **XÓA:** `console.log()`, `console.debug()` - Debug logs
- ✅ **GIỮ:** `console.error()`, `console.warn()` - Error/Warning logs (quan trọng cho debugging production)

---

## 🎯 CHIẾN LƯỢC MIGRATION

### Phase 1: Setup & Verification ✅
- [x] SweetAlert2 đã có trong layouts/main.html
- [x] SweetAlert2 đã có trong layouts/admin.html
- [x] showToast() function đã có trong main.js
- [x] Không có conflict với libraries khác

### Phase 2: Replace alert() - Priority Cao 🔴
**Files cần sửa:**
1. `staff/timesheet/index.html` (7 alerts)
2. `staff/profile/index.html` (1 alert)
3. `staff/orders/index.html` (2 alerts)
4. `staff/orders/detail.html` (2 alerts)
5. `staff/dashboard/index.html` (2 alerts)
6. `staff/chat/index.html` (9 alerts)

### Phase 3: Replace confirm() - Priority Cao 🔴
**Files cần sửa:**
1. `staff/timesheet/index.html` (1 confirm)
2. `staff/chat/index.html` (2 confirms)
3. `layouts/staff.html` (1 confirm)
4. `layouts/admin.html` (1 confirm)

### Phase 4: Replace alert() - Priority TB 🟡
**Files cần sửa:**
1. `reset-password.html` (1 alert)
2. `register.html` (1 alert)
3. `forgot-password.html` (1 alert)
4. `account/wishlist.html` (2 alerts)

### Phase 5: Replace confirm() - Priority TB 🟡
**Files cần sửa:**
1. `cart/index.html` (2 confirms)
2. `admin/users/index.html` (3 confirms)
3. `admin/reviews/index.html` (1 confirm)
4. `admin/products/index.html` (1 confirm)
5. `admin/employees/index.html` (2 confirms)
6. `account/wishlist.html` (2 confirms)

### Phase 6: Replace confirm() - Priority Thấp 🟢
**Files cần sửa:**
1. `static/js/staff-notifications.js` (1 confirm)
2. `static/js/profile-modals.js` (1 confirm)
3. `static/js/products.js` (1 confirm)

### Phase 7: Cleanup Debug Logs 🗑️
**Files cần sửa:**
1. `staff/chat/index.html` (~80+ console.log)
2. `vouchers.html` (2 console.log)

### Phase 8: Replace alert() - Priority Thấp 🟢
**Files cần sửa:**
1. `admin/users/index.html` (1 alert)

---

## 📝 CODE PATTERNS

### Pattern 1: alert() Success → showToast()
```javascript
// ❌ BEFORE
alert('Check-in thành công!');

// ✅ AFTER
showToast('Check-in thành công!', 'success');
```

### Pattern 2: alert() Error → showToast()
```javascript
// ❌ BEFORE
alert('Đã xảy ra lỗi khi check-in');

// ✅ AFTER
showToast('Đã xảy ra lỗi khi check-in', 'error');
```

### Pattern 3: alert() Validation → showToast()
```javascript
// ❌ BEFORE
alert('Vui lòng nhập đầy đủ họ, tên và email');

// ✅ AFTER
showToast('Vui lòng nhập đầy đủ họ, tên và email', 'warning');
```

### Pattern 4: alert() Info → showToast()
```javascript
// ❌ BEFORE
alert('Chức năng gửi email hàng loạt đang được phát triển');

// ✅ AFTER
showToast('Chức năng gửi email hàng loạt đang được phát triển', 'info');
```

### Pattern 5: confirm() Simple → Swal.fire()
```javascript
// ❌ BEFORE
if (!confirm('Bạn có chắc muốn check-out?')) return;

// ✅ AFTER
const result = await Swal.fire({
    title: 'Xác nhận Check-out?',
    text: 'Bạn sẽ kết thúc ca làm việc hôm nay.',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Xác nhận',
    cancelButtonText: 'Hủy',
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33'
});

if (!result.isConfirmed) return;
```

### Pattern 6: confirm() Destructive → Swal.fire()
```javascript
// ❌ BEFORE
if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

// ✅ AFTER
const result = await Swal.fire({
    title: 'Xác nhận xóa?',
    text: 'Hành động này không thể hoàn tác!',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6'
});

if (!result.isConfirmed) return;
```

### Pattern 7: console.log() Debug → XÓA
```javascript
// ❌ BEFORE
console.log('🚀 Initializing chat system...');
console.log('✅ Loaded conversations:', this.conversations.length);

// ✅ AFTER
// (Xóa hoàn toàn - không cần debug logs trong production)
```

### Pattern 8: console.error() → GIỮ LẠI
```javascript
// ✅ KEEP (Quan trọng cho debugging production)
console.error('Error loading conversations:', error);
console.warn('Cannot send typing indicator: missing required data');
```

---

## 🎨 SWEETALERT2 TYPES

### 1. Success Toast
```javascript
showToast('Thao tác thành công!', 'success');
```

### 2. Error Toast
```javascript
showToast('Có lỗi xảy ra!', 'error');
```

### 3. Warning Toast
```javascript
showToast('Vui lòng kiểm tra lại!', 'warning');
```

### 4. Info Toast
```javascript
showToast('Thông tin hữu ích', 'info');
```

### 5. Confirmation Dialog
```javascript
const result = await Swal.fire({
    title: 'Tiêu đề',
    text: 'Nội dung',
    icon: 'question', // hoặc 'warning' cho destructive actions
    showCancelButton: true,
    confirmButtonText: 'Xác nhận',
    cancelButtonText: 'Hủy'
});

if (result.isConfirmed) {
    // User clicked confirm
}
```

---

## ✅ CHECKLIST

### Phase 1: Setup ✅
- [x] Verify SweetAlert2 loaded
- [x] Verify showToast() function
- [x] Create migration plan

### Phase 2-3: Priority Cao 🔴
- [ ] staff/timesheet/index.html (7 alerts + 1 confirm)
- [ ] staff/profile/index.html (1 alert)
- [ ] staff/orders/index.html (2 alerts)
- [ ] staff/orders/detail.html (2 alerts)
- [ ] staff/dashboard/index.html (2 alerts)
- [ ] staff/chat/index.html (9 alerts + 2 confirms)
- [ ] layouts/staff.html (1 confirm)
- [ ] layouts/admin.html (1 confirm)

### Phase 4-5: Priority TB 🟡
- [ ] reset-password.html (1 alert)
- [ ] register.html (1 alert)
- [ ] forgot-password.html (1 alert)
- [ ] account/wishlist.html (2 alerts + 2 confirms)
- [ ] cart/index.html (2 confirms)
- [ ] admin/users/index.html (3 confirms)
- [ ] admin/reviews/index.html (1 confirm)
- [ ] admin/products/index.html (1 confirm)
- [ ] admin/employees/index.html (2 confirms)

### Phase 6: Priority Thấp 🟢
- [ ] static/js/staff-notifications.js (1 confirm)
- [ ] static/js/profile-modals.js (1 confirm)
- [ ] static/js/products.js (1 confirm)
- [ ] admin/users/index.html (1 alert)

### Phase 7: Cleanup 🗑️
- [ ] staff/chat/index.html (~80+ console.log)
- [ ] vouchers.html (2 console.log)

### Phase 8: Final
- [ ] Test all pages
- [ ] Verify no alert()/confirm() left
- [ ] Verify no debug console.log() left
- [ ] Update documentation

---

## 📈 EXPECTED RESULTS

### UX Improvements:
- ✅ Consistent notification style across entire website
- ✅ Beautiful, modern alerts (SweetAlert2)
- ✅ Non-blocking toast notifications
- ✅ Better user experience with confirmations
- ✅ Cleaner console (no debug logs)

### Code Quality:
- ✅ No native alert()/confirm() (outdated, ugly)
- ✅ No debug console.log() (production-ready)
- ✅ Consistent error handling
- ✅ Better maintainability

---

## 🚀 EXECUTION PLAN

**Estimated time:** 3-4 hours

**Order of execution:**
1. **Phase 2-3** (Priority Cao) - 1.5 hours
2. **Phase 4-5** (Priority TB) - 1 hour
3. **Phase 6** (Priority Thấp) - 0.5 hour
4. **Phase 7** (Cleanup) - 0.5 hour
5. **Phase 8** (Testing) - 0.5 hour

**Start with:** `staff/timesheet/index.html` (most alerts - 7)

---

## ⚠️ IMPORTANT NOTES

1. **Không xóa console.error() và console.warn()** - Cần thiết cho debugging production
2. **Xóa TẤT CẢ console.log() và console.debug()** - Chỉ dùng cho development
3. **Test kỹ mỗi file sau khi sửa** - Đảm bảo functionality không bị ảnh hưởng
4. **Giữ nguyên logic** - Chỉ thay đổi cách hiển thị thông báo
5. **Async/await cho confirm()** - SweetAlert2 confirmations return Promise

---

---

## 🔍 COMPREHENSIVE AUDIT PLAN

### BƯỚC 1: QUÉT TOÀN BỘ TEMPLATES
```bash
# Tìm tất cả alert() trong templates
grep -r "alert(" src/main/resources/templates/ --include="*.html"

# Tìm tất cả confirm() trong templates  
grep -r "confirm(" src/main/resources/templates/ --include="*.html"

# Tìm tất cả console.log() debug
grep -r "console\.log(" src/main/resources/templates/ --include="*.html"
```

### BƯỚC 2: QUÉT TOÀN BỘ JAVASCRIPT FILES
```bash
# Tìm tất cả alert() trong JS
grep -r "alert(" src/main/resources/static/js/ --include="*.js"

# Tìm tất cả confirm() trong JS
grep -r "confirm(" src/main/resources/static/js/ --include="*.js"

# Tìm tất cả console.log() debug
grep -r "console\.log(" src/main/resources/static/js/ --include="*.js"
```

### BƯỚC 3: KIỂM TRA LOGIC HIỆN TẠI
**⚠️ QUAN TRỌNG: KHÔNG ĐƯỢC THAY ĐỔI LOGIC!**

1. **Chỉ thay đổi cách hiển thị thông báo**
2. **Giữ nguyên tất cả conditions, loops, error handling**
3. **Đảm bảo async/await đúng cho confirmations**
4. **Test kỹ mỗi function sau khi sửa**

### BƯỚC 4: SỬ DỤNG NOTIFICATIONS.JS
**File notifications.js đã có sẵn các helper functions:**

```javascript
// Thay vì: alert('Success message')
showSuccess('Success message');

// Thay vì: alert('Error message') 
showError('Error message');

// Thay vì: alert('Warning message')
showWarning('Warning message');

// Thay vì: alert('Info message')
showInfo('Info message');

// Thay vì: confirm('Delete this?')
const confirmed = await confirmDelete('Xóa item này?', 'Không thể hoàn tác!');
if (confirmed) { /* proceed */ }

// Thay vì: confirm('Continue?')
const confirmed = await confirmAction('Tiếp tục?', 'Bạn có chắc chắn?');
if (confirmed) { /* proceed */ }
```

### BƯỚC 5: PATTERN MAPPING

| Tình huống | Old Code | New Code |
|------------|----------|----------|
| **Success** | `alert('Thành công!')` | `showSuccess('Thành công!')` |
| **Error** | `alert('Lỗi!')` | `showError('Lỗi!')` |
| **Warning** | `alert('Cảnh báo!')` | `showWarning('Cảnh báo!')` |
| **Info** | `alert('Thông tin')` | `showInfo('Thông tin')` |
| **Delete Confirm** | `if (!confirm('Xóa?')) return;` | `if (!(await confirmDelete('Xóa?'))) return;` |
| **Action Confirm** | `if (!confirm('OK?')) return;` | `if (!(await confirmAction('OK?'))) return;` |

---

## 📋 DETAILED EXECUTION CHECKLIST

### Phase 2-3: Priority Cao 🔴 (TIẾP TỤC)

#### ✅ COMPLETED (5/8 files):
- [x] staff/timesheet/index.html 
- [x] staff/profile/index.html
- [x] staff/orders/index.html
- [x] staff/orders/detail.html
- [x] staff/dashboard/index.html

#### 🔄 IN PROGRESS (3/8 files):

**1. staff/chat/index.html** (9 alerts + 2 confirms)
- [ ] Line 699: `alert('Không thể gửi tin nhắn')` → `showError('Không thể gửi tin nhắn')`
- [ ] Line 875: `alert('Đã đóng cuộc hội thoại')` → `showSuccess('Đã đóng cuộc hội thoại')`
- [ ] Line 878: `alert('Không thể đóng cuộc hội thoại: ...')` → `showError('Không thể đóng cuộc hội thoại: ...')`
- [ ] Line 924: `alert('Chưa chọn cuộc hội thoại')` → `showWarning('Chưa chọn cuộc hội thoại')`
- [ ] Line 970: `alert('Không thể trao lại cho AI: ...')` → `showError('Không thể trao lại cho AI: ...')`
- [ ] Line 974: `alert('Lỗi khi trao lại cho AI: ...')` → `showError('Lỗi khi trao lại cho AI: ...')`
- [ ] Line 1030: `alert('Đã nhận cuộc hội thoại')` → `showSuccess('Đã nhận cuộc hội thoại')`
- [ ] Line 1033: `alert('Không thể nhận cuộc hội thoại: ...')` → `showError('Không thể nhận cuộc hội thoại: ...')`
- [ ] Line 1037: `alert('Không thể nhận cuộc hội thoại')` → `showError('Không thể nhận cuộc hội thoại')`
- [ ] Line 1421: `alert('Không tìm thấy cuộc hội thoại này')` → `showError('Không tìm thấy cuộc hội thoại này')`
- [ ] Line 855: `confirm('Bạn có chắc muốn đóng cuộc hội thoại này?')` → `await confirmAction(...)`
- [ ] Line 931: `confirm('Trao lại cuộc hội thoại này cho Hoa AI?...')` → `await confirmAction(...)`

**2. layouts/staff.html** (1 confirm)
- [ ] Line 328: `onclick="return confirm('Bạn có chắc muốn đăng xuất?')"` → Convert to async function

**3. layouts/admin.html** (1 confirm)  
- [ ] Line 404: `onclick="return confirm('Bạn có chắc muốn đăng xuất?')"` → Convert to async function

### Phase 4-5: Priority TB 🟡 (10 files)

**Authentication Pages:**
- [ ] reset-password.html (1 alert)
- [ ] register.html (1 alert)  
- [ ] forgot-password.html (1 alert)

**User Pages:**
- [ ] account/wishlist.html (2 alerts + 2 confirms)
- [ ] cart/index.html (2 confirms)

**Admin Pages:**
- [ ] admin/users/index.html (3 confirms + 1 alert)
- [ ] admin/reviews/index.html (1 confirm)
- [ ] admin/products/index.html (1 confirm)
- [ ] admin/employees/index.html (2 confirms)

### Phase 6: Priority Thấp 🟢 (3 files)

**JavaScript Files:**
- [ ] static/js/staff-notifications.js (1 confirm)
- [ ] static/js/profile-modals.js (1 confirm)
- [ ] static/js/products.js (1 confirm)

### Phase 7: Cleanup Debug Logs 🗑️ (2 files)

**Debug Log Removal:**
- [ ] staff/chat/index.html (~80+ console.log) - XÓA HẾT
- [ ] vouchers.html (2 console.log) - XÓA HẾT

**⚠️ GIỮ LẠI:**
- ✅ console.error() - Cần thiết cho debugging production
- ✅ console.warn() - Cần thiết cho debugging production

---

## 🧪 TESTING PROTOCOL

### Sau mỗi file được sửa:

1. **Functional Test:**
   - Test tất cả buttons/actions trong file
   - Verify notifications hiển thị đúng
   - Verify logic vẫn hoạt động như cũ

2. **UI/UX Test:**
   - Notifications đẹp mắt (SweetAlert2)
   - Không còn browser alerts xấu
   - Toast position và timing phù hợp

3. **Console Check:**
   - Không còn console.log() debug
   - Vẫn có console.error() khi cần
   - Không có JavaScript errors

### Final Integration Test:

1. **Test toàn bộ user flows:**
   - Staff workflows (timesheet, orders, chat)
   - Admin workflows (users, products, reviews)
   - User workflows (register, login, cart, wishlist)

2. **Cross-browser test:**
   - Chrome, Firefox, Safari, Edge
   - Mobile responsive

3. **Performance check:**
   - No console spam
   - SweetAlert2 loads properly
   - No memory leaks

---

## 📊 SUCCESS METRICS

### Code Quality:
- ✅ 0 alert() remaining
- ✅ 0 confirm() remaining  
- ✅ 0 console.log() debug remaining
- ✅ All notifications use SweetAlert2
- ✅ Consistent notification patterns

### User Experience:
- ✅ Beautiful, modern notifications
- ✅ Consistent UI across all pages
- ✅ Non-blocking toast notifications
- ✅ Proper confirmation dialogs
- ✅ Better accessibility

### Maintainability:
- ✅ Centralized notification system (notifications.js)
- ✅ Reusable helper functions
- ✅ Clean, production-ready code
- ✅ Easy to extend/modify

**READY TO START COMPREHENSIVE MIGRATION!** 🚀
