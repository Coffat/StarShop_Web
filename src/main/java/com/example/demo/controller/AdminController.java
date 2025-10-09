package com.example.demo.controller;

import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
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
        
        // Get chart data
        Map<String, Object> revenueChartData = dashboardService.getRevenueChartData();
        model.addAttribute("revenueChart", revenueChartData);
        
        Map<String, Object> orderStatusChartData = dashboardService.getOrderStatusChartData();
        model.addAttribute("orderStatusChart", orderStatusChartData);
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Đơn hàng
     */
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("pageTitle", "Quản lý Đơn hàng");
        model.addAttribute("contentTemplate", "admin/orders/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Đơn hàng", "/admin/orders"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Sản phẩm
     */
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "Quản lý Sản phẩm");
        model.addAttribute("contentTemplate", "admin/products/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Sản phẩm", "/admin/products"));
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
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Voucher", "/admin/vouchers"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Tài chính
     */
    @GetMapping("/finance")
    public String finance(Model model) {
        model.addAttribute("pageTitle", "Quản lý Tài chính");
        model.addAttribute("contentTemplate", "admin/finance/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Tài chính", "/admin/finance"));
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layouts/admin";
    }

    /**
     * Quản lý Người dùng
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Quản lý Người dùng");
        model.addAttribute("contentTemplate", "admin/users/index");
        
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbItem("Dashboard", "/admin/dashboard"));
        breadcrumbs.add(new BreadcrumbItem("Quản lý Người dùng", "/admin/users"));
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
}
