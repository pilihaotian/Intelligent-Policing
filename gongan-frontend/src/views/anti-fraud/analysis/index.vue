<template>
  <div class="fraud-analysis">
    <a-card title="案件分析">
      <a-row :gutter="16">
        <!-- 左侧：重点人员列表 -->
        <a-col :span="8">
          <a-card title="重点人员列表" size="small">
            <template #extra>
              <a-input-search 
                v-model:value="searchKeyword" 
                placeholder="搜索人员姓名"
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
                        可疑次数: {{ item.suspiciousCount }} | 评分: {{ item.riskScore }}
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
              <a-button type="primary" size="small" style="float: right" @click="startAnalysis" :loading="analyzing">
                <template #icon><ThunderboltOutlined /></template>
                AI分析
              </a-button>
            </template>
            
            <a-descriptions :column="2" size="small" bordered>
              <a-descriptions-item label="人员编号">{{ currentCustomer.customerNo }}</a-descriptions-item>
              <a-descriptions-item label="姓名">{{ currentCustomer.customerName }}</a-descriptions-item>
              <a-descriptions-item label="证件号码">{{ currentCustomer.idNo }}</a-descriptions-item>
              <a-descriptions-item label="联系电话">{{ currentCustomer.phone }}</a-descriptions-item>
              <a-descriptions-item label="风险等级">
                <a-tag :color="getRiskColor(currentCustomer.riskLevel)">
                  {{ getRiskText(currentCustomer.riskLevel) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="风险评分">{{ currentCustomer.riskScore }}</a-descriptions-item>
              <a-descriptions-item label="可疑类型">{{ currentCustomer.suspiciousType }}</a-descriptions-item>
              <a-descriptions-item label="可疑次数">{{ currentCustomer.suspiciousCount }}</a-descriptions-item>
              <a-descriptions-item label="黑名单">
                <a-tag :color="currentCustomer.blacklistFlag ? 'red' : 'default'">
                  {{ currentCustomer.blacklistFlag ? '是' : '否' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="关注名单">
                <a-tag :color="currentCustomer.watchlistFlag ? 'orange' : 'default'">
                  {{ currentCustomer.watchlistFlag ? '是' : '否' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</a-descriptions-item>
            </a-descriptions>
            
            <!-- 交易流水 -->
            <a-divider>交易流水（最近10条）</a-divider>
            <a-table 
              :dataSource="transactionList" 
              :columns="transactionColumns" 
              size="small"
              :pagination="{ pageSize: 5 }"
              :loading="transactionLoading"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'amount'">
                  <span :style="{ color: record.amount > 50000 ? '#ff4d4f' : '#52c41a', fontWeight: 'bold' }">
                    ¥{{ Number(record.amount).toLocaleString() }}
                  </span>
                </template>
                <template v-if="column.key === 'transactionType'">
                  <a-tag :color="getTransactionTypeColor(record.transactionType)">
                    {{ getTransactionTypeText(record.transactionType) }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'riskFlag'">
                  <a-tag :color="record.riskFlag ? 'red' : 'green'">
                    {{ record.riskFlag ? '可疑' : '正常' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
            
            <!-- AI分析结果 -->
            <div v-if="analysisResult" class="analysis-result">
              <a-divider>AI分析报告</a-divider>
              <a-alert type="info" show-icon style="margin-bottom: 16px">
                <template #message>
                  案例编号: {{ analysisResult.caseNo }} | 置信度: {{ analysisResult.analysisResult?.confidence }}%
                </template>
              </a-alert>
              
              <!-- 风险疑点 -->
              <a-card title="风险疑点分析" size="small" style="margin-bottom: 16px">
                <a-list 
                  :dataSource="analysisResult.analysisResult?.suspicious_points || []" 
                  size="small"
                >
                  <template #renderItem="{ item, index }">
                    <a-list-item>
                      <a-space>
                        <a-badge :count="index + 1" :numberStyle="{ backgroundColor: getSeverityColor(item.severity) }" />
                        <div>
                          <div style="font-weight: 500">{{ item.point }}</div>
                          <div style="font-size: 12px; color: #666">{{ item.evidence }}</div>
                        </div>
                        <a-tag :color="getSeverityColor(item.severity)" size="small">{{ item.severity }}风险</a-tag>
                      </a-space>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>
              
              <!-- 行为模式分析 -->
              <a-card title="行为模式分析" size="small" style="margin-bottom: 16px">
                <p>{{ analysisResult.analysisResult?.behavior_pattern }}</p>
              </a-card>
              
              <!-- 风险结论 -->
              <a-card title="风险结论" size="small" style="margin-bottom: 16px">
                <a-alert :type="getConclusionType(analysisResult.analysisResult?.confidence)" show-icon>
                  <template #message>{{ analysisResult.analysisResult?.risk_conclusion }}</template>
                </a-alert>
              </a-card>
              
              <!-- 建议措施 -->
              <a-card title="建议措施" size="small" style="margin-bottom: 16px">
                <a-list 
                  :dataSource="analysisResult.analysisResult?.suggestions || []" 
                  size="small"
                >
                  <template #renderItem="{ item, index }">
                    <a-list-item>
                      <a-badge :count="index + 1" status="processing" />
                      <span style="margin-left: 8px">{{ item }}</span>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>
              
              <div style="margin-top: 16px; text-align: right">
                <a-space>
                  <a-button @click="viewReport" v-if="generatedReport">
                    <template #icon><FileTextOutlined /></template>
                    查看报告
                  </a-button>
                  <a-button type="primary" @click="exportReport" :loading="exporting">
                    <template #icon><ExportOutlined /></template>
                    生成分析报告
                  </a-button>
                </a-space>
              </div>
            </div>
          </a-card>
          
          <a-empty v-else description="请从左侧选择一个重点人员进行分析" />
        </a-col>
      </a-row>
    </a-card>
    
    <!-- 报告预览弹窗 -->
    <a-modal v-model:open="reportVisible" title="分析报告" width="800px" :footer="null">
      <div class="report-content" v-html="reportHtml"></div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, ExportOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { useRoute } from 'vue-router'
import { 
  getSuspiciousCustomers, 
  getTransactionList,
  analyzeCustomer,
  exportAnalysisReport 
} from '@/api/fraud'

const route = useRoute()
const loading = ref(false)
const analyzing = ref(false)
const exporting = ref(false)
const transactionLoading = ref(false)
const searchKeyword = ref('')
const currentCustomer = ref(null)
const customerList = ref([])
const transactionList = ref([])
const analysisResult = ref(null)
const generatedReport = ref(null)
const reportVisible = ref(false)
const reportHtml = ref('')

const transactionColumns = [
  { title: '交易时间', dataIndex: 'transactionTime', key: 'transactionTime', width: 140 },
  { title: '交易类型', dataIndex: 'transactionType', key: 'transactionType', width: 80 },
  { title: '交易金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '对方账号', dataIndex: 'counterAccountNo', key: 'counterAccountNo', width: 130 },
  { title: '交易渠道', dataIndex: 'channel', key: 'channel', width: 90 },
  { title: '风险标记', dataIndex: 'riskFlag', key: 'riskFlag', width: 80 }
]

function getRiskColor(level) {
  const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }
  return colors[level] || 'default'
}

function getRiskText(level) {
  const texts = { HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return texts[level] || '未知'
}

function getTransactionTypeColor(type) {
  const colors = {
    'TRANSFER_OUT': 'blue',
    'TRANSFER_IN': 'green',
    'WITHDRAW': 'orange',
    'DEPOSIT': 'cyan'
  }
  return colors[type] || 'default'
}

function getTransactionTypeText(type) {
  const texts = {
    'TRANSFER_OUT': '转出',
    'TRANSFER_IN': '转入',
    'WITHDRAW': '取现',
    'DEPOSIT': '存入'
  }
  return texts[type] || type
}

function getSeverityColor(severity) {
  const colors = { '高': '#ff4d4f', '中': '#faad14', '低': '#52c41a' }
  return colors[severity] || '#1890ff'
}

function getConclusionType(confidence) {
  if (confidence >= 80) return 'error'
  if (confidence >= 60) return 'warning'
  return 'success'
}

async function fetchCustomerList() {
  loading.value = true
  try {
    const res = await getSuspiciousCustomers({ 
      customerName: searchKeyword.value,
      current: 1,
      size: 50
    })
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
  generatedReport.value = null
  
  // 加载交易流水
  transactionLoading.value = true
  try {
    const res = await getTransactionList({ 
      customerId: customer.id,
      current: 1,
      size: 10
    })
    transactionList.value = res.records || []
  } catch (error) {
    console.error(error)
  } finally {
    transactionLoading.value = false
  }
}

async function startAnalysis() {
  if (!currentCustomer.value) return
  
  analyzing.value = true
  message.loading('正在进行AI分析，请稍候...', 0)
  
  try {
    const res = await analyzeCustomer(currentCustomer.value.id)
    analysisResult.value = res
    message.destroy()
    message.success('分析完成')
  } catch (error) {
    message.destroy()
    message.error('分析失败，请重试')
    console.error(error)
  } finally {
    analyzing.value = false
  }
}

async function exportReport() {
  if (!analysisResult.value?.caseId) {
    message.warning('请先进行AI分析')
    return
  }
  
  exporting.value = true
  try {
    const report = await exportAnalysisReport(analysisResult.value.caseId)
    generatedReport.value = report
    message.success('报告生成成功')
  } catch (error) {
    message.error('报告生成失败')
    console.error(error)
  } finally {
    exporting.value = false
  }
}

function viewReport() {
  if (generatedReport.value) {
    // 将Markdown转换为HTML（简单处理）
    reportHtml.value = generatedReport.value
      .replace(/\n/g, '<br>')
      .replace(/##\s*(.+)/g, '<h2>$1</h2>')
      .replace(/###\s*(.+)/g, '<h3>$1</h3>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    reportVisible.value = true
  }
}

onMounted(() => {
  fetchCustomerList()
  
  // 如果URL中有id参数，自动选中
  if (route.query.id) {
    const customerId = Number(route.query.id)
    setTimeout(() => {
      const customer = customerList.value.find(c => c.id === customerId)
      if (customer) {
        selectCustomer(customer)
      }
    }, 500)
  }
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
    padding: 16px;
    line-height: 1.8;
    
    h2 {
      margin-top: 16px;
      margin-bottom: 8px;
      padding-bottom: 8px;
      border-bottom: 1px solid #e8e8e8;
    }
    
    h3 {
      margin-top: 12px;
      margin-bottom: 6px;
    }
  }
}
</style>