/**
 * ========================================
 * ORDERS DETAIL PAGE JAVASCRIPT
 * Handle product review functionality
 * ========================================
 */

let currentProductReviewData = {
    orderItemId: null,
    productName: '',
    productImage: ''
};

let selectedProductRating = 0;

// Open product review modal
function openProductReviewModal(orderItemId, productName, productImage) {
    currentProductReviewData = {
        orderItemId: orderItemId,
        productName: productName,
        productImage: productImage
    };
    
    // Set product info
    document.getElementById('productReviewName').textContent = productName;
    document.getElementById('productReviewImage').src = productImage;
    document.getElementById('productReviewImage').alt = productName;
    
    // Reset form
    selectedProductRating = 0;
    document.getElementById('productReviewComment').value = '';
    updateProductRatingDisplay();
    
    // Show modal
    document.getElementById('productReviewModal').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

// Close product review modal
function closeProductReviewModal() {
    document.getElementById('productReviewModal').classList.add('hidden');
    document.body.style.overflow = 'auto';
    
    // Reset form
    selectedProductRating = 0;
    document.getElementById('productReviewComment').value = '';
    updateProductRatingDisplay();
}

// Select product rating
function selectProductRating(rating) {
    selectedProductRating = rating;
    updateProductRatingDisplay();
    
    // Enable submit button
    document.getElementById('submitProductReviewBtn').disabled = false;
}

// Update product rating display
function updateProductRatingDisplay() {
    const stars = document.querySelectorAll('#productRatingStars button');
    const ratingText = document.getElementById('productRatingText');
    
    stars.forEach((star, index) => {
        const rating = index + 1;
        if (rating <= selectedProductRating) {
            star.classList.remove('text-gray-300');
            star.classList.add('text-yellow-400');
        } else {
            star.classList.remove('text-yellow-400');
            star.classList.add('text-gray-300');
        }
    });
    
    // Update rating text
    const ratingTexts = {
        0: 'Chọn số sao để đánh giá',
        1: 'Rất tệ',
        2: 'Tệ',
        3: 'Bình thường',
        4: 'Tốt',
        5: 'Rất tốt'
    };
    
    ratingText.textContent = ratingTexts[selectedProductRating] || 'Chọn số sao để đánh giá';
}

// Submit product review
async function submitProductReview() {
    if (selectedProductRating === 0) {
        showToast('Vui lòng chọn số sao đánh giá', 'error');
        return;
    }
    
    const comment = document.getElementById('productReviewComment').value.trim();
    
    try {
        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        
        // Submit review for single product
        const response = await fetch(`/api/reviews/order-item/${currentProductReviewData.orderItemId}`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                rating: selectedProductRating,
                comment: comment
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data && data.success) {
            showToast('Đánh giá sản phẩm thành công!', 'success');
            
            // Close modal
            closeProductReviewModal();
            
            // Reload page to update UI
            window.location.reload();
        } else {
            showToast(data.message || 'Có lỗi xảy ra khi gửi đánh giá', 'error');
        }
    } catch (error) {
        console.error('Error submitting product review:', error);
        showToast('Có lỗi xảy ra khi gửi đánh giá', 'error');
    }
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Product rating stars
    document.querySelectorAll('#productRatingStars button').forEach(button => {
        button.addEventListener('click', function() {
            const rating = parseInt(this.getAttribute('data-rating'));
            selectProductRating(rating);
        });
    });
    
    // Close modal buttons
    document.getElementById('closeProductModalBtn').addEventListener('click', closeProductReviewModal);
    document.getElementById('productModalBackdrop').addEventListener('click', closeProductReviewModal);
    
    // Submit button
    document.getElementById('submitProductReviewBtn').addEventListener('click', submitProductReview);
    
    // Close on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeProductReviewModal();
        }
    });
});
