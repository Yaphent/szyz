#!/bin/bash

# 企业级权限管理平台 - 启动脚本

echo "====================================="
echo "  启动权限管理平台后端服务..."
echo "====================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未检测到Java环境，请先安装JDK 1.8+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未检测到Maven环境，请先安装Maven 3.6+"
    exit 1
fi

# 检查MySQL连接
echo "检查MySQL连接..."
mysql -h localhost -u root -proot123 -e "USE permission_db;" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "警告: 无法连接到MySQL，请确保MySQL已启动并创建了数据库"
    echo "创建数据库命令: CREATE DATABASE permission_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

# 启动服务
echo "正在启动服务..."
cd "$(dirname "$0")"
mvn spring-boot:run -Dspring-boot.run.fork=false

echo "服务已停止"
