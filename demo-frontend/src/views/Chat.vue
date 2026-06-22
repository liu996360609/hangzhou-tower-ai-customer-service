<template>
  <el-card shadow="never" class="chat-page-card" body-style="padding: 0">
    <!-- User Type Selector -->
    <div class="user-type-bar">
      <span class="bar-label">模拟用户类型：</span>
      <el-radio-group v-model="userType" size="small" @change="onUserTypeChange">
        <el-radio-button value="regular">普通用户</el-radio-button>
        <el-radio-button value="vip">
          <el-icon><StarFilled /></el-icon>
          VIP用户
        </el-radio-button>
      </el-radio-group>
      <el-tag v-if="userType === 'vip'" type="warning" size="small" effect="dark" class="vip-tag">
        <el-icon><StarFilled /></el-icon> 金卡会员
      </el-tag>
    </div>

    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="chat-session-list">
        <div class="session-list-header">
          <el-button type="primary" size="small" @click="handleNewSession" style="width: 100%">
            <el-icon><Plus /></el-icon>
            新建会话
          </el-button>
        </div>
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSession?.id === session.id }"
          @click="selectSession(session)"
        >
          <div class="session-name">{{ session.title }}</div>
          <div class="session-preview">{{ session.lastMessage || '新会话' }}</div>
          <div class="session-time">{{ session.time }}</div>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <template v-if="currentSession">
          <!-- 工具栏 -->
          <div class="chat-toolbar">
            <div class="toolbar-left">
              <span class="session-title">{{ currentSession.title }}</span>
              <el-tag v-if="isVipAgentConnected" type="success" size="small" effect="dark">
                <el-icon><Service /></el-icon> 专属客服已接入
              </el-tag>
              <el-tag v-else-if="userType === 'vip'" type="warning" size="small">
                <el-icon><StarFilled /></el-icon> VIP专属通道
              </el-tag>
            </div>
            <div style="display: flex; gap: 8px">
              <el-button size="small" @click="handleTransfer" :disabled="isTransferring">
                <el-icon><Switch /></el-icon>
                转人工
              </el-button>
            </div>
          </div>

          <!-- 消息区域 -->
          <div class="chat-messages" ref="messageContainer">
            <div v-for="(msg, idx) in messages" :key="idx">
              <!-- Regular message bubble -->
              <template v-if="!msg.cards || msg.cards.length === 0">
                <div :class="['message-bubble', msg.role]">
                  <div class="avatar">
                    <el-icon><component :is="getAvatarIcon(msg)" /></el-icon>
                  </div>
                  <div>
                    <!-- VIP Agent Header -->
                    <div v-if="msg.role === 'agent'" class="agent-header">
                      <el-tag type="warning" size="small">专属客服</el-tag>
                      <span class="agent-name">{{ msg.agentName }}</span>
                    </div>
                    <div class="content" style="white-space: pre-wrap">{{ msg.content }}</div>
                    <div class="time">{{ msg.time }}</div>
                  </div>
                </div>
              </template>
              <!-- Multimodal message with cards -->
              <template v-else>
                <div class="message-bubble bot">
                  <div class="avatar"><el-icon><Cpu /></el-icon></div>
                  <div>
                    <div class="content" v-if="msg.content" style="white-space: pre-wrap">{{ msg.content }}</div>
                    <div class="cards-container">
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
                        @action="handleCardAction"
                      />
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <div v-if="isTyping" class="message-bubble bot">
              <div class="avatar"><el-icon><Cpu /></el-icon></div>
              <div>
                <div class="content">
                  <span v-if="vipLoadingText" class="loading-text">{{ vipLoadingText }}</span>
                  <span v-else class="typing-dots">
                    <span>.</span><span>.</span><span>.</span>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <el-input
              v-model="inputText"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="输入消息，按 Enter 发送..."
              @keydown.enter.exact.prevent="sendMessage"
              resize="none"
            />
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
              <span style="font-size: 12px; color: #c0c4cc">Enter 发送，Shift+Enter 换行</span>
              <el-button type="primary" @click="sendMessage" :loading="isTyping" :disabled="!inputText.trim()">
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </template>

        <el-empty v-else description="请选择或新建一个会话" />
      </div>
    </div>
  </el-card>

  <!-- Transfer Dialog for Regular Users -->
  <el-dialog v-model="transferDialogVisible" title="转人工服务" width="480px" :close-on-click-modal="false">
    <div class="transfer-info">
      <el-result icon="info" title="人工客服信息" sub-title="普通用户请拨打以下电话或前往服务台咨询">
        <template #extra>
          <div class="transfer-detail">
            <div class="detail-item">
              <el-icon :size="20" color="#409eff"><Phone /></el-icon>
              <div>
                <div class="detail-label">服务热线</div>
                <div class="detail-value">0571-85158888</div>
              </div>
            </div>
            <div class="detail-item">
              <el-icon :size="20" color="#409eff"><Location /></el-icon>
              <div>
                <div class="detail-label">服务台地址</div>
                <div class="detail-value">杭州大厦A座一楼客服中心</div>
              </div>
            </div>
          </div>
          <el-alert type="warning" :closable="false" style="margin-top: 16px">
            暂不支持在线转接，请拨打服务热线或前往现场咨询
          </el-alert>
        </template>
      </el-result>
    </div>
    <template #footer>
      <el-button type="primary" @click="transferDialogVisible = false">我知道了</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, { sendV2Message, vipRoute, getMerchants, getProducts, getEvents, getCoupons, getNavigation } from '@/api'
