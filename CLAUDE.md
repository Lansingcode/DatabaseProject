# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目定位

本项目是一个**数据库知识记录仓库**，用户会在对话中提问数据库相关知识，需要：

1. **精确回复**：针对用户的问题给出准确、专业的数据库知识解答
2. **分析提问**：理解用户问题的背景和意图，识别知识薄弱点
3. **推荐资料**：基于用户的问题，主动推荐相关的学习资料（书籍、论文、官方文档、优质博客等）

## 项目结构

```
DatabaseProject/
├── pom.xml                                 # Maven + Spring Boot 2.7.18 + MySQL Connector
├── Dockerfile.java                         # Docker 三阶段构建 (Node 前端 + Maven + JRE)
├── docker-compose.yml                      # Docker 编排 (MySQL + Java)
├── frontend/                               # Vue 3 + Element Plus 前端
│   ├── src/
│   │   ├── App.vue                         # 根组件
│   │   ├── main.js                         # Vue 入口 (Element Plus + Pinia)
│   │   ├── api/index.js                    # REST API 封装 (9 个端点)
│   │   ├── stores/table.js                 # Pinia 状态管理
│   │   └── components/
│   │       ├── AppLayout.vue               # 左右布局
│   │       ├── Sidebar.vue                 # 侧边栏表列表
│   │       ├── DataTable.vue               # Element Plus 数据表格
│   │       ├── EmptyState.vue              # 空状态提示
│   │       ├── CreateTableModal.vue        # 创建表对话框
│   │       ├── EditTableModal.vue          # 修改表对话框
│   │       └── RowFormModal.vue            # 行表单对话框
│   └── vite.config.js                      # 构建配置 (输出到 ../src/main/resources/static)
├── src/main/java/org/database/
│   ├── Application.java                    # Spring Boot 入口 (端口 8080)
│   ├── controller/
│   │   ├── StudentController.java          # Student REST 控制器
│   │   ├── SchemaController.java           # 表结构管理控制器
│   │   └── DynamicCrudController.java      # 动态 CRUD 控制器
│   ├── service/                            # 业务层
│   ├── dao/                                # JDBC 数据访问
│   ├── model/                              # 实体类
│   └── util/
│       └── DBUtil.java                     # 数据库连接工具 (支持环境变量)
│
└── docs/                                   # 知识文档
```

## 构建与运行

### 方式一：Docker 部署（推荐）

```bash
# 启动（首次需构建镜像，约 2-3 分钟）
docker compose up -d --build

# 启动（已有镜像，几秒）
docker compose up -d

# 查看状态
docker compose ps

# 查看日志
docker logs db-java-api
docker logs db-mysql

# 重启
docker compose restart

# 停止（保留数据卷）
docker compose down

# 停止并删除数据卷（⚠️ 清空数据库）
docker compose down -v

# 查询数据库
MSYS_NO_PATHCONV=1 docker exec db-mysql mysql -u root -prootroot mydb -e "SHOW TABLES; SELECT * FROM student;"
```

### 方式二：本地开发

```bash
# 前端开发（Vite 热更新，端口 5173，自动代理 /api 到 8080）
cd frontend && npm run dev

# 后端：需要本地 MySQL 运行中，mydb 库已创建
mvn spring-boot:run

# 前端构建（产物输出到 src/main/resources/static/）
cd frontend && npm run build
```

### 端口与入口

| 服务 | 端口 | 入口 |
|------|------|------|
| 前端管理页面 | 8080 | http://localhost:8080 |
| REST API | 8080 | http://localhost:8080/api/students |
| MySQL（Docker） | 3307 | `mysql -u root -prootroot -P 3307 -h 127.0.0.1 mydb` |

## 回复风格

- 描述性文档、注释使用中文
- 代码标识符、技术术语保持原文
- 学习资料推荐时，说明资料的核心内容和适合的阅读阶段
