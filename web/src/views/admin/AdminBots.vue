<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">🤖 Bot管理</div>
      <n-button type="primary" @click="showCreate = true">创建 Bot</n-button>
    </div>

    <n-card style="margin-bottom:16px;border-radius:8px">
      <div style="display:flex;gap:12px;align-items:flex-end">
        <div><label style="font-size:12px;color:#888">关键词</label><n-input v-model:value="keyword" placeholder="搜索Bot名称/ID" clearable style="width:200px" @keydown.enter="load(1)" /></div>
        <n-button type="primary" @click="load(1)">查询</n-button>
      </div>
    </n-card>

    <n-card style="border-radius:8px">
      <n-data-table :columns="columns" :data="list" :loading="loading" :pagination="pagination" :bordered="false" :single-line="true" size="small" />
    </n-card>

    <!-- 创建 Bot 抽屉 -->
    <n-drawer v-model:show="showCreate" :width="420" placement="right">
      <n-drawer-content title="创建 Bot" closable>
        <n-form :model="form" label-placement="top" size="small">
          <n-form-item label="Bot ID（唯一标识）"><n-input v-model:value="form.botId" placeholder="如: weather-bot" /></n-form-item>
          <n-form-item label="Bot 名称"><n-input v-model:value="form.botName" placeholder="如: 天气助手" /></n-form-item>
          <n-form-item label="分类">
            <n-select v-model:value="form.category" :options="categoryOptions" placeholder="选择分类" />
          </n-form-item>
          <n-form-item label="Webhook 地址"><n-input v-model:value="form.webhookUrl" placeholder="https://..." /></n-form-item>
          <n-form-item label="描述"><n-input v-model:value="form.description" type="textarea" rows="3" placeholder="Bot功能描述" /></n-form-item>
          <n-button type="primary" block @click="handleCreate" :loading="creating">创建</n-button>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, h } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const list = ref([])
const loading = ref(false)
const total = ref(0)
const keyword = ref('')
const showCreate = ref(false)
const creating = ref(false)

const categoryOptions = [
  { label: '工具', value: '工具' },
  { label: '娱乐', value: '娱乐' },
  { label: 'AI', value: 'AI' },
  { label: '办公', value: '办公' },
  { label: '资讯', value: '资讯' },
  { label: '其他', value: '其他' },
]

const form = reactive({ botId: '', botName: '', webhookUrl: '', description: '', category: '' })

const pagination = reactive({
  page: 1, pageSize: 20, showSizePicker: true, pageSizes: [10, 20, 50],
  onChange: (p) => load(p),
  onUpdatePageSize: (s) => { pagination.pageSize = s; load(1) }
})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: 'Bot ID', key: 'botId', width: 120 },
  { title: '名称', key: 'botName', width: 120 },
  { title: '分类', key: 'category', width: 80, render: (r) => r.category || '-' },
  { title: 'Webhook', key: 'webhookUrl', ellipsis: { tooltip: true } },
  { title: '状态', key: 'status', width: 80, render: (r) => h('n-tag', { size: 'tiny', type: r.status === 1 ? 'success' : 'error' }, { default: () => r.status === 1 ? '启用' : '禁用' }) },
  { title: 'API Key', key: 'apiKey', width: 140, ellipsis: { tooltip: true } },
  {
    title: '操作', width: 160,
    render: (r) => [
      h('n-button', { size: 'tiny', quaternary: true, style: 'margin-right:8px', onClick: () => handleToggle(r) }, { default: () => r.status === 1 ? '禁用' : '启用' }),
      h('n-button', { size: 'tiny', quaternary: true, type: 'error', onClick: () => handleDelete(r) }, { default: () => '删除' })
    ]
  }
]

async function load(page) {
  loading.value = true; pagination.page = page
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/admin/bots`, {
      headers: { 'X-Admin-Role': localStorage.getItem('vela_admin_role') || 'admin' },
      params: { page: page - 1, size: pagination.pageSize, keyword: keyword.value || undefined }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || []
      total.value = res.data.data.total || 0
      pagination.pageCount = Math.max(1, Math.ceil(total.value / pagination.pageSize))
    }
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

async function handleCreate() {
  if (!form.botId || !form.botName) { msg.warning('请填写 Bot ID 和名称'); return }
  creating.value = true
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/admin/bots/create`, null, {
      headers: { 'X-Admin-Role': localStorage.getItem('vela_admin_role') || 'admin' },
      params: { appId: 1, botId: form.botId, botName: form.botName, webhookUrl: form.webhookUrl || '', description: form.description || '', category: form.category || '' }
    })
    if (res.data.code === 200) {
      msg.success('创建成功'); showCreate.value = false; load(1)
      form.botId = ''; form.botName = ''; form.webhookUrl = ''; form.description = ''; form.category = ''
    } else { msg.error(res.data.msg || '创建失败') }
  } catch (e) { msg.error('创建失败') }
  finally { creating.value = false }
}

async function handleToggle(bot) {
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/admin/bots/toggle`, null, {
      headers: { 'X-Admin-Role': localStorage.getItem('vela_admin_role') || 'admin' },
      params: { botId: bot.id }
    })
    if (res.data.code === 200) { msg.success('操作成功'); load(pagination.page) }
  } catch (e) { msg.error('操作失败') }
}

async function handleDelete(bot) {
  const d = await msg.warning('确认删除该 Bot？', { positiveText: '确认', negativeText: '取消' })
  if (!d) return
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/admin/bots/delete`, null, {
      headers: { 'X-Admin-Role': localStorage.getItem('vela_admin_role') || 'admin' },
      params: { botId: bot.id }
    })
    if (res.data.code === 200) { msg.success('已删除'); load(pagination.page) }
  } catch (e) { msg.error('删除失败') }
}

load(1)
</script>
