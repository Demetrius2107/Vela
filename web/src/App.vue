<template>
  <n-message-provider>
    <router-view />
  </n-message-provider>
</template>

<script setup>
import { darkTheme } from 'naive-ui'
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
