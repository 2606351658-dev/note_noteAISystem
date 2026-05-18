<template>
  <div class="teacher-courses">
    <el-row :gutter="20">
      <el-col :span="18">
        <h2>开设课程管理</h2>
      </el-col>
      <el-col :span="6" class="text-right">
        <el-button type="primary" @click="showCreateDialog = true">创建新课程</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-table :data="courses" style="width: 100%">
          <el-table-column prop="id" label="课程编号" width="80" />
          <el-table-column prop="className" label="课程名称" />
          <el-table-column prop="studentCount" label="学生人数" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="250">
            <template #default="scope">
              <el-button size="small" @click="viewDetails(scope.row)">查看详情</el-button>
              <el-button size="small" type="primary" @click="manageStudents(scope.row)">学生管理</el-button>
              <el-popconfirm title="确定删除该课程吗？" @confirm="deleteCourse(scope.row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- 课程详情抽屉 -->
    <el-drawer v-model="showDetails" :title="selectedCourse.className + ' - 课程详情'" size="50%">
      <template #extra>
        <el-button type="success" @click="exportCourseData">导出数据</el-button>
      </template>
      <div v-if="selectedCourse.id">
        <!-- 课程统计卡片 -->
        <el-row :gutter="20" class="mb-20">
          <el-col :span="4">
            <el-statistic title="学生总数" :value="courseStats.totalStudents" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="活跃学生" :value="courseStats.activeStudents" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="作业总数" :value="courseStats.totalAssignments" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="已提交作业" :value="courseStats.submittedAssignments" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="平均分数" :value="courseStats.averageScore" :precision="1" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="完成率" :value="courseStats.completionRate" suffix="%" />
          </el-col>
        </el-row>

        <!-- 可视化图表 -->
        <el-row :gutter="20" class="mb-20">
          <el-col :span="8">
            <el-card header="学生状态分布">
              <div ref="studentChart" style="height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card header="作业完成情况">
              <div ref="assignmentChart" style="height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card header="分数分布">
              <div ref="scoreChart" style="height: 300px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- AI总结按钮 -->
        <el-row class="mb-20">
          <el-col :span="24">
            <el-button 
              type="primary" 
              @click="generateAiSummary" 
              :loading="generatingSummary"
              style="width: 100%;"
            >
              <el-icon><MagicStick /></el-icon>
              AI课程总结
            </el-button>
          </el-col>
        </el-row>

        <!-- 课程小结展示框 -->
        <el-row class="mb-20">
          <el-col :span="24">
            <el-card header="最新课程小结" shadow="never">
              <el-input
                v-model="latestSummary"
                type="textarea"
                :rows="6"
                readonly
                placeholder="点击“AI课程总结”按钮生成课程总结..."
              />
            </el-card>
          </el-col>
        </el-row>

        <h3>课程作业概览</h3>
        <el-row class="mb-20">
          <el-button type="success" @click="openCreateTaskDialog">新建作业</el-button>
          <el-button type="primary" @click="manageStudents(selectedCourse)" style="margin-left: 10px;">
            学生管理
          </el-button>
        </el-row>
        <el-table :data="courseTasks" style="width: 100%">
          <el-table-column prop="title" label="作业标题" />
          <el-table-column prop="deadline" label="截止日期" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" type="primary" @click="goToGrading(scope.row)">批改作业</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 新建作业对话框 -->
    <el-dialog v-model="showCreateTaskDialog" title="新建作业" width="40%">
      <el-form :model="taskForm" label-width="120px">
        <el-form-item label="作业标题">
          <el-input v-model="taskForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="作业描述">
          <el-input v-model="taskForm.content" type="textarea" :rows="3" placeholder="请输入作业描述" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="taskForm.deadline" type="datetime" placeholder="选择截止日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="必须上传附件">
          <el-radio-group v-model="taskForm.attachmentRequired">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            action="/api/upload"
            :limit="1"
            :on-success="handleUploadSuccess"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateTaskDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateTask">确认发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- AI总结对话框 -->
    <el-dialog v-model="showAiSummary" title="AI课程总结" width="70%">
      <div class="ai-summary-content">
        <v-md-editor 
          :model-value="aiSummaryContent" 
          mode="preview" 
          height="400px"
        ></v-md-editor>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAiSummary = false">关闭</el-button>
          <el-button type="primary" @click="copySummary">复制内容</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建课程对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建新课程" width="40%">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="课程名称">
          <el-input v-model="createForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateCourse">确认创建</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog v-model="showStudentDialog" :title="selectedCourse.className + ' - 学生管理'" width="60%">
      <el-table :data="courseStudents" style="width: 100%">
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="getFullUrl(scope.row.avatar)" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="joinTime" label="加入时间" width="180" />
      </el-table>
      <template #footer>
        <el-button @click="showStudentDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * TeacherCourseManagement.vue
 * 教师端课程管理界面。
 *
 * 学习要点：
 * 1. el-popconfirm: 气泡确认框，用于删除确认。
 * 2. el-table: 数据表格展示课程。
 * 3. el-checkbox-group: 多选框组。
 */
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import api from '../../api'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

const getFullUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return api.defaults.baseURL + url
}

const showCreateDialog = ref(false)
const showDetails = ref(false)
const showCreateTaskDialog = ref(false)
const selectedCourse = ref({})
const courseTasks = ref([])
const showAiSummary = ref(false)
const aiSummaryContent = ref('')
const generatingSummary = ref(false)
const latestSummary = ref('')

// 图表引用
const studentChart = ref(null)
const assignmentChart = ref(null)
const scoreChart = ref(null)
let studentChartInstance = null
let assignmentChartInstance = null
let scoreChartInstance = null

// 统计数据
const courseStats = ref({
  totalStudents: 0,
  activeStudents: 0,
  totalAssignments: 0,
  submittedAssignments: 0,
  averageScore: 0,
  completionRate: 0
})

const createForm = reactive({
  name: '',
  description: '',
})

const taskForm = reactive({
  title: '',
  content: '',
  deadline: '',
  attachmentRequired: false,
  att: ''
})

// 模拟假数据：教师开设的课程
const courses = ref([
  { id: 1, className: '移动端开发技术',teacherId:1, studentCount: 45, createTime: '2023-09-01' },
  { id: 2, className: '人工智能导论',teacherId:1, studentCount: 38, createTime: '2023-09-02' }
])

//自动拉取课程
const fetchTeacherCourses = async () => {
  try {
    const res = await api.get(`api/class/withStudentCount/${userStore.id || 1}`);
    if (res.code === 200) {
      courses.value = res.data;
    }
  } catch (error) {
    console.error('获取教师课程列表失败', error);
  }
}

const courseStudents = ref([])
const showStudentDialog = ref(false)

const viewDetails = async (course) => {
  selectedCourse.value = course
  showDetails.value = true
  
  try {
    // 1. 获取课程统计和学生列表
    const res = await api.get(`/api/teacher/courses/${course.id}/details`)
    if (res.code === 200) {
      courseStats.value = res.data.stats
      courseStudents.value = res.data.students
    }

    // 2. 获取作业列表
    const taskRes = await api.get(`api/task/byClassIdWithStats/${course.id}`)
    if (taskRes.code === 200) {
      courseTasks.value = taskRes.data
    }

    // 3. 获取已有的课程小结
    const summaryRes = await api.get(`/api/assignment-summary/${course.id}`)
    if (summaryRes.code === 200 && summaryRes.data) {
      latestSummary.value = summaryRes.data.summary
    } else {
      latestSummary.value = ''
    }

    // 初始化图表
    nextTick(() => {
      initCharts()
    })
  } catch (error) {
    console.error('获取课程详情失败:', error)
    ElMessage.error('获取课程详情失败')
  }
}


