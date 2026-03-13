<template>
  <a-drawer
    v-model:open="visible"
    placement="right"
    :width="drawerWidth"
    :closable="false"
    :body-style="{ padding: 0 }"
    class="smart-nav-drawer"
  >
    <!-- 头部 -->
    <div class="drawer-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="header-top">
          <div class="header-title">
            <RobotOutlined class="robot-icon" />
            <span>智能导航助手</span>
          </div>
          <div class="header-actions">
            <a-tooltip title="同步菜单数据">
              <a-button 
                type="text" 
                class="sync-btn"
                :loading="syncing"
                @click="handleSync"
              >
                <template #icon><SyncOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-button type="text" class="close-btn" @click="visible = false">
              <CloseOutlined />
            </a-button>
          </div>
        </div>
        <p class="header-subtitle">输入关键词或自然语言，快速定位目标功能</p>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-wrapper">
        <a-input
          v-model:value="searchQuery"
          size="large"
          placeholder="输入关键词或自然语言描述..."
          allow-clear
          @keyup.enter="handleSearch"
          @input="handleInputChange"
          @focus="handleInputFocus"
          class="search-input"
        >
          <template #prefix>
            <SearchOutlined class="search-icon" />
          </template>
        </a-input>
        
        <a-button type="primary" :loading="loading" @click="handleSearch" class="search-btn">
          导航
        </a-button>

        <!-- 搜索结果/建议浮层（紧凑显示） -->
        <div v-if="showDropdown" class="dropdown-panel">
          <!-- 搜索结果 -->
          <template v-if="searchResult?.success && searchResult.candidates?.length">
            <div 
              v-for="(candidate, index) in searchResult.candidates" 
              :key="candidate.intentCode"
              class="result-item"
              :class="{ 'best': index === 0 }"
              @click="handleNavigate(candidate)"
            >
              <span class="result-index">{{ index + 1 }}</span>
              <span class="result-name">{{ candidate.intentName }}</span>
              <span class="result-confidence">{{ Math.round(candidate.confidence) }}%</span>
              <a-tag v-if="index === 0" color="green" size="small">最佳</a-tag>
            </div>
          </template>
          
          <!-- 无匹配结果 -->
          <template v-else-if="searchResult && !searchResult.success">
            <div class="no-result">{{ searchResult.message || '未找到匹配结果' }}</div>
          </template>
          
          <!-- 实时建议 -->
          <template v-else-if="suggestions.length && !searchResult">
            <div 
              v-for="item in suggestions" 
              :key="item.intentCode"
              class="suggestion-item"
              @click="handleSelectSuggestion(item)"
            >
              <span class="suggestion-name">{{ item.intentName }}</span>
              <span class="suggestion-desc">{{ item.description }}</span>
            </div>
          </template>
        </div>
      </div>

      <!-- 快捷指令 -->
      <div class="shortcuts-row" v-if="shortcuts.length && !showDropdown">
        <div 
          v-for="sc in shortcuts" 
          :key="sc.code"
          class="shortcut-tag"
          @click="handleShortcutClick(sc)"
        >
          <span class="code">{{ sc.code }}</span>
          <span class="name">{{ sc.name }}</span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="drawer-content">
      <!-- 为您推荐（图标网格） -->
      <div class="grid-section" v-if="recommendations.length">
        <div class="section-title">
          <StarOutlined />
          <span>为您推荐</span>
          <ReloadOutlined class="refresh-btn" @click="loadRecommendations" />
        </div>
        <div class="icon-grid">
          <div 
            v-for="item in recommendations" 
            :key="item.intentCode"
            class="icon-item"
            @click="handleNavigate(item)"
          >
            <component :is="getIcon(item.intentCode)" class="item-icon" />
            <span class="item-name">{{ item.intentName }}</span>
          </div>
        </div>
      </div>

      <!-- 热门导航（图标网格） -->
      <div class="grid-section" v-if="hotNavigations.length">
        <div class="section-title">
          <FireOutlined />
          <span>热门导航</span>
        </div>
        <div class="icon-grid">
          <div 
            v-for="(item, index) in hotNavigations" 
            :key="item.intentCode"
            class="icon-item"
            @click="handleNavigate(item)"
          >
            <div class="item-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <component :is="getIcon(item.intentCode)" class="item-icon" />
            <span class="item-name">{{ item.intentName }}</span>
          </div>
        </div>
      </div>

      <!-- 导航历史（固定高度滚动） -->
      <div class="history-section" v-if="historyList.length">
        <div class="section-title">
          <HistoryOutlined />
          <span>导航历史</span>
        </div>
        <div class="history-list">
          <div 
            v-for="item in historyList" 
            :key="item.id"
            class="history-item"
            @click="handleHistoryClick(item)"
          >
            <div class="history-status">
              <CheckCircleOutlined v-if="item.success" class="success-icon-sm" />
              <CloseCircleOutlined v-else class="fail-icon-sm" />
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
      </div>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, computed, watch, defineExpose } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  RobotOutlined,
  CloseOutlined,
  SearchOutlined,
  CheckCircleOutlined,
  ThunderboltOutlined,
  StarOutlined,
  ReloadOutlined,
  FireOutlined,
  HistoryOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  UserOutlined,
  FileTextOutlined,
  SecurityScanOutlined,
  EyeOutlined,
  TeamOutlined,
  SettingOutlined,
  DatabaseOutlined,
  LineChartOutlined,
  FileSearchOutlined,
  CompassOutlined
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
const visible = ref(false)

