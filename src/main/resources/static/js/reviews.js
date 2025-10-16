/**
 * Review System JavaScript
 * Handles review modal, star rating, and review display
 */

// Global variables
let currentReviewData = {
    productId: null,
    orderId: null,
    productName: '',
    rating: 0,
    isEdit: false,
    reviewId: null
};

let reviewsCurrentPage = 0;
let reviewsHasMore = true;

/**
 * Initialize review system
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeStarRating();
    initializeCommentCounter();
    setupModalEventListeners();
});

/**
 * Open review modal
 */
function openReviewModal(productId, orderId, productName, existingReview = null) {
    console.log('Opening review modal for product:', productId, 'order:', orderId);
    
    // Set current review data
    currentReviewData = {
        productId: productId,
        orderId: orderId,
        productName: productName,
        rating: 0,
        isEdit: !!existingReview,
        reviewId: existingReview?.id || null
    };
    
    // Update modal content
    document.getElementById('reviewProductName').textContent = productName;
    
    // If editing, populate existing data
    if (existingReview) {
        setStarRating(existingReview.rating);
        document.getElementById('reviewComment').value = existingReview.comment || '';
        document.getElementById('submitReviewText').textContent = 'Cập nhật đánh giá';
        updateCommentCounter();
    } else {
        // Reset for new review
        resetReviewModal();
    }
    
    // Show modal with animation
    const modal = document.getElementById('reviewModal');
    const modalContent = document.getElementById('reviewModalContent');
    
    modal.classList.remove('hidden');
    modal.classList.add('review-modal-show');
    
    // Trigger animation
    setTimeout(() => {
        modalContent.style.transform = 'scale(1)';
        modalContent.style.opacity = '1';
    }, 10);
    
    // Focus on first star for accessibility
    document.querySelector('.star-btn').focus();
}

/**
 * Close review modal
 */
function closeReviewModal() {
    const modal = document.getElementById('reviewModal');
    const modalContent = document.getElementById('reviewModalContent');
    
    // Animate out
    modalContent.style.transform = 'scale(0.95)';
    modalContent.style.opacity = '0';
    
    setTimeout(() => {
        modal.classList.add('hidden');
        modal.classList.remove('review-modal-show');
        resetReviewModal();
    }, 200);
}

/**
 * Reset modal to initial state
 */
function resetReviewModal() {
    currentReviewData.rating = 0;
    currentReviewData.isEdit = false;
    currentReviewData.reviewId = null;
    
    // Reset stars
    document.querySelectorAll('.star-btn').forEach(star => {
        star.classList.remove('active');
    });
    
    // Reset form
    document.getElementById('reviewComment').value = '';
    document.getElementById('reviewComment').classList.remove('comment-warning', 'comment-danger');
    document.getElementById('commentCounter').textContent = '0/1000';
    document.getElementById('commentCounter').classList.remove('warning', 'danger');
    document.getElementById('ratingText').textContent = 'Chọn số sao để đánh giá';
    document.getElementById('ratingText').className = 'text-sm text-gray-500';
    document.getElementById('submitReviewText').textContent = 'Gửi đánh giá';
    
    // Hide error
    hideReviewError();
    
    // Reset submit button
    const submitBtn = document.getElementById('submitReviewBtn');
    submitBtn.disabled = false;
    submitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
}

/**
 * Initialize star rating functionality
 */
function initializeStarRating() {
    const stars = document.querySelectorAll('.star-btn');
    
    stars.forEach((star, index) => {
        // Click handler
        star.addEventListener('click', () => {
            const rating = parseInt(star.dataset.rating);
            setStarRating(rating);
            currentReviewData.rating = rating;
        });
        
        // Hover effects
        star.addEventListener('mouseenter', () => {
            const rating = parseInt(star.dataset.rating);
            highlightStars(rating);
        });
        
        star.addEventListener('mouseleave', () => {
            highlightStars(currentReviewData.rating);
        });
        
        // Keyboard support
        star.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                star.click();
            }
        });
    });
}

/**
 * Set star rating
 */
function setStarRating(rating) {
    currentReviewData.rating = rating;
    highlightStars(rating);
    updateRatingText(rating);
}

/**
 * Highlight stars up to given rating
 */
function highlightStars(rating) {
    const stars = document.querySelectorAll('.star-btn');
    
    stars.forEach((star, index) => {
        const starRating = parseInt(star.dataset.rating);
        if (starRating <= rating) {
            star.classList.add('active');
        } else {
            star.classList.remove('active');
        }
    });
}

