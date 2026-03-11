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

// AI分析（创建新案例）
export function analyzeCustomer(customerId) {
  return request.post('/fraud/analysis', null, { params: { customerId } })
}

// 重新分析（覆盖已有案例）
export function reanalyzeCase(caseId) {
  return request.post(`/fraud/analysis/reanalyze/${caseId}`)
}

// 获取案例详情
export function getAnalysisResult(caseId) {
  return request.get(`/fraud/case/${caseId}`)
}

// 查询人员的历史案例分析
export function getCaseListByCustomer(customerId) {
  return request.get(`/fraud/case/list/${customerId}`)
}

// 获取人员最新的案例分析
export function getLatestCase(customerId) {
  return request.get(`/fraud/case/latest/${customerId}`)
}

// 生成分析报告
export function generateAnalysisReport(caseId) {
  return request.post(`/fraud/report/generate/${caseId}`)
}