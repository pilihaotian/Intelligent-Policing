import request from './request'

export function getDocumentList(params) {
  return request.get('/ops/document/list', { params })
}

export function getDocumentDetail(id) {
  return request.get(`/ops/document/${id}`)
}

export function uploadDocument(formData) {
  return request.post('/ops/document/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function extractDocument(id) {
  return request.post(`/ops/document/extract/${id}`)
}

export function saveExtractResult(id, data) {
  return request.post('/ops/document/confirm', { documentId: id, confirmedData: data })
}

export function deleteDocument(id) {
  return request.delete(`/ops/document/${id}`)
}
