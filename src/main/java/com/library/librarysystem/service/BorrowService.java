package com.library.librarysystem.service;

import com.library.librarysystem.entity.Book;
import com.library.librarysystem.entity.BorrowRecord;
import com.library.librarysystem.entity.User;
import com.library.librarysystem.repository.BookRepository;
import com.library.librarysystem.repository.BorrowRecordRepository;
import com.library.librarysystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowService {
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 借阅图书
     */
    @Transactional
    public BorrowRecord borrowBook(String userId, String bookId) {
        // 获取用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取图书
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        // 检查库存
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("图书库存不足");
        }
        
        // 检查用户借阅数量
        List<BorrowRecord> currentBorrows = borrowRecordRepository
            .findByUserIdAndStatus(userId, "BORROWED");
        if (currentBorrows.size() >= user.getMaxBorrow()) {
            throw new RuntimeException("借阅数量已达上限");
        }
        
        // 检查是否已借阅同一本书
        boolean alreadyBorrowed = currentBorrows.stream()
            .anyMatch(record -> record.getBookId().equals(bookId));
        if (alreadyBorrowed) {
            throw new RuntimeException("您已借阅此书");
        }
        
        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setUsername(user.getUsername());
        record.setBookId(bookId);
        record.setBookTitle(book.getTitle());
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(30)); // 30天后应还
        record.setStatus("BORROWED");
        
        // 减少图书库存
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        return borrowRecordRepository.save(record);
    }
    
    /**
     * 归还图书
     */
    @Transactional
    public BorrowRecord returnBook(String borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
            .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (!"BORROWED".equals(record.getStatus())) {
            throw new RuntimeException("图书已归还或状态异常");
        }
        
        // 更新借阅记录
        record.setReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        
        // 增加图书库存
        Book book = bookRepository.findById(record.getBookId()).orElse(null);
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }
        
        return borrowRecordRepository.save(record);
    }
    
    /**
     * 续借图书
     */
    @Transactional
    public BorrowRecord renewBook(String borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
            .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (!"BORROWED".equals(record.getStatus())) {
            throw new RuntimeException("只能续借已借阅的图书");
        }
        
        // 延长应还日期（再延长30天）
        record.setDueDate(record.getDueDate().plusDays(30));
        
        return borrowRecordRepository.save(record);
    }
    
    /**
     * 获取用户的借阅记录
     */
    public List<BorrowRecord> getUserBorrowRecords(String userId) {
        return borrowRecordRepository.findByUserId(userId);
    }
    
    /**
     * 获取用户当前的借阅（未归还）
     */
    public List<BorrowRecord> getUserCurrentBorrows(String userId) {
        return borrowRecordRepository.findByUserIdAndStatus(userId, "BORROWED");
    }
    
    /**
     * 获取所有借阅记录
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }
    
    /**
     * 统计借阅数量
     */
    public Map<String, Object> getBorrowStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // 总借阅次数
            long totalBorrows = borrowRecordRepository.count();
            stats.put("totalBorrows", totalBorrows);
            
            // 当前借出数量
            List<BorrowRecord> borrowedRecords = borrowRecordRepository.findByStatus("BORROWED");
            stats.put("currentBorrows", borrowedRecords.size());
            
            // 逾期数量
            List<BorrowRecord> overdueRecords = getOverdueRecords();
            stats.put("overdueBorrows", overdueRecords.size());
            
            // 归还数量
            List<BorrowRecord> returnedRecords = borrowRecordRepository.findByStatus("RETURNED");
            stats.put("returnedBorrows", returnedRecords.size());
            
            System.out.println("借阅统计完成: " + stats);
            
            return stats;
        } catch (Exception e) {
            System.err.println("获取借阅统计失败: " + e.getMessage());
            // 返回默认值
            stats.put("totalBorrows", 0);
            stats.put("currentBorrows", 0);
            stats.put("overdueBorrows", 0);
            stats.put("returnedBorrows", 0);
            return stats;
        }
    }

    /**
     * 获取逾期记录
     */
    public List<BorrowRecord> getOverdueRecords() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<BorrowRecord> borrowedRecords = borrowRecordRepository.findByStatus("BORROWED");
            
            return borrowedRecords.stream()
                .filter(record -> record.getDueDate().isBefore(now))
                .peek(record -> {
                    // 更新状态为逾期
                    if (!"OVERDUE".equals(record.getStatus())) {
                        record.setStatus("OVERDUE");
                        borrowRecordRepository.save(record);
                    }
                })
                .toList();
        } catch (Exception e) {
            System.err.println("获取逾期记录失败: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * 获取图书的借阅记录
     */
    public List<BorrowRecord> getBookBorrowRecords(String bookId) {
        return borrowRecordRepository.findByBookId(bookId);
    }
    
    /**
     * 获取用户的借阅统计详情
     */
    public Map<String, Object> getUserBorrowStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取用户的所有借阅记录
            List<BorrowRecord> records = borrowRecordRepository.findByUserId(userId);
            
            // 当前借阅数量
            long currentBorrows = records.stream()
                .filter(r -> "BORROWED".equals(r.getStatus()))
                .count();
            
            // 逾期数量
            long overdueCount = records.stream()
                .filter(r -> "OVERDUE".equals(r.getStatus()))
                .count();
            
            // 总借阅次数
            long totalBorrows = records.size();
            
            // 已归还数量
            long returnedCount = records.stream()
                .filter(r -> "RETURNED".equals(r.getStatus()))
                .count();
            
            // 计算平均借阅天数
            long totalBorrowDays = records.stream()
                .filter(r -> r.getReturnDate() != null)
                .mapToLong(r -> java.time.Duration.between(r.getBorrowDate(), r.getReturnDate()).toDays())
                .sum();
            
            long avgBorrowDays = returnedCount > 0 ? totalBorrowDays / returnedCount : 30;
            
            // 获取最常借阅的图书类型（简化版）
            String favoriteCategory = "未统计";
            
            stats.put("currentBorrows", currentBorrows);
            stats.put("overdueCount", overdueCount);
            stats.put("totalBorrows", totalBorrows);
            stats.put("returnedCount", returnedCount);
            stats.put("avgBorrowDays", avgBorrowDays);
            stats.put("favoriteCategory", favoriteCategory);
            stats.put("lastBorrowDate", getLastBorrowDate(records));
            stats.put("firstBorrowDate", getFirstBorrowDate(records));
            
        } catch (Exception e) {
            System.err.println("获取用户借阅统计详情失败: " + e.getMessage());
            stats.put("currentBorrows", 0);
            stats.put("overdueCount", 0);
            stats.put("totalBorrows", 0);
            stats.put("returnedCount", 0);
            stats.put("avgBorrowDays", 30);
            stats.put("favoriteCategory", "未统计");
        }
        
        return stats;
    }

    /**
     * 获取最后一次借阅时间
     */
    private String getLastBorrowDate(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            return "暂无借阅记录";
        }
        
        return records.stream()
            .map(BorrowRecord::getBorrowDate)
            .max(java.time.LocalDateTime::compareTo)
            .map(date -> date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .orElse("暂无");
    }

    /**
     * 获取第一次借阅时间
     */
    private String getFirstBorrowDate(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            return "暂无借阅记录";
        }
        
        return records.stream()
            .map(BorrowRecord::getBorrowDate)
            .min(java.time.LocalDateTime::compareTo)
            .map(date -> date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .orElse("暂无");
    }
   
}