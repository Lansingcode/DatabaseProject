<template>
  <el-dialog v-model="visible" :title="'修改表: ' + store.currentTable" width="520px" :close-on-click-modal="false" @close="reset">
    <div style="margin-bottom:8px;font-size:0.85rem;color:#666;">新增列</div>
    <div v-for="(col, i) in addColumns" :key="'a'+i" class="col-row">
      <el-input v-model="col.name" placeholder="列名" style="width:130px" maxlength="64" />
      <el-select v-model="col.type" style="width:140px">
        <el-option v-for="t in types" :key="t" :value="t" />
      </el-select>
      <el-checkbox v-model="col.notNull" label="NOT NULL" size="small" />
      <el-button :icon="Delete" circle size="small" @click="addColumns.splice(i,1)" />
    </div>
    <el-button @click="addColumns.push({name:'',type:'INT',notNull:false})" size="small">
      <el-icon><Plus /></el-icon> 添加列
    </el-button>

    <el-divider />
    <div style="margin-bottom:8px;font-size:0.85rem;color:#666;">删除列</div>
    <el-checkbox-group v-model="dropColumns">
      <div v-for="col in store.columns" :key="col.name" style="margin-bottom:4px;">
        <el-checkbox :value="col.name" :disabled="col.primaryKey" size="small">
          {{ col.name }}
          <span style="color:#aaa;">({{ col.type }})</span>
          <span v-if="col.primaryKey" style="color:#ff4d4f;">主键不可删除</span>
        </el-checkbox>
      </div>
    </el-checkbox-group>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="submitting">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useTableStore } from '../stores/table'

const store = useTableStore()
const visible = ref(false)
const submitting = ref(false)
const addColumns = ref([])
const dropColumns = ref([])

const types = ['INT', 'VARCHAR(100)', 'TEXT', 'BIGINT', 'DOUBLE', 'DATE', 'DATETIME', 'BOOLEAN']

function reset() {
  addColumns.value = []
  dropColumns.value = []
}

async function submit() {
  const add = addColumns.value.filter(c => c.name.trim())
  if (!add.length && !dropColumns.value.length) {
    ElMessage.warning('没有变化')
    return
  }
  submitting.value = true
  try {
    await store.alterTable(store.currentTable, {
      addColumns: add.map(c => ({ name: c.name.trim(), type: c.type, notNull: c.notNull })),
      dropColumns: dropColumns.value,
    })
    ElMessage.success('表结构已更新')
    visible.value = false
    reset()
  } catch (e) {
    ElMessage.error('修改失败: ' + e.message)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open: () => { visible.value = true } })
</script>

<style scoped>
.col-row { display: flex; gap: 6px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
</style>
