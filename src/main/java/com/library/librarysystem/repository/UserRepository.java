package com.library.librarysystem.repository;

import com.library.librarysystem.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查邮箱是否存在
    boolean existsByEmail(String email);

    // 根据角色查找用户
    List<User> findByRole(String role);

    // 根据角色分页查找用户
    Page<User> findByRole(String role, Pageable pageable);
}