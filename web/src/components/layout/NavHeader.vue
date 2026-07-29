<template>
  <n-layout-header
    bordered
    style="padding: 0 20px; display: flex; align-items: center; height: 52px; background: rgba(255,255,255,0.88); backdropFilter: blur(20px) saturate(1.4); WebkitBackdropFilter: blur(20px) saturate(1.4); borderBottom: '1px solid rgba(79,110,247,0.06)'"
  >
    <!-- Logo -->
    <div style="display: flex; align-items: center; gap: 10px; cursor: pointer" @click="$router.push('/chat')">
      <div style="width: 28px; height: 28px; border-radius: 8px; background: linear-gradient(135deg, #4F6EF7, #7C3AED); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 700; boxShadow: 0 2px 8px rgba(79,110,247,0.3)">V</div>
      <span style="font-weight: 700; font-size: 16px; background: linear-gradient(135deg, #4F6EF7, #7C3AED); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text">Vela IM</span>
    </div>

    <div style="flex: 1" />

    <!-- 导航按钮 -->
    <div style="display: flex; align-items: center; gap: 4px; padding: 3px; background: #f5f5f5; border-radius: 10px">
      <div
        v-for="item in navItems" :key="item.key"
        @click="item.click"
        :style="{
          padding: '6px 14px', cursor: 'pointer', borderRadius: '8px', fontSize: '13px', fontWeight: 500,
          display: 'flex', alignItems: 'center', gap: '6px',
          background: isActive(item.path) ? '#fff' : 'transparent',
          color: isActive(item.path) ? '#4F6EF7' : '#666',
          boxShadow: isActive(item.path) ? '0 1px 4px rgba(0,0,0,0.06)' : 'none',
          transition: 'all 0.15s'
        }"
        @mouseenter="$event.currentTarget.style.background = isActive(item.path) ? '#fff' : '#eef0f4'"
        @mouseleave="$event.currentTarget.style.background = isActive(item.path) ? '#fff' : 'transparent'"
      >
        <span>{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <!-- Bot 下拉 -->
    <n-dropdown v-if="featureFlagStore.isEnabled('bot_market')" trigger="click" :options="botMenuOptions" @select="handleBotMenu" style="margin-left: 4px">
      <n-button
        quaternary
        size="small"
        style="borderRadius: 8px; padding: 6px 10px"
        :type="$router.currentRoute.value.path.startsWith('/bot') ? 'primary' : 'default'"
      >
        <template #icon><span>🤖</span></template>
        Bot
      </n-button>
    </n-dropdown>

    <!-- 用户头像 -->
    <n-dropdown trigger="click" :options="menuOptions" @select="handleMenu" style="margin-left: 8px">
      <n-avatar
        round :size="30"
        style="cursor: pointer; background: linear-gradient(135deg, #4F6EF7, #7C3AED); boxShadow: 0 2px 6px rgba(79,110,247,0.25)"
      >{{ userId[0] }}</n-avatar>
    </n-dropdown>
  </n-layout-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useFeatureFlagStore } from '../../stores/featureFlags'

const router = useRouter()
const userStore = useUserStore()
const featureFlagStore = useFeatureFlagStore()
const userId = computed(() => userStore.userId || '用户')

function isActive(path) {
  const cur = router.currentRoute.value.path
  if (typeof path === 'string') return cur === path
  if (Array.isArray(path)) return path.some(p => cur.startsWith(p))
  return false
}

const navItems = [
  { key: 'chat', label: '会话', icon: '💬', path: '/chat', click: () => router.push('/chat') },
  { key: 'contacts', label: '通讯录', icon: '👥', path: '/contacts', click: () => router.push('/contacts') },
  { key: 'office', label: '办公', icon: '📋', path: ['/office', '/knowledge'], click: () => router.push('/office') },
]

const menuOptions = [
  { label: '👤 个人资料', key: 'profile' },
  { label: '⭐ 我的收藏', key: 'favorites' },
  { label: '⚙️ 管理后台', key: 'admin' },
  { type: 'divider' },
  { label: '🚪 退出登录', key: 'logout' }
]

const botMenuOptions = [
  { label: '🤖 Bot 市场', key: 'botMarket' },
  { label: '📦 我的 Bot', key: 'myBots' }
]

function handleMenu(key) {
  if (key === 'profile') router.push('/profile')
  else if (key === 'favorites') router.push('/favorites')
  else if (key === 'admin') router.push('/admin')
  else if (key === 'logout') { userStore.logout(); router.push('/login') }
}

function handleBotMenu(key) {
  if (key === 'botMarket') router.push('/bot/market')
  if (key === 'myBots') router.push('/bot/my')
}
</script>
