# JDBC CRUD 数据库知识总结

> 基于本项目 `StudentDao` 实现，覆盖从 Java 连接 MySQL 到完成增删改查的完整知识体系。

---

## 一、JDBC 架构与核心接口

JDBC（Java Database Connectivity）是 Java 访问数据库的标准 API，定义在 `java.sql` 包中。

### 核心接口调用链

```
加载驱动  →  获取连接  →  创建 Statement  →  执行 SQL  →  处理结果  →  释放资源
  ↓             ↓              ↓                ↓            ↓           ↓
Class.forName  DriverManager  Connection        executeXxx   ResultSet   close()
               .getConnection .createStatement  .executeUpdate
                                               .executeQuery
```

| 接口 | 作用 | 本项目对应位置 |
|------|------|---------------|
| `Driver` | 数据库驱动，实现 JDBC 与具体数据库的通信协议 | `DBUtil` 静态块 `Class.forName("com.mysql.cj.jdbc.Driver")` |
| `Connection` | 代表一个数据库连接会话 | `DBUtil.getConnection()` |
| `Statement` | 执行静态 SQL（无参数） | `StudentDao.createTable()`、`findAll()` |
| `PreparedStatement` | 预编译 SQL，支持参数占位符 `?`，防 SQL 注入 | `StudentDao.insert()`、`update()`、`delete()`、`findById()` |
| `ResultSet` | 查询结果集，游标遍历 | `StudentDao.rowToStudent()` |

---

## 二、DDL vs DML

SQL 语句分为两大类，在本项目中均有用例：

| 类型 | 全称 | 作用 | 执行方法 | 本项目示例 |
|------|------|------|---------|-----------|
| **DDL** | Data Definition Language | 定义/修改表结构 | `Statement.execute()` | `CREATE TABLE IF NOT EXISTS student (...)` |
| **DML** | Data Manipulation Language | 操作表中数据 | `executeUpdate()` 增删改<br>`executeQuery()` 查询 | INSERT、UPDATE、DELETE、SELECT |

### DDL —— 建表语句详解

```sql
CREATE TABLE IF NOT EXISTS student (
    id    INT PRIMARY KEY AUTO_INCREMENT,   -- 主键约束 + 自增
    name  VARCHAR(50) NOT NULL,             -- 非空约束
    age   INT,                              -- 可空字段
    grade VARCHAR(20)
)
```

| 关键字 | 含义 |
|--------|------|
| `IF NOT EXISTS` | 幂等建表：表已存在时不报错，避免重复执行导致异常 |
| `PRIMARY KEY` | 主键约束：唯一标识一行，自带唯一索引，不允许 NULL |
| `AUTO_INCREMENT` | 自增列：插入时省略该字段，MySQL 自动生成递增整数 |
| `NOT NULL` | 非空约束：该列不允许插入 NULL 值 |
| `VARCHAR(N)` | 变长字符串，N 为最大字符数（注意：在 MySQL 中是字符数，非字节数） |

---

## 三、Statement vs PreparedStatement

这是 JDBC 中最关键的**安全边界**。

| | Statement | PreparedStatement |
|------|-----------|-------------------|
| SQL 编译 | 每次执行都编译 | **预编译一次**，后续复用执行计划 |
| 参数传递 | 字符串拼接 | `?` 占位符 + `setXxx()` 方法 |
| SQL 注入风险 | **高危**：拼接用户输入可直接篡改 SQL | **安全**：参数值与 SQL 结构分离 |
| 使用场景 | 无参数的 DDL、全表查询 | 所有带参数的 DML |

### 为什么 PreparedStatement 能防 SQL 注入

```sql
-- 危险写法（字符串拼接）：
"SELECT * FROM student WHERE name = '" + userInput + "'"
-- 用户输入：' OR '1'='1' -- 
-- 拼接后：SELECT * FROM student WHERE name = '' OR '1'='1' --'
-- 结果：返回全表数据

-- 安全写法（PreparedStatement）：
ps.setString(1, "张三' OR '1'='1' --");
-- MySQL 将整个字符串作为 name 的值来处理，不会执行其中的 SQL 逻辑
```

