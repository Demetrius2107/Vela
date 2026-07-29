const { contextBridge } = require('electron')

// 暴露给渲染进程的 API
contextBridge.exposeInMainWorld('velaDesktop', {
  platform: process.platform,
  isElectron: true,
})
