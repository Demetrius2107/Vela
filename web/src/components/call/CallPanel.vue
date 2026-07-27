<template>
  <div v-if="status !== 'idle'" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999; background: rgba(0,0,0,0.85); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff">
    <!-- 响铃 / 呼叫中 -->
    <template v-if="status === 'ringing'">
      <div style="font-size: 48px; margin-bottom: 16px">📞</div>
      <div style="font-size: 20px; font-weight: 600; margin-bottom: 4px">{{ remoteUserId }}</div>
      <div style="font-size: 14px; color: #aaa; margin-bottom: 40px">邀请你{{ isVideo ? '视频' : '语音' }}通话</div>
      <div style="display: flex; gap: 40px">
        <div @click="$emit('accept')" style="width: 64px; height: 64px; border-radius: 50%; background: #31c451; display: flex; align-items: center; justify-content: center; font-size: 28px; cursor: pointer">📞</div>
        <div @click="$emit('reject')" style="width: 64px; height: 64px; border-radius: 50%; background: #e74c3c; display: flex; align-items: center; justify-content: center; font-size: 28px; cursor: pointer">✕</div>
      </div>
    </template>

    <!-- 呼叫中 -->
    <template v-else-if="status === 'calling'">
      <div style="font-size: 48px; margin-bottom: 16px; animation: pulse 1s infinite">{{ isVideo ? '📹' : '📞' }}</div>
      <div style="font-size: 20px; font-weight: 600; margin-bottom: 4px">{{ remoteUserId }}</div>
      <div style="font-size: 14px; color: #aaa; margin-bottom: 40px">正在呼叫...</div>
      <div @click="$emit('end')" style="width: 64px; height: 64px; border-radius: 50%; background: #e74c3c; display: flex; align-items: center; justify-content: center; font-size: 28px; cursor: pointer">✕</div>
    </template>

    <!-- 通话中 -->
    <template v-else-if="status === 'connected'">
      <div v-if="isVideo" style="position: relative; width: 100%; flex: 1; background: #222">
        <video ref="remoteVideoRef" autoplay playsinline style="width: 100%; height: 100%; object-fit: contain" />
        <video ref="localVideoRef" autoplay playsinline muted style="position: absolute; bottom: 80px; right: 20px; width: 160px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.5)" />
      </div>
      <div v-else style="text-align: center; padding: 60px">
        <div style="font-size: 80px; margin-bottom: 16px; border-radius: 50%; background: #333; width: 120px; height: 120px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px">🎤</div>
        <div style="font-size: 20px; font-weight: 600; margin-bottom: 4px">{{ remoteUserId }}</div>
        <div style="font-size: 14px; color: #4caf50; margin-bottom: 60px">通话中 {{ duration }}</div>
      </div>
      <div @click="$emit('end')" style="width: 64px; height: 64px; border-radius: 50%; background: #e74c3c; display: flex; align-items: center; justify-content: center; font-size: 28px; cursor: pointer; position: absolute; bottom: 20px">✕</div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({ status: String, remoteUserId: String, isVideo: Boolean })
defineEmits(['accept', 'reject', 'end'])

const remoteVideoRef = ref(null)
const localVideoRef = ref(null)
const seconds = ref(0)
let timer = null

const duration = computed(() => {
  const m = Math.floor(seconds.value / 60)
  const s = seconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

watch(() => props.status, (v) => {
  if (v === 'connected') { timer = setInterval(() => seconds.value++, 1000) }
  else { clearInterval(timer); seconds.value = 0 }
})

defineExpose({ remoteVideoRef, localVideoRef })
</script>

<style>
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>
