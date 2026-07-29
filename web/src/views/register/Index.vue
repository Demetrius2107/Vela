<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); position: relative; overflow: hidden">
    <!-- 装饰圆 -->
    <div style="position: absolute; top: -100px; right: -100px; width: 400px; height: 400px; border-radius: 50%; background: rgba(255,255,255,0.05)" />
    <div style="position: absolute; bottom: -80px; left: -80px; width: 300px; height: 300px; border-radius: 50%; background: rgba(255,255,255,0.05)" />
    
    <n-card :bordered="false" style="width: 420px; border-radius: 16px; box-shadow: 0 12px 48px rgba(0,0,0,0.2); animation: fadeInUp 0.5s ease">
      <div style="text-align: center; margin-bottom: 28px">
        <div style="font-size: 36px; margin-bottom: 8px">🚀</div>
        <n-gradient-text type="primary" size="26" style="font-weight: 700">创建账号</n-gradient-text>
        <p style="color: #888; margin-top: 8px; font-size: 14px">加入 Vela IM，开始即时通讯</p>
      </div>
      
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="用户 ID" path="userId">
          <n-input v-model:value="form.userId" placeholder="字母或数字，4-20位" size="large">
            <template #prefix><span style="color: #999">👤</span></template>
          </n-input>
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="form.nickname" placeholder="你的显示名称" size="large">
            <template #prefix><span style="color: #999">📝</span></template>
          </n-input>
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="至少6位" size="large">
            <template #prefix><span style="color: #999">🔒</span></template>
          </n-input>
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="form.confirmPassword" type="password" show-password-on="click" placeholder="再次输入密码" size="large">
            <template #prefix><span style="color: #999">✓</span></template>
          </n-input>
        </n-form-item>
        <n-button type="primary" block size="large" round :loading="loading" @click="handleRegister" style="height: 44px; font-size: 16px">注 册</n-button>
      </n-form>
      
      <div style="text-align: center; margin-top: 20px">
        <n-text depth="3" style="font-size: 13px">
          已有账号？
          <n-button text type="primary" @click="$router.push('/login')" style="font-weight: 600">去登录 →</n-button>
        </n-text>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'

const router = useRouter()
const msg = useMessage()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  userId: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  userId: [
    { required: true, message: '请输入用户 ID', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '4-20位字母或数字', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称不超过20字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_, v) => v === form.password || new Error('两次密码不一致'), trigger: 'blur' }
  ]
}

async function handleRegister() {
  try { await formRef.value?.validate() } catch { return }
  loading.value = true
  setTimeout(() => {
    msg.success('🎉 注册成功，请登录')
    loading.value = false
    router.push('/login')
  }, 500)
}
</script>

<style scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
