package org.database.model;

/**
 * 列元信息 —— 从 information_schema 读取。
 */
public class ColumnInfo {

    private String name;
    private String type;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;

    public ColumnInfo() {
    }

    public ColumnInfo(String name, String type, boolean nullable, boolean primaryKey, boolean autoIncrement) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }

    public boolean isPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }

    public boolean isAutoIncrement() { return autoIncrement; }
    public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }
}
