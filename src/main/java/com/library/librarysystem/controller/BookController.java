package com.library.librarysystem.controller;

import com.library.librarysystem.entity.Book;

import com.library.librarysystem.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/books")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    /**
     * 图书列表页面（分页）
     */
    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {
        
        // 检查是否登录
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Page<Book> bookPage = bookService.searchBooks(keyword, page, size);
        
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        
        return "book/list";
    }
    
    /**
     * 查看图书详情
     */
    @GetMapping("/{id}")
    public String viewBook(@PathVariable String id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Book book = bookService.getBookById(id);
        if (book == null) {
            model.addAttribute("error", "图书不存在");
            return "redirect:/books";
        }
        
        model.addAttribute("book", book);
        return "book/view";
    }
    
    /**
     * 显示添加图书页面（仅管理员）
     */
    @GetMapping("/add")
    public String showAddBookForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "无权限访问");
            return "redirect:/books";
        }
        
        model.addAttribute("book", new Book());
        return "book/add";
    }
    
    /**
     * 处理添加图书请求
     */
    @PostMapping("/add")
    public String addBook(
            @ModelAttribute Book book,
            Model model,
            HttpSession session) {
        
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "无权限操作");
            return "redirect:/books";
        }
        
        try {
            // 设置默认值
            if (book.getTotalCopies() == null) {
                book.setTotalCopies(1);
            }
            if (book.getAvailableCopies() == null) {
                book.setAvailableCopies(book.getTotalCopies());
            }
            
            bookService.addBook(book);
            model.addAttribute("success", "图书添加成功");
        } catch (Exception e) {
            model.addAttribute("error", "添加失败：" + e.getMessage());
        }
        
        return "redirect:/books";
    }
    
    /**
     * 显示编辑图书页面
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "无权限访问");
            return "redirect:/books";
        }
        
        Book book = bookService.getBookById(id);
        if (book == null) {
            model.addAttribute("error", "图书不存在");
            return "redirect:/books";
        }
        
        model.addAttribute("book", book);
        return "book/edit";
    }
    
    /**
     * 处理更新图书请求
     */
    @PostMapping("/{id}/edit")
    public String updateBook(
            @PathVariable String id,
            @ModelAttribute Book book,
            Model model,
            HttpSession session) {
        
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "无权限操作");
            return "redirect:/books";
        }
        
        try {
            book.setId(id); // 确保ID不变
            bookService.updateBook(book);
            model.addAttribute("success", "图书更新成功");
        } catch (Exception e) {
            model.addAttribute("error", "更新失败：" + e.getMessage());
        }
        
        return "redirect:/books/" + id;
    }
    
    /**
     * 删除图书
     */
    @GetMapping("/{id}/delete")
    public String deleteBook(@PathVariable String id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "无权限操作");
            return "redirect:/books";
        }
        
        try {
            bookService.deleteBook(id);
            model.addAttribute("success", "图书删除成功");
        } catch (Exception e) {
            model.addAttribute("error", "删除失败：" + e.getMessage());
        }
        
        return "redirect:/books";
    }
}