<template>
  <div style="min-height: 100vh; background: #f5f5f5">
    <div style="background: #1a1a2e; padding: 16px 24px; color: #fff; display: flex; align-items: center; gap: 12px">
      <div style="font-weight: 700; font-size: 18px">Vela 管理后台</div>
      <div style="flex: 1" />
      <n-button quaternary size="tiny" style="color: #fff" @click="$router.push('/chat')">返回IM</n-button>
    </div>
    <div style="display: flex; height: calc(100vh - 56px)">
      <div style="width: 200px; background: #fff; border-right: 1px solid #e8e8e8; padding: 8px 0">
        <div v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
          :style="{ padding: '12px 20px', cursor: 'pointer', fontSize: '14px', color: activeTab === tab.key ? '#2080f0' : '#333', background: activeTab === tab.key ? '#e8f0fe' : 'transparent', borderLeft: activeTab === tab.key ? '3px solid #2080f0' : '3px solid transparent' }">
          {{ tab.label }}
        </div>
      </div>
      <div style="flex: 1; padding: 24px; overflow-y: auto">
        <!-- 看板 -->
        <div v-if="activeTab === 'dashboard'">
          <div style="font-size: 20px; font-weight: 600; margin-bottom: 16px">数据看板</div>
          <div v-if="!stats" style="color: #888; padding: 40px; text-align: center; background: #fff; border-radius: 12px">
            <div style="font-size: 48px; margin-bottom: 12px">📊</div>
            <n-button @click="loadStats">加载数据</n-button>
          </div>
          <div v-else style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px">
            <div style="background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06)">
              <div style="font-size: 12px; color: #888; margin-bottom: 4px">注册用户</div>
              <div style="font-size: 32px; font-weight: 700; color: #2080f0">{{ stats.totalUsers }}</div>
            </div>
            <div style="background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06)">
              <div style="font-size: 12px; color: #888; margin-bottom: 4px">活跃群组</div>
              <div style="font-size: 32px; font-weight: 700; color: #18a058">{{ stats.totalGroups }}</div>
            </div>
            <div style="background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06)">
              <div style="font-size: 12px; color: #888; margin-bottom: 4px">消息总数</div>
              <div style="font-size: 32px; font-weight: 700; color: #d03050">{{ stats.totalMessages }}</div>
            </div>
          </div>
        </div>
        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
            <div style="font-size: 20px; font-weight: 600">用户管理</div>
            <n-input v-model:value="userKeyword" placeholder="搜索用户ID/昵称" clearable style="width: 240px" @keydown.enter="loadUsers" />
          </div>
          <n-table v-if="users.length" :single-line="true" size="small">
            <thead><tr><th>用户ID</th><th>昵称</th><th>头像</th><th>性别</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="u in users" :key="u.userId">
                <td>{{ u.userId }}</td>
                <td>{{ u.nickName }}</td>
                <td>{{ u.photo || '-' }}</td>
                <td>{{ u.userSex || '-' }}</td>
                <td><n-button size="tiny" quaternary style="color: #e74c3c">禁用</n-button></td>
              </tr>
            </tbody>
          </n-table>
          <div v-else style="text-align: center; padding: 40px; color: #888">
            <n-button @click="loadUsers">加载用户</n-button>
          </div>
        </div>
        <!-- 群组管理 -->
        <div v-if="activeTab === 'groups'">
          <div style="font-size: 20px; font-weight: 600; margin-bottom: 16px">群组管理</div>
          <n-table v-if="groups.length" :single-line="true" size="small">
            <thead><tr><th>群ID</th><th>群名称</th><th>群主</th><th>类型</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="g in groups" :key="g.groupId">
                <td>{{ g.groupId }}</td>
                <td>{{ g.groupName }}</td>
                <td>{{ g.ownerId }}</td>
                <td>{{ g.groupType === 1 ? '私有' : '公开' }}</td>
                <td>{{ g.status === 0 ? '正常' : '已解散' }}</td>
                <td><n-button v-if="g.status === 0" size="tiny" quaternary style="color: #e74c3c" @click="dissolveGroup(g.groupId, g.appId)">解散</n-button></td>
              </tr>
            </tbody>
          </n-table>
          <div v-else style="text-align: center; padding: 40px; color: #888">
            <n-button @click="loadGroups">加载群组</n-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const activeTab = ref('dashboard')
const tabs = [{ key: 'dashboard', label: '📊 数据看板' }, { key: 'users', label: '👤 用户管理' }, { key: 'groups', label: '👥 群组管理' }]

const stats = ref(null)
const users = ref([])
const groups = ref([])
const userKeyword = ref('')

async function loadStats() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/dashboard`)
    if (data.code === 200) stats.value = data.data
  } catch (e) { msg.error('加载数据失败') }
}

async function loadUsers() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/users`, { params: { keyword: userKeyword.value || undefined } })
    if (data.code === 200) users.value = data.data || []
  } catch (e) { msg.error('加载用户失败') }
}

async function loadGroups() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/groups`)
    if (data.code === 200) groups.value = data.data || []
  } catch (e) { msg.error('加载群组失败') }
}

async function dissolveGroup(groupId, appId) {
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/group/dissolve`, null, { params: { groupId, appId } })
    if (data.code === 200) { msg.success('已解散'); loadGroups() }
  } catch (e) { msg.error('解散失败') }
}
</script>
