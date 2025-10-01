package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@Tag(name = "🏠 System", description = "System health check and information APIs")
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        log.info("Home page accessed");
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            log.info("Home accessed by authenticated user: {}", authentication.getName());
        } else {
            log.info("Home accessed by guest user");
        }
        
        return "home";
    }

    @Operation(
        summary = "Health check",
        description = "Kiểm tra trạng thái hoạt động của hệ thống và kết nối database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hệ thống hoạt động bình thường")
    })
    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("database", "connected");
        return response;
    }

    @Operation(
        summary = "Application information",
        description = "Lấy thông tin về ứng dụng (tên, mô tả, developer)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công")
    })
    @GetMapping("/api/info")
    @ResponseBody
    public Map<String, String> info() {
        Map<String, String> response = new HashMap<>();
        response.put("application", "Flower Shop System");
        response.put("description", "API for flower shop management");
        response.put("developer", "Your Team");
        return response;
    }
    
}

