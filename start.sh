#!/bin/bash

echo "=================================="
echo "学生打卡系统 - 启动脚本"
echo "=================================="

# 检查是否安装了必要的工具
check_requirements() {
    echo "检查运行环境..."
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        echo "❌ 未找到 Java，请先安装 JDK 1.8+"
        exit 1
    fi
    echo "✅ Java 版本: $(java -version 2>&1 | head -n 1)"
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        echo "❌ 未找到 Maven，请先安装 Maven 3.6+"
        exit 1
    fi
    echo "✅ Maven 版本: $(mvn -version | head -n 1)"
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        echo "❌ 未找到 Node.js，请先安装 Node.js 16+"
        exit 1
    fi
    echo "✅ Node.js 版本: $(node -v)"
    
    # 检查 MySQL
    if ! command -v mysql &> /dev/null; then
        echo "⚠️  未找到 MySQL 命令，请确保 MySQL 8.0+ 已安装并运行"
    else
        echo "✅ MySQL 已安装"
    fi
    
    echo ""
}

# 启动后端
start_backend() {
    echo "=================================="
    echo "启动后端服务..."
    echo "=================================="
    
    cd backend
    
    echo "正在构建项目..."
    mvn clean install -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "✅ 项目构建成功"
        echo "正在启动后端服务..."
        mvn spring-boot:run &
        BACKEND_PID=$!
        echo "后端服务 PID: $BACKEND_PID"
        echo "后端服务地址: http://localhost:8080"
    else
        echo "❌ 项目构建失败"
        exit 1
    fi
    
    cd ..
    echo ""
}

# 启动前端
start_frontend() {
    echo "=================================="
    echo "启动前端服务..."
    echo "=================================="
    
    cd frontend
    
    # 检查是否已安装依赖
    if [ ! -d "node_modules" ]; then
        echo "正在安装依赖..."
        npm install
    fi
    
    echo "正在启动前端服务..."
    npm run dev &
    FRONTEND_PID=$!
    echo "前端服务 PID: $FRONTEND_PID"
    echo "前端服务地址: http://localhost:3000"
    
    cd ..
    echo ""
}

# 主函数
main() {
    check_requirements
    
    echo "=================================="
    echo "请选择启动模式："
    echo "1. 启动后端服务"
    echo "2. 启动前端服务"
    echo "3. 同时启动前后端"
    echo "=================================="
    read -p "请输入选项 (1/2/3): " choice
    
    case $choice in
        1)
            start_backend
            ;;
        2)
            start_frontend
            ;;
        3)
            start_backend
            sleep 5
            start_frontend
            ;;
        *)
            echo "无效的选项"
            exit 1
            ;;
    esac
    
    echo "=================================="
    echo "✅ 服务启动完成！"
    echo "=================================="
    echo ""
    echo "按 Ctrl+C 停止所有服务"
    
    # 等待用户中断
    wait
}

# 运行主函数
main
