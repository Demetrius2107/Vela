<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <NavHeader />
    <div style="max-width: 1200px; margin: 0 auto; padding: 24px 20px">
      <!-- 标题 -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px">
        <div>
          <div style="font-size: 24px; font-weight: 700; color: #1a1a2e">🤖 Bot 市场</div>
          <div style="font-size: 13px; color: #888; margin-top: 4px">发现好用的机器人，提升聊天效率</div>
        </div>
      </div>

      <!-- 搜索框 -->
      <div style="margin-bottom: 20px">
        <n-input v-model:value="keyword" placeholder="搜索 Bot 名称或描述..." size="large" round clearable @keydown.enter="search">
          <template #prefix><n-icon><search-outline /></n-icon></template>
        </n-input>
      </div>

      <!-- 分类筛选 -->
      <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 24px">
        <n-tag
          v-for="cat in categories"
          :key="cat.category"
          :type="activeCategory === cat.category ? 'primary' : 'default'"
          size="medium"
          round
          bordered
          style="cursor: pointer; padding: 4px 12px"
          @click="switchCategory(cat.category)"
        >
          {{ cat.category }} <span style="color: #999; margin-left: 4px; font-size: 12px">{{ cat.count }}</span>
        </n-tag>
      </div>

      <!-- Bot 列表 -->
      <div v-if="loading" style="text-align: center; padding: 60px"><n-spin size="large" /></div>

      <div v-else-if="list.length === 0" style="text-align: center; padding: 80px 20px">
        <div style="font-size: 48px; margin-bottom: 12px">🔍</div>
        <div style="color: #888">没有找到匹配的 Bot</div>
      </div>

      <div v-else style="display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px">
        <div v-for="bot in list" :key="bot.id" style="background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; flex-direction: column">
          <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 12px">
            <n-avatar round :size="48" color="#2080f0" style="font-size: 20px">{{ bot.botName?.[0] || 'B' }}</n-avatar>
            <div style="flex: 1; min-width: 0">
              <div style="font-weight: 600; font-size: 16px; color: #333; display: flex; align-items: center; gap: 6px">
                {{ bot.botName }}
                <n-tag v-if="bot.category" size="tiny" round bordered style="font-size: 10px">{{ bot.category }}</n-tag>
              </div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">@{{ bot.botId }}</div>
            </div>
          </div>
          <div style="font-size: 13px; color: #666; line-height: 1.6; flex: 1; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
            {{ bot.description || '暂无描述' }}
          </div>
          <div style="display: flex; gap: 8px">
            <n-button
              :type="installedSet.has(bot.botId) ? 'default' : 'primary'"
              size="small"
              round
              :disabled="installedSet.has(bot.botId)"
              :loading="installingBotId === bot.botId"
              @click="installBot(bot)"
              style="flex: 1"
            >
              {{ installedSet.has(bot.botId) ? '✅ 已安装' : '➕ 安装' }}
            </n-button>
            <n-button size="small" quaternary round @click="showDetail(bot)">
              详情
            </n-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bot 详情抽屉 -->
    <n-drawer v-model:show="detailVisible" :width="400" placement="right">
      <n-drawer-content title="Bot 详情" closable>
        <div v-if="detailBot" style="text-align: center; margin-bottom: 24px">
          <n-avatar round :size="64" color="#2080f0" style="font-size: 28px; margin-bottom: 12px">{{ detailBot.botName?.[0] || 'B' }}</n-avatar>
          <div style="font-weight: 700; font-size: 20px; color: #333">{{ detailBot.botName }}</div>
          <div style="font-size: 12px; color: #999">@{{ detailBot.botId }}</div>
          <n-tag v-if="detailBot.category" size="tiny" round bordered style="margin-top: 8px">{{ detailBot.category }}</n-tag>
        </div>
        <div v-if="detailBot" style="margin-bottom: 20px">
          <div style="font-weight: 600; font-size: 14px; color: #333; margin-bottom: 8px">描述</div>
          <div style="font-size: 13px; color: #666; line-height: 1.7">{{ detailBot.description || '暂无描述' }}</div>
        </div>
        <n-button
          v-if="detailBot"
          :type="installedSet.has(detailBot.botId) ? 'default' : 'primary'"
          block
          round
          :disabled="installedSet.has(detailBot.botId)"
          :loading="installingBotId === detailBot.botId"
          @click="installBot(detailBot)"
        >
          {{ installedSet.has(detailBot.botId) ? '✅ 已安装' : '➕ 安装此 Bot' }}
        </n-button>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import axios from 'axios'
import NavHeader from '../../components/layout/NavHeader.vue'
import { useUserStore } from '../../stores/user'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const userId = computed(() => userStore.userId || 'test_user')
const list = ref([])
const categories = ref([])
const loading = ref(false)
const keyword = ref('')
const activeCategory = ref('全部')
const installingBotId = ref('')
const installedSet = ref(new Set())
const detailVisible = ref(false)
const detailBot = ref(null)

async function loadCategories() {
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/bot/market/categories`, { params: { appId: 1 } })
    if (res.data.code === 200) categories.value = res.data.data || []
  } catch (e) { /* ignore */ }
}

async function loadList() {
  loading.value = true
  try {
    const params = { appId: 1 }
    if (activeCategory.value !== '全部') params.category = activeCategory.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await axios.get(`${VELA.API_URL}/v1/bot/market/list`, { params })
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

async function loadInstalled() {
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/bot/market/my`, { params: { appId: 1, userId: userId.value } })
    if (res.data.code === 200) {
      installedSet.value = new Set((res.data.data || []).map(b => b.botId))
    }
  } catch (e) { /* ignore */ }
}

function search() { loadList() }

function switchCategory(cat) {
  activeCategory.value = cat
  loadList()
}

async function installBot(bot) {
  installingBotId.value = bot.botId
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/bot/market/install`, null, {
      params: { appId: 1, userId: userId.value, botId: bot.botId }
    })
    if (res.data.code === 200) {
      msg.success(`已安装 ${bot.botName}`)
      installedSet.value = new Set([...installedSet.value, bot.botId])
    } else {
      msg.error(res.data.msg || '安装失败')
    }
  } catch (e) { msg.error('安装失败') }
  finally { installingBotId.value = '' }
}

function showDetail(bot) {
  detailBot.value = bot
  detailVisible.value = true
}

onMounted(() => {
  loadCategories()
  loadList()
  loadInstalled()
})
</script>
