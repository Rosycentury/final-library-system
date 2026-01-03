package com.library.librarysystem.controller;

import com.library.librarysystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // 如果已登录，重定向到首页
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "auth/login";
    }
    
    /**
     * 处理登录请求
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        
        Map<String, Object> result = userService.login(username, password);
        
        if ((Boolean) result.get("success")) {
            // 登录成功，保存用户信息到session
            session.setAttribute("user", result.get("user"));
            session.setAttribute("username", username);
            session.setAttribute("userId", ((com.library.librarysystem.entity.User) result.get("user")).getId());
            session.setAttribute("role", ((com.library.librarysystem.entity.User) result.get("user")).getRole());
            
            return "redirect:/";
        } else {
            // 登录失败，显示错误信息
            model.addAttribute("error", result.get("message"));
            return "auth/login";
        }
    }
    
    /**
     * 显示注册页面
     */
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "auth/register";
    }
    
    /**
     * 处理注册请求
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String name,
            @RequestParam String email,
            Model model) {
        
        // 验证密码确认
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "auth/register";
        }
        
        // 调用服务层注册
        Map<String, Object> result = userService.register(username, password, name, email);
        
        if ((Boolean) result.get("success")) {
            model.addAttribute("success", "注册成功！请登录");
            return "auth/login";
        } else {
            model.addAttribute("error", result.get("message"));
            return "auth/register";
        }
    }
    
    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 销毁session
        return "redirect:/login";
    }
    
    /**
     * 显示个人中心
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        com.library.librarysystem.entity.User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "user/profile";
    }
}