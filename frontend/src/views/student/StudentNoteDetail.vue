<template>
  <div class="student-note-detail">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 笔记内容 -->
    <div v-else-if="note.id" class="note-content">
      <el-card>
        <template #header>
          <div class="note-header">
            <div class="note-title-section">
              <div class="title-actions">
                <h1 class="note-title">{{ note.title || '无标题笔记' }}</h1>
                <div class="action-buttons">
                  <el-button 
                    type="primary" 
                    @click="handleEdit"
                    round
                  >
                    <el-icon><Edit /></el-icon>
                    编辑笔记
                  </el-button>
                  <el-button 
                    type="info"
                    @click="$router.back()"
                    round
                  >
                    <el-icon><ArrowLeft /></el-icon>
                    返回
                  </el-button>
                </div>
              </div>
              
              <div class="note-meta">
                <div class="author-info">
                  <el-avatar :size="32" :src="note.authorAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <div class="author-details">
                    <span class="author-name">{{ note.authorName || '匿名用户' }}</span>
                    <span class="publish-date">{{ formatDate(note.createTime) }}</span>
                  </div>
                </div>
                <div class="note-stats">
                  <div class="stat-item">
                    <el-icon><View /></el-icon>
                    <span>{{ viewCount || 0 }} 次阅读</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Star /></el-icon>
                    <span>{{ likeCount || 0 }} 个点赞</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>{{ commentCount || 0 }} 条评论</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="note-tags">
              <el-tag v-if="note.taskId" type="success" size="small">
                <el-icon><Document /></el-icon> 作业笔记
              </el-tag>
              <el-tag v-else type="info" size="small">
                <el-icon><Edit /></el-icon> 自由笔记
              </el-tag>
              <el-tag v-if="note.courseName" type="warning" size="small">
                {{ note.courseName }}
              </el-tag>
              <el-tag v-if="note.isScore === 1" type="success" size="small">
                <el-icon><Check /></el-icon> 已批改
              </el-tag>
              <el-tag v-else-if="note.taskId" type="warning" size="small">
                <el-icon><Clock /></el-icon> 待批改
              </el-tag>
            </div>
          </div>
        </template>

        <!-- 笔记内容区域 -->
        <div class="note-body">
          <v-md-editor 
            :model-value="note.content" 
            mode="preview"
            height="auto"
          ></v-md-editor>
        </div>

        <!-- 附件区域 -->
        <div v-if="attachments && attachments.length > 0" class="attachment-section">
          <el-divider />
          <div class="attachment-header">
            <el-icon><Document /></el-icon>
            <span>附件 ({{ attachments.length }})</span>
          </div>
          <div class="attachment-list">
            <div v-for="(file, index) in attachments" :key="index" class="attachment-item">
              <div class="file-info" @click="downloadAttachment(file)">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ file.fileName }}</span>
              </div>
              <div class="file-actions">
                <el-tag size="small" type="info" v-if="file.fileSize">{{ file.fileSize }}KB</el-tag>
                <el-button link type="primary" size="small" @click="downloadAttachment(file)">
                  <el-icon><Download /></el-icon> 下载
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 笔记信息 -->
        <div class="note-info">
          <el-descriptions title="笔记信息" :column="2" border>
            <el-descriptions-item label="创建时间">{{ formatDate(note.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDate(note.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="标签">{{ note.tag || '无' }}</el-descriptions-item>
            <el-descriptions-item label="摘要">{{ note.summary || '无' }}</el-descriptions-item>
            <el-descriptions-item label="可见性">
              <el-tag v-if="note.vision === 2" type="success" size="small">公开</el-tag>
              <el-tag v-else-if="note.vision === 1" type="info" size="small">课程可见</el-tag>
              <el-tag v-else type="warning" size="small">仅自己可见</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag v-if="note.status === 1" type="success" size="small">已发布</el-tag>
              <el-tag v-else type="info" size="small">草稿</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 互动区域 -->
        <div class="interaction-section">
          <el-button 
            :type="hasLiked ? 'primary' : 'default'" 
            :loading="likeLoading"
            @click="handleLike"
            round
          >
            <el-icon><Star /></el-icon>
            {{ hasLiked ? '已点赞' : '点赞' }}
          </el-button>
          
          <el-button 
            v-if="note.vision !== 2"
            type="success"
            @click="makePublic"
            :loading="publicLoading"
            round
          >
            <el-icon><Share /></el-icon>
            设为公开
          </el-button>
        </div>
      </el-card>

      <!-- 教师批改信息（如果已批改） -->
      <el-card v-if="note.isScore === 1 && noteComments.length > 0" class="comment-section">
        <template #header>
          <h3><el-icon><ChatDotRound /></el-icon> 教师批改信息</h3>
        </template>
        
        <div v-for="comment in noteComments" :key="comment.id" class="teacher-comment">
          <div class="comment-header">
            <div class="teacher-info">
              <el-avatar :size="40" :src="'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <div class="teacher-details">
                <span class="teacher-name">教师 {{ comment.teacherId }}</span>
                <span class="comment-date">{{ formatDate(comment.updateTime) }}</span>
              </div>
            </div>
            <div class="comment-score">
              <el-rate 
                v-model="comment.score" 
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value} 分"
              />
            </div>
          </div>
          
          <div class="comment-content">
            <v-md-editor 
              :model-value="comment.comment" 
              mode="preview"
              height="auto"
            ></v-md-editor>
          </div>
        </div>
      </el-card>

      <!-- 批改状态提示 -->
      <el-card v-else-if="note.taskId && note.isScore === 0" class="grading-status">
        <template #header>
          <h3><el-icon><Clock /></el-icon> 批改状态</h3>
        </template>
        <div class="status-content">
          <el-result
            icon="info"
            title="等待教师批改"
            sub-title="您的作业笔记已提交，请耐心等待教师批改。"
          >
            <template #extra>
              <el-button type="primary" @click="$router.back()">返回列表</el-button>
            </template>
          </el-result>
        </div>
      </el-card>
    </div>

    <!-- 笔记不存在 -->
    <div v-else class="empty-state">
      <el-empty description="笔记不存在或已被删除">
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  View, Star, ChatDotRound, Document, Edit, ArrowLeft, 
  Check, Clock, Share, Download
} from '@element-plus/icons-vue'
import studentApi from '../../api/student'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const note = ref({})
const attachments = ref([])
const loading = ref(false)
const viewCount = ref(0)
const likeCount = ref(0)
const commentCount = ref(0)
const hasLiked = ref(false)
const likeLoading = ref(false)
const publicLoading = ref(false)
const noteComments = ref([])

/**
 * 加载笔记详情
 */
const loadNoteDetail = async () => {
  loading.value = true
  try {
    const noteId = route.params.id
    
    // 加载笔记详情
    const response = await studentApi.getNoteDetail(noteId)
    if (response && response.code === 200) {
      const data = response.data
      note.value = data.note
      attachments.value = data.attachments || []
      
      // 检查是否是自己的笔记
      if (note.value.userId !== userStore.id) {
        ElMessage.warning('这不是您的笔记，正在跳转到公开笔记详情页')
        router.push(`/blog/notes/${noteId}`)
        return
      }
      
      // 加载互动统计
      await loadInteractionStats(noteId)
      
      // 加载批改信息（如果是作业笔记）
      if (note.value.taskId && note.value.isScore === 1) {
        await loadNoteComments(noteId)
      }
    } else {
      ElMessage.error(response?.message || '笔记不存在')
    }
  } catch (error) {
    console.error('加载笔记失败:', error)
    ElMessage.error('加载笔记失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 加载互动统计
 */
const loadInteractionStats = async (noteId) => {
  try {
    // 获取互动统计（包含查看次数、点赞次数和用户互动状态）
    const statsResponse = await studentApi.getNoteInteractionStats(noteId, userStore.id)
    console.log('[StudentNoteDetail.vue] 互动统计响应:', statsResponse)
    
    if (statsResponse) {
      const stats = statsResponse // NoteInteractionController doesn't use ApiResponse yet
      viewCount.value = stats.viewCount || 0
      likeCount.value = stats.likeCount || 0
      hasLiked.value = stats.hasLiked || false
    } else {
      // 如果统一接口失败，尝试分开调用
      await loadInteractionStatsFallback(noteId)
    }
    
    // 记录查看
    await studentApi.viewNote(noteId, userStore.id)
    
  } catch (error) {
    console.error('加载互动统计失败:', error)
    // 降级方案：尝试分开调用
    await loadInteractionStatsFallback(noteId)
  }
}

/**
 * 降级方案：分开加载互动统计
 */
const loadInteractionStatsFallback = async (noteId) => {
  try {
    // 获取查看次数
    const viewResponse = await studentApi.getNoteViewCount(noteId)
    console.log('[StudentNoteDetail.vue] 查看次数响应:', viewResponse)
    if (viewResponse && viewResponse.code === 200) {
      viewCount.value = viewResponse.data || 0
    }
    
    // 获取点赞次数
    const likeResponse = await studentApi.getNoteLikeCount(noteId)
    console.log('[StudentNoteDetail.vue] 点赞次数响应:', likeResponse)
    if (likeResponse && likeResponse.code === 200) {
      likeCount.value = likeResponse.data || 0
    }
    
    // 获取用户互动状态
    const statusResponse = await studentApi.getUserNoteInteractionStatus(noteId, userStore.id)
    console.log('[StudentNoteDetail.vue] 用户互动状态响应:', statusResponse)
    if (statusResponse && statusResponse.code === 200) {
      hasLiked.value = statusResponse.data?.hasLiked || false
    }
  } catch (error) {
    console.error('降级方案加载互动统计失败:', error)
  }
}

/**
 * 加载笔记批改信息
 */
const loadNoteComments = async (noteId) => {
  try {
    const response = await studentApi.getNoteComments(noteId)
    if (response && Array.isArray(response)) {
      noteComments.value = response
      commentCount.value = response.length
    }
  } catch (error) {
    console.error('加载批改信息失败:', error)
  }
}

/**
 * 处理点赞/取消点赞
 */
const handleLike = async () => {
  console.log("[StudentNoteDetail.vue] 开始处理点赞操作")
  likeLoading.value = true
  try {
    const response = await studentApi.toggleNoteLike(route.params.id, userStore.id)
    console.log("[StudentNoteDetail.vue] 点赞响应:", response)
    
    if (response && response.code === 200) {
      // 根据API响应更新状态
      const isLiked = response.data?.type === 1 || response.data?.hasLiked === true
      hasLiked.value = isLiked
      
      // 更新点赞计数
      if (isLiked) {
        likeCount.value += 1
        ElMessage.success('点赞成功')
      } else {
        likeCount.value = Math.max(0, likeCount.value - 1)
        ElMessage.success('已取消点赞')
      }
      
      // 重新加载互动统计以确保数据同步
      await loadInteractionStats(route.params.id)
    } else {
      console.error('[StudentNoteDetail.vue] 点赞响应格式错误:', response)
      ElMessage.error(response?.message || '点赞操作失败')
    }
  } catch (error) {
    console.error('[StudentNoteDetail.vue] 点赞操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    likeLoading.value = false
  }
}

/**
 * 处理编辑笔记
 */
const handleEdit = () => {
  router.push({
    name: 'note-editor',
    params: { id: 'edit' },
    query: {
      noteId: note.value.id,
      taskId: note.value.taskId,
      courseId: note.value.courseId
    }
  })
}

/**
 * 设为公开笔记
 */
const makePublic = async () => {
  publicLoading.value = true
  try {
    // 这里需要调用更新笔记的API，将vision设置为2
    const updateData = {
      title: note.value.title,
      content: note.value.content,
      tag: note.value.tag,
      summary: note.value.summary,
      vision: 2  // 设置为公开
    }
    
    const response = await studentApi.updateNote(note.value.id, updateData)
    if (response && response.code === 200) {
      note.value.vision = 2
      ElMessage.success('笔记已设为公开')
    } else {
      ElMessage.error(response?.message || '设置失败')
    }
  } catch (error) {
    console.error('设为公开失败:', error)
    ElMessage.error('设置失败，请稍后重试')
  } finally {
    publicLoading.value = false
  }
}

/**
 * 下载/预览附件
 */
const downloadAttachment = (file) => {
  let url = file.fileUrl
  if (!url.startsWith('http')) {
    url = window.location.origin + url
  }
  window.open(url, '_blank')
}

/**
 * 格式化日期
 */
const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

onMounted(() => {
  loadNoteDetail()
})
</script>

<style lang="scss" scoped>
.student-note-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  padding: 40px 0;
}

.note-content {
  .note-header {
    margin-bottom: 20px;
    
    .title-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .note-title {
        margin: 0;
        font-size: 28px;
        color: #303133;
        line-height: 1.3;
        flex: 1;
      }
      
      .action-buttons {
        display: flex;
        gap: 12px;
        flex-shrink: 0;
      }
    }
    
    .note-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .author-info {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .author-details {
          display: flex;
          flex-direction: column;
          
          .author-name {
            font-weight: 500;
            color: #303133;
            font-size: 14px;
          }
          
          .publish-date {
            font-size: 12px;
            color: #909399;
            margin-top: 2px;
          }
        }
      }
      
      .note-stats {
        display: flex;
        gap: 24px;
        
        .stat-item {
          display: flex;
          align-items: center;
          gap: 6px;
          color: #606266;
          font-size: 14px;
          
          .el-icon {
            color: #909399;
          }
        }
      }
    }
    
    .note-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
  
  .note-body {
    margin: 24px 0;
    min-height: 300px;
    
    :deep(.v-md-editor) {
      border: none;
      box-shadow: none;
      background-color: transparent;
    }
  }
  
  .attachment-section {
    margin: 24px 0;
    
    .attachment-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }
    
    .attachment-list {
      .attachment-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        background: #f8f9fa;
        border: 1px solid #ebeef5;
        border-radius: 6px;
        margin-bottom: 8px;
        transition: all 0.2s;
        
        &:hover {
          background: #ecf5ff;
          border-color: #b3d8ff;
        }
        
        .file-info {
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          flex: 1;
          overflow: hidden;
          
          .file-name {
            font-size: 14px;
            color: #409eff;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
        
        .file-actions {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-shrink: 0;
        }
      }
    }
  }
  
  .note-info {
    margin: 24px 0;
  }
  
  .interaction-section {
    display: flex;
    gap: 12px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
  }
}

.comment-section,
.grading-status {
  margin-top: 24px;
  
  .teacher-comment {
    padding: 16px;
    border-radius: 8px;
    background-color: #f8f9fa;
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .teacher-info {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .teacher-details {
          display: flex;
          flex-direction: column;
          
          .teacher-name {
            font-weight: 500;
            color: #303133;
            font-size: 14px;
          }
          
          .comment-date {
            font-size: 12px;
            color: #909399;
            margin-top: 2px;
          }
        }
      }
      
      .comment-score {
        display: flex;
        align-items: center;
      }
    }
    
    .comment-content {
      :deep(.v-md-editor) {
        border: none;
        box-shadow: none;
        background-color: transparent;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title-actions {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 16px;
    
    .action-buttons {
      width: 100%;
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
  }
  
  .note-meta {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 16px;
    
    .note-stats {
      width: 100%;
      justify-content: space-between;
    }
  }
  
  .interaction-section {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}
</style>
