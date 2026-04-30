package org.database.controller;

import org.database.model.Student;
import org.database.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student RESTful API 控制器。
 * <p>
 * 端点设计遵循 REST 资源命名规范：
 * GET    /api/students     → 查询全部
 * GET    /api/students/{id} → 按 id 查询
 * POST   /api/students     → 新增
 * PUT    /api/students/{id} → 更新
 * DELETE /api/students/{id} → 删除
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService service;

    /** GET /api/students —— 查询全部学生 */
    @GetMapping
    public ResponseEntity<List<Student>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /** GET /api/students/{id} —— 按 id 查询 */
    @GetMapping("/{id}")
    public ResponseEntity<Student> findById(@PathVariable int id) {
        Student student = service.findById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    /** POST /api/students —— 新增学生 */
    @PostMapping
    public ResponseEntity<Student> insert(@RequestBody Student student) {
        service.insert(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }

    /** PUT /api/students/{id} —— 全量更新学生信息 */
    @PutMapping("/{id}")
    public ResponseEntity<Student> update(@PathVariable int id, @RequestBody Student student) {
        Student existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.update(id, student));
    }

    /** DELETE /api/students/{id} —— 删除学生 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        int rows = service.delete(id);
        if (rows == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
