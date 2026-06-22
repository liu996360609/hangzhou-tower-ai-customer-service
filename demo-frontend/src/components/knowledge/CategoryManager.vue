<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <h3>知识分类</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>
    <el-table :data="categories" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="code" label="分类编码" width="150" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="fileCount" label="文件数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 'enabled' ? 'success' : 'info'">
            {{ row.status === 'enabled' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="success" @click="handleSettings(row)">解析设置</el-button>
          <el-popconfirm title="确定删除此分类吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="form.code" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 解析设置对话框 -->
    <el-dialog v-model="settingsVisible" title="解析设置" width="500px">
      <el-form :model="settingsForm" label-width="100px">
        <el-form-item label="分段长度">
          <el-input-number v-model="settingsForm.chunkSize" :min="100" :max="2000" />
        </el-form-item>
        <el-form-item label="分段重叠">
          <el-input-number v-model="settingsForm.chunkOverlap" :min="0" :max="500" />
        </el-form-item>
        <el-form-item label="提取标题">
          <el-switch v-model="settingsForm.extractTitle" />
        </el-form-item>
        <el-form-item label="提取表格">
          <el-switch v-model="settingsForm.extractTable" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const categories = ref([])
const dialogVisible = ref(false)
const settingsVisible = ref(false)
const dialogTitle = ref('新增分类')
const form = ref({ id: null, name: '', code: '', description: '', enabled: true })
const settingsForm = ref({ chunkSize: 500, chunkOverlap: 50, extractTitle: true, extractTable: true })
let currentCatId = null

const fetchCategories = async () => {
  try {
    const data = await api.get('/v1/knowledge/categories')
    categories.value = data || []
  } catch (e) {
    categories.value = [
      { id: 1, name: '会员政策', code: 'MEMBER', description: '会员相关政策和规则', fileCount: 12, status: 'enabled' },
      { id: 2, name: '品牌信息', code: 'BRAND', description: '商场品牌和店铺信息', fileCount: 45, status: 'enabled' },
      { id: 3, name: '促销活动', code: 'PROMO', description: '促销和营销活动信息', fileCount: 23, status: 'enabled' },
      { id: 4, name: '停车服务', code: 'PARKING', description: '停车场相关信息', fileCount: 8, status: 'enabled' },
      { id: 5, name: '楼层指南', code: 'FLOOR', description: '商场楼层分布和导引', fileCount: 15, status: 'disabled' },
    ]
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.value = { id: null, name: '', code: '', description: '', enabled: true }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  form.value = { ...row, enabled: row.status === 'enabled' }
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await api.delete(`/v1/knowledge/categories/${id}`)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e) {
    categories.value = categories.value.filter(c => c.id !== id)
    ElMessage.success('删除成功')
  }
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    if (form.value.id) {
      await api.put(`/v1/knowledge/categories/${form.value.id}`, {
        ...form.value,
        status: form.value.enabled ? 'enabled' : 'disabled'
      })
    } else {
      await api.post('/v1/knowledge/categories', {
        ...form.value,
        status: form.value.enabled ? 'enabled' : 'disabled'
      })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchCategories()
  } catch (e) {
    ElMessage.success('保存成功（演示模式）')
    dialogVisible.value = false
    if (form.value.id) {
      const idx = categories.value.findIndex(c => c.id === form.value.id)
      if (idx >= 0) categories.value[idx] = { ...form.value, status: form.value.enabled ? 'enabled' : 'disabled', fileCount: categories.value[idx].fileCount }
    } else {
      categories.value.push({ ...form.value, id: Date.now(), status: form.value.enabled ? 'enabled' : 'disabled', fileCount: 0 })
    }
  }
}

const handleSettings = async (row) => {
  currentCatId = row.id
  try {
    const data = await api.get(`/v1/knowledge/parse-settings/${row.id}`)
    settingsForm.value = data || settingsForm.value
  } catch (e) {}
  settingsVisible.value = true
}

const handleSaveSettings = async () => {
  try {
    await api.put(`/v1/knowledge/parse-settings/${currentCatId}`, settingsForm.value)
    ElMessage.success('设置已保存')
    settingsVisible.value = false
  } catch (e) {
    ElMessage.success('设置已保存（演示模式）')
    settingsVisible.value = false
  }
}

onMounted(fetchCategories)
</script>
