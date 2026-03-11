<template>
  <div class="fraud-analysis">
    <a-card title="案件分析">
      <a-row :gutter="16">
        <!-- 左侧：可疑人员列表 -->
        <a-col :span="8">
          <a-card title="可疑人员列表" size="small">
            <template #extra>
              <a-input-search 
                v-model:value="searchKeyword" 
                placeholder="搜索人员"
                size="small"
                @search="handleSearch"
              />
            </template>
            
            <a-list 
              :dataSource="customerList" 
              :loading="loading"
              size="small"
            >
              <template #renderItem="{ item }">
                <a-list-item 
                  :class="['customer-item', { active: currentCustomer?.id === item.id }]"
                  @click="selectCustomer(item)"
                >
                  <a-list-item-meta>
                    <template #title>
                      <span>{{ item.customerName }}</span>
                      <a-tag 
                        :color="getRiskColor(item.riskLevel)" 
                        size="small"
                        style="margin-left: 8px"
                      >
                        {{ getRiskText(item.riskLevel) }}
                      </a-tag>
                    </template>
                    <template #description>
                      <div>{{ item.idNo }}</div>
                      <div style="font-size: 12px; color: #999">
                        {{ item.suspiciousCount }} 条可疑交易
                      </div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
        
        <!-- 右侧：分析面板 -->
        <a-col :span="16">
          <a-card v-if="currentCustomer" size="small">
            <template #title>
              <span>人员画像 - {{ currentCustomer.customerName }}</span>
              <a-space style="float: right">
                <a-button 
                  v-if="latestCase" 
                  size="small" 
                  @click="openHistoryModal"
                >
                  <template #icon><HistoryOutlined /></template>
                  历史记录
                </a-button>
                <a-button 
                  type="primary" 
                  size="small" 
                  :loading="analyzing"
                  @click="handleAnalyze"
                >
                  <template #icon><ThunderboltOutlined /></template>
                  {{ latestCase ? '重新分析' : 'AI分析' }}
                </a-button>
              </a-space>
            </template>
            
            <a-descriptions :column="2" size="small" bordered>
              <a-descriptions-item label="姓名">{{ currentCustomer.customerName }}</a-descriptions-item>
              <a-descriptions-item label="身份证号">{{ currentCustomer.idNo }}</a-descriptions-item>
              <a-descriptions-item label="联系电话">{{ currentCustomer.phone }}</a-descriptions-item>
              <a-descriptions-item label="风险等级">
                <a-tag :color="getRiskColor(currentCustomer.riskLevel)">
                  {{ getRiskText(currentCustomer.riskLevel) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="可疑类型">{{ getSuspiciousTypeText(currentCustomer.suspiciousType) }}</a-descriptions-item>
              <a-descriptions-item label="可疑次数">{{ currentCustomer.suspiciousCount }}</a-descriptions-item>
              <a-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</a-descriptions-item>
            </a-descriptions>
            
            <!-- 交易流水 -->
            <a-divider>可疑交易流水</a-divider>
            <a-table 
              :dataSource="transactionList" 
              :columns="transactionColumns" 
              size="small"
              :pagination="{ pageSize: 5 }"
              :loading="transLoading"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'amount'">
                  <span :style="{ color: record.amount > 50000 ? '#ff4d4f' : '#52c41a' }">
                    ¥{{ record.amount?.toLocaleString() }}
                  </span>
                </template>
                <template v-if="column.key === 'riskFlag'">
                  <a-tag :color="record.riskFlag ? 'red' : 'green'">
                    {{ record.riskFlag ? '风险' : '正常' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
            
            <!-- AI分析结果 -->
            <div v-if="analysisResult" class="analysis-result">
              <a-divider>
                <span>AI分析报告</span>
                <a-tag color="blue" style="margin-left: 8px">{{ latestCase?.caseNo }}</a-tag>
              </a-divider>
              
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="置信度">
                  <a-progress 
                    :percent="analysisResult.confidence || 0" 
                    :strokeColor="getProgressColor(analysisResult.confidence || 0)"
                  />
                </a-descriptions-item>
                <a-descriptions-item label="主要疑点">
                  <a-list 
                    :dataSource="analysisResult.suspicious_points || []" 
                    size="small"
                    :split="false"
                  >
                    <template #renderItem="{ item, index }">
                      <a-list-item style="padding: 4px 0">
                        <a-badge :count="index + 1" :numberStyle="{ backgroundColor: '#ff4d4f' }" />
                        <span style="margin-left: 8px">{{ item.point }}</span>
                        <a-tag 
                          :color="getSeverityColor(item.severity)" 
                          size="small"
                          style="margin-left: 8px"
                        >
                          {{ item.severity }}
                        </a-tag>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-descriptions-item>
                <a-descriptions-item label="行为模式">
                  {{ analysisResult.behavior_pattern }}
                </a-descriptions-item>
                <a-descriptions-item label="风险结论">
                  {{ analysisResult.risk_conclusion }}
                </a-descriptions-item>
                <a-descriptions-item label="建议措施">
                  <ul style="margin: 0; padding-left: 20px">
                    <li v-for="(s, i) in (analysisResult.suggestions || [])" :key="i">{{ s }}</li>
                  </ul>
                </a-descriptions-item>
              </a-descriptions>
              
              <!-- 详细报告 -->
              <div v-if="analysisReport" class="report-section" style="margin-top: 16px">
                <a-divider>详细分析报告</a-divider>
                <div class="report-content" v-html="renderMarkdown(analysisReport)"></div>
              </div>
              
              <div style="margin-top: 16px; text-align: right">
                <a-space>
                  <a-button 
                    :loading="generatingReport"
                    @click="handleGenerateReport"
                  >
                    <template #icon><FileTextOutlined /></template>
                    {{ analysisReport ? '重新生成报告' : '生成详细报告' }}
                  </a-button>
                  <a-button type="primary" @click="handleExportReport">
                    <template #icon><ExportOutlined /></template>
                    导出报告
                  </a-button>
                </a-space>
              </div>
            </div>
            
            <a-empty v-else description="暂无分析结果，请点击AI分析按钮进行分析" />
          </a-card>
          
          <a-empty v-else description="请从左侧选择一个可疑人员进行分析" />
        </a-col>
      </a-row>
    </a-card>
    
    <!-- 历史记录弹窗 -->
    <a-modal 
      v-model:open="showHistoryModal" 
      title="历史分析记录" 
      width="800px"
      :footer="null"
    >
      <a-table 
        :dataSource="historyCases" 
        :columns="historyColumns"
        size="small"
        :loading="historyLoading"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="loadCase(record)">查看</a-button>
              <a-button type="link" size="small" @click="reanalyzeFromHistory(record)">重新分析</a-button>
            </a-space>
          </template>
          <template v-if="column.key === 'confidence'">
            <a-progress :percent="record.confidence || 0" size="small" />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, ExportOutlined, HistoryOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { 
  getSuspiciousCustomers, 
  getCustomerTransactions, 
  analyzeCustomer,
  reanalyzeCase,
  getCaseListByCustomer,
  getLatestCase,
  generateAnalysisReport
} from '@/api/fraud'
import { marked } from 'marked'

const loading = ref(false)
const analyzing = ref(false)
const generatingReport = ref(false)
const transLoading = ref(false)
const historyLoading = ref(false)
const showHistoryModal = ref(false)
const searchKeyword = ref('')
const currentCustomer = ref(null)
const customerList = ref([])
const transactionList = ref([])
const analysisResult = ref(null)
const analysisReport = ref(null)
const latestCase = ref(null)
const historyCases = ref([])

const transactionColumns = [
  { title: '交易时间', dataIndex: 'transactionTime', key: 'transactionTime', width: 150 },
  { title: '交易类型', dataIndex: 'transactionType', key: 'transactionType', width: 100 },
  { title: '交易金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '对方账户', dataIndex: 'counterAccountNo', key: 'counterAccountNo', width: 150 },
  { title: '交易渠道', dataIndex: 'channel', key: 'channel', width: 100 },
  { title: '风险标记', dataIndex: 'riskFlag', key: 'riskFlag', width: 80 }
]

const historyColumns = [
  { title: '案例编号', dataIndex: 'caseNo', key: 'caseNo', width: 150 },
  { title: '案例类型', dataIndex: 'caseType', key: 'caseType', width: 120 },
  { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 150 },
  { title: '分析时间', dataIndex: 'createdTime', key: 'createdTime', width: 160 },
  { title: '操作', key: 'action', width: 150 }
]

function getRiskColor(level) {
  const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }
  return colors[level] || 'default'
}

function getRiskText(level) {
  const texts = { HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return texts[level] || '未知'
}

function getSuspiciousTypeText(type) {
  const texts = {
    TELECOM_FRAUD: '电信诈骗',
    MONEY_LAUNDERING: '洗钱嫌疑',
    GAMBLING: '网络赌博',
    SUSPICIOUS_TRANSFER: '可疑转账',
    ILLEGAL_FUNDRAISING: '非法集资',
    CYBER_CRIME: '网络犯罪',
    ASSOCIATION_SUSPICIOUS: '关联可疑',
    ABNORMAL_BEHAVIOR: '异常行为',
    DRUG_TRAFFICKING: '毒品犯罪',
    FRAUD_SUSPECTED: '诈骗嫌疑'
  }
  return texts[type] || type
}

function getProgressColor(score) {
  if (score >= 80) return '#ff4d4f'
  if (score >= 60) return '#faad14'
  return '#52c41a'
}

function getSeverityColor(severity) {
  const colors = { '高': 'red', '中': 'orange', '低': 'green' }
  return colors[severity] || 'default'
}

function renderMarkdown(content) {
  return marked(content || '')
}

async function fetchCustomerList() {
  loading.value = true
  try {
    const res = await getSuspiciousCustomers({ keyword: searchKeyword.value })
    customerList.value = res.records || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchCustomerList()
}

async function selectCustomer(customer) {
  currentCustomer.value = customer
  analysisResult.value = null
  analysisReport.value = null
  latestCase.value = null
  
  // 加载交易流水
  transLoading.value = true
  try {
    const res = await getCustomerTransactions(customer.id)
    transactionList.value = res.records || []
  } catch (error) {
    console.error(error)
  } finally {
    transLoading.value = false
  }
  
  // 加载最新案例分析
  try {
    const caseData = await getLatestCase(customer.id)
    if (caseData) {
      latestCase.value = caseData
      loadAnalysisFromCase(caseData)
    }
  } catch (error) {
    console.error('加载最新案例失败', error)
  }
}

function loadAnalysisFromCase(caseData) {
  try {
    // 先从 analysisContent 解析完整数据
    let fullResult = {}
    if (caseData.analysisContent) {
      try {
        fullResult = JSON.parse(caseData.analysisContent)
      } catch (e) {
        console.error('解析 analysisContent 失败', e)
      }
    }
    
    // 如果 suspiciousPoints 有值，用它覆盖疑点
    if (caseData.suspiciousPoints) {
      fullResult.suspicious_points = JSON.parse(caseData.suspiciousPoints)
    }
    
    // 使用 confidence 覆盖（数据库中的值）
    if (caseData.confidence !== null && caseData.confidence !== undefined) {
      fullResult.confidence = caseData.confidence
    }
    
    analysisResult.value = fullResult
  } catch (e) {
    console.error('解析分析结果失败', e)
    analysisResult.value = null
  }
  analysisReport.value = caseData.analysisReport
}

async function handleAnalyze() {
  if (!currentCustomer.value) return
  
  analyzing.value = true
  message.loading('正在进行AI分析，请稍候...', 0)
  
  try {
    let res
    if (latestCase.value) {
      // 重新分析
      res = await reanalyzeCase(latestCase.value.id)
      message.success('重新分析完成')
    } else {
      // 新建分析
      res = await analyzeCustomer(currentCustomer.value.id)
      message.success('分析完成')
    }
    
    // 更新显示
    analysisResult.value = res.analysisResult
    analysisReport.value = res.analysisReport || null
    latestCase.value = {
      id: res.caseId,
      caseNo: res.caseNo,
      confidence: res.analysisResult?.confidence,
      caseType: currentCustomer.value.suspiciousType,
      createdTime: new Date().toISOString()
    }
    // 如果历史弹窗打开，刷新历史记录
    if (showHistoryModal.value) {
      await loadHistoryCases()
    }
  } catch (error) {
    message.error('分析失败，请重试')
    console.error(error)
  } finally {
    message.destroy()
    analyzing.value = false
  }
}

async function handleGenerateReport() {
  if (!latestCase.value) {
    message.warning('请先进行分析')
    return
  }
  
  generatingReport.value = true
  message.loading('正在生成报告...', 0)
  
  try {
    const report = await generateAnalysisReport(latestCase.value.id)
    analysisReport.value = report
    message.success('报告生成完成')
  } catch (error) {
    message.error('生成报告失败')
    console.error(error)
  } finally {
    message.destroy()
    generatingReport.value = false
  }
}

async function handleExportReport() {
  if (!analysisReport.value) {
    message.warning('请先生成详细报告')
    return
  }
  
  // 导出为文件
  const blob = new Blob([analysisReport.value], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${currentCustomer.value.customerName}_分析报告_${latestCase.value.caseNo}.md`
  a.click()
  URL.revokeObjectURL(url)
  message.success('报告已导出')
}

async function loadHistoryCases() {
  if (!currentCustomer.value) return
  
  historyLoading.value = true
  try {
    historyCases.value = await getCaseListByCustomer(currentCustomer.value.id)
  } catch (error) {
    console.error(error)
  } finally {
    historyLoading.value = false
  }
}

async function openHistoryModal() {
  showHistoryModal.value = true
  await loadHistoryCases()
}

function loadCase(caseData) {
  loadAnalysisFromCase(caseData)
  latestCase.value = caseData
  showHistoryModal.value = false
}

async function reanalyzeFromHistory(caseData) {
  showHistoryModal.value = false
  latestCase.value = caseData
  await handleAnalyze()
}

onMounted(() => {
  fetchCustomerList()
})
</script>

<style lang="less" scoped>
.fraud-analysis {
  .customer-item {
    cursor: pointer;
    transition: background 0.2s;
    
    &.active {
      background: #e6f7ff;
    }
    
    &:hover {
      background: #f5f5f5;
    }
  }
  
  .analysis-result {
    margin-top: 16px;
  }
  
  .report-content {
    background: #fafafa;
    padding: 16px;
    border-radius: 4px;
    max-height: 400px;
    overflow-y: auto;
    
    :deep(h1), :deep(h2), :deep(h3) {
      margin-top: 16px;
      margin-bottom: 8px;
    }
    
    :deep(ul), :deep(ol) {
      padding-left: 20px;
    }
    
    :deep(p) {
      margin-bottom: 8px;
    }
  }
}
</style>
