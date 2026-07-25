<template>
  <n-layout style="height: 100vh">
    <NavHeader />
    <n-layout position="absolute" style="top: 48px; bottom: 0; padding: 24px; display: flex; justify-content: center">
      <div style="width: 560px">
        <n-h2>设置</n-h2>

        <!-- 显示设置 -->
        <n-card title="显示设置" style="margin-bottom: 16px">
          <n-space vertical>
            <n-space align="center" justify="space-between">
              <span>显示用户头像</span>
              <n-switch v-model:value="display.showAvatar" @update:value="saveDisplay" />
            </n-space>
            <n-space align="center" justify="space-between">
              <span>显示在线状态</span>
              <n-switch v-model:value="display.showStatus" @update:value="saveDisplay" />
            </n-space>
          </n-space>
        </n-card>

        <!-- 通知设置 -->
        <n-card title="通知设置" style="margin-bottom: 16px">
          <n-space vertical>
            <n-space align="center" justify="space-between">
              <span>消息通知</span>
              <n-switch v-model:value="notification.enabled" />
            </n-space>
            <n-space align="center" justify="space-between">
              <span>声音</span>
              <n-switch v-model:value="notification.sound" />
            </n-space>
            <n-space align="center" justify="space-between">
              <span>震动</span>
              <n-switch v-model:value="notification.vibration" />
            </n-space>
          </n-space>
        </n-card>

        <!-- 账号安全 -->
        <n-card title="账号安全" style="margin-bottom: 16px">
          <n-space vertical>
            <n-space align="center" justify="space-between">
              <span>修改密码</span>
              <n-button size="small" quaternary @click="handleChangePwd">前往修改</n-button>
            </n-space>
            <n-divider style="margin: 8px 0" />
            <n-space align="center" justify="space-between">
              <span>当前账号</span>
              <n-text>{{ currentUser }}</n-text>
            </n-space>
          </n-space>
        </n-card>

        <!-- 关于 -->
        <n-card title="关于">
          <n-descriptions :column="1" label-placement="left" size="small">
            <n-descriptions-item label="应用名称">Vela IM</n-descriptions-item>
            <n-descriptions-item label="版本">0.1.0</n-descriptions-item>
            <n-descriptions-item label="技术栈">Vue 3 + Naive UI + Pinia</n-descriptions-item>
            <n-descriptions-item label="后端">Spring Boot + Netty + RabbitMQ</n-descriptions-item>
          </n-descriptions>
        </n-card>
      </div>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import NavHeader from '../../components/layout/NavHeader.vue'

const msg = useMessage()
const currentUser = localStorage.getItem('vela_user_id') || '未登录'

const display = reactive({
  showAvatar: localStorage.getItem('vela_show_avatar') !== 'false',
  showStatus: localStorage.getItem('vela_show_status') !== 'false'
})

function saveDisplay() {
  localStorage.setItem('vela_show_avatar', display.showAvatar)
  localStorage.setItem('vela_show_status', display.showStatus)
}

const notification = reactive({
  enabled: true,
  sound: true,
  vibration: false
})

function handleChangePwd() {
  msg.info('密码修改功能待开发')
}
</script>
