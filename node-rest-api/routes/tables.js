const express = require('express');
const router = express.Router();
const { pool } = require('../db/connection');

// 标识符校验：仅允许字母、数字、下划线
const IDENT = /^[a-zA-Z_]\w*$/;
function validateName(name) {
  if (!name || !IDENT.test(name)) {
    throw new Error('非法标识符: ' + name);
  }
}

// ==================== 表结构管理 (DDL) ====================

/** GET /api/tables —— 列出所有用户表 */
router.get('/', async (req, res) => {
  try {
    const [rows] = await pool.query(
      "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'mydb' AND TABLE_TYPE = 'BASE TABLE'"
    );
    res.json(rows.map(r => r.TABLE_NAME));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/** GET /api/tables/:name —— 获取表结构 */
router.get('/:name', async (req, res) => {
  try {
    validateName(req.params.name);
    const [cols] = await pool.query(
      'SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, EXTRA ' +
      'FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ' +
      'ORDER BY ORDINAL_POSITION',
      ['mydb', req.params.name]
    );
    if (cols.length === 0) {
      return res.status(404).json({ error: '表不存在' });
    }
    res.json({
      tableName: req.params.name,
      columns: cols.map(c => ({
        name: c.COLUMN_NAME,
        type: c.COLUMN_TYPE,
        nullable: c.IS_NULLABLE === 'YES',
        primaryKey: c.COLUMN_KEY === 'PRI',
        autoIncrement: c.EXTRA.includes('auto_increment')
      }))
    });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** POST /api/tables —— 创建新表 */
router.post('/', async (req, res) => {
  try {
    const { tableName, columns } = req.body;
    if (!tableName || !columns || columns.length === 0) {
      return res.status(400).json({ error: 'tableName 和 columns 不能为空' });
    }
    validateName(tableName);
    columns.forEach(c => validateName(c.name));

    const colSql = columns.map(c => {
      let sql = `${c.name} ${c.type}`;
      if (c.notNull) sql += ' NOT NULL';
      if (c.autoIncrement) sql += ' AUTO_INCREMENT';
      if (c.primaryKey) sql += ' PRIMARY KEY';
      return sql;
    }).join(', ');

    await pool.execute(`CREATE TABLE ${tableName} (${colSql})`);
    res.json({ message: '表创建成功' });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** DELETE /api/tables/:name —— 删除表 */
router.delete('/:name', async (req, res) => {
  try {
    validateName(req.params.name);
    await pool.execute(`DROP TABLE IF EXISTS ${req.params.name}`);
    res.json({ message: '表已删除' });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** PUT /api/tables/:name —— 修改表（增/删列） */
router.put('/:name', async (req, res) => {
  const conn = await pool.getConnection();
  try {
    validateName(req.params.name);
    const { addColumns = [], dropColumns = [] } = req.body;

    for (const c of addColumns) {
      validateName(c.name);
      let sql = `ALTER TABLE ${req.params.name} ADD COLUMN ${c.name} ${c.type}`;
      if (c.notNull) sql += ' NOT NULL';
      await conn.execute(sql);
    }
    for (const col of dropColumns) {
      validateName(col);
      await conn.execute(`ALTER TABLE ${req.params.name} DROP COLUMN ${col}`);
    }
    res.json({ message: '表结构已更新' });
  } catch (err) {
    res.status(400).json({ error: err.message });
  } finally {
    conn.release();
  }
});

// ==================== 动态数据 CRUD ====================

/** GET /api/tables/:name/rows —— 查询全表 */
router.get('/:name/rows', async (req, res) => {
  try {
    validateName(req.params.name);
    const [rows] = await pool.query(`SELECT * FROM ${req.params.name}`);
    res.json(rows);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** GET /api/tables/:name/rows/:id —— 按主键查询单行 */
router.get('/:name/rows/:id', async (req, res) => {
  try {
    validateName(req.params.name);
    // 获取主键列名
    const [pkRows] = await pool.query(
      'SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = ?',
      ['mydb', req.params.name, 'PRI']
    );
    if (pkRows.length === 0) {
      return res.status(400).json({ error: '该表没有主键' });
    }
    const pkCol = pkRows[0].COLUMN_NAME;
    const [rows] = await pool.query(
      `SELECT * FROM ${req.params.name} WHERE ${pkCol} = ?`,
      [req.params.id]
    );
    if (rows.length === 0) {
      return res.status(404).json({ error: '记录不存在' });
    }
    res.json(rows[0]);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** POST /api/tables/:name/rows —— 新增行 */
router.post('/:name/rows', async (req, res) => {
  try {
    validateName(req.params.name);
    const result = await pool.query(`INSERT INTO ${req.params.name} SET ?`, [req.body]);
    const insertId = result[0].insertId;
    // 尝试读取刚插入的行
    try {
      const [pkRows] = await pool.query(
        'SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = ?',
        ['mydb', req.params.name, 'PRI']
      );
      if (pkRows.length > 0) {
        const pkCol = pkRows[0].COLUMN_NAME;
        const [rows] = await pool.query(
          `SELECT * FROM ${req.params.name} WHERE ${pkCol} = ?`, [insertId]
        );
        if (rows.length > 0) {
          return res.status(201).json(rows[0]);
        }
      }
    } catch (e) { /* fallback */ }
    res.status(201).json({ id: insertId, ...req.body });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** PUT /api/tables/:name/rows/:id —— 更新行 */
router.put('/:name/rows/:id', async (req, res) => {
  try {
    validateName(req.params.name);
    const [pkRows] = await pool.query(
      'SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = ?',
      ['mydb', req.params.name, 'PRI']
    );
    if (pkRows.length === 0) {
      return res.status(400).json({ error: '该表没有主键' });
    }
    const pkCol = pkRows[0].COLUMN_NAME;
    const [result] = await pool.query(
      `UPDATE ${req.params.name} SET ? WHERE ${pkCol} = ?`,
      [req.body, req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: '记录不存在' });
    }
    res.json({ message: '更新成功' });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/** DELETE /api/tables/:name/rows/:id —— 删除行 */
router.delete('/:name/rows/:id', async (req, res) => {
  try {
    validateName(req.params.name);
    const [pkRows] = await pool.query(
      'SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = ?',
      ['mydb', req.params.name, 'PRI']
    );
    if (pkRows.length === 0) {
      return res.status(400).json({ error: '该表没有主键' });
    }
    const pkCol = pkRows[0].COLUMN_NAME;
    const [result] = await pool.query(
      `DELETE FROM ${req.params.name} WHERE ${pkCol} = ?`,
      [req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: '记录不存在' });
    }
    res.status(204).send();
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

module.exports = router;
