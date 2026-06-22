import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(config => {
  return config
})

api.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// V1 APIs
export const createSession = (data) => api.post('/v1/chat/sessions', data)
export const getSessions = () => api.get('/v1/chat/sessions')
export const getSessionHistory = (sessionId) => api.get(`/v1/chat/sessions/${sessionId}/history`)
export const sendMessage = (sessionId, data) => api.post(`/v1/chat/sessions/${sessionId}/messages`, data)
export const transferSession = (sessionId, data) => api.post(`/v1/chat/sessions/${sessionId}/transfer`, data)
export const resetDemo = () => api.post('/v1/demo/reset')

// VIP Routing
export const vipRoute = (data) => api.post('/v1/vip/route', data)

// Backend Data Queries (V2)
export const getMerchants = (params) => api.get('/v2/data/merchants', { params })
export const getProducts = (params) => api.get('/v2/data/products', { params })
export const getEvents = (params) => api.get('/v2/data/events', { params })
export const getCoupons = (params) => api.get('/v2/data/coupons', { params })
export const getNavigation = (params) => api.get('/v2/data/navigation', { params })

// V2 Chat (multimodal responses)
export const sendV2Message = (sessionId, data) => api.post(`/v2/chat/sessions/${sessionId}/messages`, data)
export const transferV2 = (sessionId, data) => api.post(`/v2/chat/sessions/${sessionId}/transfer`, data)

// Agent Workbench
export const getAgentTasks = (agentId) => api.get(`/v1/agent/vip/${agentId}/tasks`)
export const acceptTask = (agentId, taskId) => api.post(`/v1/agent/vip/${agentId}/tasks/${taskId}/accept`)
export const respondToVIP = (agentId, taskId, data) => api.post(`/v1/agent/vip/${agentId}/tasks/${taskId}/respond`, data)
export const getAgentNotifications = (agentId) => api.get(`/v1/agent/vip/${agentId}/notifications`)
export const markNotificationRead = (agentId, notifId) => api.put(`/v1/agent/vip/${agentId}/notifications/${notifId}/read`)

export default api
