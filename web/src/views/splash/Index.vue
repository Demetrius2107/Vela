<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <div style="text-align: center">
      <div style="width: 80px; height: 80px; border-radius: 20px; background: #fff; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.15)">
        <span style="font-size: 36px; font-weight: bold; background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text">V</span>
      </div>
      <n-gradient-text type="primary" size="32" style="font-weight: bold; color: #fff !important">Vela IM</n-gradient-text>
      <p style="color: rgba(255,255,255,0.7); margin-top: 12px; font-size: 16px">正在加载...</p>
      <div style="margin-top: 32px">
        <n-spin :stroke="'#fff'" :size="28" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  setTimeout(() => {
    router.replace('/chat')
  }, 2000)
})
</script>
