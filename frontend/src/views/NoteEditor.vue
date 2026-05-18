<template>
  <div class="editor-container">
    <!-- 顶部导航栏 -->
    <header class="editor-navbar">
      <div class="left-section">
        <el-button link @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="divider"></div>
        <div class="note-info">
          <el-input 
            v-model="noteTitle" 
            placeholder="无标题笔记" 
            class="title-input"
            :input-style="{ fontSize: '1.2rem', fontWeight: 'bold' }"
          />
          <div class="note-type-tags">
            <el-tag v-if="noteType === 'assignment'" type="success" size="small">
              <el-icon><Document /></el-icon> 作业笔记
            </el-tag>
            <el-tag v-else type="info" size="small">
              <el-icon><Edit /></el-icon> 自由笔记
            </el-tag>
            <el-tag v-if="courseInfo" type="warning" size="small">
              {{ courseInfo.name }}
            </el-tag>
          </div>
        </div>
      </div>
      <div class="right-section">
        <el-tag type="info" class="status-tag">自动保存</el-tag>
        <el-button type="primary" round class="publish-btn" @click="handlePublish">
          {{ noteType === 'assignment' ? '提交作业' : '发布笔记' }}
        </el-button>
      </div>
    </header>

    <div class="main-layout">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-overlay">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>正在加载笔记...</span>
      </div>
      
      <!-- 左侧信息栏 -->
      <aside class="editor-sidebar">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>笔记信息</span>
            </div>
          </template>
          
          <div class="info-form">
            <el-form label-position="top">
              <el-form-item label="标签">
                <el-select
                  v-model="noteTagsList"
                  multiple
                  filterable
                  remote
                  reserve-keyword
                  placeholder="请输入标签"
                  :remote-method="remoteSearchTags"
                  :loading="tagsLoading"
                  allow-create
                  default-first-option
                  @change="handleTagsChange"
                >
                  <el-option
                    v-for="item in availableTags"
                    :key="item.id"
                    :label="item.name"
                    :value="item.name"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="摘要">
                <el-input
                  v-model="noteSummary"
                  type="textarea"
                  :rows="4"
                  placeholder="输入笔记摘要..."
                  maxlength="200"
                  show-word-limit
                  resize="none"
                />
              </el-form-item>

              <el-form-item label="附件">
                <el-upload
                  class="attachment-upload"
                  action=""
                  :http-request="handleFileUpload"
                  :show-file-list="false"
                >
                  <el-button size="small" type="primary">点击上传附件</el-button>
                </el-upload>
                <div class="attachment-list">
                  <div v-for="(file, index) in attachments" :key="index" class="attachment-item">
                    <div class="file-info" @click="handleFileOpen(file)">
                      <el-icon><Document /></el-icon>
                      <span class="file-name">{{ file.fileName }}</span>
                    </div>
                    <el-button link type="danger" @click="removeAttachment(index)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-form-item>
              
              <el-form-item label="可见性">
                <el-radio-group v-model="noteVision">
                  <el-radio :label="0">仅自己可见</el-radio>
                  <el-radio :label="1">课程可见</el-radio>
                  <el-radio :label="2">公开</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <!-- 版本控制面板 -->
        <el-card class="version-card" v-if="noteId">
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>版本控制</span>
            </div>
          </template>
          
          <div class="version-info">
            <div class="version-stats">
              <el-statistic title="版本总数" :value="versions.length" />
              <el-button 
                type="primary" 
                size="small" 
                @click="loadVersions" 
                :loading="versionLoading"
                style="margin-top: 10px;"
              >
                <el-icon><Refresh /></el-icon>
                刷新版本
              </el-button>
            </div>
            
            <el-divider />
            
            <div class="version-list">
              <div v-if="versions.length === 0" class="empty-version">
                <el-empty description="暂无版本记录" :image-size="60" />
              </div>
              
              <el-timeline v-else>
                <el-timeline-item
                  v-for="version in versions"
                  :key="version.id"
                  :timestamp="formatDate(version.createdAt)"
                  placement="top"
                  :type="selectedVersion?.id === version.id ? 'primary' : 'info'"
                  @click="selectVersion(version)"
                  style="cursor: pointer;"
                >
                  <el-card shadow="hover" class="version-item">
                    <div class="version-header">
                      <span class="version-title">版本 {{ version.version }}</span>
                      <el-tag size="small" type="info">{{ version.changeSummary || '自动保存' }}</el-tag>
                    </div>
                    <div class="version-content">
                      <div class="version-preview">{{ truncateContent(version.content, 100) }}</div>
                      <div class="version-actions">
                        <el-button size="mini" @click.stop="viewVersion(version)">查看</el-button>
                        <el-button size="mini" type="primary" @click.stop="revertToVersion(version)" v-if="version.version !== 1">回退</el-button>
                      </div>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
        </el-card>
      </aside>
      
      <!-- 主编辑器区域 -->
      <main class="editor-main">
        <v-md-editor 
          v-model="noteContent" 
          height="100%"
          placeholder="开始你的创作..."
          left-toolbar="undo redo clear | h italic strikethrough quote | ul ol table hr | link image code"
          :disabled="loading"
        ></v-md-editor>
      </main>

      <!-- AI 助手悬浮触发按钮 -->
      <div 
        class="ai-trigger-btn" 
        :class="{ 'is-active': isAiOpen }"
        @click="isAiOpen = !isAiOpen"
      >
        <el-icon><MagicStick /></el-icon>
        <span>AI 助手</span>
      </div>

      <!-- 版本控制触发按钮 -->
      <div 
        class="version-trigger-btn" 
        :class="{ 'is-active': isVersionOpen }"
        @click="toggleVersionWindow"
      >
        <el-icon><Clock /></el-icon>
        <span>版本历史</span>
      </div>

      <!-- AI 助手悬浮窗口 -->
      <transition name="slide-fade">
        <aside v-if="isAiOpen" class="ai-float-window">
          <div class="ai-header">
            <div class="title">
              <el-icon><MagicStick /></el-icon>
              <span>AI 智能助手</span>
            </div>
            <el-button link @click="isAiOpen = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          
          <!-- AI 快捷指令 -->
          <div class="ai-actions">
            <el-button-group>
              <el-button size="small" @click="aiAction('polish_note')" :loading="aiLoading">
                <el-icon><Edit /></el-icon>润色
              </el-button>
              <el-button size="small" @click="aiAction('summarize_note')" :loading="aiLoading">
                <el-icon><Document /></el-icon>总结
              </el-button>
              <el-button size="small" @click="aiAction('find_sources')" :loading="aiLoading">
                <el-icon><Search /></el-icon>资料
              </el-button>
            </el-button-group>
          </div>

          <!-- 聊天区域 -->
          <div class="chat-area">
            <div class="chat-messages" ref="chatBox">
              <div 
                v-for="(msg, index) in chatHistory" 
                :key="index" 
                :class="['message-wrapper', msg.role === 'user' ? 'user' : 'ai']"
              >
                <div class="avatar">
                  <el-icon v-if="msg.role === 'ai'"><MagicStick /></el-icon>
                  <el-icon v-else><User /></el-icon>
                </div>
                <div class="message-content">
                  <v-md-editor 
                    v-if="msg.role === 'ai'" 
                    :model-value="msg.content" 
                    mode="preview" 
                    class="markdown-reply"
                    left-toolbar=""
                    right-toolbar=""
                    :disabled="true"
                  ></v-md-editor>
                  <span v-else>{{ msg.content }}</span>
                </div>
              </div>
            </div>
            
            <div class="chat-input-wrapper">
              <el-input
                v-model="userQuestion"
                placeholder="问问 AI..."
                :rows="2"
                type="textarea"
                resize="none"
                @keyup.enter.ctrl="askAI"
              >
                <template #suffix>
                  <el-button link @click="askAI" :loading="aiLoading">
                    <el-icon><Promotion /></el-icon>
                  </el-button>
                </template>
              </el-input>
              <div class="input-tip">Ctrl + Enter 发送</div>
            </div>
          </div>
        </aside>
      </transition>

      <!-- 版本控制悬浮窗口 -->
      <transition name="slide-fade">
        <aside v-if="isVersionOpen" class="version-float-window">
          <div class="version-header">
            <div class="title">
              <el-icon><Clock /></el-icon>
              <span>版本历史管理</span>
            </div>
            <el-button link @click="isVersionOpen = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          
          <!-- 版本比较区域 -->
          <div class="version-compare-section">
            <div class="compare-header">
              <el-icon><Histogram /></el-icon>
              <span>版本比较</span>
            </div>
            <div class="compare-controls">
              <el-select v-model="compareVersion1" placeholder="选择版本1" size="small" style="width: 48%;">
                <el-option
                  v-for="version in versions"
                  :key="version.id"
                  :label="`版本 ${version.version} - ${formatDate(version.createdAt)}`"
                  :value="version.version"
                />
              </el-select>
              <span class="compare-vs">VS</span>
              <el-select v-model="compareVersion2" placeholder="选择版本2" size="small" style="width: 48%;">
                <el-option
                  v-for="version in versions"
                  :key="version.id"
                  :label="`版本 ${version.version} - ${formatDate(version.createdAt)}`"
                  :value="version.version"
                />
              </el-select>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              @click="compareVersions" 
              :disabled="!compareVersion1 || !compareVersion2"
              :loading="isComparing"
              style="margin-top: 10px; width: 100%;"
            >
              <el-icon><Histogram /></el-icon>
              比较版本
            </el-button>
            
            <div v-if="compareResult" class="compare-result">
              <h4>比较结果：</h4>
              <pre>{{ compareResult }}</pre>
            </div>
          </div>

          <!-- 版本详情区域 -->
          <div class="version-detail-section" v-if="selectedVersion">
            <div class="detail-header">
              <h4>版本 {{ selectedVersion.version }} 详情</h4>
              <el-tag type="info">{{ formatDate(selectedVersion.createdAt) }}</el-tag>
            </div>
            <div class="detail-content">
              <div class="detail-item">
                <strong>标题：</strong>
                <span>{{ selectedVersion.title }}</span>
              </div>
              <div class="detail-item">
                <strong>变更摘要：</strong>
                <span>{{ selectedVersion.changeSummary || '无' }}</span>
              </div>
              <div class="detail-actions">
                <el-button type="primary" size="small" @click="previewVersion(selectedVersion)">
                  预览内容
                </el-button>
                <el-button type="warning" size="small" @click="revertToVersion(selectedVersion)" v-if="selectedVersion.version !== 1">
                  回退到此版本
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 版本预览对话框 -->
          <el-dialog v-model="showVersionPreview" title="版本预览" width="70%">
            <div class="version-preview-content">
              <h3>{{ previewVersionData?.title || '无标题' }}</h3>
              <div class="version-meta">
                <el-tag size="small">版本 {{ previewVersionData?.version }}</el-tag>
                <el-tag size="small" type="info">{{ formatDate(previewVersionData?.createdAt) }}</el-tag>
                <el-tag size="small" type="success" v-if="previewVersionData?.changeSummary">{{ previewVersionData.changeSummary }}</el-tag>
              </div>
              <div class="version-content-preview">
                <v-md-editor 
                  :model-value="previewVersionData?.content || ''" 
                  mode="preview" 
                  height="400px"
                ></v-md-editor>
              </div>
            </div>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="showVersionPreview = false">关闭</el-button>
                <el-button type="primary" @click="restoreVersion(previewVersionData)" v-if="previewVersionData?.version !== 1">
                  恢复到此版本
                </el-button>
              </span>
            </template>
          </el-dialog>
        </aside>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, MagicStick, Edit, Document, Search, 
  User, Promotion, Close, Loading, InfoFilled,
  Clock, Histogram, Refresh, Delete
} from '@element-plus/icons-vue'
import api from '../api'
import commonApi from '../api/common'
import studentApi, {getNoteDetail} from '../api/student'
import noteVersionApi from '../api/noteVersion'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const noteTitle = ref('')
const noteContent = ref('# 您好, Markdown!\n\n在这里开始编写您的笔记...\n')
const userQuestion = ref('')
const chatHistory = ref([
  { role: 'ai', content: '您好！今天我能如何帮您优化您的笔记？' }
])
const aiLoading = ref(false)
const chatBox = ref(null)
const isAiOpen = ref(false) // 控制 AI 窗口显示

