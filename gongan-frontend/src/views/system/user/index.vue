<template>
  <div class="user-manage" :class="{ 'rainbow-mode': rainbowMode, 'dark-egg-mode': darkEggMode }">
    <!-- 撒花容器 -->
    <div v-if="showConfetti" class="confetti-container">
      <div v-for="i in 50" :key="i" class="confetti" :style="confettiStyle(i)"></div>
    </div>
    
    <!-- 星星特效容器 -->
    <div v-if="showStars" class="stars-container">
      <div v-for="i in 30" :key="i" class="star" :style="starStyle(i)"></div>
    </div>

    <a-card>
      <template #title>
        <span 
          class="title-egg" 
          :class="{ 'title-shake': titleShake }"
          @click="handleTitleClick"
          @dblclick="handleTitleDblClick"
        >
          {{ titleText }}
        </span>
        <span v-if="showSecretBadge" class="secret-badge">🏆 彩蛋猎人</span>
      </template>
      
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="用户名">
          <a-input 
            v-model:value="searchForm.username" 
            placeholder="请输入用户名" 
            allowClear
            @keyup="handleSearchInputKeyup"
          />
        </a-form-item>
        <a-form-item label="真实姓名">
          <a-input v-model:value="searchForm.realName" placeholder="请输入真实姓名" allowClear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button v-if="darkEggMode" type="primary" ghost @click="darkEggMode = false">
              退出暗黑彩蛋模式
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
      
      <!-- 操作按钮 -->
      <div class="table-actions">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增用户
        </a-button>
        <span v-if="konamiActivated" class="konami-hint">
          🎮 Konami Code 已激活！
        </span>
      </div>
      
      <!-- 用户表格 -->
      <a-table 
        :dataSource="userList" 
        :columns="columns" 
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :customRow="customRow"
        rowKey="id"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'username'">
            <span 
              class="username-cell"
              :class="{ 'lucky-user': luckyUserIndex === index && showLuckyUser }"
            >
              {{ record.username }}
              <span v-if="luckyUserIndex === index && showLuckyUser" class="lucky-badge">
                🍀 幸运用户
              </span>
            </span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'gender'">
            <span 
              class="gender-cell"
              @mouseenter="handleGenderHover(record.gender, record.id)"
              @mouseleave="handleGenderLeave(record.id)"
            >
              {{ record.gender === 1 ? '男' : record.gender === 2 ? '女' : '未知' }}
              <span v-if="activeGenderEmoji[record.id]" class="gender-emoji">{{ activeGenderEmoji[record.id] }}</span>
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleAssignRole(record)">分配角色</a-button>
              <a-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 新增/编辑弹窗 -->
    <a-modal 
      v-model:open="modalVisible" 
      :title="modalTitle"
      @ok="handleSubmit"
      :confirmLoading="submitLoading"
      :class="{ 'modal-dance': modalDance }"
    >
      <a-form 
        ref="formRef"
        :model="userForm" 
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="userForm.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="userForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="userForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="性别" name="gender">
          <a-radio-group v-model:value="userForm.gender">
            <a-radio :value="1">男</a-radio>
            <a-radio :value="2">女</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="userForm.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- 分配角色弹窗 -->
    <a-modal 
      v-model:open="roleModalVisible" 
      title="分配角色"
      @ok="handleRoleSubmit"
    >
      <a-checkbox-group v-model:value="selectedRoles">
        <a-row>
          <a-col :span="12" v-for="role in roleList" :key="role.id">
            <a-checkbox :value="role.id">{{ role.roleName }}</a-checkbox>
          </a-col>
        </a-row>
      </a-checkbox-group>
    </a-modal>

    <!-- 彩蛋提示弹窗 -->
    <a-modal 
      v-model:open="easterEggModalVisible" 
      :title="easterEggTitle"
      :footer="null"
      class="easter-egg-modal"
    >
      <div class="easter-egg-content">
        <p>{{ easterEggMessage }}</p>
        <div v-if="easterEggEmoji" class="big-emoji">{{ easterEggEmoji }}</div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, assignRoles } from '@/api/user'
import { getAllRoles } from '@/api/role'

// ========== 基础状态 ==========
const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const roleModalVisible = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增用户')
const formRef = ref(null)
const currentUserId = ref(null)

const searchForm = reactive({
  username: '',
  realName: '',
  status: undefined
})

const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 1,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入真实姓名' }]
}

const userList = ref([])
const roleList = ref([])

const selectedRoles = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '性别', dataIndex: 'gender', key: 'gender' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 200 }
]

