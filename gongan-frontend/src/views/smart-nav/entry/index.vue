<template>
  <div class="smart-nav">
    <a-card>
      <div class="nav-container">
        <h2><CompassOutlined /> 智能导航</h2>
        <p class="desc">请描述您想要进行的操作，系统将智能识别您的意图并导航到对应页面</p>
        
        <div class="input-section">
          <a-textarea
            v-model:value="userInput"
            placeholder="例如：我想查看..."
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
import { ref, reactive } from 'vue'
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
import { navigate } from '@/api/nav'

const router = useRouter()
const loading = ref(false)
const userInput = ref('')
const recommendList = ref([])
const navHistory = ref([])

const intentMap = {
  'suspicious_customer': { path: '/anti-fraud/customer', title: '重点人员管理', icon: UserOutlined, desc: '查看和管理重点人员列表' },
  'fraud_analysis': { path: '/anti-fraud/analysis', title: '案件分析', icon: SecurityScanOutlined, desc: '进行案件深度分析研判' },
  'legal_document': { path: '/ops-risk/document', title: '案件信息填报', icon: FileTextOutlined, desc: 'AI智能提取文书信息并填报' },
  'ai_chat': { path: '/ai-assistant/chat', title: '智能问答', icon: RobotOutlined, desc: '与AI助手进行对话' },
  'knowledge_base': { path: '/ai-assistant/kb', title: '知识库管理', icon: RobotOutlined, desc: '管理业务知识库' },
  'aml_due_diligence': { path: '/aml/due-diligence', title: '人员核查', icon: EyeOutlined, desc: '人员背景核查管理' },
  'aml_suspicious': { path: '/aml/suspicious', title: '线索管理', icon: EyeOutlined, desc: '线索情报管理' },
  'user_manage': { path: '/system/user', title: '用户管理', icon: UserOutlined, desc: '管理系统用户' }
}

async function handleNavigate() {
  if (!userInput.value.trim()) {
    message.warning('请输入您想要进行的操作')
    return
  }
  
  loading.value = true
  
  try {
    const res = await navigate(userInput.value)
    
    // 添加到历史记录
    navHistory.value.unshift({
      input: userInput.value,
      success: res.success,
      target: res.targetName,
      path: res.targetPath,
      message: res.message,
      time: new Date().toLocaleString()
    })
    
    // 保留最近10条记录
    if (navHistory.value.length > 10) {
      navHistory.value = navHistory.value.slice(0, 10)
    }
    
    if (res.success) {
      // 更新推荐列表
      recommendList.value = (res.recommendations || []).map(intent => {
        const item = intentMap[intent]
        if (item) {
          return { ...item, path: item.path }
        }
        return null
      }).filter(Boolean)
      
      // 导航到目标页面
      message.success(`正在导航至: ${res.targetName}`)
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
      margin-bottom: 32px;
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
