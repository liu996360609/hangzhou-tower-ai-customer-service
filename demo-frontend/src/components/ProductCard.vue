<template>
  <el-card class="product-card" shadow="hover" :body-style="{ padding: '0' }">
    <div class="card-image-wrapper">
      <img :src="product.image || '/placeholder.png'" :alt="product.name" class="card-image" />
      <div class="price-badge" v-if="product.price">
        ¥{{ typeof product.price === 'number' ? product.price.toFixed(2) : product.price }}
      </div>
    </div>
    <div class="card-content">
      <h4 class="product-name">{{ product.name }}</h4>
      <p class="product-desc" v-if="product.description">{{ product.description }}</p>
      <div class="card-footer">
        <el-tag v-if="product.brand" size="small" effect="plain">{{ product.brand }}</el-tag>
        <el-button type="primary" size="small" class="action-btn" @click="handleAction">
          {{ actionText }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({
  product: {
    type: Object,
    required: true,
    default: () => ({ name: '', image: '', price: '', brand: '', description: '' })
  },
  actionText: {
    type: String,
    default: '查看商品'
  }
})

const emit = defineEmits(['action'])

const handleAction = () => emit('action', props.product)
</script>

<style scoped>
.product-card {
  max-width: 260px;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
}

.card-image-wrapper {
  height: 140px;
  overflow: hidden;
  background: #f5f7fa;
  position: relative;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.price-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(245, 108, 108, 0.92);
  color: #fff;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
}

.card-content {
  padding: 12px;
}

.product-name {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  margin: 0 0 10px;
  font-size: 12px;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.action-btn {
  flex-shrink: 0;
}
</style>
