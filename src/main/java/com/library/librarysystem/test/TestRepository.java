package com.library.librarysystem.test;

import com.library.librarysystem.entity.Book;
import com.library.librarysystem.entity.User;
import com.library.librarysystem.repository.BookRepository;
import com.library.librarysystem.repository.UserRepository;
import com.library.librarysystem.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)  // 在管理员初始化之后执行
public class TestRepository implements CommandLineRunner {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 检查测试数据 ===");
        
        // 不再清空数据库，只插入必要的测试数据
        
        // 检查是否有图书数据，如果没有则插入测试图书
        long bookCount = bookRepository.count();
        if (bookCount == 0) {
            System.out.println("插入测试图书...");
            
            Book book1 = new Book("Java编程思想", "Bruce Eckel");
            book1.setIsbn("9787111234567");
            book1.setPublisher("机械工业出版社");
            book1.setTotalCopies(10);
            book1.setAvailableCopies(10);
            
            Book book2 = new Book("Spring Boot实战", "Craig Walls");
            book2.setIsbn("9787111245678");
            book2.setPublisher("人民邮电出版社");
            book2.setTotalCopies(5);
            book2.setAvailableCopies(5);
            
            bookRepository.save(book1);
            bookRepository.save(book2);
            System.out.println("插入测试图书完成，数量: " + bookRepository.count());
        } else {
            System.out.println("已有图书数据，数量: " + bookCount);
        }
        
        // 检查是否有普通读者用户，如果没有则创建
        if (!userRepository.existsByUsername("reader")) {
            System.out.println("创建测试读者账户...");
            
            User reader = new User();
            reader.setUsername("reader");
            // 使用PasswordUtil加密密码
            reader.setPassword(passwordUtil.encrypt("123456"));
            reader.setName("测试读者");
            reader.setEmail("reader@library.com");
            reader.setRole("READER");
            
            userRepository.save(reader);
            System.out.println("测试读者账户创建完成");
        } else {
            System.out.println("测试读者账户已存在");
        }
        
        System.out.println("=== 测试数据检查完成 ===");
    }
}