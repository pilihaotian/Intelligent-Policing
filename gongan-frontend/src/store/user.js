import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo, getMenus } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])

  const isLoggedIn = computed(() => !!token.value)

  async function doLogin(credentials) {
    const res = await login(credentials)
    // 后端返回accessToken，前端使用token
    token.value = res.accessToken || res.token
    localStorage.setItem('token', token.value)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res
  }

  async function fetchMenus() {
    const res = await getMenus()
    menus.value = res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    menus,
    isLoggedIn,
    doLogin,
    fetchUserInfo,
    fetchMenus,
    logout
  }
})