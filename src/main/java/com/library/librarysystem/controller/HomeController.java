package com.library.librarysystem.controller;

import com.library.librarysystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        System.out.println("=== 访问首页开始 ===");
        
        try {
            Object user = session.getAttribute("user");
            if (user != null) {
                String username = (String) session.getAttribute("username");
                String role = (String) session.getAttribute("role");
                String userId = (String) session.getAttribute("userId");
                
                System.out.println("用户已登录: username=" + username + ", role=" + role + ", userId=" + userId);
                
                model.addAttribute("username", username);
                model.addAttribute("role", role);
                model.addAttribute("userId", userId);
                
                // 获取用户基本信息
                if (userId != null && !userId.trim().isEmpty()) {
                    try {
                        var userEntity = userService.getUserById(userId);
                        if (userEntity != null) {
                            model.addAttribute("userEntity", userEntity);
                            model.addAttribute("maxBorrow", userEntity.getMaxBorrow());
                        } else {
                            model.addAttribute("maxBorrow", 5); // 默认值
                        }
                    } catch (Exception e) {
                        System.err.println("获取用户信息失败: " + e.getMessage());
                        model.addAttribute("maxBorrow", 5); // 默认值
                    }
                } else {
                    model.addAttribute("maxBorrow", 5); // 默认值
                }
                
                // 设置加载标志
                model.addAttribute("loaded", true);
                
                if ("ADMIN".equals(role)) {
                    System.out.println("跳转到管理员首页");
                    return "admin/home";
                } else {
                    System.out.println("跳转到用户首页");
                    return "user/home-v2"; // 使用AJAX版本
                }
            } else {
                System.out.println("用户未登录，跳转到公共首页");
                return "index";
            }
        } catch (Exception e) {
            System.err.println("首页加载出现异常: " + e.getMessage());
            e.printStackTrace();
            // 即使出错也返回一个简单页面
            model.addAttribute("error", "页面加载出现错误，请重试");
            return "index";
        }
    }

    /**
     * 测试用的简单首页
     */
    @GetMapping("/home-test")
    public String homeTest(HttpSession session, Model model) {
        model.addAttribute("username", "测试用户");
        model.addAttribute("role", "READER");
        model.addAttribute("loaded", true);
        return "user/home-simple";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }
}