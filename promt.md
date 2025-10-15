Prompt Hướng Dẫn Sửa Lỗi (Dành cho Cursor AI)
Chào bạn, tôi đang gặp một vấn đề với chức năng "Yêu thích" trên trang danh sách sản phẩm (`products/index.html`). Khi tôi thêm một sản phẩm vào danh sách yêu thích, icon trái tim chuyển sang trạng thái "active" (đầy màu), nhưng khi tôi tải lại trang thì nó lại trở về trạng thái ban đầu (rỗng).

**Yêu cầu:**

Hãy sửa lỗi này để icon trái tim **luôn hiển thị đúng trạng thái** (đã yêu thích hay chưa) mỗi khi trang sản phẩm được tải, bằng cách thực hiện các bước sau:

**Bước 1: Cập nhật DTO của sản phẩm để chứa trạng thái yêu thích**

* Mở file DTO mà `ProductController` đang sử dụng để hiển thị danh sách sản phẩm (có thể là `ProductDTO` hoặc `AdminProductDTO`).
* Thêm một thuộc tính boolean mới vào DTO này:
    ```java
    private boolean isFavorite = false; // Mặc định là false
    ```
    *Nhớ thêm getter và setter cho nó.*

**Bước 2: Cập nhật `ProductService` để gán trạng thái yêu thích**

* Mở file `ProductService.java`.
* Tìm đến phương thức lấy danh sách sản phẩm phân trang (ví dụ: `findAll`, `findPaginated`, hoặc tương tự).
* Trong phương thức này, hãy:
    1.  Lấy thông tin người dùng đang đăng nhập.
    2.  Nếu người dùng đã đăng nhập, lấy danh sách ID các sản phẩm có trong wishlist của họ (ví dụ: `wishlistService.getProductIdsInWishlist(currentUser)`).
    3.  Khi bạn chuyển đổi từ `Product` (Entity) sang `ProductDTO`, hãy kiểm tra xem ID của sản phẩm có nằm trong danh sách ID yêu thích của người dùng không.
    4.  Nếu có, gán `productDTO.setFavorite(true)`.

**Bước 3: Cập nhật `products/index.html` để hiển thị icon đúng trạng thái**

* Mở file `resources/templates/products/index.html`.
* Tìm đến nút `<button>` có class `btn-wishlist`.
* Sử dụng `th:classappend` để thêm class `active` nếu sản phẩm đã được yêu thích.
* Sử dụng `th:if` và `th:unless` để hiển thị đúng icon (trái tim đầy màu hoặc rỗng).

**Mã HTML để bạn tham khảo:**
```html
<button class="btn-action btn-wishlist"
        th:classappend="${product.isFavorite} ? 'active' : ''"
        th:data-product-id="${product.id}"
        onclick="toggleWishlist(this)"
        title="Thêm vào yêu thích"
        sec:authorize="isAuthenticated()">
    
    <svg th:if="${product.isFavorite}" class="w-5 h-5" xmlns="[http://www.w3.org/2000/svg](http://www.w3.org/2000/svg)" viewBox="0 0 20 20" fill="currentColor">
        <path d="m9.653 16.915-.005-.003-.019-.01a20.759 20.759 0 0 1-1.162-.682 22.045 22.045 0 0 1-2.582-1.9C4.045 12.733 2 10.352 2 7.5a4.5 4.5 0 0 1 8-2.828A4.5 4.5 0 0 1 18 7.5c0 2.852-2.044 5.233-3.885 6.82a22.049 22.049 0 0 1-3.744 2.582l-.019.01-.005.003h-.002a.739.739 0 0 1-.69.001l-.002-.001Z" />
    </svg>
    
    <svg th:unless="${product.isFavorite}" class="w-5 h-5" xmlns="[http://www.w3.org/2000/svg](http://www.w3.org/2000/svg)" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
    </svg>
</button>
Lưu ý: Bạn cũng cần làm điều tương tự cho trang chi tiết sản phẩm products/detail.html.

Bước 4 (Quan trọng): Cập nhật JavaScript toggleWishlist

Mở main.js, tìm hàm toggleWishlist.

Khi API /api/wishlist/toggle trả về kết quả thành công, hàm JavaScript đang thay đổi icon của button. Hãy giữ nguyên logic này vì nó mang lại hiệu ứng real-time ngay sau khi click, không cần chờ tải lại trang.

Bằng cách kết hợp cả hai phương pháp (kiểm tra từ server khi tải trang và cập nhật bằng JS khi người dùng tương tác), bạn sẽ có trải nghiệm người dùng tốt nhất.