import MerchantCard from '@/components/MerchantCard.vue'
import ProductCard from '@/components/ProductCard.vue'
import EventCard from '@/components/EventCard.vue'
import CouponCard from '@/components/CouponCard.vue'
import NavigationCard from '@/components/NavigationCard.vue'

// User type state
const userType = ref('regular') // 'regular' | 'vip'
const isVipAgentConnected = ref(false)
const isTransferring = ref(false)
const transferDialogVisible = ref(false)
const vipLoadingText = ref('')

// Chat state
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const messageContainer = ref(null)

const aiResponses = [
  '您好！我是杭州大厦AI客服小助手。关于您的问题，我为您解答：\n\n金卡会员的办理条件是在商场累计消费满50,000元或单笔消费满30,000元。您可以携带身份证和购物凭证到一楼客服中心办理。\n\n还有什么可以帮到您的吗？',
  '您好！杭州大厦的停车场收费标准如下：\n\n• 工作日：首小时免费，之后每小时10元，每日封顶80元\n• 周末及节假日：首小时免费，之后每小时15元，每日封顶100元\n• 会员享受8折优惠\n\n请问还有其他问题吗？',
  '您好！杭州大厦营业时间为：\n\n• 周一至周四：10:00 - 21:30\n• 周五至周日：10:00 - 22:00\n• 节假日可能调整，请关注官方公众号\n\n祝您购物愉快！',
  '关于积分兑换，具体规则如下：\n\n• 5,000积分 = 50元购物券\n• 10,000积分 = 120元购物券\n• 20,000积分 = 280元购物券\n\n金卡会员积分1.5倍累积，兑换更划算哦！',
]
let responseIndex = 0

// Mock data for multimodal cards
const mockMerchants = [
  { id: 1, name: 'Dior美妆', image: 'https://via.placeholder.com/280x140/8B5CF6/fff?text=Dior', floor: 'A座1F', category: '美妆', description: '国际知名奢侈美妆品牌，新品首发地' },
  { id: 2, name: '绿茶餐厅', image: 'https://via.placeholder.com/280x140/10B981/fff?text=GreenTea', floor: 'C座4F', category: '餐饮', description: '杭帮菜经典，人均60-80元' },
  { id: 3, name: '周大福', image: 'https://via.placeholder.com/280x140/F59E0B/fff?text=ChowTaiFook', floor: 'A座2F', category: '珠宝', description: '知名珠宝品牌，黄金首饰、钻石定制' },
]

