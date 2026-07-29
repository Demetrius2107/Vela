<template>
  <div>
    <div style="font-size:22px;font-weight:700;color:#1a1a2e;margin-bottom:16px">📋 操作日志</div>

    <n-card style="margin-bottom:16px;border-radius:8px">
      <div style="display:flex;gap:12px;align-items:flex-end">
        <div><label style="font-size:12px;color:#888">操作者</label><n-input v-model:value="operatorId" placeholder="按操作者筛选" clearable style="width:160px" /></div>
        <n-button type="primary" @click="load(1)">查询</n-button>
      </div>
    </n-card>

    <n-table :single-line="true" size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa">
        <th>操作者</th><th>操作类型</th><th>对象类型</th><th>对象ID</th><th>详情</th><th>操作时间</th>
      </tr></thead>
      <tbody>
        <tr v-for="log in logs" :key="log.id">
          <td>{{ log.operatorId }}</td>
          <td><n-tag size="tiny" round>{{ log.action }}</n-tag></td>
          <td>{{ log.targetType }}</td>
          <td style="font-size:12px">{{ log.targetId }}</td>
          <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px">{{ log.detail || '-' }}</td>
          <td style="font-size:12px;color:#888">{{ log.operateTime ? new Date(log.operateTime).toLocaleString() : '-' }}</td>
        </tr>
        <tr v-if="!logs.length"><td colspan="6" style="text-align:center;padding:40px;color:#888">暂无操作记录</td></tr>
      </tbody>
    </n-table>

    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <n-pagination :page="page" :page-count="pages" @update:page="load" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const logs = ref([])
const operatorId = ref('')
const page = ref(1)
const pages = ref(1)

async function load(p) {
  if (p) page.value = p
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/operations`, {
      params: { operatorId: operatorId.value || undefined, page: page.value - 1 }
    })
    if (data.code === 200) { logs.value = data.data.list || []; pages.value = data.data.pages || 1 }
  } catch (e) { msg.error('加载失败') }
}
load(1)
</script>
