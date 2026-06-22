/**
 * 杭州大厦AI客服系统 - Mock Server
 * 包含所有72+ API端点
 */

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { resetData, deepCloneData } = require('./data/resetData');

const app = express();
const PORT = process.env.PORT || 8080;

// 中间件
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// 请求日志
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// ==================== 数据存储 ====================
let data = deepCloneData();

// ==================== 辅助函数 ====================
function success(res, payload = null) {
  return res.json({ code: 200, message: 'success', data: payload });
}

function error(res, message = '操作失败', code = 500) {
  return res.status(200).json({ code, message, data: null });
}

function notFound(res, resource = '资源') {
  return res.status(200).json({ code: 404, message: `${resource}不存在`, data: null });
}

function paginate(list, page = 1, size = 10) {
  const start = (page - 1) * size;
  const end = start + size;
  return {
    records: list.slice(start, end),
    total: list.length,
    page,
    size,
  };
}

// ==================== 标准响应中间件 ====================
app.use('/api', (req, res, next) => {
  const originalJson = res.json.bind(res);
  res.json = function (body) {
    if (body && typeof body === 'object' && 'code' in body) {
      return originalJson(body);
    }
    return originalJson({ code: 200, message: 'success', data: body });
  };
  next();
});

// ==================== Knowledge Base - Categories ====================
app.get('/api/v1/knowledge/categories', (req, res) => {
  success(res, data.categories.sort((a, b) => a.sortOrder - b.sortOrder));
});

app.post('/api/v1/knowledge/categories', (req, res) => {
  const now = new Date().toISOString();
  const newCat = {
    id: data.nextCategoryId++,
    name: req.body.name,
    description: req.body.description || '',
    sortOrder: req.body.sortOrder || data.categories.length + 1,
    createdAt: now,
    updatedAt: now,
  };
  data.categories.push(newCat);
  success(res, newCat);
});

app.put('/api/v1/knowledge/categories/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const cat = data.categories.find(c => c.id === id);
  if (!cat) return notFound(res, '分类');
  Object.assign(cat, req.body, { updatedAt: new Date().toISOString() });
  success(res, cat);
});

app.delete('/api/v1/knowledge/categories/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const idx = data.categories.findIndex(c => c.id === id);
  if (idx === -1) return notFound(res, '分类');
  data.categories.splice(idx, 1);
  success(res, null);
});

// ==================== Knowledge Base - Files ====================
app.get('/api/v1/knowledge/files', (req, res) => {
  const { categoryId, status } = req.query;
  let files = [...data.knowledgeFiles];
  if (categoryId) files = files.filter(f => f.categoryId === parseInt(categoryId));
  if (status) files = files.filter(f => f.status === status);
  success(res, files);
});

app.post('/api/v1/knowledge/files/upload', (req, res) => {
  const now = new Date().toISOString();
  const newFile = {
    id: data.nextFileId++,
    fileName: req.body.fileName || 'unknown-file.pdf',
    originalName: req.body.originalName || req.body.fileName || 'unknown-file.pdf',
    categoryId: req.body.categoryId || 1,
    fileSize: req.body.fileSize || Math.floor(Math.random() * 500000) + 10000,
    mimeType: req.body.mimeType || 'application/pdf',
    status: 'PARSING',
    parseProgress: 0,
    tags: req.body.tags || [],
    uploadedBy: req.body.uploadedBy || '系统',
    createdAt: now,
    updatedAt: now,
  };
  data.knowledgeFiles.push(newFile);
  success(res, newFile);
});

app.post('/api/v1/knowledge/files/:id/parse', (req, res) => {
  const id = parseInt(req.params.id);
  const file = data.knowledgeFiles.find(f => f.id === id);
  if (!file) return notFound(res, '文件');
  // 模拟解析过程
  file.status = 'PARSE_SUCCESS';
  file.parseProgress = 100;
  file.updatedAt = new Date().toISOString();
  success(res, file);
});

app.get('/api/v1/knowledge/files/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const file = data.knowledgeFiles.find(f => f.id === id);
  if (!file) return notFound(res, '文件');
  success(res, file);
});

app.delete('/api/v1/knowledge/files/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const idx = data.knowledgeFiles.findIndex(f => f.id === id);
  if (idx === -1) return notFound(res, '文件');
  data.knowledgeFiles.splice(idx, 1);
  success(res, null);
});

app.put('/api/v1/knowledge/files/:id/tags', (req, res) => {
  const id = parseInt(req.params.id);
  const file = data.knowledgeFiles.find(f => f.id === id);
  if (!file) return notFound(res, '文件');
  file.tags = req.body.tags || [];
  file.updatedAt = new Date().toISOString();
  success(res, file);
});

// ==================== Knowledge Base - Parse Settings ====================
app.get('/api/v1/knowledge/parse-settings/:catId', (req, res) => {
  const catId = parseInt(req.params.catId);
  const settings = data.parseSettings[catId] || { chunkSize: 500, overlap: 50, enableOcr: true, language: 'zh' };
  success(res, settings);
});

app.put('/api/v1/knowledge/parse-settings/:catId', (req, res) => {
  const catId = parseInt(req.params.catId);
  data.parseSettings[catId] = { ...data.parseSettings[catId], ...req.body };
  success(res, data.parseSettings[catId]);
});

