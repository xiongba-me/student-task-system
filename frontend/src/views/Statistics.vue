<template>
  <div class="statistics-container">
    <!-- 头部标题 -->
    <div class="page-header">
      <h2>数据统计分析</h2>
      <p>学习时长和任务完成情况统计</p>
    </div>

    <!-- 饼图：各分类总用时占比 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>各分类总用时占比</span>
        </div>
      </template>
      <div ref="pieChartRef" class="chart-container" style="height: 400px;"></div>
    </el-card>

    <!-- 柱状图：各分类每日用时对比 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>各分类每日用时对比</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="loadData"
            size="small"
          />
        </div>
      </template>
      <div ref="barChartRef" class="chart-container" style="height: 500px;"></div>
    </el-card>

    <!-- 打卡统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="12" :sm="6" v-for="stat in statsData" :key="stat.label">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="28" color="#fff">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-value">{{ stat.value }}</p>
            <p class="stat-label">{{ stat.label }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import {
  Calendar,
  DocumentChecked,
  TrendCharts,
  WarningFilled
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { markRaw, nextTick, onMounted, onUnmounted, ref } from 'vue'

const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

const dateRange = ref([
  dayjs().subtract(14, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

const statsData = ref([
  { label: '累计天数', value: 0, icon: markRaw(TrendCharts), color: '#409EFF' },
  { label: '完成任务', value: 0, icon: markRaw(DocumentChecked), color: '#67C23A' },
  { label: '进行中', value: 0, icon: markRaw(WarningFilled), color: '#F56C6C' },
  { label: '总时长', value: '0h', icon: markRaw(Calendar), color: '#E6A23C' }
])

// 分类颜色配置（根据设计图）
const categoryColors = {
  '语文': '#5B8DBE',
  '数学': '#5FA777',
  '英语': '#E5A84B',
  '放松': '#D77878',
  '运动': '#6EC5C0',
  '技能': '#9B89C7',
  '其他': '#95A5A6'
}

// 加载饼图数据
const loadPieChart = async () => {
  try {
    const res = await request.get('/task/category-statistics', {
      params: {
        startDate: dateRange.value[0],
        endDate: dateRange.value[1]
      }
    })
    
    const categoryData = res.data || []
    
    // 转换数据格式
    const pieData = categoryData.map(item => ({
      name: item.category || '其他',
      value: Math.round((item.totalDuration || 0) / 3600 * 10) / 10, // 转为小时
      itemStyle: {
        color: categoryColors[item.category] || categoryColors['其他']
      }
    }))
    
    // 初始化饼图
    if (!pieChart && pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
    }
    
    const option = {
      title: {
        text: '各分类总用时占比',
        left: 'center',
        top: 20,
        textStyle: {
          fontSize: 16,
          color: '#666'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}h ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '10%',
        top: 'center',
        itemGap: 15,
        textStyle: {
          fontSize: 14
        }
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: false,
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n{c}h',
            fontSize: 12
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: true
          },
          data: pieData
        }
      ]
    }
    
    pieChart.setOption(option)
  } catch (error) {
    console.error('加载饼图数据失败:', error)
  }
}

// 加载柱状图数据
const loadBarChart = async () => {
  try {
    const res = await request.get('/task/list', {
      params: {
        page: 1,
        size: 1000,
        startDate: dateRange.value[0],
        endDate: dateRange.value[1]
      }
    })
    
    const tasks = res.data.records || []
    
    // 按日期和分类聚合数据
    const dateMap = new Map()
    const categories = new Set()
    
    tasks.forEach(task => {
      if (task.status === 'completed' && task.duration) {
        const date = task.taskDate
        const category = task.category || '其他'
        categories.add(category)
        
        if (!dateMap.has(date)) {
          dateMap.set(date, {})
        }
        
        const dateData = dateMap.get(date)
        dateData[category] = (dateData[category] || 0) + (task.duration / 3600)
      }
    })
    
    // 生成日期列表
    const dates = []
    let currentDate = dayjs(dateRange.value[0])
    const endDate = dayjs(dateRange.value[1])
    
    while (currentDate.isBefore(endDate) || currentDate.isSame(endDate)) {
      dates.push(currentDate.format('M/D'))
      currentDate = currentDate.add(1, 'day')
    }
    
    // 生成系列数据
    const seriesData = []
    const categoryList = Array.from(categories)
    
    categoryList.forEach(category => {
      const data = []
      let currentDate = dayjs(dateRange.value[0])
      
      while (currentDate.isBefore(endDate) || currentDate.isSame(endDate)) {
        const dateStr = currentDate.format('YYYY-MM-DD')
        const dateData = dateMap.get(dateStr) || {}
        data.push(Math.round((dateData[category] || 0) * 10) / 10)
        currentDate = currentDate.add(1, 'day')
      }
      
      seriesData.push({
        name: category,
        type: 'bar',
        stack: 'total',
        label: {
          show: true,
          formatter: (params) => {
            return params.value > 0 ? params.value + 'h' : ''
          },
          fontSize: 11,
          color: '#fff'
        },
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          color: categoryColors[category] || categoryColors['其他']
        },
        data: data
      })
    })
    
    // 初始化柱状图
    if (!barChart && barChartRef.value) {
      barChart = echarts.init(barChartRef.value)
    }
    
    const option = {
      title: {
        text: '各分类每日用时对比',
        left: 'center',
        top: 20,
        textStyle: {
          fontSize: 16,
          color: '#666'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function(params) {
          let result = params[0].axisValue + '<br/>'
          let total = 0
          params.forEach(item => {
            if (item.value > 0) {
              result += item.marker + item.seriesName + ': ' + item.value + 'h<br/>'
              total += item.value
            }
          })
          if (total > 0) {
            result += '<hr/>总计: ' + Math.round(total * 10) / 10 + 'h'
          }
          return result
        }
      },
      legend: {
        data: categoryList,
        top: 50,
        textStyle: {
          fontSize: 13
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: 100,
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: dates,
          axisLabel: {
            fontSize: 11,
            rotate: 30
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '小时',
          axisLabel: {
            fontSize: 11
          }
        }
      ],
      series: seriesData
    }
    
    barChart.setOption(option)
  } catch (error) {
    console.error('加载柱状图数据失败:', error)
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await request.get('/task/statistics', {
      params: {
        startDate: dateRange.value[0],
        endDate: dateRange.value[1]
      }
    })
    
    const data = res.data || {}
    
    const totalDuration = data.totalDuration || 0
    const hours = Math.floor(totalDuration / 3600)
    const minutes = Math.floor((totalDuration % 3600) / 60)
    
    statsData.value[0].value = data.totalTasks || 0
    statsData.value[1].value = data.completedTasks || 0
    statsData.value[2].value = data.inProgressTasks || 0
    statsData.value[3].value = hours > 0 ? `${hours}h${minutes}m` : `${minutes}m`
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载所有数据
const loadData = async () => {
  await loadStatistics()
  await nextTick()
  await loadPieChart()
  await loadBarChart()
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.statistics-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 14px;
  color: #999;
}

.chart-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.chart-container {
  width: 100%;
  min-height: 300px;
}

.stats-cards {
  margin-top: 30px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  text-align: center;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 8px 0;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 22px;
  }

  .chart-container {
    height: 300px !important;
  }

  .stat-value {
    font-size: 22px;
  }
}
</style>
