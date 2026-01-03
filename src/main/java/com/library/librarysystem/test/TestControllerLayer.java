package com.library.librarysystem.test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestControllerLayer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Controller层测试信息 ===");
        System.out.println("可用页面：");
        System.out.println("1. 首页: http://localhost:8080/");
        System.out.println("2. 登录: http://localhost:8080/login");
        System.out.println("3. 注册: http://localhost:8080/register");
        System.out.println("4. 图书列表: http://localhost:8080/books");
        System.out.println("\n管理员页面：");
        System.out.println("5. 仪表板: http://localhost:8080/admin/dashboard");
        System.out.println("6. 用户管理: http://localhost:8080/admin/users");
        System.out.println("\n用户页面：");
        System.out.println("7. 我的借阅: http://localhost:8080/borrow/my-records");
        System.out.println("8. 个人中心: http://localhost:8080/profile");
        System.out.println("\nAPI接口：");
        System.out.println("9. 搜索API: http://localhost:8080/api/books/search?keyword=Java");
        System.out.println("10. 统计API: http://localhost:8080/api/statistics");
        System.out.println("\n测试账户：");
        System.out.println("管理员: admin / admin123");
        System.out.println("普通用户: reader / 123456");
        System.out.println("=== Controller层测试准备完成 ===\n");
    }
}