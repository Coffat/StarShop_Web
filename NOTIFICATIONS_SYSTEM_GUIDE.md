# 📢 HƯỚNG DẪN SỬ DỤNG HỆ THỐNG THÔNG BÁO - STARSHOP

**Ngày tạo:** 23/10/2025  
**File:** `/static/js/notifications.js`

---

## 🎯 MỤC ĐÍCH

Tạo **1 hệ thống thông báo tập trung** để:
- ✅ Dễ quản lý - Sửa 1 chỗ, apply toàn bộ
- ✅ Consistent API - Dùng giống nhau ở mọi nơi
- ✅ Fallback support - Nếu SweetAlert2 chưa load
- ✅ Type safety - Có JSDoc comments

---

## 📦 ĐÃ THÊM VÀO LAYOUTS

### ✅ layouts/main.html (Line 89-93)
```html
<!-- SweetAlert2 - MUST load before notifications.js -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<!-- Notifications Utility - Centralized notification system -->
<script th:src="@{/js/notifications.js}"></script>
```

### ✅ layouts/staff.html (Line 19-23)
```html
<!-- SweetAlert2 - MUST load before notifications.js -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<!-- Notifications Utility - Centralized notification system -->
<script th:src="@{/js/notifications.js}"></script>
```

### ⏳ layouts/admin.html
**TODO:** Cần thêm tương tự

---

## 🚀 CÁCH SỬ DỤNG

### 1. TOAST NOTIFICATIONS (Khuyên dùng)

#### Success Toast
```javascript
showSuccess('Thao tác thành công!');
// hoặc
showToast('Thao tác thành công!', 'success');
```

#### Error Toast
```javascript
showError('Có lỗi xảy ra!');
// hoặc
showToast('Có lỗi xảy ra!', 'error');
```

#### Warning Toast
```javascript
showWarning('Vui lòng kiểm tra lại!');
// hoặc
showToast('Vui lòng kiểm tra lại!', 'warning');
```

#### Info Toast
```javascript
showInfo('Thông tin hữu ích');
// hoặc
showToast('Thông tin hữu ích', 'info');
```

---

### 2. CONFIRMATION DIALOGS

#### Normal Confirmation
```javascript
const confirmed = await confirmAction('Xác nhận thao tác?', 'Bạn có chắc chắn muốn tiếp tục?');
if (confirmed) {
    // User clicked "Xác nhận"
    console.log('User confirmed');
} else {
    // User clicked "Hủy"
    console.log('User cancelled');
}
```

#### Delete Confirmation (Destructive)
```javascript
const confirmed = await confirmDelete('Xác nhận xóa?', 'Hành động này không thể hoàn tác!');
if (confirmed) {
    // Proceed with deletion
    deleteItem();
}
```

#### Custom Confirmation
```javascript
const confirmed = await showConfirm({
    title: 'Xác nhận Check-out?',
    text: 'Bạn sẽ kết thúc ca làm việc hôm nay.',
    icon: 'question',
    confirmButtonText: 'Xác nhận',
    cancelButtonText: 'Hủy',
    isDanger: false
});

if (confirmed) {
    // User confirmed
}
```

---

### 3. DIALOG VỚI HTML CONTENT

```javascript
showDialog({
    title: 'Chi tiết đơn hàng',
    html: `
        <div class="text-left">
            <p><strong>Mã đơn:</strong> #12345</p>
            <p><strong>Tổng tiền:</strong> 500,000đ</p>
            <p><strong>Trạng thái:</strong> Đang giao</p>
        </div>
    `,
    icon: 'info',
    confirmButtonText: 'Đóng'
});
```

---

### 4. LOADING DIALOG

```javascript
// Show loading
showLoading('Đang xử lý...');

// Do async work
await someAsyncOperation();

// Close loading
closeLoading();
```

---

## 📚 FULL API REFERENCE

### Toast Functions