// 版本控制相关
const isVersionOpen = ref(false) // 控制版本窗口显示
const versions = ref([])
const selectedVersion = ref(null)
const versionLoading = ref(false)
const compareVersion1 = ref(null)
const compareVersion2 = ref(null)
const compareResult = ref('')
const isComparing = ref(false)

// 笔记类型：作业笔记或自由笔记
const noteType = ref('free') // 'assignment' 或 'free'
const assignmentInfo = ref(null)
const courseInfo = ref(null)
const loading = ref(false)

// 笔记数据
const noteTags = ref('')
const noteTagsList = ref([])
const noteSummary = ref('')
const noteVision = ref(0) // 0:仅自己可见, 1:课程可见, 2:公开
const attachments = ref([])
const availableTags = ref([])
const tagsLoading = ref(false)

// 标签相关方法
const remoteSearchTags = async (query) => {
  if (query) {
    tagsLoading.value = true
    try {
      const response = await commonApi.searchTags(query)
      availableTags.value = response
    } catch (error) {
      console.error('搜索标签失败:', error)
    } finally {
      tagsLoading.value = false
    }
  } else {
    availableTags.value = []
  }
}

const handleTagsChange = (val) => {
  noteTags.value = val.join(',')
}

// 附件相关方法
const handleFileUpload = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await commonApi.uploadFile(formData)
    if (response.code === 200) {
      attachments.value.push({
        fileName: response.data.fileName,
        fileUrl: response.data.fileUrl,
        fileSize: response.data.fileSize
      })
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传附件失败:', error)
    ElMessage.error('上传失败')
  }
}

