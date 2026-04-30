package org.database.util;

import java.sql.*;

/**
 * 数据库连接工具类。
 * <p>
 * 使用前请根据本地环境修改 DB_URL、USER、PASSWORD 三个常量。
 */
public final class DBUtil {

    /** 数据库连接地址，mydb 为目标数据库名 */
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    /** 数据库用户名 */
    private static final String USER = "root";
    /** 数据库密码 —— 请改为你的 root 密码 */
    private static final String PASSWORD = "rootroot";

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
