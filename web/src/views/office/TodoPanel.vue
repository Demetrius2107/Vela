<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">✅ 待办</div>
      <n-button type="primary" size="small" @click="showCreate=true">新建待办</n-button>
    </div>
    <n-table size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa"><th>标题</th><th>优先级</th><th>截止</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="t in list" :key="t.id">
          <td>{{ t.title }}</td>
          <td><n-tag size="tiny" :type="t.priority===3?'error':t.priority===2?'warning':'default'">{{ {1:'普通',2:'重要',3:'紧急'}[t.priority]||'-' }}</n-tag></td>
          <td style="font-size:12px;color:#888">{{ t.dueTime?new Date(t.dueTime).toLocaleDateString():'-' }}</td>
          <td><n-tag size="tiny" :type="t.status===2?'success':'default'">{{ {0:'待办',1:'进行中',2:'已完成'}[t.status]||'-' }}</n-tag></td>
          <td><n-button v-if="t.status!==2" size="tiny" quaternary style="color:#52c41a" @click="done(t.id)">完成</n-button></td>
        </tr>
      </tbody>
    </n-table>
    <n-modal v-model:show="showCreate" title="新建待办" preset="card" style="width:400px">
      <n-space vertical>
        <n-input v-model:value="form.title" placeholder="待办内容" />
        <n-select v-model:value="form.priority" :options="[{label:'普通',value:1},{label:'重要',value:2},{label:'紧急',value:3}]" />
        <n-date-picker v-model:value="form.dueTime" type="date" placeholder="截止日期" />
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
const form = reactive({ title:'', priority:1, dueTime:null })

async function load() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/office/todo/list`, { params: { userId:'user001', appId:10000 } })
    if (data.code===200) list.value = data.data.list||[]
  } catch(e){}
}
async function done(id) {
  await axios.post(`${VELA.API_URL}/v1/office/todo/status`, null, { params: { id, status:2 } })
  msg.success('已完成'); load()
}
async function handleCreate() {
  await axios.post(`${VELA.API_URL}/v1/office/todo/create`, {
    userId:'user001', appId:10000, title:form.title, priority:form.priority, dueTime:form.dueTime
  })
  msg.success('已创建'); showCreate.value=false; load()
}
onMounted(load)
</script>
