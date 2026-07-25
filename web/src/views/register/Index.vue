<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <n-card :bordered="false" style="width: 420px; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,0.15)">
      <div style="text-align: center; margin-bottom: 24px">
        <n-gradient-text type="primary" size="28" style="font-weight: bold">创建账号</n-gradient-text>
        <p style="color: #888; margin-top: 8px; font-size: 14px">加入 Vela IM，开始即时通讯</p>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="用户 ID" path="userId">
          <n-input v-model:value="form.userId" placeholder="字母或数字，4-20位" size="large" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="form.nickname" placeholder="你的显示名称" size="large" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="至少6位" size="large" />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="form.confirmPassword" type="password" show-password-on="click" placeholder="再次输入密码" size="large" />
        </n-form-item>
        <n-button type="primary" block size="large" round :loading="loading" @click="handleRegister">注 册</n-button>
      </n-form>
      <div style="text-align: center; margin-top: 16px">
        <n-text depth="3" style="font-size: 13px">
          已有账号？
          <n-button text type="primary" @click="$router.push('/login')">去登录</n-button>
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
    msg.success('注册成功，请登录')
    loading.value = false
    router.push('/login')
  }, 500)
}
</script>
