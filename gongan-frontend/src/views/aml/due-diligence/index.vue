<template>
  <div class="due-diligence">
    <a-card title="人员核查">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="核查类型">
          <a-select v-model:value="searchForm.ddType" placeholder="请选择" allowClear style="width: 150px">
            <a-select-option value="ENHANCED">加强核查</a-select-option>
            <a-select-option value="STANDARD">标准核查</a-select-option>
            <a-select-option value="SIMPLIFIED">简化核查</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="核查状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 150px">
            <a-select-option value="PENDING">待核查</a-select-option>
            <a-select-option value="IN_PROGRESS">核查中</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="REJECTED">已排除</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
      
      <a-table 
        :dataSource="ddList" 
        :columns="columns" 
        :loading="loading"
        :pagination="pagination"
        rowKey="id"
        style="margin-top: 16px"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'ddType'">
            <a-tag :color="getDdTypeColor(record.ddType)">
              {{ getDdTypeText(record.ddType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleAiAssist(record)">
                <RobotOutlined /> AI辅助
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 核查详情弹窗 -->
    <a-modal 
      v-model:open="detailVisible" 
      title="人员核查详情"
      width="900px"
      :footer="null"
    >
      <div v-if="currentRecord">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="核查类型">
            <a-tag :color="getDdTypeColor(currentRecord.ddType)">
              {{ getDdTypeText(currentRecord.ddType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="核查状态">
            <a-tag :color="getStatusColor(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="核查事由" :span="2">{{ currentRecord.ddReason }}</a-descriptions-item>
          <a-descriptions-item label="业务性质">{{ currentRecord.businessNature }}</a-descriptions-item>
          <a-descriptions-item label="资金来源">{{ currentRecord.fundSource }}</a-descriptions-item>
          <template v-if="customerInfoObj">
            <a-descriptions-item label="姓名">{{ customerInfoObj.name }}</a-descriptions-item>
            <a-descriptions-item label="身份证号">{{ customerInfoObj.idCard }}</a-descriptions-item>
            <a-descriptions-item label="联系电话">{{ customerInfoObj.phone }}</a-descriptions-item>
            <a-descriptions-item label="地址" v-if="customerInfoObj.address">{{ customerInfoObj.address }}</a-descriptions-item>
          </template>
          <a-descriptions-item label="风险评估" :span="2" v-if="riskAssessmentObj">
            <div><strong>风险等级：</strong>
              <a-tag :color="getRiskColor(riskAssessmentObj.level)">{{ riskAssessmentObj.level || '未知' }}</a-tag>
            </div>
            <div style="margin-top: 8px" v-if="riskAssessmentObj.factors?.length">
              <strong>风险因素：</strong>
              <ul style="margin: 4px 0; padding-left: 20px">
                <li v-for="(factor, i) in riskAssessmentObj.factors" :key="i">{{ factor }}</li>
              </ul>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="风险评估" :span="2" v-else>
            <span style="color: #999">暂无风险评估数据</span>
          </a-descriptions-item>
        </a-descriptions>
        
        <!-- AI分析结果 -->
        <div v-if="analysisResult" style="margin-top: 16px">
          <a-divider>AI分析结果</a-divider>
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="风险评估">
              <a-space>
                <a-tag :color="getRiskColor(analysisResult.risk_assessment?.level)">
                  风险等级: {{ analysisResult.risk_assessment?.level }}
                </a-tag>
              </a-space>
              <div style="margin-top: 8px">
                <strong>风险因素:</strong>
                <ul style="margin: 4px 0; padding-left: 20px">
                  <li v-for="(factor, i) in (analysisResult.risk_assessment?.factors || [])" :key="i">{{ factor }}</li>
                </ul>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="可疑点">
              <a-list :dataSource="analysisResult.suspicious_points || []" size="small" :split="false">
                <template #renderItem="{ item }">
                  <a-list-item style="padding: 4px 0">
                    <WarningOutlined style="color: #faad14; margin-right: 8px" />
                    {{ item.point }}
                    <a-tag :color="getSeverityColor(item.severity)" size="small" style="margin-left: 8px">
                      {{ item.severity }}
                    </a-tag>
                  </a-list-item>
                </template>
              </a-list>
            </a-descriptions-item>
            <a-descriptions-item label="核查建议">
              <ul style="margin: 0; padding-left: 20px">
                <li v-for="(s, i) in (analysisResult.suggestions || [])" :key="i">{{ s }}</li>
              </ul>
            </a-descriptions-item>
            <a-descriptions-item label="置信度">
              <a-progress :percent="analysisResult.confidence || 0" :strokeColor="getProgressColor(analysisResult.confidence || 0)" />
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </a-modal>
    
    <!-- AI辅助分析弹窗 -->
    <a-modal 
      v-model:open="aiAssistVisible" 
      title="AI辅助分析"
      width="700px"
      :footer="null"
    >
      <div v-if="aiLoading" style="text-align: center; padding: 40px">
        <a-spin size="large" />
        <p style="margin-top: 16px">AI正在分析人员信息...</p>
      </div>
      
      <div v-else-if="analysisResult">
        <a-alert type="info" show-icon style="margin-bottom: 16px">
          <template #message>AI分析建议</template>
        </a-alert>
        
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="风险评估">
            <a-tag :color="getRiskColor(analysisResult.risk_assessment?.level)">
              {{ analysisResult.risk_assessment?.level }}风险
            </a-tag>
            <ul style="margin: 8px 0 0 0; padding-left: 20px">
              <li v-for="(factor, i) in (analysisResult.risk_assessment?.factors || [])" :key="i">{{ factor }}</li>
            </ul>
          </a-descriptions-item>
          <a-descriptions-item label="可疑点">
            <a-list :dataSource="analysisResult.suspicious_points || []" size="small" :split="false">
              <template #renderItem="{ item }">
                <a-list-item style="padding: 4px 0">
                  <WarningOutlined style="color: #faad14; margin-right: 8px" />{{ item.point }}
                  <a-tag :color="getSeverityColor(item.severity)" size="small" style="margin-left: 8px">{{ item.severity }}</a-tag>
                </a-list-item>
              </template>
            </a-list>
          </a-descriptions-item>
          <a-descriptions-item label="核查建议">
            <ul style="margin: 0; padding-left: 20px">
              <li v-for="(s, i) in (analysisResult.suggestions || [])" :key="i">{{ s }}</li>
            </ul>
          </a-descriptions-item>
          <a-descriptions-item label="置信度">
            <a-progress :percent="analysisResult.confidence || 0" />
          </a-descriptions-item>
        </a-descriptions>
        
        <div style="margin-top: 16px; text-align: right">
          <a-button type="primary" :loading="aiLoading" @click="handleReanalyze">
            <template #icon><ReloadOutlined /></template>
            重新分析
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { RobotOutlined, WarningOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getDDList, getDDDetail, aiAssistDD } from '@/api/aml'

const loading = ref(false)
const detailVisible = ref(false)
const aiAssistVisible = ref(false)
const aiLoading = ref(false)
const currentRecord = ref(null)
const ddList = ref([])
const analysisResult = ref(null)

// 解析人员信息JSON
const customerInfoObj = computed(() => {
  if (!currentRecord.value?.customerInfo) return null
  try {
    return JSON.parse(currentRecord.value.customerInfo)
  } catch {
    return null
  }
})

// 解析风险评估数据（优先使用AI分析结果中的风险数据）
const riskAssessmentObj = computed(() => {
  // 优先使用AI分析结果中的风险数据
  if (analysisResult.value?.risk_assessment) {
    return analysisResult.value.risk_assessment
  }
  // 其次使用数据库中的risk_assessment字段
  if (currentRecord.value?.riskAssessment) {
    try {
      return JSON.parse(currentRecord.value.riskAssessment)
    } catch {
      return null
    }
  }
  return null
})

const searchForm = reactive({
  ddType: undefined,
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
  { title: '人员ID', dataIndex: 'customerId', key: 'customerId', width: 80 },
  { title: '核查类型', dataIndex: 'ddType', key: 'ddType', width: 100 },
  { title: '核查事由', dataIndex: 'ddReason', key: 'ddReason', ellipsis: true },
  { title: '业务性质', dataIndex: 'businessNature', key: 'businessNature', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 150 },
  { title: '操作', key: 'action', width: 160 }
]

function getDdTypeColor(type) {
  const colors = { ENHANCED: 'red', STANDARD: 'orange', SIMPLIFIED: 'green' }
  return colors[type] || 'default'
}

function getDdTypeText(type) {
  const texts = { ENHANCED: '加强核查', STANDARD: '标准核查', SIMPLIFIED: '简化核查' }
  return texts[type] || type
}

function getStatusColor(status) {
  const colors = { 
    PENDING: 'default', 
    IN_PROGRESS: 'processing', 
    COMPLETED: 'success', 
    REJECTED: 'error' 
  }
  return colors[status] || 'default'
}

function getStatusText(status) {
  const texts = { 
    PENDING: '待核查', 
    IN_PROGRESS: '核查中', 
    COMPLETED: '已完成', 
    REJECTED: '已排除' 
  }
  return texts[status] || '未知'
}

function getRiskColor(level) {
  const colors = { '极高': 'magenta', '高': 'red', '中': 'orange', '低': 'green' }
  return colors[level] || 'default'
}

function getSeverityColor(severity) {
  const colors = { '高': 'red', '中': 'orange', '低': 'green' }
  return colors[severity] || 'default'
}

function getProgressColor(score) {
  if (score >= 80) return '#ff4d4f'
  if (score >= 60) return '#faad14'
  return '#52c41a'
}

function formatJson(str) {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

async function fetchDDList() {
  loading.value = true
  try {
    const res = await getDDList({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
    ddList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleTableChange(page) {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchDDList()
}

function handleSearch() {
  pagination.current = 1
  fetchDDList()
}

async function handleView(record) {
  try {
    currentRecord.value = await getDDDetail(record.id)
    
    // 解析已有的分析结果
    if (currentRecord.value.aiAnalysis) {
      try {
        let jsonStr = currentRecord.value.aiAnalysis.trim()
        if (jsonStr.includes('```json')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('```json') + 7, jsonStr.lastIndexOf('```'))
        } else if (jsonStr.includes('```')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('```') + 3, jsonStr.lastIndexOf('```'))
        }
        analysisResult.value = JSON.parse(jsonStr.trim())
      } catch (e) {
        console.error('解析分析结果失败', e)
        analysisResult.value = null
      }
    } else {
      analysisResult.value = null
    }
    
    detailVisible.value = true
  } catch (error) {
    message.error('获取详情失败')
  }
}

async function handleAiAssist(record) {
  aiAssistVisible.value = true
  analysisResult.value = null
  currentRecord.value = null
  aiLoading.value = true
  
  try {
    const res = await aiAssistDD(record.id, false)
    analysisResult.value = res.analysisResult
    currentRecord.value = res
    
    if (res.isHistory) {
      message.info('已加载历史分析记录，可点击"重新分析"生成新结果')
    } else {
      message.success('AI分析完成')
    }
  } catch (error) {
    message.error('AI分析失败')
    aiAssistVisible.value = false
  } finally {
    aiLoading.value = false
  }
}

async function doAiAnalyze(id, force = false) {
  aiLoading.value = true
  try {
    const res = await aiAssistDD(id, force)
    analysisResult.value = res.analysisResult
    currentRecord.value = res
    
    if (force) {
      message.success('AI分析完成')
    }
  } catch (error) {
    message.error('AI分析失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleReanalyze() {
  if (!currentRecord.value) return
  await doAiAnalyze(currentRecord.value.id, true)
}

onMounted(() => {
  fetchDDList()
})
</script>

<style lang="less" scoped>
.due-diligence {
  .search-form {
    margin-bottom: 16px;
  }
}
</style>