const mockProducts = [
  { id: 1, name: 'Dior烈艳蓝金唇膏999', image: 'https://via.placeholder.com/260x140/EC4899/fff?text=Dior+999', price: 370, brand: 'Dior', description: '经典正红色，哑光丝绒质地' },
  { id: 2, name: '周大福传承系列黄金手镯', image: 'https://via.placeholder.com/260x140/F59E0B/fff?text=Gold', price: 12800, brand: '周大福', description: '古法黄金工艺，寓意传承' },
]

const mockEvents = [
  { id: 1, title: '杭州大厦春季美妆节', image: 'https://via.placeholder.com/280x140/F43F5E/fff?text=Beauty', startDate: '2026-03-15', endDate: '2026-04-15', location: 'A座1F中庭', description: '大牌美妆满1000减200，新品首发体验' },
  { id: 2, title: '珠宝巡展-璀璨之星', image: 'https://via.placeholder.com/280x140/8B5CF6/fff?text=Jewelry', startDate: '2026-04-01', endDate: '2026-04-30', location: 'A座2F', description: '国际珠宝品牌联合展出，限量款首发' },
]

const mockCoupons = [
  { id: 1, name: '美妆品类满减券', value: 200, type: 'cash', condition: 1000, expiryDate: '2026-04-30' },
  { id: 2, name: '全场通用8.8折券', value: '8.8', type: 'percent', condition: 500, expiryDate: '2026-12-31' },
]

const mockNavigations = [
  { id: 1, name: '客服中心', location: 'A座1F东侧', floor: '1F', description: '会员办理、积分兑换、咨询服务' },
  { id: 2, name: '停车场入口', location: 'B座B1层', floor: 'B1', description: '会员停车8折优惠，充电桩可用' },
]

// VIP state
const vipAgentInfo = ref(null)
const vipQueuePosition = ref(0)

const onUserTypeChange = () => {
  // Clear VIP state when switching
  isVipAgentConnected.value = false
  vipAgentInfo.value = null
  vipLoadingText.value = ''
}

// Card rendering helpers
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

const getAvatarIcon = (msg) => {
  if (msg.role === 'agent') return 'Service'
  if (msg.role === 'bot') return 'Cpu'
  return 'User'
}

const handleCardAction = (cardData) => {
  ElMessage.success('已打开卡片详情（演示模式）')
}

// Build multimodal response based on user query
const buildMultimodalResponse = (query) => {
  const cards = []
  const q = query.toLowerCase()

  if (q.includes('美妆') || q.includes('化妆品') || q.includes('品牌') || q.includes('有什么店')) {
    cards.push({ type: 'merchantCard', data: mockMerchants[0] })
    cards.push({ type: 'merchantCard', data: mockMerchants[1] })
  }
  if (q.includes('吃') || q.includes('餐厅') || q.includes('餐饮')) {
    cards.push({ type: 'merchantCard', data: mockMerchants[1] })
  }
  if (q.includes('口红') || q.includes('唇膏') || q.includes('推荐') || q.includes('商品')) {
    cards.push({ type: 'productCard', data: mockProducts[0] })
  }
  if (q.includes('黄金') || q.includes('珠宝') || q.includes('手镯')) {
    cards.push({ type: 'productCard', data: mockProducts[1] })
    cards.push({ type: 'merchantCard', data: mockMerchants[2] })
  }
  if (q.includes('活动') || q.includes('促销') || q.includes('优惠')) {
    cards.push({ type: 'eventCard', data: mockEvents[0] })
    cards.push({ type: 'couponCard', data: mockCoupons[0] })
  }
  if (q.includes('客服') || q.includes('在哪') || q.includes('位置') || q.includes('导航') || q.includes('怎么去')) {
    cards.push({ type: 'navigationCard', data: mockNavigations[0] })
  }
  if (q.includes('停车')) {
    cards.push({ type: 'navigationCard', data: mockNavigations[1] })
  }

  // Fallback: show a mix if no specific match
  if (cards.length === 0) {
    cards.push({ type: 'navigationCard', data: mockNavigations[0] })
    cards.push({ type: 'couponCard', data: mockCoupons[1] })
  }

  return {
    content: '为您找到以下信息：',
    cards: cards
  }
}

