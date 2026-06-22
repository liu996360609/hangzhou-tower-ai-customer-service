<template>
  <div>
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="角色权限" name="roles">
        <RoleManager />
      </el-tab-pane>
      <el-tab-pane label="敏感词管理" name="sensitive">
        <SensitiveWords />
      </el-tab-pane>
      <el-tab-pane label="操作日志" name="logs">
        <OperationLogs />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import RoleManager from '@/components/settings/RoleManager.vue'
import SensitiveWords from '@/components/settings/SensitiveWords.vue'
import OperationLogs from '@/components/settings/OperationLogs.vue'

const activeTab = ref('roles')
</script>
