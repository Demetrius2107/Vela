<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="font-size:22px;font-weight:700;color:#1a1a2e">👥 群组管理</div>
      <div style="display:flex;gap:8px">
        <n-select v-model:value="statusFilter" :options="statusOptions" style="width:120px" @update:value="load(1)" />
        <n-input v-model:value="keyword" placeholder="搜索群ID/群名" clearable style="width:200px" @keydown.enter="load(1)" />
        <n-button type="primary" @click="load(1)">搜索</n-button>
      </div>
    </div>

    <n-table :single-line="true" size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa">
        <th>群ID</th><th>群名称</th><th>群主</th><th>类型</th><th>成员上限</th><th>状态</th><th>创建时间</th><th style="width:120px">操作</th>
      </tr></thead>
      <tbody>
        <tr v-for="g in groups" :key="g.groupId">
          <td><n-button text style="color:#1890ff" @click="showDetail(g)">{{ g.groupId }}</n-button></td>
          <td>{{ g.groupName }}</td>
          <td>{{ g.ownerId }}</td>
          <td>{{ g.groupType === 1 ? '私有' : '公开' }}</td>
          <td>{{ g.maxMemberCount || '-' }}</td>
          <td><n-tag :type="g.status === 0 ? 'success' : 'error'" size="tiny" round>{{ g.status === 0 ? '正常' : '已解散' }}</n-tag></td>
          <td style="color:#888;font-size:12px">{{ g.createTime ? new Date(g.createTime).toLocaleDateString() : '-' }}</td>
          <td>
            <n-button v-if="g.status === 0" size="tiny" quaternary style="color:#ff4d4f" @click="dissolve(g.groupId, g.appId)">解散</n-button>
          </td>
        </tr>
        <tr v-if="!groups.length"><td colspan="8" style="text-align:center;padding:40px;color:#888">暂无数据</td></tr>
      </tbody>
    </n-table>

    <div style="display:flex;justify-content:flex-end;margin-top:12px">
      <n-pagination :page="page" :page-count="pages" @update:page="load" />
    </div>

    <n-drawer v-model:show="showDrawer" :width="400" placement="right">
      <n-drawer-content title="群组详情" closable>
        <div v-if="detailGroup">
          <n-descriptions :column="1" size="small" bordered>
            <n-descriptions-item label="群ID">{{ detailGroup.groupId }}</n-descriptions-item>
            <n-descriptions-item label="群名称">{{ detailGroup.groupName }}</n-descriptions-item>
            <n-descriptions-item label="群主">{{ detailGroup.ownerId }}</n-descriptions-item>
            <n-descriptions-item label="类型">{{ detailGroup.groupType === 1 ? '私有' : '公开' }}</n-descriptions-item>
            <n-descriptions-item label="状态">{{ detailGroup.status === 0 ? '正常' : '已解散' }}</n-descriptions-item>
            <n-descriptions-item label="简介">{{ detailGroup.introduction || '-' }}</n-descriptions-item>
            <n-descriptions-item label="公告">{{ detailGroup.notification || '-' }}</n-descriptions-item>
            <n-descriptions-item label="成员上限">{{ detailGroup.maxMemberCount || '-' }}</n-descriptions-item>
            <n-descriptions-item label="全员禁言">{{ detailGroup.mute === 1 ? '是' : '否' }}</n-descriptions-item>
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
const groups = ref([])
const keyword = ref('')
const page = ref(1)
const pages = ref(1)
const showDrawer = ref(false)
const detailGroup = ref(null)
const statusFilter = ref(null)

const statusOptions = [
  { label: '全部', value: null },
  { label: '正常', value: 0 },
  { label: '已解散', value: 1 },
]

async function load(p) {
  if (p) page.value = p
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/groups`, {
      params: { keyword: keyword.value || undefined, page: page.value - 1, size: 20, status: statusFilter.value }
    })
    if (data.code === 200) {
      groups.value = data.data.list || []
      pages.value = data.data.pages || 1
    }
  } catch (e) { msg.error('加载失败') }
}

function showDetail(g) { detailGroup.value = g; showDrawer.value = true }

async function dissolve(groupId, appId) {
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/groups/dissolve`, null, { params: { groupId, appId } })
    if (data.code === 200) { msg.success('已解散'); load() }
  } catch (e) { msg.error('操作失败') }
}

load(1)
</script>
