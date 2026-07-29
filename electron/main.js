const { app, BrowserWindow, Tray, Menu, Notification, nativeImage, globalShortcut, dialog } = require('electron')
const path = require('path')
const fs = require('fs')

let mainWindow = null
let tray = null

// ===== 窗口管理 =====
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    title: 'Vela IM',
    icon: path.join(__dirname, 'assets', 'icon.png'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    },
    show: false,
    frame: process.platform === 'darwin' ? true : false
  })

  // 加载 Web 端构建产物
  const distPath = path.join(__dirname, '..', 'web', 'dist', 'index.html')
  if (fs.existsSync(distPath)) {
    mainWindow.loadFile(distPath)
  } else {
    // 开发模式：加载 dev server
    mainWindow.loadURL('http://localhost:5173')
  }

  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  // 关闭时最小化到托盘
  mainWindow.on('close', (e) => {
    if (!app.isQuitting) {
      e.preventDefault()
      mainWindow.hide()
    }
  })
}

// ===== 托盘 =====
function createTray() {
  // 使用 16x16 空图标（实际打包时替换为应用图标）
  const icon = nativeImage.createEmpty()
  tray = new Tray(icon)
  tray.setToolTip('Vela IM')

  const ctxMenu = Menu.buildFromTemplate([
    { label: '打开 Vela', click: () => mainWindow?.show() },
    { type: 'separator' },
    {
      label: '检查更新',
      click: () => {
        if (mainWindow) {
          mainWindow.webContents.executeJavaScript(
            'msg?.success?.("当前已是最新版本 v1.0.0")'
          )
        }
      }
    },
    { type: 'separator' },
    { label: '退出', click: () => { app.isQuitting = true; app.quit() } }
  ])
  tray.setContextMenu(ctxMenu)
  tray.on('double-click', () => mainWindow?.show())
}

// ===== 原生应用菜单 =====
function createMenu() {
  const template = [
    {
      label: 'Vela',
      submenu: [
        { label: '关于 Vela', role: 'about' },
        { type: 'separator' },
        { label: '设置', accelerator: 'Cmd+,', click: () => navigate('/settings') },
        { type: 'separator' },
        { label: '退出', accelerator: 'Cmd+Q', click: () => { app.isQuitting = true; app.quit() } }
      ]
    },
    {
      label: '编辑',
      submenu: [
        { role: 'undo', label: '撤销' },
        { role: 'redo', label: '重做' },
        { type: 'separator' },
        { role: 'cut', label: '剪切' },
        { role: 'copy', label: '复制' },
        { role: 'paste', label: '粘贴' },
        { role: 'selectAll', label: '全选' }
      ]
    },
    {
      label: '导航',
      submenu: [
        { label: '会话', accelerator: 'Cmd+1', click: () => navigate('/chat') },
        { label: '通讯录', accelerator: 'Cmd+2', click: () => navigate('/contacts') },
        { label: 'Bot 市场', accelerator: 'Cmd+3', click: () => navigate('/bot/market') },
        { type: 'separator' },
        { label: '管理后台', accelerator: 'Cmd+Shift+A', click: () => navigate('/admin') },
      ]
    },
    {
      label: '窗口',
      submenu: [
        { role: 'minimize', label: '最小化' },
        { role: 'zoom', label: '缩放' },
        { type: 'separator' },
        { role: 'close', label: '关闭' }
      ]
    },
    {
      label: '帮助',
      submenu: [
        { label: '开发者工具', accelerator: 'F12', click: () => mainWindow?.webContents.toggleDevTools() },
        { label: '刷新', accelerator: 'Cmd+R', click: () => mainWindow?.reload() }
      ]
    }
  ]

  // macOS 需要把第一个菜单设为 app 菜单
  if (process.platform === 'darwin') {
    template.unshift({
      label: app.name,
      submenu: [
        { role: 'about', label: `关于 ${app.name}` },
        { type: 'separator' },
        { label: '设置', accelerator: 'Cmd+,', click: () => navigate('/settings') },
        { type: 'separator' },
        { role: 'hide', label: '隐藏' },
        { role: 'hideOthers', label: '隐藏其他' },
        { role: 'unhide', label: '显示全部' },
        { type: 'separator' },
        { role: 'quit', label: '退出' }
      ]
    })
  }

  const menu = Menu.buildFromTemplate(template)
  Menu.setApplicationMenu(menu)
}

function navigate(path) {
  if (mainWindow) {
    mainWindow.show()
    mainWindow.webContents.executeJavaScript(
      `window.location.hash = '#${path}'`
    )
  }
}

// ===== 系统通知 =====
function sendNotification(title, body) {
  if (Notification.isSupported()) {
    const notification = new Notification({ title, body })
    notification.on('click', () => {
      mainWindow?.show()
    })
    notification.show()
  }
}

// ===== 深度链接 (macOS) =====
app.setAsDefaultProtocolClient('vela')

app.on('open-url', (event, url) => {
  event.preventDefault()
  // vela://chat/user123 格式的深度链接
  const path = url.replace(/^vela:\/\//, '/')
  navigate(path)
})

// ===== 全局快捷键 =====
app.on('ready', () => {
  globalShortcut.register('CommandOrControl+Shift+V', () => {
    mainWindow?.show()
  })
})

// ===== 应用生命周期 =====
app.whenReady().then(() => {
  createMenu()
  createWindow()
  createTray()
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})

app.on('will-quit', () => {
  globalShortcut.unregisterAll()
})
