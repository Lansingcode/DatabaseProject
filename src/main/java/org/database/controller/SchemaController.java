package org.database.controller;

import org.database.model.ColumnDef;
import org.database.model.TableInfo;
import org.database.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 表结构管理 API（DDL）。
 *
 * GET    /api/tables             → 列出所有表
 * GET    /api/tables/{name}      → 获取表结构
 * POST   /api/tables             → 创建表
 * DELETE /api/tables/{name}      → 删除表
 * PUT    /api/tables/{name}      → 修改表（增/删列）
 */
@RestController
@RequestMapping("/api/tables")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @GetMapping
    public List<String> listTables() {
        return schemaService.listTables();
    }

    @GetMapping("/{tableName}")
    public TableInfo getTable(@PathVariable String tableName) {
        return schemaService.getTable(tableName);
    }

    @PostMapping
    public ResponseEntity<?> createTable(@RequestBody Map<String, Object> body) {
        try {
            String tableName = (String) body.get("tableName");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cols = (List<Map<String, Object>>) body.get("columns");
            List<ColumnDef> columnDefs = new ArrayList<>();
            for (Map<String, Object> c : cols) {
                ColumnDef def = new ColumnDef();
                def.setName((String) c.get("name"));
                def.setType((String) c.get("type"));
                def.setNotNull(Boolean.TRUE.equals(c.get("notNull")));
                def.setPrimaryKey(Boolean.TRUE.equals(c.get("primaryKey")));
                def.setAutoIncrement(Boolean.TRUE.equals(c.get("autoIncrement")));
                columnDefs.add(def);
            }
            schemaService.createTable(tableName, columnDefs);
            return ResponseEntity.ok(msg("表创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(msg("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{tableName}")
    public ResponseEntity<?> dropTable(@PathVariable String tableName) {
        schemaService.dropTable(tableName);
        return ResponseEntity.ok(msg("表已删除"));
    }

    @PutMapping("/{tableName}")
    public ResponseEntity<?> alterTable(@PathVariable String tableName, @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> addCols = (List<Map<String, Object>>) body.getOrDefault("addColumns", Collections.emptyList());
            for (Map<String, Object> c : addCols) {
                ColumnDef def = new ColumnDef();
                def.setName((String) c.get("name"));
                def.setType((String) c.get("type"));
                def.setNotNull(Boolean.TRUE.equals(c.get("notNull")));
                schemaService.addColumn(tableName, def);
            }
            @SuppressWarnings("unchecked")
            List<String> dropCols = (List<String>) body.getOrDefault("dropColumns", Collections.emptyList());
            for (String col : dropCols) {
                schemaService.dropColumn(tableName, col);
            }
            return ResponseEntity.ok(msg("表结构已更新"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(msg("error", e.getMessage()));
        }
    }

    /** 单键值 Map 快捷构造（Java 8 无 Map.of） */
    private Map<String, String> msg(String value) {
        Map<String, String> m = new HashMap<>();
        m.put("message", value);
        return m;
    }

    private Map<String, String> msg(String key, String value) {
        Map<String, String> m = new HashMap<>();
        m.put(key, value);
        return m;
    }
}
