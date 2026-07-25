<template>
  <div style="flex: 1; padding: 20px; overflow-y: auto; background: #f9f9f9">
    <div
      v-for="msg in messages"
      :key="msg.id"
      :style="{
        display: 'flex',
        justifyContent: msg.isSelf ? 'flex-end' : 'flex-start',
        marginBottom: '16px'
      }"
    >
      <n-card
        :bordered="false"
        :style="{
          maxWidth: '60%',
          background: msg.isSelf ? '#d1e7ff' : '#fff',
          borderRadius: msg.isSelf ? '12px 12px 2px 12px' : '12px 12px 12px 2px',
          padding: '8px 14px',
          boxShadow: '0 1px 3px rgba(0,0,0,0.08)'
        }"
      >
        <n-text>{{ msg.content }}</n-text>
        <div :style="{ textAlign: msg.isSelf ? 'right' : 'left', marginTop: '4px' }">
          <n-text depth="3" style="font-size: 11px">{{ msg.time }}</n-text>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
defineProps({ messages: Array })
</script>
