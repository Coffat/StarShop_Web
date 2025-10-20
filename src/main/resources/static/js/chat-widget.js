/**
 * Customer Chat Widget - WebSocket Integration
 * Handles real-time chat between customers and staff
 */

let chatWidgetStompClient = null;
let chatWidgetConversationId = null;
let chatWidgetUserId = null;
let chatWidgetUserName = null;
let chatWidgetIsConnected = false;
// Keep only one active subscription per conversation
let chatWidgetConversationSubscription = null;
let chatWidgetSubscribedConversationId = null;
// Track last outbound message to trigger resync if response misses
let chatWidgetLastSendAt = 0;
// Prevent multiple simultaneous message loads
let chatWidgetLoadingMessages = false;

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
        // Prevent duplicate subscriptions
        if (chatWidgetSubscribedConversationId === conversationId && chatWidgetConversationSubscription) {
            return;
        }
        if (chatWidgetConversationSubscription) {
            try { chatWidgetConversationSubscription.unsubscribe(); } catch (e) { /* noop */ }
            chatWidgetConversationSubscription = null;
        }

        chatWidgetConversationSubscription = chatWidgetStompClient.subscribe('/topic/chat/' + conversationId, function(message) {
            const chatMessage = JSON.parse(message.body);
            displayChatWidgetMessage(chatMessage);
        });
        chatWidgetSubscribedConversationId = conversationId;
        // Sau khi subscribe, đảm bảo đang ở cuối danh sách (trường hợp conversation vừa tạo)
        setTimeout(scrollChatWidgetToBottom, 50);
        // Safety resync right after subscribe to avoid missing early messages
        // Only if we haven't loaded messages recently
        setTimeout(() => {
            if (!chatWidgetLoadingMessages) {
                try { loadChatWidgetMessages(conversationId); } catch (e) { /* noop */ }
            }
        }, 200);
    }
}

// Subscribe to general chat updates to handle hide_typing broadcast
if (!window.chatWidgetSubscribedChatUpdates) {
    window.chatWidgetSubscribedChatUpdates = true;
    try {
        const ensureConnection = () => {
            if (chatWidgetStompClient && chatWidgetIsConnected) {
                chatWidgetStompClient.subscribe('/topic/chat-updates', function(message) {
                    try {
                        const update = JSON.parse(message.body);
                        if (update && update.type === 'hide_typing') {
                            if (!update.data || !chatWidgetConversationId || update.data.conversationId == chatWidgetConversationId) {
                                hideTypingIndicator();
                            }
                        }
                    } catch (e) { /* noop */ }
                });
            } else {
                setTimeout(ensureConnection, 500);
            }
        };
        ensureConnection();
    } catch (e) { /* noop */ }
}

/**
 * Load existing conversation or create new one
 */
