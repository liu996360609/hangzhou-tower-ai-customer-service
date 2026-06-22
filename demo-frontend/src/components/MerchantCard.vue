<template>
  <el-card class="merchant-card" shadow="hover" :body-style="{ padding: '0' }">
    <div class="card-image-wrapper">
      <img :src="merchant.image || '/placeholder.png'" :alt="merchant.name" class="card-image" />
    </div>
    <div class="card-content">
      <div class="card-header">
        <h4 class="merchant-name">{{ merchant.name }}</h4>
        <el-tag v-if="merchant.category" :type="categoryTagType(merchant.category)" size="small">{{ merchant.category }}</el-tag>
      </div>
      <p class="merchant-floor" v-if="merchant.floor">
        <el-icon><Location /></el-icon>
        {{ merchant.floor }}
      </p>
      <p class="merchant-desc" v-if="merchant.description">{{ merchant.description }}</p>
      <el-button type="primary" size="small" class="action-btn" @click="handleAction">
        {{ actionText }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({
  merchant: {
    type: Object,
    required: true,
    default: () => ({ name: '', image: '', floor: '', category: '', description: '' })
  },
  actionText: {
    type: String,
    default: '查看详情'
  }
})

const emit = defineEmits(['action'])

const categoryTagType = (category) => {
  const map = { '餐饮': 'warning', '服饰': '', '美妆': 'danger', '珠宝': 'success', '数码': 'info' }
  return map[category] || ''
}

const handleAction = () => emit('action', props.merchant)
</script>

<style scoped>
.merchant-card {
  max-width: 280px;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
}

.card-image-wrapper {
  height: 140px;
  overflow: hidden;
  background: #f5f7fa;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-content {
  padding: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.merchant-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.merchant-floor {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 6px;
  font-size: 12px;
  color: #909399;
}

.merchant-desc {
  margin: 0 0 10px;
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-btn {
  width: 100%;
}
</style>
