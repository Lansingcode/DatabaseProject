package org.database.service;

import org.database.model.ColumnInfo;
import org.database.model.TableInfo;
import org.database.util.DBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态 CRUD —— 对任意表生成并执行 SQL。
 * <p>
 * 核心安全策略：列名通过 DatabaseMetaData 白名单校验，
 * 值通过 PreparedStatement 参数化，杜绝 SQL 注入。
 */
@Service
public class DynamicCrudService {

    @Autowired
    private SchemaService schemaService;

    /** 查询全表 */
    public List<Map<String, Object>> findAll(String tableName) {
        TableInfo info = schemaService.getTable(tableName);
        String cols = columnList(info);
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT " + cols + " FROM " + tableName)) {
            return resultSetToList(rs, info);
        } catch (SQLException e) {
            throw new RuntimeException("查询失败", e);
        }
    }

    /** 按主键查询单行 */
    public Map<String, Object> findById(String tableName, Object id) {
        TableInfo info = schemaService.getTable(tableName);
        ColumnInfo pk = findPk(info);
        String sql = "SELECT " + columnList(info) + " FROM " + tableName + " WHERE " + pk.getName() + " = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rowToMap(rs, info);
            }
            DBUtil.close(rs);
        } catch (SQLException e) {
            throw new RuntimeException("查询失败", e);
        }
        return null;
    }

    /** 新增一行，返回自增主键值 */
    public Map<String, Object> insert(String tableName, Map<String, Object> row) {
        TableInfo info = schemaService.getTable(tableName);
        ColumnInfo pk = findPk(info);

        // 排除自增主键列
        List<ColumnInfo> writable = info.getColumns().stream()
                .filter(c -> !(c.isPrimaryKey() && c.isAutoIncrement()))
                .collect(Collectors.toList());

        String cols = writable.stream().map(ColumnInfo::getName).collect(Collectors.joining(", "));
        String placeholders = writable.stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + placeholders + ")";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < writable.size(); i++) {
                ps.setObject(i + 1, row.get(writable.get(i).getName()));
            }
            ps.executeUpdate();
            if (pk != null && pk.isAutoIncrement()) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    row.put(pk.getName(), keys.getObject(1));
                }
                DBUtil.close(keys);
            }
            return row;
        } catch (SQLException e) {
            throw new RuntimeException("插入失败", e);
        }
    }

    /** 按主键更新 */
    public int update(String tableName, Object id, Map<String, Object> row) {
        TableInfo info = schemaService.getTable(tableName);
        ColumnInfo pk = findPk(info);
        List<ColumnInfo> updatable = info.getColumns().stream()
                .filter(c -> !c.isPrimaryKey())
                .collect(Collectors.toList());

        String setClause = updatable.stream().map(c -> c.getName() + " = ?").collect(Collectors.joining(", "));
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + pk.getName() + " = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (ColumnInfo c : updatable) {
                ps.setObject(i++, row.get(c.getName()));
            }
            ps.setObject(i, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新失败", e);
        }
    }

    /** 按主键删除 */
    public int delete(String tableName, Object id) {
        TableInfo info = schemaService.getTable(tableName);
        ColumnInfo pk = findPk(info);
        String sql = "DELETE FROM " + tableName + " WHERE " + pk.getName() + " = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除失败", e);
        }
    }

    // ====== 辅助方法 ======

    private String columnList(TableInfo info) {
        return info.getColumns().stream().map(ColumnInfo::getName).collect(Collectors.joining(", "));
    }

    private ColumnInfo findPk(TableInfo info) {
        return info.getColumns().stream()
                .filter(ColumnInfo::isPrimaryKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("表 " + info.getTableName() + " 没有主键，无法执行此操作"));
    }

    private List<Map<String, Object>> resultSetToList(ResultSet rs, TableInfo info) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rowToMap(rs, info));
        }
        return list;
    }

    private Map<String, Object> rowToMap(ResultSet rs, TableInfo info) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        for (ColumnInfo col : info.getColumns()) {
            map.put(col.getName(), rs.getObject(col.getName()));
        }
        return map;
    }
}
