<template>
  <n-layout style="height: 100vh">
    <NavHeader />
    <n-layout position="absolute" style="top: 48px; bottom: 0; padding: 24px; display: flex; justify-content: center">
      <div style="width: 640px">
        <n-h2>搜索消息</n-h2>
        <n-input
          v-model:value="keyword"
          placeholder="输入关键词搜索聊天记录..."
          clearable
          size="large"
          @keydown.enter="handleSearch"
        >
          <template #prefix><n-icon><search-outline /></n-icon></template>
        </n-input>

        <n-divider />

        <n-empty v-if="searched && results.length === 0" description="未找到相关消息" />

        <n-space vertical>
          <n-card
            v-for="r in results"
            :key="r.id"
            size="small"
            hoverable
            @click="jumpToConversation(r)"
            style="cursor: pointer"
          >
            <n-space align="center">
              <n-avatar round :size="36" :color="r.color">{{ r.from[0] }}</n-avatar>
              <div>
                <n-text strong>{{ r.from }}</n-text>
                <n-text depth="3" style="font-size: 12px; margin-left: 8px">{{ r.time }}</n-text>
                <br />
                <n-text>{{ r.content }}</n-text>
              </div>
            </n-space>
          </n-card>
        </n-space>
      </div>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import NavHeader from '../../components/layout/NavHeader.vue'

const router = useRouter()
const keyword = ref('')
const results = ref([])
const searched = ref(false)

function handleSearch() {
  if (!keyword.value.trim()) return
  searched.value = true
  // TODO: 调用消息搜索接口（未来接 ES）
  results.value = []
}

function jumpToConversation(r) {
  router.push('/chat')
}
</script>
