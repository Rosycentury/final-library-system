package com.library.librarysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DbTestController {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @GetMapping("/db-test")
    public Map<String, Object> testDb() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取数据库信息
            String dbName = mongoTemplate.getDb().getName();
            
            // 获取集合列表
            Iterable<String> collections = mongoTemplate.getCollectionNames();
            
            result.put("status", "success");
            result.put("database", dbName);
            result.put("message", "MongoDB连接成功！");
            result.put("collections", collections);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "MongoDB连接失败：" + e.getMessage());
            result.put("error", e.toString());
        }
        
        return result;
    }
}