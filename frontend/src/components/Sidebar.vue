<template>
  <div class="sidebar-inner">
    <h2 class="sidebar-title">数据库</h2>
    <div class="db-select">
      <div class="db-select-row">
        <el-select v-model="selectedDb" placeholder="选择数据库" size="small" @change="onDbChange" class="db-select-input">
          <el-option v-for="db in store.databases" :key="db" :label="db" :value="db" />
        </el-select>
        <el-button size="small" @click="$emit('create-database')" class="btn-add-db">
          <el-icon><Plus /></el-icon>
        </el-button>
        <el-button size="small" @click="$emit('delete-database')" class="btn-del-db" :disabled="store.databases.length <= 1">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
    <h2 class="sidebar-title">数据表</h2>
    <el-menu
      :default-active="store.currentTable"
      background-color="#1a1a2e"
      text-color="#ccc"
      active-text-color="#fff"
      @select="onSelect"
    >
      <el-menu-item
        v-for="t in store.tables"
        :key="t"
        :index="t"
      >
        <el-icon><Grid /></el-icon>
        <span>{{ t }}</span>
      </el-menu-item>
    </el-menu>
    <div class="sidebar-footer">
      <el-button type="primary" @click="$emit('create')" class="btn-new">
        <el-icon><Plus /></el-icon> 新建表
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Grid, Plus, Delete } from '@element-plus/icons-vue'
import { useTableStore } from '../stores/table'

const store = useTableStore()
defineEmits(['create', 'create-database', 'delete-database'])

const selectedDb = ref(store.currentDatabase)

watch(() => store.currentDatabase, (val) => { selectedDb.value = val })

function onDbChange(name) {
  store.switchDatabase(name)
}

function onSelect(name) {
  store.selectTable(name)
}
</script>

<style scoped>
.sidebar-inner { display: flex; flex-direction: column; height: 100%; }
.sidebar-title { padding: 16px 20px 12px; font-size: 1rem; color: #fff; border-bottom: 1px solid #333; margin: 0; }
.db-select { padding: 8px 12px; }
.db-select-row { display: flex; gap: 4px; align-items: center; }
.db-select-input { flex: 1; }
.btn-add-db { flex-shrink: 0; }
.btn-del-db { flex-shrink: 0; }
.sidebar-footer { padding: 12px 16px; }
.btn-new { width: 100%; }
</style>
