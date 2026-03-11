import request from './request'

export function getOrgList(params) {
  return request.get('/system/org/list', { params })
}

export function getOrgTree() {
  return request.get('/system/org/tree')
}

export function getOrgById(id) {
  return request.get(`/system/org/${id}`)
}

export function addOrg(data) {
  return request.post('/system/org', data)
}

export function updateOrg(data) {
  return request.put('/system/org', data)
}

export function deleteOrg(id) {
  return request.delete(`/system/org/${id}`)
}
