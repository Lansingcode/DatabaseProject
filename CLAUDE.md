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
├── Dockerfile.java                         # Docker 多阶段构建
├── docker-compose.yml                      # Docker 编排 (MySQL + Java)
├── src/main/java/org/database/
│   ├── Application.java                    # Spring Boot 入口 (端口 8080)
│   ├── controller/
│   │   ├── StudentController.java          # Student REST 控制器
│   │   ├── SchemaController.java           # 表结构管理控制器
│   │   └── DynamicCrudController.java      # 动态 CRUD 控制器
│   ├── service/
│   │   ├── StudentService.java             # Student 业务层
│   │   ├── SchemaService.java              # 表结构管理服务
│   │   └── DynamicCrudService.java         # 动态 CRUD 服务
│   ├── dao/
│   │   └── StudentDao.java                 # JDBC 数据访问 (PreparedStatement)
│   ├── model/
│   │   ├── Student.java                    # 学生实体
│   │   ├── ColumnDef.java                  # 列定义
│   │   ├── ColumnInfo.java                 # 列信息
│   │   └── TableInfo.java                  # 表信息
│   └── util/
│       └── DBUtil.java                     # 数据库连接工具 (支持环境变量)
│
└── docs/                                   # 知识文档
    ├── JDBC-CRUD知识总结.md
    ├── HTTP访问数据库方式总结.md
    └── RESTful-API模块说明.md
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
# 前提：本地 MySQL 运行中，mydb 库已创建
mvn spring-boot:run
# 访问 http://localhost:8080
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
