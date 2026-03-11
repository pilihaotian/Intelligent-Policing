import request from './request'

export function getUserList(params) {
  return request.get('/system/user/list', { params })
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function assignRoles(userId, roleIds) {
  return request.post(`/system/user/${userId}/roles`, roleIds)
}

export function getRoleList(params) {
  return request.get('/system/role/list', { params })
}

export function getOrgTree() {
  return request.get('/system/org/tree')
}

export function getUserMenus() {
  return request.get('/auth/menus')
}

export function getMenuTree() {
  return request.get('/system/menu/tree')
}