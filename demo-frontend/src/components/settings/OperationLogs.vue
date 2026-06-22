<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <div style="display: flex; gap: 12px">
        <el-select v-model="filterUser" placeholder="操作用户" style="width: 150px" clearable>
          <el-option label="全部" value="" />
          <el-option label="管理员" value="管理员" />
          <el-option label="张审核" value="张审核" />
          <el-option label="王小美" value="王小美" />
        </el-select>
        <el-select v-model="filterModule" placeholder="操作模块" style="width: 150px" clearable>
          <el-option label="全部" value="" />
          <el-option label="知识库" value="knowledge" />
          <el-option label="对话" value="chat" />
          <el-option label="客服" value="cs" />
          <el-option label="系统" value="system" />
        </el-select>
        <el-date-picker v-model="filterDate" type="date" placeholder="选择日期" style="width: 150px" />
      </div>
      <el-button type="primary" @click="fetchLogs">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
    </div>

    <el-table :data="logs" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="user" label="操作用户" width="100" />
      <el-table-column prop="module" label="模块" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="moduleType(row.module)">{{ row.module }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="操作" min-width="200" show-overflow-tooltip />
      <el-table-column prop="target" label="操作对象" min-width="150" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="result" label="结果" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === '成功' ? 'success' : 'danger'">{{ row.result }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="170" />
    </el-table>

    <el-pagination
      class="mt-20"
      background
      layout="total, prev, pager, next"
      :total="logs.length"
      :page-size="10"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const logs = ref([])
const loading = ref(false)
const filterUser = ref('')
const filterModule = ref('')
const filterDate = ref(null)

const moduleType = (m) => ({ knowledge: 'primary', chat: 'success', cs: 'warning', system: 'info' }[m] || 'info')

const fetchLogs = async () => {
  loading.value = true
  try {
    const data = await api.get('/v1/system/logs')
    if (data && data.length) logs.value = data
    else throw new Error('no data')
  } catch (e) {
    logs.value = demoLogs
  } finally {
    loading.value = false
  }
}

const demoLogs = [
  { id: 1, user: '管理员', module: 'knowledge', action: '新增知识分类', target: '分类: 停车服务', ip: '192.168.1.100', result: '成功', createTime: '2026-06-11 14:32:15' },
  { id: 2, user: '张审核', module: 'knowledge', action: '审核知识条目', target: 'ID: 6 - 退换货流程', ip: '192.168.1.102', result: '成功', createTime: '2026-06-11 13:45:22' },
  { id: 3, user: '管理员', module: 'system', action: '添加敏感词', target: '敏感词: 退钱', ip: '192.168.1.100', result: '成功', createTime: '2026-06-11 12:30:10' },
  { id: 4, user: '王小美', module: 'cs', action: '接入客服会话', target: '会话: cs-001 - 张先生', ip: '192.168.1.105', result: '成功', createTime: '2026-06-11 11:20:33' },
  { id: 5, user: '王小美', module: 'cs', action: '关闭客服会话', target: '会话: cs-001 - 张先生', ip: '192.168.1.105', result: '成功', createTime: '2026-06-11 11:45:18' },
  { id: 6, user: '管理员', module: 'knowledge', action: '上传知识文件', target: '文件: 618促销活动方案.xlsx', ip: '192.168.1.100', result: '成功', createTime: '2026-06-11 10:15:42' },
  { id: 7, user: '管理员', module: 'knowledge', action: '触发文件解析', target: '文件ID: 3', ip: '192.168.1.100', result: '成功', createTime: '2026-06-11 10:16:05' },
  { id: 8, user: '张审核', module: 'knowledge', action: '驳回知识条目', target: 'ID: 7 - 618活动优惠', ip: '192.168.1.102', result: '成功', createTime: '2026-06-11 09:30:55' },
  { id: 9, user: '管理员', module: 'system', action: '修改角色权限', target: '角色: 客服人员', ip: '192.168.1.100', result: '成功', createTime: '2026-06-10 17:00:12' },
  { id: 10, user: '管理员', module: 'system', action: '重置演示数据', target: '全部数据', ip: '192.168.1.100', result: '成功', createTime: '2026-06-10 09:00:00' },
]

onMounted(fetchLogs)
</script>
