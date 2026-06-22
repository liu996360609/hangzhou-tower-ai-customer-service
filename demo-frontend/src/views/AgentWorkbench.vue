<template>
  <div class="agent-workbench">
    <!-- Agent Login Simulation -->
    <div v-if="!isLoggedIn" class="login-card">
      <el-card shadow="never" class="login-box">
        <div class="login-header">
          <el-icon :size="48" color="#409eff"><Headset /></el-icon>
          <h2>专属客服工作台</h2>
          <p class="login-subtitle">请选择您的客服账号登录</p>
        </div>
        <el-select v-model="selectedAgent" placeholder="选择客服账号" size="large" style="width: 100%; margin-bottom: 16px">
          <el-option v-for="agent in agentList" :key="agent.id" :label="agent.name" :value="agent">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-avatar :size="28" :style="{ background: agent.color }">{{ agent.name[0] }}</el-avatar>
              <span>{{ agent.name }}</span>
              <el-tag :type="agent.status === 'online' ? 'success' : 'info'" size="small">
                {{ agent.status === 'online' ? '空闲' : '忙碌' }}
              </el-tag>
            </div>
          </el-option>
        </el-select>
        <el-button type="primary" size="large" style="width: 100%" @click="handleLogin" :disabled="!selectedAgent">
          登录工作台
        </el-button>
      </el-card>
    </div>

    <!-- Main Workbench -->
    <div v-else class="workbench-layout">
      <!-- Top Bar -->
      <div class="workbench-header">
        <div class="header-left">
          <h3 class="workbench-title">
            <el-icon :size="22" color="#409eff"><Headset /></el-icon>
            专属客服工作台
          </h3>
        </div>
        <div class="header-right">
          <!-- Notification Bell -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-bell">
            <el-button :icon="Bell" circle @click="showNotifications" />
          </el-badge>
          <!-- Agent Info -->
          <div class="agent-badge" v-if="currentAgent">
            <el-avatar :size="32" :style="{ background: currentAgent.color }">
              {{ currentAgent.name[0] }}
            </el-avatar>
            <span class="agent-name-label">{{ currentAgent.name }}</span>
          </div>
          <el-button size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </div>

      <!-- Main Content -->
      <div class="workbench-body">
        <div class="workbench-grid">
          <!-- Task List Panel -->
          <el-card class="task-panel" shadow="never">
            <template #header>
              <div class="panel-header">
                <span><el-icon><List /></el-icon> VIP 服务队列</span>
                <el-badge :value="tasks.length" type="warning" />
              </div>
            </template>

            <div v-if="tasks.length === 0" class="empty-tasks">
              <el-empty description="暂无等待服务的VIP用户" :image-size="80" />
            </div>

            <div v-for="task in tasks" :key="task.id" class="task-card-wrapper">
              <el-card shadow="hover" :class="['task-card', { active: currentTask?.id === task.id }]">
                <div class="task-user-info">
                  <el-avatar :size="40" :style="{ background: task.userCardLevel === '钻石卡' ? '#8B5CF6' : '#F59E0B' }">
                    {{ task.userName[0] }}
                  </el-avatar>
                  <div class="user-details">
                    <div class="user-name-row">
                      <span class="user-name">{{ task.userName }}</span>
                      <el-tag :type="task.userCardLevel === '钻石卡' ? '' : 'warning'" size="small" effect="dark">
                        {{ task.userCardLevel }}
                      </el-tag>
                    </div>
                    <div class="user-phone">{{ task.userPhone }}</div>
                  </div>
                </div>
                <div class="task-question">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>{{ task.question }}</span>
                </div>
                <div class="task-time">
                  <el-icon><Clock /></el-icon>
                  {{ task.waitTime }}
                </div>
                <el-button
                  v-if="task.status === 'waiting'"
                  type="primary"
                  size="small"
                  class="task-action-btn"
                  @click="handleAcceptTask(task)"
                  :loading="task.accepting"
                >
                  <el-icon><CircleCheckFilled /></el-icon>
                  接入服务
                </el-button>
                <el-button
                  v-else-if="task.status === 'active'"
                  type="success"
                  size="small"
                  class="task-action-btn"
                  @click="openChatView(task)"
                >
                  <el-icon><ChatDotRound /></el-icon>
                  继续对话
                </el-button>
              </el-card>
            </div>
          </el-card>

          <!-- Chat View Panel -->
          <el-card v-if="currentTask" class="chat-panel" shadow="never">
            <template #header>
              <div class="chat-panel-header">
                <div class="chat-panel-user">
                  <el-avatar :size="32" :style="{ background: currentTask.userCardLevel === '钻石卡' ? '#8B5CF6' : '#F59E0B' }">
                    {{ currentTask.userName[0] }}
                  </el-avatar>
                  <div>
                    <div class="chat-panel-name">
                      {{ currentTask.userName }}
                      <el-tag :type="currentTask.userCardLevel === '钻石卡' ? '' : 'warning'" size="small" effect="dark">
                        {{ currentTask.userCardLevel }}
                      </el-tag>
                    </div>
                    <div class="chat-panel-phone">{{ currentTask.userPhone }}</div>
                  </div>
                </div>
                <el-button size="small" type="danger" plain @click="handleEndChat">
                  <el-icon><Close /></el-icon>
                  结束服务
                </el-button>
              </div>
            </template>

            <!-- Chat Messages -->
            <div class="chat-messages-area" ref="agentChatContainer">
              <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['agent-msg-bubble', msg.sender]">
                <template v-if="msg.sender === 'agent'">
                  <div class="msg-content">
                    <div class="msg-text">{{ msg.text }}</div>
                    <div v-if="msg.cards && msg.cards.length" class="msg-cards">
                      <component
                        v-for="(card, cIdx) in msg.cards"
                        :key="cIdx"
                        :is="getCardComponent(card.type)"
                        :merchant="card.type === 'merchantCard' ? card.data : undefined"
                        :product="card.type === 'productCard' ? card.data : undefined"
                        :event="card.type === 'eventCard' ? card.data : undefined"
                        :coupon="card.type === 'couponCard' ? card.data : undefined"
                        :navigation="card.type === 'navigationCard' ? card.data : undefined"
                        :action-text="getCardActionText(card.type)"
                      />
                    </div>
                    <div class="msg-time">{{ msg.time }}</div>
                  </div>
                  <el-avatar :size="32" :style="{ background: currentAgent.color }">
                    {{ currentAgent.name[0] }}
                  </el-avatar>
                </template>
                <template v-else>
                  <el-avatar :size="32" :style="{ background: currentTask.userCardLevel === '钻石卡' ? '#8B5CF6' : '#F59E0B' }">
                    {{ currentTask.userName[0] }}
                  </el-avatar>
                  <div class="msg-content">
                    <div class="msg-text">{{ msg.text }}</div>
                    <div class="msg-time">{{ msg.time }}</div>
                  </div>
                </template>
              </div>
            </div>

            <!-- Reply Area -->
            <div class="reply-area">
              <!-- Multimodal Content Picker -->
              <div class="content-picker">
                <el-tabs v-model="activePickerTab" type="border-card" class="picker-tabs">
                  <el-tab-pane label="商家" name="merchant">
                    <el-input v-model="merchantSearch" placeholder="搜索商家..." size="small" clearable style="margin-bottom: 8px" />
                    <div class="picker-list">
                      <div v-for="item in filteredMerchants" :key="item.id" class="picker-item" @click="addCardToReply('merchantCard', item)">
                        <el-avatar :size="32" :src="item.image" shape="square">
                          <el-icon><Shop /></el-icon>
                        </el-avatar>
                        <div class="picker-item-info">
                          <div class="picker-item-name">{{ item.name }}</div>
                          <div class="picker-item-sub">{{ item.floor }} · {{ item.category }}</div>
                        </div>
                        <el-button size="small" type="primary" plain>添加</el-button>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="商品" name="product">
                    <el-input v-model="productSearch" placeholder="搜索商品..." size="small" clearable style="margin-bottom: 8px" />
                    <div class="picker-list">
                      <div v-for="item in filteredProducts" :key="item.id" class="picker-item" @click="addCardToReply('productCard', item)">
                        <el-avatar :size="32" :src="item.image" shape="square">
                          <el-icon><ShoppingBag /></el-icon>
                        </el-avatar>
                        <div class="picker-item-info">
                          <div class="picker-item-name">{{ item.name }}</div>
                          <div class="picker-item-sub">¥{{ item.price }} · {{ item.brand }}</div>
                        </div>
                        <el-button size="small" type="primary" plain>添加</el-button>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="活动" name="event">
                    <el-input v-model="eventSearch" placeholder="搜索活动..." size="small" clearable style="margin-bottom: 8px" />
                    <div class="picker-list">
                      <div v-for="item in filteredEvents" :key="item.id" class="picker-item" @click="addCardToReply('eventCard', item)">
                        <el-avatar :size="32" :src="item.image" shape="square">
                          <el-icon><Calendar /></el-icon>
                        </el-avatar>
                        <div class="picker-item-info">
                          <div class="picker-item-name">{{ item.title }}</div>
                          <div class="picker-item-sub">{{ item.location }}</div>
                        </div>
                        <el-button size="small" type="primary" plain>添加</el-button>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="优惠券" name="coupon">
                    <div class="picker-list">
                      <div v-for="item in filteredCoupons" :key="item.id" class="picker-item" @click="addCardToReply('couponCard', item)">
                        <el-avatar :size="32" shape="square" style="background: linear-gradient(135deg, #f56c6c, #e64343)">
                          <el-icon><Ticket /></el-icon>
                        </el-avatar>
                        <div class="picker-item-info">
                          <div class="picker-item-name">{{ item.name }}</div>
                          <div class="picker-item-sub">
                            {{ item.type === 'percent' ? item.value + '折' : '¥' + item.value }}
                            <span v-if="item.condition">满{{ item.condition }}可用</span>
                          </div>
                        </div>
                        <el-button size="small" type="danger" plain>添加</el-button>
                      </div>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="导航" name="navigation">
                    <div class="picker-list">
                      <div v-for="item in filteredNavigations" :key="item.id" class="picker-item" @click="addCardToReply('navigationCard', item)">
                        <el-avatar :size="32" shape="square" style="background: #ecf5ff">
                          <el-icon><LocationFilled /></el-icon>
                        </el-avatar>
                        <div class="picker-item-info">
                          <div class="picker-item-name">{{ item.name }}</div>
                          <div class="picker-item-sub">{{ item.location }} · {{ item.floor }}</div>
                        </div>
                        <el-button size="small" type="primary" plain>添加</el-button>
                      </div>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </div>

              <!-- Selected Cards Preview -->
              <div v-if="replyCards.length > 0" class="reply-cards-preview">
                <div class="preview-label">已添加内容：</div>
                <div class="preview-cards">
                  <el-tag
                    v-for="(card, idx) in replyCards"
                    :key="idx"
                    closable
                    @close="removeCardFromReply(idx)"
                    size="small"
                    effect="plain"
                  >
                    {{ getCardLabel(card.type) }}: {{ getCardItemName(card) }}
                  </el-tag>
                </div>
              </div>

              <!-- Text Input and Send -->
              <div class="reply-input-row">
                <el-input
                  v-model="replyText"
                  type="textarea"
                  :autosize="{ minRows: 1, maxRows: 3 }"
                  placeholder="输入回复内容..."
                  resize="none"
                  class="reply-text-input"
                />
                <el-button type="primary" @click="handleSendReply" :disabled="!replyText.trim() && replyCards.length === 0">
                  <el-icon><Promotion /></el-icon>
                  发送回复
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- No Active Chat Placeholder -->
          <el-card v-else class="chat-panel-placeholder" shadow="never">
            <el-empty description="选择一个VIP用户开始服务" :image-size="120">
              <template #description>
                <p>请在左侧队列中选择一位VIP用户</p>
                <p class="placeholder-sub">接入后将进入专属对话模式</p>
              </template>
            </el-empty>
          </el-card>
        </div>
      </div>
    </div>

    <!-- Notification Drawer -->
    <el-drawer v-model="notifDrawerVisible" title="通知中心" size="360px">
      <div v-if="notifications.length === 0">
        <el-empty description="暂无通知" />
      </div>
      <div v-for="notif in notifications" :key="notif.id" class="notif-item" :class="{ unread: !notif.read }">
        <div class="notif-content">
          <el-badge is-dot :hidden="notif.read" class="notif-dot">
            <div class="notif-title">{{ notif.title }}</div>
          </el-badge>
          <div class="notif-desc">{{ notif.description }}</div>
          <div class="notif-time">{{ notif.time }}</div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, List, Close, Shop, ShoppingBag, Calendar, Ticket, LocationFilled, Promotion, CircleCheckFilled, ChatDotRound, ChatLineRound, Clock } from '@element-plus/icons-vue'