function loadOrCreateConversation() {
    fetch('/api/chat/conversations/my')
        .then(response => response.json())
        .then(data => {
            if (data.data && data.data.length > 0) {
                // Use the first active conversation (OPEN or ASSIGNED)
                const activeConversation = data.data.find(c => c.status === 'OPEN' || c.status === 'ASSIGNED');
                if (activeConversation) {
                    chatWidgetConversationId = activeConversation.id;
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
            // Silent fail for conversation loading
        });
}

/**
 * Create new conversation
 */
function createNewConversation() {
    // Don't create conversation immediately, wait for first message
}

/**
 * Load messages for conversation
 */
function loadChatWidgetMessages(conversationId) {
    // Prevent multiple simultaneous loads
    if (chatWidgetLoadingMessages) {
        return;
    }
    
    chatWidgetLoadingMessages = true;
    
    // Load with pagination to get ALL messages (increase page size)
    fetch('/api/chat/conversations/' + conversationId + '/messages?page=0&size=100')
        .then(response => response.json())
        .then(data => {
            if (data.data) {
                const messagesContainer = document.getElementById('chatWidgetMessages');
                
                // Sort messages by sentAt ascending (oldest first)
                const sortedMessages = data.data.sort((a, b) => {
                    return new Date(a.sentAt) - new Date(b.sentAt);
                });
                
                // Check if we need to reload or just add new messages
                const existingMessageIds = new Set();
                const existingMessages = messagesContainer.querySelectorAll('[data-message-id]');
                existingMessages.forEach(el => {
                    const id = el.getAttribute('data-message-id');
                    if (id && !id.startsWith('temp-')) {
                        existingMessageIds.add(id);
                    }
                });
                
                // Only clear and reload if we have significantly different messages
                const newMessageIds = new Set(sortedMessages.map(m => m.id.toString()));
                const hasNewMessages = sortedMessages.some(m => !existingMessageIds.has(m.id.toString()));
                
                if (hasNewMessages && sortedMessages.length > existingMessageIds.size) {
                    
                    // Preserve temp user messages before clearing
                    const tempUserMessages = [];
                    const tempElements = messagesContainer.querySelectorAll('[data-message-id^="temp-user-"]');
                    tempElements.forEach(el => {
                        const id = el.getAttribute('data-message-id');
                        const content = el.querySelector('.text-sm.leading-relaxed')?.innerHTML;
                        if (id && content) {
                            tempUserMessages.push({ id, content });
                        }
                    });
                    
                    // Clear ALL existing messages
                    messagesContainer.innerHTML = '';
                    
                    // Display messages in order
                    sortedMessages.forEach(message => {
                        displayChatWidgetMessage(message, true); // true = skip duplicate check for initial load
                    });
                    
                    // Restore temp user messages if they weren't replaced by persisted messages
                    tempUserMessages.forEach(tempMsg => {
                        const persistedExists = sortedMessages.some(m => 
                            m.senderId === chatWidgetUserId && 
                            parseMarkdown(m.content).trim() === tempMsg.content.trim()
                        );
                        if (!persistedExists) {
                            // Recreate temp message element
                            const tempDiv = document.createElement('div');
                            tempDiv.className = 'flex mb-4 justify-end';
                            tempDiv.setAttribute('data-message-id', tempMsg.id);
                            tempDiv.innerHTML = `
                                <div class="flex items-end space-x-2 max-w-xs">
                                    <div class="flex flex-col items-end">
                                        <div class="bg-gradient-to-r from-pink-500 to-purple-600 text-white px-4 py-3 rounded-2xl rounded-br-md shadow-lg">
                                            <div class="text-sm leading-relaxed">${tempMsg.content}</div>
                                        </div>
                                        <span class="text-xs text-gray-500 mt-1 px-2">✓</span>
                                    </div>
                                    <div class="w-8 h-8 rounded-full bg-gradient-to-r from-pink-500 to-purple-600 flex items-center justify-center text-white text-sm font-semibold">
                                        ${chatWidgetUserName ? chatWidgetUserName.charAt(0).toUpperCase() : 'C'}
                                    </div>
                                </div>
                            `;
                            messagesContainer.appendChild(tempDiv);
                        }
                    });
                } else {
                    // No new messages, keeping existing display
                }
                
                // Scroll to bottom after loading
                setTimeout(() => {
                    scrollChatWidgetToBottom();
                }, 100);
            }
        })
        .catch(error => {
            // Silent fail for message loading
        })
        .finally(() => {
            chatWidgetLoadingMessages = false;
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
        
        // Force reload messages when opening chat to ensure we have latest
        if (chatWidgetConversationId && !chatWidgetLoadingMessages) {
            loadChatWidgetMessages(chatWidgetConversationId);
        } else {
            // Scroll to bottom
            scrollChatWidgetToBottom();
        }
        
        // Focus input
        document.getElementById('chatWidgetInput').focus();
    } else {
        chatWindow.style.display = 'none';
    }
}

/**
 * Send message from widget with streaming support
 */
function sendWidgetMessage() {
    const input = document.getElementById('chatWidgetInput');
    const content = input.value.trim();
    
    if (!content) {
        return;
    }
    
    // Clear input immediately for better UX
    input.value = '';
    
    // Create message object
    const message = {
        conversationId: chatWidgetConversationId,
        content: content,
        messageType: 'TEXT'
    };
    
    // Display user message immediately with a more unique ID
    const tempMessageId = 'temp-user-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    displayChatWidgetMessage({
        id: tempMessageId,
        senderId: chatWidgetUserId,
        content: content,
        sentAt: new Date().toISOString(),
        isRead: false
    });
    
    // Show typing indicator for AI response if conversation not assigned to staff
    try {
        if (window.chatWidgetConversation && window.chatWidgetConversation.status === 'ASSIGNED') {
            // Do not show AI typing when staff is handling
        } else {
            showTypingIndicator();
        }
    } catch (e) {
        // Fallback to showing typing indicator if state unknown
        showTypingIndicator();
    }
    
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
    
    // Check if streaming is supported and enabled
    const supportsStreaming = window.EventSource && chatWidgetConversationId;
    
    if (supportsStreaming) {
        // Try streaming approach
        sendMessageWithStreaming(message, headers, apiEndpoint);
    } else {
        // Fallback to regular approach
        sendMessageRegular(message, headers, apiEndpoint);
    }

    // Failsafe: if no new message arrives within 2.5s, reload messages
    chatWidgetLastSendAt = Date.now();
    setTimeout(() => {
        const elapsed = Date.now() - chatWidgetLastSendAt;
        if (elapsed >= 2400 && chatWidgetConversationId && !chatWidgetLoadingMessages) {
            try { loadChatWidgetMessages(chatWidgetConversationId); } catch (e) { /* noop */ }
        }
    }, 2500);
}

/**
 * Send message with streaming response
 */
function sendMessageWithStreaming(message, headers, apiEndpoint) {
    // Send message first
    fetch(apiEndpoint, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(message)
    })
    .then(response => response.json())
    .then(data => {
        if (data.data) {
            // Update conversation ID if needed
            if (!chatWidgetConversationId && data.data.conversationId) {
                chatWidgetConversationId = data.data.conversationId;
                subscribeToChatWidgetConversation(chatWidgetConversationId);
                // Khi bắt đầu stream ngay lần đầu, đảm bảo đã có placeholder trước
                // và đồng thời fallback fetch lại nếu stream không tới
                setTimeout(() => {
                    if (!chatWidgetLoadingMessages) {
                        try { loadChatWidgetMessages(chatWidgetConversationId); } catch (e) { /* noop */ }
                    }
                }, 150);
            }
            
            // Start streaming response
            startStreamingResponse(chatWidgetConversationId);
        } else {
            hideTypingIndicator();
            displayChatWidgetMessage({
                id: 'error-' + Date.now(),
                senderId: 'system',
                content: 'Không thể gửi tin nhắn. Vui lòng thử lại.',
                sentAt: new Date().toISOString(),
                senderName: 'System'
            });
        }
    })
    .catch(error => {
        hideTypingIndicator();
        // Fallback to regular approach
        sendMessageRegular(message, headers, apiEndpoint);
    });
}

