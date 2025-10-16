(function() {
	let selectedPaymentMethod = null;
	let cartData = null;
	let shippingFee = 0;
	let shippingCalculated = false;
	let appliedVoucher = null;

	document.addEventListener('DOMContentLoaded', function() {
		loadCartData();
		loadPaymentMethods();
		const btn = document.getElementById('place-order-btn');
		if (btn) {
			btn.addEventListener('click', placeOrder);
		}
			const voucherBtn = document.getElementById('apply-voucher-btn');
			if (voucherBtn) {
				voucherBtn.addEventListener('click', applyVoucher);
			}
		const addressSelect = document.getElementById('addressSelect');
		console.log('Address select element:', addressSelect);
		if (addressSelect) {
			console.log('Address select options:', addressSelect.options.length);
			console.log('Selected value:', addressSelect.value);
			
			addressSelect.addEventListener('change', function() {
				console.log('Address changed to:', this.value);
				updateAddressDetails();
				calculateShippingFee();
				updatePlaceOrderButton();
			});
			// Show initial address if default is selected
			if (addressSelect.value) {
				console.log('Auto-calculating shipping for default address:', addressSelect.value);
				updateAddressDetails();
				calculateShippingFee();
			} else {
				console.log('No default address selected');
			}
		} else {
			console.log('Address select element not found!');
		}
	});

	function updateAddressDetails() {
		const select = document.getElementById('addressSelect');
		const indicator = document.getElementById('addressSelectedIndicator');
		const shippingSection = document.getElementById('shippingFeeSection');
		
		console.log('updateAddressDetails called, select value:', select?.value);
		
		if (select && select.value) {
			const selectedOption = select.options[select.selectedIndex];
			console.log('Selected option:', selectedOption?.text);
			
			// Show green indicator
			if (indicator) {
				indicator.style.display = 'flex';
			}
			
			// Show shipping section
			if (shippingSection) {
				shippingSection.style.display = 'block';
			}
		} else {
			// Hide green indicator
			if (indicator) {
				indicator.style.display = 'none';
			}
			
			// Hide shipping section
			if (shippingSection) {
				shippingSection.style.display = 'none';
			}
			shippingFee = 0;
			shippingCalculated = false;
		}
	}

	function calculateShippingFee() {
		const select = document.getElementById('addressSelect');
		const shippingContent = document.getElementById('shippingFeeContent');
		
		console.log('calculateShippingFee called, select:', select, 'value:', select?.value);
		
		if (!select || !select.value) {
			console.log('No address selected, skipping shipping calculation');
			return;
		}
		
		// Show loading (Tailwind) if container exists
		if (shippingContent) {
			shippingContent.innerHTML = `
				<div class="flex items-center text-sm text-gray-600">
					<svg class="animate-spin h-4 w-4 text-primary" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path></svg>
					<span class="ml-2">Đang tính phí vận chuyển...</span>
				</div>
			`;
		}
		
		// Call shipping fee API
		const csrfToken = getCsrfToken();
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
		
		const requestBody = {
			addressId: parseInt(select.value),
			serviceTypeId: 2 // Default GHN service type
		};
		
		console.log('Making shipping API request:', requestBody);
		console.log('CSRF token:', csrfToken, 'Header:', csrfHeader);
		
		fetch(window.location.origin + '/api/shipping/fee', {
			method: 'POST',
			headers: { 
				'Content-Type': 'application/json',
				'X-Requested-With': 'XMLHttpRequest',
				[csrfHeader]: csrfToken
			},
			credentials: 'include',
			body: JSON.stringify(requestBody)
		})
		.then(r => {
			console.log('Raw response status:', r.status, 'ok:', r.ok);
			return r.json();
		})
		.then(response => {
			console.log('Shipping fee API response:', response); // Debug log
			if (response.data && response.data.success) {
				shippingFee = response.data.shippingFee || 0;
				shippingCalculated = true;
				
				if (shippingContent) {
					shippingContent.innerHTML = `
						<div class="flex items-center justify-between">
							<div>
								<strong class="text-green-600">${formatCurrency(shippingFee)}</strong>
								<br><small class="text-gray-500">Giao hàng nhanh (GHN)</small>
							</div>
							<svg class="h-5 w-5 text-green-600" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-7.25 9a.75.75 0 01-1.127.05l-3.25-3.5a.75.75 0 111.102-1.02l2.66 2.868 6.71-8.34a.75.75 0 011.012-.11z" clip-rule="evenodd"/></svg>
						</div>
					`;
				}
			} else {
				// Fallback to free shipping
				shippingFee = 0;
				shippingCalculated = true;
				
				if (shippingContent) {
					shippingContent.innerHTML = `
						<div class="flex items-center justify-between">
							<div>
								<strong class="text-green-600">Miễn phí</strong>
								<br><small class="text-gray-500">${response.error || 'Không thể tính phí GHN'}</small>
							</div>
							<span class="text-green-600">🎁</span>
						</div>
					`;
				}
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
			
			if (shippingContent) {
				shippingContent.innerHTML = `
					<div class="flex items-center justify-between">
						<div>
							<strong class="text-green-600">Miễn phí</strong>
							<br><small class="text-gray-500">Lỗi: ${error.message || 'Không thể tính phí vận chuyển'}</small>
						</div>
						<span class="text-amber-600">⚠️</span>
					</div>
				`;
			}
			
			// Update order summary
			if (cartData) {
				displayOrderSummary(cartData);
			}
		});
	}

	function loadCartData() {
		fetch(window.location.origin + '/api/cart/get', { credentials: 'include' })
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
					document.getElementById('cart-items').innerHTML = '<div class="rounded-lg bg-amber-50 text-amber-800 px-4 py-3">Giỏ hàng trống</div>';
					document.getElementById('order-summary').innerHTML = '<div class="rounded-lg bg-amber-50 text-amber-800 px-4 py-3">Không có sản phẩm nào trong giỏ hàng</div>';
				}
			})
			.catch(() => {
				document.getElementById('cart-items').innerHTML = '<div class="rounded-lg bg-red-50 text-red-700 px-4 py-3">Có lỗi xảy ra khi tải giỏ hàng</div>';
				document.getElementById('order-summary').innerHTML = '<div class="rounded-lg bg-red-50 text-red-700 px-4 py-3">Có lỗi xảy ra khi tải thông tin</div>';
			});
	}

	function loadPaymentMethods() {
		fetch(window.location.origin + '/api/payment/methods', { credentials: 'include' })
			.then(r => r.json())
			.then(response => {
				if (response.data) displayPaymentMethods(response.data);
			})
			.catch(() => {
				document.getElementById('payment-methods').innerHTML = '<div class="rounded-lg bg-red-50 text-red-700 px-4 py-3">Có lỗi xảy ra khi tải phương thức thanh toán</div>';
			});
	}

	function displayCartItems(cart) {
		let html = '';
		if (cart.items && cart.items.length > 0) {
			cart.items.forEach(function(item) {
				const imageUrl = (item.productImageUrl || item.imageUrl || (item.product && (item.product.imageUrl || item.product.thumbnail)) || item.productImage || '/images/products/default.jpg');
				const name = (item.productName || (item.product && item.product.name) || item.name || '');
				const unitPrice = getFirstNumber(
					item.unitPrice,
					item.price,
					item.productPrice,
					item.salePrice,
					item.finalPrice,
					item?.product?.price,
					item?.product?.salePrice
				);
				const quantity = getFirstNumber(item.quantity, item.qty, item.count, 1);
				const lineTotal = getFirstNumber(
					item.totalPrice,
					item.lineTotal,
					item.amount,
					item.totalAmount,
					unitPrice * quantity
				);
				html += '<div class="flex items-center gap-4 mb-3 p-4 rounded-2xl border-2 border-gray-400 bg-white shadow-sm hover:border-primary transition-all duration-200">'
					+ '<img src="' + imageUrl + '" class="rounded-xl flex-shrink-0" width="96" height="96" style="object-fit: cover;">'
					+ '<div class="flex-1 min-w-0">'
					+ '<div class="text-sm font-semibold text-gray-900 truncate">' + escapeHtml(name) + '</div>'
					+ '<div class="text-xs text-gray-500">' + formatCurrency(unitPrice) + ' × ' + quantity + '</div>'
					+ '</div>'
					+ '<div class="text-right">'
					+ '<strong class="text-gray-900 whitespace-nowrap">' + formatCurrency(lineTotal) + '</strong>'
					+ '</div>'
					+ '</div>';
			});
		} else {
			html = '<div class="rounded-lg bg-amber-50 text-amber-800 px-4 py-3">Giỏ hàng trống</div>';
		}
		document.getElementById('cart-items').innerHTML = html;
	}

	function displayOrderSummary(cart) {
		let html = '';
		if (cart.items && cart.items.length > 0) {
			let subtotal = getFirstNumber(
				cart.totalAmount,
				cart.totalPrice,
				cart.subtotal,
				(function sumItems() {
					try {
						return (cart.items || []).reduce(function(acc, it){
							const unitPrice = getFirstNumber(it.unitPrice, it.price, it.productPrice, it.salePrice, it.finalPrice, it?.product?.price, it?.product?.salePrice);
							const quantity = getFirstNumber(it.quantity, it.qty, it.count, 1);
							const lineTotal = getFirstNumber(it.totalPrice, it.lineTotal, it.amount, it.totalAmount, unitPrice * quantity);
							return acc + lineTotal;
						}, 0);
					} catch(_) { return 0; }
				})()
			);
			let discount = appliedVoucher && appliedVoucher.discount ? appliedVoucher.discount : 0;
			let shipping = shippingFee || 0;
			let total = Math.max(0, subtotal - discount) + shipping;
			
			html = '<div class="flex items-center justify-between mb-2">'
				+ '<span class="text-gray-600">Tạm tính:</span>'
				+ '<span class="font-medium">' + formatCurrency(subtotal) + '</span>'
				+ '</div>'
				+ (discount > 0 ? ('<div class="flex items-center justify-between mb-2">'
				+ '<span class="text-gray-600">Giảm giá:</span>'
				+ '<span class="font-medium text-green-600">- ' + formatCurrency(discount) + '</span>'
				+ '</div>') : '')
				+ '<div class="flex items-center justify-between mb-2">'
				+ '<span class="text-gray-600">Phí vận chuyển:</span>';
			
			if (shipping > 0) {
				html += '<span class="text-primary font-medium">' + formatCurrency(shipping) + '</span>';
			} else {
				html += '<span class="text-green-600 font-medium">Miễn phí</span>';
			}
			
			html += '</div>'
				+ '<div class="my-3 h-px bg-gray-100"></div>'
				+ '<div class="flex items-center justify-between mb-1">'
				+ '<span class="font-semibold text-gray-800">Tổng cộng:</span>'
				+ '<span class="font-semibold text-primary">' + formatCurrency(total) + '</span>'
				+ '</div>';
		} else {
			html = '<div class="rounded-lg bg-amber-50 text-amber-800 px-4 py-3">Không có sản phẩm nào trong giỏ hàng</div>';
		}
		document.getElementById('order-summary').innerHTML = html;
	}

	// Helpers for robust numeric parsing and safe HTML
	function toNumber(val) {
		if (val == null) return NaN;
		if (typeof val === 'number') return val;
		if (typeof val === 'string') {
			const cleaned = val.replace(/[^0-9.-]/g, '');
			const parsed = parseFloat(cleaned);
			return isNaN(parsed) ? NaN : parsed;
		}
		return NaN;
	}

	function getFirstNumber() {
		for (let i = 0; i < arguments.length; i++) {
			const n = toNumber(arguments[i]);
			if (!isNaN(n)) return n;
		}
		return 0;
	}

	function escapeHtml(str) {
		try {
			return String(str)
				.replace(/&/g, '&amp;')
				.replace(/</g, '&lt;')
				.replace(/>/g, '&gt;')
				.replace(/"/g, '&quot;')
				.replace(/'/g, '&#039;');
		} catch (_) { return str; }
	}

	function displayPaymentMethods(methods) {
		let html = '';
		Object.keys(methods).forEach(function(key) {
			const method = methods[key];
			const isAvailable = method.available;
			const disabledAttr = isAvailable ? '' : 'disabled';
			const containerClasses = 'group relative rounded-2xl border-2 border-gray-400 p-4 transition-all duration-200 cursor-pointer bg-white shadow-sm ' + (isAvailable ? 'hover:border-primary' : 'opacity-60 cursor-not-allowed');
			html += '<label for="payment-' + key + '" class="' + containerClasses + '">'
				+ '<input class="sr-only" type="radio" name="paymentMethod" id="payment-' + key + '" value="' + key + '" ' + disabledAttr + '>'
				+ '<div class="flex items-start gap-3">'
				+ '<div class="flex-1">'
				+ '<div class="text-base font-semibold text-gray-900">' + method.displayName + '</div>'
				+ '<div class="text-xs text-gray-500">' + method.englishName + '</div>'
				+ '</div>'
				+ '<div class="text-right">'
				+ '<span class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ' + (isAvailable ? 'bg-green-50 text-green-700' : 'bg-amber-50 text-amber-700') + '">' + method.statusMessage + '</span>'
				+ '</div>'
				+ '</div>'
				+ '<div class="mt-3 hidden group-hover:flex items-center justify-between text-xs text-gray-500">'
				+ '<span>Mô tả ngắn</span>'
				+ '<span class="text-primary">Chọn ➜</span>'
				+ '</div>'
				+ '</label>';
		});
		document.getElementById('payment-methods').innerHTML = html;
		Array.from(document.querySelectorAll('input[name="paymentMethod"]')).forEach(function(r){
			r.addEventListener('change', function(){
				selectedPaymentMethod = this.value;
				updatePlaceOrderButton();
				// Highlight selected container
				document.querySelectorAll('#payment-methods label').forEach(function(lbl){
					lbl.classList.remove('ring-2','ring-primary','bg-primary/5','border-primary');
				});
				const selectedLabel = this.closest('label');
				if (selectedLabel) {
					selectedLabel.classList.add('ring-2','ring-primary','bg-primary/5','border-primary');
				}
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
		btn.innerHTML = '<svg class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path></svg>Đang xử lý...';
		const data = {
			orderRequest: {
				addressId: parseInt(addressSelect.value),
				notes: document.getElementById('orderNotes').value.trim(),
				paymentMethod: selectedPaymentMethod,
				serviceTypeId: 2 // GHN default service type
			},
			paymentMethod: selectedPaymentMethod
		};
		if (appliedVoucher && appliedVoucher.code) {
			data.orderRequest.voucherCode = appliedVoucher.code;
		}
		fetch(window.location.origin + '/api/orders/create-with-payment', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			credentials: 'include',
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
					// Update cart count to 0 since cart is cleared after order
					if (typeof updateCartCount === 'function') {
						updateCartCount(0);
					}
                    window.location.href = '/account/orders';
					return;
				}
			}
			alert((response.data && response.data.payment && response.data.payment.message) || 'Có lỗi xảy ra');
		})
		.catch(() => alert('Có lỗi xảy ra khi đặt hàng'))
		.finally(() => {
			btn.disabled = false;
			btn.innerHTML = 'Đặt hàng';
		});
	}

	function applyVoucher() {
		const input = document.getElementById('voucher-input');
		const message = document.getElementById('voucher-message');
		const btn = document.getElementById('apply-voucher-btn');
		const code = (input.value || '').trim().toUpperCase();
		if (!code) {
			message.textContent = 'Vui lòng nhập mã voucher';
			message.className = 'mt-2 text-sm text-amber-700';
			return;
		}
		if (!cartData || !cartData.totalAmount) {
			message.textContent = 'Không thể áp dụng voucher khi giỏ hàng trống';
			message.className = 'mt-2 text-sm text-amber-700';
			return;
		}
		btn.disabled = true;
		btn.innerHTML = '<svg class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path></svg>Đang áp dụng...';

		// Include CSRF header for Spring Security to avoid 403
		const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
		fetch(window.location.origin + '/api/vouchers/apply', {
			method: 'POST',
			headers: (function(){
				const h = { 'Content-Type': 'application/json' };
				if (csrfToken && csrfHeader) { h[csrfHeader] = csrfToken; }
				return h;
			})(),
			credentials: 'include',
			body: JSON.stringify({ code, orderAmount: cartData.totalAmount })
		})
		.then(r => r.json())
		.then(res => {
			if (res && res.success) {
				appliedVoucher = { code, discount: res.discount || 0 };
				message.textContent = 'Áp dụng voucher thành công: ' + code + ' (-' + formatCurrency(appliedVoucher.discount) + ')';
				message.className = 'mt-2 text-sm text-green-600';
				if (cartData) displayOrderSummary(cartData);
			} else {
				appliedVoucher = null;
				message.textContent = (res && res.message) || 'Voucher không khả dụng';
				message.className = 'mt-2 text-sm text-red-600';
				if (cartData) displayOrderSummary(cartData);
			}
		})
		.catch(() => {
			appliedVoucher = null;
			message.textContent = 'Có lỗi khi áp dụng voucher';
			message.className = 'mt-2 text-sm text-red-600';
			if (cartData) displayOrderSummary(cartData);
		})
		.finally(() => {
			btn.disabled = false;
			btn.innerHTML = 'Áp dụng';
		});
	}

	function formatCurrency(amount) {
		try {
			return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
		} catch (_) { return amount; }
	}
})();

