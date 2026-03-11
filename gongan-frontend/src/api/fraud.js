import request from './request'

export function getSuspiciousCustomers(params) {
  return request.get('/fraud/customer/list', { params })
}

export function getCustomerDetail(id) {
  return request.get(`/fraud/customer/${id}`)
}

export function getCustomerTransactions(customerId) {
  return request.get('/fraud/transaction/list', { params: { customerId } })
}

export function getTransactionList(params) {
  return request.get('/fraud/transaction/list', { params })
}

export function analyzeCustomer(customerId) {
  return request.post('/fraud/analysis', null, { params: { customerId } })
}

export function getAnalysisResult(caseId) {
  return request.get(`/fraud/case/${caseId}`)
}

export function exportAnalysisReport(caseId) {
  return request.post(`/fraud/report/generate/${caseId}`)
}