/**
 * Send message with regular (non-streaming) response
 */
function sendMessageRegular(message, headers, apiEndpoint) {
    fetch(apiEndpoint, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(message)
    })
    .then(response => response.json())
    .then(data => {
        hideTypingIndicator();
        
        if (data.data) {
            // Update conversation ID if needed
            if (!chatWidgetConversationId && data.data.conversationId) {
                chatWidgetConversationId = data.data.conversationId;
                subscribeToChatWidgetConversation(chatWidgetConversationId);
                // Safety: ngay sau khi có conversation đầu tiên, tải lại toàn bộ tin nhắn
                // để tránh miss phản hồi AI do subscribe trễ trong lần đầu
                setTimeout(() => {
                    if (!chatWidgetLoadingMessages) {
                        try { loadChatWidgetMessages(chatWidgetConversationId); } catch (e) { /* noop */ }
                    }
                }, 150);
            }
            
            // AI response will come via WebSocket
        } else {
            displayChatWidgetMessage({
                id: 'error-' + Date.now(),
                senderId: 'system',
                content: 'Không thể gửi tin nhắn. Vui lòng thử lại.',
                sentAt: new Date().toISOString(),
                senderName: 'System'
            });
        }
    })
    .catch(error => {
        hideTypingIndicator();
        displayChatWidgetMessage({
            id: 'error-' + Date.now(),
            senderId: 'system',
            content: 'Có lỗi xảy ra. Vui lòng thử lại.',
            sentAt: new Date().toISOString(),
            senderName: 'System'
        });
    });
}

