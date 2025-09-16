package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
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

    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("database", "connected");
        return response;
    }

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

