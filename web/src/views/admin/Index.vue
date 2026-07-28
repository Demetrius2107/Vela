<template>
  <div style="min-height: 100vh; background: #f0f2f5">
    <!-- Header -->
    <div style="background: #001529; padding: 0 24px; height: 56px; display: flex; align-items: center; gap: 16px; color: #fff">
      <div style="font-weight: 700; font-size: 18px; letter-spacing: 1px">VELA 管理后台</div>
      <div style="flex:1" />
      <n-tag size="small" round :bordered="false" style="background:#1890ff20;color:#1890ff">运营端 v1.0</n-tag>
      <n-button quaternary size="tiny" style="color:rgba(255,255,255,0.65)" @click="$router.push('/chat')">返回 IM</n-button>
    </div>
    <div style="display: flex; height: calc(100vh - 56px)">
      <!-- Sidebar -->
      <div style="width: 220px; background: #fff; border-right: 1px solid #e8e8e8; padding: 8px 0; flex-shrink: 0">
        <div v-for="item in menuItems" :key="item.key"
          @click="activeTab = item.key"
          :style="{ padding: '14px 24px', cursor: 'pointer', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '10px',
            color: activeTab === item.key ? '#1890ff' : '#333',
            background: activeTab === item.key ? '#e6f7ff' : 'transparent',
            borderRight: activeTab === item.key ? '3px solid #1890ff' : '3px solid transparent' }">
          <span>{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </div>
      </div>
      <!-- Content -->
      <div style="flex: 1; padding: 24px; overflow-y: auto; background: #f0f2f5">
        <!-- 数据看板 -->
        <AdminDashboard v-if="activeTab === 'dashboard'" />
        <!-- 用户管理 -->
        <AdminUsers v-if="activeTab === 'users'" />
        <!-- 群组管理 -->
        <AdminGroups v-if="activeTab === 'groups'" />
        <!-- 消息审计 -->
        <AdminMessages v-if="activeTab === 'messages'" />
        <!-- 操作日志 -->
        <AdminOperations v-if="activeTab === 'operations'" />
        <!-- 管理员管理 -->
        <AdminAdmins v-if="activeTab === 'admins'" />
        <!-- 系统配置 -->
        <AdminConfig v-if="activeTab === 'config'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdminDashboard from './AdminDashboard.vue'
import AdminUsers from './AdminUsers.vue'
import AdminGroups from './AdminGroups.vue'
import AdminMessages from './AdminMessages.vue'
import AdminOperations from './AdminOperations.vue'
import AdminAdmins from './AdminAdmins.vue'
import AdminConfig from './AdminConfig.vue'

const activeTab = ref('dashboard')
const menuItems = [
  { key: 'dashboard', label: '数据看板', icon: '📊' },
  { key: 'users', label: '用户管理', icon: '👤' },
  { key: 'groups', label: '群组管理', icon: '👥' },
  { key: 'messages', label: '消息审计', icon: '💬' },
  { key: 'operations', label: '操作日志', icon: '📋' },
  { key: 'admins', label: '管理员', icon: '🔐' },
  { key: 'config', label: '系统配置', icon: '⚙️' },
]
</script>
