<template>
  <n-layout style="height: 100vh">
    <NavHeader />
    <n-layout position="absolute" style="top: 48px; bottom: 0; padding: 24px; display: flex; justify-content: center">
      <div style="width: 560px">
        <n-h2>查找用户</n-h2>
        <n-space>
          <n-input v-model:value="keyword" placeholder="输入用户 ID 或昵称搜索..." clearable style="flex: 1" />
          <n-button type="primary" @click="handleSearch">搜索</n-button>
        </n-space>

        <n-divider />

        <n-space vertical v-if="results.length > 0">
          <n-card v-for="u in results" :key="u.userId" size="small" hoverable>
            <n-space align="center">
              <n-avatar round :color="'#2080f0'">{{ u.nickname?.[0] || '?' }}</n-avatar>
              <div>
                <n-text strong>{{ u.nickname }}</n-text>
                <br />
                <n-text depth="3" style="font-size: 12px">ID: {{ u.userId }}</n-text>
              </div>
              <div style="flex: 1" />
              <n-button size="tiny" type="primary" ghost @click="handleAddFriend(u)">加好友</n-button>
            </n-space>
          </n-card>
        </n-space>

        <n-empty v-else-if="searched" description="未找到匹配的用户" />
      </div>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import NavHeader from '../../components/layout/NavHeader.vue'

const msg = useMessage()
const keyword = ref('')
const results = ref([])
const searched = ref(false)

async function handleSearch() {
  if (!keyword.value.trim()) return
  searched.value = true
  // TODO: 调用搜索接口
  results.value = []
}

function handleAddFriend(user) {
  // TODO: 调用添加好友接口
  msg.success(`已向 ${user.nickname} 发送好友请求`)
}
</script>
