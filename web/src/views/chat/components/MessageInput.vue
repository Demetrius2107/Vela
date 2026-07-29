<template>
  <n-layout-footer bordered style="padding: 12px 20px; background: #fff">
    <n-space>
      <n-input
        v-model:value="text"
        type="textarea"
        :rows="2"
        placeholder="输入消息..."
        @keydown.enter.prevent="handleSend"
        style="flex: 1"
      />
      <n-button type="primary" @click="handleSend" style="align-self: flex-end">发送</n-button>
    </n-space>
  </n-layout-footer>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['send'])
const text = ref('')

function handleSend() {
  const content = text.value.trim()
  if (!content) return
  emit('send', content)
  text.value = ''
}
</script>
