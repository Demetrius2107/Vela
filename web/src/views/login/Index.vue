<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <n-card :bordered="false" style="width: 400px; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,0.15)">
      <div style="text-align: center; margin-bottom: 24px">
        <n-gradient-text type="primary" size="28" style="font-weight: bold">Vela IM</n-gradient-text>
        <p style="color: #888; margin-top: 8px; font-size: 14px">欢迎回来，请登录你的账号</p>
      </div>
      <n-form>
        <n-form-item label="用户 ID">
          <n-input v-model:value="userId" placeholder="请输入用户 ID" size="large" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" show-password-on="click" placeholder="请输入密码" size="large" />
        </n-form-item>
        <n-button type="primary" block size="large" round @click="handleLogin" style="margin-top: 8px">登 录</n-button>
      </n-form>
      <div style="text-align: center; margin-top: 16px">
        <n-text depth="3" style="font-size: 13px">
          还没有账号？
          <n-button text type="primary" @click="$router.push('/register')">立即注册</n-button>
        </n-text>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const userId = ref('')
const password = ref('')

function handleLogin() {
  if (!userId.value || !password.value) return
  userStore.loginSuccess(userId.value, 'demo-token')
  router.push('/splash')
}
</script>
