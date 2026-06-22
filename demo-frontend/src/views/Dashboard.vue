<template>
  <div>
    <!-- 指标卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="item in metrics" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 会话趋势 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span class="card-title">近7天会话趋势</span>
              <el-radio-group v-model="trendRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="bar-chart">
            <div class="bar-item" v-for="(item, idx) in trendData" :key="idx">
              <div class="bar-value">{{ item.value }}</div>
              <div
                class="bar"
                :style="{ height: (item.value / maxTrend) * 150 + 'px', background: item.gradient }"
              ></div>
              <div class="bar-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 热门问题 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">热门问题 TOP 10</span>
          </template>
          <div class="hot-questions">
            <div
              v-for="(item, idx) in hotQuestions"
              :key="idx"
              class="hot-question-item"
            >
              <span class="rank" :class="{ 'rank-top3': idx < 3 }">{{ idx + 1 }}</span>
              <span class="question-text">{{ item.question }}</span>
              <span class="question-count">{{ item.count }}次</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期会话 -->
    <el-card shadow="hover" class="mt-20">
      <template #header>
        <span class="card-title">近期会话</span>
      </template>
      <el-table :data="recentSessions" stripe style="width: 100%">
        <el-table-column prop="sessionId" label="会话ID" width="180" />
        <el-table-column prop="memberName" label="会员" width="120" />
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="channel" label="渠道" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.channel === 'AI' ? 'primary' : 'success'">{{ row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80" />
        <el-table-column prop="satisfaction" label="满意度" width="100">
          <template #default="{ row }">
            <span v-if="row.satisfaction">{{ '⭐'.repeat(row.satisfaction) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api'
import { ChatLineRound, DataLine, SuccessFilled, User } from '@element-plus/icons-vue'

const trendRange = ref('week')
const trendData = ref([])
const hotQuestions = ref([])
const recentSessions = ref([])
const metrics = ref([
  { label: '总会话数', value: '12,847', icon: 'ChatLineRound', color: '#409eff' },
  { label: 'AI回答准确率', value: '94.2%', icon: 'SuccessFilled', color: '#67c23a' },
  { label: '转人工率', value: '12.5%', icon: 'DataLine', color: '#e6a23c' },
  { label: '客户满意度', value: '4.6/5.0', icon: 'User', color: '#f56c6c' }
])

const maxTrend = computed(() => Math.max(...trendData.value.map(d => d.value), 1))

const gradients = [
  'linear-gradient(180deg, #409eff 0%, #79bbff 100%)',
  'linear-gradient(180deg, #67c23a 0%, #b3e19d 100%)',
  'linear-gradient(180deg, #e6a23c 0%, #f5d291 100%)',
  'linear-gradient(180deg, #f56c6c 0%, #fab6b6 100%)',
]

const statusType = (status) => {
  const map = { '已关闭': 'info', '进行中': 'success', '已转人工': 'warning' }
  return map[status] || 'info'
}

const fetchDashboard = async () => {
  try {
    const data = await api.get('/v1/reports/dashboard')
    if (data) {
      // Map API data to metrics
      if (data.totalSessions) metrics.value[0].value = data.totalSessions.toLocaleString()
      if (data.aiAccuracy) metrics.value[1].value = (data.aiAccuracy * 100).toFixed(1) + '%'
      if (data.transferRate) metrics.value[2].value = (data.transferRate * 100).toFixed(1) + '%'
      if (data.satisfaction) metrics.value[3].value = data.satisfaction.toFixed(1) + '/5.0'
    }
  } catch (e) { console.log('Using demo data for dashboard') }

  // Demo trend data
  trendData.value = [
    { label: '周一', value: 156, gradient: gradients[0] },
    { label: '周二', value: 230, gradient: gradients[0] },
    { label: '周三', value: 189, gradient: gradients[0] },
    { label: '周四', value: 278, gradient: gradients[0] },
    { label: '周五', value: 245, gradient: gradients[0] },
    { label: '周六', value: 312, gradient: gradients[0] },
    { label: '周日', value: 198, gradient: gradients[0] }
  ]

  try {
    const hq = await api.get('/v1/reports/hot-questions')
    if (hq && hq.length) {
      hotQuestions.value = hq.slice(0, 10)
    } else {
      throw new Error('no data')
    }
  } catch (e) {
    hotQuestions.value = [
      { question: '如何办理会员卡？', count: 523 },
      { question: '停车场收费标准', count: 412 },
      { question: '营业时间是多少？', count: 387 },
      { question: '积分怎么兑换礼品？', count: 356 },
      { question: '有哪些品牌折扣活动？', count: 298 },
      { question: '如何查询积分余额？', count: 267 },
      { question: '发票怎么开具？', count: 245 },
      { question: '退换货流程是什么？', count: 234 },
      { question: 'VIP有哪些特权？', count: 198 },
      { question: '商场楼层分布', count: 176 }
    ]
  }

  try {
    const ss = await api.get('/v1/reports/sessions')
    if (ss && ss.length) {
      recentSessions.value = ss.slice(0, 10)
    } else {
      throw new Error('no data')
    }
  } catch (e) {
    recentSessions.value = [
      { sessionId: 'S20260611001', memberName: '张三', question: '如何办理金卡会员？', channel: 'AI', status: '已关闭', satisfaction: 5, duration: '2m', createTime: '2026-06-11 14:32:15' },
      { sessionId: 'S20260611002', memberName: '李四', question: '停车场周末收费', channel: 'AI', status: '已关闭', satisfaction: 4, duration: '1m', createTime: '2026-06-11 14:28:03' },
      { sessionId: 'S20260611003', memberName: '王五', question: '投诉某品牌服务态度', channel: '人工', status: '进行中', satisfaction: null, duration: '15m', createTime: '2026-06-11 14:15:42' },
      { sessionId: 'S20260611004', memberName: '赵六', question: '积分兑换规则', channel: 'AI', status: '已转人工', satisfaction: null, duration: '8m', createTime: '2026-06-11 13:58:20' },
      { sessionId: 'S20260611005', memberName: '孙七', question: '本周有哪些活动？', channel: 'AI', status: '已关闭', satisfaction: 5, duration: '3m', createTime: '2026-06-11 13:45:11' },
      { sessionId: 'S20260611006', memberName: '周八', question: '如何开发票', channel: 'AI', status: '已关闭', satisfaction: 3, duration: '4m', createTime: '2026-06-11 13:30:55' },
    ]
  }
}

onMounted(fetchDashboard)
</script>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.hot-questions {
  max-height: 360px;
  overflow-y: auto;
}

.hot-question-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}

.hot-question-item:last-child {
  border-bottom: none;
}

.rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  background: #f0f2f5;
  margin-right: 12px;
  flex-shrink: 0;
}

.rank-top3 {
  background: #409eff;
  color: #fff;
}

.question-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-count {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
  flex-shrink: 0;
}

.text-muted {
  color: #c0c4cc;
}
</style>
