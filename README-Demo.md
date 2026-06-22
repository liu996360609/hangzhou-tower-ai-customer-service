# 杭州大厦AI客服系统 - 产品Demo (V2)

> 完整呈现产品功能、用户链路、百炼API对接方案的交互式Demo
> **版本**: V2.0 — 普通用户/VIP用户双通道分流、专属坐席小程序工作台、多模态推荐引擎

## 📁 项目结构

```
杭州大厦/
├── demo-backend/          # Spring Boot 后端骨架（生产版本）
├── demo-frontend/         # Vue 3 + Element Plus 前端（V2已更新）
├── mock-server/           # Node.js Mock Server（演示用，72+ API，V2已更新）
├── 杭州大厦AI客服系统_产品需求文档.md  # 最终版产品需求文档（V2合并）
└── 杭州大厦AI客服系统_技术方案.md      # 最终版技术方案文档（V2合并）
```

## 📋 V2核心变更

| 变更点 | V1 | V2 |
|--------|----|----|
| **普通用户转人工** | 在线聊天转接人工坐席 | 仅提供**电话客服号码 + 服务台地址**，不提供在线转接 |
| **VIP用户专属服务** | 转接专属会员经理（在线聊天） | 首次提问即识别身份，**专属坐席独占服务**，忙线时排队+电话双选项 |
| **VIP排队管理** | 基础排队 | **排队位置展示 + 预计等待时间 + 双选项（等待/电话直拨）** |
| **专属坐席小程序** | PC端客服工作台 | **会员经理小程序端工作台**，推送通知、多模态内容推送 |
| **多模态推荐** | 无 | AI回复包含**商户/商品/优惠券/活动推荐卡片 + 导航** |
| **知识库方案** | 完整方案 | **保持不变** |

## 🚀 快速启动

### 方式一：Mock Server + 前端（推荐，无需Java环境）

```bash
# 终端1：启动Mock Server（模拟72+后端API）
cd mock-server
npm install
node server.js
# Mock Server 运行在 http://localhost:8080

# 终端2：启动前端
cd demo-frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

### 方式二：Spring Boot 后端 + 前端（需要Java 17 + Maven）

```bash
# 终端1：启动Spring Boot后端
cd demo-backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080

# 终端2：启动前端
cd demo-frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

## 📋 Demo覆盖模块

| 模块 | 页面 | 功能 |
|------|------|------|
| **首页仪表盘** | `/` | 总会话数、AI准确率、转人工率、满意度、热门问题趋势 |
| **知识库管理** | `/knowledge` | 分类管理、文件管理（上传/解析/标签）、知识列表、双人审核、检索测试、补丁问答 |
| **AI对话** | `/chat` | 智能对话、多轮上下文、**多模态推荐卡片（商户/商品/优惠券/活动/导航）** |
| **在线客服** | `/cs` | 坐席状态、**VIP专属路由**、客服工作台、满意度评价 |
| **专属坐席工作台** | `/agent` | **V2新增：VIP任务管理、多模态内容选择器、推送通知** |
| **数据报表** | `/reports` | 会话统计、热门问题、满意度分析、数据导出 |
| **系统设置** | `/settings` | 角色权限、敏感词管理、操作日志 |

## 🔄 完整用户链路演示

### 链路1：用户进线 → AI自动回答 → 问题解决

```
1. 打开 "AI对话" 页面
2. 点击 "新建对话"，输入手机号 13800138000（VIP钻石卡用户）
3. 系统自动识别会员身份，展示会员信息
4. 输入问题："请问今天营业时间是什么？"
5. AI通过RAG检索知识库，返回回答 + 溯源文档
6. 用户继续追问："那春节期间呢？"
7. AI基于上下文继续回答
8. 会话结束，展示满意度评价
```

### 链路2：用户进线 → AI无法回答 → 转人工 → 会员专属路由

```
1. 打开 "AI对话" 页面
2. 输入问题（知识库中不存在的内容）
3. AI回复表示无法确定，建议转人工
4. 点击 "转人工" 按钮
5. 系统识别为VIP钻石卡用户 → 路由至专属会员经理（张三）
6. 经理在线 → 直接接入人工会话
7. 人工客服看到AI前序对话摘要
8. 客服回复用户问题
9. 结束会话，发送满意度评价
```

### 链路3：知识管理员 → 上传文档 → 双人审核 → 发布上线

