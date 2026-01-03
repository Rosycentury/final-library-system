package com.library.librarysystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class LibrarySystemApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        // 强制设置端口
        System.setProperty("server.port", "8080");
        
        SpringApplication.run(LibrarySystemApplication.class, args);
        
        System.out.println("==========================================");
        System.out.println("图书馆管理系统启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("测试接口：http://localhost:8080/test");
        System.out.println("数据库测试：http://localhost:8080/db-test");
        System.out.println("图书列表：http://localhost:8080/books");
        System.out.println("==========================================");
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}