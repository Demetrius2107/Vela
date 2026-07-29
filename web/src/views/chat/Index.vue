<template>
  <div style="height: 100vh; display: flex; flex-direction: column; background: #fff">
    <NavHeader />
    <div ref="mainRef" style="flex: 1; display: flex; overflow: hidden">
      <div :style="{ width: sidebarWidth + 'px', display: 'flex', flexDirection: 'column', background: '#fff', flexShrink: 0, borderRight: '1px solid rgba(0,0,0,0.04)' }">
        <div style="padding: 16px 14px 10px">
          <n-input placeholder="搜索会话或用户..." round clearable size="small">
            <template #prefix><span style="color: #bbb">🔍</span></template>
          </n-input>
        </div>
        <div style="padding: 2px 14px 10px; display: flex; gap: 6px; flex-wrap: wrap">
          <n-tag v-for="f in filters" :key="f.key" size="tiny" round :bordered="false" :style="{ background: activeFilter === f.key ? 'linear-gradient(135deg, #4F6EF7, #7C3AED)' : '#f5f5f5', color: activeFilter === f.key ? '#fff' : '#666', cursor: 'pointer', fontWeight: activeFilter === f.key ? 600 : 400 }" @click="activeFilter = f.key">{{ f.label }}</n-tag>
        </div>
        <div style="flex: 1; overflow-y: auto; padding: 0 8px">
          <div
            v-for="c in filteredConversations" :key="c.id"
            @click="selectConversation(c)"
            @contextmenu.prevent="showConvCtxMenu($event, c)"
            :style="{
              padding: '12px 12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '12px',
              borderRadius: '12px', marginBottom: '3px',
              background: currentConv?.id === c.id ? c.color + '12' : 'transparent',
              borderLeft: '3px solid ' + (currentConv?.id === c.id ? c.color : 'transparent'),
              transition: 'all 0.2s'
            }"
            @mouseenter="$event.currentTarget.style.background = currentConv?.id === c.id ? c.color + '18' : c.color + '08'"
            @mouseleave="$event.currentTarget.style.background = currentConv?.id === c.id ? c.color + '12' : 'transparent'"
          >
            <div style="position: relative; flex-shrink: 0">
              <n-avatar round :color="c.color" size="medium" :style="{ boxShadow: '0 2px 8px ' + c.color + '40' }">{{ c.name[0] }}</n-avatar>
              <div v-if="c.online" style="position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; border-radius: 50%; background: #22C55E; border: 2px solid #fff" />
            </div>
            <div style="flex: 1; min-width: 0">
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span :style="{ fontWeight: 600, fontSize: '14px', color: c.color, display: 'flex', alignItems: 'center', gap: '4px' }">{{ c.name }}<span v-if="c.pinned" style="font-size: 10px">📌</span><span v-if="c.isBot" style="font-size: 10px">🤖</span></span>
                <div style="display: flex; align-items: center; gap: 4px">
                  <span v-if="c.muted" style="font-size: 10px; color: #ccc">🔇</span>
                  <span style="font-size: 11px; color: #bbb">{{ c.time }}</span>
                </div>
              </div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 3px">
                <span style="font-size: 13px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1"><span v-if="c.lastIsSelf" style="color: #bbb">你: </span>{{ c.lastMessage || c.signature || '暂无消息' }}</span>
                <n-badge v-if="c.unread" :value="c.unread" :max="99" style="flex-shrink: 0; margin-left: 4px" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div @mousedown="startResize" style="width: 4px; cursor: col-resize; background: transparent; flex-shrink: 0; position: relative; z-index: 10">
        <div style="width: 1px; height: 100%; background: #e8e8e8; margin: 0 auto" />
      </div>
      <div v-if="currentConv" style="flex: 1; display: flex; flex-direction: column">
        <!-- 顶部彩色渐变条 -->
        <div :style="{ height: '3px', background: 'linear-gradient(90deg, ' + currentConv.color + ', ' + currentConv.color + '88, transparent)', flexShrink: 0 }" />
        <div style="padding: 12px 20px; border-bottom: 1px solid #e8e8e8; display: flex; align-items: center; gap: 10px; background: rgba(255,255,255,0.88); backdropFilter: 'blur(20px)'; WebkitBackdropFilter: 'blur(20px)'">
          <div style="position: relative">
            <n-avatar round :color="currentConv.color" size="small" :style="{ boxShadow: '0 2px 8px ' + currentConv.color + '40' }">{{ currentConv.name[0] }}</n-avatar>
            <div v-if="currentConv.online" style="position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; border-radius: 50%; background: #22C55E; border: 2px solid #fff" />
          </div>
          <div style="flex: 1">
            <div style="font-weight: 600; font-size: 15px; color: #333">{{ currentConv.name }}<span v-if="currentConv.isBot" style="font-size: 11px; color: #722ed1; margin-left: 4px">🤖 Bot</span></div>
            <div style="font-size: 12px; color: #31c451">{{ currentConv.isBot ? '🟢 在线 · 自动回复' : (currentConv.online ? '在线' : '离线') }} {{ currentConv.signature ? '· ' + currentConv.signature : '' }}</div>
          </div>
          <n-button v-if="!currentConv.isGroup" quaternary size="small" circle @click="startAudioCall" title="语音通话">🎤</n-button>
          <n-button v-if="!currentConv.isGroup" quaternary size="small" circle @click="startVideoCall" title="视频通话">📹</n-button>
          <n-button quaternary size="small" circle @click="showInfoPanel = !showInfoPanel">⋮</n-button>
        </div>
        <div style="flex: 1; display: flex; overflow: hidden">
          <div style="flex: 1; display: flex; flex-direction: column; overflow: hidden">