```
1. 打开 "知识库管理" 页面
2. 进入 "分类管理" Tab，查看已有分类
3. 进入 "文件管理" Tab，点击 "上传文件"
4. 选择文件后模拟上传（调用百炼 ApplyFileUploadLease → AddFile）
5. 文件解析状态变为 PARSE_SUCCESS
6. 进入 "知识列表" Tab，找到新建的知识条目
7. 点击 "提交审核"
8. 切换到 "审核管理" Tab
9. 审核人A点击 "通过"
10. 审核人B点击 "通过"
11. 知识条目状态变为 "已发布"，可被AI检索使用
```

### 链路4：检索测试 → 发现知识盲区 → 补充知识

```
1. 打开 "知识库管理" → "检索测试" Tab
2. 输入问题："杭州大厦有什么促销活动？"
3. 系统展示检索结果（召回文档 + 分数 + AI回答）
4. 如果分数低或AI回答不满意：
   a. 点击 "添加到知识库"
   b. 填写答案内容
   c. 提交审核流程
```

### 链路5：客服工作台 → 接待用户 → AI辅助回复

```
1. 打开 "在线客服" 页面
2. 设置坐席状态为 "在线"
3. 等待队列中出现新会话
4. 点击 "接入" 分配会话
5. 工作台展示：
   - 用户信息面板（会员卡级、专属经理）
   - AI前序对话摘要
   - AI推荐回复建议
6. 客服可一键发送推荐回复或自定义回复
7. 问题解决后点击 "结束会话"
8. 系统自动发送满意度评价
```

## 🔌 API 接口总览（72+ 端点）

### 知识库管理（26个）

| 方法 | 路径 | 说明 | 对应百炼API |
|------|------|------|-------------|
| GET | `/api/v1/knowledge/categories` | 分类列表 | ListCategory |
| POST | `/api/v1/knowledge/categories` | 新增分类 | AddCategory |
| PUT | `/api/v1/knowledge/categories/:id` | 更新分类 | - |
| DELETE | `/api/v1/knowledge/categories/:id` | 删除分类 | DeleteCategory |
| GET | `/api/v1/knowledge/files` | 文件列表 | ListFile |
| POST | `/api/v1/knowledge/files/upload` | 模拟上传 | ApplyFileUploadLease → AddFile |
| POST | `/api/v1/knowledge/files/:id/parse` | 触发解析 | DescribeFile |
| GET | `/api/v1/knowledge/files/:id` | 文件详情 | DescribeFile |
| DELETE | `/api/v1/knowledge/files/:id` | 删除文件 | DeleteFile |
| PUT | `/api/v1/knowledge/files/:id/tags` | 更新标签 | UpdateFileTag |
| GET | `/api/v1/knowledge/parse-settings/:catId` | 解析设置 | GetParseSettings |
| PUT | `/api/v1/knowledge/parse-settings/:catId` | 修改解析 | ChangeParseSetting |
| GET | `/api/v1/knowledge/items` | 知识列表 | ListIndexDocuments |
| GET | `/api/v1/knowledge/items/:id` | 知识详情 | ListIndexFileDetails |
| POST | `/api/v1/knowledge/items` | 新增知识 | SubmitIndexAddDocumentsJob |
| PUT | `/api/v1/knowledge/items/:id` | 更新知识 | UpdateChunk |
| DELETE | `/api/v1/knowledge/items/:id` | 删除知识 | DeleteIndexDocument |
| POST | `/api/v1/knowledge/items/:id/submit-review` | 提交审核 | - |
| PUT | `/api/v1/knowledge/items/:id/review` | 审核操作 | - |
| GET | `/api/v1/knowledge/reviews` | 审核列表 | - |
| POST | `/api/v1/knowledge/retrieve` | 检索测试 | Retrieve |
| GET | `/api/v1/knowledge/patches` | 补丁问答列表 | - |
| POST | `/api/v1/knowledge/patches` | 新增补丁 | - |
| PUT | `/api/v1/knowledge/patches/:id` | 更新补丁 | - |
| DELETE | `/api/v1/knowledge/patches/:id` | 删除补丁 | - |
| GET | `/api/v1/knowledge/versions/:itemId` | 版本列表 | - |
| GET | `/api/v1/knowledge/stats` | 知识统计 | GetIndexMonitor |

### AI对话（6个）

