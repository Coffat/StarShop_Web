package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Web Controller for serving Thymeleaf templates
 * Following rules.mdc specifications for MVC pattern
 */
@Controller
@Slf4j
public class WebController {

    /**
     * Serve login page
     * @param error Error message if any
     * @param success Success message if any
     * @param model Spring Model
     * @return login template name
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "success", required = false) String success,
                          Model model) {
        
        log.info("Serving login page with error: {}, success: {}", error, success);
        
        if (error != null) {
            switch (error) {
                case "oauth2":
                    model.addAttribute("error", "OAuth2 authentication failed. Please try again.");
                    break;
                case "invalid":
                    model.addAttribute("error", "Invalid email or password.");
                    break;
                case "expired":
                    model.addAttribute("error", "Your session has expired. Please login again.");
                    break;
                default:
                    model.addAttribute("error", "Login failed. Please try again.");
            }
        }
        
        if (success != null) {
            switch (success) {
                case "logout":
                    model.addAttribute("success", "You have been logged out successfully.");
                    break;
                case "register":
                    model.addAttribute("success", "Registration successful! Please login with your credentials.");
                    break;
                default:
                    model.addAttribute("success", success);
            }
        }
        
        return "login";
    }


    /**
     * Dashboard page (after successful login)
     * @param model Spring Model
     * @return dashboard template name
     */
    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        log.info("Dashboard page accessed");
        model.addAttribute("message", "Welcome to StarShop Dashboard!");
        return "dashboard";
    }
}