// 响应式宽度（约1/3屏幕）
const drawerWidth = computed(() => {
  const width = Math.max(360, Math.min(480, window.innerWidth / 3))
  return width
})

// 状态
const searchQuery = ref('')
const loading = ref(false)
const searchResult = ref(null)
const suggestions = ref([])
const shortcuts = ref([])
const recommendations = ref([])
const historyList = ref([])
const hotNavigations = ref([])
const syncing = ref(false)

// 是否显示下拉面板
const showDropdown = computed(() => {
  return (suggestions.value.length > 0 && !searchResult.value) || 
         (searchResult.value !== null)
})

// 暴露方法给父组件
function open() {
  visible.value = true
}

function close() {
  visible.value = false
}

defineExpose({ open, close })

// 输入获取焦点
function handleInputFocus() {
  if (searchQuery.value.trim()) {
    handleInputChange()
  }
}

// 输入变化时获取建议（最多3个）
let inputTimer = null
function handleInputChange() {
  if (inputTimer) clearTimeout(inputTimer)
  
  // 输入变化时清除之前的搜索结果
  searchResult.value = null
  
  if (!searchQuery.value.trim()) {
    suggestions.value = []
    return
  }

  inputTimer = setTimeout(async () => {
    try {
      const res = await getSuggestions(searchQuery.value, 3)
      suggestions.value = res || []
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

  loading.value = true
  suggestions.value = []

  try {
    const res = await recognizeIntent(searchQuery.value)
    searchResult.value = res
    
    // 如果只有一个高置信度结果，直接导航
    if (res.success && res.candidates?.length === 1 && res.candidates[0].confidence >= 90) {
      handleNavigate(res.candidates[0])
    }
  } catch (error) {
    message.error('导航服务异常')
    searchResult.value = { success: false, message: '服务异常，请稍后重试' }
  } finally {
    loading.value = false
  }
}

// 点击快捷指令
function handleShortcutClick(sc) {
  searchQuery.value = sc.code
  handleSearch()
}

// 选择建议项
function handleSelectSuggestion(item) {
  searchQuery.value = item.intentName
  suggestions.value = []
  searchResult.value = null
  handleNavigate(item)
}

// 导航到目标页面
function handleNavigate(item) {
  if (item.targetPath) {
    message.loading(`正在导航至: ${item.intentName}`, 0.5)
    setTimeout(() => {
      router.push(item.targetPath)
      visible.value = false
    }, 500)
  }
}

// 点击历史记录
function handleHistoryClick(item) {
  if (item.targetPath) {
    router.push(item.targetPath)
    visible.value = false
  } else if (item.inputText) {
    searchQuery.value = item.inputText
    handleSearch()
  }
}

// 加载数据
async function loadRecommendations() {
  try {
    recommendations.value = await getRecommendations(6) || []
  } catch (e) {}
}

async function loadShortcuts() {
  try {
    shortcuts.value = await getShortcuts() || []
  } catch (e) {}
}

async function loadHistory() {
  try {
    historyList.value = await getIntentHistory(10) || []
  } catch (e) {}
}

async function loadHot() {
  try {
    hotNavigations.value = await getHotNavigations(6) || []
  } catch (e) {}
}

async function handleSync() {
  syncing.value = true
  try {
    const res = await syncMenusToIntents()
    if (res.success) {
      message.success(res.message || `同步完成！新增 ${res.added || 0} 条意图`)
      await Promise.all([loadShortcuts(), loadRecommendations(), loadHot()])
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

// 获取历史记录目标名称
function getHistoryTargetName(item) {
  const allItems = [...recommendations.value, ...hotNavigations.value]
  const intent = allItems.find(r => r.intentCode === item.intentCode)
  return intent?.intentName || item.intentCode || '未知'
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

// 点击外部关闭下拉
function handleClickOutside(e) {
  if (!e.target.closest('.search-wrapper') && !e.target.closest('.dropdown-panel')) {
    suggestions.value = []
    if (searchResult.value && !searchResult.value.success) {
      searchResult.value = null
    }
  }
}

// 打开时加载数据
watch(visible, (val) => {
  if (val) {
    loadShortcuts()
    loadRecommendations()
    loadHistory()
    loadHot()
    document.addEventListener('click', handleClickOutside)
  } else {
    document.removeEventListener('click', handleClickOutside)
    // 关闭时重置
    searchQuery.value = ''
    searchResult.value = null
    suggestions.value = []
  }
})

// 监听搜索结果变化，刷新历史
watch(searchResult, (val) => {
  if (val?.success) {
    setTimeout(loadHistory, 1000)
  }
})
</script>

<style lang="less" scoped>
.smart-nav-drawer {
  :deep(.ant-drawer-body) {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
}

// 头部
.drawer-header {
  position: relative;
  padding: 16px;
  overflow: hidden;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    z-index: 0;
    
    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -50%;
      width: 100%;
      height: 100%;
      background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    }
  }
  
  .header-content {
    position: relative;
    z-index: 1;
  }
  
  .header-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 4px;
  }
  
  .header-title {
    display: flex;
    align-items: center;
    color: white;
    font-size: 18px;
    font-weight: 600;
    
    .robot-icon {
      font-size: 24px;
      margin-right: 8px;
      animation: float 3s ease-in-out infinite;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 4px;
    
    .sync-btn, .close-btn {
      color: white;
      font-size: 14px;
      width: 32px;
      height: 32px;
      border-radius: 6px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
  
  .header-subtitle {
    color: rgba(255, 255, 255, 0.85);
    font-size: 12px;
    margin: 0;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

// 搜索区域
.search-section {
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #f0f0f0;
}

.search-wrapper {
  position: relative;
  display: flex;
  gap: 8px;
  
  .search-input {
    flex: 1;
    border-radius: 8px;
    
    :deep(.ant-input-affix-wrapper) {
      border-radius: 8px;
    }
  }
  
  .search-btn {
    border-radius: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    font-weight: 500;
    
    &:hover {
      opacity: 0.9;
    }
  }
  
  .search-icon {
    color: #bfbfbf;
  }
}

// 下拉面板（搜索结果/建议）
.dropdown-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  margin-top: 4px;
  z-index: 100;
  overflow: hidden;
  max-height: 180px;
  overflow-y: auto;

  .result-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    border-bottom: 1px solid #f5f5f5;
    transition: all 0.2s;

    &:hover {
      background: linear-gradient(90deg, #f0f5ff 0%, #fff 100%);
    }

    &:last-child {
      border-bottom: none;
    }

    &.best {
      background: linear-gradient(90deg, #f6ffed 0%, #fff 100%);
    }

    .result-index {
      width: 18px;
      height: 18px;
      border-radius: 50%;
      background: #667eea;
      color: white;
      text-align: center;
      line-height: 18px;
      font-size: 10px;
      margin-right: 8px;
      flex-shrink: 0;
    }

    .result-name {
      flex: 1;
      font-weight: 500;
      font-size: 13px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .result-confidence {
      font-size: 12px;
      color: #999;
      margin-right: 6px;
    }
  }

  .no-result {
    padding: 16px;
    text-align: center;
    color: #999;
    font-size: 13px;
  }

  .suggestion-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    cursor: pointer;
    border-bottom: 1px solid #f5f5f5;
    transition: background 0.2s;

    &:hover {
      background: linear-gradient(90deg, #f0f5ff 0%, #fff 100%);
    }

    &:last-child {
      border-bottom: none;
    }

    .suggestion-name {
      font-weight: 500;
      font-size: 13px;
      color: #333;
    }

    .suggestion-desc {
      font-size: 11px;
      color: #999;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 快捷指令
.shortcuts-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.shortcut-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  background: linear-gradient(135deg, #f0f5ff 0%, #fff 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e6e8eb;
  font-size: 11px;

  &:hover {
    border-color: #667eea;
    background: linear-gradient(135deg, #e6f0ff 0%, #fff 100%);
  }

  .code {
    font-family: 'SF Mono', Monaco, monospace;
    font-weight: 600;
    color: #667eea;
    margin-right: 3px;
  }

  .name {
    color: #666;
  }
}

// 内容区域
.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

// 图标网格区域
.grid-section {
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  
  .anticon {
    margin-right: 6px;
    color: #667eea;
    font-size: 14px;
  }
  
  .refresh-btn {
    margin-left: auto;
    cursor: pointer;
    color: #999;
    font-size: 14px;
    
    &:hover {
      color: #667eea;
    }
  }
}

// 图标网格（一行3个）
.icon-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
  position: relative;

  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    transform: translateY(-2px);
    
    .item-icon {
      transform: scale(1.1);
    }
  }

  .item-badge {
    position: absolute;
    top: 6px;
    left: 6px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #bfbfbf;
    color: white;
    text-align: center;
    line-height: 16px;
    font-size: 10px;
    font-weight: 600;

    &.rank-1 { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); }
    &.rank-2 { background: linear-gradient(135deg, #ffa502 0%, #ff7f50 100%); }
    &.rank-3 { background: linear-gradient(135deg, #ffd93d 0%, #f9ca24 100%); }
  }

  .item-icon {
    font-size: 24px;
    color: #667eea;
    margin-bottom: 6px;
    transition: transform 0.2s;
  }

  .item-name {
    font-size: 12px;
    color: #333;
    text-align: center;
    line-height: 1.3;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

// 历史记录（固定高度滚动）
.history-section {
  .history-list {
    max-height: 150px;
    overflow-y: auto;
    background: #fafafa;
    border-radius: 8px;
    padding: 8px;
  }
}

.history-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: white;
  }

  .history-status {
    margin-right: 8px;
    
    .success-icon-sm {
      color: #52c41a;
      font-size: 12px;
    }
    
    .fail-icon-sm {
      color: #ff4d4f;
      font-size: 12px;
    }
  }

  .history-content {
    flex: 1;
    min-width: 0;

    .history-query {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 12px;
      color: #333;
    }

    .history-target {
      font-size: 11px;
      color: #667eea;
      margin-top: 1px;
    }
  }

  .history-time {
    font-size: 10px;
    color: #bfbfbf;
    margin-left: 6px;
    flex-shrink: 0;
  }
}
</style>
