<template>
  <n-layout style="height: 100vh">
    <n-layout-header
      bordered
      style="padding: 0 20px; display: flex; align-items: center; height: 48px; background: #fff"
    >
      <n-gradient-text type="primary" size="18" style="font-weight: bold">Vela IM</n-gradient-text>
      <div style="flex: 1" />
      <n-space>
        <n-button quaternary size="small" @click="$router.push('/chat')">会话</n-button>
        <n-button type="primary" size="small">通讯录</n-button>
        <n-button quaternary size="small" @click="logout">退出</n-button>
      </n-space>
    </n-layout-header>

    <n-layout position="absolute" style="top: 48px; bottom: 0" has-sider>
      <!-- 搜索栏 -->
      <n-layout-sider bordered width="320" style="background: #f5f5f5">
        <div style="padding: 16px">
          <n-input placeholder="搜索好友或群组..." round clearable />
        </div>
        <n-menu :value="activeKey" :options="menuOptions" @update:value="handleMenuSelect" />
      </n-layout-sider>

      <!-- 内容区域 -->
      <n-layout style="padding: 24px">
        <!-- 好友列表 -->
        <template v-if="activeKey === 'friends'">
          <n-h2>好友列表</n-h2>
          <n-space vertical>
            <n-card v-for="f in friends" :key="f.id" size="small" hoverable>
              <n-space align="center">
                <n-avatar round :color="f.color">{{ f.name[0] }}</n-avatar>
                <div>
                  <n-text strong>{{ f.name }}</n-text>
                  <br />
                  <n-text depth="3" style="font-size: 12px">{{ f.signature || '这个人很懒，什么都没写' }}</n-text>
                </div>
                <div style="flex: 1" />
                <n-button size="tiny" quaternary @click="openChat(f)">发消息</n-button>
                <n-button size="tiny" quaternary type="error">删除</n-button>
              </n-space>
            </n-card>
          </n-space>
        </template>

        <!-- 群组列表 -->
        <template v-if="activeKey === 'groups'">
          <n-h2>群组列表</n-h2>
          <n-space vertical>
            <n-card v-for="g in groups" :key="g.id" size="small" hoverable>
              <n-space align="center">
                <n-avatar round :color="g.color" style="background: #18a058">{{ g.name[0] }}</n-avatar>
                <div>
                  <n-text strong>{{ g.name }}</n-text>
                  <br />
                  <n-text depth="3" style="font-size: 12px">{{ g.memberCount }} 人</n-text>
                </div>
                <div style="flex: 1" />
                <n-button size="tiny" quaternary @click="openChat(g)">进入群聊</n-button>
              </n-space>
            </n-card>
          </n-space>
        </template>

        <!-- 好友请求 -->
        <template v-if="activeKey === 'requests'">
          <n-h2>好友请求</n-h2>
          <n-empty v-if="requests.length === 0" description="暂无新的好友请求" />
          <n-space vertical v-else>
            <n-card v-for="r in requests" :key="r.id" size="small">
              <n-space align="center">
                <n-avatar round>{{ r.from[0] }}</n-avatar>
                <div>
                  <n-text strong>{{ r.from }}</n-text>
                  <br />
                  <n-text depth="3" style="font-size: 12px">{{ r.message }}</n-text>
                </div>
                <div style="flex: 1" />
                <n-button size="tiny" type="primary">同意</n-button>
                <n-button size="tiny" quaternary type="error">拒绝</n-button>
              </n-space>
            </n-card>
          </n-space>
        </template>
      </n-layout>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeKey = ref('friends')

const menuOptions = [
  { label: '好友列表', key: 'friends' },
  { label: '群组列表', key: 'groups' },
  { label: '好友请求', key: 'requests' }
]

const friends = ref([
  { id: 1, name: '张三', color: '#2080f0', signature: '前端开发' },
  { id: 2, name: '李四', color: '#d03050', signature: '后端架构' },
  { id: 3, name: '王五', color: '#f0a020', signature: '产品经理' }
])

const groups = ref([
  { id: 1, name: '项目团队', color: '#18a058', memberCount: 8 },
  { id: 2, name: '技术交流群', color: '#2080f0', memberCount: 128 }
])

const requests = ref([
  { id: 1, from: '赵六', message: '你好，我是赵六' }
])

function handleMenuSelect(key) {
  activeKey.value = key
}

function openChat(contact) {
  router.push('/chat')
}

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>
