<template>
  <el-dialog v-model="visible" :title="title" width="500px" :close-on-click-modal="false" @close="reset">
    <el-form label-position="top" @submit.prevent="submit">
      <el-form-item
        v-for="col in store.columns"
        :key="col.name"
        :label="col.name + ' (' + col.type + ')'"
      >
        <el-input
          v-if="!isTextType(col.type)"
          v-model="form[col.name]"
          :disabled="isDisabled(col)"
          :type="isNumeric(col.type) ? 'number' : 'text'"
        />
        <el-input
          v-else
          v-model="form[col.name]"
          type="textarea"
          :rows="3"
          :disabled="isDisabled(col)"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="submitting">{{ isEdit ? '保存' : '创建' }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useTableStore } from '../stores/table'

const store = useTableStore()
const visible = ref(false)
const submitting = ref(false)
const editingRowIndex = ref(null)
const form = reactive({})

const isEdit = computed(() => editingRowIndex.value != null)
const title = computed(() => (isEdit.value ? '编辑行: ' : '添加行: ') + store.currentTable)

function isDisabled(col) {
  if (!isEdit.value && col.autoIncrement) return true
  if (isEdit.value && col.primaryKey) return true
  return false
}

function isNumeric(type) {
  const t = type.toUpperCase()
  return t.startsWith('INT') || t.startsWith('BIGINT') || t.startsWith('DOUBLE') || t.startsWith('FLOAT')
}

function isTextType(type) {
  return type.toUpperCase().startsWith('TEXT')
}

function reset() {
  editingRowIndex.value = null
  for (const key of Object.keys(form)) delete form[key]
}

async function submit() {
  const rowData = {}
  for (const col of store.columns) {
    if (isDisabled(col)) continue
    const val = form[col.name]
    if (val === '' || val == null) {
      rowData[col.name] = null
    } else if (isNumeric(col.type)) {
      rowData[col.name] = Number(val)
    } else {
      rowData[col.name] = val
    }
  }

  const pk = store.getPkColumn()
  const rowId = isEdit.value && pk ? store.rows[editingRowIndex.value][pk.name] : null

  submitting.value = true
  try {
    await store.saveRow(store.currentTable, rowData, rowId)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    visible.value = false
    reset()
  } catch (e) {
    ElMessage.error('操作失败: ' + e.message)
  } finally {
    submitting.value = false
  }
}

function open(rowData, rowIndex) {
  reset()
  for (const col of store.columns) {
    const val = rowData ? rowData[col.name] : ''
    form[col.name] = val != null ? val : ''
  }
  if (rowIndex != null) editingRowIndex.value = rowIndex
  visible.value = true
}

defineExpose({ open })
</script>