// ==================== Knowledge Base - Items ====================
app.get('/api/v1/knowledge/items', (req, res) => {
  const { categoryId, status, keyword } = req.query;
  let items = [...data.knowledgeItems];
  if (categoryId) items = items.filter(i => i.categoryId === parseInt(categoryId));
  if (status) items = items.filter(i => i.status === status);
  if (keyword) {
    const kw = keyword.toLowerCase();
    items = items.filter(i =>
      i.title.toLowerCase().includes(kw) || i.content.toLowerCase().includes(kw)
    );
  }
  const { page, size } = req.query;
  if (page && size) {
    success(res, paginate(items, parseInt(page), parseInt(size)));
  } else {
    success(res, items);
  }
});

app.get('/api/v1/knowledge/items/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const item = data.knowledgeItems.find(i => i.id === id);
  if (!item) return notFound(res, '知识条目');
  success(res, item);
});

app.post('/api/v1/knowledge/items', (req, res) => {
  const now = new Date().toISOString();
  const newItem = {
    id: data.nextItemId++,
    categoryId: req.body.categoryId,
    title: req.body.title,
    content: req.body.content,
    status: req.body.status || 'draft',
    createdBy: req.body.createdBy || '系统',
    updatedBy: null,
    createdAt: now,
    updatedAt: now,
    publishedAt: null,
  };
  data.knowledgeItems.push(newItem);
  success(res, newItem);
});

app.put('/api/v1/knowledge/items/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const item = data.knowledgeItems.find(i => i.id === id);
  if (!item) return notFound(res, '知识条目');
  Object.assign(item, req.body, {
    updatedAt: new Date().toISOString(),
    updatedBy: req.body.updatedBy || item.updatedBy,
  });
  success(res, item);
});

app.delete('/api/v1/knowledge/items/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const idx = data.knowledgeItems.findIndex(i => i.id === id);
  if (idx === -1) return notFound(res, '知识条目');
  data.knowledgeItems.splice(idx, 1);
  success(res, null);
});

// ==================== Knowledge Base - Review ====================
app.post('/api/v1/knowledge/items/:id/submit-review', (req, res) => {
  const id = parseInt(req.params.id);
  const item = data.knowledgeItems.find(i => i.id === id);
  if (!item) return notFound(res, '知识条目');
  item.status = 'pending_review_a';
  item.updatedAt = new Date().toISOString();
  success(res, item);
});

app.put('/api/v1/knowledge/items/:id/review', (req, res) => {
  const id = parseInt(req.params.id);
  const item = data.knowledgeItems.find(i => i.id === id);
  if (!item) return notFound(res, '知识条目');
  const { action, reviewer } = req.body; // action: approve | reject
  if (item.status === 'pending_review_a') {
    if (action === 'approve') {
      item.status = 'pending_review_b';
    } else {
      item.status = 'draft';
    }
  } else if (item.status === 'pending_review_b') {
    if (action === 'approve') {
      item.status = 'published';
      item.publishedAt = new Date().toISOString();
    } else {
      item.status = 'pending_review_a';
    }
  }
  item.updatedAt = new Date().toISOString();
  item.updatedBy = reviewer || '审核员';
  success(res, item);
});

app.get('/api/v1/knowledge/reviews', (req, res) => {
  const pending = data.knowledgeItems.filter(i => i.status.startsWith('pending'));
  success(res, pending);
});

// ==================== Knowledge Base - Retrieve ====================
app.post('/api/v1/knowledge/retrieve', (req, res) => {
  const { query, topK = 5 } = req.body;
  if (!query) return error(res, '查询内容不能为空');

  const results = mockRetrieve(query);
  success(res, {
    query,
    results: results.slice(0, topK),
    aiResponse: generateAIResponse(query),
  });
});

function mockRetrieve(query) {
  const allResults = [
    { title: '2024年营业时间通知', score: 0.95, content: '杭州大厦营业时间为每天10:00-22:00。周一至周日正常营业，无需预约。节假日期间可能会有调整，请关注官方通知。' },
    { title: '节假日营业安排', score: 0.82, content: '春节期间营业时间调整为10:00-20:00。国庆节期间正常营业。' },
    { title: '停车场收费标准', score: 0.93, content: '小型车：首小时免费，之后5元/小时，每日封顶50元。会员享受停车优惠：银卡9折，金卡8折，钻石卡7折。' },
    { title: '停车场位置及入口', score: 0.88, content: '杭州大厦停车场位于地下B1-B3层，共500个车位。入口位于武林广场东侧。' },
    { title: 'VIP会员等级与权益', score: 0.94, content: '普卡：消费1元=1积分；银卡：累计消费5万元，积分1.2倍；金卡：累计消费20万元，积分1.5倍+专属客服；钻石卡：累计消费50万元，积分2倍+专属经理+停车7折。' },
    { title: '会员积分兑换规则', score: 0.87, content: '100积分=1元，可在服务台或线上商城兑换。积分有效期为获取后12个月。' },
    { title: '奢侈品品牌位置指南', score: 0.91, content: 'LV位于A座1楼，GUCCI位于A座2楼，CHANEL位于B座1楼，HERMES位于B座2楼。' },
    { title: '餐饮品牌楼层分布', score: 0.85, content: 'B1层：美食广场、星巴克、喜茶；5层：海底捞、外婆家、绿茶餐厅；6层：高端餐饮区。' },
    { title: '春季品牌折扣季', score: 0.90, content: '3月1日-3月31日，全场品牌参与折扣活动，部分品牌低至5折。会员额外享受95折。' },
    { title: '满减活动规则', score: 0.86, content: '单笔消费满1000减100，满3000减400，满5000减800。不可与其他优惠叠加。' },
    { title: '退换货政策', score: 0.89, content: '商品购买后7天内可退换，需保持商品完好，携带购物小票。特殊商品（食品、贴身衣物等）不支持退换。' },
    { title: '发票开具说明', score: 0.83, content: '购物后可在服务台开具发票，支持电子发票和纸质发票。电子发票将在24小时内发送至您的邮箱。' },
  ];

  const keywordMap = {
    '营业': [0, 1],
    '停车': [2, 3],
    '会员': [4, 5],
    '积分': [4, 5],
    'VIP': [4],
    '品牌': [6, 7],
    'LV': [6],
    'GUCCI': [6],
    '餐饮': [7],
    '餐厅': [7],
    '促销': [8, 9],
    '折扣': [8, 9],
    '满减': [9],
    '活动': [8, 9],
    '退换': [10],
    '退货': [10],
    '发票': [11],
  };

  let matchedIndices = new Set();
  for (const [keyword, indices] of Object.entries(keywordMap)) {
    if (query.includes(keyword)) {
      indices.forEach(i => matchedIndices.add(i));
    }
  }

  if (matchedIndices.size > 0) {
    return Array.from(matchedIndices).map(i => allResults[i]);
  }

  // 默认返回前几个
  return allResults.slice(0, 3);
}

