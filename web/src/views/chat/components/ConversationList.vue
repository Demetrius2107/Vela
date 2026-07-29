<template>
  <div style="padding: 12px">
    <n-input placeholder="搜索会话..." round clearable>
      <template #prefix><n-icon><search-outline /></n-icon></template>
    </n-input>
  </div>
  <n-list hoverable clickable>
    <n-list-item
      v-for="item in list"
      :key="item.id"
      :class="{ active: item.id === activeId }"
      @click="$emit('select', item)"
      style="cursor: pointer"
    >
      <template #prefix>
        <n-badge :value="item.unread" :max="99">
          <n-avatar round :color="item.color">{{ item.name[0] }}</n-avatar>
        </n-badge>
      </template>
      <n-thing :title="item.name" :description="item.lastMessage">
        <template #description-extra>
          <n-text depth="3" style="font-size: 12px">{{ item.time }}</n-text>
        </template>
      </n-thing>
    </n-list-item>
  </n-list>
</template>

<script setup>
import { NIcon } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'

defineProps({ list: Array, activeId: [String, Number] })
defineEmits(['select'])
</script>

<style scoped>
.active { background: #e8f0fe; }
</style>
