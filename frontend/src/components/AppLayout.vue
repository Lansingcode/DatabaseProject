<template>
  <el-container class="app-container">
    <el-aside width="220px" class="sidebar">
      <Sidebar @create="$emit('create-table')" @create-database="$emit('create-database')" @delete-database="$emit('delete-database')" />
    </el-aside>
    <el-main class="main-area">
      <slot />
    </el-main>
  </el-container>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
</script>

<style scoped>
.app-container { height: 100vh; }
.sidebar { background: #1a1a2e; overflow-y: auto; }
.main-area { background: #f0f2f5; padding: 20px 24px; overflow-y: auto; }
</style>
