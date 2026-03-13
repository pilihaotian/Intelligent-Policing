<template>
  <div class="smart-nav-page">
    <!-- 顶部搜索区域 -->
    <div class="search-header">
      <div class="header-content">
        <h1 class="title">
          <CompassOutlined class="title-icon" />
          智能导航
          <a-tooltip title="同步菜单数据">
            <a-button 
              type="link" 
              class="sync-btn"
              :loading="syncing"
              @click="handleSync"
            >
              <template #icon><SyncOutlined /></template>
            </a-button>
          </a-tooltip>
        </h1>
        <p class="subtitle">输入关键词或自然语言，快速定位目标功能</p>

        <!-- 搜索输入框 -->
        <div class="search-box">
          <a-input
            v-model:value="searchQuery"
            size="large"
            placeholder="输入关键词、快捷指令或自然语言描述..."
            allow-clear
            @keyup.enter="handleSearch"
            @input="handleInputChange"
          >
            <template #prefix>
              <SearchOutlined class="search-icon" />
            </template>
            <template #suffix>
              <a-space :size="4">
                <a-tag v-if="matchedShortcut" color="blue" class="shortcut-tag">
                  {{ matchedShortcut.name }}
                </a-tag>
                <a-button type="primary" :loading="loading" @click="handleSearch">
                  导航
                </a-button>
              </a-space>
            </template>
          </a-input>

          <!-- 实时建议下拉框 -->
          <div v-if="showSuggestions && suggestions.length" class="suggestions-dropdown">
            <div 
              v-for="item in suggestions" 
              :key="item.intentCode"
              class="suggestion-item"
              @click="handleSelectSuggestion(item)"
            >
              <span class="suggestion-name">{{ item.intentName }}</span>
              <span class="suggestion-desc">{{ item.description }}</span>
            </div>
          </div>
        </div>

        <!-- 快捷指令提示 -->
        <div class="shortcuts-hint">
          <span class="hint-label">快捷指令：</span>
          <a-tag 
            v-for="sc in displayShortcuts" 
            :key="sc.code"
            class="shortcut-chip"
            @click="searchQuery = sc.code"
          >
            {{ sc.code }} → {{ sc.name }}
          </a-tag>
        </div>
      </div>
    </div>

    <!-- 搜索结果区域 -->
    <div v-if="searchResult" class="result-section">
      <a-card v-if="searchResult.success" class="result-card success-card">
        <template #title>
          <CheckCircleOutlined style="color: #52c41a; margin-right: 8px" />
          {{ searchResult.matched ? '为您找到以下匹配结果' : searchResult.message }}
        </template>
        
        <!-- 候选结果列表 -->
        <div class="candidates-list">
          <div 
            v-for="(candidate, index) in searchResult.candidates" 
            :key="candidate.intentCode"
            class="candidate-card"
            :class="{ 'best-match': index === 0 }"
            @click="handleNavigate(candidate)"
          >
            <div class="candidate-header">
              <span class="candidate-index">{{ index + 1 }}</span>
              <span class="candidate-name">{{ candidate.intentName }}</span>
              <a-tag v-if="index === 0" color="green" class="best-tag">最佳匹配</a-tag>
              <a-tag v-else-if="index === 1" color="blue">备选</a-tag>
              <a-tag v-else color="default">相关</a-tag>
            </div>
            <div class="candidate-desc">{{ candidate.description }}</div>
            <div class="candidate-footer">
              <a-progress 
                :percent="Math.round(candidate.confidence)" 
                :stroke-color="getProgressColor(candidate.confidence)"
                size="small"
                style="width: 120px"
              />
              <span class="match-type">{{ candidate.matchType }}</span>
            </div>
          </div>
        </div>
      </a-card>

      <a-card v-else class="result-card fail-card">
        <template #title>
          <WarningOutlined style="color: #faad14; margin-right: 8px" />
          {{ searchResult.message || '未找到匹配结果' }}
        </template>
        
        <div v-if="searchResult.candidates?.length" class="suggestions-text">
          <p>您是否想要：</p>
          <div class="candidates-list">
            <div 
              v-for="candidate in searchResult.candidates" 
              :key="candidate.intentCode"
              class="candidate-card suggestion"
              @click="handleNavigate(candidate)"
            >
              <span class="candidate-name">{{ candidate.intentName }}</span>
              <span class="candidate-desc">{{ candidate.description }}</span>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 下方内容区域 -->
    <div class="content-section">
      <a-row :gutter="16">
        <!-- 左侧：推荐功能 -->
        <a-col :span="12">
          <a-card title="为您推荐" class="recommend-card">
            <template #extra>
              <ReloadOutlined class="refresh-btn" @click="loadRecommendations" />
            </template>
            <div class="recommend-grid">
              <div 
                v-for="item in recommendations" 
                :key="item.intentCode"
                class="recommend-item"
                @click="handleNavigate(item)"
              >
                <component :is="getIcon(item.intentCode)" class="recommend-icon" />
                <div class="recommend-info">
                  <div class="recommend-name">{{ item.intentName }}</div>
                  <div class="recommend-count" v-if="item.frequency">
                    访问 {{ item.frequency }} 次
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧：导航历史 -->
        <a-col :span="12">
          <a-card title="导航历史" class="history-card">
            <template #extra>
              <span class="history-count">最近 {{ historyList.length }} 条</span>
            </template>
            <div v-if="historyList.length" class="history-list">
              <div 
                v-for="item in historyList" 
                :key="item.id"
                class="history-item"
                @click="handleHistoryClick(item)"
              >
                <div class="history-status">
                  <CheckCircleOutlined v-if="item.success" style="color: #52c41a" />
                  <CloseCircleOutlined v-else style="color: #ff4d4f" />
                </div>
                <div class="history-content">
                  <div class="history-query">{{ item.inputText }}</div>
                  <div class="history-target" v-if="item.targetPath">
                    → {{ getHistoryTargetName(item) }}
                  </div>
                </div>
                <div class="history-time">{{ formatTime(item.createdTime) }}</div>
              </div>
            </div>
            <a-empty v-else description="暂无导航历史" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          </a-card>
        </a-col>
      </a-row>

      <!-- 热门导航 -->
      <a-card title="热门导航" class="hot-card" style="margin-top: 16px">
        <div class="hot-list">
          <div 
            v-for="(item, index) in hotNavigations" 
            :key="item.intentCode"
            class="hot-item"
            @click="handleNavigate(item)"
          >
            <span class="hot-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
            <span class="hot-name">{{ item.intentName }}</span>
            <span class="hot-count">{{ item.count }} 次访问</span>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import {
  CompassOutlined,
  SearchOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  ReloadOutlined,
  CloseCircleOutlined,
  UserOutlined,
  FileTextOutlined,
  RobotOutlined,
  SecurityScanOutlined,
  EyeOutlined,
  TeamOutlined,
  SettingOutlined,
  DatabaseOutlined,
  LineChartOutlined,
  FileSearchOutlined,
  SyncOutlined
} from '@ant-design/icons-vue'
import {
  recognizeIntent,
  getSuggestions,
  getRecommendations,
  getShortcuts,
  getHotNavigations,
  getIntentHistory,
  syncMenusToIntents
} from '@/api/nav'

