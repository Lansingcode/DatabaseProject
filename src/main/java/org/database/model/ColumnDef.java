package org.database.model;

/**
 * 列定义 —— 用于创建或修改表时描述列属性。
 */
public class ColumnDef {

    private String name;            // 列名
    private String type;            // SQL 类型, 如 "INT", "VARCHAR(50)", "TEXT"
    private boolean notNull;        // 是否 NOT NULL
    private boolean primaryKey;     // 是否主键
    private boolean autoIncrement;  // 是否自增（仅 INT 主键有效）

    public ColumnDef() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isNotNull() { return notNull; }
    public void setNotNull(boolean notNull) { this.notNull = notNull; }

    public boolean isPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }

    public boolean isAutoIncrement() { return autoIncrement; }
    public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }

    /** 根据字段定义生成 CREATE TABLE 中的列片段, 如 "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY" */
    public String toSql() {
        StringBuilder sb = new StringBuilder(name).append(" ").append(type);
        if (notNull) sb.append(" NOT NULL");
        if (autoIncrement) sb.append(" AUTO_INCREMENT");
        if (primaryKey) sb.append(" PRIMARY KEY");
        return sb.toString();
    }
}
