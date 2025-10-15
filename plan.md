# AI Chat Upgrade Plan (2-phase, full scope)

## Scope

- Keep 2-phase pipeline:
- Phase 1: JSON analysis for routing only; never shown to users.
- Phase 2: Natural VN reply to user.
- Implement improvements per `phan_tich_cai_thien_chatAI.md`.

## Key Changes

### 1) Prompts

- Update `AiPromptService.generateSystemPrompt()`:
- Remove the “max 20 words” constraint; allow short greeting/ack.
- Keep JSON schema; emphasize reply is a short greeting/ack only.
- Update `AiPromptService.generateFinalResponsePrompt(...)`:
- Add “Độ dài linh hoạt” guidelines (chào: ~10–25; thông tin: ~30–60; tư vấn SP: ~100–150; chit-chat: ~60–90; tối đa 2 emoji).
- Require CTA only when intent = product_advice or chit-chat with open intent; omit in FAQ, greeting, or handoff. Avoid repetitiveness across replies.
- Only require product images when intent is product consulting; otherwise no image requirement.

### 2) Per-situation model parameters

- Add lightweight “profile” selector in `AiChatService` (or a new `AiGenerationProfileService`) mapping intent/context → {temperature, topP, maxTokens, timeout, streaming}:
- Greeting/Closing: temp 0.3–0.4, topP 0.8, maxTokens 128, timeout 10s.
- FAQ: temp 0.4, topP 0.85, maxTokens 256, timeout 12s.
- Chit-chat: temp 0.5, topP 0.9, maxTokens 384, timeout 15s.
- Product advice (+images): temp 0.65–0.7, topP 0.9, maxTokens 768–1024, timeout 20s.
- Handoff (PII/order): temp 0.2, topP 0.7, maxTokens 128, timeout 10s.
- Extend `GeminiClient` to accept per-call overrides (temperature, topP, maxTokens, timeout, streaming); `GeminiProperties` remain defaults.

### 3) Parallel tools + time-boxing

- In `AiToolExecutorService.executeTools(...)`:
- Run `product_search` and `shipping_fee` in parallel with 700–900ms time budget each; cancel pending tasks when timeout elapses.
- If shipping fee missing by timeout, include “Phí ship dự kiến … (xác nhận lại ngay khi có số)”.

### 4) Hot cache

- Introduce small in-memory caches with TTL:
- `ProductRecommendationCache` (2–5 minutes) for top/popular items and recent queries.
- `ShippingFeeCache` (10–30 minutes) for bracketed fee lookups.
- Integrate caches into tool executors; bypass network if warm hit.
- Add explicit invalidation hooks when flash sale/promo code events change.

### 5) Streaming + UX

- Add streaming generation path in `GeminiClient` (server-side streaming) and `AiChatService`:
- For Greeting/FAQ/Chit-chat/Product advice, stream tokens to WebSocket via `WebSocketService` as partial updates; send final consolidated message on completion.
- Frontend `static/js/chat-widget.js`:
- Render incremental message updates smoothly; keep a spinner until done.
- When streaming fallback triggers, immediately notify front-end to hide typing indicator.

### 6) Reliability

- Add 2–3 retries with short exponential backoff (0.4s, 0.8s, 1.6s) for Gemini calls and tool HTTP calls (idempotent).
- Keep overall phase-2 timeout aligned with profile.

### 7) Safety + handoff

- Keep current PII gating (already present). Ensure system prompt also flags PII/order/payment → handoff.
- Standardize handoff message per guideline.

### 8) Lightweight response QA

- After generation, run quick checks:
- Enforce CTA when required by intent rule above.
- If intent is product advice: ensure 2–3 items, include bold name, price, image markdown; otherwise keep as-is.
- If too long vs. profile target, truncate politely.

## Files To Update

- `src/main/java/com/example/demo/service/AiPromptService.java`
- `src/main/java/com/example/demo/service/AiChatService.java`
- `src/main/java/com/example/demo/client/GeminiClient.java` (add per-call overrides + streaming)
- `src/main/java/com/example/demo/config/GeminiProperties.java` (optional: add topP default)
- `src/main/java/com/example/demo/service/AiToolExecutorService.java` (parallel + cache + timeboxing)
- `src/main/java/com/example/demo/service/cache/` (new: `ProductRecommendationCache`, `ShippingFeeCache`)
- `src/main/resources/static/js/chat-widget.js` (incremental rendering + fallback hide typing)
- `src/main/resources/application.yml` (optional: gemini.top-p default; cache TTLs)

## Non-breaking Behavior

- If streaming not supported in env, fall back to non-streaming full reply and notify front-end to hide typing indicator.
- If profile missing, fall back to `GeminiProperties` defaults.

## Acceptance Criteria

- Phase 1 continues to return JSON strictly for routing; never rendered to user.
- Phase 2 uses per-situation parameters; responses match length rules, conditional CTA, and image rules by intent.
- Tool calls are parallelized with timeboxing; graceful fallback text when partial data missing.
- Cached hits reduce latency; logs show cache usage; cache invalidated on promo/flash sale changes.
- Streaming works in chat UI with partial updates, or clean fallback to single-shot with indicator hidden.
- Retries with backoff implemented; timeouts enforced.
- Handoff triggers with standardized message.
- Log latency (AI + tools), cache hits, retry count, and token usage per call.

## To-dos

- [ ] Revise system and final prompts per flexible-length and conditional CTA rules
- [ ] Add per-situation generation profiles and selector
- [ ] Extend Gemini client for per-call params and streaming
- [ ] Execute tools in parallel with 700–900ms timeboxing
- [ ] Add product/shipping caches with TTL and integrate; add invalidation hooks
- [ ] Enable incremental message rendering in chat-widget.js; hide indicator on fallback
- [ ] Add retries with short exponential backoff for AI/tools
- [ ] Add post-gen checks (CTA, item count, length, images when needed)
- [ ] Add logging for latency, cache hits, retries, token usage