const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
}

const handleFileOpen = (file) => {
  // fileUrl already contains /api/files/download/xxx from upload response
  let url = file.fileUrl
  if (!url.startsWith('http')) {
    url = window.location.origin + url
  }
  window.open(url, '_blank')
}

// 计算属性：从路由参数获取参数
const noteId = computed(() => route.query.noteId)
const courseId = computed(() => route.query.courseId)
const taskId = computed(() => route.query.taskId)
const assignmentTitle = computed(() => route.query.assignmentTitle)

/**
 * 初始化笔记信息
 */
const initNoteInfo = async () => {
  // 如果有 noteId，加载现有笔记
  console.log("[NoteEditor.vue]noteId:"+noteId.value)
  if (noteId.value) {
    await loadExistingNote()
  } else {
    // 如果没有 noteId，根据 taskId 判断笔记类型
    if (taskId.value) {
      noteType.value = 'assignment'
      assignmentInfo.value = {
        taskId: taskId.value,
        title: assignmentTitle.value || '未知作业'
      }
    } else {
      noteType.value = 'free'
    }
  }
  
  // 获取课程信息
  if (courseId.value) {
    await fetchCourseInfo(courseId.value)
  }
}

/**
 * 加载现有笔记
 */
const loadExistingNote = async () => {
  loading.value = true
  console.log("[NoteEditor.loadExistingNote]noteId.value:"+noteId.value)
  try {
    const response = await studentApi.getNoteDetail(noteId.value)
    
    if (response && response.code === 200) {
      const data = response.data
      const note = data.note
      
      // 填充笔记数据
      noteTitle.value = note.title || '无标题笔记'
      noteContent.value = note.content || '# 您好, Markdown!\n\n在这里开始编写您的笔记...\n'
      noteSummary.value = note.summary || ''
      noteVision.value = note.vision || 0
      
      // 处理标签
      if (note.tag) {
        noteTags.value = note.tag
        noteTagsList.value = note.tag.split(',').map(t => t.trim())
        // 初始化可选标签列表，防止显示ID
        availableTags.value = noteTagsList.value.map(name => ({ name, id: name }))
      }
      
      // 处理附件
      attachments.value = data.attachments || []
      
      // 根据 taskId 判断笔记类型
      if (note.taskId) {
        noteType.value = 'assignment'
        assignmentInfo.value = {
          taskId: note.taskId,
          title: assignmentTitle.value || '未知作业'
        }
      } else {
        noteType.value = 'free'
      }
      
      ElMessage.success('笔记加载成功')
    } else {
      ElMessage.warning('未找到笔记数据，使用默认模板')
      // 设置默认笔记类型
      if (taskId.value) {
        noteType.value = 'assignment'
        assignmentInfo.value = {
          taskId: taskId.value,
          title: assignmentTitle.value || '未知作业'
        }
      }
    }
  } catch (error) {
    console.error('加载笔记失败:', error)
    ElMessage.error('加载笔记失败，请稍后重试')
    // 设置默认笔记类型
    if (taskId.value) {
      noteType.value = 'assignment'
      assignmentInfo.value = {
        taskId: taskId.value,
        title: assignmentTitle.value || '未知作业'
      }
    }
  } finally {
    loading.value = false
  }
}

