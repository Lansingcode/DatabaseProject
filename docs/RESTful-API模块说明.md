# RESTful API 模块说明

> 本项目包含两个独立的 RESTful API 实现，功能完全相同（student 表 CRUD），分别用 **Java + Spring Boot** 和 **Node.js + Express** 实现，便于横向对比学习。

---

## 一、项目结构

```
DatabaseProject/
│
├── src/main/java/org/database/          ← Java Spring Boot 模块
│   ├── Application.java                 # Spring Boot 入口，启动时自动建表
│   ├── controller/
│   │   └── StudentController.java       # REST 控制器
│   ├── service/
│   │   └── StudentService.java          # 业务层
│   ├── dao/
│   │   └── StudentDao.java              # JDBC 数据访问层
│   ├── model/
│   │   └── Student.java                 # 实体类
│   └── util/
│       └── DBUtil.java                  # JDBC 连接工具
│
├── node-rest-api/                       ← Node.js Express 模块
│   ├── server.js                        # Express 入口，启动时自动建表
│   ├── routes/
│   │   └── students.js                  # 路由处理器（CRUD）
│   ├── db/
│   │   └── connection.js                # MySQL 连接池 + 建表
│   ├── package.json
│   └── .gitignore
│
└── docs/                                ← 文档
```

---

## 二、API 端点对照

两个模块提供完全一致的 RESTful API：

| 方法 | 端点 | 功能 | HTTP 状态码（成功） | HTTP 状态码（失败） |
|------|------|------|--------------------|--------------------|
| `GET` | `/api/students` | 查询全部 | 200 OK | 500 |
| `GET` | `/api/students/{id}` | 按 id 查询 | 200 OK | 404 / 500 |
| `POST` | `/api/students` | 新增 | 201 Created | 400（缺 name）/ 500 |
| `PUT` | `/api/students/{id}` | 全量更新 | 200 OK | 400 / 404 / 500 |
| `DELETE` | `/api/students/{id}` | 删除 | 204 No Content | 404 / 500 |

### 请求体示例（POST / PUT）

```json
{
  "name": "张三",
  "age": 20,
  "grade": "大三"
}
```

### curl 测试命令（两个模块仅端口不同）

```bash
# Java 模块 — 端口 8080
curl -X GET    http://localhost:8080/api/students
curl -X POST   http://localhost:8080/api/students -H "Content-Type: application/json" -d '{"name":"赵六","age":22,"grade":"大四"}'
curl -X PUT    http://localhost:8080/api/students/1 -H "Content-Type: application/json" -d '{"name":"张三丰","age":23,"grade":"研一"}'
curl -X DELETE http://localhost:8080/api/students/1

# Node.js 模块 — 端口 3000
curl -X GET    http://localhost:3000/api/students
curl -X POST   http://localhost:3000/api/students -H "Content-Type: application/json" -d '{"name":"赵六","age":22,"grade":"大四"}'
curl -X PUT    http://localhost:3000/api/students/1 -H "Content-Type: application/json" -d '{"name":"张三丰","age":23,"grade":"研一"}'
curl -X DELETE http://localhost:3000/api/students/1
```

---

## 三、Java 模块（Spring Boot 2.7.18 + JDBC）

### 启动方式

```bash
# 命令行启动
mvn spring-boot:run

# 或者在 IDE 中运行 Application.java 的 main 方法
```

### 端口

**8080**（Spring Boot 默认，可在 `src/main/resources/application.properties` 中修改 `server.port`）

### 架构分层

```
HTTP 请求
  │
  ▼
StudentController  (接收请求, 参数校验, 返回 JSON)
  │  @RestController + @RequestMapping
  ▼
StudentService     (业务逻辑层, 调用 DAO)
  │  @Service
  ▼
StudentDao         (数据访问层, 纯 JDBC, PreparedStatement 防注入)
  │
  ▼
DBUtil.getConnection()  (连接管理)
  │
  ▼
MySQL (mydb.student)
```