import { getAgentTasks, acceptTask, respondToVIP, getAgentNotifications, markNotificationRead } from '@/api'
import MerchantCard from '@/components/MerchantCard.vue'
import ProductCard from '@/components/ProductCard.vue'
import EventCard from '@/components/EventCard.vue'
import CouponCard from '@/components/CouponCard.vue'
import NavigationCard from '@/components/NavigationCard.vue'

// Login State
const isLoggedIn = ref(false)
const currentAgent = ref(null)
const selectedAgent = ref(null)

const agentList = [
  { id: 'agent-001', name: '张三', color: '#409eff', status: 'online' },
  { id: 'agent-002', name: '李四', color: '#67c23a', status: 'busy' },
  { id: 'agent-003', name: '王五', color: '#e6a23c', status: 'online' },
]

// Task List
const tasks = ref([])

// Current Active Task / Chat
const currentTask = ref(null)
const chatMessages = ref([])
const agentChatContainer = ref(null)

// Reply State
const replyText = ref('')
const replyCards = ref([])

// Content Picker
const activePickerTab = ref('merchant')
const merchantSearch = ref('')
const productSearch = ref('')
const eventSearch = ref('')

// Notifications
const unreadCount = ref(2)
const notifDrawerVisible = ref(false)
const notifications = ref([
  { id: 1, title: '新VIP服务请求', description: '钻石卡会员 王小明 等待接入', time: '14:32', read: false },
  { id: 2, title: '新VIP服务请求', description: '金卡会员 李小红 等待接入', time: '14:28', read: false },
])