| Function | Parameters | Description |
|----------|------------|-------------|
| `showToast(message, type, options)` | message: string<br>type: 'success'\|'error'\|'warning'\|'info'<br>options: object | Hiển thị toast notification |
| `showSuccess(message)` | message: string | Shorthand cho success toast |
| `showError(message)` | message: string | Shorthand cho error toast |
| `showWarning(message)` | message: string | Shorthand cho warning toast |
| `showInfo(message)` | message: string | Shorthand cho info toast |

### Confirmation Functions

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `showConfirm(config)` | config: object | Promise\<boolean\> | Hiển thị confirmation dialog |
| `confirmAction(title, text)` | title: string<br>text: string | Promise\<boolean\> | Normal confirmation |
| `confirmDelete(title, text)` | title: string<br>text: string | Promise\<boolean\> | Delete confirmation (red button) |

### Dialog Functions

| Function | Parameters | Description |
|----------|------------|-------------|
| `showDialog(config)` | config: object | Hiển thị dialog với HTML content |
| `showLoading(message)` | message: string | Hiển thị loading dialog |
| `closeLoading()` | - | Đóng loading dialog |

---

## 🔧 CONFIG OBJECTS

### showConfirm() config:
```javascript
{
    title: 'Tiêu đề',              // Required
    text: 'Nội dung',              // Optional
    icon: 'question',              // 'question', 'warning', 'info'
    confirmButtonText: 'Xác nhận', // Optional
    cancelButtonText: 'Hủy',       // Optional
    isDanger: false                // true = red confirm button
}
```

### showDialog() config:
```javascript
{
    title: 'Tiêu đề',              // Required
    html: '<div>...</div>',        // HTML content
    icon: 'info',                  // 'info', 'success', 'warning', 'error'
    confirmButtonText: 'Đóng'      // Optional
}
```

### showToast() options:
```javascript
{
    timer: 3000,                   // Auto close after ms
    position: 'top-end',           // Position
    // ... other SweetAlert2 options
}
```

---

## ✅ VÍ DỤ THỰC TẾ

### Example 1: Check-in Success
```javascript
async function checkIn() {
    try {
        const response = await fetch('/api/staff/check-in', { method: 'POST' });
        const data = await response.json();
        
        if (data.success) {
            showSuccess('Check-in thành công!');
        } else {
            showError(data.message || 'Check-in thất bại');
        }
    } catch (error) {
        console.error('Error checking in:', error);
        showError('Đã xảy ra lỗi khi check-in');
    }
}
```

### Example 2: Check-out with Confirmation
```javascript
async function checkOut() {
    const confirmed = await confirmAction(
        'Xác nhận Check-out?',
        'Bạn sẽ kết thúc ca làm việc hôm nay.'
    );
    
    if (!confirmed) return;
    
    try {
        const response = await fetch('/api/staff/check-out', { method: 'POST' });
        const data = await response.json();
        
        if (data.success) {
            showSuccess(`Check-out thành công! Bạn đã làm việc ${data.data.hoursWorked} giờ hôm nay.`);
        } else {
            showError(data.message || 'Check-out thất bại');
        }
    } catch (error) {
        console.error('Error checking out:', error);
        showError('Đã xảy ra lỗi khi check-out');
    }
}
```

### Example 3: Delete with Confirmation
```javascript
async function deleteProduct(productId) {
    const confirmed = await confirmDelete(
        'Xác nhận xóa sản phẩm?',
        'Hành động này không thể hoàn tác!'
    );
    
    if (!confirmed) return;
    
    try {
        const response = await fetch(`/api/products/${productId}`, { method: 'DELETE' });
        
        if (response.ok) {
            showSuccess('Đã xóa sản phẩm thành công!');
            location.reload();
        } else {
            showError('Không thể xóa sản phẩm');
        }
    } catch (error) {
        console.error('Error deleting product:', error);
        showError('Đã xảy ra lỗi khi xóa sản phẩm');
    }
}
```

