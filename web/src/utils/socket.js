import { VELA } from './constants'

let ws = null
let heartbeatTimer = null
const listeners = new Map()

export function connect(userId, token) {
  if (ws) return
  ws = new WebSocket(`${VELA.WS_URL}?userId=${userId}&token=${token}`)

  ws.onopen = () => {
    console.log('[Vela WS] 已连接')
    startHeartbeat()
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      const handler = listeners.get(data.command)
      if (handler) handler(data)
    } catch (e) {
      console.warn('[Vela WS] 消息解析失败', e)
    }
  }

  ws.onclose = () => {
    console.log('[Vela WS] 已断开')
    stopHeartbeat()
    ws = null
    setTimeout(() => connect(userId, token), 3000)
  }
}

export function disconnect() {
  stopHeartbeat()
  if (ws) {
    ws.close()
    ws = null
  }
}

export function send(data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(data))
  }
}

export function on(command, handler) {
  listeners.set(command, handler)
}

export function off(command) {
  listeners.delete(command)
}

function startHeartbeat() {
  heartbeatTimer = setInterval(() => {
    if (ws) ws.send(JSON.stringify({ type: 'heartbeat' }))
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}
