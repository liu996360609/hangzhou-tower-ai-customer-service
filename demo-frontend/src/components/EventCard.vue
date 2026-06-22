<template>
  <el-card class="event-card" shadow="hover" :body-style="{ padding: '0' }">
    <div class="card-image-wrapper">
      <img :src="event.image || '/placeholder.png'" :alt="event.title" class="card-image" />
      <div class="date-badge" v-if="event.startDate">
        <span class="month">{{ formatDateMonth(event.startDate) }}</span>
        <span class="day">{{ formatDateDay(event.startDate) }}</span>
      </div>
    </div>
    <div class="card-content">
      <h4 class="event-title">{{ event.title }}</h4>
      <p class="event-dates" v-if="event.startDate && event.endDate">
        <el-icon><Calendar /></el-icon>
        {{ event.startDate }} ~ {{ event.endDate }}
      </p>
      <p class="event-location" v-if="event.location">
        <el-icon><Location /></el-icon>
        {{ event.location }}
      </p>
      <p class="event-desc" v-if="event.description">{{ event.description }}</p>
      <el-button type="warning" size="small" class="action-btn" @click="handleAction">
        {{ actionText }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({
  event: {
    type: Object,
    required: true,
    default: () => ({ title: '', image: '', startDate: '', endDate: '', location: '', description: '' })
  },
  actionText: {
    type: String,
    default: '了解详情'
  }
})

const emit = defineEmits(['action'])

const formatDateMonth = (dateStr) => {
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC']
  const d = new Date(dateStr)
  return months[d.getMonth()] || dateStr
}

const formatDateDay = (dateStr) => {
  const d = new Date(dateStr)
  return String(d.getDate()).padStart(2, '0')
}

const handleAction = () => emit('action', props.event)
</script>

<style scoped>
.event-card {
  max-width: 280px;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
}

.card-image-wrapper {
  height: 140px;
  overflow: hidden;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  position: relative;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8;
}

.date-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 4px 10px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.date-badge .month {
  display: block;
  font-size: 10px;
  color: #f5576c;
  font-weight: 600;
  letter-spacing: 1px;
}

.date-badge .day {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.card-content {
  padding: 12px;
}

.event-title {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.event-dates,
.event-location {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}

.event-desc {
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
