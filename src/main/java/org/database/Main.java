package org.database;

import org.database.dao.StudentDao;
import org.database.model.Student;

import java.util.List;

/**
 * 演示 Student 表的完整 CRUD 流程。
 * <p>
 * 运行前请确保：
 * 1. 本地 MySQL 服务已启动
 * 2. 数据库 mydb 已存在（没有的话在 MySQL 中执行 CREATE DATABASE mydb;）
 * 3. DBUtil 中的密码已修改为你的 root 密码
 */
public class Main {
    public static void main(String[] args) {
        StudentDao dao = new StudentDao();

        // 1. 建表
        dao.createTable();

        // 2. 新增
        System.out.println("\n===== 新增 =====");
        dao.insert(new Student("张三", 20, "大三"));
        dao.insert(new Student("李四", 21, "大四"));
        dao.insert(new Student("王五", 19, "大二"));

        // 3. 查询全部
        System.out.println("\n===== 查询全部 =====");
        List<Student> all = dao.findAll();
        all.forEach(System.out::println);

        // 4. 按 id 查询
        System.out.println("\n===== 按 id 查询 =====");
        Student s = dao.findById(1);
        System.out.println(s);

        // 5. 更新
        System.out.println("\n===== 更新 =====");
        if (s != null) {
            s.setName("张三丰");
            s.setAge(22);
            dao.update(s);
        }

        // 6. 删除
        System.out.println("\n===== 删除 =====");
        dao.delete(3);

        // 7. 最终查询
        System.out.println("\n===== 最终全部数据 =====");
        dao.findAll().forEach(System.out::println);
    }
}
