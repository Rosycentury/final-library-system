package com.library.librarysystem.controller;

import com.library.librarysystem.entity.User;
import com.library.librarysystem.service.UserService;
import com.library.librarysystem.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowService borrowService;

    /**
     * 管理员仪表板 - 修复版本
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("=== 管理员仪表板访问开始 ===");
        
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }

            // 获取统计数据
            var users = userService.getAllUsers();
            var readers = userService.getAllReaders();
            var admins = userService.getAllAdmins();
            var borrowStats = borrowService.getBorrowStatistics();
            
            // 设置模型属性
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalReaders", readers.size());
            model.addAttribute("totalAdmins", admins.size());
            model.addAttribute("borrowStats", borrowStats);
            
            System.out.println("仪表板统计数据:");
            System.out.println("  总用户数: " + users.size());
            System.out.println("  读者数: " + readers.size());
            System.out.println("  管理员数: " + admins.size());
            
            System.out.println("=== 管理员仪表板访问结束 ===");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            System.err.println("管理员仪表板加载失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "加载仪表板失败: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    /**
     * 用户管理页面 - 修复版本（添加预计算统计信息）
     */
    @GetMapping("/users")
    public String userManagement(HttpSession session, Model model) {
        System.out.println("=== 用户管理页面访问开始 ===");
        
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }

            // 获取所有用户
            var users = userService.getAllUsers();
            
            // 预计算统计信息（避免在模板中使用复杂的SpringEL表达式）
            int totalReaders = 0;
            int totalAdmins = 0;
            int activeUsers = 0;
            
            for (User user : users) {
                String userRole = user.getRole();
                if ("READER".equals(userRole)) {
                    totalReaders++;
                } else if ("ADMIN".equals(userRole)) {
                    totalAdmins++;
                }
                
                // 如果用户有状态字段，可以统计活跃用户
                // if ("ACTIVE".equals(user.getStatus())) {
                //     activeUsers++;
                // }
            }
            
            // 使用Java 8 Stream API的备选方案（更简洁）
            // long totalReaders = users.stream()
            //     .filter(u -> "READER".equals(u.getRole()))
            //     .count();
            // long totalAdmins = users.stream()
            //     .filter(u -> "ADMIN".equals(u.getRole()))
            //     .count();
            
            // 设置模型属性
            model.addAttribute("users", users);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalReaders", totalReaders);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("loaded", true);
            
            System.out.println("用户管理页面加载完成:");
            System.out.println("  用户数量: " + users.size());
            System.out.println("  读者数量: " + totalReaders);
            System.out.println("  管理员数量: " + totalAdmins);
            System.out.println("  第一个用户: " + (users.isEmpty() ? "无" : users.get(0).getUsername()));
            
            System.out.println("=== 用户管理页面访问结束 ===");
            
            return "admin/users";
            
        } catch (Exception e) {
            System.err.println("用户管理页面加载失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "加载用户列表失败: " + e.getMessage());
            model.addAttribute("users", new java.util.ArrayList<>());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalReaders", 0);
            model.addAttribute("totalAdmins", 0);
            model.addAttribute("loaded", true);
            return "admin/users";
        }
    }

    /**
     * 查看用户详情 - 修复版本
     */
    @GetMapping("/users/{id}")
    public String viewUser(
            @PathVariable String id,
            HttpSession session,
            Model model) {
        
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }

            User user = userService.getUserById(id);
            if (user == null) {
                model.addAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }

            model.addAttribute("user", user);
            
            // 获取用户的借阅记录
            var borrowRecords = borrowService.getUserBorrowRecords(id);
            model.addAttribute("borrowRecords", borrowRecords);
            
            return "admin/user-detail";
            
        } catch (Exception e) {
            System.err.println("查看用户详情失败: " + e.getMessage());
            model.addAttribute("error", "加载用户详情失败: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * 添加新用户（管理员）- 修复版本
     */
    @PostMapping("/users/add")
    public String addUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String role,
            HttpSession session,
            Model model) {
        
        try {
            // 检查权限
            String adminRole = (String) session.getAttribute("role");
            if (!"ADMIN".equals(adminRole)) {
                model.addAttribute("error", "无权限操作");
                return "redirect:/";
            }

            System.out.println("添加新用户: " + username + ", 角色: " + role);
            
            Map<String, Object> result = userService.register(username, password, name, email);
            
            if ((Boolean) result.get("success")) {
                // 更新角色
                User user = (User) result.get("user");
                user.setRole(role);
                userService.updateUser(user);
                
                model.addAttribute("success", "用户添加成功");
                System.out.println("用户添加成功: " + username);
            } else {
                model.addAttribute("error", result.get("message"));
                System.err.println("用户添加失败: " + result.get("message"));
            }
            
        } catch (Exception e) {
            System.err.println("添加用户异常: " + e.getMessage());
            model.addAttribute("error", "添加用户失败: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    /**
     * 删除用户 - 修复版本
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable String id,
            HttpSession session,
            Model model) {
        
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限操作");
                return "redirect:/";
            }

            boolean success = userService.deleteUser(id);
            
            if (success) {
                model.addAttribute("success", "用户删除成功");
            } else {
                model.addAttribute("error", "删除失败：用户不存在");
            }
            
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            model.addAttribute("error", "删除失败：" + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    /**
     * 所有借阅记录 - 修复版本
     */
    @GetMapping("/all-borrows")
    public String allBorrowRecords(HttpSession session, Model model) {
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }

            var records = borrowService.getAllBorrowRecords();
            model.addAttribute("records", records);
            
            return "admin/all-borrows";
            
        } catch (Exception e) {
            System.err.println("加载借阅记录失败: " + e.getMessage());
            model.addAttribute("error", "加载借阅记录失败: " + e.getMessage());
            return "admin/all-borrows";
        }
    }

    /**
     * 逾期记录管理 - 修复版本
     */
    @GetMapping("/overdue")
    public String overdueManagement(HttpSession session, Model model) {
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }

            var overdueRecords = borrowService.getOverdueRecords();
            model.addAttribute("overdueRecords", overdueRecords);
            model.addAttribute("overdueCount", overdueRecords.size());
            
            // 计算超时天数
            LocalDateTime now = LocalDateTime.now();
            Map<String, Long> overdueDaysMap = new HashMap<>();
            int overdue7Days = 0;
            int overdue30Days = 0;
            
            for (var record : overdueRecords) {
                long daysOverdue = java.time.Duration.between(record.getDueDate(), now).toDays();
                overdueDaysMap.put(record.getId(), daysOverdue);
                
                if (daysOverdue > 7) overdue7Days++;
                if (daysOverdue > 30) overdue30Days++;
            }
            
            model.addAttribute("overdueDaysMap", overdueDaysMap);
            model.addAttribute("overdue7Days", overdue7Days);
            model.addAttribute("overdue30Days", overdue30Days);
            
            return "admin/overdue";
            
        } catch (Exception e) {
            System.err.println("加载逾期记录失败: " + e.getMessage());
            model.addAttribute("error", "加载逾期记录失败: " + e.getMessage());
            return "admin/overdue";
        }
    }
    
    /**
     * 数据库诊断页面
     */
    @GetMapping("/diagnose")
    public String diagnoseDatabase(HttpSession session, Model model) {
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }
            
            StringBuilder diagnoseInfo = new StringBuilder();
            diagnoseInfo.append("=== 数据库诊断信息 ===\n\n");
            
            // 用户统计
            long totalUsers = userService.getUserCount();
            List<User> allUsers = userService.getAllUsers();
            List<User> readers = userService.getAllReaders();
            List<User> admins = userService.getAllAdmins();
            
            diagnoseInfo.append("1. 用户统计:\n");
            diagnoseInfo.append("   总用户数: ").append(totalUsers).append("\n");
            diagnoseInfo.append("   读者数量: ").append(readers.size()).append("\n");
            diagnoseInfo.append("   管理员数量: ").append(admins.size()).append("\n\n");
            
            // 用户列表
            diagnoseInfo.append("2. 用户列表:\n");
            if (allUsers.isEmpty()) {
                diagnoseInfo.append("   警告: 没有用户数据!\n");
            } else {
                for (User user : allUsers) {
                    diagnoseInfo.append("   - ").append(user.getUsername())
                                .append(" (").append(user.getName()).append(")")
                                .append(" - 角色: ").append(user.getRole())
                                .append(" - ID: ").append(user.getId()).append("\n");
                }
            }
            
            model.addAttribute("diagnoseInfo", diagnoseInfo.toString());
            return "admin/diagnose";
            
        } catch (Exception e) {
            model.addAttribute("error", "诊断失败: " + e.getMessage());
            return "admin/diagnose";
        }
    }
    
    /**
     * 搜索用户（新增功能）
     */
    @GetMapping("/users/search")
    public String searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleFilter,
            HttpSession session,
            Model model) {
        
        try {
            // 检查权限
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }
            
            List<User> searchResults;
            
            if ((keyword == null || keyword.trim().isEmpty()) && 
                (roleFilter == null || roleFilter.trim().isEmpty())) {
                // 没有搜索条件，返回所有用户
                searchResults = userService.getAllUsers();
            } else {
                // 执行搜索
                searchResults = userService.searchUsers(keyword, roleFilter);
            }
            
            // 预计算统计信息
            int totalReaders = 0;
            int totalAdmins = 0;
            
            for (User user : searchResults) {
                if ("READER".equals(user.getRole())) {
                    totalReaders++;
                } else if ("ADMIN".equals(user.getRole())) {
                    totalAdmins++;
                }
            }
            
            model.addAttribute("users", searchResults);
            model.addAttribute("totalUsers", searchResults.size());
            model.addAttribute("totalReaders", totalReaders);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("keyword", keyword);
            model.addAttribute("roleFilter", roleFilter);
            model.addAttribute("loaded", true);
            
            return "admin/users";
            
        } catch (Exception e) {
            System.err.println("搜索用户失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "搜索用户失败: " + e.getMessage());
            model.addAttribute("users", new java.util.ArrayList<>());
            model.addAttribute("loaded", true);
            return "admin/users";
        }
    }
}