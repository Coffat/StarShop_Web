package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for detecting Personally Identifiable Information (PII)
 * Used to determine if conversation should be handed off to staff
 */
@Service
@Slf4j
public class PiiDetectionService {

    // Regex patterns for PII detection
    private static final Pattern PHONE_PATTERN = Pattern.compile("0[0-9]{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(số\\s+\\d+|\\d+/\\d+|\\d+\\s+đường|\\d+\\s+phường|\\d+\\s+quận)", Pattern.CASE_INSENSITIVE);
    
    // Keywords indicating customer is providing their personal address (not asking about store)
    private static final String[] CUSTOMER_ADDRESS_INDICATORS = {
        "giao đến", "giao tới", "ship đến", "ship tới", "giao hàng đến", 
        "địa chỉ của tôi", "địa chỉ của mình", "địa chỉ nhận hàng",
        "tôi ở", "mình ở", "nhà tôi", "nhà mình"
    };
    
    // Keywords asking about store info (NOT PII)
    private static final String[] STORE_INFO_KEYWORDS = {
        "cửa hàng", "shop", "địa chỉ shop", "địa chỉ cửa hàng",
        "ở đâu", "chỗ nào", "bên nào", "hotline", "liên hệ",
        "thông tin", "giờ mở cửa", "giờ làm việc"
    };

    /**
     * Detect if message contains PII
     */
    public boolean containsPII(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        String lowerMessage = message.toLowerCase();
        
        // First check if customer is asking about STORE info (NOT PII)
        for (String keyword : STORE_INFO_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                log.debug("Customer asking about store info, not PII");
                return false; // This is asking about store, not providing personal info
            }
        }

        // Check for phone number (but not if asking about store)
        if (containsPhoneNumber(message)) {
            log.debug("PII detected: Phone number");
            return true;
        }

        // Check for email
        if (containsEmail(message)) {
            log.debug("PII detected: Email");
            return true;
        }

        // Check for detailed address ONLY if customer is providing their own address
        if (containsCustomerAddress(lowerMessage)) {
            log.debug("PII detected: Customer personal address");
            return true;
        }

        return false;
    }

    /**
     * Check if message contains phone number
     */
    public boolean containsPhoneNumber(String message) {
        Matcher matcher = PHONE_PATTERN.matcher(message);
        return matcher.find();
    }

    /**
     * Check if message contains email
     */
    public boolean containsEmail(String message) {
        Matcher matcher = EMAIL_PATTERN.matcher(message);
        return matcher.find();
    }

    /**
     * Check if message contains CUSTOMER's personal address
     * (not asking about store address)
     */
    public boolean containsCustomerAddress(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Check if customer is providing their own address
        boolean hasAddressIndicator = false;
        for (String indicator : CUSTOMER_ADDRESS_INDICATORS) {
            if (lowerMessage.contains(indicator)) {
                hasAddressIndicator = true;
                break;
            }
        }
        
        // Only flag as PII if customer is providing their address
        if (hasAddressIndicator) {
            // Check for address pattern (số 123, 123/45, etc.)
            Matcher matcher = ADDRESS_PATTERN.matcher(lowerMessage);
            return matcher.find();
        }
        
        return false;
    }
    
    /**
     * Check if message contains detailed address (old method for compatibility)
     */
    @Deprecated
    public boolean containsDetailedAddress(String message) {
        return containsCustomerAddress(message);
    }

    /**
     * Mask phone number in text for logging
     */
    public String maskPhoneNumber(String text) {
        if (text == null) {
            return null;
        }
        
        Matcher matcher = PHONE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String phone = matcher.group();
            String masked = phone.substring(0, 2) + "******" + phone.substring(8);
            matcher.appendReplacement(sb, masked);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * Mask email in text for logging
     */
    public String maskEmail(String text) {
        if (text == null) {
            return null;
        }
        
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String email = matcher.group();
            int atIndex = email.indexOf('@');
            if (atIndex > 2) {
                String masked = email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
                matcher.appendReplacement(sb, masked);
            } else {
                matcher.appendReplacement(sb, "***@" + email.substring(atIndex + 1));
            }
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * Mask all PII in text for logging
     */
    public String maskAllPII(String text) {
        if (text == null) {
            return null;
        }
        
        String masked = maskPhoneNumber(text);
        masked = maskEmail(masked);
        
        return masked;
    }

    /**
     * Get PII types detected in message
     */
    public String getPIITypes(String message) {
        if (message == null || message.isEmpty()) {
            return "NONE";
        }

        StringBuilder types = new StringBuilder();
        
        if (containsPhoneNumber(message)) {
            types.append("PHONE,");
        }
        if (containsEmail(message)) {
            types.append("EMAIL,");
        }
        if (containsDetailedAddress(message)) {
            types.append("ADDRESS,");
        }

        if (types.length() == 0) {
            return "NONE";
        }

        // Remove trailing comma
        return types.substring(0, types.length() - 1);
    }
}