/**
 * Update rating text based on selected stars
 */
function updateRatingText(rating) {
    const ratingText = document.getElementById('ratingText');
    const messages = {
        1: { text: 'Rất không hài lòng', class: 'rating-poor' },
        2: { text: 'Không hài lòng', class: 'rating-poor' },
        3: { text: 'Bình thường', class: 'rating-average' },
        4: { text: 'Hài lòng', class: 'rating-good' },
        5: { text: 'Rất hài lòng', class: 'rating-excellent' }
    };
    
    if (rating > 0 && messages[rating]) {
        ratingText.textContent = messages[rating].text;
        ratingText.className = `text-sm font-medium ${messages[rating].class}`;
    } else {
        ratingText.textContent = 'Chọn số sao để đánh giá';
        ratingText.className = 'text-sm text-gray-500';
    }
}

/**
 * Initialize comment counter
 */
function initializeCommentCounter() {
    const commentTextarea = document.getElementById('reviewComment');
    if (commentTextarea) {
        commentTextarea.addEventListener('input', updateCommentCounter);
    }
}

/**
 * Update comment character counter
 */
function updateCommentCounter() {
    const commentTextarea = document.getElementById('reviewComment');
    const counter = document.getElementById('commentCounter');
    const length = commentTextarea.value.length;
    const maxLength = 1000;
    
    counter.textContent = `${length}/${maxLength}`;
    
    // Update styling based on character count
    counter.classList.remove('warning', 'danger');
    commentTextarea.classList.remove('comment-warning', 'comment-danger');
    
    if (length > maxLength * 0.9) {
        counter.classList.add('danger');
        commentTextarea.classList.add('comment-danger');
    } else if (length > maxLength * 0.8) {
        counter.classList.add('warning');
        commentTextarea.classList.add('comment-warning');
    }
}

/**
 * Setup modal event listeners
 */
function setupModalEventListeners() {
    // Close modal when clicking outside
    document.getElementById('reviewModal').addEventListener('click', (e) => {
        if (e.target.id === 'reviewModal') {
            closeReviewModal();
        }
    });
    
    // Close modal with Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !document.getElementById('reviewModal').classList.contains('hidden')) {
            closeReviewModal();
        }
    });
}

/**
 * Submit review
 */
