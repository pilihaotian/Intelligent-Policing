<template>
  <a-layout class="main-layout">
    <!-- 侧边栏 -->
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      :trigger="null" 
      collapsible
      theme="dark"
    >
      <div class="logo">
                  <span v-if="!collapsed">智慧公安管理系统</span>        <span v-else>公安</span>
      </div>
      
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-sub-menu key="system">
          <template #icon><SettingOutlined /></template>
          <template #title>系统管理</template>
          <a-menu-item key="system-user">用户管理</a-menu-item>
          <a-menu-item key="system-role">角色管理</a-menu-item>
          <a-menu-item key="system-org">机构管理</a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="ops-risk">
          <template #icon><FileTextOutlined /></template>
          <template #title>执法办案</template>
          <a-menu-item key="ops-document">案件信息填报</a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="ai-assistant">
          <template #icon><RobotOutlined /></template>
          <template #title>智能助手</template>
          <a-menu-item key="ai-kb">知识库管理</a-menu-item>
          <a-menu-item key="ai-chat">智能问答</a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="anti-fraud">
          <template #icon><SecurityScanOutlined /></template>
          <template #title>刑侦研判</template>
          <a-menu-item key="fraud-customer">重点人员</a-menu-item>
          <a-menu-item key="fraud-transaction">资金流水</a-menu-item>
          <a-menu-item key="fraud-analysis">案件分析</a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="aml">
          <template #icon><EyeOutlined /></template>
          <template #title>治安管理</template>
          <a-menu-item key="aml-dd">人员核查</a-menu-item>
          <a-menu-item key="aml-suspicious">线索管理</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    
    <a-layout>
      <!-- 头部 -->
      <a-layout-header class="header">
        <div class="header-left">
          <MenuUnfoldOutlined 
            v-if="collapsed" 
            class="trigger" 
            @click="collapsed = !collapsed"
          />
          <MenuFoldOutlined 
            v-else 
            class="trigger" 
            @click="collapsed = !collapsed"
          />
        </div>
        
        <div class="header-right">
          <a-dropdown>
            <div class="user-info">
              <a-avatar :src="userInfo?.avatar" :size="32">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span class="username">{{ userInfo?.realName || '用户' }}</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleLogout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <!-- 内容区域 -->
      <a-layout-content class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
    
    <!-- 智能导航浮动按钮 -->
    <div class="smart-nav-float-btn" @click="openSmartNav">
      <RobotOutlined class="float-icon" />
      <span class="pulse-ring"></span>
    </div>
    
    <!-- 智能导航侧边栏 -->
    <SmartNavDrawer ref="smartNavRef" />
  </a-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import SmartNavDrawer from '@/components/SmartNavDrawer.vue'
import {
  SettingOutlined,
  FileTextOutlined,
  RobotOutlined,
  SecurityScanOutlined,
  EyeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const selectedKeys = ref(['system-user'])
const smartNavRef = ref(null)

const userInfo = computed(() => userStore.userInfo)

const routeMap = {
  'system-user': '/system/user',
  'system-role': '/system/role',
  'system-org': '/system/org',
  'ops-document': '/ops-risk/document',
  'ai-kb': '/ai-assistant/kb',
  'ai-chat': '/ai-assistant/chat',
  'fraud-customer': '/anti-fraud/customer',
  'fraud-transaction': '/anti-fraud/transaction',
  'fraud-analysis': '/anti-fraud/analysis',
  'aml-dd': '/aml/due-diligence',
  'aml-suspicious': '/aml/suspicious'
}

function handleMenuClick({ key }) {
  const path = routeMap[key]
  if (path) {
    router.push(path)
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function openSmartNav() {
  smartNavRef.value?.open()
}
</script>

<style lang="less" scoped>
.main-layout {
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.1);
}

.header {
  background: #fff;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  
  &:hover {
    color: #1890ff;
  }
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  
  .username {
    margin-left: 8px;
  }
}

.content {
  margin: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 智能导航浮动按钮
.smart-nav-float-btn {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  .float-icon {
    font-size: 24px;
    color: white;
  }
  
  // 脉冲动画
  .pulse-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: rgba(102, 126, 234, 0.4);
    animation: pulse 2s ease-out infinite;
    pointer-events: none;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.8);
    opacity: 0;
  }
}
</style>
