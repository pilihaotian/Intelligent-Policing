<template>
  <div class="clue-management">
    <a-card title="线索管理">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="关联人员">
          <a-input v-model:value="searchForm.customerName" placeholder="请输入" allowClear />
        </a-form-item>
        <a-form-item label="线索状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option value="PENDING">待处理</a-select-option>
            <a-select-option value="CONFIRMED">已立案</a-select-option>
            <a-select-option value="EXCLUDED">已排除</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
      
      <a-table 
        :dataSource="clueList" 
        :columns="columns" 
        :loading="loading" 
        rowKey="id" 
        style="margin-top: 16px"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            <span :style="{ color: record.totalAmount > 5000000 ? '#ff4d4f' : '#52c41a' }">
              ¥{{ record.totalAmount?.toLocaleString() }}
            </span>
          </template>
          <template v-if="column.key === 'alertType'">
            <a-tag :color="getAlertTypeColor(record.alertType)">{{ record.alertType }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="handleAiAnalyze(record)">
                <RobotOutlined /> AI分析
              </a-button>
              <a-button 
                type="link" 
                size="small" 
                :disabled="record.status !== 'PENDING'" 
                @click="handleScreen(record)"
              >
                处理
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 线索详情弹窗 -->
    <a-modal 
      v-model:open="detailVisible" 
      :title="`线索详情 - ${currentClue?.reportNo}`"
      width="900px"
      :footer="null"
    >
      <div v-if="currentClue">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="线索编号">{{ currentClue.reportNo }}</a-descriptions-item>
          <a-descriptions-item label="线索类型">
            <a-tag :color="getAlertTypeColor(currentClue.alertType)">{{ currentClue.alertType }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="关联人员">{{ currentClue.customerName }}</a-descriptions-item>
          <a-descriptions-item label="身份证号">{{ currentClue.idCard }}</a-descriptions-item>
          <a-descriptions-item label="涉及笔数">{{ currentClue.transactionCount }} 笔</a-descriptions-item>
          <a-descriptions-item label="涉及金额">
            <span style="color: #ff4d4f; font-weight: bold">¥{{ currentClue.totalAmount?.toLocaleString() }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="可疑类型">{{ currentClue.suspiciousTypes }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentClue.status)">{{ getStatusText(currentClue.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间" :span="2">{{ currentClue.createdTime }}</a-descriptions-item>
        </a-descriptions>
        
        <!-- AI分析结果 -->
        <div v-if="analysisResult" style="margin-top: 16px">
          <a-divider>AI分析结果</a-divider>
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="线索评估">
              <a-space>
                <a-tag :color="getRiskColor(analysisResult.clue_assessment?.credibility)">
                  可信度: {{ analysisResult.clue_assessment?.credibility }}
                </a-tag>
                <a-tag :color="getUrgencyColor(analysisResult.clue_assessment?.urgency)">
                  紧急程度: {{ analysisResult.clue_assessment?.urgency }}
                </a-tag>
                <a-tag :color="getRiskColor(analysisResult.clue_assessment?.risk_level)">
                  风险等级: {{ analysisResult.clue_assessment?.risk_level }}
                </a-tag>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="犯罪模式">
              <div><strong>类型:</strong> {{ analysisResult.crime_pattern?.type }}</div>
              <div><strong>手法:</strong> {{ analysisResult.crime_pattern?.method }}</div>
            </a-descriptions-item>
            <a-descriptions-item label="关键证据">
              <a-list :dataSource="analysisResult.key_evidence || []" size="small" :split="false">
                <template #renderItem="{ item }">
                  <a-list-item style="padding: 4px 0">
                    <CheckCircleOutlined style="color: #52c41a; margin-right: 8px" />
                    {{ item.evidence }}
                    <a-tag size="small" style="margin-left: 8px">{{ item.importance }}</a-tag>
                  </a-list-item>
                </template>
              </a-list>
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
            <a-descriptions-item label="侦查建议">
              <ul style="margin: 0; padding-left: 20px">
                <li v-for="(s, i) in (analysisResult.investigation_suggestions || [])" :key="i">{{ s }}</li>
              </ul>
            </a-descriptions-item>
            <a-descriptions-item label="置信度">
              <a-progress :percent="analysisResult.confidence || 0" :strokeColor="getProgressColor(analysisResult.confidence || 0)" />
            </a-descriptions-item>
          </a-descriptions>
        </div>
        
        <!-- 分析报告 -->
        <div v-if="reportContent" style="margin-top: 16px">
          <a-divider>详细分析报告</a-divider>
          <div class="report-content" v-html="renderMarkdown(reportContent)"></div>
        </div>
        
        <div style="margin-top: 16px; text-align: right">
          <a-space>
            <a-button :loading="analyzing" @click="handleReanalyze">
              <template #icon><ReloadOutlined /></template>
              重新分析
            </a-button>
            <a-button :loading="generatingReport" @click="handleGenerateReport">
              <template #icon><FileTextOutlined /></template>
              {{ reportContent ? '重新生成报告' : '生成详细报告' }}
            </a-button>
            <a-button type="primary" @click="handleExportReport" :disabled="!reportContent">
              <template #icon><ExportOutlined /></template>
              导出报告
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
    
    <!-- 线索处理弹窗 -->
    <a-modal 
      v-model:open="screenModalVisible" 
      title="线索处理" 
      width="600px" 
      @ok="handleSubmitScreen" 
      :confirmLoading="submitLoading"
    >
      <a-form :model="screenForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="处理结论" required>
          <a-radio-group v-model:value="screenForm.conclusion">
            <a-radio value="CONFIRMED">确认线索</a-radio>
            <a-radio value="EXCLUDED">排除嫌疑</a-radio>
            <a-radio value="FURTHER">进一步侦查</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="处理说明" required>
          <a-textarea v-model:value="screenForm.description" :rows="4" placeholder="请输入处理说明" />
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- AI分析遮罩弹窗 -->
    <a-modal
      v-model:open="analyzing"
      :closable="false"
      :footer="null"
      :maskClosable="false"
      :keyboard="false"
      width="300px"
      centered
    >
      <div style="text-align: center; padding: 30px 0">
        <a-spin size="large" />
        <p style="margin-top: 20px; font-size: 16px; color: #333">正在AI分析，请稍后...</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  RobotOutlined, 
  WarningOutlined, 
  CheckCircleOutlined,
  FileTextOutlined,
  ExportOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getClueList, getClueDetail, aiAnalyzeClue, handleClue, generateClueReport } from '@/api/aml'
import { marked } from 'marked'

const loading = ref(false)
const submitLoading = ref(false)
const analyzing = ref(false)
const generatingReport = ref(false)
const detailVisible = ref(false)
const screenModalVisible = ref(false)
const currentClue = ref(null)
const analysisResult = ref(null)
const reportContent = ref(null)

const searchForm = reactive({
  customerName: '',
  status: undefined
})

const screenForm = reactive({
  conclusion: 'CONFIRMED',
  description: ''
})

const clueList = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '线索编号', dataIndex: 'reportNo', key: 'reportNo', width: 130 },
  { title: '关联人员', dataIndex: 'customerName', key: 'customerName', width: 80 },
  { title: '线索类型', dataIndex: 'alertType', key: 'alertType', width: 100 },
  { title: '涉及笔数', dataIndex: 'transactionCount', key: 'transactionCount', width: 80 },
  { title: '涉及金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 150 },
  { title: '操作', key: 'action', width: 200 }
]

function getAlertTypeColor(type) {
  const colors = {
    '电信诈骗': 'red',
    '跨境诈骗': 'magenta',
    '洗钱嫌疑': 'orange',
    '网络赌博': 'purple',
    '非法集资': 'volcano',
    '网络犯罪': 'blue',
    '毒品犯罪': 'red',
    '合同诈骗': 'gold',
    '投资诈骗': 'cyan'
  }
  return colors[type] || 'default'
}

function getStatusColor(status) {
  const colors = { PENDING: 'orange', CONFIRMED: 'red', EXCLUDED: 'green' }
  return colors[status] || 'default'
}

function getStatusText(status) {
  const texts = { PENDING: '待处理', CONFIRMED: '已立案', EXCLUDED: '已排除' }
  return texts[status] || '未知'
}

function getRiskColor(level) {
  const colors = { '高': 'red', '中': 'orange', '低': 'green' }
  return colors[level] || 'default'
}

function getUrgencyColor(urgency) {
  const colors = { '紧急': 'red', '一般': 'orange', '可缓': 'green' }
  return colors[urgency] || 'default'
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

function renderMarkdown(content) {
  if (!content) return ''
  return marked.parse(content, {
    breaks: true,
    gfm: true
  })
}

async function fetchClueList() {
  loading.value = true
  try {
    const res = await getClueList({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
    clueList.value = res.records || []
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
  fetchClueList()
}

function handleSearch() {
  pagination.current = 1
  fetchClueList()
}

async function handleDetail(record) {
  try {
    currentClue.value = await getClueDetail(record.id)
    analysisResult.value = null
    reportContent.value = currentClue.value.reportContent
    
    // 解析已有的分析结果
    if (currentClue.value.analysisResult) {
      try {
        let jsonStr = currentClue.value.analysisResult
        if (jsonStr.includes('```json')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('```json') + 7, jsonStr.lastIndexOf('```'))
        } else if (jsonStr.includes('```')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('```') + 3, jsonStr.lastIndexOf('```'))
        }
        analysisResult.value = JSON.parse(jsonStr.trim())
      } catch (e) {
        console.error('解析分析结果失败', e)
      }
    }
    
    detailVisible.value = true
  } catch (error) {
    message.error('获取详情失败')
  }
}

async function handleAiAnalyze(record) {
  detailVisible.value = false
  analysisResult.value = null
  reportContent.value = null
  currentClue.value = null
  
  // 显示分析中遮罩
  analyzing.value = true
  
  try {
    const res = await aiAnalyzeClue(record.id, false)
    analysisResult.value = res.analysisResult
    currentClue.value = res.clue
    reportContent.value = res.clue.reportContent
    detailVisible.value = true
    
    if (res.isHistory) {
      message.info('已加载历史分析记录，可点击"重新分析"生成新结果')
    } else {
      message.success('AI分析完成')
    }
  } catch (error) {
    message.error('AI分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

async function doAiAnalyze(clueId, force = false) {
  analyzing.value = true
  
  try {
    const res = await aiAnalyzeClue(clueId, force)
    analysisResult.value = res.analysisResult
    currentClue.value = res.clue
    reportContent.value = res.clue.reportContent
    detailVisible.value = true
    
    if (force) {
      message.success('AI分析完成')
    } else if (res.isHistory) {
      message.info('已加载历史分析记录')
    }
  } catch (error) {
    message.error('AI分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

async function handleReanalyze() {
  if (!currentClue.value) return
  analysisResult.value = null
  reportContent.value = null
  await doAiAnalyze(currentClue.value.id, true)
}

function handleScreen(record) {
  currentClue.value = record
  Object.assign(screenForm, { conclusion: 'CONFIRMED', description: '' })
  screenModalVisible.value = true
}

async function handleSubmitScreen() {
  submitLoading.value = true
  try {
    await handleClue(currentClue.value.id, screenForm)
    message.success('处理提交成功')
    screenModalVisible.value = false
    fetchClueList()
  } catch (error) {
    message.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleGenerateReport() {
  if (!currentClue.value) return
  
  generatingReport.value = true
  message.loading('正在生成报告...', 0)
  
  try {
    const report = await generateClueReport(currentClue.value.id)
    reportContent.value = report
    message.success('报告生成完成')
  } catch (error) {
    message.error('生成报告失败')
  } finally {
    message.destroy()
    generatingReport.value = false
  }
}

function handleExportReport() {
  if (!reportContent.value) {
    message.warning('请先生成详细报告')
    return
  }
  
  const blob = new Blob([reportContent.value], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${currentClue.value.reportNo}_分析报告.md`
  a.click()
  URL.revokeObjectURL(url)
  message.success('报告已导出')
}

onMounted(() => {
  fetchClueList()
})
</script>

<style lang="less" scoped>
.clue-management {
  .search-form {
    margin-bottom: 16px;
  }
  
  .report-content {
    background: #fafafa;
    padding: 20px;
    border-radius: 8px;
    max-height: 500px;
    overflow-y: auto;
    font-size: 14px;
    line-height: 1.8;
    
    :deep(h1) {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 16px 0;
      padding-bottom: 10px;
      border-bottom: 1px solid #e8e8e8;
      color: #1890ff;
    }
    
    :deep(h2) {
      font-size: 17px;
      font-weight: 600;
      margin: 20px 0 12px 0;
      color: #262626;
    }
    
    :deep(h3) {
      font-size: 15px;
      font-weight: 600;
      margin: 16px 0 8px 0;
      color: #595959;
    }
    
    :deep(p) {
      margin: 0 0 12px 0;
      color: #333;
    }
    
    :deep(ul), :deep(ol) {
      padding-left: 24px;
      margin: 8px 0;
    }
    
    :deep(li) {
      margin-bottom: 6px;
    }
    
    :deep(strong) {
      color: #262626;
      font-weight: 600;
    }
    
    :deep(code) {
      background: #f0f0f0;
      padding: 2px 6px;
      border-radius: 3px;
      font-family: monospace;
      font-size: 13px;
    }
    
    :deep(blockquote) {
      border-left: 3px solid #1890ff;
      padding-left: 12px;
      margin: 12px 0;
      color: #666;
      background: #f6f6f6;
      padding: 8px 12px;
      border-radius: 0 4px 4px 0;
    }
    
    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 12px 0;
      
      th, td {
        border: 1px solid #e8e8e8;
        padding: 8px 12px;
        text-align: left;
      }
      
      th {
        background: #fafafa;
        font-weight: 600;
      }
    }
    
    :deep(hr) {
      border: none;
      border-top: 1px solid #e8e8e8;
      margin: 16px 0;
    }
  }
}
</style>