// Fetch sessions
const fetchSessions = async () => {
  try {
    const data = await api.get('/v1/chat/sessions')
    if (data && data.length) sessions.value = data
    else throw new Error('no data')
  } catch (e) {
    sessions.value = [
      { id: 'sess-001', title: '会员咨询', lastMessage: '金卡怎么办？', time: '14:32' },
      { id: 'sess-002', title: '停车收费', lastMessage: '周末怎么收费', time: '14:28' },
      { id: 'sess-003', title: '品牌推荐', lastMessage: '有哪些美妆品牌', time: '13:58' },
    ]
  }
}

const selectSession = async (session) => {
  currentSession.value = session
  // Reset VIP state on session switch
  isVipAgentConnected.value = false
  vipLoadingText.value = ''
  try {
    const data = await api.get(`/v1/chat/sessions/${session.id}/history`)
    if (data && data.length) messages.value = data
    else throw new Error('no data')
  } catch (e) {
    messages.value = [
      { role: 'bot', content: '您好！我是杭州大厦AI客服小助手，有什么可以帮到您的？', time: session.time },
      { role: 'user', content: session.lastMessage, time: session.time },
      { role: 'bot', content: aiResponses[responseIndex % aiResponses.length], time: session.time },
    ]
    responseIndex++
  }
  scrollToBottom()
}

const handleNewSession = async () => {
  const newSession = { id: `sess-${Date.now()}`, title: '新会话', lastMessage: '', time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }
  try {
    const data = await api.post('/v1/chat/sessions', { title: '新会话' })
    if (data) Object.assign(newSession, data)
  } catch (e) {}
  sessions.value.unshift(newSession)
  selectSession(newSession)
  // Reset VIP state
  isVipAgentConnected.value = false
  vipLoadingText.value = ''
  messages.value = [{ role: 'bot', content: '您好！我是杭州大厦AI客服小助手，有什么可以帮到您的？', time: newSession.time }]
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value || !currentSession.value) return

  const now = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  messages.value.push({ role: 'user', content: text, time: now })
  currentSession.value.lastMessage = text
  currentSession.value.time = now
  inputText.value = ''
  await nextTick()
  scrollToBottom()

  isTyping.value = true

  // VIP first message routing
  if (userType.value === 'vip' && !isVipAgentConnected.value && !vipAgentInfo.value) {
    await handleVipFirstMessage(text, now)
    return
  }

  // If VIP agent is connected, simulate agent response
  if (userType.value === 'vip' && isVipAgentConnected.value) {
    await new Promise(resolve => setTimeout(resolve, 1500 + Math.random() * 1000))
    messages.value.push({
      role: 'agent',
      agentName: vipAgentInfo.value?.name || '专属客服',
      content: `您好，我是您的专属客服${vipAgentInfo.value?.name || ''}。关于您的问题："${text}"，我为您解答：\n\n${aiResponses[responseIndex % aiResponses.length]}`,
      time: now
    })
    responseIndex++
    isTyping.value = false
    await nextTick()
    scrollToBottom()
    return
  }

  // Regular user: V2 multimodal response
  await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1000))

  try {
    const data = await sendV2Message(currentSession.value.id, { content: text, userType: userType.value })
    if (data) {
      if (data.cards && data.cards.length > 0) {
        messages.value.push({ role: 'bot', content: data.content || '为您找到以下信息：', cards: data.cards, time: now })
      } else {
        messages.value.push({ role: 'bot', content: data.answer || data.content, time: now })
      }
    } else {
      throw new Error('no data')
    }
  } catch (e) {
    // Fallback: build multimodal response locally
    const response = buildMultimodalResponse(text)
    messages.value.push({ role: 'bot', ...response, time: now })
    responseIndex++
  }

  isTyping.value = false
  await nextTick()
  scrollToBottom()
}

