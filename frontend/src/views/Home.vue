<template>
  <div class="home-container">
    <!-- 顶部横幅 -->
    <div class="top-banner">
      <div class="banner-content">
        <h1>学习任务管理助手</h1>
        <p>你已坚持学习{{ statistics.totalDays || 0 }}天，已累计完成{{ statistics.completedTasks || 0 }}个任务！</p>
      </div>
      <el-dropdown @command="handleUserCommand">
        <span class="user-menu">
          {{ userStore.userInfo.realName }}
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 数据概览卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">今日学习时间</div>
        <div class="stat-value">{{ todayStudyTime }}</div>
        <div class="stat-underline"></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">运动户外时间</div>
        <div class="stat-value">{{ todayExerciseTime }}</div>
        <div class="stat-underline"></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日任务数量</div>
        <div class="stat-value">{{ todayTaskCount }}</div>
        <div class="stat-underline"></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日完成率</div>
        <div class="stat-value">{{ todayCompleteRate }}%</div>
        <div class="stat-underline"></div>
      </div>
      <div class="stat-card clickable" @click="goToStatistics">
        <div class="stat-label">统计图表汇总</div>
        <div class="stat-icon">
          <el-icon :size="36" color="#5b7fc7"><PieChart /></el-icon>
        </div>
        <div class="stat-underline"></div>
      </div>
      <div class="stat-card clickable" @click="goToTasks">
        <div class="stat-label">学习计划详情</div>
        <div class="stat-icon">
          <el-icon :size="36" color="#5b7fc7"><Calendar /></el-icon>
        </div>
        <div class="stat-underline"></div>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 今日任务 -->
      <el-card class="tasks-section">
        <template #header>
          <div class="section-header">
            <div class="header-left">
              <el-icon><Calendar /></el-icon>
              <span>今日任务</span>
            </div>
            <el-button type="primary" @click="goToTasks">
              添加任务 <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </template>

        <div v-loading="tasksLoading" class="task-list">
          <el-empty v-if="todayTasks.length === 0" description="今天还没有任务，去添加一个吧！" />
          <div
            v-for="task in todayTasks"
            :key="task.id"
            class="task-item"
            :class="{ 'completed': task.status === 'completed', 'in-progress': task.status === 'in_progress' }"
          >
            <div class="task-category" :style="{ background: getCategoryColor(task.category) }">
              {{ task.category || '其他' }}
            </div>
            <div class="task-content">
              <div class="task-title">{{ task.title || task.content }}</div>
              <div class="task-time" v-if="task.planStartTime">
                <el-icon><Clock /></el-icon>
                {{ task.planStartTime }} - {{ task.planEndTime }}
              </div>
            </div>
            <div class="task-actions">
              <el-button
                v-if="task.status === 'pending'"
                type="primary"
                size="small"
                @click="startTask(task.id)"
              >
                开始
              </el-button>
              <el-button
                v-else-if="task.status === 'in_progress'"
                type="success"
                size="small"
                @click="completeTask(task.id)"
              >
                完成
              </el-button>
              <el-tag v-else-if="task.status === 'completed'" type="success">
                <el-icon><CircleCheck /></el-icon> 已完成
              </el-tag>
              <el-tag v-else-if="task.status === 'cancelled'" type="info">已取消</el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 任务统计 -->
      <el-card class="stats-info">
        <template #header>
          <span>任务统计</span>
        </template>
        <div v-loading="statsLoading" class="stats-list">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon :size="24" color="#fff"><Document /></el-icon>
            </div>
            <div class="stat-detail">
              <div class="stat-number">{{ statistics.totalTasks || 0 }}</div>
              <div class="stat-text">总任务数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon :size="24" color="#fff"><CircleCheck /></el-icon>
            </div>
            <div class="stat-detail">
              <div class="stat-number">{{ statistics.completedTasks || 0 }}</div>
              <div class="stat-text">已完成</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon :size="24" color="#fff"><Clock /></el-icon>
            </div>
            <div class="stat-detail">
              <div class="stat-number">{{ statistics.inProgressTasks || 0 }}</div>
              <div class="stat-text">进行中</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon" style="background: #909399">
              <el-icon :size="24" color="#fff"><Timer /></el-icon>
            </div>
            <div class="stat-detail">
              <div class="stat-number">{{ totalDurationText }}</div>
              <div class="stat-text">总时长</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import {
    ArrowDown,
    Calendar,
    CircleCheck,
    Clock,
    Document,
    PieChart,
    Plus,
    Timer
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

const tasksLoading = ref(false)
const statsLoading = ref(false)
const statistics = ref({})
const todayTasks = ref([])

// 分类颜色配置
const categoryColors = {
  '语文': '#5B8DBE',
  '数学': '#5FA777',
  '英语': '#E5A84B',
  '放松': '#D77878',
  '运动': '#6EC5C0',
  '技能': '#9B89C7',
  '其他': '#95A5A6'
}

const getCategoryColor = (category) => {
  return categoryColors[category] || categoryColors['其他']
}

// 计算今日数据
const todayStudyTime = computed(() => {
  const studyCategories = ['语文', '数学', '英语', '技能']
  const studyTasks = todayTasks.value.filter(t => 
    t.status === 'completed' && studyCategories.includes(t.category)
  )
  const totalSeconds = studyTasks.reduce((sum, t) => sum + (t.duration || 0), 0)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  return hours > 0 ? `${hours}h${minutes}m` : `${minutes}m`
})

const todayExerciseTime = computed(() => {
  const exerciseTasks = todayTasks.value.filter(t => 
    t.status === 'completed' && t.category === '运动'
  )
  const totalSeconds = exerciseTasks.reduce((sum, t) => sum + (t.duration || 0), 0)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  return hours > 0 ? `${hours}h${minutes}m` : `${minutes}m`
})

const todayTaskCount = computed(() => {
  const completed = todayTasks.value.filter(t => t.status === 'completed').length
  const total = todayTasks.value.length
  return `${completed}/${total}`
})

const todayCompleteRate = computed(() => {
  const total = todayTasks.value.length
  if (total === 0) return 0
  const completed = todayTasks.value.filter(t => t.status === 'completed').length
  return Math.round((completed / total) * 100)
})

const totalDurationText = computed(() => {
  const totalSeconds = statistics.value.totalDuration || 0
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  return hours > 0 ? `${hours}h${minutes}m` : `${minutes}m`
})

// 获取统计数据
const getStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/task/statistics', {
      params: {
        startDate: dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
        endDate: dayjs().format('YYYY-MM-DD')
      }
    })
    statistics.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 加载今日任务
