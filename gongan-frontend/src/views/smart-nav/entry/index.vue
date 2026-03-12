<template>
  <div class="smart-nav">
    <a-card>
      <div class="nav-container">
        <h2><CompassOutlined /> 智能导航</h2>
        <p class="desc">请描述您想要进行的操作，系统将智能识别您的意图并导航到对应页面</p>

        <div v-if="examplePrompts.length" class="example-section">
          <span class="example-label">试试这样说：</span>
          <a-tag v-for="(p, i) in examplePrompts" :key="i" class="example-tag" @click="userInput = p">{{ p }}</a-tag>
        </div>
        
        <div class="input-section">
          <a-textarea
            v-model:value="userInput"
            :placeholder="placeholderText"
            :auto-size="{ minRows: 3, maxRows: 6 }"
            @pressEnter="handleNavigate"
          />
          <a-button 
            type="primary" 
            size="large" 
            :loading="loading"
            @click="handleNavigate"
            style="margin-top: 16px"
          >
            <template #icon><SearchOutlined /></template>
            智能导航
          </a-button>
        </div>

        <!-- 常用入口 -->
        <div v-if="shortcutIntents.length" class="shortcut-section">
          <a-divider>常用入口</a-divider>
          <a-row :gutter="12">
            <a-col :span="6" v-for="item in shortcutIntents" :key="item.targetPath">
              <a-card hoverable size="small" class="shortcut-card" @click="goToPage(item.targetPath)">
                <component :is="getIconForPath(item.targetPath)" class="shortcut-icon" />
                <div class="shortcut-title">{{ item.intentName }}</div>
              </a-card>
            </a-col>
          </a-row>
        </div>
        
        <!-- 推荐功能 -->
        <div v-if="recommendList.length" class="recommend-section">
          <a-divider>为您推荐</a-divider>
          <a-row :gutter="16">
            <a-col :span="8" v-for="item in recommendList" :key="item.path">
              <a-card hoverable class="recommend-card" @click="goToPage(item.path)">
                <component :is="item.icon" class="recommend-icon" />
                <div class="recommend-title">{{ item.title }}</div>
                <div class="recommend-desc">{{ item.desc }}</div>
              </a-card>
            </a-col>
          </a-row>
        </div>
        
        <!-- 导航历史 -->
        <div v-if="navHistory.length" class="history-section">
          <a-divider>导航历史</a-divider>
          <a-timeline>
            <a-timeline-item v-for="(item, index) in navHistory" :key="index" :color="item.success ? 'green' : 'red'">
              <p><strong>{{ item.input }}</strong></p>
              <p v-if="item.success">导航至: {{ item.target }}</p>
              <p v-else style="color: #ff4d4f">{{ item.message }}</p>
              <p style="color: #999; font-size: 12px">{{ item.time }}</p>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  CompassOutlined, 
  SearchOutlined,
  UserOutlined,
  FileTextOutlined,
  RobotOutlined,
  SecurityScanOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import { navigate, getIntentHistory, listIntents } from '@/api/nav'

const router = useRouter()
const loading = ref(false)
const userInput = ref('')
const recommendList = ref([])
const navHistory = ref([])
const examplePrompts = ref([])
const shortcutIntents = ref([])
const placeholderText = ref('例如：查重点人员、打开案件分析、查张三、填文书、智能问答…')

// 根据路径返回推荐卡片图标（用于后端返回的 recommendations）
function getIconForPath(path) {
  if (!path) return SecurityScanOutlined
  if (path.includes('customer')) return UserOutlined
  if (path.includes('analysis')) return SecurityScanOutlined
  if (path.includes('document')) return FileTextOutlined
  if (path.includes('chat')) return RobotOutlined
  if (path.includes('kb')) return RobotOutlined
  if (path.includes('due-diligence') || path.includes('suspicious')) return EyeOutlined
  if (path.includes('user')) return UserOutlined
  return SecurityScanOutlined
}

async function handleNavigate() {
  if (!userInput.value.trim()) {
    message.warning('请输入您想要进行的操作')
    return
  }
  
  loading.value = true
  
  try {
    const res = await navigate(userInput.value)
    const success = res.success ?? res.matched
    const targetName = res.targetName ?? res.intentName

    // 添加到历史记录
    navHistory.value.unshift({
      input: userInput.value,
      success,
      target: targetName,
      path: res.targetPath,
      message: res.message,
      time: new Date().toLocaleString()
    })

    // 保留最近10条记录
    if (navHistory.value.length > 10) {
      navHistory.value = navHistory.value.slice(0, 10)
    }

    if (success) {
      // 更新推荐列表（后端返回 { intentCode, targetPath, intentName, description }）
      recommendList.value = (res.recommendations || []).map(rec => ({
        path: rec.targetPath,
        title: rec.intentName,
        desc: rec.description || '',
        icon: getIconForPath(rec.targetPath)
      }))
      
      // 导航到目标页面
      message.success(`正在导航至: ${targetName}`)
      setTimeout(() => {
        router.push(res.targetPath)
      }, 800)
    } else {
      message.warning(res.message || '无法识别您的意图，请重新描述')
    }
  } catch (error) {
    message.error('导航服务异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

function goToPage(path) {
  router.push(path)
}

onMounted(async () => {
  try {
    const list = await getIntentHistory(10)
    navHistory.value = (list || []).map(item => ({
      input: item.inputText,
      success: item.success === 1,
      target: item.targetPath || item.intentCode || '',
      path: item.targetPath,
      message: item.message,
      time: item.createdTime ? new Date(item.createdTime).toLocaleString() : ''
    }))
  } catch (_) {
    // 保持本地内存历史或空
  }
  try {
    const intents = await listIntents()
    const prompts = []
    ;(intents || []).forEach(item => {
      if (item.examplePrompts && typeof item.examplePrompts === 'string') {
        item.examplePrompts.split(/[,，]/).forEach(p => {
          const t = p.trim()
          if (t && !prompts.includes(t)) prompts.push(t)
        })
      }
    })
    if (prompts.length) examplePrompts.value = prompts.slice(0, 12)
    shortcutIntents.value = (intents || []).slice(0, 8)
  } catch (_) {}
})
</script>

<style lang="less" scoped>
.smart-nav {
  .nav-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 24px;
    
    h2 {
      text-align: center;
      margin-bottom: 8px;
    }
    
    .desc {
      text-align: center;
      color: #999;
      margin-bottom: 16px;
    }

    .example-section {
      text-align: center;
      margin-bottom: 24px;
      .example-label {
        color: #666;
        margin-right: 8px;
        font-size: 13px;
      }
      .example-tag {
        cursor: pointer;
        margin-bottom: 6px;
      }
    }

    .shortcut-section {
      margin-top: 24px;
      .shortcut-card {
        text-align: center;
        cursor: pointer;
        .shortcut-icon {
          font-size: 24px;
          color: #1890ff;
        }
        .shortcut-title {
          margin-top: 8px;
          font-size: 13px;
        }
      }
    }
    
    .input-section {
      text-align: center;
    }
    
    .recommend-section {
      margin-top: 32px;
      
      .recommend-card {
        text-align: center;
        padding: 16px;
        
        .recommend-icon {
          font-size: 32px;
          color: #1890ff;
        }
        
        .recommend-title {
          margin-top: 12px;
          font-weight: 500;
        }
        
        .recommend-desc {
          margin-top: 4px;
          font-size: 12px;
          color: #999;
        }
      }
    }
    
    .history-section {
      margin-top: 32px;
    }
  }
}
</style>
