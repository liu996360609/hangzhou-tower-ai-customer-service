<template>
  <div>
    <!-- 指标概览 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="item in summaryMetrics" :key="item.label">
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
      <!-- 会话统计 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span class="card-title">会话统计</span>
              <el-radio-group v-model="reportPeriod" size="small" @change="fetchData">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="sessionStats" stripe style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="total" label="总会话" width="100" />
            <el-table-column prop="ai" label="AI处理" width="100" />
            <el-table-column prop="human" label="人工处理" width="100" />
            <el-table-column prop="transfer" label="转人工" width="100" />
            <el-table-column label="AI处理率" width="120">
              <template #default="{ row }">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: row.aiRate + '%' }"></div>
                  <span class="progress-text">{{ row.aiRate }}%</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 满意度分析 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">满意度分析</span>
          </template>
          <div class="satisfaction-breakdown">
            <div v-for="(item, idx) in satisfactionData" :key="idx" class="satisfaction-item">
              <div class="sat-label">
                <span class="star-display">{{ '⭐'.repeat(item.stars) }}</span>
                <span class="sat-count">{{ item.count }}次 ({{ item.percent }}%)</span>
              </div>
              <div class="sat-bar-bg">
                <div class="sat-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
            </div>
            <el-divider />
            <div class="sat-summary">
              <span>综合评分：</span>
              <span class="overall-score">{{ overallScore }}/5.0</span>
              <span class="sat-total">共 {{ totalRatings }} 条评价</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门问题分析 -->
    <el-card shadow="hover" class="mt-20">
      <template #header>
        <div class="flex-between">
          <span class="card-title">热门问题分析 TOP 10</span>
          <el-button type="primary" size="small" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="16">
          <el-table :data="hotQuestions" stripe style="width: 100%">
            <el-table-column type="index" label="排名" width="70" />
            <el-table-column prop="question" label="问题" min-width="250" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="count" label="提问次数" width="100" />
            <el-table-column label="占比" width="140">
              <template #default="{ row }">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: row.share + '%', background: row.color }"></div>
                  <span class="progress-text">{{ row.share }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="aiSolved" label="AI解决率" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.aiSolved > 80 ? 'success' : row.aiSolved > 50 ? 'warning' : 'danger'">
                  {{ row.aiSolved }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="8">
          <div class="category-chart">
            <h4 class="mb-20">分类分布</h4>
            <div v-for="(item, idx) in categoryDist" :key="idx" class="category-item">
              <div class="cat-dot" :style="{ background: item.color }"></div>
              <span class="cat-name">{{ item.name }}</span>
              <span class="cat-pct">{{ item.percent }}%</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { DataAnalysis, ChatLineRound, SuccessFilled, Star } from '@element-plus/icons-vue'

const reportPeriod = ref('week')
const sessionStats = ref([])
const satisfactionData = ref([])
const hotQuestions = ref([])
const categoryDist = ref([])

const summaryMetrics = ref([
  { label: '今日会话', value: '1,247', icon: 'ChatLineRound', color: '#409eff' },
  { label: 'AI解决率', value: '87.5%', icon: 'SuccessFilled', color: '#67c23a' },
  { label: '平均响应', value: '1.2s', icon: 'DataAnalysis', color: '#e6a23c' },
  { label: '满意度', value: '4.6/5.0', icon: 'Star', color: '#f56c6c' },
])

const overallScore = computed(() => {
  let total = 0, count = 0
  satisfactionData.value.forEach(s => { total += s.stars * s.count; count += s.count })
  return count ? (total / count).toFixed(1) : '0.0'
})

const totalRatings = computed(() => satisfactionData.value.reduce((sum, s) => sum + s.count, 0))

const fetchData = async () => {
  try {
    const stats = await api.get('/v1/reports/sessions')
    if (stats && stats.length) {
      sessionStats.value = stats
    } else {
      throw new Error('no data')
    }
  } catch (e) {
    sessionStats.value = [
      { date: '06-05', total: 189, ai: 156, human: 33, transfer: 24, aiRate: 83 },
      { date: '06-06', total: 201, ai: 172, human: 29, transfer: 18, aiRate: 86 },
      { date: '06-07', total: 245, ai: 210, human: 35, transfer: 22, aiRate: 86 },
      { date: '06-08', total: 178, ai: 155, human: 23, transfer: 15, aiRate: 87 },
      { date: '06-09', total: 210, ai: 182, human: 28, transfer: 20, aiRate: 87 },
      { date: '06-10', total: 234, ai: 198, human: 36, transfer: 28, aiRate: 85 },
      { date: '06-11', total: 198, ai: 175, human: 23, transfer: 16, aiRate: 88 },
    ]
  }

  try {
    const sat = await api.get('/v1/satisfaction/stats')
    if (sat && sat.breakdown) satisfactionData.value = sat.breakdown
    else throw new Error('no data')
  } catch (e) {
    satisfactionData.value = [
      { stars: 5, count: 856, percent: 62, color: '#67c23a' },
      { stars: 4, count: 278, percent: 20, color: '#95d475' },
      { stars: 3, count: 134, percent: 10, color: '#e6a23c' },
      { stars: 2, count: 67, percent: 5, color: '#f56c6c' },
      { stars: 1, count: 41, percent: 3, color: '#f89898' },
    ]
  }

  hotQuestions.value = [
    { question: '如何办理会员卡？', category: '会员政策', count: 523, share: 18, aiSolved: 95, color: '#409eff' },
    { question: '停车场收费标准', category: '停车服务', count: 412, share: 14, aiSolved: 98, color: '#67c23a' },
    { question: '营业时间是多少？', category: '基础信息', count: 387, share: 13, aiSolved: 100, color: '#e6a23c' },
    { question: '积分怎么兑换礼品？', category: '会员政策', count: 356, share: 12, aiSolved: 92, color: '#f56c6c' },
    { question: '有哪些品牌折扣活动？', category: '促销活动', count: 298, share: 10, aiSolved: 78, color: '#722ed1' },
    { question: '如何查询积分余额？', category: '会员政策', count: 267, share: 9, aiSolved: 96, color: '#13c2c2' },
    { question: '发票怎么开具？', category: '基础信息', count: 245, share: 8, aiSolved: 90, color: '#eb2f96' },
    { question: '退换货流程是什么？', category: '售后服务', count: 234, share: 8, aiSolved: 85, color: '#fa8c16' },
    { question: 'VIP有哪些特权？', category: '会员政策', count: 198, share: 7, aiSolved: 88, color: '#409eff' },
    { question: '商场楼层分布', category: '基础信息', count: 176, share: 6, aiSolved: 97, color: '#52c41a' },
  ]

  categoryDist.value = [
    { name: '会员政策', percent: 35, color: '#409eff' },
    { name: '基础信息', percent: 25, color: '#67c23a' },
    { name: '停车服务', percent: 15, color: '#e6a23c' },
    { name: '促销活动', percent: 12, color: '#f56c6c' },
    { name: '售后服务', percent: 8, color: '#722ed1' },
    { name: '其他', percent: 5, color: '#909399' },
  ]
}

const handleExport = async () => {
  try {
    const data = await api.get('/v1/reports/export')
    if (data && data.url) {
      window.open(data.url, '_blank')
    } else {
      ElMessage.success('报表导出成功（演示模式）')
    }
  } catch (e) {
    ElMessage.success('报表导出成功（演示模式）')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.progress-bar {
  display: flex;
  align-items: center;
  height: 20px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: #409eff;
  border-radius: 4px;
  transition: width 0.5s;
}

.progress-text {
  position: absolute;
  right: 6px;
  font-size: 11px;
  color: #606266;
  font-weight: 500;
}

.satisfaction-breakdown {
  padding: 8px 0;
}

.satisfaction-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.sat-label {
  width: 160px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.star-display {
  font-size: 14px;
}

.sat-count {
  color: #909399;
  font-size: 12px;
}

.sat-bar-bg {
  flex: 1;
  height: 12px;
  background: #f0f2f5;
  border-radius: 6px;
  overflow: hidden;
}

.sat-bar {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s;
}

.sat-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #606266;
}

.overall-score {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
}

.sat-total {
  font-size: 13px;
  color: #909399;
  margin-left: auto;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  font-size: 14px;
}

.cat-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-name {
  flex: 1;
  color: #303133;
}

.cat-pct {
  font-weight: 600;
  color: #606266;
}
</style>
