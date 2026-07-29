import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '../api/user'
import { STORAGE_KEYS } from '../utils/constants'

export const useUserStore = defineStore('user', () => {
  const userId = ref(localStorage.getItem(STORAGE_KEYS.USER_ID) || '')
  const userInfo = ref(null)
  const isLoggedIn = ref(!!localStorage.getItem(STORAGE_KEYS.TOKEN))

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo(userId.value)
      userInfo.value = res.data
    } catch (e) {
      console.warn('获取用户信息失败', e)
    }
  }

  function loginSuccess(id, token) {
    userId.value = id
    isLoggedIn.value = true
    localStorage.setItem(STORAGE_KEYS.TOKEN, token)
    localStorage.setItem(STORAGE_KEYS.USER_ID, id)
  }

  function logout() {
    userId.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem(STORAGE_KEYS.TOKEN)
    localStorage.removeItem(STORAGE_KEYS.USER_ID)
  }

  return { userId, userInfo, isLoggedIn, fetchUserInfo, loginSuccess, logout }
})
