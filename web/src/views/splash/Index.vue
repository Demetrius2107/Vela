<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(135deg, #4F6EF7 0%, #7C3AED 100%); position: relative; overflow: hidden">
    <!-- 装饰元素 -->
    <div style="position: absolute; top: -120px; right: -80px; width: 400px; height: 400px; border-radius: 50%; background: rgba(255,255,255,0.06)" />
    <div style="position: absolute; bottom: -100px; left: -60px; width: 300px; height: 300px; border-radius: 50%; background: rgba(255,255,255,0.06)" />
    
    <div style="text-align: center; animation: fadeInUp 0.6s ease">
      <div style="width: 80px; height: 80px; border-radius: 22px; background: rgba(255,255,255,0.95); display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; box-shadow: 0 8px 32px rgba(0,0,0,0.2); backdropFilter: blur(8px)">
        <span style="font-size: 36px; font-weight: bold; background: linear-gradient(135deg, #4F6EF7, #7C3AED); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text">V</span>
      </div>
      <h1 style="font-size: 32px; font-weight: 700; color: #fff; margin: 0">Vela IM</h1>
      <p style="color: rgba(255,255,255,0.7); margin-top: 12px; font-size: 16px">正在为你加载...</p>
      <div style="margin-top: 40px">
        <div style="width: 32px; height: 32px; border: 3px solid rgba(255,255,255,0.15); border-top-color: #fff; border-radius: 50%; animation: spin 0.8s linear infinite; margin: 0 auto" />
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
  }, 1500)
})
</script>

<style scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
