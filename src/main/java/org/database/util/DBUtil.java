package org.database.util;

import java.sql.*;

/**
 * 数据库连接工具类。
 * <p>
 * 使用前请根据本地环境修改 DB_URL、USER、PASSWORD 三个常量。
 */
public final class DBUtil {

    /** 从环境变量读取数据库配置，未设置时使用默认值 */
    private static final String DB_HOST = getEnv("DB_HOST", "localhost");
    private static final String DB_PORT = getEnv("DB_PORT", "3306");
    private static final String DB_NAME = getEnv("DB_NAME", "mydb");
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = getEnv("DB_USER", "root");
    private static final String PASSWORD = getEnv("DB_PASSWORD", "rootroot");

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC 驱动加载失败", e);
        }
    }

    /** 获取数据库连接 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    /** 获取当前数据库名（供 information_schema 查询使用） */
    public static String getDatabaseName() {
        return DB_NAME;
    }

    /** 关闭 ResultSet */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 关闭 Statement */
    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 关闭 Connection */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 一次性关闭 ResultSet, Statement, Connection */
    public static void closeAll(ResultSet rs, Statement stmt, Connection conn) {
        close(rs);
        close(stmt);
        close(conn);
    }
}
