package com.library.librarysystem.controller;

import com.library.librarysystem.entity.Book;
import com.library.librarysystem.service.BookService;
import com.library.librarysystem.service.BorrowService;
import com.library.librarysystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 搜索图书API
     */
    @GetMapping("/books/search")
    public ResponseEntity<Map<String, Object>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var bookPage = bookService.searchBooks(keyword, page, size);
            
            response.put("success", true);
            response.put("books", bookPage.getContent());
            response.put("total", bookPage.getTotalElements());
            response.put("pages", bookPage.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 借书API
     */
    @PostMapping("/borrow")
    public ResponseEntity<Map<String, Object>> borrowBook(
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = request.get("userId");
            String bookId = request.get("bookId");
            
            var record = borrowService.borrowBook(userId, bookId);
            
            response.put("success", true);
            response.put("message", "借书成功");
            response.put("borrowId", record.getId());
            response.put("dueDate", record.getDueDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 还书API
     */
    @PostMapping("/return")
    public ResponseEntity<Map<String, Object>> returnBook(
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String borrowId = request.get("borrowId");
            borrowService.returnBook(borrowId);
            
            response.put("success", true);
            response.put("message", "还书成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 添加图书API
     */
    @PostMapping("/books")
    public ResponseEntity<Map<String, Object>> addBook(@RequestBody Book book) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Book savedBook = bookService.addBook(book);
            
            response.put("success", true);
            response.put("message", "图书添加成功");
            response.put("bookId", savedBook.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户登录API
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> credentials) {
        
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        Map<String, Object> result = userService.login(username, password);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 系统统计API
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 用户统计
            var users = userService.getAllUsers();
            var readers = userService.getAllReaders();
            var admins = userService.getAllAdmins();
            
            // 图书统计
            var books = bookService.getAllBooks();
            var availableBooks = bookService.getAvailableBooks();
            
            // 借阅统计
            var borrowStats = borrowService.getBorrowStatistics();
            
            response.put("success", true);
            response.put("totalUsers", users.size());
            response.put("totalReaders", readers.size());
            response.put("totalAdmins", admins.size());
            response.put("totalBooks", books.size());
            response.put("availableBooks", availableBooks.size());
            response.put("borrowStats", borrowStats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
     * 获取用户统计数据API
     */
    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestParam(required = false) String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 如果没有提供userId，尝试从session获取
            if (userId == null || userId.isEmpty()) {
                // 在实际应用中，这里应该从安全上下文获取用户ID
                // 为了简单起见，我们先返回通用数据
                response.put("success", true);
                response.put("maxBorrow", 5);
                response.put("currentBorrows", 0);
                response.put("overdueCount", 0);
                response.put("totalBorrows", 0);
                return ResponseEntity.ok(response);
            }
            
            // 获取用户信息
            var user = userService.getUserById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取用户的借阅记录
            var records = borrowService.getUserBorrowRecords(userId);
            
            // 统计当前借阅数量
            long currentBorrows = records.stream()
                .filter(r -> "BORROWED".equals(r.getStatus()))
                .count();
            
            // 统计逾期数量
            long overdueCount = records.stream()
                .filter(r -> "OVERDUE".equals(r.getStatus()))
                .count();
            
            response.put("success", true);
            response.put("maxBorrow", user.getMaxBorrow());
            response.put("currentBorrows", currentBorrows);
            response.put("overdueCount", overdueCount);
            response.put("totalBorrows", records.size());
            response.put("username", user.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}