// Mock Data for Content Picker
const pickerMerchants = [
  { id: 1, name: 'Dior美妆', image: 'https://via.placeholder.com/32/8B5CF6/fff?text=D', floor: 'A座1F', category: '美妆', description: '国际知名奢侈美妆品牌' },
  { id: 2, name: '绿茶餐厅', image: 'https://via.placeholder.com/32/10B981/fff?text=G', floor: 'C座4F', category: '餐饮', description: '杭帮菜经典' },
  { id: 3, name: '周大福', image: 'https://via.placeholder.com/32/F59E0B/fff?text=C', floor: 'A座2F', category: '珠宝', description: '知名珠宝品牌' },
  { id: 4, name: 'Apple Store', image: 'https://via.placeholder.com/32/303133/fff?text=A', floor: 'B座1F', category: '数码', description: '苹果官方授权店' },
  { id: 5, name: 'ZARA', image: 'https://via.placeholder.com/32/606266/fff?text=Z', floor: 'A座2F', category: '服饰', description: '时尚快消品牌' },
]

const pickerProducts = [
  { id: 1, name: 'Dior烈艳蓝金唇膏999', image: 'https://via.placeholder.com/32/EC4899/fff?text=9', price: 370, brand: 'Dior', description: '经典正红色' },
  { id: 2, name: '周大福传承系列黄金手镯', image: 'https://via.placeholder.com/32/F59E0B/fff?text=金', price: 12800, brand: '周大福', description: '古法黄金工艺' },
  { id: 3, name: 'iPhone 16 Pro Max', image: 'https://via.placeholder.com/32/303133/fff?text=iP', price: 9999, brand: 'Apple', description: '最新旗舰机型' },
]

