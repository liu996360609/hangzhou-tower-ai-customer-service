<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <div style="display: flex; gap: 12px">
        <el-select v-model="filterType" placeholder="过滤类型" style="width: 150px" clearable>
          <el-option label="全部" value="" />
          <el-option label="替换" value="replace" />
          <el-option label="拦截" value="block" />
          <el-option label="警告" value="warn" />
        </el-select>
      </div>
      <div style="display: flex; gap: 8px">
        <el-input v-model="newWord" placeholder="输入敏感词" style="width: 200px" @keyup.enter="handleAdd" />
        <el-select v-model="newType" style="width: 100px">
          <el-option label="替换" value="replace" />
          <el-option label="拦截" value="block" />
          <el-option label="警告" value="warn" />
        </el-select>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加
        </el-button>
      </div>
    </div>

    <el-table :data="filteredWords" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" width="150" />
      <el-table-column prop="type" label="处理方式" width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.type)">{{ typeText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="replaceWord" label="替换词" width="120">
        <template #default="{ row }">
          <span v-if="row.type === 'replace'">{{ row.replaceWord || '***' }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="hitCount" label="命中次数" width="100" />
      <el-table-column prop="creator" label="创建人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const words = ref([])
const filterType = ref('')
const newWord = ref('')
const newType = ref('replace')

const typeTag = (t) => ({ replace: 'warning', block: 'danger', warn: 'info' }[t] || 'info')
const typeText = (t) => ({ replace: '替换', block: '拦截', warn: '警告' }[t] || t)

const filteredWords = computed(() => {
  if (!filterType.value) return words.value
  return words.value.filter(w => w.type === filterType.value)
})

const fetchWords = async () => {
  try {
    const data = await api.get('/v1/system/sensitive-words')
    if (data && data.length) words.value = data
    else throw new Error('no data')
  } catch (e) {
    words.value = demoWords
  }
}

const handleAdd = async () => {
  if (!newWord.value.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  try {
    await api.post('/v1/system/sensitive-words', { word: newWord.value, type: newType.value })
    ElMessage.success('添加成功')
    newWord.value = ''
    fetchWords()
  } catch (e) {
    ElMessage.success('添加成功（演示模式）')
    words.value.push({
      id: Date.now(),
      word: newWord.value,
      type: newType.value,
      replaceWord: '***',
      hitCount: 0,
      creator: '管理员',
      createTime: new Date().toLocaleString('zh-CN')
    })
    newWord.value = ''
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(`/v1/system/sensitive-words/${id}`)
    ElMessage.success('删除成功')
    fetchWords()
  } catch (e) {
    words.value = words.value.filter(w => w.id !== id)
    ElMessage.success('删除成功（演示模式）')
  }
}

const demoWords = [
  { id: 1, word: '垃圾', type: 'replace', replaceWord: '**', hitCount: 156, creator: '系统', createTime: '2026-01-15 10:00:00' },
  { id: 2, word: '投诉', type: 'warn', replaceWord: null, hitCount: 234, creator: '管理员', createTime: '2026-02-20 14:30:00' },
  { id: 3, word: '骗子', type: 'block', replaceWord: null, hitCount: 45, creator: '管理员', createTime: '2026-03-10 09:00:00' },
  { id: 4, word: '假货', type: 'warn', replaceWord: null, hitCount: 89, creator: '系统', createTime: '2026-04-05 16:00:00' },
  { id: 5, word: '退钱', type: 'warn', replaceWord: null, hitCount: 67, creator: '管理员', createTime: '2026-05-12 11:20:00' },
  { id: 6, word: '差劲', type: 'replace', replaceWord: '**', hitCount: 34, creator: '系统', createTime: '2026-05-20 08:00:00' },
]

onMounted(fetchWords)
</script>

<style scoped>
.text-muted {
  color: #c0c4cc;
}
</style>
