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
├── src/main/java/org/database/
│   ├── Application.java                    # Spring Boot 入口 (端口 8080)
│   ├── controller/
│   │   └── StudentController.java          # REST 控制器 (@RestController)
│   ├── service/
│   │   └── StudentService.java             # 业务层 (@Service)
│   ├── dao/
│   │   └── StudentDao.java                 # JDBC 数据访问 (PreparedStatement)
│   ├── model/
│   │   └── Student.java                    # 实体类
│   └── util/
│       └── DBUtil.java                     # 数据库连接工具
│
├── node-rest-api/                          # Node.js Express 模块 (端口 3000)
│   ├── server.js                           # Express 入口
│   ├── routes/students.js                  # 路由处理器 (CRUD)
│   ├── db/connection.js                    # mysql2 连接池
│   └── package.json
│
└── docs/                                   # 知识文档
    ├── JDBC-CRUD知识总结.md
    ├── HTTP访问数据库方式总结.md
    └── RESTful-API模块说明.md
```

## 构建与运行

```bash
# Java Spring Boot (端口 8080)
mvn spring-boot:run

# Node.js Express (端口 3000)
cd node-rest-api && npm start
```

## 回复风格

- 描述性文档、注释使用中文
- 代码标识符、技术术语保持原文
- 学习资料推荐时，说明资料的核心内容和适合的阅读阶段
