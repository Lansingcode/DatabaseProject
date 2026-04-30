package org.database.service;

import org.database.dao.StudentDao;
import org.database.model.Student;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Student 业务层 —— 在 DAO 基础上封装业务逻辑。
 */
@Service
public class StudentService {

    private final StudentDao dao = new StudentDao();

    public void createTable() {
        dao.createTable();
    }

    public Student insert(Student student) {
        dao.insert(student);
        return student;
    }

    public Student update(int id, Student student) {
        student.setId(id);
        dao.update(student);
        return findById(id);
    }

    public int delete(int id) {
        return dao.delete(id);
    }

    public Student findById(int id) {
        return dao.findById(id);
    }

    public List<Student> findAll() {
        return dao.findAll();
    }
}