function generateAIResponse(query) {
  const responses = {
    '营业': '杭州大厦的日常营业时间为每天10:00-22:00。节假日期间可能会有调整，建议您关注我们的官方通知。',
    '停车': '杭州大厦停车场首小时免费，之后5元/小时，每日封顶50元。会员可享受额外折扣：银卡9折，金卡8折，钻石卡7折。',
    '会员': '杭州大厦会员分为普卡、银卡、金卡和钻石卡四个等级，消费累计升级。钻石卡会员可享受积分2倍、专属经理服务和停车7折等权益。',
    '积分': '会员积分按100积分=1元的比例兑换，可在服务台或线上商城使用，有效期为获取后12个月。',
    'VIP': 'VIP会员最高等级为钻石卡，需累计消费50万元。钻石卡会员享有积分2倍、专属客服经理、停车7折、机场接送等多项专属权益。',
    '品牌': '杭州大厦汇聚众多国际知名品牌，LV位于A座1楼，GUCCI位于A座2楼，CHANEL位于B座1楼。如需了解具体品牌位置，请告诉我品牌名称。',
    '餐饮': '杭州大厦餐饮分布在多个楼层：B1层有美食广场、星巴克、喜茶等，5层有海底捞、外婆家、绿茶餐厅，6层为高端餐饮区。',
    '促销': '当前正在进行春季品牌折扣季活动，全场品牌参与，部分低至5折！会员额外享受95折优惠。',
    '折扣': '当前春季折扣季部分品牌低至5折，会员可额外享受95折。具体折扣因品牌而异，建议您到店了解最新活动。',
    '满减': '当前满减活动：单笔消费满1000减100，满3000减400，满5000减800。不可与其他优惠叠加使用。',
    '退换': '杭州大厦支持7天无理由退换货，商品需保持完好并携带购物小票。特殊商品如食品、贴身衣物等不支持退换。',
    '退货': '商品购买后7天内可办理退货，需保持商品完好并携带购物凭证。特殊商品除外。',
    '发票': '购物后可在服务台申请开具发票，支持电子发票和纸质发票两种形式。电子发票将在24小时内发送到您预留的邮箱。',
  };

  for (const [keyword, response] of Object.entries(responses)) {
    if (query.includes(keyword)) {
      return response;
    }
  }

  return '感谢您的提问！我会尽力为您解答。如需更详细的信息，建议您前往杭州大厦服务台咨询，或拨打客服热线：0571-88888888。';
}

// ==================== Knowledge Base - Patches ====================
app.get('/api/v1/knowledge/patches', (req, res) => {
  const { itemId, status } = req.query;
  let patches = [...data.patches];
  if (itemId) patches = patches.filter(p => p.itemId === parseInt(itemId));
  if (status) patches = patches.filter(p => p.status === status);
  success(res, patches);
});

app.post('/api/v1/knowledge/patches', (req, res) => {
  const now = new Date().toISOString();
  const newPatch = {
    id: data.nextPatchId++,
    itemId: req.body.itemId,
    title: req.body.title,
    content: req.body.content,
    status: req.body.status || 'draft',
    createdBy: req.body.createdBy || '系统',
    createdAt: now,
    updatedAt: now,
  };
  data.patches.push(newPatch);
  success(res, newPatch);
});

app.put('/api/v1/knowledge/patches/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const patch = data.patches.find(p => p.id === id);
  if (!patch) return notFound(res, '补充说明');
  Object.assign(patch, req.body, { updatedAt: new Date().toISOString() });
  success(res, patch);
});

app.delete('/api/v1/knowledge/patches/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const idx = data.patches.findIndex(p => p.id === id);
  if (idx === -1) return notFound(res, '补充说明');
  data.patches.splice(idx, 1);
  success(res, null);
});

// ==================== Knowledge Base - Versions ====================
app.get('/api/v1/knowledge/versions/:itemId', (req, res) => {
  const itemId = parseInt(req.params.itemId);
  const item = data.knowledgeItems.find(i => i.id === itemId);
  if (!item) return notFound(res, '知识条目');
  // 返回当前条目作为版本记录
  success(res, [
    {
      versionId: 1,
      itemId,
      title: item.title,
      content: item.content,
      status: item.status,
      changedBy: item.updatedBy || item.createdBy,
      changedAt: item.updatedAt,
    },
  ]);
});

// ==================== Knowledge Base - Stats ====================
app.get('/api/v1/knowledge/stats', (req, res) => {
  const total = data.knowledgeItems.length;
  const published = data.knowledgeItems.filter(i => i.status === 'published').length;
  const draft = data.knowledgeItems.filter(i => i.status === 'draft').length;
  const pending = data.knowledgeItems.filter(i => i.status.startsWith('pending')).length;
  success(res, { total, published, draft, pending });
});

