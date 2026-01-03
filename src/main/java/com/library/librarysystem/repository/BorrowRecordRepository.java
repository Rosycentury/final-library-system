package com.library.librarysystem.repository;

import com.library.librarysystem.entity.BorrowRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BorrowRecordRepository extends MongoRepository<BorrowRecord, String> {
    
    // 根据用户ID查找借阅记录
    List<BorrowRecord> findByUserId(String userId);
    
    // 根据用户ID和状态查找借阅记录
    List<BorrowRecord> findByUserIdAndStatus(String userId, String status);
    
    // 根据图书ID查找借阅记录
    List<BorrowRecord> findByBookId(String bookId);
    
    // 根据状态查找借阅记录
    List<BorrowRecord> findByStatus(String status);
    
    // 查找逾期未还的记录
    List<BorrowRecord> findByStatusAndDueDateBefore(String status, java.util.Date dueDate);
}