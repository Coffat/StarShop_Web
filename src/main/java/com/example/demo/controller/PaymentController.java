package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import com.example.demo.config.MoMoProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/payment/momo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "💳 Payment", description = "Payment processing APIs - MoMo integration")
public class PaymentController {

	private final OrderRepository orderRepository;
	private final OrderService orderService;
	private final MoMoProperties momoProperties;

	@Operation(
		summary = "MoMo callback endpoint",
		description = "Endpoint nhận callback từ MoMo sau khi user thanh toán xong. Tự động cập nhật trạng thái đơn hàng và redirect về trang kết quả."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Redirect đến trang kết quả thanh toán"),
		@ApiResponse(responseCode = "400", description = "Chữ ký không hợp lệ hoặc không tìm thấy đơn hàng")
	})
	@GetMapping("/return")
	public String momoReturn(
			@Parameter(description = "MoMo callback parameters", hidden = true)
			HttpServletRequest request,
			org.springframework.ui.Model model) {
		Map<String, String[]> params = request.getParameterMap();
		Map<String, String> flat = new TreeMap<>();
		params.forEach((k, v) -> flat.put(k, v != null && v.length > 0 ? v[0] : ""));
		log.info("MoMo return params: {}", flat);

		String signature = flat.getOrDefault("signature", "");
		String raw = buildRawSignature(flat);
		if (!verifySignature(raw, signature, momoProperties.getSecretKey())) {
			log.error("Invalid MoMo signature");
			model.addAttribute("success", false);
			model.addAttribute("message", "Chữ ký thanh toán không hợp lệ. Vui lòng liên hệ hỗ trợ.");
			model.addAttribute("pageTitle", "Lỗi thanh toán - StarShop");
			return "orders/payment-result";
		}

		String orderId = flat.get("orderId");
		String resultCode = flat.get("resultCode");
		String transId = flat.get("transId");
		String message = flat.get("message");
		
		Optional<Order> orderOpt = orderRepository.findById(parseOrderId(orderId));
		if (orderOpt.isEmpty()) {
			log.error("Order not found: {}", orderId);
			model.addAttribute("success", false);
			model.addAttribute("message", "Không tìm thấy đơn hàng. Vui lòng liên hệ hỗ trợ.");
			model.addAttribute("pageTitle", "Lỗi thanh toán - StarShop");
			return "orders/payment-result";
		}
		Order order = orderOpt.get();

		// Check payment result
		if ("0".equals(resultCode)) {
			// Payment successful
			log.info("MoMo payment successful for order {}", order.getId());
			order.setStatus(OrderStatus.PROCESSING);
			orderRepository.save(order);
			
			model.addAttribute("success", true);
			model.addAttribute("message", "Thanh toán thành công! Đơn hàng của bạn đang được xử lý.");
			model.addAttribute("order", OrderDTO.fromOrder(order));
			model.addAttribute("transactionId", transId);
			model.addAttribute("pageTitle", "Thanh toán thành công - StarShop");
		} else {
			// Payment failed - cancel order and restore stock
			log.warn("MoMo payment failed for order {} with resultCode: {}, message: {}", order.getId(), resultCode, message);
			
			// Use OrderService to properly cancel order and restore stock
			orderService.cancelOrderByPaymentFailure(order.getId());
			
			// Reload order to get updated status
			order = orderRepository.findById(order.getId()).orElse(order);
			
			model.addAttribute("success", false);
			model.addAttribute("message", "Thanh toán thất bại: " + (message != null ? message : "Vui lòng thử lại."));
			model.addAttribute("order", OrderDTO.fromOrder(order));
			model.addAttribute("transactionId", transId);
			model.addAttribute("pageTitle", "Thanh toán thất bại - StarShop");
		}

		return "orders/payment-result";
	}

	@Operation(
		summary = "MoMo IPN (Instant Payment Notification)",
		description = "Endpoint nhận thông báo thanh toán từ MoMo server. Dùng cho server-to-server notification."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "IPN được xử lý")
	})
	@PostMapping("/notify")
	@ResponseBody
	public Map<String, Object> momoNotify(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
				description = "MoMo notification payload",
				required = true
			)
			@RequestBody Map<String, Object> body) {
		log.info("MoMo notify body: {}", body);
		// In sandbox, MoMo may not post notify. Here we accept and respond success.
		Map<String, Object> resp = new HashMap<>();
		resp.put("resultCode", 0);
		resp.put("message", "success");
		return resp;
	}

	private String buildRawSignature(Map<String, String> params) {
		// Follow MoMo signature fields ordering for return
		String[] keys = new String[]{"accessKey","amount","extraData","message","orderId","orderInfo","orderType","partnerCode","payType","requestId","responseTime","resultCode","transId"};
		StringBuilder sb = new StringBuilder();
		for (String key : keys) {
			if (params.containsKey(key)) {
				if (sb.length() > 0) sb.append('&');
				sb.append(key).append('=').append(params.get(key));
			}
		}
		return sb.toString();
	}

	private boolean verifySignature(String raw, String signature, String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			mac.init(secretKeySpec);
			byte[] hmac = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
			String calc = bytesToHex(hmac);
			return calc.equals(signature);
		} catch (Exception e) {
			log.error("Verify signature error: {}", e.getMessage());
			return false;
		}
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder(2 * bytes.length);
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private Long parseOrderId(String orderId) {
		try {
			// Expect pattern like ORDER-<id>-<timestamp>
			if (orderId != null && orderId.startsWith("ORDER-")) {
				String[] parts = orderId.split("-");
				return Long.parseLong(parts[1]);
			}
			return Long.parseLong(orderId);
		} catch (Exception e) {
			return -1L;
		}
	}
}
