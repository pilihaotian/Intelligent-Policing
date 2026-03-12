import request from './request'

// ========== 人员核查相关接口 ==========

export function getDDList(params) {
  return request.get('/aml/dd/list', { params })
}

export function getDDDetail(id) {
  return request.get(`/aml/dd/${id}`)
}

export function submitDDInvestigate(id, data) {
  return request.put('/aml/dd', { id, ...data })
}

export function aiAssistDD(id, force = false) {
  return request.post(`/aml/dd/analyze/${id}`, null, { params: { force } })
}

// ========== 线索管理相关接口 ==========

export function getClueList(params) {
  return request.get('/aml/clue/list', { params })
}

export function getClueDetail(id) {
  return request.get(`/aml/clue/${id}`)
}

export function aiAnalyzeClue(id, force = false) {
  return request.post(`/aml/clue/analyze/${id}`, null, { params: { force } })
}

export function handleClue(id, data) {
  return request.post(`/aml/clue/handle/${id}`, null, { params: { ...data } })
}

export function generateClueReport(id) {
  return request.post(`/aml/clue/report/${id}`)
}

// ========== 兼容旧接口 ==========

export function getSuspiciousList(params) {
  return request.get('/aml/suspicious', { params })
}

export function submitSuspiciousScreen(id, data) {
  return request.post('/aml/suspicious', null, { params: { customerId: id, ...data } })
}

export function generateReport(id) {
  return request.post(`/aml/report/generate/${id}`)
}