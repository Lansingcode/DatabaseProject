import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'

export const useTableStore = defineStore('table', () => {
  const databases = ref([])
  const currentDatabase = ref('')
  const tables = ref([])
  const currentTable = ref(null)
  const columns = ref([])
  const rows = ref([])
  const loading = ref(false)

  async function fetchDatabases() {
    databases.value = await api.listDatabases()
    const current = await api.getCurrentDb()
    currentDatabase.value = current.database
  }

  async function deleteDatabase(name) {
    await api.deleteDatabase(name)
    await fetchDatabases()
    clearSelection()
    tables.value = []
    await fetchTables()
  }

  async function createDatabase(name) {
    await api.createDatabase(name)
    await fetchDatabases()
    clearSelection()
    tables.value = []
    await fetchTables()
  }

  async function switchDatabase(name) {
    await api.switchDatabase(name)
    currentDatabase.value = name
    clearSelection()
    tables.value = []
    await fetchTables()
  }

  async function fetchTables() {
    tables.value = await api.listTables()
  }

  async function selectTable(name) {
    currentTable.value = name
    loading.value = true
    try {
      const [tableInfo, tableRows] = await Promise.all([
        api.getTable(name),
        api.getRows(name),
      ])
      columns.value = tableInfo.columns
      rows.value = tableRows
    } finally {
      loading.value = false
    }
  }

  function clearSelection() {
    currentTable.value = null
    columns.value = []
    rows.value = []
  }

  async function createTable(data) {
    await api.createTable(data)
    await fetchTables()
  }

  async function alterTable(name, data) {
    await api.alterTable(name, data)
    await selectTable(name)
  }

  async function deleteTable(name) {
    await api.deleteTable(name)
    clearSelection()
    await fetchTables()
  }

  async function saveRow(tableName, rowData, editingRowId) {
    if (editingRowId != null) {
      await api.updateRow(tableName, editingRowId, rowData)
    } else {
      await api.insertRow(tableName, rowData)
    }
    await loadRows(tableName)
  }

  async function deleteTableRow(tableName, id) {
    await api.deleteRow(tableName, id)
    await loadRows(tableName)
  }

  async function loadRows(tableName) {
    rows.value = await api.getRows(tableName)
  }

  function getPkColumn() {
    return columns.value.find(c => c.primaryKey)
  }

  return {
    databases, currentDatabase,
    tables, currentTable, columns, rows, loading,
    fetchDatabases, createDatabase, deleteDatabase, switchDatabase, fetchTables,
    selectTable, clearSelection,
    createTable, alterTable, deleteTable,
    saveRow, deleteTableRow, getPkColumn,
  }
})
