<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">📅 日程</div>
      <n-button type="primary" size="small" @click="showCreate=true">新建日程</n-button>
    </div>
    <n-table size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa"><th>标题</th><th>开始时间</th><th>结束时间</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="s in list" :key="s.id">
          <td>{{ s.title }}</td>
          <td style="font-size:12px;color:#888">{{ s.startTime?new Date(s.startTime).toLocaleString():'-' }}</td>
          <td style="font-size:12px;color:#888">{{ s.endTime?new Date(s.endTime).toLocaleString():'-' }}</td>
          <td><n-tag size="tiny" :type="s.status===1?'success':'default'">{{ {0:'待办',1:'已完成',2:'已取消'}[s.status]||'-' }}</n-tag></td>
          <td><n-button size="tiny" quaternary style="color:#ff4d4f" @click="del(s.id)">删除</n-button></td>
        </tr>
      </tbody>
    </n-table>
    <n-modal v-model:show="showCreate" title="新建日程" preset="card" style="width:400px">
      <n-space vertical>
        <n-input v-model:value="form.title" placeholder="日程标题" />
        <n-date-picker v-model:value="form.startTime" type="datetime" placeholder="开始时间" />
        <n-date-picker v-model:value="form.endTime" type="datetime" placeholder="结束时间" />
        <n-button type="primary" block @click="handleCreate">创建</n-button>
      </n-space>
    </n-modal>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage(); const list = ref([]); const showCreate = ref(false)
const form = reactive({ title:'', startTime:null, endTime:null })

async function load() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/office/schedule/list`, { params: { userId:'user001', appId:10000 } })
    if (data.code===200) list.value = data.data.list||[]
  } catch(e){}
}
async function del(id) {
  await axios.post(`${VELA.API_URL}/v1/office/schedule/delete`, null, { params: { id } })
  msg.success('已删除'); load()
}
async function handleCreate() {
  await axios.post(`${VELA.API_URL}/v1/office/schedule/create`, {
    userId:'user001', appId:10000, title:form.title, startTime:form.startTime, endTime:form.endTime
  })
  msg.success('已创建'); showCreate.value=false; load()
}
onMounted(load)
</script>