// ==================== Chat - Sessions ====================
app.post('/api/v1/chat/sessions', (req, res) => {
  const now = new Date().toISOString();
  const sessionNum = data.nextSessionId++;
  const newSession = {
    sessionId: `sess_${String(sessionNum).padStart(3, '0')}`,
    userId: req.body.userId || `user_${sessionNum}`,
    userName: req.body.userName || `访客${String(sessionNum).padStart(3, '0')}`,
    phone: req.body.phone || null,
    agentId: null,
    agentName: null,
    status: 'active',
    type: 'ai',
    createdAt: now,
    updatedAt: now,
  };
  data.chatSessions.push(newSession);
  data.chatMessages[newSession.sessionId] = [];
  data.nextMessageId[newSession.sessionId] = 1;
  success(res, newSession);
});

app.post('/api/v1/chat/sessions/:sessionId/messages', (req, res) => {
  const { sessionId } = req.params;
  const { content, role = 'user' } = req.body;

  const session = data.chatSessions.find(s => s.sessionId === sessionId);
  if (!session) return notFound(res, '会话');

  const now = new Date();
  const msgNum = data.nextMessageId[sessionId] || 1;

  // 用户消息
  const userMsg = {
    msgId: `msg_${sessionId}_${String(msgNum).padStart(2, '0')}`,
    sessionId,
    role: 'user',
    content,
    timestamp: now.toISOString(),
  };
  if (!data.chatMessages[sessionId]) data.chatMessages[sessionId] = [];
  data.chatMessages[sessionId].push(userMsg);
  data.nextMessageId[sessionId] = msgNum + 1;
  session.updatedAt = now.toISOString();

  // AI回复
  setTimeout(() => {
    const aiMsgNum = data.nextMessageId[sessionId];
    const aiMsg = {
      msgId: `msg_${sessionId}_${String(aiMsgNum).padStart(2, '0')}`,
      sessionId,
      role: 'ai',
      content: generateAIResponse(content),
      timestamp: new Date().toISOString(),
    };
    data.chatMessages[sessionId].push(aiMsg);
    data.nextMessageId[sessionId] = aiMsgNum + 1;
    session.updatedAt = new Date().toISOString();
  }, 1000);

  success(res, {
    userMessage: userMsg,
    aiMessage: {
      content: generateAIResponse(content),
      timestamp: new Date(Date.now() + 1000).toISOString(),
    },
  });
});

app.get('/api/v1/chat/sessions/:sessionId/history', (req, res) => {
  const { sessionId } = req.params;
  const messages = data.chatMessages[sessionId] || [];
  const session = data.chatSessions.find(s => s.sessionId === sessionId);
  success(res, { session, messages });
});

app.post('/api/v1/chat/sessions/:sessionId/transfer', (req, res) => {
  const { sessionId } = req.params;
  const session = data.chatSessions.find(s => s.sessionId === sessionId);
  if (!session) return notFound(res, '会话');

  // 找一个在线的客服
  const availableAgent = data.agents.find(a => a.status === 'online' && a.type === 'common');
  if (!availableAgent) return error(res, '暂无可用客服');

  session.agentId = availableAgent.agentId;
  session.agentName = availableAgent.name;
  session.type = 'manual';
  session.updatedAt = new Date().toISOString();

  // 添加系统消息
  if (!data.chatMessages[sessionId]) data.chatMessages[sessionId] = [];
  const sysMsgNum = data.nextMessageId[sessionId] || 1;
  data.chatMessages[sessionId].push({
    msgId: `msg_${sessionId}_${String(sysMsgNum).padStart(2, '0')}`,
    sessionId,
    role: 'system',
    content: `已为您转接人工客服 ${availableAgent.name}`,
    timestamp: new Date().toISOString(),
  });
  data.nextMessageId[sessionId] = sysMsgNum + 1;

  // 添加客服问候
  const greetMsgNum = data.nextMessageId[sessionId];
  data.chatMessages[sessionId].push({
    msgId: `msg_${sessionId}_${String(greetMsgNum).padStart(2, '0')}`,
    sessionId,
    role: 'agent',
    content: `您好，我是客服${availableAgent.name}，请问有什么可以帮您的？`,
    contentAgent: availableAgent.name,
    timestamp: new Date().toISOString(),
  });
  data.nextMessageId[sessionId] = greetMsgNum + 1;

  // 更新客服会话
  const csSession = data.csSessions.find(cs => cs.originalSessionId === sessionId);
  if (csSession) {
    csSession.agentId = availableAgent.agentId;
    csSession.agentName = availableAgent.name;
    csSession.status = 'active';
  } else {
    const csNum = data.nextCsSessionId++;
    data.csSessions.push({
      sessionId: `cs_${String(csNum).padStart(3, '0')}`,
      originalSessionId: sessionId,
      userId: session.userId,
      userName: session.userName,
      phone: session.phone,
      agentId: availableAgent.agentId,
      agentName: availableAgent.name,
      status: 'active',
      waitingTime: 0,
      startTime: new Date().toISOString(),
      lastMessageAt: new Date().toISOString(),
    });
  }

  success(res, session);
});

app.get('/api/v1/chat/sessions', (req, res) => {
  const { status, type, page, size } = req.query;
  let sessions = [...data.chatSessions];
  if (status) sessions = sessions.filter(s => s.status === status);
  if (type) sessions = sessions.filter(s => s.type === type);
  sessions.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
  if (page && size) {
    success(res, paginate(sessions, parseInt(page), parseInt(size)));
  } else {
    success(res, sessions);
  }
});

