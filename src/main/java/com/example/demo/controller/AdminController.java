package com.example.demo.controller;

import com.example.demo.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController extends BaseController {

    @Autowired
    private DashboardService dashboardService;


    /**
     * Dashboard - Trang chủ admin
     */
    @GetMapping({"", "/", "/dashboard"})
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("contentTemplate", "admin/dashboard/index");
        
        // Get dashboard statistics
        Map<String, Object> dashboardStats = dashboardService.getDashboardStats();
        model.addAttribute("stats", dashboardStats);
        
        // Get chart data for main correlation chart (7 days default)
        Map<String, Object> correlationData = dashboardService.getCorrelationChartData(7);
        model.addAttribute("correlationChart", correlationData);
        
        // Get revenue trend data for side chart (7 days)
        Map<String, Object> revenueTrendData = dashboardService.getRevenueTrendData(7);
        model.addAttribute("revenueTrend", revenueTrendData);
        
        // Get order status chart data for side chart
        Map<String, Object> orderStatusChartData = dashboardService.getOrderStatusChartData();
        model.addAttribute("orderStatusChart", orderStatusChartData);
        
        // Legacy chart data for backward compatibility
        Map<String, Object> revenueChartData = dashboardService.getRevenueChartData();
        model.addAttribute("revenueChart", revenueChartData);
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }



    /**
     * Quản lý Voucher
     */
    @GetMapping("/vouchers")
    public String vouchers(Model model) {
        model.addAttribute("pageTitle", "Quản lý Voucher");
        model.addAttribute("contentTemplate", "admin/vouchers/index");
        model.addAttribute("currentPath", "/admin/vouchers");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Voucher", "/admin/vouchers"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Nhân viên
     */
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("pageTitle", "Quản lý Nhân viên");
        model.addAttribute("contentTemplate", "admin/employees/index");
        model.addAttribute("currentPath", "/admin/employees");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Nhân viên", "/admin/employees"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Người dùng (Khách hàng)
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Quản lý Khách hàng");
        model.addAttribute("contentTemplate", "admin/users/index");
        model.addAttribute("currentPath", "/admin/users");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Khách hàng", "/admin/users"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Nội dung
     */
    @GetMapping("/content")
    public String content(Model model) {
        model.addAttribute("pageTitle", "Quản lý Nội dung");
        model.addAttribute("contentTemplate", "admin/content/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Nội dung", "/admin/content"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Báo cáo
     */
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("pageTitle", "Báo cáo");
        model.addAttribute("contentTemplate", "admin/reports/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Báo cáo", "/admin/reports"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Danh mục
     */
    @GetMapping("/catalogs")
    public String catalogs(Model model) {
        model.addAttribute("pageTitle", "Quản lý Danh mục");
        model.addAttribute("contentTemplate", "admin/catalogs/index");
        model.addAttribute("currentPath", "/admin/catalogs");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Danh mục", "/admin/catalogs"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Cài đặt Hệ thống
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Cài đặt Hệ thống");
        model.addAttribute("contentTemplate", "admin/settings/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Cài đặt Hệ thống", "/admin/settings"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    // ==================== REST API ENDPOINTS ====================
    
    /**
     * API: Get correlation chart data by period
     */
    @GetMapping("/api/correlation-data")
    @ResponseBody
    public Map<String, Object> getCorrelationData(@RequestParam(defaultValue = "7") int period) {
        return dashboardService.getCorrelationChartData(period);
    }
    
    /**
     * API: Get revenue trend data by period
     */
    @GetMapping("/api/revenue-trend")
    @ResponseBody
    public Map<String, Object> getRevenueTrend(@RequestParam(defaultValue = "7") int period) {
        return dashboardService.getRevenueTrendData(period);
    }
    
    /**
     * API: Get order status chart data
     */
    @GetMapping("/api/order-status")
    @ResponseBody
    public Map<String, Object> getOrderStatus() {
        return dashboardService.getOrderStatusChartData();
    }
    
    /**
     * API: Get dashboard statistics
     */
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }
    
}