const router = useRouter()

// 状态
const searchQuery = ref('')
const loading = ref(false)
const searchResult = ref(null)
const suggestions = ref([])
const showSuggestions = ref(false)
const shortcuts = ref([])
const recommendations = ref([])
const historyList = ref([])
const hotNavigations = ref([])
const syncing = ref(false)

// 计算属性：显示前5个快捷指令
const displayShortcuts = computed(() => shortcuts.value.slice(0, 5))

// 计算属性：当前输入匹配的快捷指令
const matchedShortcut = computed(() => {
  if (!searchQuery.value) return null
  return shortcuts.value.find(s => s.code === searchQuery.value.toLowerCase())
})

// 输入变化时获取建议
let inputTimer = null
function handleInputChange() {
  if (inputTimer) clearTimeout(inputTimer)
  
  if (!searchQuery.value.trim()) {
    showSuggestions.value = false
    suggestions.value = []
    return
  }

  inputTimer = setTimeout(async () => {
    try {
      const res = await getSuggestions(searchQuery.value, 5)
      suggestions.value = res || []
      showSuggestions.value = suggestions.value.length > 0
    } catch (e) {
      // ignore
    }
  }, 300)
}

// 执行搜索
async function handleSearch() {
  if (!searchQuery.value.trim()) {
    message.warning('请输入搜索内容')
    return
  }

  showSuggestions.value = false
  loading.value = true

  try {
    const res = await recognizeIntent(searchQuery.value)
    searchResult.value = res
  } catch (error) {
    message.error('导航服务异常')
    searchResult.value = { success: false, message: '服务异常，请稍后重试' }
  } finally {
    loading.value = false
  }
}

