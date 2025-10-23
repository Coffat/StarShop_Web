# 🔄 MIGRATION: Font Awesome → Heroicons

## 📋 DANH SÁCH ICONS ĐANG DÙNG

### Icons phát hiện trong project:

| Font Awesome Icon | Dùng ở đâu | Heroicon thay thế |
|-------------------|------------|-------------------|
| `fa-heart` (regular/solid) | Wishlist button | `heart` |
| `fa-shopping-cart` | Cart button | `shopping-cart` |
| `fa-user` | Profile, avatar | `user` |
| `fa-search` | Search button | `magnifying-glass` |
| `fa-spinner` | Loading states | `arrow-path` (spin) |
| `fa-clock` | Timesheet | `clock` |
| `fa-sign-in-alt` | Check-in button | `arrow-right-on-rectangle` |
| `fa-sign-out-alt` | Check-out button | `arrow-left-on-rectangle` |
| `fa-calendar-day` | Today stats | `calendar-days` |
| `fa-calendar-week` | Week stats | `calendar` |
| `fa-calendar-alt` | Month stats | `calendar` |
| `fa-calendar` | Calendar header | `calendar` |
| `fa-chevron-left` | Pagination | `chevron-left` |
| `fa-chevron-right` | Pagination | `chevron-right` |
| `fa-list` | List view | `list-bullet` |
| `fa-calendar-times` | Empty state | `calendar-days` + `x-mark` |
| `fa-badge-check` | Verified badge | `check-badge` |
| `fa-pencil` | Edit button | `pencil` |
| `fa-check-circle` | Verified status | `check-circle` |
| `fa-exclamation-circle` | Warning | `exclamation-circle` |
| `fa-circle` | Status indicator | `circle` (custom) |
| `fa-chart-bar` | Statistics | `chart-bar` |
| `fa-redo` | Retry button | `arrow-path` |

**Tổng: ~23 icons**

---

## 🎯 CHIẾN LƯỢC MIGRATION

### Option đã chọn: **Heroicons CDN + Inline SVG**

**Lý do:**
- ✅ Lightweight (~0KB nếu inline, ~30KB nếu dùng full CDN)
- ✅ Modern design (Tailwind team)
- ✅ Free, open-source
- ✅ Dễ customize (SVG)
- ✅ Không cần build step

### Cách dùng:

#### 1. Outline icons (mặc định - stroke)
```html
<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
</svg>
```

#### 2. Solid icons (filled)
```html
<svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
  <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z"/>
</svg>
```

---

## 🚀 IMPLEMENTATION PLAN

### Phase 1: Tạo Icon Component System ✅
1. Tạo file `/static/js/heroicons.js` với helper functions
2. Tạo CSS utilities cho icons

### Phase 2: Replace trong Products Page (Priority cao)
1. Wishlist heart icon (fa-heart → heroicon heart)
2. Cart icon (fa-shopping-cart → heroicon shopping-cart)
3. Search icon (fa-search → heroicon magnifying-glass)

### Phase 3: Replace trong Staff/Admin Pages
1. Timesheet icons
2. Profile icons
3. Dashboard icons

### Phase 4: Replace Loading Spinners
1. fa-spinner → heroicon arrow-path (với animation)

### Phase 5: Cleanup
1. Remove Font Awesome CDN từ layouts
2. Test toàn bộ pages
3. Update documentation

---

## 📦 FILES CẦN SỬA

### Templates:
- `products/index.html` - Wishlist, cart icons
- `staff/timesheet/index.html` - Calendar, clock icons
- `staff/profile/index.html` - User, edit, status icons
- `layouts/main.html` - Remove Font Awesome CDN
- Và ~20 files khác

### JavaScript:
- `main.js` - Wishlist toggle icons
- `staff-dashboard.js` - Check-in/out button icons
- `admin-product-ai.js` - Loading spinner
- `admin-ai-insights.js` - Retry button icon

---

## 🎨 CSS UTILITIES

Sẽ tạo CSS classes để dễ dùng:

```css
/* Icon sizes */
.icon-xs { width: 1rem; height: 1rem; }
.icon-sm { width: 1.25rem; height: 1.25rem; }
.icon { width: 1.5rem; height: 1.5rem; }
.icon-lg { width: 2rem; height: 2rem; }
.icon-xl { width: 3rem; height: 3rem; }

/* Icon animations */
.icon-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
```

---

## ✅ READY TO START!
