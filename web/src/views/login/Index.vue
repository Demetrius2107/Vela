<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); position: relative; overflow: hidden">
    <!-- 装饰圆 -->
    <div style="position: absolute; top: -100px; right: -100px; width: 400px; height: 400px; border-radius: 50%; background: rgba(255,255,255,0.05)" />
    <div style="position: absolute; bottom: -80px; left: -80px; width: 300px; height: 300px; border-radius: 50%; background: rgba(255,255,255,0.05)" />
    
    <n-card :bordered="false" style="width: 400px; border-radius: 16px; box-shadow: 0 12px 48px rgba(0,0,0,0.2); animation: fadeInUp 0.5s ease">
      <div style="text-align: center; margin-bottom: 28px">
        <div style="font-size: 36px; margin-bottom: 8px">💬</div>
        <n-gradient-text type="primary" size="26" style="font-weight: 700">Vela IM</n-gradient-text>
        <p style="color: #888; margin-top: 8px; font-size: 14px">欢迎回来，请登录你的账号</p>
      </div>
      
      <n-form>
        <n-form-item label="用户 ID">
          <n-input v-model:value="userId" placeholder="请输入用户 ID" size="large">
            <template #prefix><span style="color: #999">👤</span></template>
          </n-input>
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" show-password-on="click" placeholder="请输入密码" size="large">
            <template #prefix><span style="color: #999">🔒</span></template>
          </n-input>
        </n-form-item>
        <n-button type="primary" block size="large" round @click="handleLogin" style="margin-top: 8px; height: 44px; font-size: 16px">登 录</n-button>
      </n-form>
      
      <div style="text-align: center; margin-top: 20px">
        <n-text depth="3" style="font-size: 13px">
          还没有账号？
          <n-button text type="primary" @click="$router.push('/register')" style="font-weight: 600">立即注册 →</n-button>
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

<style scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
