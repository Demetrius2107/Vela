<template>
  <div style="height: 100vh; display: flex; flex-direction: column; background: #fff">
    <NavHeader />
    <div ref="mainRef" style="flex: 1; display: flex; overflow: hidden">
      <div :style="{ width: sidebarWidth + 'px', display: 'flex', flexDirection: 'column', background: '#fafafa', flexShrink: 0 }">
        <div style="padding: 16px 12px 8px">
          <n-input placeholder="搜索会话或用户..." round clearable size="small" />
        </div>
        <div style="padding: 4px 12px; display: flex; gap: 6px; flex-wrap: wrap">
          <n-tag v-for="f in filters" :key="f.key" size="tiny" round :bordered="false" :style="{ background: activeFilter === f.key ? '#e8f0fe' : '#f0f0f0', color: activeFilter === f.key ? '#2080f0' : '#666', cursor: 'pointer' }" @click="activeFilter = f.key">{{ f.label }}</n-tag>
        </div>
        <div style="flex: 1; overflow-y: auto">
          <div v-for="c in filteredConversations" :key="c.id" @click="selectConversation(c)" :style="{ padding: '14px 16px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '12px', background: currentConv?.id === c.id ? '#e8f0fe' : 'transparent', borderLeft: currentConv?.id === c.id ? '3px solid #2080f0' : '3px solid transparent' }">
            <div style="position: relative; flex-shrink: 0">
              <n-avatar round :color="c.color" size="medium">{{ c.name[0] }}</n-avatar>
              <div v-if="c.online" style="position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; border-radius: 50%; background: #31c451; border: 2px solid #fafafa" />
            </div>
            <div style="flex: 1; min-width: 0">
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-weight: 600; font-size: 14px; color: #333; display: flex; align-items: center; gap: 4px">{{ c.name }}<span v-if="c.pinned" style="font-size: 11px">📌</span></span>
                <div style="display: flex; align-items: center; gap: 4px">
                  <span v-if="c.muted" style="font-size: 11px; color: #bbb">🔇</span>
                  <span style="font-size: 11px; color: #999">{{ c.time }}</span>
                </div>
              </div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 2px">
                <span style="font-size: 13px; color: #888; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1"><span v-if="c.lastIsSelf" style="color: #999">你: </span>{{ c.lastMessage || c.signature || '暂无消息' }}</span>
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
        <div style="padding: 14px 20px; border-bottom: 1px solid #e8e8e8; display: flex; align-items: center; gap: 10px; background: #fff">
          <div style="position: relative">
            <n-avatar round :color="currentConv.color" size="small">{{ currentConv.name[0] }}</n-avatar>
            <div v-if="currentConv.online" style="position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; border-radius: 50%; background: #31c451; border: 2px solid #fff" />
          </div>
          <div style="flex: 1">
            <div style="font-weight: 600; font-size: 15px; color: #333">{{ currentConv.name }}</div>
            <div style="font-size: 12px; color: #31c451">{{ currentConv.online ? '在线' : '离线' }} · {{ currentConv.signature || '这个人很懒什么都没写' }}</div>
          </div>
          <n-button v-if="!currentConv.isGroup" quaternary size="small" circle @click="startAudioCall" title="语音通话">🎤</n-button>
          <n-button v-if="!currentConv.isGroup" quaternary size="small" circle @click="startVideoCall" title="视频通话">📹</n-button>
          <n-button quaternary size="small" circle @click="showInfoPanel = !showInfoPanel">⋮</n-button>
        </div>
        <div style="flex: 1; display: flex; overflow: hidden">
          <div style="flex: 1; display: flex; flex-direction: column; overflow: hidden">
            <div ref="msgListRef" style="flex: 1; padding: 20px; overflow-y: auto; background: #f5f5f5">
              <div v-if="messages.length === 0" style="text-align: center; margin-top: 40px; color: #bbb"><n-empty description="暂无消息，发送第一条消息吧" /></div>
              <div v-for="m in messages" :key="m.id" :style="{ marginBottom: '16px' }">
                <div v-if="m.showDate" style="text-align: center; margin: 8px 0"><span style="font-size: 12px; color: #999; background: #e8e8e8; padding: 2px 12px; border-radius: 8px">{{ m.dateLabel }}</span></div>
                <div :style="{ display: 'flex', justifyContent: m.isSelf ? 'flex-end' : 'flex-start' }">
                  <div v-if="!m.isSelf" style="display: flex; gap: 8px; max-width: 70%" @contextmenu.prevent="showCtxMenu($event, m)">
                    <n-avatar round :color="currentConv?.color" size="small" style="flex-shrink: 0; margin-top: 4px">{{ currentConv?.name?.[0] }}</n-avatar>
                    <div><div :style="{ background: '#fff', padding: '10px 14px', borderRadius: '4px 16px 16px 16px', boxShadow: '0 1px 2px rgba(0,0,0,0.06)', fontSize: '14px', color: '#333', lineHeight: '1.5' }">{{ m.content }}</div><div style="font-size: 11px; color: #aaa; margin-top: 2px; padding-left: 4px">{{ m.time }}</div></div>
                  </div>
                  <div v-if="m.isSelf" style="display: flex; gap: 8px; max-width: 70%; flex-direction: row-reverse" @contextmenu.prevent="showCtxMenu($event, m)">
                    <div><div :style="{ background: '#d1e7ff', padding: '10px 14px', borderRadius: '16px 4px 16px 16px', boxShadow: '0 1px 2px rgba(0,0,0,0.06)', fontSize: '14px', color: '#333', lineHeight: '1.5' }">{{ m.content }}</div><div style="display: flex; align-items: center; justify-content: flex-end; gap: 4px; margin-top: 2px; padding-right: 4px"><span style="font-size: 11px; color: #aaa">{{ m.time }}</span><span :style="{ fontSize: '11px', color: m.status === 'read' ? '#2080f0' : '#aaa' }">{{ m.status === 'sent' ? '✓' : '✓✓' }}</span></div></div>
                  </div>
                </div>
              </div>
            </div>
            <div @mousedown="startInputResize" style="height: 4px; cursor: ns-resize; background: transparent; flex-shrink: 0; position: relative; z-index: 10; border-top: 1px solid #e8e8e8">
              <div style="width: 40px; height: 3px; border-radius: 2px; background: #ccc; margin: 1px auto" />
            </div>
            <div ref="inputAreaRef" :style="{ height: inputHeight + 'px', minHeight: '80px', maxHeight: '200px', padding: '8px 16px 12px', background: 'rgba(255,255,255,0.85)', backdropFilter: 'blur(12px)', WebkitBackdropFilter: 'blur(12px)', display: 'flex', flexDirection: 'column', flexShrink: 0 }">
              <div style="display: flex; align-items: center; gap: 2px; margin-bottom: 4px">
                <n-button quaternary size="tiny" circle @click="msg.info('表情功能待开发')">😊</n-button>
                <n-button quaternary size="tiny" circle @click="msg.info('图片功能待开发')">🖼</n-button>
                <n-button quaternary size="tiny" circle @click="msg.info('附件功能待开发')">📎</n-button>
                <n-tag size="tiny" round bordered style="marginLeft: 4px">Ctrl+Enter 发送</n-tag>
                <div style="flex:1" />
                <n-button type="primary" size="tiny" round @click="sendMessage" style="paddingLeft: 12px; paddingRight: 12px; height: 28px">发送</n-button>
              </div>
              <div style="flex: 1; display: flex">
                <n-input v-model:value="inputText" type="textarea" :rows="1" placeholder="输入消息..." size="small" :style="{ flex: 1, height: '100%', borderRadius: '10px' }" @keydown.enter.prevent="sendMessage" />
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
    <CallPanel :status="callStatus" :remoteUserId="remoteUserId" :isVideo="isVideoCall" @accept="handleAccept" @reject="handleReject" @end="endCall" />
  </div>