/**
 * 获取课程信息
 */
const fetchCourseInfo = async (courseId) => {
  try {
    const response = await studentApi.getCourseDetail(courseId)
    if (response.id) {
      courseInfo.value = {
        id: response.id,
        name: response.className || '未知课程',
        description: response.describe || ''
      }
    }
  } catch (error) {
    console.error('获取课程信息失败:', error)
  }
}

/**
 * 发布笔记
 * 根据是否有noteId决定是更新还是新增
 */
const handlePublish = async () => {
  if (!noteTitle.value) {
    ElMessage.error('请输入笔记标题')
    return
  }

  try {
    const noteData = {
      title: noteTitle.value,
      content: noteContent.value,
      tag: noteTags.value,
      summary: noteSummary.value || '',
      vision: noteVision.value,
      attachments: attachments.value
    }

    let response
    
    if (noteId.value) {
      // 如果有noteId，则更新笔记
      console.log('更新笔记，noteId:', noteId.value)
      response = await studentApi.updateNote(noteId.value, noteData)
      ElMessage.success('笔记更新成功！')
    } else if (noteType.value === 'assignment' && taskId.value) {
      // 如果没有noteId但是有taskId，创建作业笔记
      console.log('创建作业笔记，taskId:', taskId.value)
      const createNoteData = {
        ...noteData,
        userId: userStore.id || -1
      }
      response = await studentApi.createAssignmentNote(taskId.value, createNoteData)
      ElMessage.success('作业笔记创建成功！')
    } else {
      // 创建自由笔记
      console.log('创建自由笔记')
      const createNoteData = {
        ...noteData,
        userId: userStore.id || -1
      }
      response = await studentApi.createFreeNote(createNoteData)
      ElMessage.success('自由笔记创建成功！')
    }
    
    // 发布成功后返回上一页
    setTimeout(() => {
      router.back()
    }, 1500)
    
  } catch (error) {
    console.error('发布笔记失败:', error)
    ElMessage.error('发布笔记失败，请稍后重试')
  }
}

