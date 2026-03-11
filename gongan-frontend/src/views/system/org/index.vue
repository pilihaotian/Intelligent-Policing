<template>
  <div class="org-manage">
    <a-card title="机构管理">
      <div class="toolbar">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增机构
        </a-button>
      </div>
      
      <a-table 
        :dataSource="orgList" 
        :columns="columns" 
        :loading="loading" 
        rowKey="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orgType'">
            <a-tag>{{ getOrgTypeText(record.orgType) }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定删除?" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑机构' : '新增机构'" @ok="handleSubmit" :confirmLoading="submitLoading">
      <a-form :model="orgForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="机构名称" required>
          <a-input v-model:value="orgForm.orgName" placeholder="请输入机构名称" />
        </a-form-item>
        <a-form-item label="机构编码" required>
          <a-input v-model:value="orgForm.orgCode" :disabled="isEdit" placeholder="请输入机构编码" />
        </a-form-item>
        <a-form-item label="机构类型" required>
          <a-select v-model:value="orgForm.orgType" placeholder="请选择机构类型">
            <a-select-option value="HEAD_OFFICE">总行</a-select-option>
            <a-select-option value="BRANCH">分行</a-select-option>
            <a-select-option value="SUB_BRANCH">支行</a-select-option>
            <a-select-option value="DEPARTMENT">部门</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="上级机构">
          <a-tree-select
            v-model:value="orgForm.parentId"
            :tree-data="orgTree"
            :field-names="{ label: 'orgName', value: 'id', children: 'children' }"
            placeholder="请选择上级机构"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="机构地址">
          <a-input v-model:value="orgForm.address" placeholder="请输入机构地址" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="orgForm.contactPhone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="orgForm.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getOrgList, getOrgTree, addOrg, updateOrg, deleteOrg } from '@/api/org'

const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)

const orgList = ref([])
const orgTree = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const orgForm = reactive({
  id: null,
  orgName: '',
  orgCode: '',
  orgType: 'BRANCH',
  parentId: null,
  address: '',
  contactPhone: '',
  status: 1
})

const columns = [
  { title: '机构名称', dataIndex: 'orgName', key: 'orgName' },
  { title: '机构编码', dataIndex: 'orgCode', key: 'orgCode' },
  { title: '机构类型', dataIndex: 'orgType', key: 'orgType' },
  { title: '地址', dataIndex: 'address', key: 'address' },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 150 }
]

function getOrgTypeText(type) {
  const types = {
    'HEAD_OFFICE': '总行',
    'BRANCH': '分行',
    'SUB_BRANCH': '支行',
    'DEPARTMENT': '部门'
  }
  return types[type] || type || '未知'
}

async function loadOrgList() {
  loading.value = true
  try {
    const res = await getOrgList({
      current: pagination.value.current,
      size: pagination.value.pageSize
    })
    orgList.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    message.error('加载机构列表失败')
  } finally {
    loading.value = false
  }
}

async function loadOrgTree() {
  try {
    const res = await getOrgTree()
    orgTree.value = res || []
  } catch (error) {
    message.error('加载机构树失败')
  }
}

function handleTableChange(page) {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  loadOrgList()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(orgForm, { id: null, orgName: '', orgCode: '', orgType: 'BRANCH', parentId: null, address: '', contactPhone: '', status: 1 })
  modalVisible.value = true
}

function handleEdit(record) {
  isEdit.value = true
  Object.assign(orgForm, {
    id: record.id,
    orgName: record.orgName,
    orgCode: record.orgCode,
    orgType: record.orgType || 'BRANCH',
    parentId: record.parentId,
    address: record.address,
    contactPhone: record.contactPhone,
    status: record.status
  })
  modalVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateOrg(orgForm)
      message.success('修改成功')
    } else {
      await addOrg(orgForm)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadOrgList()
    loadOrgTree()
  } catch (error) {
    message.error(isEdit.value ? '修改失败' : '新增失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(record) {
  try {
    await deleteOrg(record.id)
    message.success('删除成功')
    loadOrgList()
    loadOrgTree()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadOrgList()
  loadOrgTree()
})
</script>

<style lang="less" scoped>
.org-manage {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>