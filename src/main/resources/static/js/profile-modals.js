// Modal Management
function openEditProfileModal() {
    document.getElementById('editProfileModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closeEditProfileModal() {
    document.getElementById('editProfileModal').classList.add('hidden');
    document.body.style.overflow = 'auto';
}

function openAddressModal() {
    document.getElementById('addressModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    loadProvinces();
}

function closeAddressModal() {
    document.getElementById('addressModal').classList.add('hidden');
    document.body.style.overflow = 'auto';
    document.getElementById('addressForm').reset();
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Edit profile button
    const editProfileBtn = document.querySelector('[data-bs-target="#editProfileModal"]');
    if (editProfileBtn) {
        editProfileBtn.removeAttribute('data-bs-toggle');
        editProfileBtn.removeAttribute('data-bs-target');
        editProfileBtn.onclick = openEditProfileModal;
    }

    // Add address button
    const addAddressBtn = document.querySelector('[data-bs-target="#addressModal"]');
    if (addAddressBtn) {
        addAddressBtn.removeAttribute('data-bs-toggle');
        addAddressBtn.removeAttribute('data-bs-target');
        addAddressBtn.onclick = openAddressModal;
    }

    // Load addresses
    loadAddresses();
});

// Toast Notification
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = document.getElementById('toastIcon');
    
    toastMessage.textContent = message;
    
    if (type === 'success') {
        toastIcon.innerHTML = '<svg class="w-5 h-5 text-green-600" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm3.857-9.809a.75.75 0 0 0-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 1 0-1.06 1.061l2.5 2.5a.75.75 0 0 0 1.137-.089l4-5.5Z" clip-rule="evenodd" /></svg>';
    } else {
        toastIcon.innerHTML = '<svg class="w-5 h-5 text-red-600" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16ZM8.28 7.22a.75.75 0 0 0-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 1 0 1.06 1.06L10 11.06l1.72 1.72a.75.75 0 1 0 1.06-1.06L11.06 10l1.72-1.72a.75.75 0 0 0-1.06-1.06L10 8.94 8.28 7.22Z" clip-rule="evenodd" /></svg>';
    }
    
    toast.classList.remove('hidden');
    setTimeout(() => hideToast(), 5000);
}

function hideToast() {
    document.getElementById('toast').classList.add('hidden');
}

// Address Mode Toggle
function toggleAddressMode() {
    const mode = document.querySelector('input[name="addressMode"]:checked').value;
    const districtGroup = document.getElementById('districtGroup');
    const districtSelect = document.getElementById('district');
    
    if (mode === 'NEW') {
        districtGroup.classList.add('hidden');
        districtSelect.required = false;
        districtSelect.disabled = true;
        loadWards();
    } else {
        districtGroup.classList.remove('hidden');
        districtSelect.required = true;
        districtSelect.disabled = false;
    }
}

