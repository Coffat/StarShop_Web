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
    // Load with pagination to get ALL messages (increase page size)
    fetch('/api/chat/conversations/' + conversationId + '/messages?page=0&size=100')
        .then(response => response.json())
        .then(data => {
            if (data.data) {
                const messagesContainer = document.getElementById('chatWidgetMessages');
                // Clear ALL existing messages
                messagesContainer.innerHTML = '';
                
                // Sort messages by sentAt ascending (oldest first)
                const sortedMessages = data.data.sort((a, b) => {
                    return new Date(a.sentAt) - new Date(b.sentAt);
                });
                
                // Display messages in order
                sortedMessages.forEach(message => {
                    displayChatWidgetMessage(message, true); // true = skip duplicate check for initial load
                });
                
                console.log(`Loaded ${sortedMessages.length} messages`);
                
                // Scroll to bottom after loading
                setTimeout(() => {
                    scrollChatWidgetToBottom();
                }, 100);
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
function displayChatWidgetMessage(message, skipDuplicateCheck = false) {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    const isOwn = message.senderId === chatWidgetUserId;
    
    // Check if message already exists (skip for initial load)
    if (!skipDuplicateCheck) {
        const existingMessage = messagesContainer.querySelector(`[data-message-id="${message.id}"]`);
        if (existingMessage) {
            return;
        }
    }
    
    // Create message element with MODERN BEAUTIFUL design
    const messageDiv = document.createElement('div');
    messageDiv.className = 'flex mb-4 animate-fade-in ' + (isOwn ? 'justify-end' : 'justify-start');
    messageDiv.setAttribute('data-message-id', message.id);
    
    if (isOwn) {
        // Customer message (right side)
        messageDiv.innerHTML = `
            <div class="flex items-end space-x-2 max-w-xs">
                <div class="flex flex-col items-end">
                    <div class="bg-gradient-to-r from-pink-500 to-purple-600 text-white px-4 py-3 rounded-2xl rounded-br-md shadow-lg">
                        <div class="text-sm leading-relaxed">${parseMarkdown(message.content)}</div>
                    </div>
                    <span class="text-xs text-gray-500 mt-1 px-2">
                        ${formatMessageTime(message.sentAt)}
                        ${message.isRead ? '✓✓' : '✓'}
                    </span>
                </div>
                <div class="w-8 h-8 rounded-full bg-gradient-to-r from-pink-500 to-purple-600 flex items-center justify-center text-white text-sm font-semibold">
                    ${chatWidgetUserName ? chatWidgetUserName.charAt(0).toUpperCase() : 'C'}
                </div>
            </div>
        `;
    } else {
        // Staff/AI message (left side) - parse markdown for images and links
        messageDiv.innerHTML = `
            <div class="flex items-start space-x-2 max-w-md">
                <div class="w-8 h-8 rounded-full bg-gradient-to-r from-blue-500 to-cyan-500 flex items-center justify-center text-white text-sm font-semibold flex-shrink-0">
                    🌸
                </div>
                <div class="flex flex-col items-start flex-1">
                    <div class="bg-white text-gray-800 px-4 py-3 rounded-2xl rounded-bl-md shadow-lg border border-gray-100 w-full">
                        <div class="text-sm leading-relaxed">${parseMarkdown(message.content)}</div>
                    </div>
                    <span class="text-xs text-gray-500 mt-1 px-2">${message.senderName || 'StarShop Support'}</span>
                </div>
            </div>
        `;
    }
    
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
 * Format message time
 */
function formatMessageTime(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'});
}

/**
 * Parse basic markdown to HTML
 * Supports: images, links, bold text, lists, line breaks
 */
function parseMarkdown(text) {
    if (!text) return '';
    
    let html = text;
    
    // Parse images first: ![alt](url)
    html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, function(match, alt, url) {
        const safeAlt = alt.replace(/"/g, '&quot;');
        return `<img src="${url}" alt="${safeAlt}" class="max-w-full h-auto rounded-lg my-2 cursor-pointer message-image-hover" style="max-width: 200px;" onclick="window.open('${url}', '_blank')" />`;
    });
    
    // Parse links: [text](url)
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, function(match, text, url) {
        return `<a href="${url}" target="_blank" class="text-blue-600 hover:underline">${text}</a>`;
    });
    
    // Parse bold: **text**
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    
    // Parse bullet lists: * item or - item
    html = html.replace(/^[\*\-]\s+(.+)$/gm, '<li class="ml-4 mb-1">• $1</li>');
    
    // Wrap consecutive list items in ul
    html = html.replace(/(<li[^>]*>.*?<\/li>\s*)+/g, function(match) {
        return '<ul class="my-2 space-y-1">' + match + '</ul>';
    });
    
    // Parse numbered list items: 1️⃣, 2️⃣, etc.
    html = html.replace(/(\d)️⃣/g, '<span class="font-bold text-pink-600">$1️⃣</span>');
    
    // Parse line breaks (but not inside lists)
    html = html.replace(/\n(?![<ul>|<li>])/g, '<br>');
    
    // Clean up extra br tags around lists
    html = html.replace(/<br>\s*<ul>/g, '<ul>');
    html = html.replace(/<\/ul>\s*<br>/g, '</ul>');
    
    return html;
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

