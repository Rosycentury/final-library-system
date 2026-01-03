package com.library.librarysystem.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordUtil {

    /**
     * 对密码进行SHA-256加密
     * @param password 明文密码
     * @return 加密后的密码
     */
    public String encrypt(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // 添加盐值增加安全性
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // 将盐值和哈希值组合存储
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     * @param inputPassword 用户输入的密码
     * @param storedPassword 存储的加密密码
     * @return 验证结果
     */
    public boolean verify(String inputPassword, String storedPassword) {
        try {
            if (storedPassword == null || storedPassword.isEmpty()) {
                return false;
            }
            
            // 解码存储的密码
            byte[] combined;
            try {
                combined = Base64.getDecoder().decode(storedPassword);
            } catch (IllegalArgumentException e) {
                // 如果密码不是Base64格式（可能是旧数据），尝试直接比较（仅用于迁移期间）
                System.err.println("密码格式异常，可能是明文存储: " + storedPassword);
                return false;
            }
            
            // 验证组合数组的长度（盐值16字节 + 哈希值32字节 = 48字节）
            if (combined.length != 48) {
                System.err.println("密码格式不正确，长度: " + combined.length);
                return false;
            }
            
            // 提取盐值（前16字节）
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, salt.length);
            
            // 使用相同的盐值加密输入的密码
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] inputHash = digest.digest(inputPassword.getBytes(StandardCharsets.UTF_8));
            
            // 比较哈希值（从第17字节开始，即索引16）
            for (int i = 0; i < inputHash.length; i++) {
                if (inputHash[i] != combined[16 + i]) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("密码验证异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查密码是否是加密格式
     */
    public boolean isEncryptedFormat(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(password);
            return decoded.length == 48; // 盐值16字节 + 哈希值32字节
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成随机密码（用于忘记密码功能）
     * @return 随机密码
     */
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}