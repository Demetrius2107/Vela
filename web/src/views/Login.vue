<template>
  <n-space vertical justify="center" align="center" style="height: 100vh">
    <n-card title="Vela IM 登录" style="width: 400px">
      <n-form>
        <n-form-item label="用户 ID">
          <n-input v-model:value="userId" placeholder="请输入用户 ID" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-button type="primary" block @click="handleLogin">登 录</n-button>
      </n-form>
    </n-card>
  </n-space>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userId = ref('')
const password = ref('')

async function handleLogin() {
  // TODO: 调用后端登录接口
  localStorage.setItem('token', 'demo-token')
  localStorage.setItem('userId', userId.value)
  router.push('/chat')
}
</script>
