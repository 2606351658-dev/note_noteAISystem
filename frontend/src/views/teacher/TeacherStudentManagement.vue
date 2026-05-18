<template>
  <div class="student-management">
    <el-page-header @back="router.go(-1)" title="学生管理" />
    
    <el-row :gutter="20" class="mt-20">
      <!-- 左侧：课程选择 -->
      <el-col :span="5">
        <el-card header="课程列表">
          <el-input
            v-model="courseSearch"
            placeholder="搜索课程"
            clearable
            class="mb-20"
          />
          <el-tree
            :data="courseTree"
            :props="courseTreeProps"
            node-key="id"
            @node-click="handleCourseSelect"
            highlight-current
          />
        </el-card>
      </el-col>

      <!-- 中间：学生列表 -->
      <el-col :span="13">
        <el-card v-loading="loadingStudents">
          <template #header>
            <div class="card-header">
              <span>学生列表 - {{ selectedCourse?.className || '未选择课程' }}</span>
              <span v-if="selectedCourse" class="student-count">共 {{ students.length }} 名学生</span>
            </div>
          </template>

          <el-table :data="students" style="width: 100%">
            <el-table-column label="学生信息" min-width="180">
              <template #default="scope">
                <div class="student-info-cell">
                  <el-avatar :size="32" :src="getFullUrl(scope.row.avatar)" />
                  <div class="ml-10">
                    <div class="name">{{ scope.row.realName || scope.row.username }}</div>
                    <div class="account text-gray">{{ scope.row.username }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column prop="unfinishedCount" label="未完成作业数" width="120" align="center" sortable />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="viewStudentDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：作业提交详情 -->
      <el-col :span="6">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>作业提交详情</span>
            </div>
          </template>
          
          <!-- 作业下拉选择 -->
          <el-select
            v-model="selectedTaskId"
            placeholder="请选择作业"
            style="width: 100%"
            @change="handleTaskChange"
            :disabled="courseTasks.length === 0"
          >
            <el-option
              v-for="task in courseTasks"
              :key="task.id"
              :label="task.title"
              :value="task.taskId || task.id"
            />
          </el-select>

          <el-divider />

          <!-- 提交统计 -->
          <div v-if="submissionStats.total > 0" class="summary-stats">
            <div class="stat-item">
              <span class="stat-label">总学生</span>
              <span class="stat-value">{{ submissionStats.total }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已提交</span>
              <span class="stat-value" style="color: #67C23A;">{{ submissionStats.submitted }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">未提交</span>
              <span class="stat-value" style="color: #F56C6C;">{{ submissionStats.unsubmitted }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已批改</span>
              <span class="stat-value" style="color: #409EFF;">{{ submissionStats.graded }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">平均分</span>
              <span class="stat-value" style="color: #E6A23C;">{{ submissionStats.avgScore }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">提交率</span>
              <span class="stat-value">{{ submissionStats.submissionRate }}%</span>
            </div>
          </div>

          <el-divider />

          <!-- 提交明细表格 -->
          <div v-if="selectedTaskId && submissions.length > 0" class="submission-table">
            <el-table :data="submissions" size="small" max-height="400" style="width: 100%">
              <el-table-column label="学生" min-width="100">
                <template #default="scope">
                  {{ scope.row.realName || scope.row.userName }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="70">
                <template #default="scope">
                  <el-tag v-if="scope.row.submitted" size="small" type="success">已提交</el-tag>
                  <el-tag v-else size="small" type="danger">未提交</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="分数" width="60" align="center">
                <template #default="scope">
                  <span v-if="scope.row.isScore === 1 && scope.row.score != null">
                    {{ scope.row.score }}
                  </span>
                  <span v-else class="text-gray">-</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else-if="selectedTaskId" description="暂无提交数据" />
          <el-empty v-else description="请先选择作业" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生详情弹窗 -->
    <el-dialog v-model="showStudentDetail" :title="'学生详情 - ' + (selectedStudent?.realName || selectedStudent?.username)" width="40%">
      <div v-if="selectedStudent" class="student-detail">
        <div class="detail-avatar">
          <el-avatar :size="80" :src="getFullUrl(selectedStudent.avatar)" />
        </div>
        <el-descriptions :column="2" border class="mt-20">
          <el-descriptions-item label="用户名">{{ selectedStudent.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ selectedStudent.realName || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedStudent.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="未完成作业数">{{ selectedStudent.unfinishedCount }}</el-descriptions-item>
          <el-descriptions-item label="加入时间" :span="2">{{ formatDateTime(selectedStudent.joinTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showStudentDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'
import api from '../../api'

const router = useRouter()
const userStore = useUserStore()

// 数据
const courseSearch = ref('')
const courseTree = ref([])
const courseTreeProps = {
  children: 'children',
  label: 'className'
}
const selectedCourse = ref(null)
const students = ref([])
const loadingStudents = ref(false)
const showStudentDetail = ref(false)
const selectedStudent = ref(null)
const courseTasks = ref([])

// 作业选择与提交状态
const selectedTaskId = ref(null)
const submissions = ref([])
const submissionStats = computed(() => {
  const total = submissions.value.length
  const submitted = submissions.value.filter(s => s.submitted).length
  const unsubmitted = total - submitted
  const graded = submissions.value.filter(s => s.isScore === 1).length
  const scoredSubmissions = submissions.value.filter(s => s.score != null && s.isScore === 1)
  const avgScore = scoredSubmissions.length > 0
    ? (scoredSubmissions.reduce((sum, s) => sum + Number(s.score), 0) / scoredSubmissions.length).toFixed(1)
    : '-'
  const submissionRate = total > 0 ? ((submitted / total) * 100).toFixed(1) : 0
  
  return { total, submitted, unsubmitted, graded, avgScore, submissionRate }
})

const getFullUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return api.defaults.baseURL + url
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 初始化课程列表
const initCourseTree = async () => {
  try {
    const res = await api.get('/api/class/teacher/' + userStore.id)
    if (res.code === 200) {
      courseTree.value = res.data
    }
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

// 处理课程选择
const handleCourseSelect = async (node) => {
  selectedCourse.value = node
  selectedTaskId.value = null
  submissions.value = []
  students.value = []
  courseTasks.value = []
  
  await Promise.all([
    loadStudents(node.id),
    loadCourseTasks(node.id)
  ])
}

// 加载课程作业列表
const loadCourseTasks = async (courseId) => {
  try {
    const res = await api.get(`api/task/byClassIdWithStats/${courseId}`)
    if (res.code === 200) {
      courseTasks.value = res.data
    }
  } catch (error) {
    console.error('获取作业列表失败:', error)
  }
}

// 加载学生数据
const loadStudents = async (courseId) => {
  loadingStudents.value = true
  try {
    const res = await api.get(`/api/teacher/courses/${courseId}/details`)
    if (res.code === 200) {
      students.value = res.data.students || []
    }
  } catch (error) {
    console.error('加载学生数据失败:', error)
    ElMessage.error('加载学生数据失败')
  } finally {
    loadingStudents.value = false
  }
}

// 选择作业后加载提交状态
const handleTaskChange = async (taskId) => {
  if (!taskId) {
    submissions.value = []
    return
  }
  try {
    const res = await api.get(`/api/teacher/courses/tasks/${taskId}/submissions-status`)
    if (res.code === 200) {
      submissions.value = res.data || []
    }
  } catch (error) {
    console.error('获取提交状态失败:', error)
    ElMessage.error('获取提交状态失败')
    submissions.value = []
  }
}

// 查看学生详情
const viewStudentDetail = (student) => {
  selectedStudent.value = student
  showStudentDetail.value = true
}

onMounted(() => {
  initCourseTree()
})
</script>

<style scoped>
.student-management {
  padding: 20px;
}
.mt-20 {
  margin-top: 20px;
}
.mb-20 {
  margin-bottom: 20px;
}
.ml-10 {
  margin-left: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.student-count {
  font-size: 13px;
  color: #909399;
}
.student-info-cell {
  display: flex;
  align-items: center;
}
.name {
  font-weight: bold;
  font-size: 14px;
}
.account {
  font-size: 12px;
}
.text-gray {
  color: #909399;
}
.summary-stats {
  padding: 0 5px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 16px;
  font-weight: 600;
}
.submission-table {
  max-height: 420px;
  overflow-y: auto;
}
.student-detail {
  padding: 10px;
}
.detail-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}
</style>
