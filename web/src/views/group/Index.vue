<template>
  <n-layout style="height: 100vh">
    <NavHeader />
    <n-layout position="absolute" style="top: 48px; bottom: 0; padding: 24px; display: flex; justify-content: center">
      <div style="width: 640px">
        <!-- 群信息头部 -->
        <n-card>
          <n-space align="center">
            <n-avatar round :size="56" style="background: #18a058">{{ group.name?.[0] || 'G' }}</n-avatar>
            <div style="flex: 1">
              <n-h3 style="margin: 0">{{ group.name }}</n-h3>
              <n-text depth="3">{{ group.memberCount }} 人 · {{ group.description || '暂无介绍' }}</n-text>
            </div>
            <n-button v-if="group.isOwner" size="small" quaternary @click="editing = !editing">
              {{ editing ? '取消' : '编辑' }}
            </n-button>
          </n-space>
        </n-card>

        <n-divider />

        <!-- 编辑群信息 -->
        <n-card v-if="editing" title="编辑群信息" style="margin-bottom: 16px">
          <n-form :model="editForm" label-placement="top">
            <n-form-item label="群名称">
              <n-input v-model:value="editForm.name" :maxlength="30" />
            </n-form-item>
            <n-form-item label="群介绍">
              <n-input v-model:value="editForm.description" type="textarea" :rows="3" :maxlength="200" show-count />
            </n-form-item>
            <n-button type="primary" block @click="handleSaveGroup">保存</n-button>
          </n-form>
        </n-card>

        <!-- 成员列表 -->
        <n-card title="群成员">
          <template #header-extra>
            <n-button v-if="group.isOwner || group.isAdmin" size="tiny" type="primary" @click="$router.push('/group/create')">
              添加成员
            </n-button>
          </template>
          <n-list>
            <n-list-item v-for="m in members" :key="m.userId">
              <template #prefix>
                <n-avatar round :color="m.role === 'owner' ? '#f0a020' : m.role === 'admin' ? '#2080f0' : '#888'">
                  {{ m.nickname?.[0] || '?' }}
                </n-avatar>
              </template>
              <n-thing :title="m.nickname" :description="'ID: ' + m.userId">
                <template #description-extra>
                  <n-tag :type="m.role === 'owner' ? 'warning' : m.role === 'admin' ? 'info' : 'default'" size="tiny">
                    {{ { owner: '群主', admin: '管理员', member: '成员' }[m.role] }}
                  </n-tag>
                </template>
              </n-thing>
              <template #suffix>
                <n-button v-if="group.isOwner && m.role !== 'owner'" size="tiny" quaternary type="error">移除</n-button>
              </template>
            </n-list-item>
          </n-list>
        </n-card>

        <n-divider />

        <!-- 操作按钮 -->
        <n-space justify="center">
          <n-button v-if="!group.isOwner" type="error" quaternary @click="handleLeaveGroup">退出群聊</n-button>
          <n-button v-if="group.isOwner" type="error" quaternary @click="handleDisbandGroup">解散群聊</n-button>
        </n-space>
      </div>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import NavHeader from '../../components/layout/NavHeader.vue'

const msg = useMessage()
const editing = ref(false)

const group = reactive({
  id: 1,
  name: '项目团队',
  description: '日常项目沟通群',
  memberCount: 8,
  isOwner: true,
  isAdmin: true
})

const editForm = reactive({
  name: group.name,
  description: group.description
})

const members = ref([
  { userId: 'user001', nickname: '我', role: 'owner' },
  { userId: 'user002', nickname: '张三', role: 'admin' },
  { userId: 'user003', nickname: '李四', role: 'member' },
  { userId: 'user004', nickname: '王五', role: 'member' }
])

function handleSaveGroup() {
  group.name = editForm.name
  group.description = editForm.description
  editing.value = false
  msg.success('群信息已更新')
}

function handleLeaveGroup() {
  msg.success('已退出群聊')
}

function handleDisbandGroup() {
  msg.success('群聊已解散')
}
</script>
