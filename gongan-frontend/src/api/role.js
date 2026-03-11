import request from './request'

export function getRoleList(params) {
  return request.get('/system/role/list', { params })
}

export function getAllRoles() {
  return request.get('/system/role/all')
}

export function getRoleById(id) {
  return request.get(`/system/role/${id}`)
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}

export function assignMenus(roleId, menuIds) {
  return request.post(`/system/role/${roleId}/menus`, menuIds)
}

export function getRoleMenus(roleId) {
  return request.get(`/system/role/${roleId}/menus`)
}
