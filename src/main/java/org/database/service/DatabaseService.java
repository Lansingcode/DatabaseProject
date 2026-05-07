package org.database.service;

import org.database.util.DBUtil;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库级别管理 —— 列出可用数据库、切换当前数据库。
 */
@Service
public class DatabaseService {

    /** 列出 MySQL 中所有用户可访问的数据库 */
    public List<String> listDatabases() {
        List<String> databases = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
            while (rs.next()) {
                String name = rs.getString(1);
                // 过滤系统库
                if (!"information_schema".equals(name)
                        && !"mysql".equals(name)
                        && !"performance_schema".equals(name)
                        && !"sys".equals(name)) {
                    databases.add(name);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库列表失败", e);
        }
        return databases;
    }

    /** 创建新数据库并自动切换 */
    public void createDatabase(String name) {
        SchemaService.validateName(name);
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + name + "`");
        } catch (SQLException e) {
            throw new RuntimeException("创建数据库失败: " + e.getMessage(), e);
        }
        switchDatabase(name);
    }

    /** 切换当前操作的数据库 */
    public void switchDatabase(String name) {
        DBUtil.setDatabaseName(name);
    }

    /** 获取当前数据库名 */
    public String getCurrentDatabase() {
        return DBUtil.getDatabaseName();
    }
}
