package org.database.controller;

import org.database.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理 REST 控制器。
 */
@RestController
@RequestMapping("/api/databases")
public class DatabaseController {

    @Autowired
    private DatabaseService service;

    /** GET /api/databases —— 列出所有可用数据库 */
    @GetMapping
    public ResponseEntity<List<String>> listDatabases() {
        return ResponseEntity.ok(service.listDatabases());
    }

    /** GET /api/databases/current —— 获取当前数据库名 */
    @GetMapping("/current")
    public ResponseEntity<Map<String, String>> getCurrent() {
        return ResponseEntity.ok(Collections.singletonMap("database", service.getCurrentDatabase()));
    }

    /** PUT /api/databases/current —— 切换当前数据库 */
    @PutMapping("/current")
    public ResponseEntity<Map<String, String>> switchDatabase(@RequestBody Map<String, String> body) {
        String name = body.get("database");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "database 不能为空"));
        }
        service.switchDatabase(name);
        return ResponseEntity.ok(Collections.singletonMap("database", name));
    }

    /** POST /api/databases —— 创建新数据库 */
    @PostMapping
    public ResponseEntity<Map<String, String>> createDatabase(@RequestBody Map<String, String> body) {
        String name = body.get("database");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "database 不能为空"));
        }
        try {
            service.createDatabase(name);
            return ResponseEntity.ok(Collections.singletonMap("database", name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
