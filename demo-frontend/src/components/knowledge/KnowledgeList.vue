<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <div style="display: flex; gap: 12px">
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px" clearable @change="fetchItems">
          <el-option label="全部" value="" />
          <el-option label="草稿" value="draft" />
          <el-option label="待审核" value="pending_review" />
          <el-option label="已发布" value="published" />
        </el-select>
        <el-input v-model="searchText" placeholder="搜索问题或答案" style="width: 250px" @keyup.enter="fetchItems" clearable />
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增知识
      </el-button>
    </div>

    <el-table :data="items" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" min-width="250" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="itemStatusType(row.status)">{{ itemStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="hitCount" label="命中次数" width="100" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="handleSubmitReview(row)" v-if="row.status === 'draft'">提交审核</el-button>
          <el-button link type="success" @click="handleView(row)">详情</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="问题" required>
          <el-input v-model="form.question" type="textarea" rows="2" placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="答案" required>
          <el-input v-model="form.answer" type="textarea" rows="6" placeholder="请输入答案" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const items = ref([])
const categories = ref([])
const statusFilter = ref('')
const searchText = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增知识')
const form = ref({ id: null, question: '', answer: '', categoryId: null, tags: '' })

const itemStatusType = (s) => ({ draft: 'info', pending_review: 'warning', published: 'success' }[s] || 'info')
const itemStatusText = (s) => ({ draft: '草稿', pending_review: '待审核', published: '已发布' }[s] || s)

const fetchItems = async () => {
  loading.value = true
  try {
    const data = await api.get('/v1/knowledge/items', { params: { status: statusFilter.value || undefined, keyword: searchText.value || undefined } })
    if (data && data.length) items.value = data
    else throw new Error('no data')
  } catch (e) {
    items.value = demoItems.filter(i => !statusFilter.value || i.status === statusFilter.value)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const data = await api.get('/v1/knowledge/categories')
    categories.value = data || []
  } catch (e) {
    categories.value = [{ id: 1, name: '会员政策' }, { id: 2, name: '品牌信息' }, { id: 3, name: '促销活动' }]
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增知识'
  form.value = { id: null, question: '', answer: '', categoryId: null, tags: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑知识'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info(`查看知识详情: ${row.question}`)
}

const handleSubmitReview = async (row) => {
  try {
    await api.post(`/v1/knowledge/items/${row.id}/submit-review`)
    ElMessage.success('已提交审核')
    row.status = 'pending_review'
  } catch (e) {
    ElMessage.success('已提交审核（演示模式）')
    row.status = 'pending_review'
  }
}

const handleSave = async () => {
  if (!form.value.question || !form.value.answer) {
    ElMessage.warning('请填写问题和答案')
    return
  }
  try {
    if (form.value.id) {
      await api.put(`/v1/knowledge/items/${form.value.id}`, form.value)
    } else {
      await api.post('/v1/knowledge/items', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchItems()
  } catch (e) {
    ElMessage.success('保存成功（演示模式）')
    dialogVisible.value = false
    fetchItems()
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(`/v1/knowledge/items/${id}`)
    ElMessage.success('删除成功')
    fetchItems()
  } catch (e) {
    items.value = items.value.filter(i => i.id !== id)
    ElMessage.success('删除成功（演示模式）')
  }
}

const demoItems = [
  { id: 1, question: '如何办理金卡会员？', answer: '金卡会员需在商场累计消费满50,000元，或单笔消费满30,000元即可办理。办理地点：一楼客服中心。', category: '会员政策', status: 'published', hitCount: 523, updateTime: '2026-06-10 10:00:00' },
  { id: 2, question: '停车场收费标准是什么？', answer: '工作日：首小时免费，之后每小时10元，每日封顶80元。周末及节假日：首小时免费，之后每小时15元，每日封顶100元。会员享8折优惠。', category: '停车服务', status: 'published', hitCount: 412, updateTime: '2026-06-10 09:00:00' },
  { id: 3, question: '618活动有哪些优惠？', answer: '618期间全场消费满1000减200，部分品牌叠加优惠。会员双倍积分。', category: '促销活动', status: 'draft', hitCount: 0, updateTime: '2026-06-11 08:00:00' },
  { id: 4, question: '积分可以兑换什么礼品？', answer: '积分可在客服中心或小程序兑换：5000积分=50元购物券，10000积分=120元购物券，20000积分=280元购物券。', category: '会员政策', status: 'published', hitCount: 356, updateTime: '2026-06-09 15:00:00' },
  { id: 5, question: 'GUCCI在几楼？', answer: 'GUCCI位于杭州大厦A座1楼，专柜号A101。营业时间为10:00-22:00。', category: '品牌信息', status: 'published', hitCount: 198, updateTime: '2026-06-08 11:00:00' },
  { id: 6, question: '退换货流程是什么？', answer: '7天内凭购物小票到客服中心办理退换货，需保持商品完好、包装完整。特殊商品按品牌规定执行。', category: '会员政策', status: 'pending_review', hitCount: 234, updateTime: '2026-06-11 07:00:00' },
]

onMounted(() => {
  fetchCategories()
  fetchItems()
})
</script>
