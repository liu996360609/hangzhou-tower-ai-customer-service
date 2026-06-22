<template>
  <div class="card-container">
    <el-tabs v-model="reviewTab">
      <el-tab-pane :label="`待审核A (${pendingA.length})`" name="a">
        <el-table :data="pendingA" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="answer" label="答案" min-width="250" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleReview(row, 'approve', 'a')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReview(row, 'reject', 'a')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="`待审核B (${pendingB.length})`" name="b">
        <el-table :data="pendingB" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="answer" label="答案" min-width="250" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="reviewerA" label="审核人A" width="100" />
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleReview(row, 'approve', 'b')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReview(row, 'reject', 'b')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 驳回原因对话框 -->
    <el-dialog v-model="rejectVisible" title="驳回原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" rows="3" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const reviewTab = ref('a')
const reviews = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
let pendingReviewItem = null
let pendingReviewStage = ''

const pendingA = computed(() => reviews.value.filter(r => r.stage === 'a' && r.status === 'pending'))
const pendingB = computed(() => reviews.value.filter(r => r.stage === 'b' && r.status === 'pending'))

const fetchReviews = async () => {
  try {
    const data = await api.get('/v1/knowledge/reviews')
    if (data && data.length) reviews.value = data
    else throw new Error('no data')
  } catch (e) {
    reviews.value = [
      { id: 1, question: '退换货流程是什么？', answer: '7天内凭购物小票到客服中心办理退换货，需保持商品完好。', category: '会员政策', stage: 'a', status: 'pending', reviewerA: '-', submitTime: '2026-06-11 07:00:00' },
      { id: 2, question: '618活动有哪些优惠？', answer: '618期间全场消费满1000减200，部分品牌叠加优惠。', category: '促销活动', stage: 'a', status: 'pending', reviewerA: '-', submitTime: '2026-06-11 08:00:00' },
      { id: 3, question: '积分可以兑换什么？', answer: '积分可兑换购物券，5000积分=50元。', category: '会员政策', stage: 'b', status: 'pending', reviewerA: '张审核', submitTime: '2026-06-10 14:00:00' },
    ]
  }
}

const handleReview = async (row, action, stage) => {
  if (action === 'reject') {
    pendingReviewItem = row
    pendingReviewStage = stage
    rejectReason.value = ''
    rejectVisible.value = true
    return
  }
  try {
    await api.put(`/v1/knowledge/items/${row.id}/review`, { action: 'approve', reviewer: stage })
    ElMessage.success(`审核${stage.toUpperCase()}已通过`)
    if (stage === 'a') {
      row.stage = 'b'
      row.reviewerA = '我'
    } else {
      reviews.value = reviews.value.filter(r => r.id !== row.id)
    }
  } catch (e) {
    ElMessage.success(`审核${stage.toUpperCase()}已通过（演示模式）`)
    if (stage === 'a') {
      row.stage = 'b'
      row.reviewerA = '我'
    } else {
      reviews.value = reviews.value.filter(r => r.id !== row.id)
    }
  }
}

const confirmReject = async () => {
  if (!rejectReason.value) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await api.put(`/v1/knowledge/items/${pendingReviewItem.id}/review`, { action: 'reject', reviewer: pendingReviewStage, reason: rejectReason.value })
    ElMessage.warning(`审核${pendingReviewStage.toUpperCase()}已驳回`)
    reviews.value = reviews.value.filter(r => r.id !== pendingReviewItem.id)
    rejectVisible.value = false
  } catch (e) {
    ElMessage.warning(`审核${pendingReviewStage.toUpperCase()}已驳回（演示模式）`)
    reviews.value = reviews.value.filter(r => r.id !== pendingReviewItem.id)
    rejectVisible.value = false
  }
}

onMounted(fetchReviews)
</script>
