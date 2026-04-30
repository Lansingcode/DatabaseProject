# 通过 HTTP 访问数据库的方式总结

> 核心问题：应用程序如何通过 HTTP 协议对数据库进行读写？本文将主流方案归纳为六大类，分析各自的架构模式与适用场景。

---

## 一、六种 HTTP 访问数据库方式

### 方式 1：自建 RESTful API（应用层封装）

**原理**：在后端应用中通过 JDBC/ORM 连接数据库，暴露 HTTP REST 接口供客户端调用。客户端不直接接触数据库。

```
客户端  ──HTTP──▶  应用服务(Controller → Service → DAO/JDBC)  ──TCP──▶  MySQL
```

**典型技术栈**：
- Java：Spring Boot + MyBatis / JPA
- Node.js：Express + Sequelize / Prisma
- Python：Flask + SQLAlchemy / FastAPI

**适用场景**：
- 业务逻辑复杂，接口与表结构需要解耦
- 需要权限控制、数据校验、缓存、限流等中间层能力
- 前后端分离架构（SPA / 移动端 / 第三方 API）

**优点**：最灵活，可完全自定义接口语义、鉴权、限流；不暴露数据库结构

**缺点**：开发工作量大，每个表都要写 Controller-Service-DAO 三层代码

---

### 方式 2：自动生成 REST API（数据库即 API）

**原理**：工具直接读取数据库元数据（information_schema），自动根据表结构生成 RESTful 端点，无需手写代码。

```
客户端  ──HTTP──▶  PostgREST / Hasura  ──TCP──▶  PostgreSQL
```

**代表工具**：

| 工具 | 适用数据库 | 特点 |
|------|-----------|------|
| **PostgREST** | PostgreSQL | 读取 schema 生成 REST，性能极高（C 语言编写） |
| **Hasura** | PostgreSQL | GraphQL 为主，同时支持 REST，附带权限系统 |
| **DreamFactory** | MySQL, PG, SQL Server 等 | REST + SOAP 自动生成，企业级功能 |
| **xmysql** | MySQL | 一行命令：`xmysql -h localhost -u root -p password -d mydb` |
| **MySQL CRUD API** | MySQL | Node.js 库，根据表结构自动注册 REST 路由 |

**适用场景**：
- 内部工具 / 管理后台：快速上线的 CRUD 页面
- MVP 原型验证：前端直接对接自动 API，省去后端开发
- 数据中台：将数据库的查询能力快速开放给数据消费者

**优点**：零代码，部署即用

**缺点**：自定义业务逻辑困难；API 结构与表结构强绑定（改表即改接口）

---

### 方式 3：GraphQL 中间层

**原理**：GraphQL 引擎接收客户端查询 → 解析 GraphQL AST → 翻译为 SQL → 执行并返回 JSON。

```
客户端  ──GraphQL query──▶  Hasura / Prisma / Apollo  ──SQL──▶  数据库
```

**代表工具**：

| 工具 | 特点 |
|------|------|
| **Hasura** | 实时订阅（WebSocket）+ 权限控制，连接 PG 直接可用 |
| **Prisma** | ORM + GraphQL 中间层，Schema-first 开发 |
| **Apollo Server + TypeORM** | 手动编写 Resolver，灵活度最高 |

**适用场景**：
- 前端需要灵活查询：不同页面需要不同字段组合，REST 会导致接口爆炸
- 多端差异大：Web 需要 10 个字段，移动端只需 3 个——GraphQL 按需返回
- 实时数据需求：Hasura 的 subscription 支持数据库变更实时推送

**与 REST 的核心差异**：

| | REST | GraphQL |
|------|------|------|
| 接口数量 | 每个资源多个端点 | 单一端点 |
| 返回字段 | 服务端定死 | 客户端指定 |
| 多次查询 | 需要多次请求 + 前端拼装 | 一次请求获取关联数据 |
| N+1 问题 | 需要后端手动优化 JOIN | DataLoader 自动批处理 |

**优点**：客户端精确控制返回字段，减少 over-fetching / under-fetching

**缺点**：增加了查询解析层，性能 overhead 比直连 REST 高；复杂查询的缓存策略比 REST 复杂（POST 方式不友好 CDN）

---

### 方式 4：数据库原生 HTTP 接口

**原理**：数据库自身提供 HTTP 服务，直接对外暴露 REST/SQL 接口。

```
客户端  ──HTTP──▶  数据库内置 HTTP Server（同一进程）
```

**原生支持的数据库**：

| 数据库 | HTTP 能力 | 说明 |
|--------|----------|------|
| **CouchDB** | 完整 REST API | 文档数据库，HTTP 是唯一通信协议 |
| **MongoDB Atlas** | Data API | 云端托管版提供的 HTTPS 接口 |
| **Elasticsearch** | 完整 REST API | 搜索与分析引擎，HTTP 是一等公民 |
| **Neo4j** | HTTP API | 图数据库，内置 Transactional Cypher HTTP Endpoint |
| **InfluxDB** | HTTP API | 时序数据库，写入/查询均走 HTTP |
| **MySQL** | MySQL HTTP Plugin（实验性） | 5.7+ 提供 `/sql` 端点直接执行 SQL |

**适用场景**：
- NoSQL 数据库的主要访问方式（CouchDB、ES 等设计就是 HTTP 优先）
- 边缘计算 / IoT 场景：设备直接通过 HTTP 写入时序数据库
- 无服务器架构（Serverless）：函数直连数据库 HTTP 接口，无需连接池

**优点**：零中间层，延迟最低；对 HTTP 客户端最友好（不需要数据库驱动 SDK）

