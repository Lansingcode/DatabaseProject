<template>
  <el-dialog v-model="visible" title="创建新数据库" width="420px" :close-on-click-modal="false" @close="reset">
    <el-form label-position="top" @submit.prevent="submit">
      <el-form-item label="数据库名" required>
        <el-input v-model="dbName" placeholder="如 shop、blog" maxlength="64" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="submitting">创建</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useTableStore } from '../stores/table'

const store = useTableStore()
const visible = ref(false)
const submitting = ref(false)
const dbName = ref('')

function reset() {
  dbName.value = ''
}

async function submit() {
  if (!dbName.value.trim()) { ElMessage.error('请输入数据库名'); return }
  submitting.value = true
  try {
    await store.createDatabase(dbName.value.trim())
    ElMessage.success('数据库创建成功')
    visible.value = false
    reset()
  } catch (e) {
    ElMessage.error('创建失败: ' + e.message)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open: () => { visible.value = true } })
</script>
