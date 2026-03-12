import request from './request'

export function navigate(userInput) {
  return request.post('/nav/recognize', null, { params: { query: userInput } })
}

export function getIntentHistory(limit = 10) {
  return request.get('/nav/history', { params: { limit } })
}

export function listIntents() {
  return request.get('/nav/intent/list')
}