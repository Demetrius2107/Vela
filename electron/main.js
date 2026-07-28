const { app, BrowserWindow, Tray, Menu, Notification, nativeImage } = require('electron')
const path = require('path')

let mainWindow = null
let tray = null

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    title: 'Vela IM',
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    },
    show: false
  })

  // 加载 Web 端构建产物
  mainWindow.loadFile(path.join(__dirname, '..', 'web', 'dist', 'index.html'))

  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  mainWindow.on('close', (e) => {
    if (!app.isQuitting) {
      e.preventDefault()
      mainWindow.hide()
    }
  })
}

function createTray() {
  tray = new Tray(nativeImage.createEmpty())
  tray.setToolTip('Vela IM')

  const ctxMenu = Menu.buildFromTemplate([
    { label: '打开 Vela', click: () => mainWindow?.show() },
    { type: 'separator' },
    { label: '退出', click: () => { app.isQuitting = true; app.quit() } }
  ])
  tray.setContextMenu(ctxMenu)
  tray.on('click', () => mainWindow?.show())
}

app.whenReady().then(() => {
  createWindow()
  createTray()
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})