const exportCourseData = () => {
  const data = {
    courseName: selectedCourse.value.className,
    stats: courseStats.value,
    students: courseStudents.value,
    tasks: courseTasks.value,
    summary: latestSummary.value
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${selectedCourse.value.className}_课程数据.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 初始化图表
const initCharts = () => {
  initStudentChart()
  initAssignmentChart()
  initScoreChart()
}

// 初始化学生分布图表
const initStudentChart = () => {
  if (!studentChart.value) return
  
  if (studentChartInstance) {
    studentChartInstance.dispose()
  }
  
  studentChartInstance = echarts.init(studentChart.value)
  
  // 使用真实数据
  const activeStudents = courseStats.value.activeStudents || 0
  const totalStudents = courseStats.value.totalStudents || 1
  const inactiveStudents = totalStudents - activeStudents
  
  const option = {
    title: {
      text: '学生状态分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['活跃学生', '非活跃学生']
    },
    series: [
      {
        name: '学生状态',
        type: 'pie',
        radius: '50%',
        center: ['50%', '60%'],
        data: [
          { value: activeStudents, name: '活跃学生' },
          { value: inactiveStudents, name: '非活跃学生' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  studentChartInstance.setOption(option)
}

// 初始化作业完成情况图表
const initAssignmentChart = () => {
  if (!assignmentChart.value) return
  
  if (assignmentChartInstance) {
    assignmentChartInstance.dispose()
  }
  
  assignmentChartInstance = echarts.init(assignmentChart.value)
  
  // 使用真实数据
  const submittedAssignments = courseStats.value.submittedAssignments || 0
  const totalAssignments = courseStats.value.totalAssignments || 1
  const totalStudents = courseStats.value.totalStudents || 0
  const totalPossibleSubmissions = totalStudents * totalAssignments
  
  const option = {
    title: {
      text: '作业完成情况',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        return `${params[0].name}<br/>${params[0].seriesName}: ${params[0].value}份<br/>${params[1].seriesName}: ${params[1].value}份`
      }
    },
    legend: {
      data: ['已提交', '未提交']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['作业完成情况']
    },
    yAxis: {
      type: 'value',
      name: '份数'
    },
    series: [
      {
        name: '已提交',
        type: 'bar',
        data: [submittedAssignments],
        itemStyle: {
          color: '#409EFF'
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}份'
        }
      },
      {
        name: '未提交',
        type: 'bar',
        data: [totalPossibleSubmissions - submittedAssignments],
        itemStyle: {
          color: '#F56C6C'
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}份'
        }
      }
    ]
  }
  
  assignmentChartInstance.setOption(option)
}

// 初始化分数分布图表
const initScoreChart = () => {
  if (!scoreChart.value) return
  
  if (scoreChartInstance) {
    scoreChartInstance.dispose()
  }
  
  scoreChartInstance = echarts.init(scoreChart.value)
  
  // 使用真实数据 - 这里需要从API获取分数分布数据
  // 暂时使用模拟数据，基于平均分数生成分布
  const averageScore = courseStats.value.averageScore || 0
  const totalStudents = courseStats.value.totalStudents || 0
  
  // 根据平均分数生成一个简单的正态分布模拟
  const generateScoreDistribution = (avg, total) => {
    if (total === 0) return [0, 0, 0, 0, 0]
    
    // 简单的模拟：根据平均分数生成分布
    const distribution = [0, 0, 0, 0, 0] // 0-59, 60-69, 70-79, 80-89, 90-100
    
    if (avg < 60) {
      distribution[0] = Math.round(total * 0.7)
      distribution[1] = Math.round(total * 0.2)
      distribution[2] = Math.round(total * 0.1)
    } else if (avg < 70) {
      distribution[0] = Math.round(total * 0.2)
      distribution[1] = Math.round(total * 0.5)
      distribution[2] = Math.round(total * 0.2)
      distribution[3] = Math.round(total * 0.1)
    } else if (avg < 80) {
      distribution[1] = Math.round(total * 0.2)
      distribution[2] = Math.round(total * 0.5)
      distribution[3] = Math.round(total * 0.2)
      distribution[4] = Math.round(total * 0.1)
    } else if (avg < 90) {
      distribution[2] = Math.round(total * 0.2)
      distribution[3] = Math.round(total * 0.5)
      distribution[4] = Math.round(total * 0.3)
    } else {
      distribution[3] = Math.round(total * 0.3)
      distribution[4] = Math.round(total * 0.7)
    }
    
    // 确保总数正确
    const sum = distribution.reduce((a, b) => a + b, 0)
    if (sum !== total) {
      distribution[Math.floor(avg / 20)] += total - sum
    }
    
    return distribution
  }
  
  const scoreData = generateScoreDistribution(averageScore, totalStudents)
  
  const option = {
    title: {
      text: '分数分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: function(params) {
        return `${params[0].name}<br/>人数: ${params[0].value}人`
      }
    },
    xAxis: {
      type: 'category',
      data: ['0-59', '60-69', '70-79', '80-89', '90-100']
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [
      {
        name: '人数',
        type: 'bar',
        data: scoreData,
        itemStyle: {
          color: '#67C23A'
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}人'
        }
      }
    ]
  }
  
  scoreChartInstance.setOption(option)
}

// 生成AI总结
const generateAiSummary = async () => {
  if (!selectedCourse.value.id) return
  
  generatingSummary.value = true
  try {
    const response = await api.post(`/api/assignment-summary/${selectedCourse.value.id}/generate`)
    if (response.code === 200 && response.data) {
      latestSummary.value = response.data.summary
      aiSummaryContent.value = response.data.summary
      showAiSummary.value = true
      ElMessage.success('课程总结生成成功')
    } else {
      ElMessage.error(response.message || '生成失败')
    }
  } catch (error) {
    console.error('AI总结生成失败:', error)
    ElMessage.error('服务异常，请稍后重试')
  } finally {
    generatingSummary.value = false
  }
}

const openCreateTaskDialog = () => {
  showCreateTaskDialog.value = true
}

const handleUploadSuccess = (response) => {
  taskForm.att = response.data // 假设返回的是文件URL
}

const handleCreateTask = async () => {
  if (!taskForm.title) {
    ElMessage.error('请填写作业标题')
    return
  }
  try {
    const res = await api.post('api/task', {
      ...taskForm,
      classId: selectedCourse.value.id,
      createTime: new Date().toISOString()
    })
    ElMessage.success('作业发布成功')
    showCreateTaskDialog.value = false
    viewDetails(selectedCourse.value) // 刷新列表
  } catch (error) {
    ElMessage.error('作业发布失败')
  }
}

const goToGrading = (task) => {
  router.push({ name: 'teacher-note-grading', params: { id: task.taskId } })
}

//新建课程
const handleCreateCourse = async () => {
  if (!createForm.name) {
    ElMessage.error('请填写课程名称');
    return;
  }
  try {
    const currentUserId = userStore.id || 1;
    const currentTime = new Date().toISOString();
    await api.post('api/class',{
      className : createForm.name,
      teacherId: currentUserId,
      describe: createForm.description,
      createTime: currentTime
    })
    ElMessage.success('课程创建成功')
    showCreateDialog.value = false;
    await fetchTeacherCourses();
  } catch (error) {
    ElMessage.error('课程创建失败：'+error);
  }
}

const manageStudents = (course) => {
  router.push({ 
    name: 'teacher-student-management',
    query: { courseId: course.id, courseName: course.className }
  })
}

// 复制总结内容
const copySummary = () => {
  navigator.clipboard.writeText(aiSummaryContent.value)
    .then(() => {
      ElMessage.success('内容已复制到剪贴板')
    })
    .catch(err => {
      console.error('复制失败:', err)
      ElMessage.error('复制失败')
    })
}

// 监听窗口大小变化，重新渲染图表
window.addEventListener('resize', () => {
  if (studentChartInstance) studentChartInstance.resize()
  if (assignmentChartInstance) assignmentChartInstance.resize()
  if (scoreChartInstance) scoreChartInstance.resize()
})

const deleteCourse = async (id) => {
  try {
    await api.delete(`api/class/${id}`)
    ElMessage.success('课程已删除');
    fetchTeacherCourses();
  } catch (error) {
    ElMessage.error('删除失败');
  }
}

onMounted(() => {
  fetchTeacherCourses()
})
</script>

<style scoped>
.teacher-courses {
  padding: 20px;
}
.mt-20 {
  margin-top: 20px;
}
.mb-20 {
  margin-bottom: 20px;
}
.text-right {
  text-align: right;
}

.ai-summary-content {
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

/* 统计卡片样式 */
:deep(.el-statistic) {
  text-align: center;
  
  .el-statistic__head {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }
  
  .el-statistic__number {
    font-size: 24px;
    font-weight: 600;
  }
}

/* 图表容器样式 */
.el-card :deep(.el-card__header) {
  padding: 12px 20px;
  font-weight: 600;
}

.el-card :deep(.el-card__body) {
  padding: 15px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .el-col {
    margin-bottom: 20px;
  }
}
</style>