const pickerEvents = [
  { id: 1, title: '杭州大厦春季美妆节', image: 'https://via.placeholder.com/32/F43F5E/fff?text=B', startDate: '2026-03-15', endDate: '2026-04-15', location: 'A座1F中庭', description: '大牌美妆满1000减200' },
  { id: 2, title: '珠宝巡展-璀璨之星', image: 'https://via.placeholder.com/32/8B5CF6/fff?text=J', startDate: '2026-04-01', endDate: '2026-04-30', location: 'A座2F', description: '国际珠宝品牌联合展出' },
]

const pickerCoupons = [
  { id: 1, name: '美妆品类满减券', value: 200, type: 'cash', condition: 1000, expiryDate: '2026-04-30' },
  { id: 2, name: '全场通用8.8折券', value: '8.8', type: 'percent', condition: 500, expiryDate: '2026-12-31' },
  { id: 3, name: '新会员专享券', value: 100, type: 'cash', condition: 300, expiryDate: '2026-06-30' },
]

const pickerNavigations = [
  { id: 1, name: '客服中心', location: 'A座1F东侧', floor: '1F', description: '会员办理、积分兑换' },
  { id: 2, name: '停车场入口', location: 'B座B1层', floor: 'B1', description: '会员停车8折优惠' },
  { id: 3, name: '会员中心', location: 'A座3F', floor: '3F', description: 'VIP专属休息区' },
]

// Filtered data
const filteredMerchants = computed(() => {
  if (!merchantSearch.value) return pickerMerchants
  return pickerMerchants.filter(m => m.name.includes(merchantSearch.value) || m.category.includes(merchantSearch.value))
})