| 方法 | 路径 | 说明 | 对应百炼API |
|------|------|------|-------------|
| POST | `/api/v1/chat/sessions` | 创建会话 | - |
| POST | `/api/v1/chat/sessions/:id/messages` | 发送消息 | Chat Completions |
| GET | `/api/v1/chat/sessions/:id/history` | 历史记录 | - |
| POST | `/api/v1/chat/sessions/:id/transfer` | 转人工 | - |
| GET | `/api/v1/chat/sessions` | 会话列表 | - |
| GET | `/api/v1/chat/config` | 聊天配置 | - |

### 在线客服（6个）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/agents` | 坐席列表 |
| PUT | `/api/v1/agents/:agentId/status` | 更新状态 |
| GET | `/api/v1/cs/sessions` | 客服会话 |
| POST | `/api/v1/cs/sessions/:id/assign` | 分配会话 |
| POST | `/api/v1/cs/sessions/:id/messages` | 发送消息 |
| PUT | `/api/v1/cs/sessions/:id/close` | 结束会话 |

### 会员与满意度（4个）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/members/lookup` | 查询会员 |
| GET | `/api/v1/members/vip-route` | VIP路由决策 |
| POST | `/api/v1/satisfaction` | 提交评价 |
| GET | `/api/v1/satisfaction/stats` | 满意度统计 |

### 数据报表（4个）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/reports/dashboard` | 仪表盘数据 |
| GET | `/api/v1/reports/sessions` | 会话统计 |
| GET | `/api/v1/reports/hot-questions` | 热门问题 |
| GET | `/api/v1/reports/export` | 导出报表 |

### 系统管理（8个）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/system/roles` | 角色列表 |
| POST | `/api/v1/system/roles` | 新增角色 |
| PUT | `/api/v1/system/roles/:id` | 更新角色 |
| DELETE | `/api/v1/system/roles/:id` | 删除角色 |
| GET | `/api/v1/system/logs` | 操作日志 |
| GET | `/api/v1/system/sensitive-words` | 敏感词列表 |
| POST | `/api/v1/system/sensitive-words` | 新增敏感词 |
| DELETE | `/api/v1/system/sensitive-words/:id` | 删除敏感词 |

### Demo工具（2个）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/demo/health` | 健康检查 |
| POST | `/api/v1/demo/reset` | 重置所有数据 |

## 🎨 前端页面截图说明

### 深色侧边栏导航
- 首页仪表盘（统计卡片 + 趋势图）
- 知识库管理（6个子Tab）
- AI对话（微信风格聊天界面）
- 在线客服（坐席面板 + 工作台）
- 数据报表（统计图表）
- 系统设置（权限/敏感词/日志）

### 交互特性
- 所有表单可填写并即时看到效果
- 状态标签颜色区分（绿色=成功/红色=失败/橙色=进行中）
- 操作成功/失败有Toast提示
- 分页、筛选、搜索功能完整
- 响应式布局适配桌面端

## 🔧 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Vue Router + Axios |
| 后端（生产） | Java 17 + Spring Boot 3 + Spring Cloud Alibaba |
| Mock Server | Node.js + Express + CORS + body-parser |
| AI平台 | 阿里云百炼（Qwen 3 + RAG知识库） |
| 数据库（生产） | MySQL 8.0 + Redis |

## 📌 百炼API对接点标注

Demo中标注了所有与百炼API的对接位置：
- `mock-server/server.js` 中的注释标注了每个API对应的百炼API
- `demo-backend` 中Service类的注释标注了百炼API调用点
- 前端API调用路径与百炼API一一对应

## 💡 使用建议

1. **演示前**：先访问 `/api/v1/demo/reset` 重置数据到初始状态
2. **演示时**：按照用户链路顺序展示，确保每个环节都能正常交互
3. **演示后**：查看Mock Server控制台日志，可看到每个API的调用记录
4. **对接生产**：将 `demo-backend` 部署到生产环境，替换Mock Server即可

## 📖 相关文档

- [杭州大厦AI客服系统_产品需求文档.md](./杭州大厦AI客服系统_产品需求文档.md) — **最终版PRD文档（V2合并）**
- [杭州大厦AI客服系统_技术方案.md](./杭州大厦AI客服系统_技术方案.md) — **最终版技术方案（V2合并）**
- [杭州大厦AI客服招标文件2.docx](./杭州大厦AI客服招标文件2.docx)
- [杭州大厦&脸脸沟通会-元宝纪要.txt](./杭州大厦&脸脸沟通会-元宝纪要.txt)
