<template>
  <div style="min-height:100vh;background:#f5f5f5">
    <div style="background:#fff;padding:0 24px;height:56px;display:flex;align-items:center;gap:16px;box-shadow:0 1px 4px rgba(0,0,0,0.06)">
      <div style="font-weight:700;font-size:18px;color:#1a1a2e">🏢 办公生态</div>
      <div style="flex:1" />
      <n-button quaternary size="tiny" @click="$router.push('/chat')">返回IM</n-button>
    </div>
    <div style="display:flex;height:calc(100vh-56px)">
      <div style="width:180px;background:#fff;border-right:1px solid #e8e8e8;padding:8px 0">
        <div v-for="t in tabs" :key="t.key" @click="activeTab=t.key"
          :style="{padding:'14px 20px',cursor:'pointer',fontSize:'14px',color:activeTab===t.key?'#1890ff':'#333',background:activeTab===t.key?'#e6f7ff':'transparent',borderRight:activeTab===t.key?'3px solid #1890ff':'3px solid transparent'}">
          {{ t.icon }} {{ t.label }}
        </div>
      </div>
      <div style="flex:1;padding:24px;overflow-y:auto">
        <SchedulePanel v-if="activeTab==='schedule'" />
        <TodoPanel v-if="activeTab==='todo'" />
        <ApprovalPanel v-if="activeTab==='approval'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SchedulePanel from './SchedulePanel.vue'
import TodoPanel from './TodoPanel.vue'
import ApprovalPanel from './ApprovalPanel.vue'

const activeTab = ref('schedule')
const tabs = [
  { key: 'schedule', label: '日程', icon: '📅' },
  { key: 'todo', label: '待办', icon: '✅' },
  { key: 'approval', label: '审批', icon: '📋' },
]
</script>
