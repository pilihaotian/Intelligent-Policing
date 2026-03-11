<template>
  <div class="ai-chat">
    <a-row :gutter="16" style="height: 100%">
      <!-- 左侧会话列表 -->
      <a-col :span="5" style="height: 100%">
        <a-card style="height: 100%; overflow: hidden; display: flex; flex-direction: column">
          <template #title>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>对话历史</span>
              <a-button type="primary" size="small" @click="handleNewSession">
                <template #icon><PlusOutlined /></template>
                新对话
              </a-button>
            </div>
          </template>
          
          <div style="overflow-y: auto; flex: 1">
            <a-list :dataSource="sessions" size="small">
              <template #renderItem="{ item }">
                <a-list-item 
                  :class="['session-item', { active: currentSession?.sessionId === item.sessionId }]"
                  @click="selectSession(item)"
                >
                  <a-list-item-meta>
                    <template #title>
                      <span class="session-title">{{ item.title }}</span>
                    </template>
                    <template #description>
                      <span style="font-size: 12px; color: #999">
                        {{ formatTime(item.updatedTime) }}
                      </span>
                    </template>
                  </a-list-item-meta>
                  <template #actions>
                    <a-popconfirm title="确定删除此对话？" @confirm="handleDeleteSession(item.sessionId)">
                      <DeleteOutlined class="delete-icon" @click.stop />
                    </a-popconfirm>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </div>
          
          <!-- 知识库选择 -->
          <div style="margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0">
            <div style="margin-bottom: 8px; font-weight: 500">知识库</div>
            <a-select 
              v-model:value="selectedKbId" 
              style="width: 100%"
              placeholder="选择知识库"
              allowClear
              @change="handleKbChange"
            >
              <a-select-option v-for="kb in kbList" :key="kb.id" :value="kb.id">
                {{ kb.kbName }}
              </a-select-option>
            </a-select>
          </div>
        </a-card>
      </a-col>
      
      <!-- 右侧聊天区域 -->
      <a-col :span="19" style="height: 100%">
        <a-card style="height: 100%; display: flex; flex-direction: column">
          <template #title>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>
                <span v-if="currentSession">{{ currentSession.title }}</span>
                <span v-else>智能问答助手</span>
                <a-tag v-if="currentKb" color="blue" style="margin-left: 8px">{{ currentKb.kbName }}</a-tag>
              </span>
              <a-button v-if="currentSession" size="small" @click="handleNewSession">
                新对话
              </a-button>
            </div>
          </template>
          
          <!-- 消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div v-if="messages.length === 0" class="empty-tip">
              <RobotOutlined style="font-size: 48px; color: #1890ff" />
              <p>我是公安业务智能助手，有什么可以帮您的？</p>
            </div>
            
            <div 
              v-for="(msg, index) in messages" 
              :key="index" 
              :class="['message-item', msg.role]"
            >
              <div class="avatar">
                <a-avatar v-if="msg.role === 'user'" :size="36">
                  <template #icon><UserOutlined /></template>
                </a-avatar>
                <a-avatar v-else :size="36" style="background: #1890ff">
                  <template #icon><RobotOutlined /></template>
                </a-avatar>
              </div>
              <div class="content">
                <div class="text" v-html="renderMarkdown(msg.content)"></div>
                <div v-if="msg.sources?.length" class="sources">
                  <a-divider style="margin: 8px 0" />
                  <p style="font-size: 12px; color: #999; margin-bottom: 8px">引用来源：</p>
                  <div v-for="(src, i) in msg.sources" :key="i" class="source-item">
                    <a-tooltip :title="src.content">
                      <a-tag color="blue">
                        文档 #{{ src.docId }}
                        <span v-if="src.score" style="margin-left: 4px; opacity: 0.8">({{ src.score }})</span>
                      </a-tag>
                    </a-tooltip>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="loading" class="message-item assistant">
              <div class="avatar">
                <a-avatar :size="36" style="background: #1890ff">
                  <template #icon><RobotOutlined /></template>
                </a-avatar>
              </div>
              <div class="content">
                <a-spin size="small" /> 思考中...
              </div>
            </div>
          </div>
          
          <!-- 输入区域 -->
          <div class="input-area">
            <a-textarea 
              v-model:value="inputText"
              placeholder="请输入您的问题..."
              :auto-size="{ minRows: 2, maxRows: 4 }"
              @pressEnter="handleSend"
            />
            <a-button type="primary" :loading="loading" @click="handleSend">
              发送
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import { RobotOutlined, UserOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getKbList, getSessions, createSession, getSessionMessages, deleteSession, sendMessage } from '@/api/ai'

