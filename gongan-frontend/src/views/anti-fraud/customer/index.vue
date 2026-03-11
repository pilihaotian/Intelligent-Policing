<template>
  <div class="suspicious-customer">
    <a-card title="重点人员管理">
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="人员姓名">
          <a-input v-model:value="searchForm.customerName" placeholder="请输入" allowClear />
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
      
      <a-table :dataSource="customerList" :columns="columns" :loading="loading" rowKey="id" style="margin-top: 16px">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskLevel'">
            <a-tag :color="getRiskColor(record.riskLevel)">{{ getRiskText(record.riskLevel) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
              <router-link :to="`/anti-fraud/analysis?id=${record.id}`">
                <a-button type="link" size="small">分析</a-button>
              </router-link>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <a-modal v-model:open="detailVisible" title="人员详情" width="700px" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentCustomer">
        <a-descriptions-item label="人员姓名">{{ currentCustomer.customerName }}</a-descriptions-item>
        <a-descriptions-item label="身份证号">{{ currentCustomer.idCard }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentCustomer.phone }}</a-descriptions-item>
        <a-descriptions-item label="风险等级">
          <a-tag :color="getRiskColor(currentCustomer.riskLevel)">{{ getRiskText(currentCustomer.riskLevel) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="登记日期">{{ currentCustomer.openAccountDate }}</a-descriptions-item>
        <a-descriptions-item label="管控状态">{{ currentCustomer.accountStatus }}</a-descriptions-item>
        <a-descriptions-item label="职业/身份">{{ currentCustomer.occupation }}</a-descriptions-item>
        <a-descriptions-item label="涉案记录数">{{ currentCustomer.suspiciousCount }}</a-descriptions-item>
        <a-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</a-descriptions-item>
        <a-descriptions-item label="风险特征" :span="2">{{ currentCustomer.riskFeatures }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSuspiciousCustomers } from '@/api/fraud'

const loading = ref(false)
const detailVisible = ref(false)
const currentCustomer = ref(null)
const customerList = ref([])

const searchForm = reactive({
  customerName: '',
  riskLevel: undefined
})

const columns = [
  { title: '人员姓名', dataIndex: 'customerName', key: 'customerName' },
  { title: '身份证号', dataIndex: 'idCard', key: 'idCard' },
  { title: '联系电话', dataIndex: 'phone', key: 'phone' },
  { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel' },
  { title: '涉案记录数', dataIndex: 'suspiciousCount', key: 'suspiciousCount' },
  { title: '标记时间', dataIndex: 'markTime', key: 'markTime' },
  { title: '操作', key: 'action', width: 120 }
]

function getRiskColor(level) {
  const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }
  return colors[level] || 'default'
}

function getRiskText(level) {
  const texts = { HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return texts[level] || '未知'
}

async function fetchCustomerList() {
  loading.value = true
  try {
    const res = await getSuspiciousCustomers(searchForm)
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

function handleView(record) {
  currentCustomer.value = record
  detailVisible.value = true
}

onMounted(() => {
  fetchCustomerList()
})
</script>

<style lang="less" scoped>
.suspicious-customer {
  .search-form {
    margin-bottom: 16px;
  }
}
</style>