// 选择建议项
function handleSelectSuggestion(item) {
  searchQuery.value = item.intentName
  showSuggestions.value = false
  handleNavigate(item)
}

// 导航到目标页面
function handleNavigate(item) {
  if (item.targetPath) {
    message.loading(`正在导航至: ${item.intentName}`, 0.5)
    setTimeout(() => {
      router.push(item.targetPath)
    }, 500)
  }
}

// 点击历史记录
function handleHistoryClick(item) {
  if (item.targetPath) {
    router.push(item.targetPath)
  } else if (item.inputText) {
    searchQuery.value = item.inputText
    handleSearch()
  }
}

// 加载推荐
async function loadRecommendations() {
  try {
    recommendations.value = await getRecommendations(6) || []
  } catch (e) {
    // ignore
  }
}

// 加载快捷指令
async function loadShortcuts() {
  try {
    shortcuts.value = await getShortcuts() || []
  } catch (e) {
    // ignore
  }
}

// 加载历史
async function loadHistory() {
  try {
    historyList.value = await getIntentHistory(10) || []
  } catch (e) {
    // ignore
  }
}

// 加载热门
async function loadHot() {
  try {
    hotNavigations.value = await getHotNavigations(5) || []
  } catch (e) {
    // ignore
  }
}

// 同步菜单到意图表
async function handleSync() {
  syncing.value = true
  try {
    const res = await syncMenusToIntents()
    if (res.success) {
      message.success(res.message || `全量同步完成！新增 ${res.added || 0} 条意图`)
      // 刷新数据
      await Promise.all([
        loadShortcuts(),
        loadRecommendations(),
        loadHot()
      ])
    } else {
      message.warning(res.message || '同步失败')
    }
  } catch (e) {
    message.error('同步失败：' + (e.message || '未知错误'))
  } finally {
    syncing.value = false
  }
}

// 获取图标
function getIcon(intentCode) {
  const iconMap = {
    'SUSPICIOUS_PERSON': UserOutlined,
    'CASE_ANALYSIS': SecurityScanOutlined,
    'LEGAL_DOCUMENT': FileTextOutlined,
    'AI_CHAT': RobotOutlined,
    'PERSON_CHECK': EyeOutlined,
    'CLUE_MANAGE': FileSearchOutlined,
    'USER_MANAGE': TeamOutlined,
    'KB_MANAGE': DatabaseOutlined,
    'ROLE_MANAGE': SettingOutlined,
    'ORG_MANAGE': TeamOutlined,
    'TRANSACTION_LIST': LineChartOutlined
  }
  return iconMap[intentCode] || CompassOutlined
}

// 获取进度条颜色
function getProgressColor(confidence) {
  if (confidence >= 80) return '#52c41a'
  if (confidence >= 60) return '#1890ff'
  if (confidence >= 40) return '#faad14'
  return '#ff4d4f'
}

// 获取历史记录目标名称
function getHistoryTargetName(item) {
  const intent = recommendations.value.find(r => r.intentCode === item.intentCode)
  return intent?.intentName || item.intentCode || '未知'
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return date.toLocaleDateString()
}

// 点击外部关闭建议框
function handleClickOutside(e) {
  if (!e.target.closest('.search-box')) {
    showSuggestions.value = false
  }
}

onMounted(() => {
  loadShortcuts()
  loadRecommendations()
  loadHistory()
  loadHot()
  document.addEventListener('click', handleClickOutside)
})