**缺点**：安全性依赖数据库自身的认证；功能受限于数据库暴露的 HTTP 语义（如 MySQL HTTP Plugin 不支持 PreparedStatement）

---

### 方式 5：JDBC/ODBC over HTTP（桥接代理）

**原理**：在数据库前面部署一个代理，将 HTTP 请求转为数据库协议，或直接将 JDBC/ODBC 驱动走 HTTP 隧道。

```
客户端(JDBC驱动)  ──HTTP──▶  代理/网关  ──TCP──▶  数据库
```

**实现方案**：

| 方案 | 原理 |
|------|------|
| **Cloud SQL Proxy**（GCP） | 本地部署 sidecar，将 JDBC localhost 连接通过 HTTPS 隧道转发到云数据库 |
| **rqlite** | 基于 Raft 的 SQLite，提供 HTTP API |
| **Trino (Presto) Gateway** | 联邦查询引擎，HTTP 接口 → 翻译为各数据源 SQL |
| **Custom JDBC-HTTP Bridge** | 自建 Spring Boot 服务，接收 JDBC 请求转发至数据库 |

**适用场景**：
- 云环境：数据库在私有网络，应用不能直连，需要通过 Cloud SQL Proxy
- 跨网络访问：客户端在公网，数据库在内网，走 HTTP 隧道更安全
- 联邦查询：一个 HTTP 端点聚合多个异构数据源

**优点**：对应用层透明（JDBC 驱动视角无变化）；绕过网络隔离

**缺点**：额外一跳增加延迟；代理层自身需要高可用

---

### 方式 6：WebSocket / SSE 实时推送

**原理**：客户端通过 HTTP 升级为 WebSocket，或建立 SSE 长连接，数据库变更时服务端主动推送。

```
客户端  ◀──WebSocket/SSE(长连接)──  服务端  ──LISTEN/NOTIFY──▶  PostgreSQL
                                         │
                                         ├──CDC(Canal/Debezium)──▶  MySQL binlog
```

**代表方案**：

| 方案 | 机制 |
|------|------|
| **Hasura Subscription** | GraphQL + WebSocket，订阅表变更，查询转为 PG `LISTEN/NOTIFY` |
| **PostgREST + WebSocket** | 直接暴露 PostgreSQL 的 `LISTEN/NOTIFY` 为 WebSocket |
| **RethinkDB** | 原生 changefeed，变更实时推送到订阅客户端 |
| **Supabase Realtime** | PG 逻辑复制 → WebSocket 推送 |
| **Debezium + Kafka + WebSocket** | MySQL binlog → Kafka → WebSocket 网关 → 客户端 |

**适用场景**：
- 实时看板：数据库更新后页面无需刷新
- 协作应用：多人编辑同一数据（如在线文档）
- 消息通知：数据库写入事件触发客户端通知

**优点**：真正的实时推送，不需要客户端轮询

**缺点**：架构复杂（CDC、消息队列、推送网关）；需要维护长连接状态

---

## 二、六种方式对比总览

| 方式 | 典型延迟 | 开发成本 | 灵活度 | 安全可控性 | 主流场景 |
|------|---------|---------|--------|-----------|---------|
| 自建 REST | 低 | 高 | 极高 | 完全控制 | 业务系统 |
| 自动生成 REST | 低 | 零 | 低 | 中等 | 内部工具、MVP |
| GraphQL 中间层 | 中 | 中 | 高 | 高 | 多端差异大、复杂查询 |
| 数据库原生 HTTP | 最低 | 零 | 取决于DB | 依赖DB | NoSQL、IoT |
| JDBC over HTTP | 中 | 中 | 高 | 高 | 云环境、跨网络 |
| WebSocket/SSE | 最低(推送) | 高 | 高 | 高 | 实时看板、协作应用 |

## 三、选型决策流程

```
你的场景是什么？
  │
  ├── 业务系统，逻辑复杂 ───▶ 自建 REST API（Spring Boot）
  │
  ├── 内部管理后台，快速上线 ───▶ 自动生成 REST（PostgREST / xmysql）
  │
  ├── 前端需求多变，多端差异大 ───▶ GraphQL（Hasura / Apollo）
  │
  ├── 用 NoSQL（CouchDB/ES）───▶ 数据库原生 HTTP
  │
  ├── 数据库在私有网络/云上 ───▶ Cloud SQL Proxy / JDBC over HTTP
  │
  └── 需要实时数据推送 ───▶ WebSocket + CDC（Hasura Subscription / Supabase）
```

---

## 四、本项目的实践定位

本项目当前使用的是 **方式 1（自建 REST API 的前置步骤）**：

- `DBUtil` → JDBC 直连层
- `StudentDao` → 数据访问层（DAO）
- 在此之上添加 Spring Boot Controller，即可升级为完整的自建 REST API

---

## 推荐学习资料

| 阶段 | 资料 | 内容 |
|------|------|------|
| REST 设计 | **《RESTful Web APIs》**（Richardson & Amundsen） | REST API 设计的经典著作，含 HATEOAS 等进阶模式 |
| GraphQL | **《Learning GraphQL》**（Eve Porcello） | GraphQL 入门到服务端实现 |
| 实时推送 | **Debezium 官方文档** | CDC（Change Data Capture）标准方案 |
| 自动 API | **PostgREST 官方文档**（postgrest.org） | 数据库即 API 的最佳实践 |
| NoSQL HTTP | **CouchDB 官方文档** | 理解 HTTP 作为数据库访问协议的设计哲学 |
