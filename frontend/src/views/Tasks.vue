<template>
  <div class="tasks-container">
    <!-- 顶部横幅 -->
    <div class="header-banner">
      <h1>学习计划与打卡统计助手</h1>
      <p>你已坚持打卡{{ totalDays }}天,已累计完成{{ totalTasks }}个学习计划!</p>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card">
        <div class="stat-label">今日学习时间</div>
        <div class="stat-value">{{ todayStudyTime }}h</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">运动户外时间</div>
        <div class="stat-value">{{ todaySportsTime }}h</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日任务数量</div>
        <div class="stat-value">{{ completedTasks }}/{{ totalTasksToday }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日完成率</div>
        <div class="stat-value">{{ completionRate }}%</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">统计图表汇总</div>
        <div class="stat-icon"><el-icon><PieChart /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">荣誉勋章墙</div>
        <div class="stat-icon"><el-icon><Trophy /></el-icon></div>
      </div>
    </div>

    <!-- 学习计划标题和操作按钮 -->
    <div class="section-header">
      <div class="section-title">
        <el-icon><Notebook /></el-icon>
        <span>学习计划</span>
      </div>
      <div class="actions">
        <el-button type="info" plain @click="showAddDialog">批量添加</el-button>
        <el-button type="primary" @click="showAddSingleDialog">
          <el-icon><Plus /></el-icon>
          添加计划
        </el-button>
      </div>
    </div>

    <!-- 周视图日历 -->
    <div class="week-calendar">
      <div class="week-header">
        <el-button :icon="ArrowLeft" circle size="small" @click="changeWeek(-1)" />
        <span class="week-title">{{ weekTitle }}</span>
        <el-button :icon="ArrowRight" circle size="small" @click="changeWeek(1)" />
        <el-button type="warning" size="small" @click="goToday">今天</el-button>
      </div>
      <div class="week-days">
        <div
          v-for="day in weekDays"
          :key="day.date"
          class="day-card"
          :class="{ active: day.date === selectedDate, today: day.isToday }"
          @click="selectDate(day.date)"
        >
          <div class="day-name">{{ day.name }}</div>
          <div class="day-date">{{ day.dayMonth }}</div>
        </div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div v-loading="loading" class="tasks-list">
      <div v-if="tasks.length === 0" class="empty-state">
        <el-empty description="暂无任务" />
      </div>
      
      <div
        v-for="task in tasks"
        :key="task.id"
        class="task-card"
        :class="[`task-${task.category}`, `status-${task.status}`]"
      >
        <!-- 左侧分类标签 -->
        <div class="task-category-sidebar" :style="{ backgroundColor: getCategoryColor(task.category) }">
          <div class="category-text">{{ task.category || '未分类' }}</div>
        </div>

        <!-- 主体内容 -->
        <div class="task-main">
          <div class="task-header">
            <div class="task-title-row">
              <el-icon class="task-icon"><component :is="getCategoryIcon(task.category)" /></el-icon>
              <h4>{{ task.title }}</h4>
              <el-tag v-if="task.repeatType" size="small" type="info">仅当天</el-tag>
            </div>
          </div>

          <div class="task-content">
            <div class="task-info-row">
              <span class="info-label">内容:</span>
              <span class="info-value">{{ task.content || '无描述' }}</span>
            </div>
          </div>

          <div class="task-footer">
            <div class="time-info">
              <div class="time-item plan-time">
                <span class="time-label">计划时间:</span>
                <span class="time-value">{{ formatTimeRange(task.planStartTime, task.planEndTime) }}</span>
              </div>
              <div v-if="task.status !== 'pending'" class="time-item actual-time">
                <span class="time-label">实际时间:</span>
                <span class="time-value">{{ formatTimeRange(task.actualStartTime, task.actualEndTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧状态区域 -->
        <div class="task-status-area">
          <!-- 进行中显示倒计时 -->
          <div v-if="task.status === 'in_progress'" class="countdown-display">
            <div class="countdown-time" :class="{ warning: isCountdownWarning(task) }">
              {{ getCountdownDisplay(task) }}
            </div>
            <el-button
              type="success"
              size="small"
              round
              @click="completeTask(task.id)"
            >
              <el-icon><CircleCheck /></el-icon>
              已完成
            </el-button>
          </div>

          <!-- 待开始显示开始按钮 -->
          <div v-else-if="task.status === 'pending'" class="pending-display">
            <div class="planned-duration">
              {{ getPlannedDuration(task) }}
            </div>
            <el-button
              type="primary"
              size="small"
              round
              @click="startTask(task.id)"
            >
              <el-icon><VideoPlay /></el-icon>
              开始
            </el-button>
          </div>

          <!-- 已完成显示耗时 -->
          <div v-else-if="task.status === 'completed'" class="completed-display">
            <div class="duration-time">
              {{ formatDuration(task.duration) }}
            </div>
            <el-button
              type="success"
              size="small"
              round
              disabled
            >
              <el-icon><CircleCheck /></el-icon>
              已完成
            </el-button>
          </div>

          <!-- 删除按钮 -->
          <el-button
            class="delete-btn"
            type="danger"
            size="small"
            :icon="Delete"
            circle
            @click="deleteTask(task.id)"
          />
        </div>
      </div>
    </div>

    <!-- 批量添加对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量添加任务"
      width="90%"
      :close-on-click-modal="false"
      style="max-width: 700px"
    >
      <el-form :model="batchForm" label-width="140px">
        <el-form-item label="批量输入任务">
          <el-input
            v-model="batchForm.content"
            type="textarea"
            :rows="6"
            placeholder="例如：&#10;语文作业&#10;1、完成《大青树下的小学》阅读单&#10;2、预习《花的学校》&#10;数学作业&#10;1、完成练习册P12-P15"
          />
        </el-form-item>

        <el-form-item label="重复频率">
          <el-select v-model="batchForm.repeatType" placeholder="请选择">
            <el-option label="仅当天" value="only_today" />
            <el-option label="每天" value="everyday" />
            <el-option label="每周的这天" value="every_week_day" />
            <el-option label="周一至周五" value="monday_to_friday" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>

        <el-form-item label="计划时间">
          <el-time-picker
            v-model="timeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="起始日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY年MM月DD日"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="任务分类">
          <el-select v-model="batchForm.category" placeholder="请选择分类">
            <el-option label="运动" value="运动" />
            <el-option label="英语" value="英语" />
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitBatchTasks">
          预览计划
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import {
  ArrowLeft,
  ArrowRight,
  Basketball,
  CircleCheck,
  Delete,
  EditPen,
  Histogram,
  Notebook,
  PieChart,
  Plus,
  Reading,
  Trophy,
  VideoPlay
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

const loading = ref(false)
const submitLoading = ref(false)
const batchDialogVisible = ref(false)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const currentWeekStart = ref(dayjs().startOf('week'))
const tasks = ref([])
const timeRange = ref([])
const dateRange = ref([])
const currentTime = ref(dayjs())
let countdownTimer = null

// 统计数据
const totalDays = ref(73)
const totalTasks = ref(484)
const todayStudyTime = ref('0.0')
const todaySportsTime = ref('0.0')
const completedTasks = ref(0)
const totalTasksToday = ref(0)

const batchForm = reactive({
  content: '',
  repeatType: 'only_today',
  category: '其他',
  planStartTime: null,
  planEndTime: null,
  startDate: null,
  endDate: null
})

// 计算完成率
const completionRate = computed(() => {
  if (totalTasksToday.value === 0) return 0
  return Math.round((completedTasks.value / totalTasksToday.value) * 100)
})

// 周标题
const weekTitle = computed(() => {
  const year = currentWeekStart.value.year()
  const month = currentWeekStart.value.month() + 1
  const weekNum = currentWeekStart.value.week()
  return `${year}年${month}月第${weekNum}周`
})

// 生成本周日期数组
const weekDays = computed(() => {
  const days = []
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  for (let i = 0; i < 7; i++) {
    const date = currentWeekStart.value.add(i, 'day')
    days.push({
      name: dayNames[i],
      date: date.format('YYYY-MM-DD'),
      dayMonth: date.format('M/D'),
      isToday: date.format('YYYY-MM-DD') === dayjs().format('YYYY-MM-DD')
    })
  }
  return days
})

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const res = await request.get('/task/list', {
      params: {
        page: 1,
        size: 100,
        date: selectedDate.value
      }
    })
    tasks.value = res.data.records
    
    // 更新统计数据
    updateStatistics()
  } catch (error) {
    console.error('加载任务失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStatistics = () => {
  totalTasksToday.value = tasks.value.length
  completedTasks.value = tasks.value.filter(t => t.status === 'completed').length
  
  // 计算今日学习时间和运动时间
  let studySeconds = 0
  let sportsSeconds = 0
  
  tasks.value.forEach(task => {
    if (task.status === 'completed' && task.duration) {
      if (task.category === '运动') {
        sportsSeconds += task.duration
      } else {
        studySeconds += task.duration
      }
    }
  })
  
  todayStudyTime.value = (studySeconds / 3600).toFixed(1)
  todaySportsTime.value = (sportsSeconds / 3600).toFixed(1)
}

// 显示批量添加对话框
const showAddDialog = () => {
  batchForm.content = ''
  batchForm.repeatType = 'only_today'
  batchForm.category = '其他'
  timeRange.value = []
  dateRange.value = [selectedDate.value, selectedDate.value]
  batchDialogVisible.value = true
}

// 显示单个添加对话框
const showAddSingleDialog = () => {
  ElMessage.info('请使用批量添加功能')
  showAddDialog()
}

// 提交批量任务
const submitBatchTasks = async () => {
  if (!batchForm.content.trim()) {
    ElMessage.warning('请输入任务内容')
    return
  }

  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择起止日期')
    return
  }

  submitLoading.value = true
  try {
    const data = {
      ...batchForm,
      planStartTime: timeRange.value[0],
      planEndTime: timeRange.value[1],
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }

    await request.post('/task/batch-add', data)
    ElMessage.success('任务添加成功')
    batchDialogVisible.value = false
    loadTasks()
  } catch (error) {
    console.error('添加任务失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 开始任务
const startTask = async (id) => {
  try {
    await request.post(`/task/start/${id}`)
    ElMessage.success('任务已开始')
    loadTasks()
  } catch (error) {
    console.error('开始任务失败:', error)
  }
}

// 完成任务
const completeTask = async (id) => {
  try {
    await request.post(`/task/complete/${id}`)
    ElMessage.success('任务已完成')
    loadTasks()
  } catch (error) {
    console.error('完成任务失败:', error)
  }
}

// 删除任务
const deleteTask = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/task/${id}`)
    ElMessage.success('任务已删除')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
    }
  }
}

// 选择日期
const selectDate = (date) => {
  selectedDate.value = date
  loadTasks()
}

// 切换周
const changeWeek = (weeks) => {
  currentWeekStart.value = currentWeekStart.value.add(weeks, 'week')
  selectedDate.value = currentWeekStart.value.format('YYYY-MM-DD')
  loadTasks()
}

// 回到今天
const goToday = () => {
  const today = dayjs()
  currentWeekStart.value = today.startOf('week')
  selectedDate.value = today.format('YYYY-MM-DD')
  loadTasks()
}

// 获取分类颜色
const getCategoryColor = (category) => {
  const colors = {
    '运动': '#9b59b6',
    '英语': '#3498db',
    '语文': '#27ae60',
    '数学': '#e67e22'
  }
  return colors[category] || '#95a5a6'
}

// 获取分类图标
const getCategoryIcon = (category) => {
  const icons = {
    '运动': Basketball,
    '英语': Reading,
    '语文': EditPen,
    '数学': Histogram
  }
  return icons[category] || Reading
}

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(`2000-01-01 ${time}`).format('HH:mm') : ''
}

// 格式化时间范围
const formatTimeRange = (startTime, endTime) => {
  if (!startTime || !endTime) return '--'
  return `${formatTime(startTime)}-${formatTime(endTime)}`
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '00:00:00'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// 获取计划时长
const getPlannedDuration = (task) => {
  if (!task.planStartTime || !task.planEndTime) return ''
  
  const start = dayjs(`2000-01-01 ${task.planStartTime}`)
  const end = dayjs(`2000-01-01 ${task.planEndTime}`)
  const duration = end.diff(start, 'second')
  
  return formatDuration(duration)
}

// 获取倒计时显示
const getCountdownDisplay = (task) => {
  if (!task.actualStartTime || !task.planEndTime) return '00:00:00'
  
  const actualStart = dayjs(`${selectedDate.value} ${task.actualStartTime}`)
  const now = currentTime.value
  const elapsed = now.diff(actualStart, 'second')
  
  const planStart = dayjs(`2000-01-01 ${task.planStartTime}`)
  const planEnd = dayjs(`2000-01-01 ${task.planEndTime}`)
  const plannedDuration = planEnd.diff(planStart, 'second')
  
  const remaining = plannedDuration - elapsed
  
  if (remaining <= 0) {
    return '-' + formatDuration(Math.abs(remaining))
  }
  
  return formatDuration(remaining)
}

// 判断倒计时是否需要警告
const isCountdownWarning = (task) => {
  if (!task.actualStartTime || !task.planEndTime) return false
  
  const actualStart = dayjs(`${selectedDate.value} ${task.actualStartTime}`)
  const planStart = dayjs(`2000-01-01 ${task.planStartTime}`)
  const planEnd = dayjs(`2000-01-01 ${task.planEndTime}`)
  const plannedDuration = planEnd.diff(planStart, 'second')
  const elapsed = currentTime.value.diff(actualStart, 'second')
  const remaining = plannedDuration - elapsed
  
  return remaining < 300 && remaining > 0
}

// 启动倒计时
const startCountdownTimer = () => {
  countdownTimer = setInterval(() => {
    currentTime.value = dayjs()
  }, 1000)
}

// 停止倒计时
const stopCountdownTimer = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

onMounted(() => {
  loadTasks()
  startCountdownTimer()
})

onBeforeUnmount(() => {
  stopCountdownTimer()
})
</script>

<style scoped>
.tasks-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 顶部横幅 */
.header-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 24px;
  color: white;
  text-align: center;
}

.header-banner h1 {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 600;
}

.header-banner p {
  margin: 0;
  font-size: 16px;
  opacity: 0.95;
}

/* 统计卡片 */
.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  border-bottom: 4px solid #667eea;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-icon {
  font-size: 32px;
  color: #667eea;
}

/* 学习计划标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.actions {
  display: flex;
  gap: 12px;
}

/* 周视图日历 */
.week-calendar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.week-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.week-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  min-width: 150px;
  text-align: center;
}

.week-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

.day-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.day-card:hover {
  background: #e8eaf6;
}

.day-card.active {
  background: #5b7fc7;
  color: white;
  border-color: #5b7fc7;
}

.day-card.today {
  border-color: #f59e0b;
}

.day-name {
  font-size: 12px;
  margin-bottom: 8px;
  opacity: 0.8;
}

.day-date {
  font-size: 16px;
  font-weight: 600;
}

/* 任务列表 */
.tasks-list {
  min-height: 400px;
}

.task-card {
  background: white;
  border-radius: 12px;
  padding: 0;
  margin-bottom: 16px;
  display: flex;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.task-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 左侧分类标签 */
.task-category-sidebar {
  width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
  writing-mode: vertical-rl;
  text-orientation: upright;
  letter-spacing: 4px;
  flex-shrink: 0;
}

.category-text {
  white-space: nowrap;
}

/* 主体内容 */
.task-main {
  flex: 1;
  padding: 20px;
  min-width: 0;
}

.task-header {
  margin-bottom: 12px;
}

.task-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-icon {
  font-size: 20px;
  color: #667eea;
}

.task-title-row h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.task-content {
  margin-bottom: 12px;
}

.task-info-row {
  font-size: 14px;
  line-height: 1.6;
}

.info-label {
  color: #666;
  font-weight: 500;
}

.info-value {
  color: #333;
}

.task-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.time-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.time-item {
  font-size: 13px;
}

.time-label {
  color: #999;
  margin-right: 4px;
}

.time-value {
  font-weight: 500;
}

.plan-time .time-value {
  color: #3498db;
}

.actual-time .time-value {
  color: #e74c3c;
}

/* 右侧状态区域 */
.task-status-area {
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px 16px;
  background: #fafafa;
  border-left: 1px solid #f0f0f0;
  position: relative;
  flex-shrink: 0;
}

.countdown-display,
.pending-display,
.completed-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.countdown-time {
  font-size: 28px;
  font-weight: bold;
  color: #67C23A;
  font-family: 'Courier New', monospace;
}

.countdown-time.warning {
  color: #E6A23C;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.planned-duration {
  font-size: 24px;
  font-weight: bold;
  color: #909399;
  font-family: 'Courier New', monospace;
}

.duration-time {
  font-size: 28px;
  font-weight: bold;
  color: #67C23A;
  font-family: 'Courier New', monospace;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}

.task-card.status-completed {
  opacity: 0.85;
}

.task-card.status-completed .task-main {
  background: linear-gradient(to right, transparent, #f0f9ff 50%);
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background: white;
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .task-status-area {
    width: 160px;
  }

  .countdown-time,
  .duration-time {
    font-size: 22px;
  }
}

@media (max-width: 768px) {
  .tasks-container {
    padding: 12px;
  }

  .header-banner {
    padding: 20px;
  }

  .header-banner h1 {
    font-size: 22px;
  }

  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .week-days {
    gap: 8px;
  }

  .day-card {
    padding: 12px 8px;
  }

  .task-card {
    flex-direction: column;
  }

  .task-category-sidebar {
    width: 100%;
    height: 40px;
    writing-mode: horizontal-tb;
    text-orientation: mixed;
    letter-spacing: normal;
    padding: 8px;
  }

  .task-status-area {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    border-left: none;
    border-top: 1px solid #f0f0f0;
  }

  .countdown-display,
  .pending-display,
  .completed-display {
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