// ========== 彩蛋状态 ==========
const titleClickCount = ref(0)
const titleShake = ref(false)
const rainbowMode = ref(false)
const darkEggMode = ref(false)
const konamiActivated = ref(false)
const showConfetti = ref(false)
const showStars = ref(false)
const showSecretBadge = ref(false)
const showLuckyUser = ref(false)
const luckyUserIndex = ref(-1)
const activeGenderEmoji = reactive({})
const modalDance = ref(false)
const easterEggModalVisible = ref(false)
const easterEggTitle = ref('')
const easterEggMessage = ref('')
const easterEggEmoji = ref('')

// 定时器引用（用于清理）
let titleClickTimer = null
let easterEggTimer = null
const timeoutHandles = []

const titleText = computed(() => {
  if (darkEggMode.value) return '🌙 暗黑用户管理'
  if (rainbowMode.value) return '🌈 彩虹用户管理'
  return '用户管理'
})

// 彩蛋追踪
const discoveredEasterEggs = ref(new Set())

function discoverEasterEgg(name) {
  if (!discoveredEasterEggs.value.has(name)) {
    discoveredEasterEggs.value.add(name)
    if (discoveredEasterEggs.value.size >= 5) {
      showSecretBadge.value = true
      showEasterEggModal('🏆 彩蛋猎人', '恭喜你发现了所有彩蛋！你是一名真正的探索者！', '🏆')
    }
  }
}

function showEasterEggModal(title, msg, emoji = '') {
  easterEggTitle.value = title
  easterEggMessage.value = msg
  easterEggEmoji.value = emoji
  easterEggModalVisible.value = true
  if (easterEggTimer) clearTimeout(easterEggTimer)
  easterEggTimer = setTimeout(() => {
    easterEggModalVisible.value = false
  }, 3000)
}

// ========== 彩蛋1: 标题连续点击 ==========
function handleTitleClick() {
  titleClickCount.value++
  
  if (titleClickCount.value === 5) {
    // 触发彩虹模式
    rainbowMode.value = true
    titleShake.value = true
    discoverEasterEgg('rainbow')
    showEasterEggModal('🌈 彩虹模式', '你发现了彩虹模式！页面变得五彩斑斓~', '🌈')
    const t1 = setTimeout(() => {
      titleShake.value = false
    }, 1000)
    timeoutHandles.push(t1)
    const t2 = setTimeout(() => {
      rainbowMode.value = false
      titleClickCount.value = 0
    }, 10000)
    timeoutHandles.push(t2)
  } else if (titleClickCount.value === 10) {
    // 触发暗黑模式
    darkEggMode.value = true
    discoverEasterEgg('darkMode')
    showEasterEggModal('🌙 暗黑彩蛋模式', '欢迎来到暗黑世界...', '🌙')
    titleClickCount.value = 0
  }
  
  // 3秒内没有继续点击则重置
  if (titleClickTimer) clearTimeout(titleClickTimer)
  titleClickTimer = setTimeout(() => {
    if (titleClickCount.value < 5) {
      titleClickCount.value = 0
    }
  }, 3000)
}

// ========== 彩蛋2: 标题双击 ==========
function handleTitleDblClick() {
  showConfetti.value = true
  discoverEasterEgg('confetti')
  showEasterEggModal('🎉 撒花庆祝', '双击标题触发撒花特效！', '🎉')
  setTimeout(() => {
    showConfetti.value = false
  }, 3000)
}

// ========== 彩蛋3: Konami Code ==========
const konamiSequence = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight', 'KeyB', 'KeyA']
const konamiIndex = ref(0)

function handleKonamiCode(e) {
  if (e.code === konamiSequence[konamiIndex.value]) {
    konamiIndex.value++
    if (konamiIndex.value === konamiSequence.length) {
      konamiActivated.value = true
      showStars.value = true
      discoverEasterEgg('konami')
      showEasterEggModal('🎮 Konami Code', '↑↑↓↓←→←→BA - 经典作弊码激活！', '🎮')
      const t = setTimeout(() => {
        showStars.value = false
      }, 5000)
      timeoutHandles.push(t)
      konamiIndex.value = 0
    }
  } else {
    konamiIndex.value = 0
  }
}

// ========== 彩蛋4: 搜索框输入特定文字 ==========
const secretWords = ['hello', 'admin', 'secret', 'matrix', 'hack']
let searchBuffer = ''

function handleSearchInputKeyup(e) {
  if (e.key.length === 1) {
    searchBuffer += e.key.toLowerCase()
    if (searchBuffer.length > 20) {
      searchBuffer = searchBuffer.slice(-20)
    }
    
    for (const word of secretWords) {
      if (searchBuffer.includes(word)) {
        triggerSecretWordEgg(word)
        searchBuffer = ''
        break
      }
    }
  }
}

