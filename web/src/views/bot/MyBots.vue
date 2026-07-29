<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <NavHeader />
    <div style="max-width: 1000px; margin: 0 auto; padding: 24px 20px">
      <!-- 标题 -->
      <div style="margin-bottom: 24px">
        <div style="font-size: 24px; font-weight: 700; color: #1a1a2e">📦 我的 Bot</div>
        <div style="font-size: 13px; color: #888; margin-top: 4px">管理已安装的机器人</div>
      </div>

      <!-- 加载 -->
      <div v-if="loading" style="text-align: center; padding: 60px"><n-spin size="large" /></div>

      <!-- 空状态 -->
      <div v-else-if="list.length === 0" style="text-align: center; padding: 80px 20px; background: #fff; border-radius: 12px;">
        <div style="font-size: 48px; margin-bottom: 12px">🤖</div>
        <div style="font-size: 16px; color: #333; font-weight: 600; margin-bottom: 8px">还没有安装任何 Bot</div>
        <div style="font-size: 13px; color: #888; margin-bottom: 20px">去市场发现好用的机器人吧</div>
        <n-button type="primary" round @click="$router.push('/bot/market')">去 Bot 市场</n-button>
      </div>

      <!-- Bot 列表 -->
      <div v-else style="display: grid; gap: 12px">
        <div v-for="bot in list" :key="bot.id" style="background: #fff; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px;">
          <n-avatar round :size="44" color="#2080f0" style="font-size: 18px">{{ bot.botName?.[0] || 'B' }}</n-avatar>
          <div style="flex: 1; min-width: 0">
            <div style="font-weight: 600; font-size: 15px; color: #333; display: flex; align-items: center; gap: 6px">
              {{ bot.botName }}
              <n-tag v-if="bot.category" size="tiny" round bordered>{{ bot.category }}</n-tag>
              <n-tag v-if="bot.status === 1" size="tiny" type="success" round>在线</n-tag>
            </div>
            <div style="font-size: 12px; color: #999">@{{ bot.botId }}</div>
            <div v-if="bot.description" style="font-size: 13px; color: #666; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ bot.description }}</div>
          </div>
          <div style="display: flex; gap: 8px; flex-shrink: 0">
            <n-button size="small" round type="primary" @click="goChat(bot)">💬 发消息</n-button>
            <n-button size="small" round quaternary @click="goConfig(bot)">⚙️ 配置</n-button>
            <n-popconfirm @positive-click="uninstallBot(bot)">
              <template #trigger><n-button size="small" quaternary round type="error">卸载</n-button></template>
              确认卸载 {{ bot.botName }}？
            </n-popconfirm>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import axios from 'axios'
import NavHeader from '../../components/layout/NavHeader.vue'
import { useUserStore } from '../../stores/user'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => userStore.userId || 'test_user')
const list = ref([])
const loading = ref(false)

async function loadMyBots() {
  loading.value = true
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/bot/market/my`, { params: { appId: 1, userId: userId.value } })
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

async function uninstallBot(bot) {
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/bot/market/uninstall`, null, {
      params: { appId: 1, userId: userId.value, botId: bot.botId }
    })
    if (res.data.code === 200) {
      msg.success(`已卸载 ${bot.botName}`)
      list.value = list.value.filter(b => b.botId !== bot.botId)
    } else {
      msg.error(res.data.msg || '卸载失败')
    }
  } catch (e) { msg.error('卸载失败') }
}

function goChat(bot) {
  // 跳转到聊天页并打开与该 Bot 的会话
  router.push(`/chat?botId=${bot.botId}&botName=${encodeURIComponent(bot.botName)}`)
}

function goConfig(bot) {
  router.push(`/bot/detail/${bot.id}`)
}

onMounted(loadMyBots)
</script>
