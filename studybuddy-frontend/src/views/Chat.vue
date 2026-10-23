<template>
  <div class="chat-page">
    <el-card class="tip-card mb-16">
      <div class="tip-head">
        <div class="tip">
          <b>学习搭子 · 智能问答（实验五）</b>
          <span>
            基于课程知识库的 RAG 检索增强问答；支持多轮上下文追问；模型未配置时自动降级为知识库直答。
          </span>
        </div>
        <div class="tip-switch">
          <el-switch v-model="useRag" active-text="启用 RAG 检索增强" />
        </div>
      </div>
    </el-card>

    <div class="chat-body">

      <el-card class="session-card">
        <div class="session-head">
          <span class="session-title">会话列表</span>
          <el-button size="small" type="primary" @click="newSession">新建</el-button>
        </div>
        <div v-loading="sessionLoading" class="session-list">
          <div
            v-for="s in sessions"
            :key="s.id"
            :class="['session-item', { active: s.id === currentSessionId }]"
            @click="switchSession(s.id)"
          >
            <div class="session-name">{{ s.title }}</div>
            <div class="session-meta">
              <span>{{ s.messageCount }} 条</span>
              <el-button link type="danger" size="small" @click.stop="removeSession(s)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="!sessions.length" description="暂无会话" :image-size="50" />
        </div>
      </el-card>

      <el-card class="chat-card">
        <div ref="listRef" class="msg-list">
          <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
            <div class="avatar">{{ m.role === 'user' ? '我' : 'AI' }}</div>
            <div class="bubble">
              <div class="content">{{ m.content }}</div>
              <div v-if="m.references && m.references.length" class="refs">
                <div class="refs-title">检索到的资料出处：</div>
                <div v-for="(r, j) in m.references" :key="j" class="ref-item">{{ r }}</div>
              </div>
              <div v-if="m.fallback" class="fallback-tag">（模型未配置或不可用，本条为知识库直答降级结果）</div>
            </div>
          </div>
        </div>

        <div class="context-hint">
          当前会话：{{ currentSessionId ? `#${currentSessionId}` : '（新会话，发送后自动创建）' }}
          <span v-if="lastContextSize > 0">· 本次回灌历史 {{ lastContextSize }} 条</span>
        </div>

        <div class="input-bar">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="例如：密码能不能明文存库？前后端分离跨域怎么解决？（Enter 发送，Shift+Enter 换行）"
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" :loading="loading" class="send-btn" @click="send">发送</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi } from '../api/ai'

const question = ref('')
const loading = ref(false)
const useRag = ref(true)
const listRef = ref()

const sessions = ref([])
const sessionLoading = ref(false)
const currentSessionId = ref(null)
const lastContextSize = ref(0)

const welcome = {
  role: 'assistant',
  content: '你好，我是学习搭子。可以问我《团队规范项目实训》相关的技术问题，我会优先依据课程知识库回答并给出出处。'
}

const messages = ref([{ ...welcome }])

const scrollToBottom = async () => {
  await nextTick()
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight
  }
}

const loadSessions = async () => {
  sessionLoading.value = true
  try {
    const res = await aiApi.sessions()
    sessions.value = res.data
  } catch (e) {

  } finally {
    sessionLoading.value = false
  }
}

const switchSession = async (id) => {
  if (id === currentSessionId.value) {
    return
  }
  try {
    const res = await aiApi.messages(id)
    currentSessionId.value = id
    messages.value = res.data.map((m) => ({
      role: m.role,
      content: m.content,
      references: m.references,
      fallback: m.fallback
    }))
    await scrollToBottom()
  } catch (e) {

  }
}

const newSession = () => {
  currentSessionId.value = null
  lastContextSize.value = 0
  messages.value = [{ ...welcome }]
}

const removeSession = async (s) => {
  await ElMessageBox.confirm(`确认删除会话「${s.title}」及其全部消息吗？`, '提示', { type: 'warning' })
  await aiApi.removeSession(s.id)
  if (s.id === currentSessionId.value) {
    newSession()
  }
  ElMessage.success('会话已删除')
  loadSessions()
}

const send = async () => {
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  await scrollToBottom()

  loading.value = true
  try {
    const res = await aiApi.chat({
      question: q,
      useRag: useRag.value,

      sessionId: currentSessionId.value ? String(currentSessionId.value) : null
    })
    lastContextSize.value = res.data.contextSize || 0
    if (res.data.sessionId && res.data.sessionId !== currentSessionId.value) {
      currentSessionId.value = res.data.sessionId
      loadSessions()
    }
    messages.value.push({
      role: 'assistant',
      content: res.data.answer,
      references: res.data.references,
      fallback: res.data.fallback
    })
  } catch (e) {

    messages.value.push({ role: 'assistant', content: '（本次提问未通过安全校验或被服务拒绝，请调整后重试。）' })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

onMounted(loadSessions)
</script>

<style scoped>
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tip-card .tip-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.tip-card .tip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #606266;
  font-size: 13px;
}

.tip-switch {
  flex-shrink: 0;
}

.chat-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.session-card {
  width: 230px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.session-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.session-title {
  font-weight: 600;
  font-size: 14px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  max-height: 460px;
}

.session-item {
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 6px;
  border: 1px solid transparent;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.session-name {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
  min-height: 320px;
}

.msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
}

.msg.user .avatar {
  background: #67c23a;
}

.bubble {
  max-width: 76%;
  background: #f4f4f5;
  border-radius: 8px;
  padding: 10px 14px;
  white-space: pre-wrap;
  line-height: 1.7;
}

.msg.user .bubble {
  background: #ecf5ff;
}

.refs {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
  font-size: 12px;
  color: #909399;
}

.refs-title {
  margin-bottom: 4px;
}

.ref-item {
  margin-bottom: 2px;
}

.fallback-tag {
  margin-top: 8px;
  font-size: 12px;
  color: #e6a23c;
}

.context-hint {
  font-size: 12px;
  color: #909399;
  padding: 6px 0;
  border-top: 1px solid #ebeef5;
}

.input-bar {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding-top: 8px;
}

.send-btn {
  height: 54px;
  width: 88px;
}
</style>
