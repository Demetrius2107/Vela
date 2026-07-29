<template>
  <div style="min-height: 100vh; background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%)">
    <NavHeader />
    <div style="max-width: 720px; margin: 0 auto; padding: 32px 20px">
      <!-- 群信息头部 -->
      <div style="background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); margin-bottom: 20px">
        <div style="display: flex; align-items: center; gap: 16px">
          <n-avatar round :size="60" style="background: linear-gradient(135deg, #22C55E, #18A058); boxShadow: 0 4px 12px rgba(34,197,94,0.3); font-size: 24px">{{ group.name?.[0] || 'G' }}</n-avatar>
          <div style="flex: 1">
            <div style="font-size: 20px; font-weight: 700; color: #1a1a2e">{{ group.name }}</div>
            <div style="font-size: 13px; color: #888; margin-top: 4px">{{ group.memberCount }} 人 · {{ group.description || '暂无介绍' }}</div>
          </div>
          <n-button v-if="group.isOwner" size="small" round quaternary @click="editing = !editing" style="color: #4F6EF7">
            {{ editing ? '取消' : '✏️ 编辑' }}
          </n-button>
        </div>
      </div>

      <!-- 编辑群信息 -->
      <div v-if="editing" style="background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); margin-bottom: 20px">
        <div style="font-weight: 600; font-size: 16px; color: #1a1a2e; margin-bottom: 16px">编辑群信息</div>
        <n-form :model="editForm" label-placement="top">
          <n-form-item label="群名称">
            <n-input v-model:value="editForm.name" :maxlength="30" />
          </n-form-item>
          <n-form-item label="群介绍">
            <n-input v-model:value="editForm.description" type="textarea" :rows="3" :maxlength="200" show-count />
          </n-form-item>
          <n-button type="primary" block round @click="handleSaveGroup" style="background: linear-gradient(135deg, #4F6EF7, #7C3AED); border: none">保存</n-button>
        </n-form>
      </div>

      <!-- 成员列表 -->
      <div style="background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); margin-bottom: 20px">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <span style="font-weight: 600; font-size: 16px; color: #1a1a2e">群成员 · {{ members.length }}</span>
          <n-button v-if="group.isOwner || group.isAdmin" size="tiny" round type="primary" @click="$router.push('/group/create')">+ 添加成员</n-button>
        </div>
        <div style="display: grid; gap: 8px">
          <div v-for="m in members" :key="m.userId" style="display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 10px; background: #f8f9ff; transition: background 0.15s" @mouseenter="$event.currentTarget.style.background='#eef1ff'" @mouseleave="$event.currentTarget.style.background='#f8f9ff'">
            <n-avatar round :size="40" :style="{ background: m.role === 'owner' ? 'linear-gradient(135deg, #F59E0B, #D97706)' : m.role === 'admin' ? 'linear-gradient(135deg, #4F6EF7, #7C3AED)' : 'linear-gradient(135deg, #888, #666)' }">{{ m.nickname?.[0] || '?' }}</n-avatar>
            <div style="flex: 1">
              <div style="font-weight: 600; font-size: 14px; color: #1a1a2e">{{ m.nickname }}</div>
              <div style="font-size: 12px; color: #999">ID: {{ m.userId }}</div>
            </div>
            <n-tag :type="m.role === 'owner' ? 'warning' : m.role === 'admin' ? 'info' : 'default'" size="tiny" round>
              {{ { owner: '群主', admin: '管理员', member: '成员' }[m.role] }}
            </n-tag>
            <n-button v-if="group.isOwner && m.role !== 'owner'" size="tiny" quaternary type="error" style="margin-left: 8px">移除</n-button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div style="text-align: center">
        <n-button v-if="!group.isOwner" type="error" quaternary round @click="handleLeaveGroup" style="color: #EF4444">退出群聊</n-button>
        <n-button v-if="group.isOwner" type="error" quaternary round @click="handleDisbandGroup" style="color: #EF4444">解散群聊</n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import NavHeader from '../../components/layout/NavHeader.vue'

const msg = useMessage()
const editing = ref(false)

const group = reactive({
  id: 1, name: '项目团队', description: '日常项目沟通群', memberCount: 8, isOwner: true, isAdmin: true
})

const editForm = reactive({ name: group.name, description: group.description })

const members = ref([
  { userId: 'user001', nickname: '我', role: 'owner' },
  { userId: 'user002', nickname: '张三', role: 'admin' },
  { userId: 'user003', nickname: '李四', role: 'member' },
  { userId: 'user004', nickname: '王五', role: 'member' }
])

function handleSaveGroup() {
  group.name = editForm.name; group.description = editForm.description
  editing.value = false; msg.success('群信息已更新')
}
function handleLeaveGroup() { msg.success('已退出群聊') }
function handleDisbandGroup() { msg.success('群聊已解散') }
</script>
