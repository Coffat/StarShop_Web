package com.example.demo.controller;

import com.example.demo.dto.StaffDashboardDTO;
import com.example.demo.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Staff Portal Controller
 * Serves web pages for staff members
 * Following rules.mdc specifications for MVC pattern
 */
@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
@RequiredArgsConstructor
@Slf4j
public class StaffController extends BaseController {

    private final StaffService staffService;

    /**
     * Staff dashboard page
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        try {
            Long staffId = getUserIdFromAuthentication(authentication);
            if (staffId == null) {
                return "redirect:/login";
            }
            log.info("Loading staff dashboard for staff ID: {}", staffId);
            
            // Get dashboard data
            StaffDashboardDTO dashboard = staffService.getStaffDashboard(staffId);
            
            model.addAttribute("pageTitle", "Dashboard - Staff Portal");
            model.addAttribute("currentPath", "/staff/dashboard");
            model.addAttribute("dashboard", dashboard);
            model.addAttribute("staffId", staffId);  // Required for WebSocket and notifications
            model.addAttribute("contentTemplate", "staff/dashboard/index");
            
            return "layouts/staff";
            
        } catch (Exception e) {
            log.error("Error loading staff dashboard", e);
            model.addAttribute("error", "Không thể tải trang dashboard");
            return "error/500";
        }
    }

    /**
     * Staff chat interface page
     */
    @GetMapping("/chat")
    public String chat(Model model, Authentication authentication) {
        try {
            log.info("Chat page requested, authentication: {}", authentication != null ? authentication.getName() : "null");
            Long staffId = getUserIdFromAuthentication(authentication);
            log.info("Retrieved staff ID: {}", staffId);
            if (staffId == null) {
                log.warn("Staff ID is null, redirecting to login");
                return "redirect:/login";
            }
            log.info("Loading chat interface for staff ID: {}", staffId);
            log.info("📤 Passing staffId to template: {}", staffId);
            
            model.addAttribute("pageTitle", "Hỗ trợ khách hàng - Staff Portal");
            model.addAttribute("currentPath", "/staff/chat");
            model.addAttribute("staffId", staffId);
            model.addAttribute("contentTemplate", "staff/chat/index");
            
            return "layouts/staff";
            
        } catch (Exception e) {
            log.error("Error loading chat interface", e);
            model.addAttribute("error", "Không thể tải trang chat");
            return "error/500";
        }
    }

    /**
     * Staff timesheet management page
     */
    @GetMapping("/timesheet")
    public String timesheet(Model model, Authentication authentication) {
        try {
            Long staffId = getUserIdFromAuthentication(authentication);
            if (staffId == null) {
                return "redirect:/login";
            }
            log.info("Loading timesheet page for staff ID: {}", staffId);
            
            model.addAttribute("pageTitle", "Chấm công - Staff Portal");
            model.addAttribute("currentPath", "/staff/timesheet");
            model.addAttribute("staffId", staffId);
            model.addAttribute("contentTemplate", "staff/timesheet/index");
            
            return "layouts/staff";
            
        } catch (Exception e) {
            log.error("Error loading timesheet page", e);
            model.addAttribute("error", "Không thể tải trang chấm công");
            return "error/500";
        }
    }

    /**
     * Staff profile page
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        try {
            Long staffId = getUserIdFromAuthentication(authentication);
            if (staffId == null) {
                return "redirect:/login";
            }
            log.info("Loading profile page for staff ID: {}", staffId);
            
            // Get dashboard data for profile statistics
            StaffDashboardDTO dashboard = staffService.getStaffDashboard(staffId);
            
            model.addAttribute("pageTitle", "Thông tin cá nhân - Staff Portal");
            model.addAttribute("currentPath", "/staff/profile");
            model.addAttribute("contentTemplate", "staff/profile/index");
            model.addAttribute("dashboard", dashboard);
            model.addAttribute("staffId", staffId);
            
            return "layouts/staff";
            
        } catch (Exception e) {
            log.error("Error loading profile page", e);
            model.addAttribute("error", "Không thể tải trang thông tin");
            return "error/500";
        }
    }

    /**
     * Staff reviews management page
     */
    @GetMapping("/reviews")
    public String reviews(Model model, Authentication authentication) {
        try {
            Long staffId = getUserIdFromAuthentication(authentication);
            if (staffId == null) {
                return "redirect:/login";
            }
            log.info("Loading reviews page for staff ID: {}", staffId);
            
            model.addAttribute("pageTitle", "Quản lý Đánh giá - Staff Portal");
            model.addAttribute("currentPath", "/staff/reviews");
            model.addAttribute("contentTemplate", "staff/reviews/index");
            model.addAttribute("staffId", staffId);
            
            return "layouts/staff";
            
        } catch (Exception e) {
            log.error("Error loading reviews page", e);
            model.addAttribute("error", "Không thể tải trang đánh giá");
            return "error/500";
        }
    }
}

