<template>
  <div style="min-height: 100vh; background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%)">
    <NavHeader />
    <div style="max-width: 720px; margin: 0 auto; padding: 32px 20px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px">
        <div style="font-size: 24px; font-weight: 700; color: #1a1a2e">⚙️ 设置</div>
        <n-button size="small" round :loading="saving" type="primary" @click="saveToServer" style="background:linear-gradient(135deg,#4F6EF7,#7C3AED);border:none">保存到服务器</n-button>
      </div>

      <!-- 显示设置 -->
      <n-card :bordered="false" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <template #header><span style="font-weight: 600">🎨 显示设置</span></template>
        <n-space vertical>
          <div v-for="item in displayItems" :key="item.key" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0">
            <div>
              <div style="font-weight: 500; font-size: 14px; color: #333">{{ item.label }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">{{ item.desc }}</div>
            </div>
            <n-switch v-model:value="item.value" />
          </div>
        </n-space>
      </n-card>

      <!-- 通知设置 -->
      <n-card :bordered="false" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <template #header><span style="font-weight: 600">🔔 通知设置</span></template>
        <n-space vertical>
          <div v-for="item in notifyItems" :key="item.key" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0">
            <div>
              <div style="font-weight: 500; font-size: 14px; color: #333">{{ item.label }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">{{ item.desc }}</div>
            </div>
            <n-switch v-model:value="item.value" />
          </div>
        </n-space>
      </n-card>

      <!-- 隐私设置 -->
      <n-card :bordered="false" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <template #header><span style="font-weight: 600">🔒 隐私设置</span></template>
        <n-space vertical>
          <div v-for="item in privacyItems" :key="item.key" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0">
            <div>
              <div style="font-weight: 500; font-size: 14px; color: #333">{{ item.label }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">{{ item.desc }}</div>
            </div>
            <n-switch v-model:value="item.value" />
          </div>
        </n-space>
      </n-card>

      <!-- 账号安全 -->
      <n-card :bordered="false" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <template #header><span style="font-weight: 600">🛡️ 账号安全</span></template>
        <n-space vertical>
          <div v-for="item in securityItems" :key="item.key" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0">
            <div>
              <div style="font-weight: 500; font-size: 14px; color: #333">{{ item.label }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">{{ item.desc }}</div>
            </div>
            <n-button v-if="item.btnText" size="tiny" quaternary @click="item.action">{{ item.btnText }}</n-button>
            <n-text v-else depth="3" style="font-size: 13px">{{ item.valueText }}</n-text>
          </div>
        </n-space>
      </n-card>

      <!-- 关于 -->
      <n-card :bordered="false" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <template #header><span style="font-weight: 600">ℹ️ 关于</span></template>
        <n-descriptions :column="2" label-placement="left" size="small">
          <n-descriptions-item label="应用名称">Vela IM</n-descriptions-item>
          <n-descriptions-item label="版本">0.1.0</n-descriptions-item>
          <n-descriptions-item label="技术栈">Vue 3 + Naive UI + Pinia</n-descriptions-item>
          <n-descriptions-item label="后端">Spring Boot + Netty + RabbitMQ</n-descriptions-item>
          <n-descriptions-item label="数据库">MySQL + Redis + Elasticsearch</n-descriptions-item>
          <n-descriptions-item label="同步范围">Web / Android 设置实时同步</n-descriptions-item>
        </n-descriptions>
        <n-divider style="margin: 12px 0" />
        <div style="font-size: 12px; color: #bbb; text-align: center">Copyright © 2026 Vela IM Team. All rights reserved.</div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import NavHeader from '../../components/layout/NavHeader.vue'
import { useUserStore } from '../../stores/user'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const userStore = useUserStore()
const userId = computed(() => userStore.userId || localStorage.getItem('vela_user_id') || 'test_user')
const saving = ref(false)

// ===== 设置项定义（默认值与 localStorage 合并） =====
function loadLocal(key, defaultValue) {
  const v = localStorage.getItem(`vela_${key}`)
  return v !== null ? v === 'true' : defaultValue
}

const displayItems = reactive([
  { key: 'display.showAvatar', label: '显示用户头像', desc: '在消息列表中显示用户头像', value: loadLocal('showAvatar', true) },
  { key: 'display.showStatus', label: '显示在线状态', desc: '在其他用户面前显示在线状态', value: loadLocal('showStatus', true) },
  { key: 'display.compactMode', label: '紧凑模式', desc: '减少聊天列表的间距', value: loadLocal('compactMode', false) },
])

const notifyItems = reactive([
  { key: 'notify.enabled', label: '消息通知', desc: '收到新消息时弹出通知', value: loadLocal('notify_enabled', true) },
  { key: 'notify.sound', label: '提示音', desc: '新消息到达时播放提示音', value: loadLocal('notify_sound', true) },
  { key: 'notify.vibration', label: '震动', desc: '新消息到达时震动', value: loadLocal('notify_vibration', false) },
  { key: 'notify.preview', label: '通知预览', desc: '通知栏显示消息内容预览', value: loadLocal('notify_preview', true) },
])

const privacyItems = reactive([
  { key: 'privacy.searchable', label: '允许被搜索', desc: '其他用户可以通过用户 ID 找到你', value: loadLocal('privacy_searchable', true) },
  { key: 'privacy.addFriendVerify', label: '加好友验证', desc: '添加好友需要你的同意', value: loadLocal('privacy_addFriendVerify', true) },
  { key: 'privacy.showOnline', label: '显示在线状态', desc: '在其他用户面前显示在线/离线', value: loadLocal('privacy_showOnline', true) },
])

const securityItems = reactive([
  { key: 'password', label: '修改密码', desc: '定期更换密码保障账号安全', btnText: '修改', action: () => msg.info('密码修改功能待开发') },
  { key: 'account', label: '当前账号', desc: '登录中的账号', btnText: '', valueText: userId.value, action: () => {} },
])

// ===== 从服务端加载设置 =====
async function loadFromServer() {
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/user/config/get`, {
      params: { appId: 1, userId: userId.value, clientType: 'web' }
    })
    if (res.data.code === 200 && res.data.data) {
      const config = res.data.data
      // 合并到本地状态
      const allItems = [...displayItems, ...notifyItems, ...privacyItems]
      for (const item of allItems) {
        if (config[item.key] !== undefined) {
          item.value = config[item.key] === 'true'
          // 同时写入 localStorage 作为缓存
          localStorage.setItem(`vela_${item.key.split('.').pop()}`, item.value)
        }
      }
    }
  } catch (e) { /* 离线可用，使用默认值 */ }
}

// ===== 保存到服务端 =====
async function saveToServer() {
  saving.value = true
  // 1. 写入本地
  const allItems = [...displayItems, ...notifyItems, ...privacyItems]
  for (const item of allItems) {
    const localKey = `vela_${item.key.split('.').pop()}`
    localStorage.setItem(localKey, item.value)
  }
  // 2. 写入服务端
  try {
    const configs = allItems.map(item => ({ key: item.key, value: String(item.value) }))
    const res = await axios.post(`${VELA.API_URL}/v1/user/config/save`, configs, {
      params: { appId: 1, userId: userId.value, clientType: 'web' }
    })
    if (res.data.code === 200) msg.success('设置已保存到服务器，跨端自动同步')
    else msg.error(res.data.msg || '保存失败')
  } catch (e) { msg.error('保存失败，设置已保存在本地') }
  finally { saving.value = false }
}

onMounted(() => {
  loadFromServer()
})
</script>