// 监听搜索结果变化，刷新历史
watch(searchResult, (val) => {
  if (val?.success) {
    setTimeout(loadHistory, 1000)
  }
})
</script>

<style lang="less" scoped>
.smart-nav-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

// 搜索头部
.search-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 40px 24px;
  
  .header-content {
    max-width: 800px;
    margin: 0 auto;
  }

  .title {
    color: white;
    font-size: 28px;
    margin-bottom: 8px;
    
    .title-icon {
      margin-right: 12px;
    }
  }

  .subtitle {
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: 24px;
  }
}

// 搜索框
.search-box {
  position: relative;
  
  :deep(.ant-input-affix-wrapper) {
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .search-icon {
    color: #bfbfbf;
  }

  .shortcut-tag {
    margin-right: 8px;
  }
}

// 建议下拉框
.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  margin-top: 4px;
  z-index: 100;
  overflow: hidden;

  .suggestion-item {
    padding: 12px 16px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    transition: background 0.2s;

    &:hover {
      background: #e6f7ff;
    }

    &:last-child {
      border-bottom: none;
    }

    .suggestion-name {
      font-weight: 500;
      margin-right: 12px;
    }

    .suggestion-desc {
      color: #8c8c8c;
      font-size: 12px;
    }
  }
}

// 快捷指令提示
.shortcuts-hint {
  margin-top: 12px;
  
  .hint-label {
    color: rgba(255, 255, 255, 0.7);
    font-size: 12px;
  }

  .shortcut-chip {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    cursor: pointer;
    margin: 4px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

// 搜索结果区域
.result-section {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 24px;
}

.result-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

// 候选卡片列表
.candidates-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.candidate-card {
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;

  &:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
    transform: translateY(-2px);
  }

  &.best-match {
    border-color: #52c41a;
    background: linear-gradient(to right, #f6ffed, white);
  }

  &.suggestion {
    padding: 12px;
  }

  .candidate-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;

    .candidate-index {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: #1890ff;
      color: white;
      text-align: center;
      line-height: 24px;
      font-size: 12px;
      margin-right: 12px;
    }

    .candidate-name {
      font-weight: 600;
      font-size: 16px;
      flex: 1;
    }

    .best-tag {
      margin-left: 8px;
    }
  }

  .candidate-desc {
    color: #8c8c8c;
    font-size: 13px;
    margin-bottom: 8px;
  }

  .candidate-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .match-type {
    color: #8c8c8c;
    font-size: 12px;
  }
}

// 内容区域
.content-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 24px;
}

// 推荐卡片
.recommend-card {
  height: 100%;
  
  .refresh-btn {
    cursor: pointer;
    color: #1890ff;
    
    &:hover {
      opacity: 0.8;
    }
  }
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.recommend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #e6f7ff;
  }

  .recommend-icon {
    font-size: 24px;
    color: #1890ff;
    margin-right: 12px;
  }

  .recommend-info {
    .recommend-name {
      font-weight: 500;
    }

    .recommend-count {
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}

// 历史卡片
.history-card {
  height: 100%;
  
  .history-count {
    color: #8c8c8c;
    font-size: 12px;
  }
}

.history-list {
  max-height: 280px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;

  &:hover {
    background: #fafafa;
  }

  .history-status {
    margin-right: 12px;
  }

  .history-content {
    flex: 1;
    min-width: 0;

    .history-query {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .history-target {
      font-size: 12px;
      color: #1890ff;
    }
  }

  .history-time {
    font-size: 12px;
    color: #8c8c8c;
    margin-left: 12px;
  }
}

// 热门导航
.hot-card {
  .hot-list {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .hot-item {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    background: #f5f5f5;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #e6f7ff;
    }

    .hot-rank {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #8c8c8c;
      color: white;
      text-align: center;
      line-height: 20px;
      font-size: 12px;
      margin-right: 8px;

      &.rank-1 { background: #ff4d4f; }
      &.rank-2 { background: #fa8c16; }
      &.rank-3 { background: #faad14; }
    }

    .hot-name {
      margin-right: 8px;
    }

    .hot-count {
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}
</style>