app.get('/api/v1/chat/config', (req, res) => {
  success(res, data.chatConfig);
});

// ==================== Customer Service - Agents ====================
app.get('/api/v1/agents', (req, res) => {
  success(res, data.agents);
});

app.put('/api/v1/agents/:agentId/status', (req, res) => {
  const { agentId } = req.params;
  const { status } = req.body;
  const agent = data.agents.find(a => a.agentId === agentId);
  if (!agent) return notFound(res, '客服');
  agent.status = status;
  agent.lastActiveAt = new Date().toISOString();
  if (status === 'offline') {
    agent.managerOnline = false;
  }
  success(res, agent);
});

// ==================== Customer Service - Sessions ====================
app.get('/api/v1/cs/sessions', (req, res) => {
  const { status, agentId } = req.query;
  let sessions = [...data.csSessions];
  if (status) sessions = sessions.filter(s => s.status === status);
  if (agentId) sessions = sessions.filter(s => s.agentId === agentId);
  sessions.sort((a, b) => new Date(b.lastMessageAt) - new Date(a.lastMessageAt));
  const { page, size } = req.query;
  if (page && size) {
    success(res, paginate(sessions, parseInt(page), parseInt(size)));
  } else {
    success(res, sessions);
  }
});

app.post('/api/v1/cs/sessions/:sessionId/assign', (req, res) => {
  const { sessionId } = req.params;
  const { agentId } = req.body;
  const csSession = data.csSessions.find(s => s.sessionId === sessionId);
  if (!csSession) return notFound(res, '客服会话');
  const agent = data.agents.find(a => a.agentId === agentId);
  if (!agent) return notFound(res, '客服');
  csSession.agentId = agentId;
  csSession.agentName = agent.name;
  success(res, csSession);
});

app.post('/api/v1/cs/sessions/:sessionId/messages', (req, res) => {
  const { sessionId } = req.params;
  const { content, agentId } = req.body;
  const csSession = data.csSessions.find(s => s.sessionId === sessionId);
  if (!csSession) return notFound(res, '客服会话');

  const agent = data.agents.find(a => a.agentId === (agentId || csSession.agentId));
  const agentName = agent ? agent.name : csSession.agentName;
  const now = new Date().toISOString();

  const msg = {
    msgId: `msg_cs_${Date.now()}`,
    sessionId: csSession.originalSessionId,
    role: 'agent',
    content,
    contentAgent: agentName,
    timestamp: now,
  };

  if (!data.chatMessages[csSession.originalSessionId]) {
    data.chatMessages[csSession.originalSessionId] = [];
  }
  data.chatMessages[csSession.originalSessionId].push(msg);
  csSession.lastMessageAt = now;

  success(res, msg);
});

app.put('/api/v1/cs/sessions/:sessionId/close', (req, res) => {
  const { sessionId } = req.params;
  const csSession = data.csSessions.find(s => s.sessionId === sessionId);
  if (!csSession) return notFound(res, '客服会话');
  csSession.status = 'closed';
  csSession.closedAt = new Date().toISOString();
  csSession.lastMessageAt = new Date().toISOString();

  // 更新关联的chat session
  const chatSession = data.chatSessions.find(s => s.sessionId === csSession.originalSessionId);
  if (chatSession) {
    chatSession.status = 'closed';
    chatSession.closedAt = csSession.closedAt;
    chatSession.updatedAt = csSession.closedAt;
  }

  success(res, csSession);
});

// ==================== Member & Satisfaction ====================
app.post('/api/v1/members/lookup', (req, res) => {
  const { phone } = req.body;
  if (!phone) return error(res, '手机号不能为空');
  const member = data.members.find(m => m.phone === phone);
  if (!member) return error(res, '会员不存在', 404);
  success(res, member);
});

app.get('/api/v1/members/vip-route', (req, res) => {
  success(res, [
    { level: '普卡', minSpend: 0, pointMultiplier: 1.0, benefits: ['基础积分累计'] },
    { level: '银卡', minSpend: 50000, pointMultiplier: 1.2, benefits: ['积分1.2倍', '停车9折'] },
    { level: '金卡', minSpend: 200000, pointMultiplier: 1.5, benefits: ['积分1.5倍', '停车8折', '专属客服'] },
    { level: '钻石卡', minSpend: 500000, pointMultiplier: 2.0, benefits: ['积分2倍', '停车7折', '专属经理', '机场接送'] },
  ]);
});

app.post('/api/v1/satisfaction', (req, res) => {
  const now = new Date().toISOString();
  const newRecord = {
    id: data.nextSatisfactionId++,
    sessionId: req.body.sessionId,
    score: req.body.score,
    comment: req.body.comment || '',
    createdAt: now,
  };
  data.satisfactionRecords.push(newRecord);
  success(res, newRecord);
});

app.get('/api/v1/satisfaction/stats', (req, res) => {
  const records = data.satisfactionRecords;
  const total = records.length;
  if (total === 0) return success(res, { avgScore: 0, total: 0 });
  const avgScore = (records.reduce((sum, r) => sum + r.score, 0) / total).toFixed(1);
  const distribution = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
  records.forEach(r => distribution[r.score]++);
  success(res, { avgScore: parseFloat(avgScore), total, distribution });
});

// ==================== Reports ====================
app.get('/api/v1/reports/dashboard', (req, res) => {
  success(res, data.dashboardStats);
});

