<template>
  <div class="legal-document">
    <a-card>
      <template #title>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>案件信息智能填报</span>
          <a-upload
            name="file"
            :action="uploadUrl"
            :headers="uploadHeaders"
            accept=".pdf"
            :showUploadList="false"
            @change="handleUploadChange"
          >
            <a-button type="primary">
              <template #icon><UploadOutlined /></template>
              上传PDF文书
            </a-button>
          </a-upload>
        </div>
      </template>

      <a-row :gutter="24">
        <!-- 左侧：文书列表 -->
        <a-col :span="8">
          <a-card title="文书列表" size="small" :loading="listLoading">
            <a-list :dataSource="documentList" size="small">
              <template #renderItem="{ item }">
                <a-list-item 
                  :class="['doc-item', { active: currentDoc?.id === item.id }]"
                  @click="handleSelectDoc(item)"
                >
                  <a-list-item-meta>
                    <template #title>
                      <div style="display: flex; justify-content: space-between; align-items: center">
                        <span class="doc-title">{{ item.docTitle || item.fileName }}</span>
                        <a-tag :color="getStatusColor(item.status)" size="small">
                          {{ getStatusText(item.status) }}
                        </a-tag>
                      </div>
                    </template>
                    <template #description>
                      <div>{{ item.docNo }}</div>
                      <div style="font-size: 12px; color: #999">{{ item.createdTime }}</div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
              <template #empty>
                <a-empty description="暂无文书，请上传PDF" />
              </template>
            </a-list>
            
            <div style="text-align: center; margin-top: 16px" v-if="pagination.total > pagination.pageSize">
              <a-pagination 
                v-model:current="pagination.current" 
                :total="pagination.total"
                :page-size="pagination.pageSize"
                size="small"
                @change="fetchDocumentList"
              />
            </div>
          </a-card>
        </a-col>
        
        <!-- 右侧：提取结果 -->
        <a-col :span="16">
          <a-card title="提取结果" size="small">
            <a-spin :spinning="extracting">
              <div v-if="currentDoc">
                <div style="margin-bottom: 16px">
                  <a-alert 
                    v-if="currentDoc.status === 0" 
                    type="warning" 
                    message="AI已提取以下信息，请核对确认" 
                    show-icon 
                  />
                  <a-alert 
                    v-else-if="currentDoc.status === 1" 
                    type="success" 
                    message="已确认" 
                    show-icon 
                  />
                </div>
                
                <a-form 
                  :model="formData" 
                  :label-col="{ span: 6 }"
                  :wrapper-col="{ span: 16 }"
                >
                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="案号">
                        <a-input v-model:value="formData.caseNumber" placeholder="自动提取或手动输入" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="文书日期">
                        <a-date-picker v-model:value="formData.caseDate" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                  
                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="涉及机构">
                        <a-input v-model:value="formData.orgName" placeholder="机构名称" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="涉及用户">
                        <a-input v-model:value="formData.userName" placeholder="当事人姓名" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                  
                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="涉及金额">
                        <a-input-number 
                          v-model:value="formData.amount" 
                          :min="0"
                          style="width: 100%"
                          :formatter="value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                          :parser="value => value.replace(/\¥\s?|(,*)/g, '')"
                        />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="币种">
                        <a-select v-model:value="formData.currency">
                          <a-select-option value="CNY">人民币</a-select-option>
                          <a-select-option value="USD">美元</a-select-option>
                          <a-select-option value="EUR">欧元</a-select-option>
                        </a-select>
                      </a-form-item>
                    </a-col>
                  </a-row>
                  
                  <a-form-item label="发文/审理单位">
                    <a-input v-model:value="formData.court" placeholder="法院、仲裁委等" />
                  </a-form-item>
                  
                  <a-form-item label="案由/事由">
                    <a-input v-model:value="formData.caseReason" placeholder="案件原因" />
                  </a-form-item>
                  
                  <a-form-item label="处理结果">
                    <a-textarea v-model:value="formData.result" :rows="3" placeholder="判决结果或处理结果" />
                  </a-form-item>
                  
                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-form-item label="风险等级">
                        <a-select v-model:value="formData.riskLevel">
                          <a-select-option value="HIGH">
                            <a-tag color="red">高风险</a-tag>
                          </a-select-option>
                          <a-select-option value="MEDIUM">
                            <a-tag color="orange">中风险</a-tag>
                          </a-select-option>
                          <a-select-option value="LOW">
                            <a-tag color="green">低风险</a-tag>
                          </a-select-option>
                        </a-select>
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="AI置信度">
                        <a-progress 
                          :percent="confidencePercent" 
                          :status="confidenceStatus"
                          size="small"
                        />
                      </a-form-item>
                    </a-col>
                  </a-row>
                  
                  <a-divider />
                  
                  <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                    <a-space>
                      <a-button type="primary" @click="handleConfirm" :loading="saving">
                        确认保存
                      </a-button>
                      <a-button @click="handleReExtract" :loading="extracting">
                        重新提取
                      </a-button>
                      <a-popconfirm title="确定删除此文书记录?" @confirm="handleDelete">
                        <a-button danger>删除</a-button>
                      </a-popconfirm>
                    </a-space>
                  </a-form-item>
                </a-form>
              </div>
              
              <a-empty v-else description="请选择文书或上传新的PDF" />
            </a-spin>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/store/user'