<!-- 消息区：随会话颜色变化 -->
            <div ref="msgListRef" :style="{ flex: 1, padding: '20px', overflowY: 'auto', background: 'linear-gradient(180deg, ' + (currentConv?.color || '#f8f9ff') + '06 0%, ' + (currentConv?.color || '#f0f2f5') + '04 50%, #f0f2f5 100%)' }">
              <div v-if="messages.length === 0" style="text-align: center; margin-top: 40px; color: #bbb"><n-empty description="暂无消息，发送第一条消息吧" /></div>
              <div v-for="m in messages" :key="m.id" :style="{ marginBottom: '16px', animation: 'fadeInUp 0.3s ease' }">
                <div v-if="m.showDate" style="text-align: center; margin: 12px 0">
                  <span style="font-size: 11px; color: #888; background: rgba(0,0,0,0.04); padding: 2px 14px; border-radius: 10px; backdropFilter: blur(8px)">{{ m.dateLabel }}</span>
                </div>
                <div :style="{ display: 'flex', justifyContent: m.isSelf ? 'flex-end' : 'flex-start' }">
                  <!-- 对方消息 -->
                  <div v-if="!m.isSelf" style="display: flex; gap: 8px; max-width: 70%" @contextmenu.prevent="showCtxMenu($event, m)">
                    <n-avatar round :color="currentConv?.color" size="small" style="flex-shrink: 0; margin-top: 4px; boxShadow: '0 2px 6px rgba(0,0,0,0.1)'">{{ currentConv?.name?.[0] }}</n-avatar>
                    <div>
                      <div :style="{ background: '#fff', padding: '10px 16px', borderRadius: '4px 18px 18px 18px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', fontSize: '14px', color: '#1a1a2e', lineHeight: '1.6' }">{{ m.content }}</div>
                      <div style="font-size: 11px; color: #aaa; margin-top: 3px; padding-left: 4px">{{ m.time }}</div>
                    </div>
                  </div>
                  <!-- 自己消息 -->
                  <div v-if="m.isSelf" style="display: flex; gap: 8px; max-width: 70%; flex-direction: row-reverse" @contextmenu.prevent="showCtxMenu($event, m)">
                    <div>
                      <div :style="{ background: 'linear-gradient(135deg, #4F6EF7 0%, #7C3AED 100%)', padding: '10px 16px', borderRadius: '18px 4px 18px 18px', boxShadow: '0 2px 8px rgba(79,110,247,0.2)', fontSize: '14px', color: '#fff', lineHeight: '1.6' }">{{ m.content }}</div>
                      <div style="display: flex; align-items: center; justify-content: flex-end; gap: 4px; margin-top: 3px; padding-right: 4px">
                        <span style="font-size: 11px; color: #aaa">{{ m.time }}</span>
                        <span :style="{ fontSize: '11px', color: m.status === 'read' ? '#4F6EF7' : '#bbb' }">{{ m.status === 'sent' ? '✓' : '✓✓' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div @mousedown="startInputResize" style="height: 4px; cursor: ns-resize; background: transparent; flex-shrink: 0; position: relative; z-index: 10; border-top: 1px solid #e8e8e8">
              <div style="width: 40px; height: 3px; border-radius: 2px; background: #ccc; margin: 1px auto" />
            </div>
            <!-- 输入区域：毛玻璃现代风格 -->
            <div ref="inputAreaRef" :style="{ height: inputHeight + 'px', minHeight: '100px', maxHeight: '220px', padding: '10px 16px 12px', background: 'rgba(255,255,255,0.92)', backdropFilter: 'blur(16px) saturate(1.4)', WebkitBackdropFilter: 'blur(16px) saturate(1.4)', display: 'flex', flexDirection: 'column', flexShrink: 0, borderTop: '1px solid rgba(79,110,247,0.08)' }">
              <!-- Bot 指令提示 -->
              <div v-if="currentConv?.isBot" style="margin-bottom: 6px; padding: 4px 10px; background: linear-gradient(135deg, #EEF1FF, #F5F3FF); border-radius: 8px; display: flex; gap: 6px; flex-wrap: wrap; align-items: center">
                <span style="font-size: 11px; color: #4F6EF7; font-weight: 600">🤖 指令:</span>
                <n-tag size="tiny" round :bordered="false" style="background: #fff; cursor: pointer" @click="insertCommand('/start')">/start</n-tag>
                <n-tag size="tiny" round :bordered="false" style="background: #fff; cursor: pointer" @click="insertCommand('/help')">/help</n-tag>
                <n-tag size="tiny" round :bordered="false" style="background: #fff; cursor: pointer" @click="insertCommand('/ping')">/ping</n-tag>
                <n-tag size="tiny" round :bordered="false" style="background: #fff; cursor: pointer" @click="insertCommand('/echo')">/echo</n-tag>
              </div>
              <!-- 工具栏 -->
              <div style="display: flex; align-items: center; gap: 2px; margin-bottom: 6px">
                <div style="display: flex; align-items: center; gap: 2px; padding: 2px 4px; background: #f5f5f5; border-radius: 10px">
                  <n-button quaternary size="tiny" circle style="font-size: 16px" @click="msg.info('表情功能待开发')">😊</n-button>
                  <n-button quaternary size="tiny" circle style="font-size: 16px" @click="msg.info('图片功能待开发')">🖼</n-button>
                  <n-button quaternary size="tiny" circle style="font-size: 16px" @click="msg.info('附件功能待开发')">📎</n-button>
                </div>
                <div style="flex:1" />
                <n-tag size="tiny" :bordered="false" style="background: #f5f5f5; color: #999; font-size: 11px; margin-right: 8px">⌘+Enter 发送</n-tag>
                <n-button type="primary" size="small" round @click="sendMessage" style="paddingLeft: 16px; paddingRight: 16px; height: 30px; background: linear-gradient(135deg, #4F6EF7, #7C3AED); border: none; boxShadow: 0 2px 8px rgba(79,110,247,0.3)">
                  <template #icon><span style="font-size: 14px">➤</span></template>
                  发送
                </n-button>
              </div>
              <!-- 输入框 -->
              <div style="flex: 1; display: flex; background: #f7f8fa; border-radius: 12px; border: 1px solid #eef0f4; transition: border-color 0.2s, box-shadow 0.2s; padding: 2px" :style="{ boxShadow: inputFocused ? '0 0 0 2px rgba(79,110,247,0.12)' : 'none' }">
                <textarea
                  v-model="inputText"
                  :rows="1"
                  placeholder="输入消息..."
                  style="flex: 1; border: none; outline: none; background: transparent; padding: 8px 12px; font-size: 14px; color: #1a1a2e; font-family: inherit; resize: none; line-height: 1.5"
                  @keydown.enter.prevent="sendMessage"
                  @focus="inputFocused = true"
                  @blur="inputFocused = false"
                />
              </div>
            </div>
          </div>
          <div v-if="showInfoPanel && currentConv" :style="{ width: '300px', borderLeft: '1px solid #e8e8e8', background: '#fafafa', flexShrink: 0, padding: '24px 20px', overflowY: 'auto' }">
            <div style="text-align: center; margin-bottom: 20px">
              <n-avatar round :size="64" :color="currentConv.color" style="margin-bottom: 8px">{{ currentConv.name[0] }}</n-avatar>
              <div style="font-weight: 600; font-size: 18px; color: #333">{{ currentConv.name }}</div>
              <div style="font-size: 12px; color: #31c451; margin-top: 2px">{{ currentConv.online ? '🟢 在线' : '⚪ 离线' }}</div>
              <div style="font-size: 12px; color: #888; margin-top: 4px; padding: 0 8px">{{ currentConv.signature || '这个人很懒什么都没写' }}</div>
            </div>

            <div style="margin-bottom: 16px">
              <n-space justify="center">
                <n-button :type="currentConv.pinned ? 'primary' : 'default'" size="tiny" round @click="toggleConvPin(currentConv)">
                  {{ currentConv.pinned ? '📌 已置顶' : '📌 置顶' }}
                </n-button>
                <n-button :type="currentConv.muted ? 'warning' : 'default'" size="tiny" round @click="toggleConvMute(currentConv)">
                  {{ currentConv.muted ? '🔇 已免打扰' : '🔇 免打扰' }}
                </n-button>
              </n-space>
            </div>

            <n-divider style="margin: 0 0 16px" />

            <div v-if="!currentConv.isGroup" style="margin-bottom: 16px">
              <div style="font-weight: 600; font-size: 13px; color: #666; margin-bottom: 8px">共同群组</div>
              <n-space><n-tag size="tiny" round bordered>项目团队</n-tag><n-tag size="tiny" round bordered>技术交流群</n-tag></n-space>
            </div>

            <div v-if="currentConv.isGroup" style="margin-bottom: 16px">
              <div style="font-weight: 600; font-size: 13px; color: #666; margin-bottom: 8px">群成员 · 8人</div>
              <n-space wrap><n-tag v-for="i in 6" :key="i" size="small" round>成员{{ i }}</n-tag><n-button size="tiny" quaternary>+3</n-button></n-space>
            </div>

            <n-divider style="margin: 0 0 16px" />

            <div style="margin-bottom: 16px">
              <div style="font-weight: 600; font-size: 13px; color: #666; margin-bottom: 8px; display: flex; justify-content: space-between">
                <span>近期图片</span><n-button text size="tiny" style="color: #2080f0">查看全部</n-button>
              </div>
              <n-space>
                <div v-for="i in 4" :key="i" style="width: 56px; height: 56px; border-radius: 8px; background: linear-gradient(135deg, #e8e8e8, #f5f5f5); display: flex; align-items: center; justify-content: center; font-size: 20px">🖼</div>
              </n-space>
            </div>

            <n-divider style="margin: 0 0 16px" />

            <div>
              <div style="font-weight: 600; font-size: 13px; color: #666; margin-bottom: 8px">更多信息</div>
              <div style="font-size: 12px; color: #888; line-height: 2">地区：中国 · 北京<br />备注：无<br />添加时间：2026-01-15</div>
            </div>
          </div>
        </div>
      </div>
      <div v-else style="flex: 1; display: flex; justify-content: center; align-items: center; background: #fafafa">
        <n-empty description="选择一个会话开始聊天"><template #extra><n-button size="small" @click="mockSelectFirst">选择一个</n-button></template></n-empty>
      </div>
    </div>
    <div v-if="ctxMenu.show" :style="{ position: 'fixed', top: ctxMenu.y + 'px', left: ctxMenu.x + 'px', zIndex: 9999, background: '#fff', borderRadius: '8px', boxShadow: '0 4px 20px rgba(0,0,0,0.15)', padding: '4px 0', minWidth: '140px' }" @mouseleave="ctxMenu.show = false">
      <div v-for="item in ctxMenu.items" :key="item.key" @click="handleCtxAction(item.key)" style="padding: 8px 16px; cursor: pointer; font-size: 13px; color: #333; display: flex; align-items: center; gap: 8px" @mouseenter="$event.target.style.background='#f5f5f5'" @mouseleave="$event.target.style.background='transparent'">{{ item.label }}</div>
    </div>
    <!-- 会话右键菜单 -->
    <div v-if="convCtxMenu.show" :style="{ position: 'fixed', top: convCtxMenu.y + 'px', left: convCtxMenu.x + 'px', zIndex: 9999, background: '#fff', borderRadius: '8px', boxShadow: '0 4px 20px rgba(0,0,0,0.15)', padding: '4px 0', minWidth: '150px' }" @mouseleave="convCtxMenu.show = false">
      <div @click="handleConvCtxAction('togglePin')" style="padding: 8px 16px; cursor: pointer; font-size: 13px; color: #333; display: flex; align-items: center; gap: 8px" @mouseenter="$event.target.style.background='#f5f5f5'" @mouseleave="$event.target.style.background='transparent'">{{ convCtxMenu.conv?.pinned ? '📌 取消置顶' : '📌 置顶会话' }}</div>
      <div @click="handleConvCtxAction('toggleMute')" style="padding: 8px 16px; cursor: pointer; font-size: 13px; color: #333; display: flex; align-items: center; gap: 8px" @mouseenter="$event.target.style.background='#f5f5f5'" @mouseleave="$event.target.style.background='transparent'">{{ convCtxMenu.conv?.muted ? '🔇 取消免打扰' : '🔇 消息免打扰' }}</div>
      <div style="height: 1px; background: #e8e8e8; margin: 4px 0" />
      <div @click="handleConvCtxAction('clearMsgs')" style="padding: 8px 16px; cursor: pointer; font-size: 13px; color: #d03050; display: flex; align-items: center; gap: 8px" @mouseenter="$event.target.style.background='#fff1f0'" @mouseleave="$event.target.style.background='transparent'">🗑 清空聊天记录</div>
    </div>
    <CallPanel :status="callStatus" :remoteUserId="remoteUserId" :isVideo="isVideoCall" @accept="handleAccept" @reject="handleReject" @end="endCall" />
  </div>
</template>

<script setup>
import { ref, computed, reactive, nextTick, onBeforeUnmount, onMounted, h } from 'vue'
import { useMessage } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import NavHeader from '../../components/layout/NavHeader.vue'
import CallPanel from '../../components/call/CallPanel.vue'
import { useWebRTC } from '../../utils/webrtc'
import { requestNotificationPermission, showMessageNotification } from '../../utils/notification'

const msg = useMessage()
const route = useRoute()
const router = useRouter()

// WebRTC 通话
const { callStatus, remoteUserId, isVideoCall, startCall, acceptCall, rejectCall, endCall, registerHandlers } = useWebRTC()

function startAudioCall() { if (currentConv.value) startCall(currentConv.value.id, false) }
function startVideoCall() { if (currentConv.value) startCall(currentConv.value.id, true) }
function handleAccept() { acceptCall(window.__pendingCall) }
function handleReject() { if (window.__pendingCall) rejectCall(window.__pendingCall) }

onMounted(() => {
  registerHandlers()
  requestNotificationPermission()
  // 处理从"我的Bot"跳转过来的 botId 参数
  const botId = route.query.botId
  const botName = route.query.botName
  if (botId) {
    const existing = conversations.value.find(c => c.botId === botId)
    if (existing) {
      selectConversation(existing)
    } else {
      // 如果是新 Bot，动态添加一个会话
      const newBot = {
        id: Date.now(), name: botName || botId, lastMessage: '你好，我是 ' + (botName || botId), time: '刚刚',
        color: '#722ed1', unread: 0, online: true, pinned: false, muted: false, lastIsSelf: false,
        isGroup: false, isBot: true, signature: 'Bot 助手', botId: botId
      }
      conversations.value.push(newBot)
      selectConversation(newBot)
    }
    // 清除 URL 参数
    router.replace('/chat')
  }
})

const currentConv = ref(null)
const inputText = ref('')
const msgListRef = ref(null)
const mainRef = ref(null)
const activeFilter = ref('all')
const sidebarWidth = ref(340)
const isResizing = ref(false)
const showInfoPanel = ref(false)
const inputHeight = ref(100)

let inputResizeStart = 0

const ctxMenu = reactive({ show: false, x: 0, y: 0, msg: null, items: [] })
const convCtxMenu = reactive({ show: false, x: 0, y: 0, conv: null })
const inputFocused = ref(false)
// 消息草稿：conversationId -> 输入文本
const inputDrafts = reactive({})

const filters = [
  { key: 'all', label: '全部' }, { key: 'unread', label: '未读' },
  { key: 'group', label: '群聊' }, { key: 'friend', label: '好友' }
]

const filteredConversations = computed(() => {
  let list = conversations.value
  if (activeFilter.value === 'unread') list = list.filter(c => c.unread > 0)
  if (activeFilter.value === 'group') list = list.filter(c => c.isGroup)
  if (activeFilter.value === 'friend') list = list.filter(c => !c.isGroup)
  // 置顶会话排在最前
  return [...list].sort((a, b) => {
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    return 0
  })
})

const conversations = ref([
  { id: 1, name: '张三', lastMessage: '好的明天见', time: '10:30', color: '#2080f0', unread: 3, online: true, pinned: true, muted: false, lastIsSelf: false, isGroup: false, signature: '前端开发中，有事请留言' },
  { id: 2, name: '项目团队', lastMessage: '收到，我改一下方案', time: '09:15', color: '#18a058', unread: 0, online: false, pinned: false, muted: true, lastIsSelf: true, isGroup: true, signature: '8人 · 日常项目沟通' },
  { id: 3, name: '李四', lastMessage: '文件已发你邮箱了', time: '昨天', color: '#d03050', unread: 1, online: true, pinned: false, muted: false, lastIsSelf: false, isGroup: false, signature: '后端架构，专注高并发' },
  { id: 4, name: '产品讨论组', lastMessage: '需求文档已更新', time: '昨天', color: '#f0a020', unread: 5, online: false, pinned: false, muted: false, lastIsSelf: true, isGroup: true, signature: '5人 · 产品需求讨论' },
  { id: 5, name: '王五', lastMessage: '周末打球不', time: '周三', color: '#909399', unread: 0, online: false, pinned: false, muted: false, lastIsSelf: false, isGroup: false, signature: '产品经理' },
  // Bot 会话
  { id: 100, name: '天气助手', lastMessage: '今天晴转多云，25-32°C', time: '昨天', color: '#722ed1', unread: 0, online: true, pinned: false, muted: false, lastIsSelf: false, isGroup: false, isBot: true, signature: '实时天气查询，支持全国城市', botId: 'weather-bot' },
  { id: 101, name: '翻译助手', lastMessage: '你好 → Hello', time: '前天', color: '#13c2c2', unread: 0, online: true, pinned: false, muted: false, lastIsSelf: true, isGroup: false, isBot: true, signature: '中英日韩多语言翻译', botId: 'translate-bot' },
])

const messages = ref([])

function startResize(e) {
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

function onResize(e) {
  if (!isResizing.value || !mainRef.value) return
  let newWidth = e.clientX - mainRef.value.getBoundingClientRect().left
  sidebarWidth.value = Math.max(200, Math.min(500, newWidth))
}

function stopResize() {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

function startInputResize(e) {
  inputResizeStart = e.clientY
  document.addEventListener('mousemove', onInputResize)
  document.addEventListener('mouseup', stopInputResize)
  document.body.style.cursor = 'ns-resize'
  document.body.style.userSelect = 'none'
}

function onInputResize(e) {
  const delta = inputResizeStart - e.clientY
  inputHeight.value = Math.max(80, Math.min(200, inputHeight.value + delta))
  inputResizeStart = e.clientY
}

function stopInputResize() {
  document.removeEventListener('mousemove', onInputResize)
  document.removeEventListener('mouseup', stopInputResize)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

onBeforeUnmount(() => { stopResize(); stopInputResize() })

function mockSelectFirst() { selectConversation(conversations.value[0]) }

function selectConversation(conv) {
  // 保存当前会话的草稿
  if (currentConv.value) {
    inputDrafts[currentConv.value.id] = inputText.value
  }
  currentConv.value = conv; conv.unread = 0
  // 恢复新会话的草稿
  inputText.value = inputDrafts[conv.id] || ''
  const n = new Date(); const t = `${String(n.getHours()).padStart(2,'0')}:${String(n.getMinutes()).padStart(2,'0')}`
  messages.value = [
    { id: 1, content: '你好，最近怎么样？', isSelf: false, time: '10:00', status: 'read', showDate: true, dateLabel: '今天' },
    { id: 2, content: '挺好的，刚忙完一个项目', isSelf: true, time: '10:01', status: 'read' },
    { id: 3, content: '对了，明天的会议改到下午2点了', isSelf: false, time: '10:02', status: 'read' },
    { id: 4, content: '好的收到，地点不变吧？', isSelf: true, time: '10:05', status: 'delivered' },
    { id: 5, content: '嗯，还是3楼会议室', isSelf: false, time: '10:06', status: 'read' },
    { id: 6, content: '好的，准时到', isSelf: true, time: t, status: 'sent' }
  ]
  nextTick(() => { if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight })
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !currentConv.value) return
  const n = new Date(); const t = `${String(n.getHours()).padStart(2,'0')}:${String(n.getMinutes()).padStart(2,'0')}`
  messages.value.push({ id: Date.now(), content: text, isSelf: true, time: t, status: 'sent' })
  currentConv.value.lastMessage = text; currentConv.value.lastIsSelf = true; currentConv.value.time = '刚刚'
  inputText.value = ''
  // 清除草稿
  delete inputDrafts[currentConv.value.id]
  nextTick(() => { if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight })
}

function showCtxMenu(e, m) {
  ctxMenu.msg = m; ctxMenu.x = e.clientX; ctxMenu.y = e.clientY
  ctxMenu.items = [
    { key: 'copy', label: '复制' },
    { key: 'favorite', label: '⭐ 收藏' },
    ...(m.isSelf ? [{ key: 'recall', label: '撤回' }] : []),
    { key: 'delete', label: '删除' }
  ]
  ctxMenu.show = true
}

function handleCtxAction(key) {
  const m = ctxMenu.msg; ctxMenu.show = false
  if (key === 'copy') navigator.clipboard.writeText(m.content).then(() => msg.success('已复制'))
  if (key === 'favorite') {
    msg.success('⭐ 已收藏')
    // 收藏逻辑：后续对接 /v1/favorite/add
  }
  if (key === 'recall') { messages.value = messages.value.filter(x => x.id !== m.id); msg.success('已撤回') }
  if (key === 'delete') { messages.value = messages.value.filter(x => x.id !== m.id); msg.success('已删除') }
}

function showConvCtxMenu(e, conv) {
  convCtxMenu.conv = conv
  convCtxMenu.x = e.clientX
  convCtxMenu.y = e.clientY
  convCtxMenu.show = true
}

function handleConvCtxAction(key) {
  const conv = convCtxMenu.conv
  convCtxMenu.show = false
  if (key === 'togglePin') { toggleConvPin(conv) }
  if (key === 'toggleMute') { toggleConvMute(conv) }
  if (key === 'clearMsgs') {
    messages.value = []
    msg.success('聊天记录已清空')
  }
}

function toggleConvPin(conv) {
  conv.pinned = !conv.pinned
  msg.success(conv.pinned ? '已置顶' : '已取消置顶')
}

function toggleConvMute(conv) {
  conv.muted = !conv.muted
  msg.success(conv.muted ? '已开启免打扰' : '已关闭免打扰')
}

function insertCommand(cmd) {
  inputText.value = cmd + ' '
  // 聚焦输入框
  nextTick(() => {
    const textarea = document.querySelector('.n-input textarea')
    if (textarea) textarea.focus()
  })
}
</script>