function triggerSecretWordEgg(word) {
  switch(word) {
    case 'hello':
      discoverEasterEgg('hello')
      showEasterEggModal('👋 你好！', 'Hello, World! 你好，世界！', '👋')
      break
    case 'admin':
      discoverEasterEgg('admin')
      modalDance.value = true
      showEasterEggModal('🔐 管理员彩蛋', '欢迎回来，管理员大人！', '🔐')
      const t1 = setTimeout(() => modalDance.value = false, 2000)
      timeoutHandles.push(t1)
      break
    case 'secret':
      discoverEasterEgg('secret')
      // 修复：检查用户列表是否为空
      if (userList.value.length > 0) {
        showLuckyUser.value = true
        luckyUserIndex.value = Math.floor(Math.random() * userList.value.length)
        showEasterEggModal('🔮 神秘彩蛋', '幸运用户已选中！', '🔮')
        const t2 = setTimeout(() => showLuckyUser.value = false, 5000)
        timeoutHandles.push(t2)
      } else {
        showEasterEggModal('🔮 神秘彩蛋', '暂无用户数据，无法选中幸运用户', '🔮')
      }
      break
    case 'matrix':
      discoverEasterEgg('matrix')
      darkEggMode.value = true
      showEasterEggModal('💊 矩阵模式', '欢迎来到矩阵...', '💊')
      break
    case 'hack':
      discoverEasterEgg('hack')
      showConfetti.value = true
      showStars.value = true
      rainbowMode.value = true
      showEasterEggModal('💀 黑客模式', 'Hacking... 成功！', '💀')
      const t3 = setTimeout(() => {
        showConfetti.value = false
        showStars.value = false
        rainbowMode.value = false
      }, 5000)
      timeoutHandles.push(t3)
      break
  }
}

// ========== 彩蛋5: 鼠标悬停性别 ==========
function handleGenderHover(gender, userId) {
  if (gender === 1) {
    activeGenderEmoji[userId] = '👨'
  } else if (gender === 2) {
    activeGenderEmoji[userId] = '👩'
  } else {
    activeGenderEmoji[userId] = '🤖'
  }
}

function handleGenderLeave(userId) {
  delete activeGenderEmoji[userId]
}

// ========== 彩蛋6: 表格行双击 ==========
function customRow(record, index) {
  return {
    ondblclick: () => {
      if (index === 0) {
        discoverEasterEgg('firstRow')
        showEasterEggModal('🥇 第一行彩蛋', '双击第一行，发现隐藏彩蛋！', '🥇')
      } else if (index === userList.value.length - 1) {
        discoverEasterEgg('lastRow')
        showEasterEggModal('🥉 最后一行彩蛋', '坚持到最后的人会有惊喜！', '🥉')
      }
    }
  }
}

// ========== 撒花样式 ==========
function confettiStyle(i) {
  const colors = ['#ff0000', '#00ff00', '#0000ff', '#ffff00', '#ff00ff', '#00ffff']
  return {
    left: `${Math.random() * 100}%`,
    backgroundColor: colors[i % colors.length],
    animationDelay: `${Math.random() * 2}s`,
    animationDuration: `${2 + Math.random() * 2}s`
  }
}

