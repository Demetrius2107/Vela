import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { VELA } from '../utils/constants'

/**
 * 功能开关 Store
 * 应用启动时拉取服务端 FeatureFlag，控制 UI 元素显隐。
 */
export const useFeatureFlagStore = defineStore('featureFlags', () => {
  const flags = ref({})
  const loaded = ref(false)

  async function loadFlags() {
    const userId = localStorage.getItem('vela_user_id') || 'test_user'
    try {
      const res = await axios.get(`${VELA.API_URL}/v1/feature/flags`, {
        params: { appId: 1, userId }
      })
      if (res.data.code === 200) {
        flags.value = res.data.data || {}
        loaded.value = true
      }
    } catch (e) {
      // 离线时全部放行
      flags.value = {}
      loaded.value = true
    }
  }

  function isEnabled(key) {
    if (!loaded.value) return true // 未加载时默认放行
    return flags.value[key] !== false
  }

  return { flags, loaded, loadFlags, isEnabled }
})
