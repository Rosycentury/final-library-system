package com.library.librarysystem.repository;

import com.library.librarysystem.entity.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {
    
    // 根据标题模糊查询
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // 根据作者模糊查询
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // 根据ISBN精确查询
    Book findByIsbn(String isbn);
    
    // 查询可借阅的图书（库存大于0）
    @Query("{'availableCopies': {$gt: 0}}")
    List<Book> findAvailableBooks();
    
    // 根据标题或作者搜索
    @Query("{$or: [{'title': {$regex: ?0, $options: 'i'}}, {'author': {$regex: ?0, $options: 'i'}}]}")
    List<Book> searchByKeyword(String keyword);
}