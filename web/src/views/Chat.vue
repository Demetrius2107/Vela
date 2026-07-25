<template>
  <n-layout style="height: 100vh">
    <!-- 顶部导航 -->
    <n-layout-header
      bordered
      style="padding: 0 20px; display: flex; align-items: center; height: 48px; background: #fff"
    >
      <n-gradient-text type="primary" size="18" style="font-weight: bold">Vela IM</n-gradient-text>
      <div style="flex: 1" />
      <n-space>
        <n-button
          :type="activeTab === 'chat' ? 'primary' : 'quaternary'"
          size="small"
          @click="activeTab = 'chat'"
        >
          会话
        </n-button>
        <n-button
          :type="activeTab === 'contacts' ? 'primary' : 'quaternary'"
          size="small"
          @click="$router.push('/contacts')"
        >
          通讯录
        </n-button>
        <n-dropdown trigger="click" :options="userMenuOptions" @select="handleUserMenu">
          <n-button quaternary size="small">
            <template #icon><n-icon><person-outline /></n-icon></template>
            {{ currentUser }}
          </n-button>
        </n-dropdown>
      </n-space>
    </n-layout-header>

    <!-- 主区域：会话列表 + 聊天区域 -->
    <n-layout has-sider position="absolute" style="top: 48px; bottom: 0">
      <!-- 会话列表 -->
      <n-layout-sider bordered width="320" :native-scrollbar="false" style="background: #f5f5f5">
        <div style="padding: 12px">
          <n-input placeholder="搜索会话..." round clearable>
            <template #prefix><n-icon><search-outline /></n-icon></template>
          </n-input>
        </div>
        <n-list hoverable clickable>
          <n-list-item v-for="conv in conversations" :key="conv.id" @click="selectConversation(conv)">
            <template #prefix>
              <n-avatar round :color="conv.color">{{ conv.name[0] }}</n-avatar>
            </template>
            <n-thing :title="conv.name" :description="conv.lastMessage">
              <template #description-extra>
                <n-text depth="3" style="font-size: 12px">{{ conv.time }}</n-text>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
      </n-layout-sider>

      <!-- 聊天区域 -->
      <n-layout v-if="currentConv" style="display: flex; flex-direction: column">
        <!-- 聊天头部 -->
        <n-layout-header bordered style="padding: 12px 20px; display: flex; align-items: center">
          <n-avatar round :color="currentConv.color" size="small">{{ currentConv.name[0] }}</n-avatar>
          <span style="margin-left: 10px; font-weight: bold">{{ currentConv.name }}</span>
        </n-layout-header>

        <!-- 消息列表 -->
        <n-layout-content
          ref="messageListRef"
          style="flex: 1; padding: 20px; overflow-y: auto; background: #f9f9f9"
        >
          <div
            v-for="msg in messages"
            :key="msg.id"
            :style="{
              display: 'flex',
              justifyContent: msg.isSelf ? 'flex-end' : 'flex-start',
              marginBottom: '16px'
            }"
          >
            <n-card
              :bordered="false"
              :style="{
                maxWidth: '60%',
                background: msg.isSelf ? '#d1e7ff' : '#fff',
                borderRadius: msg.isSelf ? '12px 12px 2px 12px' : '12px 12px 12px 2px',
                padding: '8px 14px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.08)'
              }"
            >
              <n-text>{{ msg.content }}</n-text>
              <div :style="{ textAlign: msg.isSelf ? 'right' : 'left', marginTop: '4px' }">
                <n-text depth="3" style="font-size: 11px">{{ msg.time }}</n-text>
              </div>
            </n-card>
          </div>
        </n-layout-content>

        <!-- 输入区域 -->
        <n-layout-footer bordered style="padding: 12px 20px; background: #fff">
          <n-space>
            <n-input
              v-model:value="inputText"
              type="textarea"
              :rows="2"
              placeholder="输入消息..."
              @keydown.enter.prevent="sendMessage"
              style="flex: 1"
            />
            <n-button type="primary" @click="sendMessage" style="align-self: flex-end">
              发送
            </n-button>
          </n-space>
        </n-layout-footer>
      </n-layout>

      <!-- 未选择会话 -->
      <n-layout v-else style="display: flex; justify-content: center; align-items: center">
        <n-empty description="选择一个会话开始聊天" />
      </n-layout>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { PersonOutline, SearchOutline } from '@vicons/ionicons5'

const router = useRouter()
const activeTab = ref('chat')
const currentUser = localStorage.getItem('userId') || '用户'
const currentConv = ref(null)
const inputText = ref('')
const messageListRef = ref(null)

// 用户菜单
const userMenuOptions = [
  { label: '退出登录', key: 'logout' }
]
function handleUserMenu(key) {
  if (key === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

// 模拟会话数据
const conversations = ref([
  { id: 1, name: '张三', lastMessage: '好的，明天见', time: '10:30', color: '#2080f0' },
  { id: 2, name: '项目群', lastMessage: '收到，我改一下', time: '09:15', color: '#18a058' },
  { id: 3, name: '李四', lastMessage: '文件已发你邮箱', time: '昨天', color: '#d03050' }
])

// 模拟消息数据
const messages = ref([])

function selectConversation(conv) {
  currentConv.value = conv
  messages.value = [
    { id: 1, content: '你好，在吗？', isSelf: false, time: '10:00' },
    { id: 2, content: '在的，有什么事吗', isSelf: true, time: '10:01' },
    { id: 3, content: '明天的会议改到下午了', isSelf: false, time: '10:02' },
    { id: 4, content: '好的，收到', isSelf: true, time: '10:05' }
  ]
  setTimeout(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTo({ top: 99999, behavior: 'smooth' })
    }
  }, 100)
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !currentConv.value) return
  messages.value.push({
    id: Date.now(),
    content: text,
    isSelf: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  inputText.value = ''
  setTimeout(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTo({ top: 99999, behavior: 'smooth' })
    }
  }, 50)
}
</script>
