<template>
  <div style="min-height: 100vh; background: #f5f7fa">
    <NavHeader />
    <div style="max-width: 800px; margin: 0 auto; padding: 24px 20px">
      <!-- 返回 -->
      <n-button quaternary size="small" @click="$router.back()" style="margin-bottom: 16px">← 返回</n-button>

      <div v-if="loading" style="text-align: center; padding: 60px"><n-spin size="large" /></div>

      <div v-else-if="!bot" style="text-align: center; padding: 80px">
        <div style="font-size: 48px; margin-bottom: 12px">🤖</div>
        <div style="color: #888">Bot 不存在</div>
      </div>

      <template v-else>
        <!-- Bot 基本信息卡片 -->
        <n-card style="border-radius: 12px; margin-bottom: 16px">
          <div style="display: flex; align-items: center; gap: 16px">
            <n-avatar round :size="64" color="#722ed1" style="font-size: 28px">{{ bot.botName?.[0] || 'B' }}</n-avatar>
            <div style="flex: 1">
              <div style="font-size: 20px; font-weight: 700; color: #333; display: flex; align-items: center; gap: 8px">
                {{ bot.botName }}
                <n-tag v-if="bot.category" size="tiny" round>{{ bot.category }}</n-tag>
                <n-tag :type="bot.status === 1 ? 'success' : 'error'" size="tiny" round>
                  {{ bot.status === 1 ? '已启用' : '已禁用' }}
                </n-tag>
              </div>
              <div style="font-size: 12px; color: #999; margin-top: 4px">@{{ bot.botId }}</div>
              <div v-if="bot.description" style="font-size: 13px; color: #666; margin-top: 6px">{{ bot.description }}</div>
            </div>
          </div>
        </n-card>

        <!-- Webhook 配置 -->
        <n-card title="Webhook 配置" style="border-radius: 12px; margin-bottom: 16px">
          <n-form label-placement="top">
            <n-form-item label="Webhook 地址">
              <n-input v-model:value="webhookUrl" placeholder="https://your-bot-server.com/webhook" />
            </n-form-item>
            <n-button size="small" type="primary" @click="updateWebhook" :loading="savingWebhook">保存</n-button>
          </n-form>
        </n-card>

        <!-- API Key -->
        <n-card title="API Key" style="border-radius: 12px; margin-bottom: 16px">
          <n-space vertical>
            <n-input :value="bot.apiKey || '---'" disabled>
              <template #suffix>
                <n-button quaternary size="tiny" @click="copyApiKey">复制</n-button>
              </template>
            </n-input>
            <n-space justify="space-between" align="center">
              <span style="font-size: 12px; color: #999">用于验证 Bot 请求身份</span>
              <n-popconfirm @positive-click="regenKey">
                <template #trigger><n-button size="tiny" type="warning" quaternary>重新生成</n-button></template>
                重新生成后旧的 API Key 将失效，确认继续？
              </n-popconfirm>
            </n-space>
          </n-space>
        </n-card>

        <!-- 可用指令 -->
        <n-card title="可用指令" style="border-radius: 12px; margin-bottom: 16px">
          <n-table :single-line="true" size="small">
            <thead><tr><th>指令</th><th>说明</th><th>示例</th></tr></thead>
            <tbody>
              <tr><td><n-tag size="tiny">/start</n-tag></td><td>启动 Bot</td><td>/start</td></tr>
              <tr><td><n-tag size="tiny">/help</n-tag></td><td>查看帮助</td><td>/help</td></tr>
              <tr><td><n-tag size="tiny">/ping</n-tag></td><td>测试 Bot 在线状态</td><td>/ping</td></tr>
              <tr><td><n-tag size="tiny">/echo</n-tag></td><td>回声测试</td><td>/echo 你好</td></tr>
              <tr><td colspan="3" style="text-align:center; color:#999">更多指令由 Bot Webhook 自定义</td></tr>
            </tbody>
          </n-table>
        </n-card>

        <!-- 删除 Bot -->
        <n-card style="border-radius: 12px">
          <n-space align="center" justify="space-between">
            <div>
              <div style="font-weight: 600; color: #d03050">危险操作</div>
              <div style="font-size: 12px; color: #999">删除后将无法恢复</div>
            </div>
            <n-popconfirm @positive-click="deleteBot">
              <template #trigger><n-button type="error" size="small">删除 Bot</n-button></template>
              确认永久删除 {{ bot.botName }}？
            </n-popconfirm>
          </n-space>
        </n-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import NavHeader from '../../components/layout/NavHeader.vue'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const route = useRoute()
const router = useRouter()

const bot = ref(null)
const loading = ref(false)
const webhookUrl = ref('')
const savingWebhook = ref(false)

async function loadBot() {
  const botId = route.params.id
  if (!botId) return
  loading.value = true
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/bot/get`, { params: { botId } })
    if (res.data.code === 200) {
      bot.value = res.data.data
      webhookUrl.value = res.data.data.webhookUrl || ''
    } else { msg.error('加载失败') }
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

async function updateWebhook() {
  if (!bot.value) return
  savingWebhook.value = true
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/bot/update-webhook`, null, {
      params: { botId: bot.value.id, webhookUrl: webhookUrl.value }
    })
    if (res.data.code === 200) { msg.success('Webhook 已更新') }
    else { msg.error(res.data.msg || '更新失败') }
  } catch (e) { msg.error('更新失败') }
  finally { savingWebhook.value = false }
}

function copyApiKey() {
  if (bot.value?.apiKey) {
    navigator.clipboard.writeText(bot.value.apiKey).then(() => msg.success('已复制 API Key'))
  }
}

async function regenKey() {
  if (!bot.value) return
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/bot/regen-key`, null, { params: { botId: bot.value.id } })
    if (res.data.code === 200) {
      bot.value.apiKey = res.data.data
      msg.success('API Key 已重新生成')
    } else { msg.error(res.data.msg || '操作失败') }
  } catch (e) { msg.error('操作失败') }
}

async function deleteBot() {
  if (!bot.value) return
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/bot/delete`, null, { params: { botId: bot.value.id } })
    if (res.data.code === 200) { msg.success('已删除'); router.push('/bot/my') }
    else { msg.error(res.data.msg || '删除失败') }
  } catch (e) { msg.error('删除失败') }
}

onMounted(loadBot)
</script>
