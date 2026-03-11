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
                      <div>{{ item.idCard }}</div>
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
              <a-button type="primary" size="small" style="float: right" @click="startAnalysis">
                <template #icon><ThunderboltOutlined /></template>
                AI分析
              </a-button>
            </template>
            
            <a-descriptions :column="2" size="small" bordered>
              <a-descriptions-item label="姓名">{{ currentCustomer.customerName }}</a-descriptions-item>
              <a-descriptions-item label="身份证号">{{ currentCustomer.idCard }}</a-descriptions-item>
              <a-descriptions-item label="联系电话">{{ currentCustomer.phone }}</a-descriptions-item>
              <a-descriptions-item label="风险等级">
                <a-tag :color="getRiskColor(currentCustomer.riskLevel)">
                  {{ getRiskText(currentCustomer.riskLevel) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="开户时间">{{ currentCustomer.openAccountDate }}</a-descriptions-item>
              <a-descriptions-item label="账户状态">{{ currentCustomer.accountStatus }}</a-descriptions-item>
              <a-descriptions-item label="职业" :span="2">{{ currentCustomer.occupation }}</a-descriptions-item>
              <a-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</a-descriptions-item>
            </a-descriptions>
            
            <!-- 交易流水 -->
            <a-divider>可疑交易流水</a-divider>
            <a-table 
              :dataSource="transactionList" 
              :columns="transactionColumns" 
              size="small"
              :pagination="{ pageSize: 5 }"
            />
            
            <!-- AI分析结果 -->
            <div v-if="analysisResult" class="analysis-result">
              <a-divider>AI分析报告</a-divider>
              <a-alert type="info" show-icon style="margin-bottom: 16px">
                <template #message>分析完成</template>
              </a-alert>
              
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="风险评分">
                  <a-progress 
                    :percent="analysisResult.riskScore" 
                    :strokeColor="getProgressColor(analysisResult.riskScore)"
                  />
                </a-descriptions-item>
                <a-descriptions-item label="主要疑点">
                  <a-list :dataSource="analysisResult.redFlags" size="small">
                    <template #renderItem="{ item, index }">
                      <a-list-item>
                        <a-badge :count="index + 1" :numberStyle="{ backgroundColor: '#ff4d4f' }" />
                        <span style="margin-left: 8px">{{ item }}</span>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-descriptions-item>
                <a-descriptions-item label="分析结论">
                  {{ analysisResult.conclusion }}
                </a-descriptions-item>
                <a-descriptions-item label="建议措施">
                  {{ analysisResult.suggestions }}
                </a-descriptions-item>
              </a-descriptions>
              
              <div style="margin-top: 16px; text-align: right">
                <a-button type="primary" @click="exportReport">
                  <template #icon><ExportOutlined /></template>
                  导出报告
                </a-button>
              </div>
            </div>
          </a-card>
          
          <a-empty v-else description="请从左侧选择一个可疑人员进行分析" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { 
  getSuspiciousCustomers, 
  getCustomerTransactions, 
  analyzeCustomer,
  exportAnalysisReport 
} from '@/api/fraud'

const loading = ref(false)
const analyzing = ref(false)
const searchKeyword = ref('')
const currentCustomer = ref(null)
const customerList = ref([])
const transactionList = ref([])
const analysisResult = ref(null)

const transactionColumns = [
  { title: '交易时间', dataIndex: 'transactionTime', width: 150 },
  { title: '交易类型', dataIndex: 'transactionType', width: 100 },
  { title: '交易金额', dataIndex: 'amount', width: 120 },
  { title: '对方账户', dataIndex: 'counterAccount', width: 150 },
  { title: '交易渠道', dataIndex: 'channel', width: 100 },
  { title: '风险标签', dataIndex: 'riskTag', width: 100 }
]

function getRiskColor(level) {
  const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }
  return colors[level] || 'default'
}

function getRiskText(level) {
  const texts = { HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return texts[level] || '未知'
}

function getProgressColor(score) {
  if (score >= 80) return '#ff4d4f'
  if (score >= 60) return '#faad14'
  return '#52c41a'
}

async function fetchCustomerList() {
  loading.value = true
  try {
    const res = await getSuspiciousCustomers({ keyword: searchKeyword.value })
    customerList.value = res.records
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
  
  try {
    const res = await getCustomerTransactions(customer.id)
    transactionList.value = res
  } catch (error) {
    console.error(error)
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
  } finally {
    analyzing.value = false
  }
}

async function exportReport() {
  try {
    await exportAnalysisReport(currentCustomer.value.id)
    message.success('报告导出成功')
  } catch (error) {
    message.error('导出失败')
  }
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
}
</style>
