package com.library.librarysystem.service;

import com.library.librarysystem.entity.User;
import com.library.librarysystem.repository.UserRepository;
import com.library.librarysystem.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Order(1) // 最高优先级执行
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 开始数据初始化检查 ===");
        
        // 检查并创建管理员账户
        initAdminUser();
        
        // 检查并创建测试用户
        initTestUsers();
        
        // 显示所有用户
        displayAllUsers();
        
        System.out.println("=== 数据初始化检查完成 ===");
    }

    /**
     * 初始化管理员账户
     */
    private void initAdminUser() {
        try {
            System.out.println("检查管理员账户...");
            
            // 检查管理员是否存在
            boolean adminExists = userRepository.existsByUsername("admin");
            
            if (!adminExists) {
                System.out.println("创建管理员账户...");
                
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordUtil.encrypt("admin123"));
                admin.setName("系统管理员");
                admin.setEmail("admin@library.com");
                admin.setRole("ADMIN");
                admin.setMaxBorrow(99); // 管理员无借阅限制
                
                userRepository.save(admin);
                System.out.println("管理员账户创建成功！用户名: admin, 密码: admin123");
            } else {
                System.out.println("管理员账户已存在");
            }
        } catch (Exception e) {
            System.err.println("初始化管理员账户失败: " + e.getMessage());
        }
    }

    /**
     * 初始化测试用户
     */
    private void initTestUsers() {
        try {
            System.out.println("检查测试用户...");
            
            // 检查测试用户是否存在
            boolean testUserExists = userRepository.existsByUsername("testuser");
            
            if (!testUserExists) {
                System.out.println("创建测试用户...");
                
                // 创建多个测试用户
                createTestUser("testuser", "测试用户", "testuser@library.com", "READER", 5);
                createTestUser("reader1", "读者1", "reader1@library.com", "READER", 5);
                createTestUser("reader2", "读者2", "reader2@library.com", "READER", 5);
                createTestUser("reader3", "读者3", "reader3@library.com", "READER", 5);
                createTestUser("librarian", "图书管理员", "librarian@library.com", "ADMIN", 10);
                
                System.out.println("测试用户创建完成");
            } else {
                System.out.println("测试用户已存在");
            }
        } catch (Exception e) {
            System.err.println("初始化测试用户失败: " + e.getMessage());
        }
    }

    /**
     * 创建测试用户
     */
    private void createTestUser(String username, String name, String email, String role, int maxBorrow) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordUtil.encrypt("123456")); // 统一密码
            user.setName(name);
            user.setEmail(email);
            user.setRole(role);
            user.setMaxBorrow(maxBorrow);
            
            userRepository.save(user);
            System.out.println("创建用户: " + username + " (" + name + ") - 角色: " + role);
        } catch (Exception e) {
            System.err.println("创建用户 " + username + " 失败: " + e.getMessage());
        }
    }

    /**
     * 显示所有用户
     */
    private void displayAllUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            System.out.println("数据库中用户总数: " + allUsers.size());
            
            if (!allUsers.isEmpty()) {
                System.out.println("用户列表:");
                for (User user : allUsers) {
                    System.out.println("  - " + user.getUsername() + " (" + user.getName() + ") - 角色: " + user.getRole());
                }
            } else {
                System.out.println("警告: 数据库中没有用户数据!");
            }
        } catch (Exception e) {
            System.err.println("获取用户列表失败: " + e.getMessage());
        }
    }
}