const handleVipFirstMessage = async (text, now) => {
  // Show loading
  vipLoadingText.value = '正在为您查询专属客服...'
  await new Promise(resolve => setTimeout(resolve, 2000))

  try {
    const data = await vipRoute({ userId: 'vip-user-001', message: text })
    if (data && data.agent) {
      // Agent available
      vipAgentInfo.value = data.agent
      isVipAgentConnected.value = true
      vipLoadingText.value = ''
      messages.value.push({
        role: 'agent',
        agentName: data.agent.name,
        content: `您好！我是您的专属客服${data.agent.name}，很高兴为您服务。请问有什么可以帮到您的？`,
        time: now
      })
    }
  } catch (e) {
    // Fallback: simulate agent availability
    const agentAvailable = Math.random() > 0.3 // 70% chance agent is free
    vipLoadingText.value = ''

    if (agentAvailable) {
      const agent = { id: 'agent-001', name: '张三', phone: '0571-85158899' }
      vipAgentInfo.value = agent
      isVipAgentConnected.value = true
      messages.value.push({
        role: 'agent',
        agentName: agent.name,
        content: `您好！我是您的专属客服${agent.name}，很高兴为您服务。请问有什么可以帮到您的？`,
        time: now
      })
    } else {
      const queuePos = Math.floor(Math.random() * 3) + 1
      vipQueuePosition.value = queuePos
      // Show queue message with options
      messages.value.push({
        role: 'bot',
        content: `当前专属客服繁忙，前方还有 ${queuePos} 位用户在等待。`,
        cards: [
          {
            type: 'navigationCard',
            data: {
              name: `等待接入（前方${queuePos}人）`,
              location: '预计等待时间：约5-10分钟',
              description: '您可以选择等待或致电专属客服'
            }
          }
        ],
        time: now,
        hasQueueOptions: true
      })
    }
  }

  isTyping.value = false
  await nextTick()
  scrollToBottom()
}

const handleTransfer = async () => {
  if (userType.value === 'vip') {
    // VIP transfer: route to agent
    isTransferring.value = true
    try {
      await api.post(`/v1/chat/sessions/${currentSession.value.id}/transfer`, { userType: 'vip', reason: '用户请求' })
      ElMessage.success('正在为您转接专属客服...')
    } catch (e) {
      // Fallback: simulate
      ElMessage.success('正在为您转接专属客服（演示模式）')
    } finally {
      isTransferring.value = false
    }
  } else {
    // Regular user: show phone number dialog
    transferDialogVisible.value = true
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

onMounted(fetchSessions)
</script>

<style scoped>
.chat-page-card {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.user-type-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(90deg, #f0f5ff 0%, #fff 100%);
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.bar-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.vip-tag {
  margin-left: 4px;
}

.chat-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-session-list {
  width: 240px;
  border-right: 1px solid #e4e7ed;
  background: #fafbfc;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.session-list-header {
  padding: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.session-item {
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.session-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-preview {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.session-time {
  font-size: 11px;
  color: #c0c4cc;
  text-align: right;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.session-title {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f7f8fa;
}

.message-bubble {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  max-width: 85%;
}

.message-bubble.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8eaf0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #606266;
}

.message-bubble.user .avatar {
  background: #409eff;
  color: #fff;
}

.content {
  background: #fff;
  padding: 12px 14px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  max-width: 520px;
  word-break: break-word;
}

.message-bubble.user .content {
  background: #409eff;
  color: #fff;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
  padding: 0 4px;
}

.message-bubble.user .time {
  text-align: right;
}

.cards-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.agent-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.agent-name {
  font-size: 12px;
  color: #e6a23c;
  font-weight: 600;
}

.loading-text {
  color: #909399;
  font-style: italic;
  font-size: 13px;
}

.chat-input-area {
  padding: 14px 20px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.transfer-info {
  padding: 10px 0;
}

.transfer-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.typing-dots span {
  animation: blink 1.4s infinite both;
  font-size: 20px;
  color: #909399;
}

.typing-dots span:nth-child(2) { animation-delay: 0.2s }
.typing-dots span:nth-child(3) { animation-delay: 0.4s }

@keyframes blink {
  0%, 80%, 100% { opacity: 0 }
  40% { opacity: 1 }
}
</style>
