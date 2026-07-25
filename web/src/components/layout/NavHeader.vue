<template>
  <n-layout-header
    bordered
    style="padding: 0 20px; display: flex; align-items: center; height: 48px; background: #fff"
  >
    <n-gradient-text type="primary" size="18" style="font-weight: bold; cursor: pointer" @click="$router.push('/chat')">
      Vela IM
    </n-gradient-text>
    <div style="flex: 1" />
    <n-space>
      <n-button
        :type="$router.currentRoute.value.path === '/chat' ? 'primary' : 'quaternary'"
        size="small"
        @click="$router.push('/chat')"
      >
        会话
      </n-button>
      <n-button
        :type="$router.currentRoute.value.path === '/contacts' ? 'primary' : 'quaternary'"
        size="small"
        @click="$router.push('/contacts')"
      >
        通讯录
      </n-button>
      <n-dropdown trigger="click" :options="menuOptions" @select="handleMenu">
        <n-avatar round :size="32" color="#2080f0" style="cursor: pointer">{{ userId[0] }}</n-avatar>
      </n-dropdown>
    </n-space>
  </n-layout-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { PersonOutline } from '@vicons/ionicons5'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const userId = computed(() => userStore.userId || '用户')

const menuOptions = [
  { label: '个人资料', key: 'profile' },
  { label: '退出登录', key: 'logout' }
]

function handleMenu(key) {
  if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>
