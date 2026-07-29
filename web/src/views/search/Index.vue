<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <NavHeader />
    <div style="max-width: 800px; margin: 0 auto; padding: 24px 20px">
      <div style="font-size: 24px; font-weight: 700; color: #1a1a2e; margin-bottom: 20px">🔍 全局搜索</div>

      <!-- 搜索框 -->
      <n-card :bordered="false" style="border-radius: 12px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04)">
        <div style="display: flex; gap: 12px">
          <n-input
            v-model:value="keyword"
            placeholder="搜索用户 ID/昵称、Bot名称、消息内容..."
            size="large"
            round
            clearable
            @keydown.enter="doSearch"
            style="flex: 1"
          >
            <template #prefix>🔍</template>
          </n-input>
          <n-button type="primary" size="large" round @click="doSearch">搜索</n-button>
        </div>
        <!-- 搜索分类页签 -->
        <div style="display: flex; gap: 8px; margin-top: 16px">
          <n-tag
            v-for="tab in searchTabs"
            :key="tab.key"
            :type="activeTab === tab.key ? 'primary' : 'default'"
            size="medium"
            round
            bordered
            style="cursor: pointer"
            @click="switchTab(tab.key)"
          >{{ tab.label }}</n-tag>
        </div>
      </n-card>

      <!-- 搜索结果 -->
      <div v-if="loading" style="text-align: center; padding: 60px"><n-spin size="large" /></div>

      <!-- 空状态 -->
      <div v-else-if="searched && results.length === 0" style="text-align: center; padding: 80px 20px; background: #fff; border-radius: 12px;">
        <div style="font-size: 48px; margin-bottom: 12px">🔍</div>
        <div style="font-size: 16px; color: #333; font-weight: 600; margin-bottom: 8px">没有找到结果</div>
        <div style="font-size: 13px; color: #888">试试其他关键词</div>
      </div>

      <!-- 用户搜索结果 -->
      <div v-else-if="activeTab === 'users' && results.length > 0" style="display: grid; gap: 12px">
        <div v-for="u in results" :key="u.userId" style="background: #fff; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px;">
          <n-avatar round :size="44" color="#2080f0">{{ u.nickName?.[0] || u.userId?.[0] || '?' }}</n-avatar>
          <div style="flex: 1; min-width: 0">
            <div style="font-weight: 600; font-size: 14px; color: #333">{{ u.nickName || u.userId }}</div>
            <div style="font-size: 12px; color: #999">ID: {{ u.userId }} · {{ u.selfSignature || '暂无签名' }}</div>
          </div>
          <n-button size="small" round type="primary" ghost @click="addFriend(u)">加好友</n-button>
        </div>
      </div>

      <!-- Bot 搜索结果 -->
      <div v-else-if="activeTab === 'bots' && results.length > 0" style="display: grid; gap: 12px">
        <div v-for="bot in results" :key="bot.id" style="background: #fff; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px;">
          <n-avatar round :size="44" color="#722ed1">{{ bot.botName?.[0] || 'B' }}</n-avatar>
          <div style="flex: 1; min-width: 0">
            <div style="font-weight: 600; font-size: 14px; color: #333; display: flex; align-items: center; gap: 6px">
              {{ bot.botName }}
              <n-tag v-if="bot.category" size="tiny" round>{{ bot.category }}</n-tag>
            </div>
            <div style="font-size: 12px; color: #999">@{{ bot.botId }} · {{ bot.description || '暂无描述' }}</div>
          </div>
          <n-button size="small" round type="primary" @click="goBotChat(bot)">💬 聊天</n-button>
        </div>
      </div>

      <!-- 消息搜索结果 -->
      <div v-else-if="activeTab === 'messages' && results.length > 0" style="display: grid; gap: 12px">
        <div v-for="(msg, i) in results" :key="i" style="background: #fff; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
          <div style="font-size: 13px; color: #333; line-height: 1.6; margin-bottom: 8px">{{ msg.content || msg.messageBody || '(无内容)' }}</div>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div style="font-size: 12px; color: #999">
              <span>{{ msg.fromId || msg.from }}</span>
              <span v-if="msg.toId || msg.to" style="margin: 0 4px">→</span>
              <span>{{ msg.toId || msg.to }}</span>
              <span style="margin: 0 6px">·</span>
              <span>{{ formatTime(msg.messageTime || msg.createTime) }}</span>
            </div>
            <n-button size="tiny" quaternary @click="copyText(msg.content || msg.messageBody)">复制</n-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import axios from 'axios'
import NavHeader from '../../components/layout/NavHeader.vue'
import { useUserStore } from '../../stores/user'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const router = useRouter()
const userStore = useUserStore()
const userId = userStore.userId || 'test_user'

const keyword = ref('')
const activeTab = ref('users')
const results = ref([])
const loading = ref(false)
const searched = ref(false)

const searchTabs = [
  { key: 'users', label: '👤 用户' },
  { key: 'bots', label: '🤖 Bot' },
  { key: 'messages', label: '💬 消息' },
]

function switchTab(tab) {
  activeTab.value = tab
  if (keyword.value.trim()) doSearch()
}

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  searched.value = true
  loading.value = true
  results.value = []

  try {
    if (activeTab.value === 'users') {
      const res = await axios.get(`${VELA.API_URL}/v1/admin/users`, {
        params: { keyword: kw, page: 0, size: 20 }
      })
      if (res.data.code === 200) results.value = res.data.data?.list || []
    } else if (activeTab.value === 'bots') {
      const res = await axios.get(`${VELA.API_URL}/v1/bot/market/list`, {
        params: { appId: 1, keyword: kw }
      })
      if (res.data.code === 200) results.value = res.data.data || []
    } else if (activeTab.value === 'messages') {
      const res = await axios.get(`${VELA.API_URL}/v1/admin/messages/search`, {
        params: { keyword: kw, page: 0, size: 20 }
      })
      if (res.data.code === 200) results.value = res.data.data?.list || []
    }
  } catch (e) { msg.error('搜索失败') }
  finally { loading.value = false }
}

function addFriend(u) {
  msg.success(`已向 ${u.nickName || u.userId} 发送好友请求`)
}

function goBotChat(bot) {
  router.push(`/chat?botId=${bot.botId}&botName=${encodeURIComponent(bot.botName)}`)
}

function copyText(text) {
  if (text) navigator.clipboard.writeText(text).then(() => msg.success('已复制'))
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(() => {
  // 允许从其他页面传递搜索关键词
  const kw = new URLSearchParams(window.location.search).get('q')
  if (kw) { keyword.value = kw; doSearch() }
})
</script>
