// Staff Dashboard JavaScript
// Handles check-in/check-out, real-time updates, and WebSocket connections

let stompClient = null;
let currentShift = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    initializeCheckInOutButton();
    connectWebSocket();
    startAutoRefresh();
});

/**
 * Initialize check-in/check-out button
 */
async function initializeCheckInOutButton() {
    try {
        const response = await fetch('/api/staff/shift/current', {
            credentials: 'same-origin'
        });
        const data = await response.json();
        
        if (data.success && data.data) {
            currentShift = data.data;
            renderCheckInOutButton();
        } else {
            renderCheckInOutButton();
        }
    } catch (error) {
        console.error('Error loading shift status:', error);
        renderCheckInOutButton();
    }
}

/**
 * Render check-in/check-out button based on current state
 */
function renderCheckInOutButton() {
    const container = document.getElementById('checkInOutButton');
    if (!container) return;
    
    if (currentShift && currentShift.activeShift) {
        // Currently checked in - show check-out button
        container.innerHTML = `
            <button onclick="performCheckOut()" 
                    class="check-out-button text-white px-4 py-2 rounded-lg font-medium flex items-center space-x-2 transition-all">
                <i class="fas fa-sign-out-alt"></i>
                <span>Check-out</span>
            </button>
        `;
    } else {
        // Not checked in - show check-in button
        container.innerHTML = `
            <button onclick="performCheckIn()" 
                    class="check-in-button text-white px-4 py-2 rounded-lg font-medium flex items-center space-x-2 transition-all">
                <i class="fas fa-sign-in-alt"></i>
                <span>Check-in</span>
            </button>
        `;
    }
}

/**
 * Perform check-in
 */
async function performCheckIn() {
    try {
        const button = event.target.closest('button');
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        
        const response = await fetch('/api/staff/check-in', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        });
        
        const data = await response.json();
        
        if (data.success) {
            currentShift = data.data;
            renderCheckInOutButton();
            showNotification('Check-in thành công!', 'success');
            
            // Refresh page stats
            if (window.location.pathname.includes('/staff/dashboard')) {
                setTimeout(() => location.reload(), 1000);
            }
        } else {
            showNotification(data.message || 'Check-in thất bại', 'error');
            button.disabled = false;
            renderCheckInOutButton();
        }
    } catch (error) {
        console.error('Error checking in:', error);
        showNotification('Đã xảy ra lỗi khi check-in', 'error');
        renderCheckInOutButton();
    }
}

/**
 * Perform check-out
 */
async function performCheckOut() {
    if (!confirm('Bạn có chắc muốn check-out?')) {
        return;
    }
    
    try {
        const button = event.target.closest('button');
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        
        const response = await fetch('/api/staff/check-out', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        });
        
        const data = await response.json();
        
        if (data.success) {
            currentShift = null;
            renderCheckInOutButton();
            showNotification(`Check-out thành công! Bạn đã làm việc ${data.data.hoursWorked} giờ hôm nay.`, 'success');
            
            // Refresh page stats
            if (window.location.pathname.includes('/staff/dashboard')) {
                setTimeout(() => location.reload(), 1000);
            }
        } else {
            showNotification(data.message || 'Check-out thất bại', 'error');
            button.disabled = false;
            renderCheckInOutButton();
        }
    } catch (error) {
        console.error('Error checking out:', error);
        showNotification('Đã xảy ra lỗi khi check-out', 'error');
        renderCheckInOutButton();
    }
}

/**
 * Connect to WebSocket for real-time updates
 */
