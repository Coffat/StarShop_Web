/**
 * Staff Notifications System - Alpine.js Component
 * Handle real-time notifications for staff members
 */

/**
 * Alpine.js component factory for staff notifications
 */
function staffNotificationComponent() {
    return {
        // Component state
        open: false,
        notifications: [],
        unreadCount: 0,
        stompSubscription: null,
        
        /**
         * Initialize component
         */
        init() {
            console.log('🔔 Initializing staff notification component...');
            console.log('📊 Initial state - notifications:', this.notifications.length, 'unreadCount:', this.unreadCount);
            
            // Load notifications from localStorage
            this.loadNotifications();
            
            console.log('📊 After load - notifications:', this.notifications.length, 'unreadCount:', this.unreadCount);
            
            // Subscribe to WebSocket
            this.connectWebSocket();
        },
        
        /**
         * Connect to WebSocket and subscribe to notification topics
         */
        connectWebSocket() {
            const self = this;
            
            // Check if global stompClient is ready
            if (typeof window.stompClient !== 'undefined' && window.stompClient && window.stompClient.connected) {
                this.subscribeToNotifications();
            } else {
                // Wait for WebSocket connection
                console.log('⏳ Waiting for WebSocket connection...');
                setTimeout(() => this.connectWebSocket(), 1000);
            }
        },
        
        /**
         * Subscribe to notification WebSocket topics
         */
        subscribeToNotifications() {
            const self = this;
            
            // Check if already subscribed
            if (this.stompSubscription) {
                console.log('⚠️ Already subscribed to notifications, skipping');
                return;
            }
            
            try {
                console.log('🔗 Subscribing to /topic/staff/notifications...');
                
                // Subscribe to broadcast notifications (like PII alerts)
                this.stompSubscription = window.stompClient.subscribe('/topic/staff/notifications', function(message) {
                    const notification = JSON.parse(message.body);
                    console.log('📩 Received staff notification:', notification);
                    self.addNotification(notification);
                });
                
                console.log('✅ Successfully subscribed to /topic/staff/notifications');
            } catch (error) {
                console.error('❌ Error subscribing to notifications:', error);
            }
        },
        
        /**
         * Add new notification
         */
        addNotification(data) {
            const notification = {
                id: Date.now(),
                message: data.message || data.content,
                type: data.type || 'new_message',
                conversationId: data.conversationId || null,
                createdAt: data.timestamp || new Date().toISOString(),
                unread: true
            };
            
            // Add to beginning of array
            this.notifications.unshift(notification);
            
            // Keep only last 50 notifications
            if (this.notifications.length > 50) {
                this.notifications = this.notifications.slice(0, 50);
            }
            
            // Update unread count
            this.updateUnreadCount();
            
            // Save to localStorage
            this.saveNotifications();
            
            // Play notification sound
            this.playNotificationSound(notification.type);
            
            // Show browser notification
            this.showBrowserNotification(notification);
            
            console.log(`✅ Added notification (total: ${this.notifications.length}, unread: ${this.unreadCount})`);
        },
        
        /**
         * Mark all notifications as read
         */
        markAllAsRead() {
            this.notifications.forEach(n => n.unread = false);
            this.updateUnreadCount();
            this.saveNotifications();
            console.log('📖 Marked all notifications as read');
        },
        
        /**
         * Handle notification click
         */
        handleNotificationClick(notification) {
            console.log('👆 Notification clicked:', notification);
            
            // Mark as read
            notification.unread = false;
            this.updateUnreadCount();
            this.saveNotifications();
            
            // Close dropdown
            this.open = false;
            
            // Navigate to conversation if available
            if (notification.conversationId) {
                // If on chat page, select conversation
                if (window.location.pathname.includes('/staff/chat')) {
                    if (typeof window.selectConversationById === 'function') {
                        window.selectConversationById(notification.conversationId);
                    } else {
                        console.error('selectConversationById not available');
                    }
                } else {
                    // Navigate to chat page with conversation
                    window.location.href = `/staff/chat?conversation=${notification.conversationId}`;
                }
            }
        },
        
        /**
         * Clear all notifications
         */
        clearAllNotifications() {
            if (confirm('Bạn có chắc muốn xóa tất cả thông báo?')) {
                this.notifications = [];
                this.updateUnreadCount();
                this.saveNotifications();
                console.log('🗑️ Cleared all notifications');
            }
        },
        
        /**
         * Update unread count
         */
        updateUnreadCount() {
            this.unreadCount = this.notifications.filter(n => n.unread).length;
        },
        
        /**
         * Format notification time
         */
        formatNotificationTime(dateTimeStr) {
            if (!dateTimeStr) return '';
            
            const date = new Date(dateTimeStr);
            const now = new Date();
            const diffMs = now - date;
            const diffMins = Math.floor(diffMs / 60000);
            const diffHours = Math.floor(diffMs / 3600000);
            const diffDays = Math.floor(diffMs / 86400000);
            
            if (diffMins < 1) return 'Vừa xong';
            if (diffMins < 60) return `${diffMins} phút trước`;
            if (diffHours < 24) return `${diffHours} giờ trước`;
            if (diffDays < 7) return `${diffDays} ngày trước`;
            
            return date.toLocaleDateString('vi-VN', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            });
        },
        
        /**
         * Play notification sound
         */
        playNotificationSound(type) {
            try {
                const audio = new Audio();
                
                // Different sounds for different notification types
                if (type === 'pii_alert') {
                    audio.src = '/sounds/urgent.mp3'; // High priority
                } else {
                    audio.src = '/sounds/notification.mp3'; // Normal
                }
                
                audio.volume = 0.5;
                audio.play().catch(e => console.log('🔇 Could not play sound:', e));
            } catch (e) {
                console.log('🔇 Notification sound not available:', e);
            }
        },
        
        /**
         * Show browser notification
         */
        showBrowserNotification(notification) {
            // Check if browser supports notifications
            if (!('Notification' in window)) return;
            
            // Request permission if needed
            if (Notification.permission === 'granted') {
                this.createBrowserNotification(notification);
            } else if (Notification.permission !== 'denied') {
                Notification.requestPermission().then(permission => {
                    if (permission === 'granted') {
                        this.createBrowserNotification(notification);
                    }
                });
            }
        },
        
        /**
         * Create browser notification
         */
        createBrowserNotification(notification) {
            const options = {
                body: notification.message,
                icon: '/images/logo.png',
                badge: '/images/badge.png',
                tag: notification.id.toString(),
                requireInteraction: notification.type === 'pii_alert' // Require action for urgent
            };
            
            const n = new Notification('StarShop - Thông báo mới', options);
            
            n.onclick = () => {
                window.focus();
                this.handleNotificationClick(notification);
                n.close();
            };
        },
        
        /**
         * Save notifications to localStorage
         */
        saveNotifications() {
            try {
                localStorage.setItem('staff_notifications', JSON.stringify(this.notifications));
                localStorage.setItem('staff_notifications_unread', this.unreadCount.toString());
            } catch (e) {
                console.error('❌ Failed to save notifications:', e);
            }
        },
        
        /**
         * Load notifications from localStorage
         */
        loadNotifications() {
            try {
                const stored = localStorage.getItem('staff_notifications');
                console.log('🔍 localStorage raw data:', stored ? stored.substring(0, 100) + '...' : 'null');
                
                if (stored) {
                    this.notifications = JSON.parse(stored);
                    this.updateUnreadCount();
                    console.log(`📥 Loaded ${this.notifications.length} notifications from storage (unread: ${this.unreadCount})`);
                    console.log('📋 First notification:', this.notifications[0]);
                } else {
                    console.log('⚠️ No notifications in localStorage');
                }
            } catch (e) {
                console.error('❌ Failed to load notifications:', e);
                this.notifications = [];
            }
        }
    };
}

// Make component factory globally available
window.staffNotificationComponent = staffNotificationComponent;

console.log('✅ Staff notifications Alpine component loaded');
