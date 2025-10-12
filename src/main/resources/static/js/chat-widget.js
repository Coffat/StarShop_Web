/**
 * Customer Chat Widget - WebSocket Integration
 * Handles real-time chat between customers and staff
 */

let chatWidgetStompClient = null;
let chatWidgetConversationId = null;
let chatWidgetUserId = null;
let chatWidgetUserName = null;
let chatWidgetIsConnected = false;

/**
 * Get CSRF token from meta tag
 */
function getCsrfToken() {
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    return {
        token: csrfToken ? csrfToken.getAttribute('content') : null,
        header: csrfHeader ? csrfHeader.getAttribute('content') : 'X-XSRF-TOKEN'
    };
}

/**
 * Initialize chat widget on page load
 */
document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    fetch('/api/auth/me')
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Not authenticated');
        })
        .then(data => {
            if (data.data) {
                chatWidgetUserId = data.data.id;
                chatWidgetUserName = data.data.firstname + ' ' + data.data.lastname;
                initializeChatWidget();
            }
        })
        .catch(error => {
            console.log('Chat widget: User not logged in');
            // Hide chat widget if not logged in
            const chatWidget = document.getElementById('chatWidget');
            if (chatWidget) {
                chatWidget.style.display = 'none';
            }
        });
});

/**
 * Initialize chat widget connection
 */
function initializeChatWidget() {
    // Connect to WebSocket
    connectChatWidget();
    
    // Load existing conversation or create new one
    loadOrCreateConversation();
}

/**
 * Connect to WebSocket
 */
function connectChatWidget() {
    const socket = new SockJS('/ws');
    chatWidgetStompClient = Stomp.over(socket);
    
    // Disable debug logging
    chatWidgetStompClient.debug = null;
    
    chatWidgetStompClient.connect({}, function(frame) {
        console.log('Chat Widget Connected: ' + frame);
        chatWidgetIsConnected = true;
        updateChatWidgetStatus('online');
        
        // Subscribe to personal queue
        chatWidgetStompClient.subscribe('/user/queue/chat', function(message) {
            const chatMessage = JSON.parse(message.body);
            displayChatWidgetMessage(chatMessage);
        });
        
        // Subscribe to conversation topic (will be set when conversation is loaded)
        if (chatWidgetConversationId) {
            subscribeToChatWidgetConversation(chatWidgetConversationId);
        }
    }, function(error) {
        console.error('Chat Widget Connection Error:', error);
        chatWidgetIsConnected = false;
        updateChatWidgetStatus('offline');
        
        // Retry connection after 5 seconds
        setTimeout(connectChatWidget, 5000);
    });
}

/**
 * Subscribe to conversation topic
 */
function subscribeToChatWidgetConversation(conversationId) {
    if (chatWidgetStompClient && chatWidgetIsConnected) {
        chatWidgetStompClient.subscribe('/topic/chat/' + conversationId, function(message) {
            const chatMessage = JSON.parse(message.body);
            displayChatWidgetMessage(chatMessage);
        });
    }
}

/**
 * Load existing conversation or create new one
 */
function loadOrCreateConversation() {
    fetch('/api/chat/conversations/my')
        .then(response => response.json())
        .then(data => {
            if (data.data && data.data.length > 0) {
                // Use the first open conversation
                const openConversation = data.data.find(c => c.status === 'OPEN' || c.status === 'IN_PROGRESS' || c.status === 'PENDING_STAFF');
                if (openConversation) {
                    chatWidgetConversationId = openConversation.id;
                    loadChatWidgetMessages(chatWidgetConversationId);
                    subscribeToChatWidgetConversation(chatWidgetConversationId);
                } else {
                    // Create new conversation
                    createNewConversation();
                }
            } else {
                // Create new conversation
                createNewConversation();
            }
        })
        .catch(error => {
            console.error('Error loading conversations:', error);
        });
}

/**
 * Create new conversation
 */
function createNewConversation() {
    // Don't create conversation immediately, wait for first message
    console.log('Conversation will be created when first message is sent');
}

/**
 * Load messages for conversation
 */
function loadChatWidgetMessages(conversationId) {
    fetch('/api/chat/conversations/' + conversationId + '/messages')
        .then(response => response.json())
        .then(data => {
            if (data.data) {
                const messagesContainer = document.getElementById('chatWidgetMessages');
                // Clear existing messages except welcome message
                const welcomeMessage = messagesContainer.querySelector('.message-received');
                messagesContainer.innerHTML = '';
                if (welcomeMessage) {
                    messagesContainer.appendChild(welcomeMessage);
                }
                
                // Display messages
                data.data.forEach(message => {
                    displayChatWidgetMessage(message);
                });
                
                // Scroll to bottom
                scrollChatWidgetToBottom();
            }
        })
        .catch(error => {
            console.error('Error loading messages:', error);
        });
}

/**
 * Toggle chat widget visibility
 */
function toggleChatWidget() {
    const chatWindow = document.getElementById('chatWidgetWindow');
    const chatButton = document.getElementById('chatBubbleButton');
    const badge = document.getElementById('chatWidgetBadge');
    
    if (chatWindow.style.display === 'none') {
        chatWindow.style.display = 'flex';
        chatButton.classList.remove('pulsate');
        badge.style.display = 'none';
        badge.textContent = '0';
        
        // Mark messages as read
        if (chatWidgetConversationId) {
            markChatWidgetMessagesAsRead(chatWidgetConversationId);
        }
        
        // Scroll to bottom
        scrollChatWidgetToBottom();
        
        // Focus input
        document.getElementById('chatWidgetInput').focus();
    } else {
        chatWindow.style.display = 'none';
    }
}

