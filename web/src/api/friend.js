import request from './request'

export function getFriendList() {
  return request.get('/friendship/getAll')
}

export function addFriend(data) {
  return request.post('/friendship/add', data)
}

export function deleteFriend(data) {
  return request.post('/friendship/delete', data)
}

export function getFriendRequests() {
  return request.get('/friendship/request/list')
}

export function approveFriendRequest(data) {
  return request.post('/friendship/request/approve', data)
}