PreparedStatement 在数据库端**先编译 SQL 骨架**，再填入参数值。参数值永远不会被当作 SQL 代码解释，从根本上杜绝了注入。

---

## 四、CRUD 操作详解

### 1. 新增（INSERT）—— 含自增主键回填

```java
String sql = "INSERT INTO student (name, age, grade) VALUES (?, ?, ?)";
PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
ps.setString(1, student.getName());          // 第1个? → name
ps.setObject(2, student.getAge(), Types.INTEGER);  // 第2个? → age（可为NULL）
ps.setString(3, student.getGrade());         // 第3个? → grade（可为NULL）
ps.executeUpdate();

ResultSet keys = ps.getGeneratedKeys();      // 获取生成的自增 id
if (keys.next()) {
    student.setId(keys.getInt(1));           // 回填到对象
}
```

**关键点**：`Statement.RETURN_GENERATED_KEYS` 让 MySQL 返回自增主键值，避免插入后还需要额外查询。

**关于 setObject vs setInt**：当列允许 NULL 时，使用 `setObject(index, value, Types.INTEGER)` 而非 `setInt()`。因为 Java 的 `int` 是基本类型不能为 null，而 `Integer` 可以为 null。`setObject` 能正确处理 null → SQL NULL 的映射。

### 2. 查询（SELECT）

```java
// 单条查询
String sql = "SELECT id, name, age, grade FROM student WHERE id=?";
ResultSet rs = ps.executeQuery();
while (rs.next()) {                           // 游标逐行遍历
    int id = rs.getInt("id");                 // 按列名取值
    String name = rs.getString("name");
}
```

**ResultSet 游标模型**：初始指向第 0 行（不是第一行），每次调用 `next()` 下移一行，返回 `false` 表示没有更多行。列可用序号（从 1 开始）或列名访问。

### 3. 更新（UPDATE）

```java
String sql = "UPDATE student SET name=?, age=?, grade=? WHERE id=?";
ps.executeUpdate();                          // 返回受影响行数
```

**注意**：`executeUpdate()` 返回 `int`，表示受影响的记录数。如果 WHERE 条件没命中，返回 0（不是异常）。

### 4. 删除（DELETE）

```java
String sql = "DELETE FROM student WHERE id=?";
ps.executeUpdate();
```

---

## 五、连接管理与资源释放

### try-with-resources（自动资源管理）

```java
try (Connection conn = DBUtil.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // 使用 conn 和 ps
}  // try 块结束，conn 和 ps 自动关闭，顺序与声明顺序相反
```

这是 JDBC 中最常见的**资源泄漏陷阱**的正确解法：

- **错误写法**：手动 `finally { conn.close(); }` —— 如果 `conn.close()` 抛异常，后续资源永远不会关闭
- **正确写法**：try-with-resources —— 编译器保证所有实现了 `AutoCloseable` 的资源按声明反序关闭

### 关闭顺序

连接池层面，关闭顺序是：**ResultSet → Statement → Connection**。先关结果集，再关语句，最后归还连接。

### DBUtil 的三层关闭方法

```java
close(ResultSet rs)   // 精细控制，单条查询场景使用
close(Statement stmt)
close(Connection conn)
closeAll(rs, stmt, conn)  // 批量关闭，全场景适用
```

没有报错的关闭方法中 `catch (SQLException ignored)` 是标准写法：关闭资源时的异常不可恢复，也不应掩盖业务异常。

---

## 六、NULL 值处理策略

数据库 NULL 与 Java null 之间的映射需要特殊处理：

| 场景 | 方案 | 代码位置 |
|------|------|---------|
| 插入可空列 | `setObject(i, value, Types.INTEGER)` | `StudentDao.insert()` 第 38 行 |
| 读出可空列 | `rs.getObject("age") != null ? rs.getInt("age") : null` | `StudentDao.rowToStudent()` 第 126 行 |

