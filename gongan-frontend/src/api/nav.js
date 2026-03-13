import request from './request'

/**
 * 智能导航 API
 */

// 意图识别 - 返回最匹配的候选结果
export function recognizeIntent(query) {
  return request.post('/nav/recognize', null, { params: { query } })
}

// 搜索建议 - 实时输入建议
export function getSuggestions(query, limit = 5) {
  return request.get('/nav/suggestions', { params: { query, limit } })
}

// 智能推荐 - 基于历史和热门
export function getRecommendations(limit = 6) {
  return request.get('/nav/recommendations', { params: { limit } })
}

// 快捷指令列表
export function getShortcuts() {
  return request.get('/nav/shortcuts')
}

// 热门导航
export function getHotNavigations(limit = 5) {
  return request.get('/nav/hot', { params: { limit } })
}

// 导航历史
export function getIntentHistory(limit = 10) {
  return request.get('/nav/history', { params: { limit } })
}

// 获取所有意图列表
export function listIntents() {
  return request.get('/nav/intent/list')
}

// 同步菜单到意图表
export function syncMenusToIntents() {
  return request.post('/nav/sync')
}

// 别名，保持兼容
export const navigate = recognizeIntent
