-- ====================================================================================
-- AI Chat System Integration
-- ====================================================================================
-- This script creates tables and functions for AI-powered chat system
-- with intelligent routing, handoff management, and analytics
-- Created: 2025-01-12
-- ====================================================================================

-- ====================================================================================
-- 1. Create routing_decisions table
-- ====================================================================================
CREATE TABLE IF NOT EXISTS routing_decisions (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES Conversations(id) ON DELETE CASCADE,
    message_id BIGINT REFERENCES Messages(id) ON DELETE SET NULL,
    intent VARCHAR(50) NOT NULL,
    confidence NUMERIC(4,3) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    need_handoff BOOLEAN NOT NULL DEFAULT FALSE,
    suggest_handoff BOOLEAN NOT NULL DEFAULT FALSE,
    handoff_reason VARCHAR(50),
    ai_response TEXT,
    tools_used TEXT[], -- Array of tool names used
    processing_time_ms INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_intent CHECK (intent IN ('SALES', 'SHIPPING', 'PROMOTION', 'ORDER_SUPPORT', 'PAYMENT', 'STORE_INFO', 'CHITCHAT', 'OTHER'))
);

COMMENT ON TABLE routing_decisions IS 'AI routing decisions and analysis history for chat messages';
COMMENT ON COLUMN routing_decisions.intent IS 'Detected intent type from AI analysis';
COMMENT ON COLUMN routing_decisions.confidence IS 'AI confidence score (0.0 to 1.0)';
COMMENT ON COLUMN routing_decisions.need_handoff IS 'Whether message requires staff intervention';
COMMENT ON COLUMN routing_decisions.suggest_handoff IS 'Whether AI suggests staff handoff';
COMMENT ON COLUMN routing_decisions.handoff_reason IS 'Reason for handoff: LOW_CONFIDENCE, PII_DETECTED, ORDER_INQUIRY, PAYMENT_ISSUE, EXPLICIT_REQUEST';
COMMENT ON COLUMN routing_decisions.tools_used IS 'Array of AI tools executed (product_search, shipping_fee, etc.)';

CREATE INDEX idx_routing_decisions_conversation ON routing_decisions(conversation_id);
CREATE INDEX idx_routing_decisions_intent ON routing_decisions(intent);
CREATE INDEX idx_routing_decisions_confidence ON routing_decisions(confidence);
CREATE INDEX idx_routing_decisions_created_at ON routing_decisions(created_at DESC);
CREATE INDEX idx_routing_decisions_need_handoff ON routing_decisions(need_handoff);

-- ====================================================================================
-- 2. Create handoff_queue table
-- ====================================================================================
CREATE TABLE IF NOT EXISTS handoff_queue (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT UNIQUE NOT NULL REFERENCES Conversations(id) ON DELETE CASCADE,
    priority INTEGER NOT NULL DEFAULT 0 CHECK (priority >= 0 AND priority <= 10),
    handoff_reason VARCHAR(50) NOT NULL,
    tags TEXT[] DEFAULT '{}',
    customer_message TEXT,
    ai_context TEXT, -- AI's analysis/context for staff
    enqueued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_at TIMESTAMPTZ,
    assigned_to_staff_id BIGINT REFERENCES Users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMPTZ,
    wait_time_seconds INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_handoff_reason CHECK (handoff_reason IN ('LOW_CONFIDENCE', 'PII_DETECTED', 'ORDER_INQUIRY', 'PAYMENT_ISSUE', 'EXPLICIT_REQUEST', 'AI_ERROR', 'COMPLEX_QUERY'))
);

COMMENT ON TABLE handoff_queue IS 'Queue of conversations waiting for or assigned to staff';
COMMENT ON COLUMN handoff_queue.priority IS 'Priority level (0=low, 5=normal, 10=urgent)';
COMMENT ON COLUMN handoff_queue.handoff_reason IS 'Reason why conversation was handed off to staff';
COMMENT ON COLUMN handoff_queue.tags IS 'Tags for categorization (e.g., payment, shipping, product)';
COMMENT ON COLUMN handoff_queue.ai_context IS 'AI analysis and context to help staff understand the situation';
COMMENT ON COLUMN handoff_queue.wait_time_seconds IS 'Time customer waited before staff assignment';