</template>

<script setup>
import { ref, computed, reactive, nextTick, onBeforeUnmount, onMounted, h } from 'vue'
import { useMessage } from 'naive-ui'
import NavHeader from '../../components/layout/NavHeader.vue'
import CallPanel from '../../components/call/CallPanel.vue'
import { useWebRTC } from '../../utils/webrtc'
import { requestNotificationPermission, showMessageNotification } from '../../utils/notification'

const msg = useMessage()

// WebRTC 通话
const { callStatus, remoteUserId, isVideoCall, startCall, acceptCall, rejectCall, endCall, registerHandlers } = useWebRTC()

function startAudioCall() { if (currentConv.value) startCall(currentConv.value.id, false) }
function startVideoCall() { if (currentConv.value) startCall(currentConv.value.id, true) }
function handleAccept() { acceptCall(window.__pendingCall) }
function handleReject() { if (window.__pendingCall) rejectCall(window.__pendingCall) }

onMounted(() => {
  registerHandlers()
  requestNotificationPermission()
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

const filters = [
  { key: 'all', label: '全部' }, { key: 'unread', label: '未读' },
  { key: 'group', label: '群聊' }, { key: 'friend', label: '好友' }
]

const filteredConversations = computed(() => {
  let list = conversations.value
  if (activeFilter.value === 'unread') list = list.filter(c => c.unread > 0)
  if (activeFilter.value === 'group') list = list.filter(c => c.isGroup)
  if (activeFilter.value === 'friend') list = list.filter(c => !c.isGroup)
  return list
})

const conversations = ref([
  { id: 1, name: '张三', lastMessage: '好的明天见', time: '10:30', color: '#2080f0', unread: 3, online: true, pinned: true, muted: false, lastIsSelf: false, isGroup: false, signature: '前端开发中，有事请留言' },
  { id: 2, name: '项目团队', lastMessage: '收到，我改一下方案', time: '09:15', color: '#18a058', unread: 0, online: false, pinned: false, muted: true, lastIsSelf: true, isGroup: true, signature: '8人 · 日常项目沟通' },
  { id: 3, name: '李四', lastMessage: '文件已发你邮箱了', time: '昨天', color: '#d03050', unread: 1, online: true, pinned: false, muted: false, lastIsSelf: false, isGroup: false, signature: '后端架构，专注高并发' },
  { id: 4, name: '产品讨论组', lastMessage: '需求文档已更新', time: '昨天', color: '#f0a020', unread: 5, online: false, pinned: false, muted: false, lastIsSelf: true, isGroup: true, signature: '5人 · 产品需求讨论' },
  { id: 5, name: '王五', lastMessage: '周末打球不', time: '周三', color: '#909399', unread: 0, online: false, pinned: false, muted: false, lastIsSelf: false, isGroup: false, signature: '产品经理' }
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
  currentConv.value = conv; conv.unread = 0
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
  nextTick(() => { if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight })
}

function showCtxMenu(e, m) {
  ctxMenu.msg = m; ctxMenu.x = e.clientX; ctxMenu.y = e.clientY
  ctxMenu.items = [
    { key: 'copy', label: '复制' }, ...(m.isSelf ? [{ key: 'recall', label: '撤回' }] : []), { key: 'delete', label: '删除' }
  ]
  ctxMenu.show = true
}

function handleCtxAction(key) {
  const m = ctxMenu.msg; ctxMenu.show = false
  if (key === 'copy') navigator.clipboard.writeText(m.content).then(() => msg.success('已复制'))
  if (key === 'recall') { messages.value = messages.value.filter(x => x.id !== m.id); msg.success('已撤回') }
  if (key === 'delete') { messages.value = messages.value.filter(x => x.id !== m.id); msg.success('已删除') }
}
</script>
