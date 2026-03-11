<template>
  <div class="aml-suspicious">
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
        :dataSource="suspiciousList" 
        :columns="columns" 
        :loading="loading" 
        rowKey="id" 
        style="margin-top: 16px"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleDetail(record)">详情</a-button>
              <a-button type="link" size="small" :disabled="record.status !== 'PENDING'" @click="handleScreen(record)">处理</a-button>
              <a-button type="link" size="small" @click="handleAiAssist(record)"><RobotOutlined /> AI</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <a-modal v-model:open="screenModalVisible" title="线索处理" width="600px" @ok="handleSubmitScreen" :confirmLoading="submitLoading">
      <a-form :model="screenForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="处理结论" required>
          <a-radio-group v-model:value="screenForm.conclusion">
            <a-radio value="CONFIRMED">确认线索</a-radio>
            <a-radio value="EXCLUDED">排除嫌疑</a-radio>
            <a-radio value="FURTHER">进一步侦查</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="案件类型">
          <a-checkbox-group v-model:value="screenForm.suspiciousTypes">
            <a-checkbox value="FRAUD">诈骗</a-checkbox>
            <a-checkbox value="THEFT">盗窃</a-checkbox>
            <a-checkbox value="PYRAMID">传销</a-checkbox>
            <a-checkbox value="ILLEGAL_FUNDRAISING">非法集资</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item label="处理说明" required>
          <a-textarea v-model:value="screenForm.description" :rows="4" placeholder="请输入处理说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { RobotOutlined } from '@ant-design/icons-vue'
import { getSuspiciousList, submitSuspiciousScreen, generateReport } from '@/api/aml'

const loading = ref(false)
const submitLoading = ref(false)
const screenModalVisible = ref(false)
const currentRecord = ref(null)

const searchForm = reactive({
  customerName: '',
  status: undefined
})

const screenForm = reactive({
  conclusion: 'CONFIRMED',
  suspiciousTypes: [],
  description: ''
})

const suspiciousList = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '关联人员', dataIndex: 'customerName', key: 'customerName' },
  { title: '身份证号', dataIndex: 'idCard', key: 'idCard' },
  { title: '涉及笔数', dataIndex: 'transactionCount', key: 'transactionCount' },
  { title: '涉及金额', dataIndex: 'totalAmount', key: 'totalAmount' },
  { title: '线索类型', dataIndex: 'alertType', key: 'alertType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 }
]

function getStatusColor(status) {
  const colors = { PENDING: 'orange', CONFIRMED: 'red', EXCLUDED: 'green' }
  return colors[status] || 'default'
}

function getStatusText(status) {
  const texts = { PENDING: '待处理', CONFIRMED: '已立案', EXCLUDED: '已排除' }
  return texts[status] || '未知'
}

async function fetchSuspiciousList() {
  loading.value = true
  try {
    const res = await getSuspiciousList({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
    suspiciousList.value = res.records || []
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
  fetchSuspiciousList()
}

function handleSearch() {
  pagination.current = 1
  fetchSuspiciousList()
}

function handleDetail(record) {
  // 查看详情
}

function handleScreen(record) {
  currentRecord.value = record
  Object.assign(screenForm, { conclusion: 'CONFIRMED', suspiciousTypes: [], description: '' })
  screenModalVisible.value = true
}

async function handleSubmitScreen() {
  submitLoading.value = true
  try {
    await submitSuspiciousScreen(currentRecord.value.id, screenForm)
    message.success('处理提交成功')
    screenModalVisible.value = false
    fetchSuspiciousList()
  } catch (error) {
    message.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleAiAssist(record) {
  message.loading('AI分析中...', 0)
  try {
    await generateReport(record.id)
    message.destroy()
    message.success('AI分析完成')
  } catch (error) {
    message.destroy()
    message.error('AI分析失败')
  }
}

onMounted(() => {
  fetchSuspiciousList()
})
</script>

<style lang="less" scoped>
.aml-suspicious {
  .search-form {
    margin-bottom: 16px;
  }
}
</style>