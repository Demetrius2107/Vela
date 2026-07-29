<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">🔐 管理员管理</div>
      <n-button type="primary" @click="showCreate = true">添加管理员</n-button>
    </div>

    <n-table :single-line="true" size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa">
        <th>ID</th><th>用户ID</th><th>角色</th><th>状态</th><th>创建时间</th><th style="width:120px">操作</th>
      </tr></thead>
      <tbody>
        <tr v-for="a in admins" :key="a.id">
          <td>{{ a.id }}</td>
          <td>{{ a.userId }}</td>
          <td><n-tag :type="a.role === 'super_admin' ? 'error' : a.role === 'operator' ? 'primary' : 'warning'" size="tiny" round>{{ a.role }}</n-tag></td>
          <td><n-tag :type="a.status === 1 ? 'success' : 'error'" size="tiny" round>{{ a.status === 1 ? '正常' : '禁用' }}</n-tag></td>
          <td style="font-size:12px;color:#888">{{ a.createTime ? new Date(a.createTime).toLocaleDateString() : '-' }}</td>
          <td><n-button size="tiny" quaternary :type="a.status === 1 ? 'error' : 'success'" @click="toggle(a.id)">{{ a.status === 1 ? '禁用' : '启用' }}</n-button></td>
        </tr>
        <tr v-if="!admins.length"><td colspan="6" style="text-align:center;padding:40px;color:#888">暂无管理员</td></tr>
      </tbody>
    </n-table>

    <n-modal v-model:show="showCreate" title="添加管理员" preset="card" style="width:400px">
      <n-space vertical>
        <n-input v-model:value="newUserId" placeholder="用户ID" />
        <n-input v-model:value="newPassword" type="password" placeholder="密码" />
        <n-select v-model:value="newRole" :options="roleOptions" />
        <n-button type="primary" block @click="handleCreate" :loading="creating">确认添加</n-button>
      </n-space>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const admins = ref([])
const showCreate = ref(false)
const newUserId = ref('')
const newPassword = ref('')
const newRole = ref('operator')
const creating = ref(false)
const roleOptions = [
  { label: '超管 (super_admin)', value: 'super_admin' },
  { label: '运营 (operator)', value: 'operator' },
  { label: '审计 (auditor)', value: 'auditor' },
]

async function load() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/admins/list`)
    if (data.code === 200) admins.value = data.data || []
  } catch (e) { msg.error('加载失败') }
}

async function toggle(id) {
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/admins/toggle`, null, { params: { adminId: id } })
    if (data.code === 200) { msg.success('操作成功'); load() }
  } catch (e) { msg.error('操作失败') }
}

async function handleCreate() {
  creating.value = true
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/admins/create`, null, {
      params: { userId: newUserId.value, password: newPassword.value, role: newRole.value }
    })
    if (data.code === 200) { msg.success('添加成功'); showCreate.value = false; load() }
    else msg.error(data.msg)
  } catch (e) { msg.error('添加失败') }
  finally { creating.value = false }
}

onMounted(load)
</script>
