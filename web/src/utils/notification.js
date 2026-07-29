/**
 * Web Notification API 封装，用于 IM 消息的桌面推送通知。
 */
export function requestNotificationPermission() {
  if (!('Notification' in window)) {
    console.log('[Vela Notify] 浏览器不支持 Notification API')
    return
  }
  if (Notification.permission === 'default') {
    Notification.requestPermission()
  }
}

export function showMessageNotification({ title, body, tag, onClick }) {
  if (!('Notification' in window) || Notification.permission !== 'granted') return

  const notification = new Notification(title || 'Vela IM', {
    body: body || '新消息',
    tag: tag || 'vela-message',
    icon: '/favicon.svg',
    silent: false,
  })

  notification.onclick = () => {
    window.focus()
    if (onClick) onClick()
    notification.close()
  }
}
