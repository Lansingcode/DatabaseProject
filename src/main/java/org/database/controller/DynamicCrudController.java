package org.database.controller;

import org.database.service.DynamicCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态数据 CRUD API —— 对任意表进行增删改查。
 *
 * GET    /api/tables/{tableName}/rows      → 查询全表
 * GET    /api/tables/{tableName}/rows/{id} → 按主键查单行
 * POST   /api/tables/{tableName}/rows      → 新增行
 * PUT    /api/tables/{tableName}/rows/{id} → 更新行
 * DELETE /api/tables/{tableName}/rows/{id} → 删除行
 */
@RestController
@RequestMapping("/api/tables/{tableName}/rows")
public class DynamicCrudController {

    @Autowired
    private DynamicCrudService crudService;

    @GetMapping
    public List<Map<String, Object>> findAll(@PathVariable String tableName) {
        return crudService.findAll(tableName);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable String tableName, @PathVariable Object id) {
        Map<String, Object> row = crudService.findById(tableName, id);
        return row != null ? ResponseEntity.ok(row) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Map<String, Object> insert(@PathVariable String tableName, @RequestBody Map<String, Object> row) {
        return crudService.insert(tableName, row);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String tableName, @PathVariable Object id,
                                     @RequestBody Map<String, Object> row) {
        int rows = crudService.update(tableName, id, row);
        if (rows > 0) {
            Map<String, String> m = new HashMap<>();
            m.put("message", "更新成功");
            return ResponseEntity.ok(m);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String tableName, @PathVariable Object id) {
        int rows = crudService.delete(tableName, id);
        return rows > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
