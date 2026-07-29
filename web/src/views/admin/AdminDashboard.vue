<template>
  <div>
    <div style="font-size: 22px; font-weight: 700; margin-bottom: 24px; color: #1a1a2e">📊 数据看板</div>
    
    <!-- 统计卡片 -->
    <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px">
      <div v-for="item in statCards" :key="item.key" :style="{ background: '#fff', borderRadius: '12px', padding: '24px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', borderLeft: '4px solid ' + item.color }">
        <div style="font-size: 13px; color: #888; margin-bottom: 4px">{{ item.label }}</div>
        <div style="font-size: 28px; font-weight: 700; color: item.color">{{ stats[item.key] ?? '-' }}</div>
      </div>
    </div>

    <!-- 数据加载 -->
    <div v-if="!loaded" style="text-align: center; padding: 60px">
      <n-button type="primary" @click="loadData" :loading="loading">加载数据</n-button>
    </div>

    <!-- 趋势 & TOP -->
    <div v-if="loaded" style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px">
      <div style="background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04)">
        <div style="font-weight: 600; margin-bottom: 12px">消息趋势（近 {{ trendDays }} 天）</div>
        <div v-if="trend" style="display: flex; align-items: baseline; gap: 8px">
          <span style="font-size: 36px; font-weight: 700; color: #1890ff">{{ trend.totalMessages }}</span>
          <span style="color: #888; font-size: 13px">条 · 日均 {{ trend.avgPerDay }} 条</span>
        </div>
      </div>
      <div style="background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04)">
        <div style="font-weight: 600; margin-bottom: 12px">最活跃群组 Top 10</div>
        <div v-if="topGroups.length" style="font-size: 13px">
          <div v-for="(g, i) in topGroups" :key="i" style="display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #f5f5f5">
            <span>{{ i + 1 }}. {{ g.groupId }}</span>
            <span style="color: #1890ff">{{ g.msgCount }} 条</span>
          </div>
        </div>
        <div v-else style="color: #888; padding: 20px; text-align: center">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const loading = ref(false)
const loaded = ref(false)
const trendDays = ref(7)
const stats = reactive({ totalUsers: 0, totalGroups: 0, totalMessages: 0, activeGroups: 0, forbiddenUsers: 0 })
const trend = ref(null)
const topGroups = ref([])

const statCards = [
  { key: 'totalUsers', label: '注册用户', color: '#1890ff' },
  { key: 'totalGroups', label: '群组总数', color: '#52c41a' },
  { key: 'totalMessages', label: '消息总数', color: '#faad14' },
  { key: 'activeGroups', label: '活跃群组', color: '#ff4d4f' },
]

async function loadData() {
  loading.value = true
  try {
    const [s, t, g] = await Promise.all([
      axios.get(`${VELA.API_URL}/v1/admin/dashboard`),
      axios.get(`${VELA.API_URL}/v1/admin/message/trend`, { params: { days: trendDays.value } }),
      axios.get(`${VELA.API_URL}/v1/admin/groups/top`, { params: { limit: 10 } }),
    ])
    if (s.data.code === 200) Object.assign(stats, s.data.data)
    if (t.data.code === 200) trend.value = t.data.data
    if (g.data.code === 200) topGroups.value = g.data.data || []
    loaded.value = true
  } catch (e) { msg.error('加载数据失败') }
  finally { loading.value = false }
}
</script>