app.get('/api/v1/reports/sessions', (req, res) => {
  const { page, size, type, status } = req.query;
  let reports = [...data.sessionReports];
  if (type) reports = reports.filter(r => r.type === type);
  if (status) reports = reports.filter(r => r.status === status);
  reports.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
  const pageNum = page ? parseInt(page) : 1;
  const pageSize = size ? parseInt(size) : 10;
  success(res, paginate(reports, pageNum, pageSize));
});

app.get('/api/v1/reports/hot-questions', (req, res) => {
  const { limit } = req.query;
  let questions = [...data.hotQuestions].sort((a, b) => b.count - a.count);
  if (limit) questions = questions.slice(0, parseInt(limit));
  success(res, questions);
});

app.get('/api/v1/reports/export', (req, res) => {
  // 模拟导出为JSON
  success(res, {
    exportDate: new Date().toISOString(),
    totalSessions: data.dashboardStats.totalSessions,
    hotQuestions: data.hotQuestions.slice(0, 10),
    satisfactionStats: data.satisfactionRecords.reduce((acc, r) => acc + r.score, 0) / data.satisfactionRecords.length,
  });
});

// ==================== System - Roles ====================
app.get('/api/v1/system/roles', (req, res) => {
  success(res, data.roles);
});

app.post('/api/v1/system/roles', (req, res) => {
  const now = new Date().toISOString();
  const newRole = {
    id: data.nextRoleId++,
    name: req.body.name,
    code: req.body.code,
    description: req.body.description || '',
    permissions: req.body.permissions || [],
    createdAt: now,
    updatedAt: now,
  };
  data.roles.push(newRole);
  success(res, newRole);
});

app.put('/api/v1/system/roles/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const role = data.roles.find(r => r.id === id);
  if (!role) return notFound(res, '角色');
  Object.assign(role, req.body, { updatedAt: new Date().toISOString() });
  success(res, role);
});

app.delete('/api/v1/system/roles/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const idx = data.roles.findIndex(r => r.id === id);
  if (idx === -1) return notFound(res, '角色');
  data.roles.splice(idx, 1);
  success(res, null);
});

// ==================== System - Logs ====================
app.get('/api/v1/system/logs', (req, res) => {
  const { page, size, module, action } = req.query;
  let logs = [...data.operationLogs].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  if (module) logs = logs.filter(l => l.module === module);
  if (action) logs = logs.filter(l => l.action === action);
  const pageNum = page ? parseInt(page) : 1;
  const pageSize = size ? parseInt(size) : 10;
  success(res, paginate(logs, pageNum, pageSize));
});

// ==================== System - Sensitive Words ====================
app.get('/api/v1/system/sensitive-words', (req, res) => {
  success(res, data.sensitiveWords);
});

app.post('/api/v1/system/sensitive-words', (req, res) => {
  const now = new Date().toISOString();
  const newWord = {
    id: data.nextSensitiveWordId++,
    word: req.body.word,
    reason: req.body.reason || '',
    createdAt: now,
  };
  data.sensitiveWords.push(newWord);
  success(res, newWord);
});

app.delete('/api/v1/system/sensitive-words/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const idx = data.sensitiveWords.findIndex(w => w.id === id);
  if (idx === -1) return notFound(res, '敏感词');
  data.sensitiveWords.splice(idx, 1);
  success(res, null);
});

// ==================== V2 APIs - VIP Exclusive Routing ====================
app.post('/api/v1/vip/route', (req, res) => {
  const { userId, query } = req.body;
  if (!userId) return error(res, 'userId不能为空');

  // Check if user has a dedicated agent
  const member = data.members.find(m => m.phone === userId || m.id === userId || `user_${m.id}` === userId);
  const hasDedicatedAgent = member && member.dedicatedAgentId;

  if (!hasDedicatedAgent) {
    // Not VIP - route to AI bot
    return success(res, {
      status: 'ai_bot',
      message: 'AI客服为您服务',
    });
  }

  // VIP user - find dedicated agent
  const agent = data.agents.find(a => a.agentId === member.dedicatedAgentId);
  if (!agent) return error(res, '专属客服不存在');

  // Check if agent is free
  if (agent.status === 'online') {
    // Agent is free - assign immediately
    return success(res, {
      agentId: agent.agentId,
      agentName: agent.name,
      agentPhone: member.dedicatedAgentPhone || '13800000001',
      status: 'assigned',
      queuePosition: 0,
    });
  } else {
    // Agent is busy - queue with wait info
    const queuePosition = agent.currentSessions + 1;
    const estimatedWait = queuePosition * 120; // 2 minutes per task
    return success(res, {
      agentId: agent.agentId,
      agentName: agent.name,
      agentPhone: member.dedicatedAgentPhone || '13800000001',
      status: 'waiting',
      queuePosition,
      estimatedWait,
      options: ['wait', 'call'],
    });
  }
});

// ==================== V2 APIs - Multimodal Data Endpoints ====================
app.get('/api/v2/data/merchants', (req, res) => {
  const { category, floor, keyword } = req.query;
  let merchants = [...data.merchants];
  if (category) merchants = merchants.filter(m => m.category === category);
  if (floor) merchants = merchants.filter(m => m.floor.includes(floor));
  if (keyword) {
    const kw = keyword.toLowerCase();
    merchants = merchants.filter(m =>
      m.name.toLowerCase().includes(kw) || m.description.toLowerCase().includes(kw)
    );
  }
  success(res, merchants);
});

app.get('/api/v2/data/products', (req, res) => {
  const { merchantId, keyword } = req.query;
  let products = [...data.products];
  if (merchantId) products = products.filter(p => p.merchantId === merchantId);
  if (keyword) {
    const kw = keyword.toLowerCase();
    products = products.filter(p =>
      p.name.toLowerCase().includes(kw) || p.description.toLowerCase().includes(kw)
    );
  }
  success(res, products);
});

