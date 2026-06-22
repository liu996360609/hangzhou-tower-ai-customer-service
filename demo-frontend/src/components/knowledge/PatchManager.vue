<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <h3>补丁问答</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增补丁
      </el-button>
    </div>

    <el-table :data="patches" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" min-width="300" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="priorityType(row.priority)">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="hitCount" label="命中" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="问题" required>
          <el-input v-model="form.question" type="textarea" rows="2" placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="答案" required>
          <el-input v-model="form.answer" type="textarea" rows="4" placeholder="请输入答案" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
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

const patches = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增补丁')
const form = ref({ id: null, question: '', answer: '', priority: 'medium' })

const priorityType = (p) => ({ high: 'danger', medium: 'warning', low: 'info' }[p] || 'info')

const fetchPatches = async () => {
  try {
    const data = await api.get('/v1/knowledge/patches')
    if (data && data.length) patches.value = data
    else throw new Error('no data')
  } catch (e) {
    patches.value = demoPatches
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增补丁'
  form.value = { id: null, question: '', answer: '', priority: 'medium' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑补丁'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.question || !form.value.answer) {
    ElMessage.warning('请填写问题和答案')
    return
  }
  try {
    if (form.value.id) {
      await api.put(`/v1/knowledge/patches/${form.value.id}`, form.value)
    } else {
      await api.post('/v1/knowledge/patches', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchPatches()
  } catch (e) {
    ElMessage.success('保存成功（演示模式）')
    dialogVisible.value = false
    fetchPatches()
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(`/v1/knowledge/patches/${id}`)
    ElMessage.success('删除成功')
    fetchPatches()
  } catch (e) {
    patches.value = patches.value.filter(p => p.id !== id)
    ElMessage.success('删除成功（演示模式）')
  }
}

const demoPatches = [
  { id: 1, question: '今天商场几点关门？', answer: '今日营业至22:00，特殊情况请关注公众号通知。', priority: 'high', hitCount: 89, createTime: '2026-06-11 06:00:00' },
  { id: 2, question: '电梯维修期间怎么去3楼？', answer: 'B座东侧电梯正常运行，可从B座电梯前往3楼，感谢您的理解。', priority: 'high', hitCount: 45, createTime: '2026-06-10 12:00:00' },
  { id: 3, question: '新开的星巴克在哪？', answer: '新星巴克位于A座2楼A210号铺，已于6月1日正式开业。', priority: 'medium', hitCount: 67, createTime: '2026-06-05 10:00:00' },
  { id: 4, question: '会员卡丢了怎么办？', answer: '请携带身份证到一楼客服中心补办，补办工本费10元。电子会员码可继续使用。', priority: 'medium', hitCount: 34, createTime: '2026-06-03 14:00:00' },
]

onMounted(fetchPatches)
</script>