const loading = ref(false)
const inputText = ref('')
const selectedKbId = ref(null)
const kbList = ref([])
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const messageListRef = ref(null)

const currentKb = computed(() => {
  return kbList.value.find(kb => kb.id === selectedKbId.value)
})

async function fetchKbList() {
  try {
    const res = await getKbList({ current: 1, size: 100 })
    kbList.value = res.records
  } catch (error) {
    console.error(error)
  }
}

async function fetchSessions() {
  try {
    sessions.value = await getSessions()
  } catch (error) {
    console.error(error)
  }
}

async function selectSession(session) {
  if (currentSession.value?.sessionId === session.sessionId) return
  
  currentSession.value = session
  selectedKbId.value = session.kbId
  
  // 加载消息历史
  try {
    const msgs = await getSessionMessages(session.sessionId)
    messages.value = msgs.map(msg => ({
      role: msg.role,
      content: msg.content,
      sources: msg.sources ? JSON.parse(msg.sources) : null
    }))
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error(error)
    messages.value = []
  }
}

async function handleNewSession() {
  try {
    const session = await createSession(selectedKbId.value)
    currentSession.value = session
    messages.value = []
    await fetchSessions()
  } catch (error) {
    console.error(error)
  }
}

async function handleDeleteSession(sessionId) {
  try {
    await deleteSession(sessionId)
    await fetchSessions()
    if (currentSession.value?.sessionId === sessionId) {
      currentSession.value = null
      messages.value = []
    }
  } catch (error) {
    console.error(error)
  }
}

function handleKbChange() {
  // 切换知识库时可以选择清空当前对话或保持
}

function renderMarkdown(text) {
  return marked(text)
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return date.toLocaleDateString()
}

async function handleSend(e) {
  // 如果是按Enter键但按下了Shift，则换行
  if (e?.shiftKey) return
  
  const question = inputText.value.trim()
  if (!question) return
  
  // 如果没有当前会话，先创建一个
  if (!currentSession.value) {
    try {
      currentSession.value = await createSession(selectedKbId.value)
      await fetchSessions()
    } catch (error) {
      console.error(error)
      return
    }
  }
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: question
  })
  
  inputText.value = ''
  loading.value = true
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  try {
    const res = await sendMessage(currentSession.value.sessionId, question)
    
    // 添加助手消息
    messages.value.push({
      role: 'assistant',
      content: res.answer,
      sources: res.sources
    })
    
    // 更新会话列表
    await fetchSessions()
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我遇到了一些问题，请稍后重试。'
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

onMounted(() => {
  fetchKbList()
  fetchSessions()
})
</script>

<style lang="less" scoped>
.ai-chat {
  height: calc(100vh - 150px);
  
  .session-item {
    cursor: pointer;
    padding: 12px !important;
    
    &.active {
      background: #e6f7ff;
    }
    
    &:hover {
      background: #f5f5f5;
      
      .delete-icon {
        opacity: 1;
      }
    }
    
    .session-title {
      display: inline-block;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .delete-icon {
      opacity: 0;
      color: #ff4d4f;
      transition: opacity 0.2s;
      
      &:hover {
        color: #ff7875;
      }
    }
  }
  
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    min-height: 400px;
    
    .empty-tip {
      text-align: center;
      padding: 60px 0;
      color: #999;
      
      p {
        margin-top: 16px;
      }
    }
  }
  
  .message-item {
    display: flex;
    margin-bottom: 20px;
    
    &.user {
      flex-direction: row-reverse;
      
      .content {
        background: #1890ff;
        color: #fff;
        border-radius: 12px 12px 0 12px;
      }
    }
    
    &.assistant {
      .content {
        background: #f5f5f5;
        border-radius: 12px 12px 12px 0;
      }
    }
    
    .avatar {
      margin: 0 12px;
    }
    
    .content {
      max-width: 70%;
      padding: 12px 16px;
      
      .text {
        word-break: break-word;
        
        :deep(pre) {
          background: #282c34;
          color: #abb2bf;
          padding: 12px;
          border-radius: 4px;
          overflow-x: auto;
        }
        
        :deep(code) {
          background: #f0f0f0;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: monospace;
        }
      }
      
      .sources {
        margin-top: 8px;
        
        .source-item {
          display: inline-block;
          margin-right: 4px;
          margin-bottom: 4px;
        }
      }
    }
  }
  
  .input-area {
    display: flex;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    
    .ant-input {
      flex: 1;
    }
  }
}
</style>