app.get('/api/v2/data/events', (req, res) => {
  const now = new Date().toISOString().split('T')[0];
  let events = [...data.events];
  const { active } = req.query;
  if (active === 'true') {
    events = events.filter(e => e.startDate <= now && e.endDate >= now);
  }
  success(res, events);
});

app.get('/api/v2/data/coupons', (req, res) => {
  let coupons = [...data.coupons];
  success(res, coupons);
});

app.get('/api/v2/data/navigation', (req, res) => {
  let nav = [...data.navigation];
  success(res, nav);
});

// ==================== V2 APIs - Chat Messages with Multimodal Response ====================
app.post('/api/v2/chat/sessions/:sessionId/messages', (req, res) => {
  const { sessionId } = req.params;
  const { content, role = 'user' } = req.body;

  const session = data.chatSessions.find(s => s.sessionId === sessionId);
  if (!session) return notFound(res, '会话');

  const now = new Date();
  const msgNum = data.nextMessageId[sessionId] || 1;

  // User message
  const userMsg = {
    msgId: `msg_${sessionId}_${String(msgNum).padStart(2, '0')}`,
    sessionId,
    role: 'user',
    content,
    timestamp: now.toISOString(),
  };
  if (!data.chatMessages[sessionId]) data.chatMessages[sessionId] = [];
  data.chatMessages[sessionId].push(userMsg);
  data.nextMessageId[sessionId] = msgNum + 1;
  session.updatedAt = now.toISOString();

  // Generate multimodal AI response
  const aiResponse = generateMultimodalResponse(content);

  // Schedule AI response
  setTimeout(() => {
    const aiMsgNum = data.nextMessageId[sessionId];
    const aiMsg = {
      msgId: `msg_${sessionId}_${String(aiMsgNum).padStart(2, '0')}`,
      sessionId,
      role: 'ai',
      content: aiResponse.text,
      multimodal: aiResponse.multimodal,
      timestamp: new Date().toISOString(),
    };
    data.chatMessages[sessionId].push(aiMsg);
    data.nextMessageId[sessionId] = aiMsgNum + 1;
    session.updatedAt = new Date().toISOString();
  }, 1000);

  success(res, {
    userMessage: userMsg,
    aiMessage: {
      text: aiResponse.text,
      multimodal: aiResponse.multimodal,
      type: 'ai_response',
      timestamp: new Date(Date.now() + 1000).toISOString(),
    },
  });
});

function generateMultimodalResponse(query) {
  const baseResponse = generateAIResponse(query);

  // Build multimodal content based on query
  const multimodal = {
    navigation: null,
    merchants: [],
    products: [],
    coupons: [],
    events: [],
  };

  // Navigation
  if (query.includes('导航') || query.includes('怎么走') || query.includes('位置') || query.includes('在哪里')) {
    multimodal.navigation = {
      title: '导航到杭州大厦',
      url: 'hzmall://nav/destination',
    };
  }

  // Merchants
  if (query.includes('品牌') || query.includes('LV') || query.includes('GUCCI') ||
      query.includes('店铺') || query.includes('在哪') || query.includes('楼层')) {
    const merchantMap = {
      'LV': ['m001'],
      'GUCCI': ['m002'],
      'CHANEL': ['m003'],
      'HERMES': ['m004'],
      '星巴克': ['m005'],
      '海底捞': ['m006'],
      '苹果': ['m008'],
      'Apple': ['m008'],
      'DIOR': ['m010'],
    };
    for (const [keyword, ids] of Object.entries(merchantMap)) {
      if (query.includes(keyword)) {
        ids.forEach(id => {
          const m = data.merchants.find(merch => merch.id === id);
          if (m) multimodal.merchants.push(m);
        });
      }
    }
    if (multimodal.merchants.length === 0 && (query.includes('品牌') || query.includes('店铺'))) {
      multimodal.merchants = data.merchants.slice(0, 3);
    }
  }

  // Products
  if (query.includes('新品') || query.includes('推荐') || query.includes('商品') || query.includes('产品')) {
    multimodal.products = data.products.slice(0, 3);
  }

  // Coupons
  if (query.includes('优惠') || query.includes('券') || query.includes('满减') || query.includes('折扣') || query.includes('促销')) {
    multimodal.coupons = data.coupons.slice(0, 3);
  }

  // Events
  if (query.includes('活动') || query.includes('促销') || query.includes('节日') || query.includes('狂欢')) {
    multimodal.events = data.events.slice(0, 2);
  }

  return {
    text: baseResponse,
    multimodal,
  };
}

