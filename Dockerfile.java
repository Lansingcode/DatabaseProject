# 第一阶段：Maven 构建
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
# 提前下载依赖（利用 Docker 层缓存）
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# 第二阶段：JRE 运行
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV DB_NAME=mydb
ENV DB_USER=root
ENV DB_PASSWORD=rootroot

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
