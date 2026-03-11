<template>
  <div class="transaction-list">
    <a-card title="资金流水调查">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="关联人员ID">
          <a-input v-model:value="searchForm.customerId" placeholder="请输入人员ID" allowClear />
        </a-form-item>
        <a-form-item label="风险标记">
          <a-select v-model:value="searchForm.riskFlag" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option :value="1">可疑</a-select-option>
            <a-select-option :value="0">正常</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="searchForm.dateRange" show-time />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
      
      <a-table :dataSource="transactionList" :columns="columns" :loading="loading" rowKey="id" style="margin-top: 16px" :pagination="pagination" :scroll="{ x: 1400 }">
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
          <template v-if="column.key === 'riskScore'">
            <a-progress 
              :percent="record.riskScore || 0" 
              :size="'small'"
              :strokeColor="getProgressColor(record.riskScore)"
            />
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 交易详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="交易详情" width="800px" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentTransaction">
        <a-descriptions-item label="交易流水号">{{ currentTransaction.transactionNo }}</a-descriptions-item>
        <a-descriptions-item label="关联人员ID">{{ currentTransaction.customerId }}</a-descriptions-item>
        <a-descriptions-item label="本方账号">{{ currentTransaction.accountNo }}</a-descriptions-item>
        <a-descriptions-item label="对方账号">{{ currentTransaction.counterAccountNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="对方户名">{{ currentTransaction.counterAccountName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="交易类型">
          <a-tag :color="getTransactionTypeColor(currentTransaction.transactionType)">
            {{ getTransactionTypeText(currentTransaction.transactionType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="交易金额">
          <span style="color: #ff4d4f; font-weight: bold">
            ¥{{ Number(currentTransaction.amount).toLocaleString() }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="币种">{{ currentTransaction.currency }}</a-descriptions-item>
        <a-descriptions-item label="交易前余额">¥{{ Number(currentTransaction.balanceBefore).toLocaleString() }}</a-descriptions-item>
        <a-descriptions-item label="交易后余额">¥{{ Number(currentTransaction.balanceAfter).toLocaleString() }}</a-descriptions-item>
        <a-descriptions-item label="交易时间">{{ currentTransaction.transactionTime }}</a-descriptions-item>
        <a-descriptions-item label="交易渠道">{{ currentTransaction.channel }}</a-descriptions-item>
        <a-descriptions-item label="设备ID">{{ currentTransaction.deviceId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ currentTransaction.ipAddress || '-' }}</a-descriptions-item>
        <a-descriptions-item label="交易地点" :span="2">{{ currentTransaction.location || '-' }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ currentTransaction.remark || '-' }}</a-descriptions-item>
        <a-descriptions-item label="风险标记">
          <a-tag :color="currentTransaction.riskFlag ? 'red' : 'green'">
            {{ currentTransaction.riskFlag ? '可疑' : '正常' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="风险评分">{{ currentTransaction.riskScore || 0 }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTransactionList } from '@/api/fraud'

const loading = ref(false)
const detailVisible = ref(false)
const currentTransaction = ref(null)
const transactionList = ref([])

const searchForm = reactive({
  customerId: '',
  riskFlag: undefined,
  dateRange: null
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '交易时间', dataIndex: 'transactionTime', key: 'transactionTime', width: 160 },
  { title: '交易流水号', dataIndex: 'transactionNo', key: 'transactionNo', width: 150 },
  { title: '关联人员ID', dataIndex: 'customerId', key: 'customerId', width: 100 },
  { title: '交易类型', dataIndex: 'transactionType', key: 'transactionType', width: 100 },
  { title: '交易金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '对方账号', dataIndex: 'counterAccountNo', key: 'counterAccountNo', width: 150 },
  { title: '对方户名', dataIndex: 'counterAccountName', key: 'counterAccountName', width: 120 },
  { title: '交易渠道', dataIndex: 'channel', key: 'channel', width: 100 },
  { title: '交易地点', dataIndex: 'location', key: 'location', width: 120 },
  { title: '风险标记', dataIndex: 'riskFlag', key: 'riskFlag', width: 80 },
  { title: '风险评分', dataIndex: 'riskScore', key: 'riskScore', width: 120 }
]

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

function getProgressColor(score) {
  if (score >= 80) return '#ff4d4f'
  if (score >= 60) return '#faad14'
  return '#52c41a'
}

async function fetchTransactionList() {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize
    }
    
    if (searchForm.customerId) {
      params.customerId = searchForm.customerId
    }
    if (searchForm.riskFlag !== undefined) {
      params.riskFlag = searchForm.riskFlag
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0].format('YYYY-MM-DD HH:mm:ss')
      params.endTime = searchForm.dateRange[1].format('YYYY-MM-DD HH:mm:ss')
    }
    
    const res = await getTransactionList(params)
    transactionList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchTransactionList()
}

function handleView(record) {
  currentTransaction.value = record
  detailVisible.value = true
}

onMounted(() => {
  fetchTransactionList()
})
</script>

<style lang="less" scoped>
.transaction-list {
  .search-form {
    margin-bottom: 16px;
  }
}
</style>