CREATE INDEX idx_handoff_queue_priority ON handoff_queue(priority DESC, enqueued_at ASC);
CREATE INDEX idx_handoff_queue_assigned_staff ON handoff_queue(assigned_to_staff_id);
CREATE INDEX idx_handoff_queue_enqueued_at ON handoff_queue(enqueued_at DESC);
CREATE INDEX idx_handoff_queue_reason ON handoff_queue(handoff_reason);

-- ====================================================================================
-- 3. Create staff_presence table
-- ====================================================================================
CREATE TABLE IF NOT EXISTS staff_presence (
    staff_id BIGINT PRIMARY KEY REFERENCES Users(id) ON DELETE CASCADE,
    online BOOLEAN NOT NULL DEFAULT FALSE,
    workload INTEGER NOT NULL DEFAULT 0 CHECK (workload >= 0),
    max_workload INTEGER NOT NULL DEFAULT 5 CHECK (max_workload > 0),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_activity_at TIMESTAMPTZ,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    status_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_status CHECK (status IN ('AVAILABLE', 'BUSY', 'AWAY', 'OFFLINE'))
);

COMMENT ON TABLE staff_presence IS 'Real-time presence and workload tracking for staff members';
COMMENT ON COLUMN staff_presence.online IS 'Whether staff is currently online';
COMMENT ON COLUMN staff_presence.workload IS 'Current number of active conversations assigned';
COMMENT ON COLUMN staff_presence.max_workload IS 'Maximum conversations staff can handle simultaneously';
COMMENT ON COLUMN staff_presence.status IS 'Current status: AVAILABLE, BUSY, AWAY, OFFLINE';

CREATE INDEX idx_staff_presence_online ON staff_presence(online);
CREATE INDEX idx_staff_presence_workload ON staff_presence(workload);
CREATE INDEX idx_staff_presence_status ON staff_presence(status);

-- ====================================================================================
-- 4. Create ai_chat_config table
-- ====================================================================================
CREATE TABLE IF NOT EXISTS ai_chat_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    config_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_config_type CHECK (config_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'TEXT'))
);

COMMENT ON TABLE ai_chat_config IS 'Configuration settings for AI chat system';
COMMENT ON COLUMN ai_chat_config.config_key IS 'Unique configuration key';
COMMENT ON COLUMN ai_chat_config.config_value IS 'Configuration value (stored as text)';
COMMENT ON COLUMN ai_chat_config.config_type IS 'Data type: STRING, NUMBER, BOOLEAN, JSON, TEXT';

CREATE INDEX idx_ai_chat_config_key ON ai_chat_config(config_key);
CREATE INDEX idx_ai_chat_config_active ON ai_chat_config(is_active);

-- ====================================================================================
-- 5. Insert default AI chat configuration
-- ====================================================================================

-- Store information
INSERT INTO ai_chat_config (config_key, config_value, config_type, description) VALUES
('store.name', 'StarShop - Hoa tươi cao cấp', 'STRING', 'Tên cửa hàng'),
('store.address', '01 Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP.HCM', 'STRING', 'Địa chỉ cửa hàng'),
('store.hotline', '1900 xxxx', 'STRING', 'Số hotline'),
('store.email', 'starshop.a.6868@gmail.com', 'STRING', 'Email liên hệ'),
('store.hours', '8:00 - 22:00 hàng ngày', 'STRING', 'Giờ mở cửa'),
('store.description', 'Chuyên cung cấp hoa tươi cao cấp, thiết kế theo yêu cầu, giao hàng nhanh toàn TP.HCM', 'TEXT', 'Mô tả cửa hàng');

