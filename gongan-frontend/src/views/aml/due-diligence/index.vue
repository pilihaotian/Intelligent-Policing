<template>
  <div class="due-diligence">
    <a-card title="人员核查">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="人员姓名">
          <a-input v-model:value="searchForm.customerName" placeholder="请输入人员姓名" allowClear />
        </a-form-item>
        <a-form-item label="核查状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 150px">
            <a-select-option value="PENDING">待核查</a-select-option>
            <a-select-option value="IN_PROGRESS">核查中</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="REJECTED">已排除</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="风险等级">
          <a-select v-model:value="searchForm.riskLevel" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
      
      <!-- 数据表格 -->
      <a-table 
        :dataSource="ddList" 
        :columns="columns" 
        :loading="loading"
        :pagination="pagination"
        rowKey="id"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskLevel'">
            <a-tag :color="getRiskColor(record.riskLevel)">
              {{ getRiskText(record.riskLevel) }}
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
              <a-button 
                type="link" 
                size="small" 
                :disabled="record.status === 'COMPLETED'"
                @click="handleInvestigate(record)"
              >
                核查
              </a-button>
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
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="人员姓名">{{ currentRecord.customerName }}</a-descriptions-item>
        <a-descriptions-item label="人员类型">{{ currentRecord.customerType }}</a-descriptions-item>
        <a-descriptions-item label="身份证号">{{ currentRecord.idNumber }}</a-descriptions-item>
        <a-descriptions-item label="风险等级">
          <a-tag :color="getRiskColor(currentRecord.riskLevel)">
            {{ getRiskText(currentRecord.riskLevel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="核查状态">
          <a-tag :color="getStatusColor(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="核查事由">{{ currentRecord.triggerReason }}</a-descriptions-item>
        <a-descriptions-item label="人员背景" :span="2">{{ currentRecord.backgroundInfo }}</a-descriptions-item>
        <a-descriptions-item label="核查结论" :span="2">{{ currentRecord.conclusion || '暂无' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
    
    <!-- 核查处理弹窗 -->
    <a-modal 
      v-model:open="investigateVisible" 
      title="人员核查处理"
      width="700px"
      @ok="handleSubmitInvestigate"
    >
      <a-form :model="investigateForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="核查方式" required>
          <a-checkbox-group v-model:value="investigateForm.methods">
            <a-checkbox value="PHONE">电话核查</a-checkbox>
            <a-checkbox value="VISIT">实地调查</a-checkbox>
            <a-checkbox value="DOCUMENT">资料查验</a-checkbox>
            <a-checkbox value="DATABASE">系统查询</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item label="核查内容" required>
          <a-textarea v-model:value="investigateForm.content" :rows="4" placeholder="请输入核查内容" />
        </a-form-item>
        <a-form-item label="核查结论" required>
          <a-radio-group v-model:value="investigateForm.conclusion">
            <a-radio value="PASS">排除嫌疑</a-radio>
            <a-radio value="RISK_CONTROL">重点管控</a-radio>
            <a-radio value="REJECT">立案侦查</a-radio>
            <a-radio value="FURTHER">进一步核查</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="风险评级">
          <a-select v-model:value="investigateForm.riskLevel" style="width: 150px">
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="investigateForm.remark" :rows="2" />
        </a-form-item>
      </a-form>
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
      
      <div v-else-if="aiResult">
        <a-alert type="info" show-icon style="margin-bottom: 16px">
          <template #message>AI分析建议</template>
        </a-alert>
        
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="风险提示">
            <a-list :dataSource="aiResult.riskAlerts" size="small">
              <template #renderItem="{ item }">
                <a-list-item><WarningOutlined style="color: #faad14; margin-right: 8px" />{{ item }}</a-list-item>
              </template>
            </a-list>
          </a-descriptions-item>
          <a-descriptions-item label="建议核查重点">{{ aiResult.suggestFocus }}</a-descriptions-item>
          <a-descriptions-item label="推荐核查方式">{{ aiResult.suggestMethods }}</a-descriptions-item>
          <a-descriptions-item label="关联风险信息">{{ aiResult.relatedRisk }}</a-descriptions-item>
        </a-descriptions>
        
        <div style="margin-top: 16px; text-align: right">
          <a-button type="primary" @click="applyAiSuggestion">应用建议</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { RobotOutlined, WarningOutlined } from '@ant-design/icons-vue'
import { getDDList, getDDDetail, submitDDInvestigate, aiAssistDD } from '@/api/aml'

const loading = ref(false)
const detailVisible = ref(false)
const investigateVisible = ref(false)
const aiAssistVisible = ref(false)
const aiLoading = ref(false)
const currentRecord = ref(null)
const ddList = ref([])
const aiResult = ref(null)

const searchForm = reactive({
  customerName: '',
  status: undefined,
  riskLevel: undefined
})

const investigateForm = reactive({
  methods: [],
  content: '',
  conclusion: 'PASS',
  riskLevel: 'MEDIUM',
  remark: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '人员姓名', dataIndex: 'customerName', key: 'customerName' },
  { title: '人员类型', dataIndex: 'customerType', key: 'customerType' },
  { title: '身份证号', dataIndex: 'idNumber', key: 'idNumber' },
  { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel' },
  { title: '核查状态', dataIndex: 'status', key: 'status' },
  { title: '核查事由', dataIndex: 'triggerReason', key: 'triggerReason' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 }
]

function getRiskColor(level) {
  const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }
  return colors[level] || 'default'
}

function getRiskText(level) {
  const texts = { HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return texts[level] || '未知'
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

async function fetchDDList() {
  loading.value = true
  try {
    const res = await getDDList(searchForm)
    ddList.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchDDList()
}

async function handleView(record) {
  try {
    const res = await getDDDetail(record.id)
    currentRecord.value = res
    detailVisible.value = true
  } catch (error) {
    message.error('获取详情失败')
  }
}

function handleInvestigate(record) {
  currentRecord.value = record
  Object.assign(investigateForm, {
    methods: [],
    content: '',
    conclusion: 'PASS',
    riskLevel: record.riskLevel || 'MEDIUM',
    remark: ''
  })
  investigateVisible.value = true
}

async function handleSubmitInvestigate() {
  try {
    await submitDDInvestigate(currentRecord.value.id, investigateForm)
    message.success('提交成功')
    investigateVisible.value = false
    fetchDDList()
  } catch (error) {
    message.error('提交失败')
  }
}

async function handleAiAssist(record) {
  currentRecord.value = record
  aiAssistVisible.value = true
  aiLoading.value = true
  aiResult.value = null
  
  try {
    const res = await aiAssistDD(record.id)
    aiResult.value = res
  } catch (error) {
    message.error('AI分析失败')
  } finally {
    aiLoading.value = false
  }
}

function applyAiSuggestion() {
  if (aiResult.value) {
    investigateForm.content = aiResult.value.suggestFocus
    investigateForm.methods = aiResult.value.recommendedMethods || []
  }
  aiAssistVisible.value = false
  investigateVisible.value = true
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