### 关键注解说明

| 注解 | 作用 |
|------|------|
| `@RestController` | 将类标记为 REST 控制器，方法的返回值自动序列化为 JSON |
| `@RequestMapping("/api/students")` | 为该控制器内所有端点指定 URL 前缀 |
| `@GetMapping("/{id}")` | 映射 GET 请求，`{id}` 为路径变量 |
| `@PostMapping` | 映射 POST 请求 |
| `@PutMapping("/{id}")` | 映射 PUT 请求 |
| `@DeleteMapping("/{id}")` | 映射 DELETE 请求 |
| `@PathVariable` | 提取 URL 路径中的变量 |
| `@RequestBody` | 将 HTTP 请求体 JSON 反序列化为 Java 对象 |
| `@Service` | 标记业务层组件，Spring 自动扫描并注入 |
| `@Autowired` | 依赖注入：Spring 自动为成员变量赋值 |
| `ResponseEntity` | 统一构建 HTTP 响应（状态码 + 响应体 + 响应头） |

---

## 四、Node.js 模块（Express 4.18 + mysql2）

### 启动方式

```bash
cd node-rest-api
npm start        # 普通启动
npm run dev      # 开发模式（文件变更自动重启，Node 18+）
```

### 端口

**3000**（可在 `server.js` 中修改 `PORT` 常量）

### 架构分层

```
HTTP 请求
  │
  ▼
server.js          (Express 实例, 注册中间件与路由)
  │
  ▼
routes/students.js (路由处理器, HTTP 语义 + SQL)
  │  express.Router()
  ▼
db/connection.js   (mysql2 连接池 + 建表)
  │  pool.getConnection() / pool.query()
  ▼
MySQL (mydb.student)
```

### 与 Java 模块的架构差异

| | Java Spring Boot | Node.js Express |
|------|-----------------|-----------------|
| 分层数量 | 3 层（Controller → Service → DAO） | 2 层（Routes → Pool） |
| 连接方式 | `DriverManager` 每次新建连接 | 连接池（`mysql2/promise`，复用连接） |
| 参数校验 | `@RequestBody` 自动反序列化 | `req.body` + 手动判空 |
| 错误处理 | 返回 null 后 Controller 判断 | try/catch + `res.status(500)` |
| JSON 解析 | Spring 自动 | `express.json()` 中间件 |

---

## 五、两个模块的设计意图

| 对比维度 | 结论 |
|---------|------|
| **SQL 注入防御** | 两边都用参数化查询（Java: PreparedStatement, Node: `?` 占位符），同等安全 |
| **连接管理** | Node 用连接池（更接近生产环境），Java 用 DriverManager（更接近教学环境） |
| **开发效率** | Node 少一层分母（Controller+Route 合二为一），但 Java 强类型更易重构 |
| **错误处理** | Java 统一通过 `ResponseEntity` 构建响应，Node 在每条路由中独立处理 |
| **适用范围** | Java 适合大型业务系统（类型安全 + 分层严谨），Node 适合轻量 API 和快速原型 |

---

## 六、测试流程

1. 确保本地 MySQL 已启动，`mydb` 数据库已存在
2. 修改密码：Java 模块改 `DBUtil.java`，Node 模块改 `db/connection.js`
3. 分别启动两个模块（端口不同，可同时运行）
4. 用 curl 或 Postman 测试 CRUD 端点

---

## 推荐学习资料

| 资料 | 内容 |
|------|------|
| **Spring Boot 官方文档**（Getting Started → Building a RESTful Web Service） | Spring Boot REST 入门教程 |
| **Express 官方指南**（expressjs.com/en/guide/routing.html） | Express 路由与中间件 |
| **《REST in Practice》**（Jim Webber） | RESTful 架构的企业级实践经验 |
| **mysql2 文档**（npmjs.com/package/mysql2） | Node.js MySQL 连接池与 Promise API |
