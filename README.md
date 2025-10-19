# 学生任务管理系统

一个基于 Spring Boot + Vue 3 的学生任务管理系统，支持用户注册、登录、任务管理和数据统计功能。

## 技术栈

### 后端
- Spring Boot 2.7.x
- MyBatis-Plus
- MySQL
- JWT 认证
- Maven

### 前端
- Vue 3
- Vite
- Pinia (状态管理)
- Axios
- Element Plus (UI组件库)

## 项目结构

```
student-task-system/
├── backend/          # Spring Boot 后端项目
│   ├── src/
│   └── pom.xml
├── frontend/         # Vue 3 前端项目
│   ├── src/
│   └── package.json
└── start.sh         # 启动脚本
```

## 功能特性

- ✅ 用户注册与登录
- ✅ JWT Token 认证
- ✅ 任务增删改查
- ✅ 任务状态管理
- ✅ 数据统计展示
- ✅ 响应式界面设计

## 快速开始

### 环境要求
- JDK 8+
- Node.js 14+
- MySQL 5.7+

### 数据库配置

1. 创建数据库
```sql
CREATE DATABASE student_task_db;
```

2. 导入初始化脚本
```bash
mysql -u root -p student_task_db < backend/src/main/resources/db_init.sql
```

3. 修改数据库配置
编辑 `backend/src/main/resources/application.yml` 中的数据库连接信息

### 安装依赖

#### 后端
```bash
cd backend
mvn clean install
```

#### 前端
```bash
cd frontend
npm install
```

### 运行项目

#### 方式一: 使用启动脚本
```bash
chmod +x start.sh
./start.sh
```

#### 方式二: 分别启动

**后端:**
```bash
cd backend
mvn spring-boot:run
```

**前端:**
```bash
cd frontend
npm run dev
```

### 访问应用

- 前端地址: http://localhost:5173
- 后端API: http://localhost:8080

## API 文档

### 用户相关
- POST `/api/user/register` - 用户注册
- POST `/api/user/login` - 用户登录
- GET `/api/user/info` - 获取用户信息

### 任务相关
- GET `/api/task/list` - 获取任务列表
- POST `/api/task` - 创建任务
- PUT `/api/task` - 更新任务
- DELETE `/api/task/{id}` - 删除任务
- GET `/api/task/statistics` - 获取统计数据

## 开发计划

- [ ] 任务优先级设置
- [ ] 任务标签分类
- [ ] 任务提醒功能
- [ ] 导出报表功能
- [ ] 多用户协作

## 许可证

MIT License
