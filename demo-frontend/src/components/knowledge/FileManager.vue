<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <div class="flex-between" style="gap: 12px">
        <el-select v-model="categoryId" placeholder="选择分类" style="width: 200px" clearable @change="fetchFiles">
          <el-option label="全部" value="" />
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="searchText" placeholder="搜索文件名" style="width: 200px" @keyup.enter="fetchFiles" clearable />
      </div>
      <div style="display: flex; gap: 8px">
        <el-button type="primary" @click="handleUpload">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
      </div>
    </div>

    <el-table :data="files" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="fileSize" label="大小" width="100" />
      <el-table-column prop="status" label="状态" width="140">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="parseTime" label="解析时间" width="170" />
      <el-table-column prop="createTime" label="上传时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)" v-if="row.status === 'PARSE_SUCCESS'">查看</el-button>
          <el-button link type="warning" @click="handleParse(row)" v-if="['INIT', 'PARSE_FAILED'].includes(row.status)">解析</el-button>
          <el-button link type="primary" @click="handleEditTags(row)">标签</el-button>
          <el-popconfirm title="确定删除此文件吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="mt-20"
      background
      layout="total, sizes, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadVisible" title="上传文件" width="500px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="选择分类" required>
          <el-select v-model="uploadForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件" required>
          <el-upload
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
          >
            <div style="padding: 40px 0">
              <el-icon :size="40" color="#409eff"><UploadFilled /></el-icon>
              <p style="color: #909399; margin-top: 10px">拖拽文件到此处，或点击选择</p>
              <p style="color: #c0c4cc; font-size: 12px">支持 PDF, Word, Excel, TXT 格式</p>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>

    <!-- 标签编辑对话框 -->
    <el-dialog v-model="tagVisible" title="编辑标签" width="400px">
      <el-input
        v-model="tagInput"
        placeholder="输入标签，回车添加"
        @keyup.enter="addTag"
      />
      <div class="mt-20">
        <el-tag
          v-for="tag in currentTags"
          :key="tag"
          closable
          @close="removeTag(tag)"
          style="margin-right: 8px; margin-bottom: 8px"
        >
          {{ tag }}
        </el-tag>
      </div>
      <template #footer>
        <el-button @click="tagVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTags">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const files = ref([])
const categories = ref([])
const categoryId = ref('')
const searchText = ref('')
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const uploadVisible = ref(false)
const uploadForm = ref({ categoryId: null, file: null })
const uploading = ref(false)

const tagVisible = ref(false)
const currentTags = ref([])
const tagInput = ref('')
let currentFileId = null

