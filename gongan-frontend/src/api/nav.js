import request from './request'

export function navigate(userInput) {
  return request.post('/nav/recognize', null, { params: { query: userInput } })
}

export function getIntentHistory() {
  return request.get('/nav/history')
}