const aiAction = async (type) => {
  if (type === 'summarize_note') {
    aiLoading.value = true
    try {
      const response = await api.post('/api/llm/summarize-note', noteContent.value, {
        headers: { 'Content-Type': 'text/plain' }
      })
      chatHistory.value.push({
        role: 'ai',
        content: `### 笔记总结\n\n${response}`
      })
    } catch (error) {
      ElMessage.error('总结失败')
    } finally {
      aiLoading.value = false
    }
    return
  }

  aiLoading.value = true
  try {
    const response = await api.post('/api/llm/custom-prompt', {
      promptType: type,
      dataId: noteId.value
    })
    chatHistory.value.push({
      role: 'ai',
      content: response
    })
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    aiLoading.value = false
  }
}

const askAI = async () => {
  if (!userQuestion.value.trim()) return
  
  const question = userQuestion.value
  chatHistory.value.push({ role: 'user', content: question })
  userQuestion.value = ''
  aiLoading.value = true
  
  try {
    const response = await api.post('/api/llm/ask-note', {
      noteContent: noteContent.value,
      userQuestion: question
    })
    chatHistory.value.push({ role: 'ai', content: response })
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    aiLoading.value = false
    nextTick(() => {
      if (chatBox.value) {
        chatBox.value.scrollTop = chatBox.value.scrollHeight
      }
    })
  }
}

