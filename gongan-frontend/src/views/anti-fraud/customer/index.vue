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
      
      <a-table :dataSource="customerList" :columns="columns" :loading="loading" rowKey="id" style="margin-top: 16px" :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskLevel'">
            <a-tag :color="getRiskColor(record.riskLevel)">{{ getRiskText(record.riskLevel) }}</a-tag>
          </template>
          <template v-if="column.key === 'blacklistFlag'">
            <a-tag :color="record.blacklistFlag ? 'red' : 'default'">
              {{ record.blacklistFlag ? '是' : '否' }}
            </a-tag>
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
        <a-descriptions-item label="人员编号">{{ currentCustomer.customerNo }}</a-descriptions-item>
        <a-descriptions-item label="人员姓名">{{ currentCustomer.customerName }}</a-descriptions-item>
        <a-descriptions-item label="证件类型">{{ getIdTypeText(currentCustomer.idType) }}</a-descriptions-item>
        <a-descriptions-item label="证件号码">{{ currentCustomer.idNo }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentCustomer.phone }}</a-descriptions-item>
        <a-descriptions-item label="电子邮箱">{{ currentCustomer.email }}</a-descriptions-item>
        <a-descriptions-item label="性别">{{ currentCustomer.gender === 1 ? '男' : '女' }}</a-descriptions-item>
        <a-descriptions-item label="出生日期">{{ currentCustomer.birthDate }}</a-descriptions-item>
        <a-descriptions-item label="人员类型">{{ currentCustomer.customerType }}</a-descriptions-item>
        <a-descriptions-item label="风险等级">
          <a-tag :color="getRiskColor(currentCustomer.riskLevel)">{{ getRiskText(currentCustomer.riskLevel) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="风险评分">{{ currentCustomer.riskScore }}</a-descriptions-item>
        <a-descriptions-item label="黑名单标记">
          <a-tag :color="currentCustomer.blacklistFlag ? 'red' : 'default'">
            {{ currentCustomer.blacklistFlag ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="关注名单标记">
          <a-tag :color="currentCustomer.watchlistFlag ? 'orange' : 'default'">
            {{ currentCustomer.watchlistFlag ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="可疑类型">{{ currentCustomer.suspiciousType }}</a-descriptions-item>
        <a-descriptions-item label="可疑次数">{{ currentCustomer.suspiciousCount }}</a-descriptions-item>
        <a-descriptions-item label="首次可疑时间">{{ currentCustomer.firstSuspiciousTime }}</a-descriptions-item>
        <a-descriptions-item label="最近可疑时间">{{ currentCustomer.lastSuspiciousTime }}</a-descriptions-item>
        <a-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</a-descriptions-item>
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

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const searchForm = reactive({
  customerName: '',
  riskLevel: undefined
})

const columns = [
  { title: '人员编号', dataIndex: 'customerNo', key: 'customerNo', width: 120 },
  { title: '人员姓名', dataIndex: 'customerName', key: 'customerName', width: 100 },
  { title: '证件号码', dataIndex: 'idNo', key: 'idNo', width: 180 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel', width: 100 },
  { title: '风险评分', dataIndex: 'riskScore', key: 'riskScore', width: 100 },
  { title: '可疑次数', dataIndex: 'suspiciousCount', key: 'suspiciousCount', width: 100 },
  { title: '最近可疑时间', dataIndex: 'lastSuspiciousTime', key: 'lastSuspiciousTime', width: 160 },
  { title: '黑名单', dataIndex: 'blacklistFlag', key: 'blacklistFlag', width: 80 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

function getRiskColor(level) {
  const colors = { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }
  return colors[level] || 'default'
}

function getRiskText(level) {
  const texts = { HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return texts[level] || '未知'
}

function getIdTypeText(type) {
  const texts = { ID_CARD: '身份证', PASSPORT: '护照', OTHER: '其他' }
  return texts[type] || type
}

async function fetchCustomerList() {
  loading.value = true
  try {
    const res = await getSuspiciousCustomers({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
    customerList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
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