# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目定位

本项目是一个**数据库知识记录仓库**，不以后续代码开发为主要目的。用户会在对话中提问数据库相关知识，需要：

1. **精确回复**：针对用户的问题给出准确、专业的数据库知识解答
2. **分析提问**：理解用户问题的背景和意图，识别知识薄弱点
3. **推荐资料**：基于用户的问题，主动推荐相关的学习资料（书籍、论文、官方文档、优质博客等）

## 项目技术栈（知识记录可能涉及的代码示例）

这是一个基于 Maven 的 Java 8 项目，可作为数据库知识学习的实验代码环境。

```bash
# 编译
mvn compile

# 运行测试
mvn test

# 打包（生成 JAR）
mvn package

# 清理构建产物
mvn clean
```

## 项目结构

```
DatabaseProject/
├── pom.xml                          # Maven 配置，groupId: org.database
├── src/main/java/org/database/      # 学习实验代码
└── src/test/java/org/database/      # 测试代码
```

- **语言版本**：Java 8（`JDK_1_8`）
- **构建工具**：Maven
- **IDE**：IntelliJ IDEA 项目

## 回复风格

- 描述性文档、注释使用中文
- 代码标识符、技术术语保持原文
- 学习资料推荐时，说明资料的核心内容和适合的阅读阶段
