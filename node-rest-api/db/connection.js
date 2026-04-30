const mysql = require('mysql2/promise');

// ============================================
// 请根据本地环境修改以下配置
// ============================================
const pool = mysql.createPool({
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: 'rootroot',   // ← 改为你的 root 密码
  database: 'mydb',
  waitForConnections: true,
  connectionLimit: 10,
});

/**
 * 建表：如果 student 表不存在则创建
 */
async function createTable() {
  const sql = `CREATE TABLE IF NOT EXISTS student (
    id    INT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(50) NOT NULL,
    age   INT,
    grade VARCHAR(20)
  )`;
  const conn = await pool.getConnection();
  try {
    await conn.execute(sql);
    console.log('[OK] 表 student 已就绪');
  } finally {
    conn.release();
  }
}

module.exports = { pool, createTable };