import { getDocumentList, extractDocument, saveExtractResult, deleteDocument } from '@/api/document'
import dayjs from 'dayjs'

const userStore = useUserStore()
const listLoading = ref(false)
const extracting = ref(false)
const saving = ref(false)
const documentList = ref([])
const currentDoc = ref(null)

const uploadUrl = computed(() => '/api/ops/document/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  caseNumber: '',
  orgName: '',
  userName: '',
  amount: null,
  currency: 'CNY',
  caseDate: null,
  court: '',
  caseReason: '',
  result: '',
  riskLevel: 'MEDIUM'
})

const confidencePercent = computed(() => {
  const c = currentDoc.value?.aiConfidence
  return c ? Math.round(c.toNumber ? c.toNumber() : c) : 0
})

const confidenceStatus = computed(() => {
  const p = confidencePercent.value
  if (p >= 80) return 'success'
  if (p >= 50) return 'normal'
  return 'exception'
})

function getStatusColor(status) {
  const colors = { 0: 'orange', 1: 'green', 2: 'red' }
  return colors[status] || 'default'
}

function getStatusText(status) {
  const texts = { 0: '待确认', 1: '已确认', 2: '已驳回' }
  return texts[status] || '未知'
}

async function fetchDocumentList() {
  listLoading.value = true
  try {
    const res = await getDocumentList({
      current: pagination.current,
      size: pagination.pageSize
    })
    documentList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    listLoading.value = false
  }
}

async function handleUploadChange(info) {
  const { status, response } = info.file
  if (status === 'done') {
    if (response?.code === 200) {
      message.success('上传成功')
      await fetchDocumentList()
      // 从列表中获取最新数据（按时间倒序，第一个就是最新的）
      if (documentList.value.length > 0) {
        handleSelectDoc(documentList.value[0])
      }
    } else {
      message.error(response?.message || '上传失败')
    }
  } else if (status === 'error') {
    message.error('上传失败')
  }
}

async function handleSelectDoc(doc) {
  currentDoc.value = doc
  console.log('选择文档:', doc)
  console.log('extractContent:', doc.extractContent)
  
  // 解析提取内容
  if (doc.extractContent) {
    try {
      const data = JSON.parse(doc.extractContent)
      console.log('解析后的数据:', data)
      Object.assign(formData, {
        caseNumber: data.caseNumber || '',
        orgName: data.orgName || '',
        userName: data.userName || '',
        amount: data.amount || null,
        currency: data.currency || 'CNY',
        caseDate: data.caseDate ? dayjs(data.caseDate) : null,
        court: data.court || '',
        caseReason: data.caseReason || '',
        result: data.result || '',
        riskLevel: data.riskLevel || 'MEDIUM'
      })
    } catch (e) {
      console.error('解析extractContent失败:', e)
      resetForm()
    }
  } else {
    console.log('extractContent为空，重置表单')
    resetForm()
  }
}

function resetForm() {
  Object.assign(formData, {
    caseNumber: '',
    orgName: '',
    userName: '',
    amount: null,
    currency: 'CNY',
    caseDate: null,
    court: '',
    caseReason: '',
    result: '',
    riskLevel: 'MEDIUM'
  })
}

async function handleReExtract() {
  if (!currentDoc.value) return
  
  extracting.value = true
  try {
    const res = await extractDocument(currentDoc.value.id)
    currentDoc.value.extractContent = JSON.stringify(res.extractedData)
    currentDoc.value.aiConfidence = res.confidence
    
    const data = res.extractedData
    Object.assign(formData, {
      caseNumber: data.caseNumber || '',
      orgName: data.orgName || '',
      userName: data.userName || '',
      amount: data.amount || null,
      currency: data.currency || 'CNY',
      caseDate: data.caseDate ? dayjs(data.caseDate) : null,
      court: data.court || '',
      caseReason: data.caseReason || '',
      result: data.result || '',
      riskLevel: data.riskLevel || 'MEDIUM'
    })
    
    message.success('重新提取完成')
  } catch (error) {
    message.error('提取失败')
  } finally {
    extracting.value = false
  }
}

async function handleConfirm() {
  if (!currentDoc.value) return
  
  saving.value = true
  try {
    await saveExtractResult(currentDoc.value.id, formData)
    currentDoc.value.status = 1
    message.success('保存成功')
    fetchDocumentList()
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!currentDoc.value) return
  
  try {
    await deleteDocument(currentDoc.value.id)
    message.success('删除成功')
    currentDoc.value = null
    resetForm()
    fetchDocumentList()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchDocumentList()
})
</script>

<style lang="less" scoped>
.legal-document {
  .doc-item {
    cursor: pointer;
    padding: 12px !important;
    border-radius: 4px;
    transition: background 0.2s;
    
    &.active {
      background: #e6f7ff;
      border-left: 3px solid #1890ff;
    }
    
    &:hover {
      background: #f5f5f5;
    }
    
    .doc-title {
      max-width: 150px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: inline-block;
    }
  }
  
  .ant-form-item {
    margin-bottom: 16px;
  }
}
</style>
