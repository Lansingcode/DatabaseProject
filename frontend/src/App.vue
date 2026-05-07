<template>
  <AppLayout @create-table="createTableModal.open()" @create-database="createDbModal.open()" @delete-database="onDeleteDatabase">
    <EmptyState v-if="!store.currentTable" />
    <DataTable
      v-else
      @add-row="rowForm.open(null, null)"
      @edit-row="onEditRow"
      @delete-row="onDeleteRow"
      @edit-table="editTableModal.open()"
      @delete-table="onDeleteTable"
    />
  </AppLayout>

  <CreateTableModal ref="createTableModal" />
  <EditTableModal ref="editTableModal" />
  <RowFormModal ref="rowForm" />
  <CreateDatabaseModal ref="createDbModal" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import AppLayout from './components/AppLayout.vue'
import EmptyState from './components/EmptyState.vue'
import DataTable from './components/DataTable.vue'
import CreateTableModal from './components/CreateTableModal.vue'
import EditTableModal from './components/EditTableModal.vue'
import RowFormModal from './components/RowFormModal.vue'
import CreateDatabaseModal from './components/CreateDatabaseModal.vue'
import { useTableStore } from './stores/table'

const store = useTableStore()
const createTableModal = ref(null)
const editTableModal = ref(null)
const rowForm = ref(null)
const createDbModal = ref(null)

onMounted(async () => {
  await store.fetchDatabases()
  await store.fetchTables()
})

function onEditRow(rowIndex) {
  rowForm.value.open(store.rows[rowIndex], rowIndex)
}

async function onDeleteRow(rowIndex) {
  const pkCol = store.getPkColumn()
  const pk = pkCol ? store.rows[rowIndex][pkCol.name] : rowIndex
  try {
    await ElMessageBox.confirm(`确定要删除 ID 为 ${pk} 的记录吗？`, '删除确认', {
      type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消',
    })
    await store.deleteTableRow(store.currentTable, pk)
    ElMessage.success('删除成功')
  } catch { /* 取消 */ }
}

async function onDeleteDatabase() {
  try {
    await ElMessageBox.confirm(
      `确定要删除数据库「${store.currentDatabase}」吗？此操作将删除库内所有数据且不可恢复。`,
      '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    const name = store.currentDatabase
    await store.deleteDatabase(name)
    ElMessage.success('数据库已删除')
  } catch { /* 取消 */ }
}

async function onDeleteTable() {
  try {
    await ElMessageBox.confirm(
      `确定要删除表「${store.currentTable}」吗？此操作将删除表中所有数据且不可恢复。`,
      '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    const name = store.currentTable
    await store.deleteTable(name)
    ElMessage.success('表已删除')
  } catch { /* 取消 */ }
}
</script>
