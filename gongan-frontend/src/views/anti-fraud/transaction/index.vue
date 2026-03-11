<template>
  <div class="transaction-list">
    <a-card title="资金流水调查">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="关联人员">
          <a-input v-model:value="searchForm.customerName" placeholder="请输入" allowClear />
        </a-form-item>
        <a-form-item label="资金类型">
          <a-select v-model:value="searchForm.transactionType" placeholder="请选择" allowClear style="width: 120px">
            <a-select-option value="TRANSFER">转账</a-select-option>
            <a-select-option value="WITHDRAW">取现</a-select-option>
            <a-select-option value="DEPOSIT">存入</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="searchForm.dateRange" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-form-item>
      </a-form>
      
      <a-table :dataSource="transactionList" :columns="columns" :loading="loading" rowKey="id" style="margin-top: 16px" :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span :style="{ color: record.amount > 50000 ? '#ff4d4f' : '#52c41a' }">
              ¥{{ record.amount.toLocaleString() }}
            </span>
          </template>
          <template v-if="column.key === 'riskTag'">
            <a-tag color="orange">{{ record.riskTag }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTransactionList } from '@/api/fraud'

const loading = ref(false)
const transactionList = ref([])

const searchForm = reactive({
  customerName: '',
  transactionType: undefined,
  dateRange: null
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '时间', dataIndex: 'transactionTime', key: 'transactionTime', width: 150 },
  { title: '关联人员', dataIndex: 'customerName', key: 'customerName' },
  { title: '资金类型', dataIndex: 'transactionType', key: 'transactionType', width: 100 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '对方账户', dataIndex: 'counterAccount', key: 'counterAccount' },
  { title: '资金渠道', dataIndex: 'channel', key: 'channel', width: 100 },
  { title: '风险标记', dataIndex: 'riskTag', key: 'riskTag', width: 120 }
]

async function fetchTransactionList() {
  loading.value = true
  try {
    const res = await getTransactionList({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
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
