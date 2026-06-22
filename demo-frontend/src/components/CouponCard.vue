<template>
  <el-card class="coupon-card" shadow="hover" :body-style="{ padding: '0' }">
    <div class="coupon-body">
      <div class="coupon-left">
        <div class="discount-amount">
          <span class="unit">¥</span>
          <span class="value">{{ discountValue }}</span>
        </div>
        <div class="discount-label">{{ coupon.type === 'percent' ? '折扣券' : '代金券' }}</div>
      </div>
      <div class="coupon-right">
        <h4 class="coupon-name">{{ coupon.name }}</h4>
        <p class="coupon-condition" v-if="coupon.condition">
          满{{ formatMoney(coupon.condition) }}可用
        </p>
        <p class="coupon-expiry" v-if="coupon.expiryDate">
          <el-icon><Clock /></el-icon>
          有效期至 {{ coupon.expiryDate }}
        </p>
        <el-button :type="couponActionType" size="small" class="action-btn" @click="handleAction">
          {{ actionText }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  coupon: {
    type: Object,
    required: true,
    default: () => ({ name: '', value: '', type: 'cash', condition: '', expiryDate: '' })
  },
  actionText: {
    type: String,
    default: '立即领取'
  }
})

const emit = defineEmits(['action'])

const discountValue = computed(() => {
  if (props.coupon.type === 'percent') {
    return props.coupon.value
  }
  return typeof props.coupon.value === 'number' ? props.coupon.value : props.coupon.value
})

const formatMoney = (val) => {
  return typeof val === 'number' ? val.toFixed(0) : val
}

const couponActionType = computed(() => {
  return 'danger'
})

const handleAction = () => emit('action', props.coupon)
</script>

<style scoped>
.coupon-card {
  max-width: 340px;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
  border: 1px solid #fce4ec;
}

.coupon-body {
  display: flex;
  min-height: 100px;
}

.coupon-left {
  flex: 0 0 110px;
  background: linear-gradient(135deg, #f56c6c 0%, #e64343 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
}

.coupon-left::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background: #f0f2f5;
  border-radius: 50%;
}

.discount-amount {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.discount-amount .unit {
  font-size: 16px;
  font-weight: 600;
}

.discount-amount .value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.discount-label {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.85;
}

.coupon-right {
  flex: 1;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.coupon-name {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.coupon-condition {
  margin: 0 0 4px;
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.coupon-expiry {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 8px;
  font-size: 11px;
  color: #909399;
}

.action-btn {
  align-self: flex-end;
}
</style>
