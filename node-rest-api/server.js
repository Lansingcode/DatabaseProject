const express = require('express');
const { createTable } = require('./db/connection');
const studentsRouter = require('./routes/students');
const tablesRouter = require('./routes/tables');

const app = express();
const PORT = 3000;

app.use(express.json());

// 路由注册
app.use('/api/students', studentsRouter);   // 保留原有 student 专用路由
app.use('/api/tables', tablesRouter);       // 新增动态多表管理路由

app.listen(PORT, async () => {
  await createTable();
  console.log(`Node.js REST API 已启动: http://localhost:${PORT}`);
  console.log(`  表管理: http://localhost:${PORT}/api/tables`);
});
