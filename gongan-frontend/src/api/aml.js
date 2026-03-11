import request from './request'

export function getDDList(params) {
  return request.get('/aml/dd/list', { params })
}

export function getDDDetail(id) {
  return request.get(`/aml/dd/${id}`)
}

export function submitDDInvestigate(id, data) {
  return request.put('/aml/dd', { id, ...data })
}

export function aiAssistDD(id) {
  return request.post(`/aml/dd/analyze/${id}`)
}

export function getSuspiciousList(params) {
  return request.get('/aml/suspicious', { params })
}

export function submitSuspiciousScreen(id, data) {
  return request.post('/aml/suspicious', null, { params: { customerId: id, ...data } })
}

export function generateReport(id) {
  return request.post(`/aml/report/generate/${id}`)
}