function connectWebSocket() {
    if (!STAFF_ID) {
        console.log('Staff ID not available, skipping WebSocket connection');
        return;
    }
    
    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        // Disable debug logging in production
        stompClient.debug = null;
        
        stompClient.connect({}, function(frame) {
            console.log('WebSocket connected for staff dashboard');
            
            // Subscribe to personal notifications
            stompClient.subscribe('/topic/messages/' + STAFF_ID, function(message) {
                const notification = JSON.parse(message.body);
                handleWebSocketNotification(notification);
            });
            
            // Subscribe to staff broadcasts
            stompClient.subscribe('/topic/staff/notifications', function(message) {
                const notification = JSON.parse(message.body);
                handleWebSocketNotification(notification);
            });
            
            // Subscribe to new conversations
            stompClient.subscribe('/topic/staff/conversations', function(message) {
                const data = JSON.parse(message.body);
                handleNewConversation(data);
            });
        }, function(error) {
            console.error('WebSocket connection error:', error);
            // Attempt to reconnect after 5 seconds
            setTimeout(connectWebSocket, 5000);
        });
    } catch (error) {
        console.error('Error setting up WebSocket:', error);
    }
}

/**
 * Handle WebSocket notifications
 */
function handleWebSocketNotification(notification) {
    console.log('Received notification:', notification);
    
    // Show notification dot
    const notificationDot = document.getElementById('notificationDot');
    if (notificationDot) {
        notificationDot.style.display = 'block';
    }
    
    // Update unread badge if it's a new message
    if (notification.type === 'new_message') {
        updateUnreadBadge();
    }
    
    // Show browser notification if supported
    if (Notification.permission === 'granted') {
        new Notification('StarShop Staff', {
            body: notification.message,
            icon: '/images/logo.png',
            badge: '/images/badge.png'
        });
    }
    
    // Play sound notification
    playNotificationSound();
}

/**
 * Handle new conversation notification
 */
function handleNewConversation(data) {
    console.log('New conversation:', data);
    
    // Show notification
    showNotification('Có cuộc hội thoại mới đang chờ xử lý!', 'info');
    
    // Refresh unassigned queue if on dashboard
    if (window.location.pathname.includes('/staff/dashboard')) {
        setTimeout(() => location.reload(), 2000);
    }
}

/**
 * Update unread message badge
 */
async function updateUnreadBadge() {
    try {
        const response = await fetch('/api/staff/dashboard', {
            credentials: 'same-origin'
        });
        const data = await response.json();
        
        if (data.success && data.data) {
            const badge = document.getElementById('unreadBadge');
            if (badge) {
                const unreadCount = data.data.unreadMessages || 0;
                if (unreadCount > 0) {
                    badge.textContent = unreadCount;
                    badge.style.display = 'inline-flex';
                    badge.classList.add('pulse-badge');
                } else {
                    badge.style.display = 'none';
                }
            }
        }
    } catch (error) {
        console.error('Error updating unread badge:', error);
    }
}

/**
 * Start auto-refresh for dashboard stats
 */
function startAutoRefresh() {
    // Refresh stats every 30 seconds
    setInterval(async () => {
        if (window.location.pathname.includes('/staff/dashboard')) {
            updateUnreadBadge();
        }
    }, 30000);
}

/**
 * Show notification message
 */
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `fixed top-4 right-4 z-50 px-6 py-4 rounded-lg shadow-lg text-white transform transition-all duration-300 ${
        type === 'success' ? 'bg-green-500' :
        type === 'error' ? 'bg-red-500' :
        type === 'warning' ? 'bg-yellow-500' :
        'bg-blue-500'
    }`;
    
    notification.innerHTML = `
        <div class="flex items-center space-x-3">
            <i class="fas ${
                type === 'success' ? 'fa-check-circle' :
                type === 'error' ? 'fa-exclamation-circle' :
                type === 'warning' ? 'fa-exclamation-triangle' :
                'fa-info-circle'
            }"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 10);
    
    // Remove after 5 seconds
    setTimeout(() => {
        notification.style.transform = 'translateX(400px)';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 5000);
}

/**
 * Play notification sound
 */
function playNotificationSound() {
    // Create and play a simple beep sound
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.value = 800;
        oscillator.type = 'sine';
        
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.5);
    } catch (error) {
        console.log('Could not play notification sound:', error);
    }
}

/**
 * Request notification permission
 */
function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
}

// Request notification permission on load
requestNotificationPermission();

