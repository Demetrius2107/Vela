<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">👤 用户管理</div>
      <div style="display:flex;gap:8px">
        <n-input v-model:value="keyword" placeholder="搜索用户ID/昵称" clearable style="width:200px" @keydown.enter="load(1)" />
        <n-button type="primary" @click="load(1)" :loading="loading">搜索</n-button>
      </div>
    </div>

    <n-table :single-line="true" size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa">
        <th style="width:40px"><n-checkbox :checked="allChecked" @update:checked="toggleAll" /></th>
        <th>用户ID</th><th>昵称</th><th>性别</th><th>状态</th><th>注册时间</th><th style="width:200px">操作</th>
      </tr></thead>
      <tbody>
        <tr v-for="u in users" :key="u.userId">
          <td><n-checkbox :checked="selected.includes(u.userId)" @update:checked="toggle(u.userId)" /></td>
          <td><n-button text style="color:#1890ff" @click="showDetail(u)">{{ u.userId }}</n-button></td>
          <td>{{ u.nickName || '-' }}</td>
          <td>{{ {1:'男',2:'女',0:'未设置'}[u.userSex] || '-' }}</td>
          <td><n-tag :type="u.forbiddenFlag === 1 ? 'error' : 'success'" size="tiny" round>{{ u.forbiddenFlag === 1 ? '已禁用' : '正常' }}</n-tag></td>
          <td style="color:#888;font-size:12px">{{ u.createTime ? new Date(u.createTime).toLocaleDateString() : '-' }}</td>
          <td>
            <n-button size="tiny" quaternary @click="showDetail(u)" style="color:#1890ff">详情</n-button>
            <n-button size="tiny" quaternary :type="u.forbiddenFlag === 1 ? 'success' : 'error'" @click="toggleForbidden(u.userId)">
              {{ u.forbiddenFlag === 1 ? '解封' : '禁用' }}
            </n-button>
          </td>
        </tr>
        <tr v-if="!users.length"><td colspan="7" style="text-align:center;padding:40px;color:#888">暂无数据</td></tr>
      </tbody>
    </n-table>

    <div style="display:flex;justify-content:space-between;align-items:center;margin-top:12px">
      <div>
        <n-button size="tiny" quaternary :disabled="!selected.length" @click="batchForbidden(true)" style="color:#ff4d4f">批量禁用</n-button>
        <n-button size="tiny" quaternary :disabled="!selected.length" @click="batchForbidden(false)" style="margin-left:8px">批量解封</n-button>
        <span style="margin-left:12px;font-size:12px;color:#888">已选 {{ selected.length }} 人</span>
      </div>
      <n-pagination :page="page" :page-count="pages" :page-size="size" @update:page="load" />
    </div>

    <!-- 详情抽屉 -->
    <n-drawer v-model:show="showDrawer" :width="400" placement="right">
      <n-drawer-content title="用户详情" closable>
        <div v-if="detailUser">
          <div style="text-align:center;margin-bottom:24px">
            <n-avatar round :size="64" color="#1890ff">{{ detailUser.userId?.[0] }}</n-avatar>
            <div style="font-weight:600;margin-top:8px">{{ detailUser.nickName || detailUser.userId }}</div>
          </div>
          <n-descriptions :column="1" size="small" bordered>
            <n-descriptions-item label="用户ID">{{ detailUser.userId }}</n-descriptions-item>
            <n-descriptions-item label="昵称">{{ detailUser.nickName || '-' }}</n-descriptions-item>
            <n-descriptions-item label="性别">{{ {1:'男',2:'女',0:'未设置'}[detailUser.userSex] || '-' }}</n-descriptions-item>
            <n-descriptions-item label="签名">{{ detailUser.selfSignature || '-' }}</n-descriptions-item>
            <n-descriptions-item label="地区">{{ detailUser.location || '-' }}</n-descriptions-item>
            <n-descriptions-item label="状态">{{ detailUser.forbiddenFlag === 1 ? '已禁用' : '正常' }}</n-descriptions-item>
            <n-descriptions-item label="禁言中">{{ detailUser.silentFlag === 1 ? '是' : '否' }}</n-descriptions-item>
            <n-descriptions-item label="用户类型">{{ {1:'普通',2:'客服',3:'机器人'}[detailUser.userType] || '-' }}</n-descriptions-item>
          </n-descriptions>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const users = ref([])
const keyword = ref('')
const page = ref(1)
const pages = ref(1)
const size = ref(20)
const loading = ref(false)
const selected = ref([])
const showDrawer = ref(false)
const detailUser = ref(null)

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/users`, {
      params: { keyword: keyword.value || undefined, page: page.value - 1, size: size.value }
    })
    if (data.code === 200) {
      users.value = data.data.list || []
      pages.value = data.data.pages || 1
    }
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

function showDetail(u) { detailUser.value = u; showDrawer.value = true }

async function toggleForbidden(uid) {
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/users/toggleForbidden`, null, { params: { userId: uid } })
    if (data.code === 200) { msg.success('操作成功'); load() }
  } catch (e) { msg.error('操作失败') }
}

async function batchForbidden(forbidden) {
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/users/batchForbidden`, selected.value, { params: { forbidden } })
    if (data.code === 200) { msg.success('操作成功'); selected.value = []; load() }
  } catch (e) { msg.error('操作失败') }
}

const allChecked = ref(false)
function toggleAll(v) { allChecked.value = v; selected.value = v ? users.value.map(u => u.userId) : [] }
function toggle(uid) {
  const i = selected.value.indexOf(uid)
  if (i >= 0) selected.value.splice(i, 1); else selected.value.push(uid)
}

load(1)
</script>
