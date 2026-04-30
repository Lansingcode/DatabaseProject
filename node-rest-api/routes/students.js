const express = require('express');
const router = express.Router();
const { pool } = require('../db/connection');

/**
 * GET /api/students —— 查询全部学生
 */
router.get('/', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT id, name, age, grade FROM student');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /api/students/:id —— 按 id 查询
 */
router.get('/:id', async (req, res) => {
  try {
    const [rows] = await pool.query(
      'SELECT id, name, age, grade FROM student WHERE id = ?',
      [req.params.id]
    );
    if (rows.length === 0) {
      return res.status(404).json({ error: '学生不存在' });
    }
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * POST /api/students —— 新增学生
 */
router.post('/', async (req, res) => {
  try {
    const { name, age, grade } = req.body;
    if (!name) {
      return res.status(400).json({ error: 'name 字段不能为空' });
    }
    const [result] = await pool.query(
      'INSERT INTO student (name, age, grade) VALUES (?, ?, ?)',
      [name, age ?? null, grade ?? null]
    );
    // 查询刚插入的记录返回完整对象
    const [rows] = await pool.query(
      'SELECT id, name, age, grade FROM student WHERE id = ?',
      [result.insertId]
    );
    res.status(201).json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * PUT /api/students/:id —— 全量更新学生信息
 */
router.put('/:id', async (req, res) => {
  try {
    const { name, age, grade } = req.body;
    if (!name) {
      return res.status(400).json({ error: 'name 字段不能为空' });
    }
    const [result] = await pool.query(
      'UPDATE student SET name = ?, age = ?, grade = ? WHERE id = ?',
      [name, age ?? null, grade ?? null, req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: '学生不存在' });
    }
    const [rows] = await pool.query(
      'SELECT id, name, age, grade FROM student WHERE id = ?',
      [req.params.id]
    );
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * DELETE /api/students/:id —— 删除学生
 */
router.delete('/:id', async (req, res) => {
  try {
    const [result] = await pool.query(
      'DELETE FROM student WHERE id = ?',
      [req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: '学生不存在' });
    }
    res.status(204).send();
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
