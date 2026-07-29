import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/v1',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('vela_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  res => {
    if (res.data.code === 200) return res.data
    return Promise.reject(new Error(res.data.msg || '请求失败'))
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('vela_token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default request
