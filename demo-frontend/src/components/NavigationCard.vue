<template>
  <el-card class="navigation-card" shadow="hover" :body-style="{ padding: '14px' }">
    <div class="nav-content">
      <div class="nav-icon-wrapper">
        <el-icon :size="28" color="#409eff"><LocationFilled /></el-icon>
      </div>
      <div class="nav-info">
        <h4 class="nav-name">{{ navigation.name }}</h4>
        <p class="nav-location" v-if="navigation.location">
          <el-icon><Position /></el-icon>
          {{ navigation.location }}
        </p>
        <p class="nav-floor" v-if="navigation.floor">
          楼层：{{ navigation.floor }}
        </p>
        <p class="nav-desc" v-if="navigation.description">{{ navigation.description }}</p>
      </div>
    </div>
    <el-button type="primary" plain size="small" class="nav-action" @click="handleAction">
      <el-icon><Guide /></el-icon>
      {{ actionText }}
    </el-button>
  </el-card>
</template>

<script setup>
const props = defineProps({
  navigation: {
    type: Object,
    required: true,
    default: () => ({ name: '', location: '', floor: '', description: '' })
  },
  actionText: {
    type: String,
    default: '查看导航'
  }
})

const emit = defineEmits(['action'])

const handleAction = () => emit('action', props.navigation)
</script>

<style scoped>
.navigation-card {
  max-width: 360px;
  border-radius: 8px;
  margin: 8px 0;
  border-left: 4px solid #409eff;
}

.nav-content {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.nav-icon-wrapper {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-info {
  flex: 1;
  min-width: 0;
}

.nav-name {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.nav-location,
.nav-floor {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}

.nav-desc {
  margin: 0 0 4px;
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.nav-action {
  margin-top: 10px;
  width: 100%;
}
</style>
