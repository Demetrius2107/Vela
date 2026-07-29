<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">📋 审批</div>
      <n-button type="primary" size="small" @click="showCreate=true">发起审批</n-button>
    </div>
    <n-table size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa"><th>标题</th><th>类型</th><th>申请人</th><th>状态</th><th>审批人</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="a in list" :key="a.id">
          <td>{{ a.title }}</td>
          <td>{{ a.type }}</td>
          <td>{{ a.applicantId }}</td>
          <td><n-tag size="tiny" :type="a.status===1?'success':a.status===2?'error':'warning'">{{ {0:'待审批',1:'已通过',2:'已拒绝'}[a.status] }}</n-tag></td>
          <td>{{ a.approverId||'-' }}</td>
          <td><n-button v-if="a.status===0" size="tiny" quaternary style="color:#52c41a" @click="approve(a.id,true)">通过</n-button></td>
        </tr>
      </tbody>
    </n-table>
    <n-modal v-model:show="showCreate" title="发起审批" preset="card" style="width:400px">
      <n-space vertical>
        <n-input v-model:value="form.title" placeholder="审批标题" />
        <n-input v-model:value="form.content" type="textarea" placeholder="审批内容" />
        <n-select v-model:value="form.type" :options="[{label:'请假',value:'leave'},{label:'报销',value:'expense'},{label:'采购',value:'purchase'},{label:'其他',value:'other'}]" />
        <n-button type="primary" block @click="handleCreate">提交</n-button>
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
const form = reactive({ title:'', content:'', type:'leave' })

async function load() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/office/approval/list`, { params: { userId:'user001', appId:10000 } })
    if (data.code===200) list.value = data.data.list||[]
  } catch(e){}
}
async function approve(id, passed) {
  await axios.post(`${VELA.API_URL}/v1/office/approval/approve`, null, { params: { id, approverId:'admin', passed } })
  msg.success('已处理'); load()
}
async function handleCreate() {
  await axios.post(`${VELA.API_URL}/v1/office/approval/submit`, {
    applicantId:'user001', appId:10000, title:form.title, content:form.content, type:form.type
  })
  msg.success('已提交'); showCreate.value=false; load()
}
onMounted(load)
</script>