const loadTodayTasks = async () => {
  tasksLoading.value = true
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const res = await request.get('/task/list', {
      params: {
        page: 1,
        size: 100,
        date: today
      }
    })
    todayTasks.value = res.data.records || []
  } catch (error) {
    console.error('加载任务失败:', error)
  } finally {
    tasksLoading.value = false
  }
}

// 开始任务
const startTask = async (taskId) => {
  try {
    await request.post(`/task/start/${taskId}`)
    ElMessage.success('任务已开始')
    await loadTodayTasks()
  } catch (error) {
    console.error('开始任务失败:', error)
  }
}

// 完成任务
const completeTask = async (taskId) => {
  try {
    await request.post(`/task/complete/${taskId}`)
    ElMessage.success('任务已完成！')
    await loadTodayTasks()
    await getStatistics()
  } catch (error) {
    console.error('完成任务失败:', error)
  }
}

// 用户菜单操作
const handleUserCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.clearUserInfo()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    ElMessage.info('个人信息功能开发中...')
  }
}

// 页面跳转
const goToTasks = () => {
  router.push('/tasks')
}

const goToStatistics = () => {
  router.push('/statistics')
}

onMounted(() => {
  getStatistics()
  loadTodayTasks()
})
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

/* 顶部横幅 */
.top-banner {
  background: linear-gradient(135deg, #5b7fc7 0%, #7891d3 100%);
  color: white;
  padding: 24px 28px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(91, 127, 199, 0.3);
}

.banner-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.banner-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.95;
}

.user-menu {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.3s;
}

.user-menu:hover {
  background: rgba(255, 255, 255, 0.15);
}

/* 数据概览卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px 16px;
  text-align: center;
  position: relative;
  transition: all 0.3s;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  font-weight: 500;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #5b7fc7;
  margin-bottom: 8px;
}

.stat-icon {
  margin: 8px 0;
}

.stat-underline {
  width: 60%;
  height: 3px;
  background: #5b7fc7;
  margin: 8px auto 0;
  border-radius: 2px;
}

/* 主体内容区 */
.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 任务列表 */
.task-list {
  min-height: 300px;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid transparent;
  transition: all 0.3s;
}

.task-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.task-item.completed {
  border-left-color: #67C23A;
  background: #f0f9ff;
}

.task-item.in-progress {
  border-left-color: #E6A23C;
  background: #fffbf0;
}

.task-category {
  padding: 4px 12px;
  border-radius: 4px;
  color: white;
  font-size: 12px;
  margin-right: 12px;
  white-space: nowrap;
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-time {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-actions {
  margin-left: 12px;
}

/* 统计信息 */
.stats-list {
  min-height: 300px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 18px;
  margin-bottom: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-item .stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-detail {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-text {
  font-size: 13px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .top-banner {
    padding: 18px 16px;
  }

  .banner-content h1 {
    font-size: 18px;
  }

  .banner-content p {
    font-size: 12px;
  }

  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }

  .stat-card {
    padding: 16px 12px;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>
