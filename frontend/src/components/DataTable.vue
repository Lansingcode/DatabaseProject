<template>
  <div>
    <div class="topbar">
      <h1>{{ store.currentTable }}</h1>
      <div class="actions">
        <el-button type="primary" @click="$emit('add-row')">
          <el-icon><Plus /></el-icon> 添加行
        </el-button>
        <el-button @click="$emit('edit-table')">修改表</el-button>
        <el-button type="danger" @click="$emit('delete-table')">删除表</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="store.loading">
      <el-table :data="store.rows" stripe border style="width: 100%" empty-text="暂无数据，点击「添加行」新增记录">
        <el-table-column
          v-for="col in store.columns"
          :key="col.name"
          :prop="col.name"
          :label="col.name"
          :sortable="col.type.toUpperCase().startsWith('INT')"
          min-width="120"
        >
          <template #header>
            <span>{{ col.name }}</span>
            <div style="font-weight:400;font-size:11px;color:#aaa;">{{ col.type }}</div>
          </template>
          <template #default="{ row }">
            <span :class="{ 'null-val': row[col.name] == null }">
              {{ row[col.name] != null ? row[col.name] : 'NULL' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row, $index }">
            <el-button size="small" type="primary" link @click="$emit('edit-row', $index)">编辑</el-button>
            <el-button size="small" type="danger" link @click="$emit('delete-row', $index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
import { useTableStore } from '../stores/table'

const store = useTableStore()
defineEmits(['add-row', 'edit-row', 'delete-row', 'edit-table', 'delete-table'])
</script>

<style scoped>
.topbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.topbar h1 { font-size: 1.4rem; color: #1a1a2e; margin: 0; }
.actions { display: flex; gap: 8px; }
.null-val { color: #ccc; font-style: italic; }
</style>
