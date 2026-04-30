package org.database.dao;

import org.database.model.Student;
import org.database.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Student 表的 CRUD 操作。
 */
public class StudentDao {

    /** 建表：如果 student 表不存在则创建 */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS student (" +
                     "id   INT PRIMARY KEY AUTO_INCREMENT," +
                     "name VARCHAR(50) NOT NULL," +
                     "age  INT," +
                     "grade VARCHAR(20)" +
                     ")";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[OK] 表 student 已就绪");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 新增一条学生记录 */
    public int insert(Student student) {
        String sql = "INSERT INTO student (name, age, grade) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getName());
            ps.setObject(2, student.getAge(), Types.INTEGER);
            ps.setString(3, student.getGrade());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    student.setId(keys.getInt(1));
                }
                DBUtil.close(keys);
            }
            System.out.println("[OK] 插入成功, id=" + student.getId());
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** 按 id 更新学生信息 */
    public int update(Student student) {
        String sql = "UPDATE student SET name=?, age=?, grade=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setObject(2, student.getAge(), Types.INTEGER);
            ps.setString(3, student.getGrade());
            ps.setInt(4, student.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "[OK] 更新成功" : "[WARN] 未找到 id=" + student.getId());
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** 按 id 删除学生记录 */
    public int delete(int id) {
        String sql = "DELETE FROM student WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "[OK] 删除成功" : "[WARN] 未找到 id=" + id);
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** 按 id 查询单条记录 */
    public Student findById(int id) {
        String sql = "SELECT id, name, age, grade FROM student WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rowToStudent(rs);
            }
            DBUtil.close(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询全部记录 */
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, name, age, grade FROM student";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rowToStudent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Student rowToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getObject("age") != null ? rs.getInt("age") : null,
                rs.getString("grade")
        );
    }
}