/**
 * Send message from widget
 */
function sendWidgetMessage() {
    const input = document.getElementById('chatWidgetInput');
    const content = input.value.trim();
    
    if (!content) {
        return;
    }
    
    // Allow sending first message even without conversation ID
    
    // Create message object
    const message = {
        conversationId: chatWidgetConversationId,
        content: content,
        messageType: 'TEXT'
    };
    
    // Get CSRF token
    const csrf = getCsrfToken();
    const headers = {
        'Content-Type': 'application/json'
    };
    if (csrf.token) {
        headers[csrf.header] = csrf.token;
    }
    
    // Determine which API to use
    const apiEndpoint = chatWidgetConversationId ? '/api/chat/messages' : '/api/chat/messages/first';
    
    // Send via REST API
    fetch(apiEndpoint, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(message)
    })
    .then(response => response.json())
    .then(data => {
        if (data.data) {
            // Message sent successfully
            input.value = '';
            
            // If this was the first message, set conversation ID
            if (!chatWidgetConversationId && data.data.conversationId) {
                chatWidgetConversationId = data.data.conversationId;
                subscribeToChatWidgetConversation(chatWidgetConversationId);
            }
            
            // Display message immediately (will be confirmed via WebSocket)
            displayChatWidgetMessage(data.data);
            
            // Scroll to bottom
            scrollChatWidgetToBottom();
        } else {
            console.error('Error sending message:', data.error);
            alert('Không thể gửi tin nhắn. Vui lòng thử lại.');
        }
    })
    .catch(error => {
        console.error('Error sending message:', error);
        alert('Không thể gửi tin nhắn. Vui lòng thử lại.');
    });
}

/**
 * Display message in chat widget
 */
function displayChatWidgetMessage(message) {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    const isOwn = message.senderId === chatWidgetUserId;
    
    // Check if message already exists
    const existingMessage = messagesContainer.querySelector(`[data-message-id="${message.id}"]`);
    if (existingMessage) {
        return;
    }
    
    // Create message element
    const messageDiv = document.createElement('div');
    messageDiv.className = 'flex mb-4 ' + (isOwn ? 'justify-end' : 'justify-start');
    messageDiv.setAttribute('data-message-id', message.id);
    
    const bubbleDiv = document.createElement('div');
    bubbleDiv.className = 'max-w-xs ' + (isOwn ? 'message-sent' : 'message-received');
    
    const contentP = document.createElement('p');
    contentP.className = 'text-sm';
    contentP.textContent = message.content;
    
    const timeDiv = document.createElement('div');
    timeDiv.className = 'message-time mt-1';
    
    const time = new Date(message.sentAt);
    const timeStr = time.getHours().toString().padStart(2, '0') + ':' + time.getMinutes().toString().padStart(2, '0');
    timeDiv.textContent = timeStr;
    
    if (isOwn) {
        const readIcon = document.createElement('i');
        readIcon.className = message.isRead ? 'fas fa-check-double ml-1 text-blue-300' : 'fas fa-check ml-1';
        timeDiv.appendChild(readIcon);
    } else {
        timeDiv.textContent = message.senderName || 'Staff';
    }
    
    bubbleDiv.appendChild(contentP);
    bubbleDiv.appendChild(timeDiv);
    messageDiv.appendChild(bubbleDiv);
    messagesContainer.appendChild(messageDiv);
    
    // Scroll to bottom
    scrollChatWidgetToBottom();
    
    // If chat window is closed and message is not from current user, show notification
    const chatWindow = document.getElementById('chatWidgetWindow');
    if (!isOwn && chatWindow.style.display === 'none') {
        showChatWidgetNotification();
    }
}

/**
 * Show notification badge
 */
function showChatWidgetNotification() {
    const badge = document.getElementById('chatWidgetBadge');
    const button = document.getElementById('chatBubbleButton');
    
    const currentCount = parseInt(badge.textContent) || 0;
    badge.textContent = currentCount + 1;
    badge.style.display = 'flex';
    button.classList.add('pulsate');
}

/**
 * Mark messages as read
 */
function markChatWidgetMessagesAsRead(conversationId) {
    const csrf = getCsrfToken();
    const headers = {
        'Content-Type': 'application/json'
    };
    if (csrf.token) {
        headers[csrf.header] = csrf.token;
    }
    
    fetch('/api/chat/conversations/' + conversationId + '/read', {
        method: 'PUT',
        headers: headers
    })
    .then(response => response.json())
    .then(data => {
        console.log('Messages marked as read');
    })
    .catch(error => {
        console.error('Error marking messages as read:', error);
    });
}

/**
 * Scroll chat to bottom
 */
function scrollChatWidgetToBottom() {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

/**
 * Update connection status
 */
function updateChatWidgetStatus(status) {
    const statusIndicator = document.getElementById('chatWidgetStatus');
    if (statusIndicator) {
        if (status === 'online') {
            statusIndicator.className = 'inline-block w-2 h-2 rounded-full bg-green-400 mr-1';
        } else {
            statusIndicator.className = 'inline-block w-2 h-2 rounded-full bg-gray-400 mr-1';
        }
    }
}