async function submitReview() {
    console.log('Submitting review...', currentReviewData);
    
    // Validate rating
    if (currentReviewData.rating === 0) {
        showReviewError('Vui lòng chọn số sao để đánh giá');
        return;
    }
    
    // Get comment
    const comment = document.getElementById('reviewComment').value.trim();
    
    // Validate comment length
    if (comment.length > 1000) {
        showReviewError('Bình luận không được quá 1000 ký tự');
        return;
    }
    
    // Show loading state
    const submitBtn = document.getElementById('submitReviewBtn');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<div class="review-loading"><div class="review-loading-spinner"></div><span>Đang gửi...</span></div>';
    
    try {
        const url = currentReviewData.isEdit 
            ? `${window.location.origin}/api/reviews/${currentReviewData.reviewId}`
            : `${window.location.origin}/api/reviews`;
        
        const method = currentReviewData.isEdit ? 'PUT' : 'POST';
        
        const requestBody = currentReviewData.isEdit 
            ? {
                rating: currentReviewData.rating,
                comment: comment
            }
            : {
                productId: currentReviewData.productId,
                orderId: currentReviewData.orderId,
                rating: currentReviewData.rating,
                comment: comment
            };
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify(requestBody)
        });
        
        const data = await response.json();
        
        if (data.success && data.data) {
            // Success
            showToast(currentReviewData.isEdit ? 'Cập nhật đánh giá thành công!' : 'Gửi đánh giá thành công!', 'success');
            closeReviewModal();
            
            // Refresh reviews if on product page
            if (typeof window.reviewSystem !== 'undefined' && typeof window.reviewSystem.loadProductReviews === 'function') {
                window.reviewSystem.loadProductReviews(currentReviewData.productId);
            }
            
            // Update review button state
            updateReviewButtonState();
            
        } else {
            showReviewError(data.message || 'Có lỗi xảy ra khi gửi đánh giá');
        }
        
    } catch (error) {
        console.error('Error submitting review:', error);
        showReviewError('Có lỗi xảy ra. Vui lòng thử lại sau.');
    } finally {
        // Reset button state
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

/**
 * Show review error
 */
function showReviewError(message) {
    const errorDiv = document.getElementById('reviewError');
    const errorText = document.getElementById('reviewErrorText');
    
    errorText.textContent = message;
    errorDiv.classList.remove('hidden');
    
    // Auto hide after 5 seconds
    setTimeout(() => {
        hideReviewError();
    }, 5000);
}

/**
 * Hide review error
 */
function hideReviewError() {
    const errorDiv = document.getElementById('reviewError');
    errorDiv.classList.add('hidden');
}

/**
 * Load product reviews
 */
async function loadProductReviews(productId, page = 0, append = false) {
    console.log('Loading reviews for product:', productId, 'page:', page);
    
    try {
        const response = await fetch(`${window.location.origin}/api/reviews/product/${productId}?page=${page}&size=10`, {
            credentials: 'include'
        });
        
        const data = await response.json();
        
        if (data.success && data.data) {
            const reviewsPage = data.data;
            
            if (!append) {
                reviewsCurrentPage = 0;
                document.getElementById('reviewsList').innerHTML = '';
            }
            
            displayReviews(reviewsPage.content, append);
            
            // Update pagination
            reviewsHasMore = !reviewsPage.last;
            reviewsCurrentPage = reviewsPage.number;
            
            // Show/hide load more button
            const loadMoreBtn = document.getElementById('loadMoreReviews');
            if (reviewsHasMore && reviewsPage.content.length > 0) {
                loadMoreBtn.classList.remove('hidden');
            } else {
                loadMoreBtn.classList.add('hidden');
            }
            
            // Load summary
            loadReviewSummary(productId);
            
        } else {
            console.error('Failed to load reviews:', data.message);
        }
        
    } catch (error) {
        console.error('Error loading reviews:', error);
    }
}

/**
 * Load review summary
 */
async function loadReviewSummary(productId) {
    try {
        const response = await fetch(`${window.location.origin}/api/reviews/summary/${productId}`, {
            credentials: 'include'
        });
        
        const data = await response.json();
        
        if (data.success && data.data) {
            displayReviewSummary(data.data);
        }
        
    } catch (error) {
        console.error('Error loading review summary:', error);
    }
}

/**
 * Display review summary
 */
function displayReviewSummary(summary) {
    // Update average rating
    document.getElementById('averageRating').textContent = summary.formattedAverageRating || '0.0';
    document.getElementById('totalReviews').textContent = summary.totalReviews || 0;
    
    // Update average stars
    const averageStars = document.getElementById('averageStars');
    averageStars.innerHTML = generateStarsHTML(summary.averageRating || 0);
    
    // Update star distribution
    const distributionContainer = document.getElementById('starDistribution');
    distributionContainer.innerHTML = '';
    
    for (let i = 5; i >= 1; i--) {
        const count = getStarCount(summary, i);
        const percentage = summary.getStarPercentage ? summary.getStarPercentage(i) : 0;
        
        const distributionHTML = `
            <div class="flex items-center gap-3">
                <span class="text-sm text-gray-600 w-8">${i} ⭐</span>
                <div class="flex-1 star-distribution-bar">
                    <div class="star-distribution-fill" style="width: ${percentage}%"></div>
                </div>
                <span class="text-sm text-gray-500 w-8">${count}</span>
            </div>
        `;
        
        distributionContainer.innerHTML += distributionHTML;
    }
}

/**
 * Get star count from summary
 */
function getStarCount(summary, stars) {
    switch (stars) {
        case 5: return summary.fiveStarCount || 0;
        case 4: return summary.fourStarCount || 0;
        case 3: return summary.threeStarCount || 0;
        case 2: return summary.twoStarCount || 0;
        case 1: return summary.oneStarCount || 0;
        default: return 0;
    }
}

/**
 * Display reviews
 */
function displayReviews(reviews, append = false) {
    const container = document.getElementById('reviewsList');
    
    if (!append) {
        container.innerHTML = '';
    }
    
    if (reviews.length === 0 && !append) {
        container.innerHTML = `
            <div class="reviews-empty">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                </svg>
                <h3 class="text-lg font-medium text-gray-900 mb-2">Chưa có đánh giá nào</h3>
                <p class="text-gray-500">Hãy là người đầu tiên đánh giá sản phẩm này!</p>
            </div>
        `;
        return;
    }
    
    reviews.forEach(review => {
        const reviewHTML = createReviewHTML(review);
        container.innerHTML += reviewHTML;
    });
}

/**
 * Create review HTML
 */
function createReviewHTML(review) {
    const userInitial = review.userName ? review.userName.charAt(0).toUpperCase() : 'U';
    const timeAgo = formatTimeAgo(review.createdAt);
    const starsHTML = generateStarsHTML(review.rating);
    
    const editActions = review.canEdit ? `
        <div class="review-actions flex gap-2">
            <button onclick="editReview(${review.id})" class="text-xs text-blue-600 hover:text-blue-800 px-2 py-1 rounded">
                Sửa
            </button>
            <button onclick="deleteReview(${review.id})" class="text-xs text-red-600 hover:text-red-800 px-2 py-1 rounded">
                Xóa
            </button>
        </div>
    ` : '';
    
    return `
        <div class="review-item bg-white rounded-xl border border-gray-100 p-6 hover:shadow-sm transition-shadow duration-200" data-review-id="${review.id}">
            <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-gradient-to-br from-pink-400 to-purple-500 rounded-full flex items-center justify-center text-white font-semibold text-lg flex-shrink-0">
                    ${review.userAvatar ? `<img src="${review.userAvatar}" alt="${review.userName}" class="w-full h-full rounded-full object-cover">` : userInitial}
                </div>
                
                <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between mb-2">
                        <div>
                            <h4 class="font-semibold text-gray-900 text-sm">${review.userName || 'Người dùng'}</h4>
                            <div class="flex items-center gap-2 mt-1">
                                <div class="flex text-yellow-400 text-sm">
                                    ${starsHTML}
                                </div>
                                <span class="text-xs text-gray-500">${timeAgo}</span>
                            </div>
                        </div>
                        ${editActions}
                    </div>
                    
                    ${review.comment ? `<p class="text-gray-700 text-sm leading-relaxed">${escapeHtml(review.comment)}</p>` : ''}
                </div>
            </div>
        </div>
    `;
}

/**
 * Generate stars HTML
 */
function generateStarsHTML(rating) {
    let starsHTML = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            starsHTML += '<span class="star-filled">⭐</span>';
        } else {
            starsHTML += '<span class="star-empty">⭐</span>';
        }
    }
    return starsHTML;
}