/**
 * Start streaming response using EventSource
 */
function startStreamingResponse(conversationId) {
    const eventSource = new EventSource(`/api/chat/stream/${conversationId}`);
    let streamingMessageId = 'streaming-' + Date.now();
    let accumulatedContent = '';
    
    // Create placeholder message for streaming content
    const placeholderMessage = {
        id: streamingMessageId,
        senderId: 'ai',
        content: '',
        sentAt: new Date().toISOString(),
        senderName: 'Hoa AI'
    };
    
    eventSource.onmessage = function(event) {
        try {
            const data = JSON.parse(event.data);
            
            if (data.type === 'chunk') {
                // Hide typing indicator on first chunk
                if (accumulatedContent === '') {
                    hideTypingIndicator();
                    displayChatWidgetMessage(placeholderMessage);
                }
                
                // Accumulate content
                accumulatedContent += data.content;
                
                // Update the streaming message
                updateStreamingMessage(streamingMessageId, accumulatedContent);
                
            } else if (data.type === 'complete') {
                // Streaming complete
                eventSource.close();
                hideTypingIndicator();
                
                // Replace with final message
                if (data.finalMessage) {
                    replaceStreamingMessage(streamingMessageId, data.finalMessage);
                }
                
            } else if (data.type === 'error') {
                // Streaming error
                eventSource.close();
                hideTypingIndicator();
                // Streaming error
                
                // Show error message
                if (accumulatedContent === '') {
                    displayChatWidgetMessage({
                        id: 'error-' + Date.now(),
                        senderId: 'system',
                        content: 'Có lỗi xảy ra khi tạo phản hồi. Vui lòng thử lại.',
                        sentAt: new Date().toISOString(),
                        senderName: 'System'
                    });
                }
            }
        } catch (error) {
            // Error parsing streaming data
        }
    };
    
    eventSource.onerror = function(error) {
        eventSource.close();
        hideTypingIndicator();
        
        // Fallback: the response will come via WebSocket
        setTimeout(() => {
            if (accumulatedContent === '') {
                // Streaming failed, waiting for WebSocket response
                // Extra safety: reload messages to avoid missed AI reply
                if (!chatWidgetLoadingMessages) {
                    try { loadChatWidgetMessages(conversationId); } catch (e) { /* noop */ }
                }
            }
        }, 1000);
    };
    
    // Close EventSource after 30 seconds (timeout)
    setTimeout(() => {
        if (eventSource.readyState !== EventSource.CLOSED) {
            eventSource.close();
            hideTypingIndicator();
        }
    }, 30000);
}

/**
 * Display message in chat widget
 */
