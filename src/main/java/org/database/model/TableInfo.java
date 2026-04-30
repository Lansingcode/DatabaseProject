package org.database.model;

import java.util.List;

/**
 * 表信息响应。
 */
public class TableInfo {

    private String tableName;
    private List<ColumnInfo> columns;

    public TableInfo() {
    }

    public TableInfo(String tableName, List<ColumnInfo> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public List<ColumnInfo> getColumns() { return columns; }
    public void setColumns(List<ColumnInfo> columns) { this.columns = columns; }
}
