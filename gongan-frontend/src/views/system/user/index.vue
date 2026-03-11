<template>
  <div class="user-manage">
    <a-card title="用户管理">
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allowClear />
        </a-form-item>
        <a-form-item label="真实姓名">
          <a-input v-model:value="searchForm.realName" placeholder="请输入真实姓名" allowClear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      
      <!-- 操作按钮 -->
      <div class="table-actions">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增用户
        </a-button>
      </div>
      
      <!-- 用户表格 -->
      <a-table 
        :dataSource="userList" 
        :columns="columns" 
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'gender'">
            {{ record.gender === 1 ? '男' : record.gender === 2 ? '女' : '未知' }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleAssignRole(record)">分配角色</a-button>
              <a-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <!-- 新增/编辑弹窗 -->
    <a-modal 
      v-model:open="modalVisible" 
      :title="modalTitle"
      @ok="handleSubmit"
      :confirmLoading="submitLoading"
    >
      <a-form 
        ref="formRef"
        :model="userForm" 
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="userForm.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="userForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="userForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="性别" name="gender">
          <a-radio-group v-model:value="userForm.gender">
            <a-radio :value="1">男</a-radio>
            <a-radio :value="2">女</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="userForm.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- 分配角色弹窗 -->
    <a-modal 
      v-model:open="roleModalVisible" 
      title="分配角色"
      @ok="handleRoleSubmit"
    >
      <a-checkbox-group v-model:value="selectedRoles">
        <a-row>
          <a-col :span="12" v-for="role in roleList" :key="role.id">
            <a-checkbox :value="role.id">{{ role.roleName }}</a-checkbox>
          </a-col>
        </a-row>
      </a-checkbox-group>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, assignRoles } from '@/api/user'
import { getAllRoles } from '@/api/role'

const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const roleModalVisible = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增用户')
const formRef = ref(null)
const currentUserId = ref(null)

const searchForm = reactive({
  username: '',
  realName: '',
  status: undefined
})

const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 1,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入真实姓名' }]
}

const userList = ref([])
const roleList = ref([])

const selectedRoles = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '性别', dataIndex: 'gender', key: 'gender' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 200 }
]

async function fetchUserList() {
  loading.value = true
  try {
    const res = await getUserList({
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    })
    userList.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchUserList()
}

function handleReset() {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = undefined
  handleSearch()
}

function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUserList()
}

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增用户'
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    gender: 1,
    status: 1
  })
  modalVisible.value = true
}

function handleEdit(record) {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  Object.assign(userForm, record)
  modalVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await updateUser(userForm)
      message.success('修改成功')
    } else {
      await addUser(userForm)
      message.success('新增成功')
    }
    
    modalVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(record) {
  try {
    await deleteUser(record.id)
    message.success('删除成功')
    fetchUserList()
  } catch (error) {
    console.error(error)
  }
}

function handleAssignRole(record) {
  currentUserId.value = record.id
  selectedRoles.value = []
  roleModalVisible.value = true
}

async function handleRoleSubmit() {
  try {
    await assignRoles(currentUserId.value, selectedRoles.value)
    message.success('分配角色成功')
    roleModalVisible.value = false
  } catch (error) {
    console.error(error)
  }
}

async function fetchRoleList() {
  try {
    const res = await getAllRoles()
    roleList.value = res || []
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

onMounted(() => {
  fetchUserList()
  fetchRoleList()
})
</script>

<style lang="less" scoped>
.user-manage {
  .search-form {
    margin-bottom: 16px;
  }
  
  .table-actions {
    margin-bottom: 16px;
  }
}
</style>