function displayChatWidgetMessage(message, skipDuplicateCheck = false) {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    const isOwn = message.senderId === chatWidgetUserId;
    
    // Enhanced duplicate check to prevent duplicate messages
    if (!skipDuplicateCheck) {
        // Check by message ID first (most reliable)
        const existingMessage = messagesContainer.querySelector(`[data-message-id="${message.id}"]`);
        if (existingMessage) {
            return;
        }
        
        // Only check for temp message replacement (not general content duplicates)
        if (isOwn && message.id && !String(message.id).startsWith('temp')) {
            // Find and remove temp message with same content
            const tempDup = Array.from(messagesContainer.querySelectorAll('[data-message-id^="temp-"]'))
                .reverse()
                .find(el => {
                    const div = el.querySelector('.text-sm.leading-relaxed');
                    return div && div.innerHTML.trim() === parseMarkdown(message.content).trim();
                });
            if (tempDup) {
                try { tempDup.remove(); } catch (e) { /* noop */ }
            }
        }
        
        // Special handling: if this is a temp user message, make sure it's not replaced by accident
        if (isOwn && String(message.id).startsWith('temp-user-')) {
            // This is a temp user message, ensure it stays visible
        }
        
        // For AI messages, only check very recent duplicates (last 1 message) to avoid blocking legitimate responses
        if (!isOwn) {
            const recentMessages = messagesContainer.querySelectorAll('.flex.mb-4');
            const lastMessage = recentMessages[recentMessages.length - 1];
            if (lastMessage) {
                const contentDiv = lastMessage.querySelector('.text-sm.leading-relaxed');
                if (contentDiv && contentDiv.innerHTML.trim() === parseMarkdown(message.content).trim()) {
                    return;
                }
            }
        }
    }
    
    // If AI/system message arrives, ensure typing indicator is hidden
    if (!isOwn) {
        try { hideTypingIndicator(); } catch (e) { /* noop */ }
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
    
    // Insert message BEFORE typing indicator if it exists
    const typingIndicator = messagesContainer.querySelector('.typing-indicator');
    if (typingIndicator) {
        messagesContainer.insertBefore(messageDiv, typingIndicator);
    } else {
        messagesContainer.appendChild(messageDiv);
    }
    
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
 * OPTIMIZED: Handles escaped newlines and ensures proper formatting
 */
function parseMarkdown(text) {
    if (!text) return '';
    
    let html = text;
    
    // Fix escaped newlines from JSON (\\n -> actual newline)
    html = html.replace(/\\n/g, '\n');
    
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
    
    // Clean up extra br tags around lists and images
    html = html.replace(/<br>\s*<ul>/g, '<ul>');
    html = html.replace(/<\/ul>\s*<br>/g, '</ul>');
    html = html.replace(/<br>\s*<img/g, '<img');
    html = html.replace(/\/>\s*<br>/g, '/>');
    
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
        // Messages marked as read
    })
    .catch(error => {
        // Error marking messages as read
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

/**
 * Show typing indicator
 */
function showTypingIndicator() {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    
    // Remove existing typing indicator
    const existingIndicator = messagesContainer.querySelector('.typing-indicator');
    if (existingIndicator) {
        existingIndicator.remove();
    }
    
    // Create typing indicator - ALWAYS append to the END of messages
    const typingDiv = document.createElement('div');
    typingDiv.className = 'flex justify-start mb-4 typing-indicator';
    typingDiv.setAttribute('role', 'status');
    typingDiv.setAttribute('aria-live', 'polite');
    typingDiv.innerHTML = `
        <div class="flex items-start gap-3 max-w-md">
            <div class="w-9 h-9 rounded-full bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center text-white text-sm font-semibold shadow-md flex-shrink-0">
                🌸
            </div>
            <div class="flex flex-col items-start">
                <div class="px-4 py-2 rounded-2xl rounded-bl-md shadow-lg border border-gray-100 bg-white/90 backdrop-blur-sm">
                    <div class="flex items-center gap-1">
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                    </div>
                </div>
                <span class="text-[11px] text-gray-500 mt-1 px-1 leading-none typing-status">Hoa AI đang trả lời…</span>
            </div>
        </div>
    `;
    
    // ALWAYS append to the very end, after all existing messages
    messagesContainer.appendChild(typingDiv);
    
    // Force scroll to bottom to show typing indicator
    setTimeout(() => {
        scrollChatWidgetToBottom();
    }, 50);
}

/**
 * Hide typing indicator
 */
function hideTypingIndicator() {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    const typingIndicator = messagesContainer.querySelector('.typing-indicator');
    if (typingIndicator) {
        typingIndicator.remove();
    }
}

/**
 * Update streaming message content
 */
function updateStreamingMessage(messageId, content) {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    const messageElement = messagesContainer.querySelector(`[data-message-id="${messageId}"]`);
    
    if (messageElement) {
        const contentDiv = messageElement.querySelector('.text-sm.leading-relaxed');
        if (contentDiv) {
            contentDiv.innerHTML = parseMarkdown(content);
            scrollChatWidgetToBottom();
        }
    }
}

/**
 * Replace streaming message with final message
 */
function replaceStreamingMessage(streamingMessageId, finalMessage) {
    const messagesContainer = document.getElementById('chatWidgetMessages');
    const streamingElement = messagesContainer.querySelector(`[data-message-id="${streamingMessageId}"]`);
    
    if (streamingElement) {
        // Remove the streaming message
        streamingElement.remove();
        
        // Display the final message
        displayChatWidgetMessage(finalMessage);
    }
}
