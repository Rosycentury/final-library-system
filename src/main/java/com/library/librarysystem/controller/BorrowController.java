package com.library.librarysystem.controller;

import com.library.librarysystem.service.BorrowService;

import com.library.librarysystem.service.UserService;
import com.library.librarysystem.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/borrow")
public class BorrowController {
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 借阅图书
     */
    @PostMapping("/borrow")
    public String borrowBook(
            @RequestParam String bookId,
            HttpSession session,
            Model model) {
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            borrowService.borrowBook(userId, bookId);
            model.addAttribute("success", "借书成功！请在30天内归还");
        } catch (Exception e) {
            model.addAttribute("error", "借书失败：" + e.getMessage());
        }
        
        return "redirect:/books";
    }
    
    /**
     * 显示我的借阅记录
     */
    @GetMapping("/my-records")
    public String myBorrowRecords(
            HttpSession session,
            Model model) {
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        var records = borrowService.getUserBorrowRecords(userId);
        model.addAttribute("records", records);
        
        // 统计信息
        var currentBorrows = borrowService.getUserCurrentBorrows(userId);
        var overdueRecords = records.stream()
            .filter(r -> "OVERDUE".equals(r.getStatus()))
            .toList();
        
        model.addAttribute("currentCount", currentBorrows.size());
        model.addAttribute("overdueCount", overdueRecords.size());
        model.addAttribute("totalCount", records.size());
        
        return "borrow/my-records";
    }
    
    /**
     * 归还图书
     */
    @PostMapping("/return")
    public String returnBook(
            @RequestParam String borrowId,
            HttpSession session,
            Model model) {
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            borrowService.returnBook(borrowId);
            model.addAttribute("success", "还书成功！");
        } catch (Exception e) {
            model.addAttribute("error", "还书失败：" + e.getMessage());
        }
        
        return "redirect:/borrow/my-records";
    }
    
    /**
     * 续借图书
     */
    @PostMapping("/renew")
    public String renewBook(
            @RequestParam String borrowId,
            HttpSession session,
            Model model) {
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            borrowService.renewBook(borrowId);
            model.addAttribute("success", "续借成功！延长30天");
        } catch (Exception e) {
            model.addAttribute("error", "续借失败：" + e.getMessage());
        }
        
        return "redirect:/borrow/my-records";
    }
    
    /**
     * 借阅统计（管理员）
     */
    @GetMapping("/statistics")
    public String borrowStatistics(HttpSession session, Model model) {
        try {
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                model.addAttribute("error", "无权限访问");
                return "redirect:/";
            }

            var stats = borrowService.getBorrowStatistics();
            model.addAttribute("stats", stats);
            
            System.out.println("统计页面加载完成");
            
            return "borrow/statistics";
        } catch (Exception e) {
            System.err.println("统计页面加载失败: " + e.getMessage());
            model.addAttribute("error", "加载统计信息失败: " + e.getMessage());
            return "borrow/statistics";
        }
    }
}