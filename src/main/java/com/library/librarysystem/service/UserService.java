package com.library.librarysystem.service;

import com.library.librarysystem.entity.User;
import com.library.librarysystem.repository.UserRepository;
import com.library.librarysystem.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;
    
    public List<User> searchUsers(String keyword, String roleFilter) {
		return null;
	}

    /**
     * 获取所有用户（简化版本）
     */
    public List<User> getAllUsers() {
        System.out.println("开始获取所有用户...");
        try {
            long startTime = System.currentTimeMillis();
            List<User> users = userRepository.findAll();
            long endTime = System.currentTimeMillis();
            
            System.out.println("获取用户列表成功，数量：" + users.size() + 
                             "，耗时：" + (endTime - startTime) + "ms");
            
            return users != null ? users : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("获取所有用户时发生异常：" + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 分页获取所有用户 - 修复版本
     */
    public Page<User> getAllUsers(int page, int size) {
        System.out.println("分页获取用户，page=" + page + ", size=" + size);
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
            Page<User> userPage = userRepository.findAll(pageable);
            
            System.out.println("分页获取用户成功，总数：" + userPage.getTotalElements() + 
                             "，当前页：" + userPage.getNumber() + 
                             "，总页数：" + userPage.getTotalPages());
            
            return userPage;
        } catch (Exception e) {
            System.err.println("分页获取用户失败：" + e.getMessage());
            e.printStackTrace();
            return Page.empty();
        }
    }

    /**
     * 获取所有读者
     */
    public List<User> getAllReaders() {
        try {
            List<User> readers = userRepository.findByRole("READER");
            System.out.println("获取读者数量: " + readers.size());
            return readers;
        } catch (Exception e) {
            System.err.println("获取读者列表失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有管理员
     */
    public List<User> getAllAdmins() {
        try {
            List<User> admins = userRepository.findByRole("ADMIN");
            System.out.println("获取管理员数量: " + admins.size());
            return admins;
        } catch (Exception e) {
            System.err.println("获取管理员列表失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(String id) {
        try {
            return userRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.err.println("根据ID获取用户失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            System.err.println("根据用户名获取用户失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 用户注册 - 修复版本
     */
    public Map<String, Object> register(String username, String password, String name, String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("用户注册: " + username);
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }

            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(email)) {
                result.put("success", false);
                result.put("message", "邮箱已被注册");
                return result;
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordUtil.encrypt(password));
            user.setName(name);
            user.setEmail(email);
            user.setRole("READER"); // 默认角色为读者
            user.setMaxBorrow(5); // 默认最大借阅数

            User savedUser = userRepository.save(user);
            
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("user", savedUser);
            
            System.out.println("用户注册成功: " + username);
            
        } catch (Exception e) {
            System.err.println("用户注册失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 用户登录 - 修复版本
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("用户登录尝试: " + username);
            
            Optional<User> userOptional = userRepository.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            User user = userOptional.get();
            
            // 验证密码
            if (!passwordUtil.verify(password, user.getPassword())) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }

            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
            
            System.out.println("用户登录成功: " + username + ", 角色: " + user.getRole());
            
        } catch (Exception e) {
            System.err.println("用户登录失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            System.err.println("更新用户失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                System.out.println("删除用户成功，ID: " + id);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 获取用户总数
     */
    public long getUserCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            System.err.println("获取用户总数失败: " + e.getMessage());
            return 0;
        }
    }
}