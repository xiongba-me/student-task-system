<template>
  <div class="home-container">
    <!-- 顶部横幅 - 添加动画效果 -->
    <div class="top-banner">
      <div class="banner-gradient"></div>
      <div class="banner-content">
        <div class="welcome-section">
          <h1>
            <span class="wave">👋</span>
            你好，{{ userStore.userInfo.realName }}
          </h1>
          <p class="subtitle">
            <el-icon class="fire-icon"><Trophy /></el-icon>
            你已坚持学习 <span class="highlight">{{ statistics.totalDays || 0 }}</span> 天
            累计完成 <span class="highlight">{{ statistics.completedTasks || 0 }}</span> 个任务！
          </p>
        </div>
        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-menu">
            <el-avatar :size="40" :src="userStore.userInfo.avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="user-info-text">
              <span class="user-name">{{ userStore.userInfo.realName }}</span>
              <span class="user-role">学生</span>
            </div>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                <el-icon><School /></el-icon>
                班级: {{ userStore.userInfo.className }}
              </el-dropdown-item>
              <el-dropdown-item disabled>
                <el-icon><Postcard /></el-icon>
                学号: {{ userStore.userInfo.studentNo }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 数据概览卡片 - 网格布局优化 -->
    <div class="stats-grid">
      <div class="stat-card glass-card" data-aos="fade-up" data-aos-delay="0">
        <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日学习</div>
          <div class="stat-value">{{ todayStudyTime }}</div>
        </div>
      </div>

      <div class="stat-card glass-card" data-aos="fade-up" data-aos-delay="100">
        <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <el-icon :size="28"><Basketball /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">运动时长</div>
          <div class="stat-value">{{ todayExerciseTime }}</div>
        </div>
      </div>

      <div class="stat-card glass-card" data-aos="fade-up" data-aos-delay="200">
        <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">任务数量</div>
          <div class="stat-value">{{ todayTaskCount }}</div>
        </div>
      </div>

      <div class="stat-card glass-card" data-aos="fade-up" data-aos-delay="300">
        <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
          <el-icon :size="28"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">完成率</div>
          <div class="stat-value">{{ todayCompleteRate }}%</div>
        </div>
      </div>

      <div class="stat-card glass-card clickable" @click="goToStatistics" data-aos="fade-up" data-aos-delay="400">
        <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
          <el-icon :size="28"><PieChart /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">统计图表</div>
          <div class="stat-action">查看详情 →</div>
        </div>
      </div>

      <div class="stat-card glass-card clickable" @click="goToTasks" data-aos="fade-up" data-aos-delay="500">
        <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">学习计划</div>
          <div class="stat-action">去添加 →</div>
        </div>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 今日任务 -->
      <el-card class="tasks-section glass-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <div class="header-left">
              <div class="section-icon">
                <el-icon :size="20"><Calendar /></el-icon>
              </div>
              <div>
                <span class="section-title">今日任务</span>
                <span class="section-subtitle">{{ dayjs().format('YYYY年MM月DD日 dddd') }}</span>
              </div>
            </div>
            <el-button type="primary" @click="goToTasks" round>
              <el-icon><Plus /></el-icon>
              添加任务
            </el-button>
          </div>
        </template>

        <div v-loading="tasksLoading" class="task-list">
          <el-empty v-if="todayTasks.length === 0" description="今天还没有任务，去添加一个吧！">
            <el-button type="primary" @click="goToTasks" round>添加任务</el-button>
          </el-empty>

          <div
              v-for="task in todayTasks"
              :key="task.id"
              class="task-item"
              :class="{ 'completed': task.status === 'completed', 'in-progress': task.status === 'in_progress' }"
          >
            <div class="task-left">
              <div class="task-category" :style="{ background: getCategoryColor(task.category) }">
                {{ task.category || '其他' }}
              </div>
              <div class="task-content">
                <div class="task-title">{{ task.title || task.content }}</div>
                <div class="task-time" v-if="task.planStartTime">
                  <el-icon><Clock /></el-icon>
                  {{ task.planStartTime }} - {{ task.planEndTime }}
                  <span v-if="task.duration" class="duration-badge">
                    耗时 {{ formatDuration(task.duration) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="task-actions">
              <el-button
                  v-if="task.status === 'pending'"
                  type="primary"
                  size="default"
                  round
                  @click="startTask(task.id)"
              >
                <el-icon><VideoPlay /></el-icon>
                开始
              </el-button>
              <el-button
                  v-else-if="task.status === 'in_progress'"
                  type="success"
                  size="default"
                  round
                  @click="completeTask(task.id)"
              >
                <el-icon><CircleCheck /></el-icon>
                完成
              </el-button>
              <div v-else-if="task.status === 'completed'" class="completed-badge">
                <el-icon class="check-icon"><CircleCheckFilled /></el-icon>
                <span>已完成</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 任务统计 -->
      <el-card class="stats-info glass-card" shadow="hover">
        <template #header>
          <div class="section-header">
            <div class="header-left">
              <div class="section-icon">
                <el-icon :size="20"><DataAnalysis /></el-icon>
              </div>
              <span class="section-title">任务统计</span>
            </div>
          </div>
        </template>

        <div v-loading="statsLoading" class="stats-list">
          <div class="stat-item" v-for="(stat, index) in statsItems" :key="index">
            <div class="stat-icon" :style="{ background: stat.gradient }">
              <el-icon :size="24">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-detail">
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-text">{{ stat.label }}</div>
            </div>
            <div class="stat-progress" v-if="stat.progress !== undefined">
              <el-progress
                  :percentage="stat.progress"
                  :color="stat.color"
                  :show-text="false"
                  :stroke-width="6"
              />
            </div>
          </div>

          <!-- 进度环形图 -->
          <div class="progress-chart">
            <el-progress
                type="dashboard"
                :percentage="todayCompleteRate"
                :width="140"
                :stroke-width="12"
            >
              <template #default="{ percentage }">
                <div class="progress-content">
                  <span class="percentage-value">{{ percentage }}%</span>
                  <span class="percentage-label">完成率</span>
                </div>
              </template>
            </el-progress>
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
  Basketball,
  Calendar,
  CircleCheck,
  CircleCheckFilled,
  Clock,
  DataAnalysis,
  Document,
  PieChart,
  Plus,
  Postcard,
  School,
  SwitchButton,
  Timer,
  TrendCharts,
  Trophy,
  UserFilled,
  VideoPlay
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

dayjs.locale('zh-cn')

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
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`
})

const todayExerciseTime = computed(() => {
  const exerciseTasks = todayTasks.value.filter(t =>
      t.status === 'completed' && t.category === '运动'
  )
  const totalSeconds = exerciseTasks.reduce((sum, t) => sum + (t.duration || 0), 0)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`
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
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`
})

// 统计项
const statsItems = computed(() => [
  {
    icon: Document,
    label: '总任务数',
    value: statistics.value.totalTasks || 0,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    color: '#667eea',
    progress: undefined
  },
  {
    icon: CircleCheck,
    label: '已完成',
    value: statistics.value.completedTasks || 0,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    color: '#43e97b',
    progress: statistics.value.totalTasks ? Math.round((statistics.value.completedTasks / statistics.value.totalTasks) * 100) : 0
  },
  {
    icon: Clock,
    label: '进行中',
    value: statistics.value.inProgressTasks || 0,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    color: '#fa709a',
    progress: undefined
  },
  {
    icon: Timer,
    label: '总时长',
    value: totalDurationText.value,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    color: '#4facfe',
    progress: undefined
  }
])

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

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
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
}

/* 玻璃态效果 */
.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 顶部横幅 */
.top-banner {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px;
  border-radius: 20px;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.banner-gradient {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.banner-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.welcome-section h1 {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
}

.wave {
  display: inline-block;
  animation: wave 2s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-20deg); }
}

.subtitle {
  margin: 0;
  font-size: 16px;
  opacity: 0.95;
  display: flex;
  align-items: center;
  gap: 8px;
}

.fire-icon {
  color: #FFA500;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.highlight {
  font-weight: 700;
  font-size: 20px;
  color: #FFD700;
  margin: 0 4px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.user-menu:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  opacity: 0.8;
}

.dropdown-icon {
  margin-left: 4px;
  transition: transform 0.3s;
}

/* 数据概览卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  line-height: 1;
}

.stat-action {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
  margin-top: 4px;
}

/* 主体内容区 */
.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.section-subtitle {
  display: block;
  font-size: 13px;
  color: #999;
  font-weight: 400;
  margin-top: 2px;
}

/* 任务列表 */
.task-list {
  min-height: 300px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 14px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.task-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #667eea;
  transition: width 0.3s;
}

.task-item:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.task-item:hover::before {
  width: 8px;
}

.task-item.completed {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  opacity: 0.9;
}

.task-item.completed::before {
  background: #43e97b;
}

.task-item.in-progress {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
}

.task-item.in-progress::before {
  background: #fa709a;
}

.task-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.task-category {
  padding: 6px 14px;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: 15px;
  color: #333;
  font-weight: 600;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-time {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;
}

.duration-badge {
  margin-left: 8px;
  padding: 2px 8px;
  background: rgba(67, 233, 123, 0.2);
  border-radius: 4px;
  font-size: 12px;
  color: #43e97b;
  font-weight: 600;
}

.task-actions {
  flex-shrink: 0;
}

.completed-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #43e97b;
  font-size: 14px;
  font-weight: 600;
}

.check-icon {
  font-size: 20px;
}

/* 统计信息 */
.stats-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 300px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 14px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-item .stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.stat-detail {
  flex: 1;
  margin-left: 16px;
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-text {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.stat-progress {
  margin-top: 8px;
}

.progress-chart {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 14px;
}

.progress-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.percentage-value {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
}

.percentage-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 16px;
  }

  .top-banner {
    padding: 20px;
  }

  .banner-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome-section h1 {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .highlight {
    font-size: 16px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon-wrapper {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 20px;
  }

  .task-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .task-left {
    width: 100%;
  }

  .task-actions {
    width: 100%;
  }

  .task-actions .el-button {
    width: 100%;
  }
}
</style>