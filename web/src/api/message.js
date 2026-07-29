import request from './request'

export function sendP2PMessage(data) {
  return request.post('/message/send', data)
}

export function sendGroupMessage(data) {
  return request.post('/message/group/send', data)
}

export function getMessageHistory(data) {
  return request.post('/message/history', data)
}
