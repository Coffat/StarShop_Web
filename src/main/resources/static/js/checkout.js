(function() {
	let selectedPaymentMethod = null;
	let cartData = null;

	document.addEventListener('DOMContentLoaded', function() {
		loadCartData();
		loadPaymentMethods();
		const btn = document.getElementById('place-order-btn');
		if (btn) {
			btn.addEventListener('click', placeOrder);
		}
		['shippingName','shippingPhone','shippingAddress'].forEach(function(id){
			const el = document.getElementById(id);
			if (el) el.addEventListener('input', updatePlaceOrderButton);
		});
	});

	function loadCartData() {
		fetch('/api/cart/get', { credentials: 'same-origin' })
			.then(r => r.json())
			.then(response => {
				if (response.data && response.data.success) {
					cartData = response.data.cart;
					displayCartItems(cartData);
					displayOrderSummary(cartData);
					if (cartData.items && cartData.items.length > 0) {
						document.getElementById('place-order-btn').disabled = false;
					}
				} else {
					document.getElementById('cart-items').innerHTML = '<div class="alert alert-warning">Giỏ hàng trống</div>';
					document.getElementById('order-summary').innerHTML = '<div class="alert alert-warning">Không có sản phẩm nào trong giỏ hàng</div>';
				}
			})
			.catch(() => {
				document.getElementById('cart-items').innerHTML = '<div class="alert alert-danger">Có lỗi xảy ra khi tải giỏ hàng</div>';
				document.getElementById('order-summary').innerHTML = '<div class="alert alert-danger">Có lỗi xảy ra khi tải thông tin</div>';
			});
	}

	function loadPaymentMethods() {
		fetch('/api/payment/methods', { credentials: 'same-origin' })
			.then(r => r.json())
			.then(response => {
				if (response.data) displayPaymentMethods(response.data);
			})
			.catch(() => {
				document.getElementById('payment-methods').innerHTML = '<div class="alert alert-danger">Có lỗi xảy ra khi tải phương thức thanh toán</div>';
			});
	}

	function displayCartItems(cart) {
		let html = '';
		if (cart.items && cart.items.length > 0) {
			cart.items.forEach(function(item) {
				html += '<div class="d-flex align-items-center mb-3 p-3 border rounded">'
					+ '<img src="' + (item.productImageUrl || '/images/products/default.jpg') + '" class="rounded me-3" width="60" height="60" style="object-fit: cover;">'
					+ '<div class="flex-grow-1">'
					+ '<h6 class="mb-1">' + (item.productName || '') + '</h6>'
					+ '<small class="text-muted">Số lượng: ' + (item.quantity || 0) + '</small>'
					+ '</div>'
					+ '<div class="text-end">'
					+ '<strong>' + formatCurrency(item.totalPrice || 0) + '</strong>'
					+ '</div>'
					+ '</div>';
			});
		} else {
			html = '<div class="alert alert-warning">Giỏ hàng trống</div>';
		}
		document.getElementById('cart-items').innerHTML = html;
	}

	function displayOrderSummary(cart) {
		let html = '';
		if (cart.items && cart.items.length > 0) {
			let subtotal = cart.totalAmount || 0;
			let shipping = 0;
			let total = subtotal + shipping;
			html = '<div class="d-flex justify-content-between mb-2">'
				+ '<span>Tạm tính:</span>'
				+ '<span>' + formatCurrency(subtotal) + '</span>'
				+ '</div>'
				+ '<div class="d-flex justify-content-between mb-2">'
				+ '<span>Phí vận chuyển:</span>'
				+ '<span class="text-success">Miễn phí</span>'
				+ '</div>'
				+ '<hr>'
				+ '<div class="d-flex justify-content-between mb-3">'
				+ '<strong>Tổng cộng:</strong>'
				+ '<strong class="text-primary">' + formatCurrency(total) + '</strong>'
				+ '</div>';
		} else {
			html = '<div class="alert alert-warning">Không có sản phẩm nào trong giỏ hàng</div>';
		}
		document.getElementById('order-summary').innerHTML = html;
	}

	function displayPaymentMethods(methods) {
		let html = '';
		Object.keys(methods).forEach(function(key) {
			const method = methods[key];
			const isAvailable = method.available;
			const statusClass = isAvailable ? 'text-success' : 'text-warning';
			const disabledAttr = isAvailable ? '' : 'disabled';
			html += '<div class="form-check mb-3">'
				+ '<input class="form-check-input" type="radio" name="paymentMethod" id="payment-' + key + '" value="' + key + '" ' + disabledAttr + '>'
				+ '<label class="form-check-label" for="payment-' + key + '">'
				+ '<div class="d-flex align-items-center">'
				+ '<div class="flex-grow-1">'
				+ '<strong>' + method.displayName + '</strong><br>'
				+ '<small class="text-muted">' + method.englishName + '</small>'
				+ '</div>'
				+ '<div class="text-end">'
				+ '<small class="' + statusClass + '">' + method.statusMessage + '</small>'
				+ '</div>'
				+ '</div>'
				+ '</label>'
				+ '</div>';
		});
		document.getElementById('payment-methods').innerHTML = html;
		Array.from(document.querySelectorAll('input[name="paymentMethod"]')).forEach(function(r){
			r.addEventListener('change', function(){
				selectedPaymentMethod = this.value;
				updatePlaceOrderButton();
			});
		});
	}

	function updatePlaceOrderButton() {
		let hasItems = cartData && cartData.items && cartData.items.length > 0;
		let hasPaymentMethod = selectedPaymentMethod !== null;
		let shippingName = document.getElementById('shippingName')?.value?.trim() || '';
		let shippingPhone = document.getElementById('shippingPhone')?.value?.trim() || '';
		let shippingAddress = document.getElementById('shippingAddress')?.value?.trim() || '';
		let hasShippingInfo = shippingName !== '' && shippingPhone !== '' && shippingAddress !== '';
		document.getElementById('place-order-btn').disabled = !(hasItems && hasPaymentMethod && hasShippingInfo);
	}

	function placeOrder() {
		if (!selectedPaymentMethod) {
			alert('Vui lòng chọn phương thức thanh toán');
			return;
		}
		const btn = document.getElementById('place-order-btn');
		btn.disabled = true;
		btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
		const data = {
			orderRequest: {
				shippingAddress: document.getElementById('shippingAddress').value.trim(),
				shippingPhone: document.getElementById('shippingPhone').value.trim(),
				notes: document.getElementById('orderNotes').value.trim()
			},
			paymentMethod: selectedPaymentMethod
		};
		fetch('/api/orders/create-with-payment', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			credentials: 'same-origin',
			body: JSON.stringify(data)
		})
		.then(r => r.json())
		.then(response => {
			if (response.data && response.data.payment) {
				const payment = response.data.payment;
				const details = payment.paymentDetails || {};
				if (payment.success && details.payUrl) {
					window.location.href = details.payUrl;
					return;
				}
				if (payment.success) {
					alert('Đơn hàng đã được tạo thành công!');
					window.location.href = '/orders';
					return;
				}
			}
			alert((response.data && response.data.payment && response.data.payment.message) || 'Có lỗi xảy ra');
		})
		.catch(() => alert('Có lỗi xảy ra khi đặt hàng'))
		.finally(() => {
			btn.disabled = false;
			btn.innerHTML = '<i class="fas fa-check me-2"></i>Đặt hàng';
		});
	}

	function formatCurrency(amount) {
		try {
			return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
		} catch (_) { return amount; }
	}
})();
