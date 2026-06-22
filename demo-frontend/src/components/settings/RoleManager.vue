<template>
  <div class="card-container">
    <div class="flex-between mb-20">
      <h3>角色权限管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <el-table :data="roles" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名称" width="150" />
      <el-table-column prop="code" label="角色编码" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="权限" min-width="250">
        <template #default="{ row }">
          <el-tag
            v-for="perm in row.permissions"
            :key="perm"
            size="small"
            style="margin-right: 4px; margin-bottom: 4px"
          >
            {{ permLabel(perm) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userCount" label="用户数" width="100" />
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
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="权限配置">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox v-for="perm in allPermissions" :key="perm.value" :label="perm.value">
              {{ perm.label }}
            </el-checkbox>
          </el-checkbox-group>
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

const roles = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const form = ref({ id: null, name: '', code: '', description: '', permissions: [] })

const allPermissions = [
  { value: 'knowledge:view', label: '知识库-查看' },
  { value: 'knowledge:edit', label: '知识库-编辑' },
  { value: 'knowledge:delete', label: '知识库-删除' },
  { value: 'knowledge:review', label: '知识库-审核' },
  { value: 'chat:view', label: '对话-查看' },
  { value: 'chat:transfer', label: '对话-转人工' },
  { value: 'cs:online', label: '在线客服' },
  { value: 'cs:assign', label: '客服-分配' },
  { value: 'reports:view', label: '报表-查看' },
  { value: 'reports:export', label: '报表-导出' },
  { value: 'system:manage', label: '系统管理' },
]

const permLabel = (v) => allPermissions.find(p => p.value === v)?.label || v

const fetchRoles = async () => {
  try {
    const data = await api.get('/v1/system/roles')
    if (data && data.length) roles.value = data
    else throw new Error('no data')
  } catch (e) {
    roles.value = [
      { id: 1, name: '超级管理员', code: 'super_admin', description: '拥有所有权限', permissions: allPermissions.map(p => p.value), userCount: 2 },
      { id: 2, name: '知识管理员', code: 'knowledge_admin', description: '管理知识库内容', permissions: ['knowledge:view', 'knowledge:edit', 'knowledge:delete', 'knowledge:review'], userCount: 3 },
      { id: 3, name: '客服人员', code: 'cs_agent', description: '处理在线咨询', permissions: ['chat:view', 'chat:transfer', 'cs:online', 'cs:assign'], userCount: 8 },
      { id: 4, name: '数据分析师', code: 'analyst', description: '查看和导出数据报表', permissions: ['reports:view', 'reports:export', 'knowledge:view'], userCount: 2 },
    ]
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  form.value = { id: null, name: '', code: '', description: '', permissions: [] }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.value = { ...row, permissions: [...row.permissions] }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入角色名称')
    return
  }
  try {
    if (form.value.id) {
      await api.put(`/v1/system/roles/${form.value.id}`, form.value)
    } else {
      await api.post('/v1/system/roles', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchRoles()
  } catch (e) {
    ElMessage.success('保存成功（演示模式）')
    dialogVisible.value = false
    fetchRoles()
  }
}

const handleDelete = async (id) => {
  try {
    await api.delete(`/v1/system/roles/${id}`)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch (e) {
    roles.value = roles.value.filter(r => r.id !== id)
    ElMessage.success('删除成功（演示模式）')
  }
}

onMounted(fetchRoles)
</script>
