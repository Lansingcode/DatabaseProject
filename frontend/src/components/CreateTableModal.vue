<template>
  <el-dialog v-model="visible" title="创建新表" width="560px" :close-on-click-modal="false" @close="reset">
    <el-form label-position="top" @submit.prevent="submit">
      <el-form-item label="表名" required>
        <el-input v-model="tableName" placeholder="如 course" maxlength="64" />
      </el-form-item>

      <div style="margin-bottom:8px;font-size:0.85rem;color:#666;">列定义</div>
      <div v-for="(col, i) in columns" :key="i" class="col-row">
        <el-input v-model="col.name" placeholder="列名" style="width:130px" maxlength="64" />
        <el-select v-model="col.type" style="width:140px">
          <el-option v-for="t in types" :key="t" :value="t" />
        </el-select>
        <el-checkbox v-model="col.notNull" label="NOT NULL" size="small" />
        <el-checkbox v-model="col.primaryKey" label="PK" size="small" />
        <el-checkbox v-model="col.autoIncrement" label="自增" size="small" />
        <el-button :icon="Delete" circle size="small" @click="removeCol(i)" :disabled="columns.length <= 1" />
      </div>
      <el-button @click="addCol" style="margin-top:8px">
        <el-icon><Plus /></el-icon> 添加列
      </el-button>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="submitting">创建</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useTableStore } from '../stores/table'

const store = useTableStore()
const visible = ref(false)
const submitting = ref(false)
const tableName = ref('')
const columns = reactive([newColumn()])

const types = ['INT', 'VARCHAR(100)', 'TEXT', 'BIGINT', 'DOUBLE', 'DATE', 'DATETIME', 'BOOLEAN']

function newColumn() {
  return { name: '', type: 'INT', notNull: false, primaryKey: false, autoIncrement: false }
}

function addCol() { columns.push(newColumn()) }
function removeCol(i) { columns.splice(i, 1) }

function reset() {
  tableName.value = ''
  columns.length = 0
  columns.push(newColumn())
}

async function submit() {
  if (!tableName.value.trim()) { ElMessage.error('请输入表名'); return }
  const cols = columns.filter(c => c.name.trim())
  if (!cols.length) { ElMessage.error('至少定义一个列'); return }
  const name = tableName.value.trim()
  submitting.value = true
  try {
    await store.createTable({ tableName: name, columns: cols })
    ElMessage.success('表创建成功')
    visible.value = false
    reset()
    await store.selectTable(name)
  } catch (e) {
    ElMessage.error('创建失败: ' + e.message)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open: () => { visible.value = true } })
</script>

<style scoped>
.col-row { display: flex; gap: 6px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
</style>
