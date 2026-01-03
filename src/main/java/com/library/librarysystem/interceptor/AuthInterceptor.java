package com.library.librarysystem.interceptor;

import org.springframework.stereotype.Component;


import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        
        String uri = request.getRequestURI();
        
        // 公开访问的路径
        if (uri.startsWith("/login") || 
            uri.startsWith("/register") || 
            uri.startsWith("/css/") || 
            uri.startsWith("/js/") || 
            uri.startsWith("/images/") || 
            uri.equals("/") ||
            uri.startsWith("/test") ||
            uri.startsWith("/api/")) {
            return true;
        }
        
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");
        
        // 检查是否登录
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }
        
        // 检查管理员权限
        if (uri.startsWith("/admin") || uri.startsWith("/books/add") || uri.startsWith("/books/edit")) {
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                response.sendRedirect("/");
                return false;
            }
        }
        
        return true;
    }
}