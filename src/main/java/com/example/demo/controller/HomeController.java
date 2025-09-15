package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Flower Shop API!");
        response.put("status", "running");
        response.put("version", "1.0.0");
        response.put("endpoints", new String[]{
            "/api/",
            "/api/health",
            "/api/info"
        });
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("database", "connected");
        return response;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        Map<String, String> response = new HashMap<>();
        response.put("application", "Flower Shop System");
        response.put("description", "API for flower shop management");
        response.put("developer", "Your Team");
        return response;
    }
}