// 版本控制相关方法
const showVersionPreview = ref(false)
const previewVersionData = ref(null)

/**
 * 加载笔记版本列表
 */
const loadVersions = async () => {
  if (!noteId.value) return
  
  versionLoading.value = true
  try {
    const response = await noteVersionApi.getNoteVersions(noteId.value)
    if (response && response.code === 200) {
      versions.value = response.data || []
      if (versions.value.length > 0) {
        selectedVersion.value = versions.value[0]
      }
    } else {
      versions.value = []
    }
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    versionLoading.value = false
  }
}

/**
 * 选择版本
 */
const selectVersion = (version) => {
  selectedVersion.value = version
}

/**
 * 查看版本详情
 */
const viewVersion = (version) => {
  selectedVersion.value = version
}

/**
 * 预览版本内容
 */
const previewVersion = (version) => {
  previewVersionData.value = version
  showVersionPreview.value = true
}

/**
 * 恢复到此版本
 */
const restoreVersion = async (version) => {
  try {
    const confirm = await ElMessageBox.confirm(
      `确定要恢复到版本 ${version.version} 吗？当前内容将被覆盖。`,
      '确认恢复',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirm) {
      const response = await noteVersionApi.revertToVersion(
        version.noteId, 
        version.version, 
        userStore.id || -1
      )
      
      if (response && response.code === 200) {
        ElMessage.success('版本恢复成功！')
        // 重新加载笔记内容
        await loadExistingNote()
        // 重新加载版本列表
        await loadVersions()
        showVersionPreview.value = false
      }
    }
  } catch (error) {
    console.error('版本恢复失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('版本恢复失败')
    }
  }
}

/**
 * 回退到指定版本
 */
const revertToVersion = async (version) => {
  await restoreVersion(version)
}

/**
 * 比较两个版本
 */
const compareVersions = async () => {
  if (!compareVersion1.value || !compareVersion2.value) {
    ElMessage.warning('请选择要比较的两个版本')
    return
  }
  
  if (compareVersion1.value === compareVersion2.value) {
    ElMessage.warning('请选择两个不同的版本进行比较')
    return
  }
  
  isComparing.value = true
  try {
    const response = await noteVersionApi.compareVersions(
      noteId.value, 
      compareVersion1.value, 
      compareVersion2.value
    )
    
    if (response && response.code === 200) {
      compareResult.value = response.data
    }
  } catch (error) {
    console.error('版本比较失败:', error)
    ElMessage.error('版本比较失败')
  } finally {
    isComparing.value = false
  }
}

/**
 * 切换版本窗口显示
 */
const toggleVersionWindow = () => {
  isVersionOpen.value = !isVersionOpen.value
  if (isVersionOpen.value && noteId.value) {
    loadVersions()
  }
}

/**
 * 格式化日期
 */
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 截断内容
 */
const truncateContent = (content, length = 100) => {
  if (!content) return ''
  if (content.length <= length) return content
  return content.substring(0, length) + '...'
}

// 组件挂载时初始化
onMounted(() => {
  initNoteInfo()
})

