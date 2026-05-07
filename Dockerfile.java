# 第一阶段：Node.js 构建前端
FROM node:24-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 第二阶段：Maven 构建
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
COPY --from=frontend-build /frontend/dist/ ./src/main/resources/static/
RUN mvn package -DskipTests -B

# 第三阶段：JRE 运行
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
