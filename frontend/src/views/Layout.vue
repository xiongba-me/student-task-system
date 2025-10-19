<template>
  <div class="layout-container">
    <el-container>
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <h2>学习任务管理系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="35" :src="userStore.userInfo.avatar || ''">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo.realName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  学号: {{ userStore.userInfo.studentNo }}
                </el-dropdown-item>
                <el-dropdown-item disabled>
                  班级: {{ userStore.userInfo.className }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="false"
            router
          >
            <el-menu-item index="/home">
              <el-icon><Odometer /></el-icon>
              <template #title>任务概览</template>
            </el-menu-item>
            <el-menu-item index="/tasks">
              <el-icon><Document /></el-icon>
              <template #title>学习计划</template>
            </el-menu-item>
            <el-menu-item index="/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>数据统计</template>
            </el-menu-item>
          </el-menu>
          
          <div class="collapse-btn" @click="toggleCollapse">
            <el-icon v-if="!isCollapse"><Fold /></el-icon>
            <el-icon v-else><Expand /></el-icon>
          </div>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="layout-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
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
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.el-container {
  height: 100%;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  margin-left: 10px;
  font-size: 14px;
  color: #333;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid #e6e6e6;
  transition: width 0.3s;
  position: relative;
}

.el-menu {
  border-right: none;
  height: calc(100% - 50px);
}

.collapse-btn {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-top: 1px solid #e6e6e6;
  transition: background 0.3s;
}

.collapse-btn:hover {
  background: #f5f7fa;
}

.layout-main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-header {
    padding: 0 10px;
  }
  
  .header-left h2 {
    font-size: 16px;
  }
  
  .username {
    display: none;
  }
  
  .layout-aside {
    position: absolute;
    z-index: 1000;
    height: calc(100% - 60px);
  }
  
  .layout-main {
    padding: 10px;
  }
}
</style>
