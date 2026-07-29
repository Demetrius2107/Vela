<template>
  <div style="min-height:100vh;background:linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%)">
    <NavHeader />
    <div style="display:flex;height:calc(100vh - 52px)">
      <!-- 左侧导航 -->
      <div style="width:180px;background:#fff;border-right:1px solid rgba(0,0,0,0.04);padding:16px 8px;flex-shrink:0">
        <div style="font-weight:700;font-size:16px;color:#1a1a2e;padding:0 12px 16px">🏢 办公生态</div>
        <div v-for="t in tabs" :key="t.key" @click="activeTab=t.key"
          :style="{
            padding:'12px 14px',cursor:'pointer',fontSize:'14px',borderRadius:'10px',marginBottom:'2px',
            color:activeTab===t.key?'#4F6EF7':'#333',
            background:activeTab===t.key?'linear-gradient(135deg, #EEF1FF, #F5F3FF)':'transparent',
            fontWeight:activeTab===t.key?600:400,
            transition:'all 0.15s'
          }"
          @mouseenter="$event.currentTarget.style.background=activeTab===t.key?'linear-gradient(135deg, #EEF1FF, #F5F3FF)':'#f8f9ff'"
          @mouseleave="$event.currentTarget.style.background=activeTab===t.key?'linear-gradient(135deg, #EEF1FF, #F5F3FF)':'transparent'"
        >
          {{ t.icon }} {{ t.label }}
        </div>
      </div>
      <!-- 内容区域 -->
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
import NavHeader from '../../components/layout/NavHeader.vue'
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