**原理**：`ResultSet.getInt()` 在数据库值为 NULL 时会返回 0（默认值），而不是 null。需要先通过 `getObject()` 判断是否为 NULL，再决定如何取值。这是 JDBC API 设计的一个经典坑。

---

## 七、MySQL JDBC URL 参数详解

```text
jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

| 组件 | 含义 |
|------|------|
| `jdbc:mysql://` | 协议标识，指定 MySQL 连接 |
| `localhost` | 主机地址（本地） |
| `3306` | MySQL 默认端口 |
| `mydb` | 目标数据库名 |

| 参数 | 作用 |
|------|------|
| `useSSL=false` | 关闭 SSL：本地开发环境不需要加密连接，避免 SSL 握手开销 |
| `serverTimezone=UTC` | 指定时区：MySQL 8.x 驱动要求显式设置，否则报 `The server time zone value...` 错误 |
| `allowPublicKeyRetrieval=true` | 允许客户端获取服务器公钥：使用 `caching_sha2_password` 认证插件时需要 |

---

## 八、驱动加载机制

```java
static {
    Class.forName("com.mysql.cj.jdbc.Driver");
}
```

`Class.forName()` 触发类的静态初始化，MySQL 驱动类中的静态块调用 `DriverManager.registerDriver()` 注册自身。之后的 `DriverManager.getConnection()` 会遍历所有已注册驱动，找到匹配 URL 协议的驱动来创建连接。

> **注意**：从 JDBC 4.0（Java 6）开始，`Class.forName()` 理论上可省略（驱动通过 SPI 自动注册）。但显式声明可以避免类加载顺序问题，且让代码意图更清晰。`com.mysql.cj.jdbc.Driver` 中的 `cj` 代表 Connector/J 8.x 后的新包名。

---

## 九、三层架构设计

```
Main (入口层)
  │
  ├── model/Student    (实体层) —— 纯粹的数据载体，字段与表列一一映射
  │
  ├── dao/StudentDao   (数据访问层) —— 封装所有 SQL，对外暴露面向对象的方法
  │
  └── util/DBUtil      (工具层) —— 连接获取与释放，独立于具体业务
```

**分层好处**：
- DAO 不管理连接生命周期，只写 SQL 逻辑
- DBUtil 可被多个 DAO 复用，换数据库只需改一处
- 实体类独立于 DAO，可在不同层之间传递

---

## 十、扩展延伸知识

### 项目中未涉及但应了解的内容

| 知识点 | 简要说明 |
|--------|---------|
| **事务管理** | `conn.setAutoCommit(false)` + `commit()` / `rollback()`，保证多条 SQL 的原子性。本项目每次操作独立获取连接，没有跨操作事务需求 |
| **连接池** | 生产环境不用 `DriverManager` 直连，改用 HikariCP / Druid 等连接池复用连接 |
| **批处理** | `ps.addBatch()` + `ps.executeBatch()`，批量插入大量数据时性能远超逐条插入 |
| **ORM 框架** | MyBatis、Hibernate/JPA 在 JDBC 之上封装，简化 SQL 编写和结果映射 |

### 安全提示

- pom.xml 中 `mysql-connector-j:8.0.33` 存在 CVE-2023-22102（CVSS 8.3），建议关注 MySQL 官方更新
- 生产环境须使用强密码，禁用 `allowPublicKeyRetrieval=true`（本地开发除外）

---

## 推荐学习路径

| 阶段 | 资料 | 说明 |
|------|------|------|
| 入门 | **《SQL 必知必会》**（Ben Forta） | 最精简的 SQL 入门书，覆盖 CRUD、JOIN、聚合、子查询 |
| 进阶 | **MySQL 官方文档**（dev.mysql.com/doc） | 第一手参考资料，尤其是 SQL Syntax 和 Optimization 章节 |
| JDBC | **Oracle 官方 JDBC Tutorial** | JDBC 基础的标准教程，含事务、连接池示例 |
| 深入 | **《高性能 MySQL》第4版** | 索引优化、查询优化、复制、分库分表等生产级知识 |
| 安全 | **OWASP SQL Injection Prevention Cheat Sheet** | 防御 SQL 注入的权威参考 |
