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
@Order(3)  // 在数据初始化之后执行
public class PasswordMigrationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordUtil passwordUtil;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 检查密码加密状态 ===");
        
        List<User> users = userRepository.findAll();
        int migratedCount = 0;
        
        for (User user : users) {
            String storedPassword = user.getPassword();
            
            // 检查密码是否已经是加密格式
            if (!passwordUtil.isEncryptedFormat(storedPassword)) {
                System.out.println("迁移用户密码: " + user.getUsername());
                
                // 重新加密密码
                String encryptedPassword = passwordUtil.encrypt(storedPassword);
                user.setPassword(encryptedPassword);
                userRepository.save(user);
                migratedCount++;
            }
        }
        
        if (migratedCount > 0) {
            System.out.println("密码迁移完成，共迁移 " + migratedCount + " 个用户");
        } else {
            System.out.println("所有密码都已加密，无需迁移");
        }
        
        System.out.println("=== 密码检查完成 ===");
    }
}