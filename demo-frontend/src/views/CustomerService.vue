<template>
  <div>
    <el-row :gutter="20">
      <!-- 客服状态面板 -->
      <el-col :span="24">
        <el-card shadow="hover" class="mb-20">
          <template #header>
            <span class="card-title">客服在线状态</span>
          </template>
          <div class="agents-panel">
            <div v-for="agent in agents" :key="agent.id" class="agent-card">
              <el-avatar :size="48">
                <el-icon :size="24"><UserFilled /></el-icon>
              </el-avatar>
              <div class="agent-info">
                <div class="agent-name">{{ agent.name }}</div>
                <div class="agent-role">{{ agent.role }}</div>
                <div class="agent-status">
                  <span class="status-badge" :class="'status-' + agent.status"></span>
                  {{ statusText(agent.status) }}
                </div>
              </div>
              <div class="agent-actions">
                <el-switch
                  v-model="agent.status"
                  active-value="online"
                  inactive-value="offline"
                  inline-prompt
                  active-text="在线"
                  inactive-text="离线"
                  @change="handleToggleStatus(agent)"
                />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 等待队列 -->
      <el-col :span="8">
        <el-card shadow="hover" class="queue-card">
          <template #header>
            <span class="card-title">等待队列 ({{ queue.length }})</span>
          </template>
          <div class="queue-list">
            <div v-for="session in queue" :key="session.id" class="queue-item">
              <div class="queue-item-info">
                <div class="queue-member">{{ session.memberName }}</div>
                <div class="queue-preview">{{ session.lastMessage }}</div>
                <el-tag size="small" :type="session.isVip ? 'warning' : 'info'" class="mt-4">
                  {{ session.isVip ? 'VIP' : '普通' }} | {{ session.waitTime }}
                </el-tag>
              </div>
              <el-button type="primary" size="small" @click="assignSession(session)">接入</el-button>
            </div>
            <el-empty v-if="queue.length === 0" description="暂无等待会话" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 客服工作台 -->
      <el-col :span="16">
        <el-card shadow="hover" body-style="padding: 0" class="workspace-card">
          <template v-if="activeSession">
            <div class="cs-workspace">
              <!-- 用户信息面板 -->
              <div class="cs-user-panel">
                <div class="user-header">
                  <el-avatar :size="56">
                    <el-icon :size="28"><UserFilled /></el-icon>
                  </el-avatar>
                  <div class="user-basic">
                    <div class="user-name">{{ activeSession.memberName }}</div>
                    <el-tag size="small" :type="activeSession.isVip ? 'warning' : 'info'">
                      {{ activeSession.memberLevel }}
                    </el-tag>
                  </div>
                </div>
                <el-divider />
                <div class="user-details">
                  <div class="detail-item">
                    <span class="label">手机号</span>
                    <span class="value">{{ activeSession.phone || '138****8888' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">专属管家</span>
                    <span class="value">{{ activeSession.manager || '张经理' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">积分余额</span>
                    <span class="value">{{ activeSession.points || 12500 }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">累计消费</span>
                    <span class="value">¥{{ activeSession.totalSpend || '58,200' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">历史会话</span>
                    <span class="value">{{ activeSession.historyCount || 5 }}次</span>
                  </div>
                </div>
                <el-divider />
                <div>
                  <div class="detail-label" style="margin-bottom: 8px">快捷回复</div>
                  <el-tag
                    v-for="reply in quickReplies"
                    :key="reply"
                    size="small"
                    class="quick-reply-tag"
                    @click="insertReply(reply)"
                  >
                    {{ reply }}
                  </el-tag>
                </div>
              </div>

              <!-- 聊天区域 -->
              <div class="cs-chat-area">
                <div class="chat-toolbar">
                  <span class="session-title">{{ activeSession.memberName }} - {{ activeSession.lastMessage }}</span>
                  <div style="display: flex; gap: 8px">
                    <el-button type="success" size="small" @click="closeSession">
                      <el-icon><CircleCheck /></el-icon>
                      结束会话
                    </el-button>
                  </div>
                </div>

                <div class="chat-messages" ref="chatContainer">
                  <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['message-bubble', msg.role]">
                    <div class="avatar">
                      <el-icon><component :is="msg.role === 'agent' ? 'Service' : 'User'" /></el-icon>
                    </div>
                    <div>
                      <div class="content">{{ msg.content }}</div>
                      <div class="time">{{ msg.time }}</div>
                    </div>
                  </div>
                </div>

                <div class="chat-input-area">
                  <el-input
                    v-model="agentInput"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 4 }"
                    placeholder="输入回复内容..."
                    @keydown.enter.exact.prevent="sendAgentMessage"
                    resize="none"
                  />
                  <div style="display: flex; justify-content: flex-end; margin-top: 8px">
                    <el-button type="primary" @click="sendAgentMessage" :disabled="!agentInput.trim()">
                      <el-icon><Promotion /></el-icon>
                      发送
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <el-empty v-else description="请选择或接入一个会话" style="padding: 80px 0" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const agents = ref([])
const queue = ref([])
const activeSession = ref(null)
const chatMessages = ref([])
const agentInput = ref('')
const chatContainer = ref(null)

const quickReplies = [
  '您好，请问有什么可以帮到您的？',
  '感谢您的反馈，我们会尽快处理。',
  '请问还有其他问题吗？',
  '祝您购物愉快！',
  '建议您前往一楼客服中心办理。',
]

const statusText = (s) => ({ online: '在线', busy: '忙碌', offline: '离线' }[s] || s)

const fetchAgents = async () => {
  try {
    const data = await api.get('/v1/agents')
    if (data && data.length) agents.value = data
    else throw new Error('no data')
  } catch (e) {
    agents.value = [
      { id: 1, name: '王小美', role: '金牌客服', status: 'online', handlingCount: 2 },
      { id: 2, name: '李大明', role: '客服专员', status: 'online', handlingCount: 1 },
      { id: 3, name: '赵小红', role: 'VIP客服', status: 'busy', handlingCount: 3 },
      { id: 4, name: '孙小强', role: '客服专员', status: 'offline', handlingCount: 0 },
    ]
  }
}

const fetchQueue = async () => {
  try {
    const data = await api.get('/v1/cs/sessions')
    if (data && data.length) queue.value = data.filter(s => s.status === 'waiting')
    else throw new Error('no data')
  } catch (e) {
    queue.value = [
      { id: 'cs-001', memberName: '张先生', lastMessage: '我想咨询一下退换货', waitTime: '等待3分钟', isVip: true, memberLevel: '金卡', phone: '138****8888', manager: '张经理', points: 12500, totalSpend: '58,200', historyCount: 5 },
      { id: 'cs-002', memberName: '李女士', lastMessage: '停车场能月租吗？', waitTime: '等待8分钟', isVip: false, memberLevel: '银卡', phone: '139****6666', manager: '无', points: 3200, totalSpend: '15,800', historyCount: 2 },
      { id: 'cs-003', memberName: '王先生', lastMessage: '投诉某专柜服务态度差', waitTime: '等待12分钟', isVip: true, memberLevel: '钻石卡', phone: '136****9999', manager: '刘经理', points: 85000, totalSpend: '320,000', historyCount: 15 },
    ]
  }
}

const handleToggleStatus = async (agent) => {
  const newStatus = agent.status === 'online' ? 'online' : 'offline'
  try {
    await api.put(`/v1/agents/${agent.id}/status`, { status: newStatus })
    ElMessage.success(`${agent.name} 已${newStatus === 'online' ? '上线' : '下线'}`)
  } catch (e) {
    ElMessage.success(`${agent.name} 状态已更新（演示模式）`)
  }
}

const assignSession = async (session) => {
  try {
    await api.post(`/v1/cs/sessions/${session.id}/assign`, { agentId: 1 })
    ElMessage.success(`已接入 ${session.memberName}`)
  } catch (e) {
    ElMessage.success(`已接入 ${session.memberName}（演示模式）`)
  }
  activeSession.value = session
  chatMessages.value = [
    { role: 'user', content: session.lastMessage, time: '14:30' },
  ]
  queue.value = queue.value.filter(s => s.id !== session.id)
  scrollToBottom()
}

const sendAgentMessage = async () => {
  const text = agentInput.value.trim()
  if (!text || !activeSession.value) return

  const now = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  chatMessages.value.push({ role: 'agent', content: text, time: now })
  agentInput.value = ''

  try {
    await api.post(`/v1/cs/sessions/${activeSession.value.id}/messages`, { content: text, role: 'agent' })
  } catch (e) {}

  await nextTick()
  scrollToBottom()
}

const insertReply = (text) => {
  agentInput.value = text
}

const closeSession = async () => {
  try {
    await ElMessageBox.confirm('确定结束此会话吗？结束后将发送满意度调查。', '确认结束', {
      confirmButtonText: '结束并发送调查',
      cancelButtonText: '取消',
      type: 'info'
    })
    await api.put(`/v1/cs/sessions/${activeSession.value.id}/close`)
    try {
      await api.post('/v1/satisfaction', { sessionId: activeSession.value.id, type: 'survey_sent' })
    } catch (e) {}
    ElMessage.success('会话已结束，满意度调查已发送')
    activeSession.value = null
    chatMessages.value = []
    fetchQueue()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.success('会话已结束（演示模式）')
      activeSession.value = null
      chatMessages.value = []
    }
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  fetchAgents()
  fetchQueue()
})
</script>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.agents-panel {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.agent-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  min-width: 220px;
}

.agent-info {
  flex: 1;
}

.agent-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.agent-role {
  font-size: 12px;
  color: #909399;
}

.agent-status {
  font-size: 12px;
  color: #606266;
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.queue-card, .workspace-card {
  height: calc(100vh - 280px);
}

.queue-list {
  max-height: calc(100vh - 380px);
  overflow-y: auto;
}

.queue-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.queue-member {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.queue-preview {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.mt-4 { margin-top: 4px }

.user-header {
  text-align: center;
  padding: 8px 0;
}

.user-basic {
  margin-top: 8px;
}

.user-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.user-details {
  padding: 4px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
}

.detail-item .label {
  color: #909399;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

.detail-label {
  font-weight: 600;
  font-size: 13px;
  color: #606266;
}

.quick-reply-tag {
  margin-right: 6px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-reply-tag:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
}
</style>
