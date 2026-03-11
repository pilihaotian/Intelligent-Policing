<template>
  <div class="knowledge-base">
    <a-card title="知识库管理">
      <!-- 操作栏 -->
      <div class="toolbar">
        <a-button type="primary" @click="handleAddKb">
          <template #icon><PlusOutlined /></template>
          新建知识库
        </a-button>
      </div>
      
      <!-- 知识库列表 -->
      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="8" v-for="kb in kbList" :key="kb.id">
          <a-card hoverable class="kb-card" @click="handleViewKb(kb)">
            <template #title>
              <FolderOutlined style="color: #1890ff; margin-right: 8px" />
              {{ kb.kbName }}
            </template>
            <template #extra>
              <a-dropdown @click.stop>
                <a-button type="text" size="small">
                  <MoreOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleEditKb(kb)">编辑</a-menu-item>
                    <a-menu-item @click="handleUploadDoc(kb)">上传文档</a-menu-item>
                    <a-menu-item danger @click="handleDeleteKb(kb)">删除</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </template>
            
            <p class="kb-desc">{{ kb.description || '暂无描述' }}</p>
            <div class="kb-stats">
              <span><FileTextOutlined /> {{ kb.docCount || 0 }} 文档</span>
              <span><ClockCircleOutlined /> {{ kb.updatedTime }}</span>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
    
    <!-- 新建/编辑知识库弹窗 -->
    <a-modal 
      v-model:open="kbModalVisible" 
      :title="kbModalTitle"
      @ok="handleSaveKb"
    >
      <a-form :model="kbForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="知识库名称" required>
          <a-input v-model:value="kbForm.kbName" placeholder="请输入知识库名称" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="kbForm.description" :rows="3" placeholder="请输入描述" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="kbForm.kbType" style="width: 100%">
            <a-select-option value="LAW">法律法规</a-select-option>
            <a-select-option value="CASE">案件案例</a-select-option>
            <a-select-option value="BIZ">业务知识</a-select-option>
            <a-select-option value="DOC">文书模板</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
    
    <!-- 上传文档弹窗 -->
    <a-modal 
      v-model:open="uploadModalVisible" 
      title="上传文档"
      @ok="handleConfirmUpload"
    >
      <a-upload-dragger
        v-model:fileList="uploadFileList"
        name="files"
        :multiple="true"
        :before-upload="beforeUpload"
        accept=".pdf,.doc,.docx,.txt"
      >
        <p class="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p class="ant-upload-text">点击或拖拽文件到此区域</p>
        <p class="ant-upload-hint">支持 PDF、Word、TXT 格式</p>
      </a-upload-dragger>
    </a-modal>
    
    <!-- 知识库详情弹窗 -->
    <a-modal 
      v-model:open="detailModalVisible" 
      :title="currentKb?.kbName"
      width="900px"
      :footer="null"
    >
      <a-table 
        :dataSource="docList" 
        :columns="docColumns" 
        :loading="docLoading"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'READY' ? 'green' : record.status === 'PENDING' ? 'orange' : 'red'">
              {{ record.status === 'READY' ? '就绪' : record.status === 'PENDING' ? '待处理' : record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handlePreviewDoc(record)">预览</a-button>
              <a-popconfirm title="确定删除?" @confirm="handleDeleteDoc(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  PlusOutlined, 
  FolderOutlined, 
  MoreOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'
import { getKbList, createKb, updateKb, deleteKb, getKbDocs, uploadDocs, deleteDoc } from '@/api/ai'

const kbList = ref([])
const kbModalVisible = ref(false)
const uploadModalVisible = ref(false)
const detailModalVisible = ref(false)
const kbModalTitle = ref('新建知识库')
const currentKb = ref(null)
const docList = ref([])
const docLoading = ref(false)
const uploadFileList = ref([])

const kbForm = reactive({
  id: null,
  kbName: '',
  description: '',
  kbType: 'RISK_MANAGEMENT'
})

const docColumns = [
  { title: '文档名称', dataIndex: 'docName', key: 'docName' },
  { title: '文档类型', dataIndex: 'docType', key: 'docType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '上传时间', dataIndex: 'createdTime', key: 'createdTime' },
  { title: '操作', key: 'action', width: 150 }
]

async function fetchKbList() {
  try {
    const res = await getKbList({ current: 1, size: 100 })
    kbList.value = res.records
  } catch (error) {
    console.error(error)
  }
}

function handleAddKb() {
  kbModalTitle.value = '新建知识库'
  Object.assign(kbForm, { id: null, kbName: '', description: '', kbType: 'RISK_MANAGEMENT' })
  kbModalVisible.value = true
}

function handleEditKb(kb) {
  kbModalTitle.value = '编辑知识库'
  Object.assign(kbForm, kb)
  kbModalVisible.value = true
}

async function handleSaveKb() {
  try {
    if (kbForm.id) {
      await updateKb(kbForm.id, kbForm)
      message.success('修改成功')
    } else {
      await createKb(kbForm)
      message.success('创建成功')
    }
    kbModalVisible.value = false
    fetchKbList()
  } catch (error) {
    message.error('操作失败')
  }
}

async function handleDeleteKb(kb) {
  try {
    await deleteKb(kb.id)
    message.success('删除成功')
    fetchKbList()
  } catch (error) {
    message.error('删除失败')
  }
}

async function handleViewKb(kb) {
  currentKb.value = kb
  detailModalVisible.value = true
  docLoading.value = true
  
  try {
    const res = await getKbDocs(kb.id)
    docList.value = res
  } catch (error) {
    console.error(error)
  } finally {
    docLoading.value = false
  }
}

function handleUploadDoc(kb) {
  currentKb.value = kb
  uploadFileList.value = []
  uploadModalVisible.value = true
}

function beforeUpload(file) {
  uploadFileList.value = [...uploadFileList.value, file]
  return false
}

async function handleConfirmUpload() {
  if (uploadFileList.value.length === 0) {
    message.warning('请选择文件')
    return
  }

  const formData = new FormData()
  uploadFileList.value.forEach(file => {
    // ant-design-vue 包装的文件对象，原始文件在 originFileObj 中
    const rawFile = file.originFileObj || file
    formData.append('files', rawFile)
  })

  try {
    await uploadDocs(currentKb.value.id, formData)
    message.success('上传成功，文档正在处理中')
    uploadModalVisible.value = false
    fetchKbList()
  } catch (error) {
    message.error('上传失败')
  }
}

async function handleDeleteDoc(doc) {
  try {
    await deleteDoc(doc.id)
    message.success('删除成功')
    handleViewKb(currentKb.value)
  } catch (error) {
    message.error('删除失败')
  }
}

function handlePreviewDoc(doc) {
  message.info('文档预览功能开发中')
}

onMounted(() => {
  fetchKbList()
})
</script>

<style lang="less" scoped>
.knowledge-base {
  .toolbar {
    margin-bottom: 16px;
  }
  
  .kb-card {
    margin-bottom: 16px;
    
    .kb-desc {
      color: #666;
      font-size: 13px;
      height: 40px;
      overflow: hidden;
    }
    
    .kb-stats {
      display: flex;
      justify-content: space-between;
      color: #999;
      font-size: 12px;
      margin-top: 12px;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}
</style>
