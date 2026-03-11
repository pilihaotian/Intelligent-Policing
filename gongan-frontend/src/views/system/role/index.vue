<template>
  <div class="role-manage">
    <a-card title="角色管理">
      <div class="toolbar">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增角色
        </a-button>
      </div>
      
      <a-table 
        :dataSource="roleList" 
        :columns="columns" 
        :loading="loading" 
        rowKey="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handlePermission(record)">权限配置</a-button>
              <a-popconfirm title="确定删除?" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    
    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑角色' : '新增角色'" @ok="handleSubmit" :confirmLoading="submitLoading">
      <a-form :model="roleForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="角色名称" required>
          <a-input v-model:value="roleForm.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色编码" required>
          <a-input v-model:value="roleForm.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="roleForm.description" :rows="2" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="roleForm.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
    
    <a-modal v-model:open="permModalVisible" title="权限配置" width="600px" @ok="handleSavePermission" :confirmLoading="permLoading">
      <a-tree
        v-model:checkedKeys="checkedKeys"
        :tree-data="menuTree"
        checkable
        :field-names="{ title: 'menuName', key: 'id', children: 'children' }"
        default-expand-all
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getRoleList, addRole, updateRole, deleteRole, assignMenus, getRoleMenus } from '@/api/role'
import { getMenuTree } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const permLoading = ref(false)
const modalVisible = ref(false)
const permModalVisible = ref(false)
const isEdit = ref(false)
const currentRoleId = ref(null)
const checkedKeys = ref([])

const roleList = ref([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const columns = [
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action', width: 200 }
]

const menuTree = ref([])

async function loadRoleList() {
  loading.value = true
  try {
    const res = await getRoleList({
      current: pagination.value.current,
      size: pagination.value.pageSize
    })
    roleList.value = res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    message.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  try {
    const menus = await getMenuTree()
    menuTree.value = buildMenuTree(menus || [])
  } catch (error) {
    console.error('加载菜单树失败', error)
  }
}

function buildMenuTree(menus, parentId = 0) {
  return menus
    .filter(menu => menu.parentId === parentId)
    .map(menu => ({
      id: menu.id,
      menuName: menu.menuName,
      children: buildMenuTree(menus, menu.id)
    }))
}

function handleTableChange(page) {
  pagination.value.current = page.current
  pagination.value.pageSize = page.pageSize
  loadRoleList()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(roleForm, { id: null, roleName: '', roleCode: '', description: '', status: 1 })
  modalVisible.value = true
}

function handleEdit(record) {
  isEdit.value = true
  Object.assign(roleForm, {
    id: record.id,
    roleName: record.roleName,
    roleCode: record.roleCode,
    description: record.description,
    status: record.status
  })
  modalVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateRole(roleForm)
      message.success('修改成功')
    } else {
      await addRole(roleForm)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadRoleList()
  } catch (error) {
    message.error(isEdit.value ? '修改失败' : '新增失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(record) {
  try {
    await deleteRole(record.id)
    message.success('删除成功')
    loadRoleList()
  } catch (error) {
    message.error('删除失败')
  }
}

async function handlePermission(record) {
  currentRoleId.value = record.id
  permModalVisible.value = true
  
  // 加载角色已有的权限
  try {
    const res = await getRoleMenus(record.id)
    checkedKeys.value = res || []
  } catch (error) {
    checkedKeys.value = []
  }
}

async function handleSavePermission() {
  permLoading.value = true
  try {
    await assignMenus(currentRoleId.value, checkedKeys.value)
    message.success('权限配置成功')
    permModalVisible.value = false
  } catch (error) {
    message.error('权限配置失败')
  } finally {
    permLoading.value = false
  }
}

onMounted(() => {
  loadRoleList()
  loadMenuTree()
})
</script>

<style lang="less" scoped>
.role-manage {
  .toolbar {
    margin-bottom: 16px;
  }
}
</style>