// 监听笔记ID变化，加载版本
watch(noteId, (newVal) => {
  if (newVal) {
    loadVersions()
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.editor-navbar {
  height: 80px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background-color: #fff;
  z-index: 100;

  .left-section {
    display: flex;
    align-items: center;
    flex: 1;

    .divider {
      width: 1px;
      height: 20px;
      background-color: #e0e0e0;
      margin: 0 16px;
    }

    .note-info {
      display: flex;
      flex-direction: column;
      flex: 1;
      gap: 8px;
      
      .title-input {
        :deep(.el-input__wrapper) {
          box-shadow: none !important;
          background: transparent;
          padding: 0;
        }
      }
      
      .note-type-tags {
        display: flex;
        gap: 8px;
        align-items: center;
        
        .el-tag {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .right-section {
    display: flex;
    align-items: center;
    gap: 16px;

    .status-tag {
      background-color: #f5f7fa;
      border: none;
      color: #909399;
    }

    .publish-btn {
      padding: 0 24px;
    }
  }
}

.main-layout {
  flex: 1;
  display: flex;
  position: relative; // 为 AI 悬浮窗提供定位基准
  overflow: hidden;
}

.editor-sidebar {
  width: 280px;
  padding: 20px;
  border-right: 1px solid #f0f0f0;
  background-color: #fafafa;
  overflow-y: auto;

  .attachment-list {
    margin-top: 10px;
    
    .attachment-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px;
      background: #fff;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      margin-bottom: 8px;
      
      .file-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        flex: 1;
        overflow: hidden;
        
        .file-name {
          font-size: 13px;
          color: #409eff;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
  
  .info-card {
    :deep(.el-card__header) {
      padding: 16px 20px;
      background-color: #f5f7fa;
      border-bottom: 1px solid #ebeef5;
    }
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #303133;
    }
    
    .info-form {
      .el-form-item {
        margin-bottom: 20px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .el-form-item__label {
          font-weight: 500;
          color: #606266;
          margin-bottom: 8px;
        }
      }
    }
  }
}

.editor-main {
  flex: 1;
  
  :deep(.v-md-editor) {
    border: none;
    box-shadow: none;
  }
}

/* AI 触发按钮 */
.ai-trigger-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background-color: #409eff;
  color: white;
  padding: 12px 8px;
  border-radius: 12px 0 0 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: -2px 0 10px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  z-index: 101;

  span {
    writing-mode: vertical-rl;
    font-size: 13px;
    letter-spacing: 2px;
  }

  &:hover {
    padding-right: 12px;
    background-color: #66b1ff;
  }

  &.is-active {
    right: 360px; // 窗口打开时跟随移动
    border-radius: 12px;
    transform: translateY(-50%) translateX(50%); // 微调位置
    opacity: 0; // 或者隐藏
    pointer-events: none;
  }
}

/* AI 悬浮窗口 */
.ai-float-window {
  position: absolute;
  right: 20px;
  top: 20px;
  bottom: 20px;
  width: 360px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  z-index: 102;
  border: 1px solid rgba(235, 238, 245, 0.8);
  overflow: hidden;

  .ai-header {
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;

    .title {
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #303133;
    }
  }

  .ai-actions {
    padding: 12px 20px;
    border-bottom: 1px solid #f0f0f0;
    
    .el-button-group {
      width: 100%;
      display: flex;
      
      .el-button {
        flex: 1;
        padding: 8px 0;
      }
    }
  }

  .chat-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    padding: 16px;
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding-bottom: 16px;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: #e4e7ed;
      border-radius: 2px;
    }

    .message-wrapper {
      display: flex;
      gap: 10px;
      margin-bottom: 16px;
      
      .avatar {
        width: 28px;
        height: 28px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        font-size: 14px;
      }

      .message-content {
        padding: 8px 12px;
        border-radius: 10px;
        font-size: 13.5px;
        line-height: 1.5;
        max-width: calc(100% - 38px);
        word-wrap: break-word;
        overflow-wrap: break-word;
        box-sizing: border-box;
      }

      &.ai {
        width: 100%;
        .avatar {
          background-color: #ecf5ff;
          color: #409eff;
        }
        .message-content {
          background-color: #f4f7f9;
          border: 1px solid #ebeef5;
          color: #606266;
          padding: 0;
          overflow: hidden;

          .markdown-reply {
            :deep(.v-md-editor) {
              border: none;
              box-shadow: none;
              background-color: transparent;
            }
            :deep(.v-md-editor__main) {
              padding: 8px 12px;
              background-color: transparent;
            }
            :deep(.github-markdown-body) {
              padding: 0 !important;
              font-size: 13.5px;
              background-color: transparent;
            }
          }
        }
      }

      &.user {
        flex-direction: row-reverse;
        width: 100%;
        .avatar {
          background-color: #f2f6fc;
          color: #409eff;
        }
        .message-content {
          background-color: #409eff;
          color: #fff;
          white-space: pre-wrap;
        }
      }
    }
  }

  .chat-input-wrapper {
    background-color: #fff;
    border-radius: 12px;
    padding: 10px;
    border: 1px solid #e4e7ed;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.03);

    :deep(.el-textarea__inner) {
      border: none;
      padding: 0;
      box-shadow: none !important;
      font-size: 13px;
    }

    .input-tip {
      text-align: right;
      font-size: 11px;
      color: #c0c4cc;
      margin-top: 4px;
    }
  }
}

/* 版本控制触发按钮 */
.version-trigger-btn {
  position: absolute;
  right: 0;
  top: 60%;
  transform: translateY(-50%);
  background-color: #67c23a;
  color: white;
  padding: 12px 8px;
  border-radius: 12px 0 0 12px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: -2px 0 10px rgba(103, 194, 58, 0.3);
  transition: all 0.3s ease;
  z-index: 101;

  span {
    writing-mode: vertical-rl;
    font-size: 13px;
    letter-spacing: 2px;
  }

  &:hover {
    padding-right: 12px;
    background-color: #85ce61;
  }

  &.is-active {
    right: 360px;
    border-radius: 12px;
    transform: translateY(-50%) translateX(50%);
    opacity: 0;
    pointer-events: none;
  }
}

/* 版本控制悬浮窗口 */
.version-float-window {
  position: absolute;
  right: 20px;
  top: 20px;
  bottom: 20px;
  width: 400px;
  background-color: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  z-index: 102;
  border: 1px solid rgba(235, 238, 245, 0.8);
  overflow: hidden;

  .version-header {
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;
    background-color: #f5f7fa;

    .title {
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #303133;
    }
  }

  .version-compare-section {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .compare-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      font-weight: 500;
      color: #606266;
    }
    
    .compare-controls {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      
      .compare-vs {
        font-weight: bold;
        color: #909399;
        width: 4%;
        text-align: center;
      }
    }
    
    .compare-result {
      margin-top: 16px;
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 8px;
      border: 1px solid #ebeef5;
      
      h4 {
        margin-top: 0;
        margin-bottom: 8px;
        color: #303133;
      }
      
      pre {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
        font-size: 12px;
        line-height: 1.4;
        color: #606266;
      }
    }
  }

  .version-detail-section {
    padding: 16px;
    flex: 1;
    overflow-y: auto;
    
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h4 {
        margin: 0;
        color: #303133;
      }
    }
    
    .detail-content {
      .detail-item {
        margin-bottom: 12px;
        
        strong {
          color: #606266;
          margin-right: 8px;
        }
        
        span {
          color: #303133;
        }
      }
      
      .detail-actions {
        margin-top: 20px;
        display: flex;
        gap: 8px;
      }
    }
  }
}

/* 版本控制卡片样式 */
.version-card {
  margin-top: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #303133;
  }
  
  .version-info {
    .version-stats {
      text-align: center;
      padding: 10px 0;
    }
    
    .version-list {
      max-height: 300px;
      overflow-y: auto;
      
      .empty-version {
        padding: 20px 0;
        text-align: center;
        color: #909399;
      }
      
      .version-item {
        margin: 8px 0;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .version-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .version-title {
            font-weight: 500;
            color: #303133;
          }
        }
        
        .version-content {
          .version-preview {
            font-size: 12px;
            color: #606266;
            margin-bottom: 8px;
            line-height: 1.4;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }
          
          .version-actions {
            display: flex;
            justify-content: flex-end;
            gap: 4px;
          }
        }
      }
    }
  }
}

/* 版本预览对话框 */
.version-preview-content {
  .version-meta {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    flex-wrap: wrap;
  }
  
  .version-content-preview {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    overflow: hidden;
  }
}

/* 动画效果 */
.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.slide-fade-enter-from, .slide-fade-leave-to {
  transform: translateX(100%) scale(0.9);
  opacity: 0;
}
</style>