const filteredProducts = computed(() => {
  if (!productSearch.value) return pickerProducts
  return pickerProducts.filter(p => p.name.includes(productSearch.value) || p.brand.includes(productSearch.value))
})

const filteredEvents = computed(() => {
  if (!eventSearch.value) return pickerEvents
  return pickerEvents.filter(e => e.title.includes(eventSearch.value))
})

const filteredCoupons = computed(() => pickerCoupons)
const filteredNavigations = computed(() => pickerNavigations)

// Mock tasks
const mockTasks = [
  {
    id: 'task-001',
    userName: '王小明',
    userCardLevel: '钻石卡',
    userPhone: '138****1234',
    question: '请问Dior新款口红有哪些？能帮我推荐一下吗？',
    status: 'waiting',
    waitTime: '等待 3分钟',
    accepting: false
  },
  {
    id: 'task-002',
    userName: '李小红',
    userCardLevel: '金卡',
    userPhone: '139****5678',
    question: '我想了解一下最近的促销活动，有什么优惠吗？',
    status: 'waiting',
    waitTime: '等待 8分钟',
    accepting: false
  },
]

// Card helpers
const getCardComponent = (type) => {
  const map = {
    merchantCard: 'MerchantCard',
    productCard: 'ProductCard',
    eventCard: 'EventCard',
    couponCard: 'CouponCard',
    navigationCard: 'NavigationCard',
  }
  return map[type] || 'MerchantCard'
}

const getCardActionText = (type) => {
  const map = {
    merchantCard: '查看详情',
    productCard: '查看商品',
    eventCard: '了解详情',
    couponCard: '立即领取',
    navigationCard: '查看导航',
  }
  return map[type] || '查看详情'
}

const getCardLabel = (type) => {
  const map = {
    merchantCard: '商家',
    productCard: '商品',
    eventCard: '活动',
    couponCard: '优惠券',
    navigationCard: '导航',
  }
  return map[type] || '卡片'
}

const getCardItemName = (card) => {
  const d = card.data
  return d.name || d.title || ''
}

// Add card to reply
const addCardToReply = (type, data) => {
  replyCards.value.push({ type, data })
  ElMessage.success(`已添加${getCardLabel(type)}：${getCardItemName({ data })}`)
}

const removeCardFromReply = (idx) => {
  replyCards.value.splice(idx, 1)
}

// Login
const handleLogin = () => {
  if (!selectedAgent.value) return
  currentAgent.value = selectedAgent.value
  isLoggedIn.value = true
  tasks.value = [...mockTasks]
  ElMessage.success(`欢迎回来，${currentAgent.value.name}！`)
}

const handleLogout = () => {
  isLoggedIn.value = false
  currentAgent.value = null
  currentTask.value = null
  chatMessages.value = []
  replyCards.value = []
  replyText.value = ''
}

// Accept task
const handleAcceptTask = async (task) => {
  task.accepting = true
  try {
    await acceptTask(currentAgent.value.id, task.id)
  } catch (e) {
    // Fallback: local update
  }
  task.status = 'active'
  task.accepting = false
  task.waitTime = '服务中'

  // Initialize chat with the user's question
  currentTask.value = task
  chatMessages.value = [
    { sender: 'user', text: task.question, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }
  ]
  ElMessage.success(`已接入 ${task.userName} 的服务`)
  scrollToAgentBottom()
}

const openChatView = (task) => {
  currentTask.value = task
  scrollToAgentBottom()
}

const handleEndChat = () => {
  if (!currentTask.value) return
  const taskIdx = tasks.value.findIndex(t => t.id === currentTask.value.id)
  if (taskIdx !== -1) {
    tasks.value.splice(taskIdx, 1)
  }
  currentTask.value = null
  chatMessages.value = []
  replyCards.value = []
  replyText.value = []
  replyText.value = ''
  ElMessage.success('服务已结束')
}

