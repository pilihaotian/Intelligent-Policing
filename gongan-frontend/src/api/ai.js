import request from './request'

export function getKbList(params) {
  return request.get('/ai/kb/list', { params })
}

export function createKb(data) {
  return request.post('/ai/kb', data)
}

export function updateKb(id, data) {
  return request.put('/ai/kb', { id, ...data })
}

export function deleteKb(id) {
  return request.delete(`/ai/kb/${id}`)
}

export function getKbDocs(kbId) {
  return request.get('/ai/document/list', { params: { kbId } })
}

export function uploadDocs(kbId, formData) {
  return request.post('/ai/document/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    params: { kbId }
  })
}

export function deleteDoc(docId) {
  return request.delete(`/ai/document/${docId}`)
}

// 会话管理 API
export function getSessions() {
  return request.get('/ai/sessions')
}

export function createSession(kbId) {
  return request.post('/ai/sessions', null, { params: { kbId } })
}

export function getSession(sessionId) {
  return request.get(`/ai/sessions/${sessionId}`)
}

export function getSessionMessages(sessionId) {
  return request.get(`/ai/sessions/${sessionId}/messages`)
}

export function deleteSession(sessionId) {
  return request.delete(`/ai/sessions/${sessionId}`)
}

export function sendMessage(sessionId, question) {
  return request.post(`/ai/sessions/${sessionId}/messages`, null, { 
    params: { question } 
  })
}

// 简单聊天（无历史）
export function chat(question, kbId, sessionId) {
  return request.post('/ai/chat', null, { 
    params: { 
      question, 
      kbId: kbId || '', 
      sessionId: sessionId || '' 
    } 
  })
}

export function getChatHistory(sessionId) {
  return request.get(`/ai/chat/history/${sessionId}`)
}