// ========== 星星样式 ==========
function starStyle(i) {
  return {
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 1}s`,
    animationDuration: `${1 + Math.random()}s`
  }
}

// ========== 基础功能方法 ==========
async function fetchUserList() {
  loading.value = true
  try {
    const res = await getUserList({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
    userList.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchUserList()
}

function handleReset() {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = undefined
  handleSearch()
}

function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUserList()
}

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增用户'
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    gender: 1,
    status: 1
  })
  modalVisible.value = true
}

function handleEdit(record) {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  Object.assign(userForm, record)
  modalVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await updateUser(userForm)
      message.success('修改成功')
    } else {
      await addUser(userForm)
      message.success('新增成功')
    }
    
    modalVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(record) {
  try {
    await deleteUser(record.id)
    message.success('删除成功')
    fetchUserList()
  } catch (error) {
    console.error(error)
  }
}

function handleAssignRole(record) {
  currentUserId.value = record.id
  selectedRoles.value = []
  roleModalVisible.value = true
}

async function handleRoleSubmit() {
  try {
    await assignRoles(currentUserId.value, selectedRoles.value)
    message.success('分配角色成功')
    roleModalVisible.value = false
  } catch (error) {
    console.error(error)
  }
}

async function fetchRoleList() {
  try {
    const res = await getAllRoles()
    roleList.value = res || []
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  fetchUserList()
  fetchRoleList()
  // 监听键盘事件（Konami Code）
  window.addEventListener('keydown', handleKonamiCode)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKonamiCode)
  // 清理所有定时器
  if (titleClickTimer) clearTimeout(titleClickTimer)
  if (easterEggTimer) clearTimeout(easterEggTimer)
  timeoutHandles.forEach(handle => clearTimeout(handle))
})
</script>

<style lang="less" scoped>
.user-manage {
  position: relative;
  transition: all 0.5s ease;
  
  &.rainbow-mode {
    animation: rainbow-bg 3s linear infinite;
  }
  
  &.dark-egg-mode {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
    color: #e0e0e0;
    padding: 20px;
    border-radius: 12px;
    
    :deep(.ant-card) {
      background: rgba(30, 30, 50, 0.9);
      border-color: #4a4a6a;
      
      .ant-card-head {
        background: transparent;
        border-color: #4a4a6a;
        color: #e0e0e0;
      }
      
      .ant-table {
        background: transparent;
        color: #e0e0e0;
        
        .ant-table-thead > tr > th {
          background: rgba(50, 50, 80, 0.8);
          color: #e0e0e0;
        }
        
        .ant-table-tbody > tr > td {
          border-color: #4a4a6a;
        }
        
        .ant-table-tbody > tr:hover > td {
          background: rgba(70, 70, 100, 0.5);
        }
      }
      
      .ant-input, .ant-select-selector {
        background: rgba(50, 50, 80, 0.8);
        border-color: #4a4a6a;
        color: #e0e0e0;
      }
    }
  }
  
  .title-egg {
    cursor: pointer;
    user-select: none;
    transition: all 0.3s ease;
    
    &:hover {
      text-shadow: 0 0 10px rgba(24, 144, 255, 0.5);
    }
    
    &.title-shake {
      animation: shake 0.5s ease-in-out;
    }
  }
  
  .secret-badge {
    margin-left: 10px;
    padding: 2px 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    font-size: 12px;
    color: white;
    animation: pulse 2s infinite;
  }
  
  .konami-hint {
    margin-left: 16px;
    color: #52c41a;
    font-size: 12px;
    animation: blink 1s infinite;
  }
  
  .username-cell {
    transition: all 0.3s ease;
    
    &.lucky-user {
      color: #faad14;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(250, 173, 20, 0.5);
    }
    
    .lucky-badge {
      margin-left: 8px;
      font-size: 12px;
    }
  }
  
  .gender-cell {
    cursor: default;
    
    .gender-emoji {
      margin-left: 5px;
      animation: bounce 0.5s ease;
    }
  }
  
  .search-form {
    margin-bottom: 16px;
  }
  
  .table-actions {
    margin-bottom: 16px;
  }
}

// 撒花特效
.confetti-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
  
  .confetti {
    position: absolute;
    width: 10px;
    height: 10px;
    top: -10px;
    animation: confetti-fall linear forwards;
    border-radius: 2px;
  }
}

@keyframes confetti-fall {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(720deg);
    opacity: 0;
  }
}

// 星星特效
.stars-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
  
  .star {
    position: absolute;
    width: 20px;
    height: 20px;
    font-size: 20px;
    animation: star-twinkle ease-in-out infinite;
    
    &::before {
      content: '⭐';
    }
  }
}

@keyframes star-twinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

// 彩虹背景动画
@keyframes rainbow-bg {
  0% { background-color: rgba(255, 0, 0, 0.1); }
  16% { background-color: rgba(255, 165, 0, 0.1); }
  33% { background-color: rgba(255, 255, 0, 0.1); }
  50% { background-color: rgba(0, 255, 0, 0.1); }
  66% { background-color: rgba(0, 127, 255, 0.1); }
  83% { background-color: rgba(139, 0, 255, 0.1); }
  100% { background-color: rgba(255, 0, 0, 0.1); }
}

// 抖动动画
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px) rotate(-5deg); }
  75% { transform: translateX(10px) rotate(5deg); }
}

// 脉冲动画
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

// 闪烁动画
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// 弹跳动画
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

// 弹窗舞蹈动画
:deep(.modal-dance) {
  .ant-modal-content {
    animation: dance 0.5s ease-in-out;
  }
}

@keyframes dance {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-3deg); }
  75% { transform: rotate(3deg); }
}

// 彩蛋弹窗样式
.easter-egg-modal {
  :deep(.ant-modal-content) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    
    .ant-modal-header {
      background: transparent;
      border-bottom: 1px solid rgba(255, 255, 255, 0.3);
      
      .ant-modal-title {
        color: white;
        font-size: 18px;
      }
    }
    
    .ant-modal-close {
      color: white;
    }
  }
  
  .easter-egg-content {
    text-align: center;
    padding: 20px;
    
    p {
      font-size: 16px;
      margin-bottom: 15px;
    }
    
    .big-emoji {
      font-size: 60px;
      animation: bounce 0.5s ease infinite;
    }
  }
}
</style>