// Send reply
const handleSendReply = async () => {
  if ((!replyText.value.trim() && replyCards.value.length === 0) || !currentTask.value) return

  const now = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  const msg = {
    sender: 'agent',
    text: replyText.value,
    cards: replyCards.value.length > 0 ? [...replyCards.value] : null,
    time: now
  }
  chatMessages.value.push(msg)

  // Try to send to API
  try {
    await respondToVIP(currentAgent.value.id, currentTask.value.id, {
      text: replyText.value,
      cards: replyCards.value
    })
  } catch (e) {
    // Fallback: local only
  }

  // Simulate user reply after some time
  setTimeout(() => {
    const userReplies = [
      '谢谢您的推荐，我一会儿去看看！',
      '这个活动听起来不错，请问具体在哪里？',
      '好的，我了解了，还有其他推荐吗？',
      '太感谢了，这个优惠券怎么领取？'
    ]
    chatMessages.value.push({
      sender: 'user',
      text: userReplies[Math.floor(Math.random() * userReplies.length)],
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
    scrollToAgentBottom()
  }, 3000)

  // Clear reply
  replyText.value = ''
  replyCards.value = []
  scrollToAgentBottom()
}

const scrollToAgentBottom = () => {
  nextTick(() => {
    if (agentChatContainer.value) {
      agentChatContainer.value.scrollTop = agentChatContainer.value.scrollHeight
    }
  })
}

// Notifications
const showNotifications = () => {
  notifDrawerVisible.value = true
  // Mark all as read
  notifications.value.forEach(n => n.read = true)
  unreadCount.value = 0
}

onMounted(() => {
  // Could fetch tasks from API here
})
</script>

<style scoped>
.agent-workbench {
  height: 100%;
  min-height: calc(100vh - 120px);
}

/* Login */
.login-card {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 200px);
}

.login-box {
  width: 420px;
  border-radius: 12px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h2 {
  margin: 12px 0 4px;
  font-size: 22px;
  color: #303133;
}

.login-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* Workbench Layout */
.workbench-layout {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}

.workbench-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.workbench-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-bell {
  cursor: pointer;
}

.agent-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 20px;
}

.agent-name-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.workbench-body {
  flex: 1;
  padding: 16px;
  background: #f0f2f5;
  overflow: hidden;
}

.workbench-grid {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 16px;
  height: 100%;
}

/* Task Panel */
.task-panel {
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.task-panel :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.task-panel :deep(.el-card__body) {
  padding: 12px;
  overflow-y: auto;
  flex: 1;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 14px;
}

.panel-header .el-icon {
  margin-right: 6px;
}

.empty-tasks {
  padding: 40px 0;
  text-align: center;
}

.task-card-wrapper {
  margin-bottom: 10px;
}

.task-card {
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.task-card:hover {
  border-color: #409eff;
}

.task-card.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
}

.task-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.user-phone {
  font-size: 12px;
  color: #909399;
}

.task-question {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 10px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.4;
}

.task-question .el-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.task-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.task-action-btn {
  width: 100%;
}

/* Chat Panel */
.chat-panel {
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-panel :deep(.el-card__header) {
  padding: 10px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.chat-panel :deep(.el-card__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.chat-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-panel-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-panel-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.chat-panel-phone {
  font-size: 12px;
  color: #909399;
}

.chat-messages-area {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f7f8fa;
}

.agent-msg-bubble {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.agent-msg-bubble.user {
  flex-direction: row-reverse;
}

.msg-content {
  max-width: 60%;
}

.msg-text {
  background: #fff;
  padding: 10px 14px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
}

.agent-msg-bubble.agent .msg-text {
  background: #ecf5ff;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.agent-msg-bubble.user .msg-text {
  background: #fff;
  border-top-right-radius: 12px;
  border-top-left-radius: 4px;
}

.msg-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
  padding: 0 4px;
}

/* Reply Area */
.reply-area {
  border-top: 1px solid #e4e7ed;
  background: #fff;
}

.content-picker {
  border-bottom: 1px solid #f0f2f5;
}

.picker-tabs {
  border: none;
}

.picker-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 12px;
}

.picker-tabs :deep(.el-tabs__content) {
  padding: 10px 12px;
}

.picker-list {
  max-height: 160px;
  overflow-y: auto;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.picker-item:hover {
  background: #f5f7fa;
}

.picker-item-info {
  flex: 1;
  min-width: 0;
}

.picker-item-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.picker-item-sub {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reply-cards-preview {
  padding: 8px 12px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f2f5;
}

.preview-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.preview-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.reply-input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 10px 12px;
}

.reply-text-input {
  flex: 1;
}

/* Chat Placeholder */
.chat-panel-placeholder {
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-panel-placeholder :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.placeholder-sub {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

/* Notification */
.notif-item {
  padding: 12px;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}

.notif-item:hover {
  background: #f5f7fa;
}

.notif-item.unread {
  background: #f0f5ff;
}

.notif-dot {
  margin-bottom: 6px;
}

.notif-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.notif-desc {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.notif-time {
  font-size: 11px;
  color: #c0c4cc;
}
</style>
