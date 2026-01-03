package com.library.librarysystem.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "borrow_records")
public class BorrowRecord {
    @Id
    private String id;
    
    private String userId;
    private String username; // 冗余字段，便于查询
    private String bookId;
    private String bookTitle; // 冗余字段，便于查询
    
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private LocalDateTime dueDate; // 应还日期
    private String status; // "BORROWED", "RETURNED", "OVERDUE"
    private LocalDateTime createTime;
    
    // 构造方法
    public BorrowRecord() {
        this.createTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public LocalDateTime getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    @Override
    public String toString() {
        return "BorrowRecord{" +
                "id='" + id + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", borrowDate=" + borrowDate +
                '}';
    }
}