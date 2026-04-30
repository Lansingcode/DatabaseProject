const express = require('express');
const { createTable } = require('./db/connection');
const studentsRouter = require('./routes/students');

const app = express();
const PORT = 3000;

// 中间件：解析 JSON 请求体
app.use(express.json());

// 路由注册
app.use('/api/students', studentsRouter);

// 启动服务
app.listen(PORT, async () => {
  await createTable();
  console.log(`Node.js REST API 已启动: http://localhost:${PORT}/api/students`);
});