-- AI behavior configuration
INSERT INTO ai_chat_config (config_key, config_value, config_type, description) VALUES
('ai.confidence_threshold_auto', '0.80', 'NUMBER', 'Ngưỡng confidence để AI tự động trả lời (≥0.80)'),
('ai.confidence_threshold_suggest', '0.65', 'NUMBER', 'Ngưỡng confidence để AI gợi ý chuyển staff (0.65-0.79)'),
('ai.confidence_threshold_handoff', '0.65', 'NUMBER', 'Ngưỡng confidence để bắt buộc chuyển staff (<0.65)'),
('ai.max_conversation_history', '10', 'NUMBER', 'Số tin nhắn tối đa trong lịch sử hội thoại gửi cho AI'),
('ai.response_timeout_seconds', '30', 'NUMBER', 'Timeout cho AI response (seconds)'),
('ai.enable_auto_handoff', 'true', 'BOOLEAN', 'Tự động chuyển staff khi cần thiết'),
('ai.enable_pii_detection', 'true', 'BOOLEAN', 'Bật phát hiện thông tin cá nhân'),
('ai.enable_product_suggestions', 'true', 'BOOLEAN', 'Bật gợi ý sản phẩm'),
('ai.max_product_suggestions', '3', 'NUMBER', 'Số sản phẩm tối đa gợi ý mỗi lần'),
('ai.default_shipping_weight', '500', 'NUMBER', 'Trọng lượng mặc định để tính phí ship (gram)');

-- AI personality and prompts
INSERT INTO ai_chat_config (config_key, config_value, config_type, description) VALUES
('ai.bot_name', 'Hoa AI', 'STRING', 'Tên của AI chatbot'),
('ai.bot_emoji', '🌸', 'STRING', 'Emoji đại diện cho bot'),
('ai.greeting_message', 'Xin chào! 👋 Mình là Hoa AI, trợ lý ảo của StarShop. Mình có thể giúp gì cho bạn hôm nay?', 'TEXT', 'Tin nhắn chào mừng'),
('ai.personality', 'Lịch sự, thân thiện, nhiệt tình, chuyên nghiệp. Sử dụng ngôn ngữ gần gũi nhưng tôn trọng khách hàng.', 'TEXT', 'Tính cách của AI');

-- Policies
INSERT INTO ai_chat_config (config_key, config_value, config_type, description) VALUES
('policy.shipping', 'Miễn phí ship đơn từ 500k trong bán kính 5km. Giao hàng trong 2-4 giờ. Hỗ trợ giao toàn TP.HCM.', 'TEXT', 'Chính sách vận chuyển'),
('policy.return', 'Đổi trả trong 24h nếu hoa không đúng mô tả hoặc bị héo. Hoàn tiền 100% nếu giao trễ quá 1 giờ.', 'TEXT', 'Chính sách đổi trả'),
('policy.payment', 'Hỗ trợ thanh toán: COD, MoMo, chuyển khoản ngân hàng, thẻ tín dụng.', 'TEXT', 'Phương thức thanh toán');

-- ====================================================================================
-- 6. Functions for AI chat system
-- ====================================================================================

-- Function to update staff workload
CREATE OR REPLACE FUNCTION update_staff_workload() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.assigned_staff_id IS NOT NULL THEN
        -- Update workload when conversation is assigned
        INSERT INTO staff_presence (staff_id, workload, last_activity_at)
        VALUES (NEW.assigned_staff_id, 1, now())
        ON CONFLICT (staff_id) DO UPDATE
        SET workload = staff_presence.workload + 1,
            last_activity_at = now(),
            updated_at = now();
    END IF;
    
    IF OLD.assigned_staff_id IS NOT NULL AND NEW.status = 'CLOSED' THEN
        -- Decrease workload when conversation is closed
        UPDATE staff_presence
        SET workload = GREATEST(workload - 1, 0),
            last_activity_at = now(),
            updated_at = now()
        WHERE staff_id = OLD.assigned_staff_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update staff workload
