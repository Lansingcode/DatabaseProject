package org.database.service;

import org.database.model.ColumnDef;
import org.database.model.ColumnInfo;
import org.database.model.TableInfo;
import org.database.util.DBUtil;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表结构管理 —— DDL 操作与元数据查询。
 */
@Service
public class SchemaService {

    /** 标识符校验：仅允许字母、数字、下划线，且以字母或下划线开头 */
    private static final Pattern IDENT = Pattern.compile("^[a-zA-Z_]\\w*$");

    /** 获取库中所有用户表 */
    public List<String> listTables() {
        List<String> tables = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             ResultSet rs = conn.getMetaData().getTables(DBUtil.getDatabaseName(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取表列表失败", e);
        }
        return tables;
    }

    /** 获取指定表的列信息 */
    public TableInfo getTable(String tableName) {
        validateName(tableName);
        List<ColumnInfo> columns = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            // 主键列名集合
            List<String> pkCols = new ArrayList<>();
            try (ResultSet rs = meta.getPrimaryKeys(DBUtil.getDatabaseName(), null, tableName)) {
                while (rs.next()) pkCols.add(rs.getString("COLUMN_NAME"));
            }
            // 列信息
            try (ResultSet rs = meta.getColumns(DBUtil.getDatabaseName(), null, tableName, "%")) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    boolean isAutoInc = false;
                    try (ResultSet ai = meta.getColumns(DBUtil.getDatabaseName(), null, tableName, colName)) {
                        if (ai.next()) {
                            isAutoInc = "YES".equalsIgnoreCase(ai.getString("IS_AUTOINCREMENT"));
                        }
                    }
                    columns.add(new ColumnInfo(
                            colName,
                            rs.getString("TYPE_NAME") + (rs.getInt("COLUMN_SIZE") > 0 ? "(" + rs.getInt("COLUMN_SIZE") + ")" : ""),
                            "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")),
                            pkCols.contains(colName),
                            isAutoInc
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取表结构失败: " + tableName, e);
        }
        return new TableInfo(tableName, columns);
    }

    /** 创建新表 */
    public void createTable(String tableName, List<ColumnDef> columns) {
        validateName(tableName);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个列");
        }
        for (ColumnDef col : columns) validateName(col.getName());

        String colSql = columns.stream().map(ColumnDef::toSql).collect(Collectors.joining(", "));
        String sql = "CREATE TABLE " + tableName + " (" + colSql + ")";
        execute(sql);
    }

    /** 删除表 */
    public void dropTable(String tableName) {
        validateName(tableName);
        execute("DROP TABLE IF EXISTS " + tableName);
    }

    /** 追加列 */
    public void addColumn(String tableName, ColumnDef column) {
        validateName(tableName);
        validateName(column.getName());
        execute("ALTER TABLE " + tableName + " ADD COLUMN " + column.toSql());
    }

    /** 删除列 */
    public void dropColumn(String tableName, String columnName) {
        validateName(tableName);
        validateName(columnName);
        execute("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
    }

    /** 重命名表 */
    public void renameTable(String oldName, String newName) {
        validateName(oldName);
        validateName(newName);
        execute("RENAME TABLE " + oldName + " TO " + newName);
    }

    // ====== 内部方法 ======

    private void execute(String sql) {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("DDL 执行失败: " + e.getMessage(), e);
        }
    }

    /** 严格校验标识符，防止 SQL 注入 */
    static void validateName(String name) {
        if (name == null || !IDENT.matcher(name).matches()) {
            throw new IllegalArgumentException("非法标识符: " + name);
        }
    }
}