// Load Provinces
async function loadProvinces() {
    try {
        const response = await fetch('/api/locations/provinces');
        const data = await response.json();
        
        const provinceSelect = document.getElementById('province');
        provinceSelect.innerHTML = '<option value="">-- Chọn Tỉnh/Thành phố --</option>';
        
        if (data.success && data.data) {
            data.data.forEach(province => {
                const option = document.createElement('option');
                option.value = province.ProvinceID;
                option.textContent = province.ProvinceName;
                provinceSelect.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Error loading provinces:', error);
        showToast('Không thể tải danh sách tỉnh/thành phố', 'error');
    }
}

// Load Districts
async function loadDistricts() {
    const provinceId = document.getElementById('province').value;
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    
    districtSelect.innerHTML = '<option value="">-- Chọn Quận/Huyện --</option>';
    wardSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
    districtSelect.disabled = true;
    wardSelect.disabled = true;
    
    if (!provinceId) return;
    
    const mode = document.querySelector('input[name="addressMode"]:checked').value;
    
    if (mode === 'NEW') {
        loadWards();
        return;
    }
    
    try {
        const response = await fetch(`/api/locations/districts?province_id=${provinceId}`);
        const data = await response.json();
        
        if (data.success && data.data) {
            data.data.forEach(district => {
                const option = document.createElement('option');
                option.value = district.DistrictID;
                option.textContent = district.DistrictName;
                districtSelect.appendChild(option);
            });
            districtSelect.disabled = false;
        }
    } catch (error) {
        console.error('Error loading districts:', error);
        showToast('Không thể tải danh sách quận/huyện', 'error');
    }
}

// Load Wards
async function loadWards() {
    const mode = document.querySelector('input[name="addressMode"]:checked').value;
    const wardSelect = document.getElementById('ward');
    wardSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
    wardSelect.disabled = true;
    
    let url;
    if (mode === 'NEW') {
        const provinceId = document.getElementById('province').value;
        if (!provinceId) return;
        url = `/api/locations/wards?province_id=${provinceId}`;
    } else {
        const districtId = document.getElementById('district').value;
        if (!districtId) return;
        url = `/api/locations/wards?district_id=${districtId}`;
    }
    
    try {
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.success && data.data) {
            data.data.forEach(ward => {
                const option = document.createElement('option');
                option.value = ward.WardCode;
                option.textContent = ward.WardName;
                wardSelect.appendChild(option);
            });
            wardSelect.disabled = false;
        }
    } catch (error) {
        console.error('Error loading wards:', error);
        showToast('Không thể tải danh sách phường/xã', 'error');
    }
}

// Submit Address
async function submitAddress(event) {
    event.preventDefault();
    
    const mode = document.querySelector('input[name="addressMode"]:checked').value;
    const submitBtn = document.getElementById('submitAddressBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Đang xử lý...';
    
    const addressData = {
        addressMode: mode,
        provinceId: parseInt(document.getElementById('province').value),
        districtId: mode === 'OLD' ? parseInt(document.getElementById('district').value) : null,
        wardCode: document.getElementById('ward').value,
        addressDetail: document.getElementById('addressDetail').value.trim(),
        isDefault: document.getElementById('isDefault').checked
    };
    
    try {
        const response = await fetch('/api/addresses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(addressData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast('Thêm địa chỉ thành công!', 'success');
            closeAddressModal();
            loadAddresses();
        } else {
            showToast(result.message || 'Có lỗi xảy ra', 'error');
        }
    } catch (error) {
        console.error('Error submitting address:', error);
        showToast('Không thể thêm địa chỉ. Vui lòng thử lại.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Thêm địa chỉ';
    }
}

// Load Addresses
async function loadAddresses() {
    const addressesList = document.getElementById('addressesList');
    addressesList.innerHTML = '<div class="text-center py-8"><div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto"></div><p class="mt-3 text-sm text-gray-500">Đang tải địa chỉ...</p></div>';
    
    try {
        const response = await fetch('/api/addresses');
        const data = await response.json();
        
        if (data.success && data.data && data.data.length > 0) {
            addressesList.innerHTML = data.data.map(address => `
                <div class="border border-gray-200 rounded-lg p-4 hover:border-pink-300 transition-all mb-3 ${address.isDefault ? 'border-pink-500 bg-pink-50' : ''}">
                    <div class="flex items-start justify-between">
                        <div class="flex-1">
                            ${address.isDefault ? '<span class="inline-block px-2 py-1 bg-pink-600 text-white text-xs font-medium rounded mb-2">Mặc định</span>' : ''}
                            <p class="text-gray-900 font-medium">${address.fullAddress || address.addressDetail}</p>
                            ${address.ghnCompatible ? '<span class="inline-flex items-center mt-2 text-xs text-green-600"><svg class="w-4 h-4 mr-1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm3.857-9.809a.75.75 0 0 0-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 1 0-1.06 1.061l2.5 2.5a.75.75 0 0 0 1.137-.089l4-5.5Z" clip-rule="evenodd" /></svg>Hỗ trợ giao hàng nhanh</span>' : ''}
                        </div>
                        <div class="flex items-center gap-2 ml-4">
                            ${!address.isDefault ? `<button onclick="deleteAddress(${address.id})" class="text-red-600 hover:text-red-700 p-2"><svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.75 1A2.75 2.75 0 0 0 6 3.75v.443c-.795.077-1.584.176-2.365.298a.75.75 0 1 0 .23 1.482l.149-.022.841 10.518A2.75 2.75 0 0 0 7.596 19h4.807a2.75 2.75 0 0 0 2.742-2.53l.841-10.52.149.023a.75.75 0 0 0 .23-1.482A41.03 41.03 0 0 0 14 4.193V3.75A2.75 2.75 0 0 0 11.25 1h-2.5ZM10 4c.84 0 1.673.025 2.5.075V3.75c0-.69-.56-1.25-1.25-1.25h-2.5c-.69 0-1.25.56-1.25 1.25v.325C8.327 4.025 9.16 4 10 4ZM8.58 7.72a.75.75 0 0 0-1.5.06l.3 7.5a.75.75 0 1 0 1.5-.06l-.3-7.5Zm4.34.06a.75.75 0 1 0-1.5-.06l-.3 7.5a.75.75 0 1 0 1.5.06l.3-7.5Z" clip-rule="evenodd" /></svg></button>` : ''}
                        </div>
                    </div>
                </div>
            `).join('');
        } else {
            addressesList.innerHTML = `
                <div class="text-center py-12">
                    <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <p class="text-gray-500 mb-4">Chưa có địa chỉ giao hàng</p>
                    <button onclick="openAddressModal()" class="text-pink-600 hover:text-pink-700 font-medium">Thêm địa chỉ đầu tiên</button>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading addresses:', error);
        addressesList.innerHTML = '<div class="text-center py-8 text-red-600">Không thể tải danh sách địa chỉ</div>';
    }
}

// Delete Address
async function deleteAddress(addressId) {
    if (!confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/addresses/${addressId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast('Xóa địa chỉ thành công!', 'success');
            loadAddresses();
        } else {
            showToast(result.message || 'Không thể xóa địa chỉ', 'error');
        }
    } catch (error) {
        console.error('Error deleting address:', error);
        showToast('Không thể xóa địa chỉ. Vui lòng thử lại.', 'error');
    }
}
