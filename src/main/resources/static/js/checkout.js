(function() {
	let selectedPaymentMethod = null;
	let cartData = null;
	let shippingFee = 0;
	let shippingCalculated = false;

	document.addEventListener('DOMContentLoaded', function() {
		loadCartData();
		loadPaymentMethods();
		const btn = document.getElementById('place-order-btn');
		if (btn) {
			btn.addEventListener('click', placeOrder);
		}
		const addressSelect = document.getElementById('addressSelect');
		if (addressSelect) {
			addressSelect.addEventListener('change', function() {
				updateAddressDetails();
				calculateShippingFee();
				updatePlaceOrderButton();
			});
			// Show initial address if default is selected
			if (addressSelect.value) {
				updateAddressDetails();
				calculateShippingFee();
			}
		}
	});

	function updateAddressDetails() {
		const select = document.getElementById('addressSelect');
		const detailsDiv = document.getElementById('selectedAddressDetails');
		const detailsText = document.getElementById('addressDetailsText');
		const shippingSection = document.getElementById('shippingFeeSection');
		
		if (select.value) {
			const selectedOption = select.options[select.selectedIndex];
			detailsText.textContent = selectedOption.text;
			detailsDiv.style.display = 'block';
			shippingSection.style.display = 'block';
		} else {
			detailsDiv.style.display = 'none';
			shippingSection.style.display = 'none';
			shippingFee = 0;
			shippingCalculated = false;
		}
	}

	function calculateShippingFee() {
		const select = document.getElementById('addressSelect');
		const shippingContent = document.getElementById('shippingFeeContent');
		
		if (!select.value) {
			return;
		}
		
		// Show loading
		shippingContent.innerHTML = `
			<div class="text-center">
				<div class="spinner-border spinner-border-sm text-primary" role="status">
					<span class="visually-hidden">Đang tính phí...</span>
				</div>
				<small class="text-muted ms-2">Đang tính phí vận chuyển...</small>
			</div>
		`;
		
		// Call shipping fee API
		fetch('/api/shipping/fee', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			credentials: 'same-origin',
			body: JSON.stringify({
				addressId: parseInt(select.value),
				serviceTypeId: 2 // Default GHN service type
			})
		})
		.then(r => r.json())
		.then(response => {
			if (response.data && response.data.success) {
				shippingFee = response.data.shippingFee || 0;
				shippingCalculated = true;
				
				shippingContent.innerHTML = `
					<div class="d-flex justify-content-between align-items-center">
						<div>
							<strong class="text-success">${formatCurrency(shippingFee)}</strong>
							<br><small class="text-muted">Giao hàng nhanh (GHN)</small>
						</div>
						<i class="fas fa-check-circle text-success"></i>
					</div>
				`;
			} else {
				// Fallback to free shipping
				shippingFee = 0;
				shippingCalculated = true;
				
				shippingContent.innerHTML = `
					<div class="d-flex justify-content-between align-items-center">
						<div>
							<strong class="text-success">Miễn phí</strong>
							<br><small class="text-muted">${response.error || 'Không thể tính phí GHN'}</small>
						</div>
						<i class="fas fa-gift text-success"></i>
					</div>
				`;
			}
			
			// Update order summary with new shipping fee
			if (cartData) {
				displayOrderSummary(cartData);
			}
		})
		.catch(error => {
			console.error('Error calculating shipping fee:', error);
			// Fallback to free shipping
			shippingFee = 0;
			shippingCalculated = true;
			
			shippingContent.innerHTML = `
				<div class="d-flex justify-content-between align-items-center">
					<div>
						<strong class="text-success">Miễn phí</strong>
						<br><small class="text-muted">Không thể tính phí vận chuyển</small>
					</div>
					<i class="fas fa-exclamation-triangle text-warning"></i>
				</div>
			`;
			
			// Update order summary
			if (cartData) {
				displayOrderSummary(cartData);
			}
		});
	}

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
			let shipping = shippingFee || 0;
			let total = subtotal + shipping;
			
			html = '<div class="d-flex justify-content-between mb-2">'
				+ '<span>Tạm tính:</span>'
				+ '<span>' + formatCurrency(subtotal) + '</span>'
				+ '</div>'
				+ '<div class="d-flex justify-content-between mb-2">'
				+ '<span>Phí vận chuyển:</span>';
			
			if (shipping > 0) {
				html += '<span class="text-primary">' + formatCurrency(shipping) + '</span>';
			} else {
				html += '<span class="text-success">Miễn phí</span>';
			}
			
			html += '</div>'
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
		let addressSelect = document.getElementById('addressSelect');
		let hasAddress = addressSelect && addressSelect.value !== '';
		let btn = document.getElementById('place-order-btn');
		if (btn) {
			btn.disabled = !(hasItems && hasPaymentMethod && hasAddress);
		}
	}

	function placeOrder() {
		if (!selectedPaymentMethod) {
			alert('Vui lòng chọn phương thức thanh toán');
			return;
		}
		
		const addressSelect = document.getElementById('addressSelect');
		if (!addressSelect) {
			alert('Vui lòng thêm địa chỉ giao hàng trước khi đặt hàng');
			window.location.href = '/account/profile';
			return;
		}
		
		if (!addressSelect.value) {
			alert('Vui lòng chọn địa chỉ giao hàng');
			return;
		}
		
		const btn = document.getElementById('place-order-btn');
		btn.disabled = true;
		btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
		const data = {
			orderRequest: {
				addressId: parseInt(addressSelect.value),
				notes: document.getElementById('orderNotes').value.trim(),
				paymentMethod: selectedPaymentMethod,
				serviceTypeId: 2 // GHN default service type
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
