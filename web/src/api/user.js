import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function getUserInfo(userId) {
  return request.get('/user/info', { params: { userId } })
}

export function updateUserInfo(data) {
  return request.post('/user/update', data)
}