### Example 4: Form Validation
```javascript
function validateForm() {
    const email = document.getElementById('email').value.trim();
    
    if (!email) {
        showWarning('Vui lòng nhập email!');
        return false;
    }
    
    if (!isValidEmail(email)) {
        showWarning('Email không hợp lệ!');
        return false;
    }
    
    return true;
}
```

### Example 5: Show Details Dialog
```javascript
function showOrderDetails(order) {
    showDialog({
        title: `Chi tiết đơn hàng #${order.id}`,
        html: `
            <div class="text-left space-y-2">
                <p><strong>Khách hàng:</strong> ${order.customerName}</p>
                <p><strong>Tổng tiền:</strong> ${formatCurrency(order.total)}</p>
                <p><strong>Trạng thái:</strong> ${order.status}</p>
                <p><strong>Ngày đặt:</strong> ${formatDate(order.createdAt)}</p>
            </div>
        `,
        icon: 'info',
        confirmButtonText: 'Đóng'
    });
}
```

---

## 🔄 MIGRATION GUIDE

### Từ alert() → showToast()
```javascript
// ❌ BEFORE
alert('Thành công!');
alert('Có lỗi xảy ra');

// ✅ AFTER
showSuccess('Thành công!');
showError('Có lỗi xảy ra');
```

### Từ confirm() → confirmAction()
```javascript
// ❌ BEFORE
if (!confirm('Bạn có chắc?')) return;
doSomething();

// ✅ AFTER
const confirmed = await confirmAction('Bạn có chắc?');
if (!confirmed) return;
doSomething();
```

### Từ confirm() (delete) → confirmDelete()
```javascript
// ❌ BEFORE
if (!confirm('Xóa sản phẩm này?')) return;
deleteProduct();

// ✅ AFTER
const confirmed = await confirmDelete('Xóa sản phẩm này?', 'Không thể hoàn tác!');
if (!confirmed) return;
deleteProduct();
```

---

## ⚠️ IMPORTANT NOTES

1. **Async/Await Required:** Tất cả confirmation functions đều return Promise
2. **SweetAlert2 Dependency:** Cần load SweetAlert2 trước notifications.js
3. **Fallback Support:** Tự động fallback về native alert/confirm nếu SweetAlert2 chưa load
4. **Global Access:** Có thể dùng qua `window.Notifications.showToast()` hoặc trực tiếp `showToast()`

---

## 🐛 TROUBLESHOOTING

### Lỗi: "Swal is not defined"
**Nguyên nhân:** SweetAlert2 chưa được load  
**Giải pháp:** Đảm bảo SweetAlert2 CDN được load trước notifications.js

### Lỗi: "showToast is not a function"
**Nguyên nhân:** notifications.js chưa được load  
**Giải pháp:** Thêm `<script th:src="@{/js/notifications.js}"></script>` vào layout

### Toast không hiển thị
**Nguyên nhân:** Có thể do z-index hoặc CSS conflict  
**Giải pháp:** Check console for errors, verify SweetAlert2 loaded

---

## 📊 BENEFITS

| Before | After | Benefit |
|--------|-------|---------|
| alert() ở nhiều file | showToast() centralized | ✅ Dễ maintain |
| confirm() ở nhiều file | confirmAction() centralized | ✅ Consistent UX |
| Nhiều implementations khác nhau | 1 implementation duy nhất | ✅ Không conflict |
| Ugly native alerts | Beautiful SweetAlert2 | ✅ Better UX |
| Blocking alerts | Non-blocking toasts | ✅ Better UX |

---

**🎉 READY TO USE!**

Bây giờ chỉ cần:
1. Import notifications.js vào layout
2. Dùng `showSuccess()`, `showError()`, `confirmAction()`, etc.
3. Enjoy consistent, beautiful notifications! 🚀