DROP TRIGGER IF EXISTS trg_update_staff_workload ON Conversations;
CREATE TRIGGER trg_update_staff_workload
    AFTER INSERT OR UPDATE ON Conversations
    FOR EACH ROW
    EXECUTE FUNCTION update_staff_workload();

-- Function to calculate wait time in handoff queue
CREATE OR REPLACE FUNCTION calculate_handoff_wait_time() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.assigned_at IS NOT NULL AND OLD.assigned_at IS NULL THEN
        NEW.wait_time_seconds = EXTRACT(EPOCH FROM (NEW.assigned_at - NEW.enqueued_at))::INTEGER;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to calculate wait time
DROP TRIGGER IF EXISTS trg_calculate_handoff_wait_time ON handoff_queue;
CREATE TRIGGER trg_calculate_handoff_wait_time
    BEFORE UPDATE ON handoff_queue
    FOR EACH ROW
    WHEN (NEW.assigned_at IS NOT NULL AND OLD.assigned_at IS NULL)
    EXECUTE FUNCTION calculate_handoff_wait_time();

-- Function to get AI chat statistics
CREATE OR REPLACE FUNCTION get_ai_chat_stats(
    start_date TIMESTAMPTZ DEFAULT now() - INTERVAL '7 days',
    end_date TIMESTAMPTZ DEFAULT now()
)
RETURNS TABLE(
    total_messages BIGINT,
    ai_handled BIGINT,
    staff_handoff BIGINT,
    ai_containment_rate NUMERIC,
    avg_confidence NUMERIC,
    avg_response_time_ms NUMERIC,
    top_intent VARCHAR,
    top_handoff_reason VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_messages,
        COUNT(*) FILTER (WHERE need_handoff = FALSE) as ai_handled,
        COUNT(*) FILTER (WHERE need_handoff = TRUE) as staff_handoff,
        ROUND(
            (COUNT(*) FILTER (WHERE need_handoff = FALSE)::NUMERIC / NULLIF(COUNT(*), 0)) * 100, 
            2
        ) as ai_containment_rate,
        ROUND(AVG(confidence), 3) as avg_confidence,
        ROUND(AVG(processing_time_ms), 2) as avg_response_time_ms,
        (
            SELECT intent 
            FROM routing_decisions 
            WHERE created_at BETWEEN start_date AND end_date
            GROUP BY intent 
            ORDER BY COUNT(*) DESC 
            LIMIT 1
        ) as top_intent,
        (
            SELECT handoff_reason 
            FROM routing_decisions 
            WHERE created_at BETWEEN start_date AND end_date 
            AND need_handoff = TRUE
            AND handoff_reason IS NOT NULL
            GROUP BY handoff_reason 
            ORDER BY COUNT(*) DESC 
            LIMIT 1
        ) as top_handoff_reason
    FROM routing_decisions
    WHERE created_at BETWEEN start_date AND end_date;
END;
$$ LANGUAGE plpgsql;

-- Function to get available staff for auto-assignment
CREATE OR REPLACE FUNCTION get_available_staff_for_assignment()
RETURNS TABLE(
    staff_id BIGINT,
    staff_name TEXT,
    current_workload INTEGER,
    max_workload INTEGER,
    availability_score NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id as staff_id,
        u.firstname || ' ' || u.lastname as staff_name,
        COALESCE(sp.workload, 0) as current_workload,
        COALESCE(sp.max_workload, 5) as max_workload,
        CASE 
            WHEN sp.online = TRUE AND sp.status = 'AVAILABLE' THEN
                (1.0 - (COALESCE(sp.workload, 0)::NUMERIC / COALESCE(sp.max_workload, 5))) * 100
            ELSE 0
        END as availability_score
    FROM Users u
    LEFT JOIN staff_presence sp ON u.id = sp.staff_id
    WHERE u.role = 'STAFF' 
    AND u.is_active = TRUE
    AND (sp.online = TRUE OR sp.online IS NULL)
    AND (sp.workload < sp.max_workload OR sp.workload IS NULL)
    ORDER BY availability_score DESC, COALESCE(sp.workload, 0) ASC;
END;
$$ LANGUAGE plpgsql;

-- Function to auto-assign conversation from handoff queue
CREATE OR REPLACE FUNCTION auto_assign_from_handoff_queue(queue_id_param BIGINT)
RETURNS BIGINT AS $$
DECLARE
    selected_staff_id BIGINT;
    conv_id BIGINT;
BEGIN
    -- Get the best available staff
    SELECT staff_id INTO selected_staff_id
    FROM get_available_staff_for_assignment()
    WHERE availability_score > 0
    LIMIT 1;
    
    IF selected_staff_id IS NOT NULL THEN
        -- Get conversation ID from queue
        SELECT conversation_id INTO conv_id
        FROM handoff_queue
        WHERE id = queue_id_param;
        
        -- Assign conversation to staff
        UPDATE Conversations
        SET assigned_staff_id = selected_staff_id,
            status = 'ASSIGNED',
            updated_at = now()
        WHERE id = conv_id;
        
        -- Update handoff queue
        UPDATE handoff_queue
        SET assigned_to_staff_id = selected_staff_id,
            assigned_at = now()
        WHERE id = queue_id_param;
        
        RETURN selected_staff_id;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================================
-- 7. Create views for reporting
-- ====================================================================================

-- View for AI performance metrics
CREATE OR REPLACE VIEW ai_performance_summary AS
SELECT 
    DATE(rd.created_at) as date,
    COUNT(*) as total_decisions,
    COUNT(*) FILTER (WHERE rd.need_handoff = FALSE) as ai_handled,
    COUNT(*) FILTER (WHERE rd.need_handoff = TRUE) as staff_handoff,
    ROUND(AVG(rd.confidence), 3) as avg_confidence,
    ROUND(AVG(rd.processing_time_ms), 2) as avg_processing_time_ms,
    COUNT(DISTINCT rd.conversation_id) as unique_conversations
FROM routing_decisions rd
GROUP BY DATE(rd.created_at)
ORDER BY date DESC;

COMMENT ON VIEW ai_performance_summary IS 'Daily AI performance metrics';

-- View for handoff queue status
CREATE OR REPLACE VIEW handoff_queue_status AS
SELECT 
    hq.id,
    hq.conversation_id,
    c.customer_id,
    customer.firstname || ' ' || customer.lastname as customer_name,
    hq.priority,
    hq.handoff_reason,
    hq.enqueued_at,
    hq.assigned_at,
    hq.assigned_to_staff_id,
    staff.firstname || ' ' || staff.lastname as staff_name,
    CASE 
        WHEN hq.resolved_at IS NOT NULL THEN 'RESOLVED'
        WHEN hq.assigned_at IS NOT NULL THEN 'ASSIGNED'
        ELSE 'WAITING'
    END as queue_status,
    EXTRACT(EPOCH FROM (COALESCE(hq.assigned_at, now()) - hq.enqueued_at))::INTEGER as wait_seconds
FROM handoff_queue hq
INNER JOIN Conversations c ON hq.conversation_id = c.id
INNER JOIN Users customer ON c.customer_id = customer.id
LEFT JOIN Users staff ON hq.assigned_to_staff_id = staff.id
ORDER BY hq.priority DESC, hq.enqueued_at ASC;

COMMENT ON VIEW handoff_queue_status IS 'Current status of handoff queue with customer and staff info';

-- ====================================================================================
-- 8. Grant permissions (if needed)
-- ====================================================================================

-- GRANT SELECT, INSERT, UPDATE ON routing_decisions TO your_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON handoff_queue TO your_app_user;
-- GRANT SELECT, INSERT, UPDATE ON staff_presence TO your_app_user;
-- GRANT SELECT, INSERT, UPDATE ON ai_chat_config TO your_app_user;

-- ====================================================================================
-- End of Script
-- ====================================================================================