/**
 * Format time ago
 */
function formatTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return 'Vừa xong';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} phút trước`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} giờ trước`;
    if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)} ngày trước`;
    if (diffInSeconds < 31536000) return `${Math.floor(diffInSeconds / 2592000)} tháng trước`;
    return `${Math.floor(diffInSeconds / 31536000)} năm trước`;
}

/**
 * Escape HTML
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Load more reviews
 */
function loadMoreReviews() {
    if (reviewsHasMore && currentReviewData.productId) {
        loadProductReviews(currentReviewData.productId, reviewsCurrentPage + 1, true);
    }
}

/**
 * Update review button state after successful review
 */
function updateReviewButtonState() {
    // This will be called from account-orders.js or other places
    // to update the review button state after successful review
    if (typeof refreshOrderReviewButtons === 'function') {
        refreshOrderReviewButtons();
    }
}

/**
 * Edit review
 */
async function editReview(reviewId) {
    // Implementation for editing existing review
    console.log('Edit review:', reviewId);
    showToast('Tính năng chỉnh sửa đánh giá đang được phát triển', 'info');
}

/**
 * Delete review
 */
async function deleteReview(reviewId) {
    if (!confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) {
        return;
    }
    
    try {
        const response = await fetch(`${window.location.origin}/api/reviews/${reviewId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('Xóa đánh giá thành công!', 'success');
            
            // Remove review from DOM
            const reviewElement = document.querySelector(`[data-review-id="${reviewId}"]`);
            if (reviewElement) {
                reviewElement.remove();
            }
            
            // Refresh reviews
            if (currentReviewData.productId && typeof window.reviewSystem !== 'undefined' && typeof window.reviewSystem.loadProductReviews === 'function') {
                window.reviewSystem.loadProductReviews(currentReviewData.productId);
            }
            
        } else {
            showToast(data.message || 'Có lỗi xảy ra khi xóa đánh giá', 'error');
        }
        
    } catch (error) {
        console.error('Error deleting review:', error);
        showToast('Có lỗi xảy ra. Vui lòng thử lại sau.', 'error');
    }
}

// Export functions for use in other scripts
window.reviewSystem = {
    openReviewModal,
    closeReviewModal,
    loadProductReviews,
    loadMoreReviews
};