// ==================== V2 APIs - Transfer (Regular User: Phone + Address Only) ====================
app.post('/api/v2/chat/sessions/:sessionId/transfer', (req, res) => {
  const { sessionId } = req.params;
  const session = data.chatSessions.find(s => s.sessionId === sessionId);
  if (!session) return notFound(res, '会话');

  // Check if user is VIP
  const member = data.members.find(m =>
    m.phone === session.phone || m.id === session.userId || `user_${m.id}` === session.userId
  );
  const isVip = member && (member.level === '钻石卡' || member.level === '金卡');

  if (!isVip) {
    // Regular user: phone + address guide ONLY
    return success(res, {
      transferType: 'phone_guide',
      phone: data.chatConfig.customerServicePhone || '0571-85158888',
      serviceDeskAddress: data.chatConfig.serviceDeskAddress || '杭州大厦A座一楼服务台',
      message: '请拨打客服电话或前往服务台咨询',
    });
  }

  // VIP user: route to dedicated agent
  if (member && member.dedicatedAgentId) {
    const agent = data.agents.find(a => a.agentId === member.dedicatedAgentId);
    if (agent && agent.status === 'online') {
      // Create VIP task
      const taskNum = data.nextVipTaskId++;
      const task = {
        taskId: `vip_task_${String(taskNum).padStart(3, '0')}`,
        userId: session.userId,
        userName: session.userName,
        userPhone: session.phone,
        memberLevel: member.level,
        dedicatedAgentId: member.dedicatedAgentId,
        dedicatedAgentName: member.dedicatedAgentName,
        query: '请求人工客服',
        status: 'pending',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      data.vipTasks.push(task);

      return success(res, {
        transferType: 'dedicated_agent',
        agentId: agent.agentId,
        agentName: agent.name,
        agentPhone: member.dedicatedAgentPhone,
        status: 'assigned',
        queuePosition: 0,
      });
    } else {
      return success(res, {
        transferType: 'dedicated_agent',
        agentId: member.dedicatedAgentId,
        agentName: member.dedicatedAgentName,
        agentPhone: member.dedicatedAgentPhone,
        status: 'waiting',
        queuePosition: (agent ? agent.currentSessions : 0) + 1,
        estimatedWait: 300,
        options: ['wait', 'call'],
      });
    }
  }

  // Fallback for VIP without dedicated agent
  return success(res, {
    transferType: 'phone_guide',
    phone: data.chatConfig.customerServicePhone || '0571-85158888',
    serviceDeskAddress: data.chatConfig.serviceDeskAddress || '杭州大厦A座一楼服务台',
    message: '请拨打客服电话或前往服务台咨询',
  });
});

// ==================== V2 APIs - VIP Agent Workbench ====================
// List active VIP tasks for agent
app.get('/api/v1/agent/vip/:agentId/tasks', (req, res) => {
  const { agentId } = req.params;
  const { status } = req.query;
  let tasks = data.vipTasks.filter(t => t.dedicatedAgentId === agentId);
  if (status) tasks = tasks.filter(t => t.status === status);
  tasks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  success(res, tasks);
});

// Accept a VIP task
app.post('/api/v1/agent/vip/:agentId/tasks/:taskId/accept', (req, res) => {
  const { agentId, taskId } = req.params;
  const task = data.vipTasks.find(t => t.taskId === taskId);
  if (!task) return notFound(res, 'VIP任务');
  if (task.dedicatedAgentId !== agentId) return error(res, '无权限操作此任务');

  task.status = 'accepted';
  task.acceptedAt = new Date().toISOString();
  task.updatedAt = new Date().toISOString();

  // Update agent reception tasks count
  const agent = data.agents.find(a => a.agentId === agentId);
  if (agent) {
    agent.receptionTasks = (agent.receptionTasks || 0) + 1;
  }

  success(res, task);
});

// Send response to VIP
app.post('/api/v1/agent/vip/:agentId/tasks/:taskId/respond', (req, res) => {
  const { agentId, taskId } = req.params;
  const { text, multimodal } = req.body;
  const task = data.vipTasks.find(t => t.taskId === taskId);
  if (!task) return notFound(res, 'VIP任务');
  if (task.dedicatedAgentId !== agentId) return error(res, '无权限操作此任务');

  task.status = 'responded';
  task.lastResponse = { text, multimodal, timestamp: new Date().toISOString() };
  task.updatedAt = new Date().toISOString();

  success(res, {
    taskId,
    status: 'sent',
    response: { text, multimodal },
    timestamp: new Date().toISOString(),
  });
});

// Get push notifications for agent
app.get('/api/v1/agent/vip/:agentId/notifications', (req, res) => {
  const { agentId } = req.params;
  const { unread } = req.query;
  let notifications = data.agentNotifications.filter(n => n.agentId === agentId);
  if (unread === 'true') notifications = notifications.filter(n => !n.read);
  notifications.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  success(res, notifications);
});

// Mark notification as read
app.put('/api/v1/agent/vip/:agentId/notifications/:notifId/read', (req, res) => {
  const { agentId, notifId } = req.params;
  const notif = data.agentNotifications.find(n => n.id === notifId);
  if (!notif) return notFound(res, '通知');
  if (notif.agentId !== agentId) return error(res, '无权限操作此通知');

  notif.read = true;
  success(res, notif);
});

// ==================== Demo ====================
app.post('/api/v1/demo/reset', (req, res) => {
  resetData(data);
  success(res, { message: '数据已重置为初始状态' });
});

app.get('/api/v1/demo/health', (req, res) => {
  success(res, {
    status: 'UP',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    dataStats: {
      categories: data.categories.length,
      knowledgeFiles: data.knowledgeFiles.length,
      knowledgeItems: data.knowledgeItems.length,
      agents: data.agents.length,
      chatSessions: data.chatSessions.length,
      members: data.members.length,
      merchants: data.merchants.length,
      products: data.products.length,
      events: data.events.length,
      coupons: data.coupons.length,
      vipTasks: data.vipTasks.length,
    },
  });
});

// ==================== 启动服务器 ====================
app.listen(PORT, '0.0.0.0', () => {
  console.log(`
╔══════════════════════════════════════════════════════╗
║                                                      ║
║   杭州大厦AI客服系统 - Mock Server                    ║
║   运行地址: http://localhost:${PORT}                     ║
║   API端点: http://localhost:${PORT}/api/v1             ║
║   健康检查: http://localhost:${PORT}/api/v1/demo/health║
║   数据重置: POST http://localhost:${PORT}/api/v1/demo/reset  ║
║                                                      ║
╚══════════════════════════════════════════════════════╝
  `);
});
