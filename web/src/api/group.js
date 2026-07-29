import request from './request'

export function createGroup(data) {
  return request.post('/group/create', data)
}

export function getGroupList() {
  return request.get('/group/getAll')
}

export function getGroupInfo(groupId) {
  return request.get('/group/info', { params: { groupId } })
}

export function addGroupMember(data) {
  return request.post('/group/member/add', data)
}

export function removeGroupMember(data) {
  return request.post('/group/member/remove', data)
}
