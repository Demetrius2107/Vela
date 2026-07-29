<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <div class="vela-app">
          <router-view />
        </div>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { onMounted } from 'vue'
import { velaTheme } from './utils/theme'
import { useFeatureFlagStore } from './stores/featureFlags'

const themeOverrides = velaTheme.common
const featureFlagStore = useFeatureFlagStore()

onMounted(() => {
  featureFlagStore.loadFlags()
})
</script>

<style>
/* ===== Reset ===== */
* { margin: 0; padding: 0; box-sizing: border-box; }

/* ===== 全局 ===== */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ecf1 100%);
  color: #1a1a2e;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.vela-app {
  min-height: 100vh;
}

/* ===== 毛玻璃卡片 ===== */
.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(24px) saturate(1.2);
  -webkit-backdrop-filter: blur(24px) saturate(1.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(79, 110, 247, 0.06);
}

.glass-card:hover {
  box-shadow: 0 8px 32px rgba(79, 110, 247, 0.10);
  background: rgba(255, 255, 255, 0.78);
}

/* ===== 卡片通用样式 ===== */
.modern-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.modern-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

/* ===== 苹果风格滚动条 ===== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.20);
}

/* ===== 选中文本 ===== */
::selection {
  background: rgba(79, 110, 247, 0.15);
}

/* ===== 渐变品牌色 ===== */
.gradient-text {
  background: linear-gradient(135deg, #4F6EF7 0%, #7C3AED 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.gradient-bg {
  background: linear-gradient(135deg, #4F6EF7 0%, #7C3AED 100%);
}

/* ===== 平滑动画 ===== */
.fade-in-up {
  animation: fadeInUp 0.4s ease;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in {
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ===== Naive UI 覆盖 ===== */
.n-input {
  --n-border-radius: 10px !important;
}

.n-button--round {
  border-radius: 24px !important;
}

.n-card {
  border-radius: 14px !important;
  transition: box-shadow 0.2s ease !important;
}

.n-tag {
  --n-border-radius: 8px !important;
}
</style>
