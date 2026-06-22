<template>
  <div>
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="分类管理" name="categories">
        <CategoryManager />
      </el-tab-pane>
      <el-tab-pane label="文件管理" name="files">
        <FileManager />
      </el-tab-pane>
      <el-tab-pane label="知识列表" name="items">
        <KnowledgeList />
      </el-tab-pane>
      <el-tab-pane label="审核管理" name="reviews">
        <ReviewManager />
      </el-tab-pane>
      <el-tab-pane label="检索测试" name="retrieve">
        <RetrieveTest />
      </el-tab-pane>
      <el-tab-pane label="补丁问答" name="patches">
        <PatchManager />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CategoryManager from '@/components/knowledge/CategoryManager.vue'
import FileManager from '@/components/knowledge/FileManager.vue'
import KnowledgeList from '@/components/knowledge/KnowledgeList.vue'
import ReviewManager from '@/components/knowledge/ReviewManager.vue'
import RetrieveTest from '@/components/knowledge/RetrieveTest.vue'
import PatchManager from '@/components/knowledge/PatchManager.vue'

const activeTab = ref('categories')
</script>