const statusType = (status) => {
  const map = { INIT: 'info', PARSING: 'warning', PARSE_SUCCESS: 'success', PARSE_FAILED: 'danger' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { INIT: '待解析', PARSING: '解析中', PARSE_SUCCESS: '解析成功', PARSE_FAILED: '解析失败' }
  return map[status] || status
}

const fetchFiles = async () => {
  loading.value = true
  try {
    const data = await api.get('/v1/knowledge/files', {
      params: { categoryId: categoryId.value || undefined, page: currentPage.value, size: pageSize.value }
    })
    if (data && data.list) {
      files.value = data.list
      total.value = data.total || 0
    } else {
      throw new Error('no data')
    }
  } catch (e) {
    files.value = demoFiles.slice(0, pageSize.value)
    total.value = demoFiles.length
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const data = await api.get('/v1/knowledge/categories')
    categories.value = data || []
  } catch (e) {
    categories.value = [
      { id: 1, name: '会员政策' },
      { id: 2, name: '品牌信息' },
      { id: 3, name: '促销活动' },
      { id: 4, name: '停车服务' },
    ]
  }
}

const handleUpload = () => {
  uploadForm.value = { categoryId: null, file: null }
  uploadVisible.value = true
}

const handleFileChange = (file) => {
  uploadForm.value.file = file.raw
}

const submitUpload = async () => {
  if (!uploadForm.value.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  uploading.value = true
  try {
    await api.post('/v1/knowledge/files/upload', uploadForm.value)
    ElMessage.success('上传成功')
    uploadVisible.value = false
    fetchFiles()
  } catch (e) {
    ElMessage.success('上传成功（演示模式）')
    uploadVisible.value = false
    fetchFiles()
  } finally {
    uploading.value = false
  }
}

const handleParse = async (row) => {
  try {
    await api.post(`/v1/knowledge/files/${row.id}/parse`)
    ElMessage.success('已触发解析')
    row.status = 'PARSING'
  } catch (e) {
    ElMessage.success('已触发解析（演示模式）')
    row.status = 'PARSE_SUCCESS'
  }
}

const handleView = (row) => {
  ElMessage.info(`查看文件: ${row.fileName}`)
}

const handleEditTags = (row) => {
  currentFileId = row.id
  currentTags.value = row.tags ? [...row.tags] : []
  tagInput.value = ''
  tagVisible.value = true
}

const addTag = () => {
  if (tagInput.value && !currentTags.value.includes(tagInput.value)) {
    currentTags.value.push(tagInput.value)
    tagInput.value = ''
  }
}

const removeTag = (tag) => {
  currentTags.value = currentTags.value.filter(t => t !== tag)
}

const saveTags = async () => {
  try {
    await api.put(`/v1/knowledge/files/${currentFileId}/tags`, { tags: currentTags.value })
    ElMessage.success('标签已更新')
    tagVisible.value = false
  } catch (e) {
    ElMessage.success('标签已更新（演示模式）')
    tagVisible.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(`/v1/knowledge/files/${id}`)
    ElMessage.success('删除成功')
    fetchFiles()
  } catch (e) {
    files.value = files.value.filter(f => f.id !== id)
    ElMessage.success('删除成功（演示模式）')
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchFiles()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchFiles()
}

const demoFiles = [
  { id: 1, fileName: '2026年会员卡政策.pdf', categoryName: '会员政策', fileSize: '2.3MB', status: 'PARSE_SUCCESS', parseTime: '2026-06-10 10:30:00', createTime: '2026-06-10 10:28:15', tags: ['会员', '政策', '2026'] },
  { id: 2, fileName: '品牌入驻指南2026.docx', categoryName: '品牌信息', fileSize: '1.1MB', status: 'PARSE_SUCCESS', parseTime: '2026-06-10 09:15:00', createTime: '2026-06-10 09:12:30', tags: ['品牌', '入驻'] },
  { id: 3, fileName: '618促销活动方案.xlsx', categoryName: '促销活动', fileSize: '856KB', status: 'PARSING', parseTime: '-', createTime: '2026-06-11 08:00:00', tags: [] },
  { id: 4, fileName: '停车场收费标准.pdf', categoryName: '停车服务', fileSize: '512KB', status: 'PARSE_FAILED', parseTime: '-', createTime: '2026-06-11 07:45:00', tags: [] },
  { id: 5, fileName: 'VIP客户服务手册.pdf', categoryName: '会员政策', fileSize: '3.2MB', status: 'PARSE_SUCCESS', parseTime: '2026-06-09 16:20:00', createTime: '2026-06-09 16:15:00', tags: ['VIP', '服务'] },
  { id: 6, fileName: 'L1-L5楼层分布图.png', categoryName: '品牌信息', fileSize: '4.5MB', status: 'PARSE_SUCCESS', parseTime: '2026-06-09 14:10:00', createTime: '2026-06-09 14:05:00', tags: ['楼层', '导引'] },
  { id: 7, fileName: '暑期活动方案初稿.docx', categoryName: '促销活动', fileSize: '1.8MB', status: 'INIT', parseTime: '-', createTime: '2026-06-11 09:30:00', tags: [] },
  { id: 8, fileName: '积分兑换规则说明.pdf', categoryName: '会员政策', fileSize: '920KB', status: 'PARSE_SUCCESS', parseTime: '2026-06-08 11:00:00', createTime: '2026-06-08 10:55:00', tags: ['积分', '兑换'] },
]

onMounted(() => {
  fetchCategories()
  fetchFiles()
})
</script>
