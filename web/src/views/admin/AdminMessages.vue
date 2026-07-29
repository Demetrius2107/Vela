<template>
  <div>
    <div style="font-size:22px;font-weight:700;color:#1a1a2e;margin-bottom:16px">💬 消息审计</div>

    <n-card style="margin-bottom:16px;border-radius:8px">
      <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end">
        <div><label style="font-size:12px;color:#888">关键词</label><n-input v-model:value="keyword" placeholder="搜索消息内容" clearable style="width:200px" /></div>
        <div><label style="font-size:12px;color:#888">用户ID</label><n-input v-model:value="userId" placeholder="按发送者筛选" clearable style="width:160px" /></div>
        <n-button type="primary" @click="search" :loading="loading">搜索</n-button>
      </div>
    </n-card>

    <n-table :single-line="true" size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa">
        <th>消息ID</th><th>发送者</th><th>接收者</th><th>消息内容</th><th>发送时间</th>
      </tr></thead>
      <tbody>
        <tr v-for="m in messages" :key="m.messageKey">
          <td style="font-size:12px;color:#888">{{ m.messageKey }}</td>
          <td>{{ m.fromId }}</td>
          <td>{{ m.toId }}</td>
          <td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px">{{ m.messageBody || '-' }}</td>
          <td style="font-size:12px;color:#888">{{ m.createTime ? new Date(m.createTime).toLocaleString() : '-' }}</td>
        </tr>
        <tr v-if="!messages.length"><td colspan="5" style="text-align:center;padding:40px;color:#888">{{ searched ? '未找到匹配消息' : '输入关键词搜索消息记录' }}</td></tr>
      </tbody>
    </n-table>

    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <n-pagination :page="page" :page-count="pages" @update:page="loadPage" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const messages = ref([])
const keyword = ref('')
const userId = ref('')
const page = ref(1)
const pages = ref(1)
const loading = ref(false)
const searched = ref(false)

async function search() {
  page.value = 1; searched.value = true; await loadPage(1)
}

async function loadPage(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/messages/search`, {
      params: { keyword: keyword.value || undefined, userId: userId.value || undefined, page: page.value - 1, size: 20 }
    })
    if (data.code === 200) {
      messages.value = data.data.list || []
      pages.value = data.data.pages || 1
    }
  } catch (e) { msg.error('搜索失败') }
  finally { loading.value = false }
}
</script>
