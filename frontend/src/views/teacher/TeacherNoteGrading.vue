<template>
  <div class="note-grading">
    <el-page-header @back="router.go(-1)" title="作业批改与评分" />
    
    <el-row :gutter="20" class="mt-20">
      <!-- 学生提交列表 -->
      <el-col :span="6">
        <el-card header="待批改列表">
          <el-table :data="submissions" style="width: 100%" @current-change="handleSelectSubmission" highlight-current-row v-loading="loadingList">
            <el-table-column prop="userName" label="姓名" />
            <el-table-column prop="isScore" label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.submitted" :type="scope.row.isScore === 1 ? 'success' : 'warning'">
                  {{ scope.row.isScore === 1 ? '已批改' : '未批改' }}
                </el-tag>
                <el-tag v-else type="info">未提交</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 批改详情 -->
      <el-col :span="12">
        <el-card v-if="selectedSubmission.noteId" v-loading="loadingDetail">
          <template #header>
            <div class="grading-header">
              <span>{{ selectedSubmission.userName }} 的笔记</span>
            </div>
          </template>
          
          <div class="note-content">
            <h3>{{ noteDetail.title }}</h3>
            <v-md-editor :model-value="noteDetail.content" mode="preview"></v-md-editor>
            
            <!-- 附件列表 -->
            <div v-if="attachments && attachments.length > 0" class="attachment-section">
              <el-divider />
              <h4><el-icon><Document /></el-icon> 附件 ({{ attachments.length }})</h4>
              <div class="attachment-list">
                <div v-for="(file, index) in attachments" :key="index" class="attachment-item">
                  <el-icon><Document /></el-icon>
                  <span class="file-name" @click="downloadAttachment(file)">{{ file.fileName }}</span>
                  <el-tag size="small" type="info" v-if="file.fileSize">{{ file.fileSize }}KB</el-tag>
                  <el-button link type="primary" size="small" @click="downloadAttachment(file)">
                    <el-icon><Download /></el-icon> 下载
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
        <el-empty v-else description="请从左侧选择一个学生笔记进行批改" />
      </el-col>

      <!-- 评分表单区域 (固定在右侧或作为侧边栏) -->
      <el-col :span="6">
        <el-card header="批改评分" class="sticky-card">
          <el-form :model="gradingForm" label-position="top">
            <el-form-item label="打分 (0-100)">
              <el-input-number v-model="gradingForm.score" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
            <el-form-item label="评价内容">
              <el-input v-model="gradingForm.comment" type="textarea" :rows="10" placeholder="请输入评价内容..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width: 100%; margin-bottom: 10px;" @click="submitGrading" :disabled="!selectedSubmission.noteId">提交批改</el-button>
              <el-button type="warning" style="width: 100%" @click="setAsExampleNote" :disabled="!selectedSubmission.noteId">
                <el-icon><Star /></el-icon>
                设为示例笔记
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 悬浮批改按钮 (如果评分表单被隐藏时使用，这里我们采用侧边栏形式，保留一个浮动按钮作为视觉增强) -->
    <div class="floating-grade-btn" v-if="selectedSubmission.noteId">
      <el-button type="success" circle size="large" @click="focusComment">
        <el-icon><edit /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Star, Document, Download } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import api from '../../api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const taskId = route.params.id
const selectedSubmission = ref({})
const noteDetail = ref({})
const loadingList = ref(false)
const loadingDetail = ref(false)
const gradingForm = reactive({
  score: 0,
  comment: ''
})

const submissions = ref([])
const isSettingExample = ref(false)
const exampleReason = ref('')
const attachments = ref([])

const fetchSubmissions = async () => {
  loadingList.value = true
  try {
    const res = await api.get(`/api/teacher/courses/tasks/${taskId}/submissions-status`)
    if (res.code === 200) {
      submissions.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取学生提交列表失败')
  } finally {
    loadingList.value = false
  }
}

const handleSelectSubmission = async (val) => {
  if (val && val.submitted) {
    selectedSubmission.value = val
    // 回显已有的评分和评价
    gradingForm.score = val.score || 0
    gradingForm.comment = val.comment || ''
    
    // 获取笔记详情
    loadingDetail.value = true
    try {
      const res = await api.get(`api/note/${val.noteId}`)
      if (res && res.code === 200) {
        noteDetail.value = res.data.note
        attachments.value = res.data.attachments || []
      }
    } catch (error) {
      ElMessage.error('获取笔记详情失败')
    } finally {
      loadingDetail.value = false
    }
  } else if (val && !val.submitted) {
    selectedSubmission.value = val
    noteDetail.value = { title: '未提交', content: '该学生尚未提交本作业。' }
    gradingForm.score = 0
    gradingForm.comment = ''
  }
}

const submitGrading = async () => {
  if (!selectedSubmission.value.noteId) {
    ElMessage.warning('该学生未提交笔记，无法批改')
    return
  }
  try {
    const res = await api.put(`api/note-comments/noteId/${selectedSubmission.value.noteId}` ,{
      teacherId: userStore.id || 1,
      noteId: selectedSubmission.value.noteId,
      comment: gradingForm.comment,
      score: gradingForm.score,
      updateTime: new Date().toISOString()
    })

    if (res.code === 200 || res) {
      ElMessage.success('批改提交成功！')
    }
    const res1 = await api.put(`api/note/is_score/${selectedSubmission.value.noteId}` )
    if (res1.code === 200 || res) {
      ElMessage.success('批改状态修改成功！')
      await fetchSubmissions()
    }
  } catch (error) {
    ElMessage.error('提交失败：' + error)
  }
}

const setAsExampleNote = async () => {
  if (!selectedSubmission.value.noteId) return
  
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入设为示例笔记的原因：',
      '设为示例笔记',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入原因...',
        inputPattern: /^.{1,500}$/,
        inputErrorMessage: '原因长度应在1-500个字符之间'
      }
    )
    
    if (reason) {
      exampleReason.value = reason
      isSettingExample.value = true
      
      try {
        const response = await api.post('/api/exemplar-note', {
          noteId: selectedSubmission.value.noteId,
          reason: reason,
          teacherId: userStore.id || 1
        })
        
        if (response.code === 200 || response) {
          ElMessage.success('已成功设为示例笔记！')
        } else {
          ElMessage.error('设为示例笔记失败')
        }
      } catch (error) {
        console.error('设为示例笔记失败:', error)
        ElMessage.error('设为示例笔记失败')
      } finally {
        isSettingExample.value = false
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作取消:', error)
    }
  }
}

const focusComment = () => {
  ElMessage.info('请在右侧表单输入评分和评语')
}

const downloadAttachment = (file) => {
  let url = file.fileUrl
  if (!url.startsWith('http')) {
    url = window.location.origin + url
  }
  window.open(url, '_blank')
}

onMounted(() => {
  fetchSubmissions()
})
</script>

<style scoped>
.note-grading {
  padding: 20px;
}
.mt-20 {
  margin-top: 20px;
}
.grading-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.note-content {
  min-height: 400px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}
.markdown-preview {
  line-height: 1.8;
  font-size: 16px;
}
.sticky-card {
  position: sticky;
  top: 20px;
}
.floating-grade-btn {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  border-radius: 50%;
}
</style>
