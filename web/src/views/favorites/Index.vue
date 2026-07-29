<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <NavHeader />
    <div style="max-width: 900px; margin: 0 auto; padding: 24px 20px">
      <!-- 标题 -->
      <div style="margin-bottom: 24px">
        <div style="font-size: 24px; font-weight: 700; color: #1a1a2e">⭐ 我的收藏</div>
        <div style="font-size: 13px; color: #888; margin-top: 4px">收藏的重要消息，随时查阅</div>
      </div>

      <!-- 加载 -->
      <div v-if="loading" style="text-align: center; padding: 60px"><n-spin size="large" /></div>

      <!-- 空状态 -->
      <div v-else-if="list.length === 0" style="text-align: center; padding: 80px 20px; background: #fff; border-radius: 12px;">
        <div style="font-size: 48px; margin-bottom: 12px">⭐</div>
        <div style="font-size: 16px; color: #333; font-weight: 600; margin-bottom: 8px">还没有收藏任何消息</div>
        <div style="font-size: 13px; color: #888; margin-bottom: 20px">在聊天中右键消息可以收藏</div>
        <n-button type="primary" round @click="$router.push('/chat')">去聊天</n-button>
      </div>

      <!-- 收藏列表 -->
      <div v-else style="display: grid; gap: 12px">
        <div v-for="item in list" :key="item.id" style="background: #fff; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: flex-start; gap: 14px;">
          <div style="font-size: 20px; flex-shrink: 0; margin-top: 2px">⭐</div>
          <div style="flex: 1; min-width: 0">
            <div style="font-size: 13px; color: #333; line-height: 1.6; margin-bottom: 8px; word-break: break-word">{{ item.content }}</div>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div style="font-size: 12px; color: #999">
                <span>来自 {{ item.fromId }}</span>
                <span style="margin: 0 6px">·</span>
                <span>{{ formatTime(item.createTime) }}</span>
              </div>
              <n-button size="tiny" quaternary type="error" @click="handleRemove(item)">删除</n-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" style="display: flex; justify-content: center; margin-top: 24px">
        <n-pagination :page="page" :page-count="pageCount" @update:page="load($event)" />
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
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const pageCount = ref(1)

async function load(p) {
  page.value = p || 1
  loading.value = true
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/favorite/list`, {
      params: { appId: 1, userId: userId.value, page: page.value - 1, size: pageSize.value }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || []
      total.value = res.data.data.total || 0
      pageCount.value = Math.max(1, Math.ceil(total.value / pageSize.value))
    }
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

async function handleRemove(item) {
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/favorite/remove`, null, {
      params: { appId: 1, userId: userId.value, conversationId: item.conversationId, messageTime: item.messageTime }
    })
    if (res.data.code === 200) {
      msg.success('已删除收藏')
      list.value = list.value.filter(i => i.id !== item.id)
      total.value--
      pageCount.value = Math.max(1, Math.ceil(total.value / pageSize.value))
    } else { msg.error(res.data.msg || '操作失败') }
  } catch (e) { msg.error('操作失败') }
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(() => load(1))
</script>
