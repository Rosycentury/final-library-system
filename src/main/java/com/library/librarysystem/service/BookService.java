package com.library.librarysystem.service;

import com.library.librarysystem.entity.Book;
import com.library.librarysystem.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    /**
     * 添加图书
     */
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }
    
    /**
     * 更新图书信息
     */
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }
    
    /**
     * 根据ID获取图书
     */
    public Book getBookById(String id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据ISBN获取图书
     */
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    /**
     * 删除图书
     */
    public void deleteBook(String id) {
        bookRepository.deleteById(id);
    }
    
    /**
     * 获取所有图书
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    /**
     * 搜索图书（分页）
     */
    public Page<Book> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果没有关键词，返回所有图书
            List<Book> allBooks = bookRepository.findAll();
            return getPageFromList(allBooks, pageable);
        } else {
            // 使用自定义查询搜索
            List<Book> books = bookRepository.searchByKeyword(keyword);
            return getPageFromList(books, pageable);
        }
    }
    
    /**
     * 获取可借阅的图书
     */
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    /**
     * 根据标题搜索
     */
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    /**
     * 根据作者搜索
     */
    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }
    
    /**
     * 减少图书库存
     */
    public boolean decreaseStock(String bookId, int quantity) {
        Book book = getBookById(bookId);
        if (book != null && book.getAvailableCopies() >= quantity) {
            book.setAvailableCopies(book.getAvailableCopies() - quantity);
            bookRepository.save(book);
            return true;
        }
        return false;
    }
    
    /**
     * 增加图书库存
     */
    public boolean increaseStock(String bookId, int quantity) {
        Book book = getBookById(bookId);
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + quantity);
            // 确保不超过总数量
            if (book.getAvailableCopies() > book.getTotalCopies()) {
                book.setAvailableCopies(book.getTotalCopies());
            }
            bookRepository.save(book);
            return true;
        }
        return false;
    }
    
    /**
     * 从List创建Page对象
     */
    private Page<Book> getPageFromList(List<Book> books, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());
        
        if (start > books.size()) {
            return Page.empty(pageable);
        }
        
        return new PageImpl<>(books.subList(start, end), pageable, books.size());
    }
}