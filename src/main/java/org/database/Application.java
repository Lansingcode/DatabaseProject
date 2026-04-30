package org.database;

import org.database.service.StudentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot 入口。
 * <p>
 * 启动后自动建表，然后监听 HTTP 端口 8080。
 * 测试端点：curl http://localhost:8080/api/students
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /** 启动时自动完成建表 */
    @Bean
    CommandLineRunner initTable(StudentService service) {
        return args -> service.createTable();
    }
}
