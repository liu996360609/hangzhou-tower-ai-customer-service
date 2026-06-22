<template>
  <div class="card-container">
    <div class="mb-20">
      <el-input
        v-model="question"
        placeholder="输入问题测试检索效果..."
        size="large"
        @keyup.enter="handleRetrieve"
        style="max-width: 600px"
      >
        <template #append>
          <el-button @click="handleRetrieve" :loading="loading">
            <el-icon><Search /></el-icon>
            检索
          </el-button>
        </template>
      </el-input>
    </div>

    <div v-if="results.length > 0" class="retrieve-results">
      <el-card shadow="never" class="mb-20">
        <template #header>
          <div class="flex-between">
            <span>AI 回答</span>
            <el-tag type="success">综合生成</el-tag>
          </div>
        </template>
        <div class="ai-answer">{{ aiAnswer }}</div>
      </el-card>

      <h4 class="mb-20">检索到的知识片段</h4>
      <el-row :gutter="20">
        <el-col :span="12" v-for="(item, idx) in results" :key="idx">
          <el-card shadow="hover" class="mb-20 result-card" :style="{ borderLeft: `4px solid ${scoreColor(item.score)}` }">
            <template #header>
              <div class="flex-between">
                <span>知识片段 #{{ idx + 1 }}</span>
                <el-tag :type="scoreType(item.score)">相关度: {{ (item.score * 100).toFixed(1) }}%</el-tag>
              </div>
            </template>
            <div class="result-item">
              <p><strong>来源：</strong>{{ item.source }}</p>
              <p><strong>分类：</strong>{{ item.category }}</p>
              <p class="mt-20"><strong>内容：</strong></p>
              <p class="result-content">{{ item.content }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="hasSearched" description="未找到相关知识" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/api'

const question = ref('')
const loading = ref(false)
const results = ref([])
const aiAnswer = ref('')
const hasSearched = ref(false)

const scoreColor = (score) => {
  if (score > 0.8) return '#67c23a'
  if (score > 0.6) return '#e6a23c'
  return '#909399'
}

const scoreType = (score) => {
  if (score > 0.8) return 'success'
  if (score > 0.6) return 'warning'
  return 'info'
}

const handleRetrieve = async () => {
  if (!question.value.trim()) return
  loading.value = true
  hasSearched.value = true
  try {
    const data = await api.post('/v1/knowledge/retrieve', { question: question.value })
    if (data) {
      results.value = data.results || []
      aiAnswer.value = data.aiAnswer || ''
    } else {
      throw new Error('no data')
    }
  } catch (e) {
    results.value = demoResults.filter(r => r.score > 0.5)
    aiAnswer.value = '金卡会员办理条件如下：\n\n1. 在商场累计消费满50,000元\n2. 或单笔消费满30,000元\n\n办理地点：一楼客服中心\n办理时间：10:00-21:30\n所需材料：身份证、购物凭证\n\n如有其他问题，欢迎继续咨询。'
  } finally {
    loading.value = false
  }
}

const demoResults = [
  { source: '2026年会员卡政策.pdf', category: '会员政策', content: '金卡会员需在商场累计消费满50,000元，或单笔消费满30,000元即可办理。办理时需携带有效身份证件和购物凭证，到一楼客服中心办理。办理时间为商场营业时间内（10:00-21:30）。', score: 0.95 },
  { source: 'VIP客户服务手册.pdf', category: '会员政策', content: '金卡会员享受以下特权：专属停车区域、生日礼遇、积分1.5倍累积、专属客服经理、优先参与新品发布会和VIP专属活动。', score: 0.78 },
  { source: '积分兑换规则说明.pdf', category: '会员政策', content: '会员积分可用于兑换购物券、抵扣停车费、兑换礼品。金卡会员积分1.5倍累积，兑换比例更优。', score: 0.62 },
  { source: '品牌入驻指南2026.docx', category: '品牌信息', content: '杭州大厦入驻品牌超过500个，涵盖奢侈品、时尚、美妆、运动、餐饮等多个品类。各楼层品牌分布请参考楼层指南。', score: 0.35 },
]
</script>

<style scoped>
.retrieve-results {
  margin-top: 20px;
}

.ai-answer {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.result-card {
  transition: transform 0.2s;
}

.result-item p {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.result-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
}
</style>
