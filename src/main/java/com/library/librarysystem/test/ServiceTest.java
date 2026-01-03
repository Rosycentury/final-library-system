package com.library.librarysystem.test;

import com.library.librarysystem.service.UserService;
import com.library.librarysystem.service.BookService;
import com.library.librarysystem.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServiceTest implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowService borrowService;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Service层完整测试开始 ===");
        
        // 测试1：密码加密功能
        System.out.println("\n1. 测试密码加密功能...");
        try {
            userService.register("test1", "password123", "测试用户1", "test1@test.com");
            var loginResult1 = userService.login("test1", "password123");
            System.out.println("   登录测试: " + (loginResult1.get("success").equals(true) ? "✓ 通过" : "✗ 失败"));
            
            var loginResult2 = userService.login("test1", "wrongpassword");
            System.out.println("   错误密码测试: " + (loginResult2.get("success").equals(false) ? "✓ 通过" : "✗ 失败"));
        } catch (Exception e) {
            System.out.println("   密码加密测试失败: " + e.getMessage());
        }
        
        // 测试2：图书服务
        System.out.println("\n2. 测试图书服务...");
        try {
            var books = bookService.getAllBooks();
            System.out.println("   图书总数: " + books.size());
            
            var searchResult = bookService.searchBooks("Java", 0, 10);
            System.out.println("   搜索'Java'结果: " + searchResult.getTotalElements() + " 本");
            
            System.out.println("   图书服务测试: ✓ 通过");
        } catch (Exception e) {
            System.out.println("   图书服务测试失败: " + e.getMessage());
        }
        
        // 测试3：借阅服务统计
        System.out.println("\n3. 测试借阅服务...");
        try {
            var stats = borrowService.getBorrowStatistics();
            System.out.println("   借阅统计: " + stats);
            System.out.println("   借阅服务测试: ✓ 通过");
        } catch (Exception e) {
            System.out.println("   借阅服务测试失败: " + e.getMessage());
        }
        
        System.out.println("\n=== Service层完整测试完成 ===");
    }
}