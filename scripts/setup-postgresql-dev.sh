#!/bin/bash

# PostgreSQL开发环境设置脚本
echo "正在设置PostgreSQL开发环境..."

# 检查PostgreSQL是否已安装
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL未安装。请先安装PostgreSQL："
    echo "macOS: brew install postgresql"
    echo "Ubuntu: sudo apt-get install postgresql postgresql-contrib"
    echo "CentOS/RHEL: sudo yum install postgresql-server postgresql-contrib"
    exit 1
fi

# 启动PostgreSQL服务
echo "启动PostgreSQL服务..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    brew services start postgresql
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    sudo systemctl start postgresql
fi

# 创建开发数据库
echo "创建开发数据库..."
PGPASSWORD=postgrespw psql -h localhost -p 55000 -U postgres << EOF
CREATE DATABASE gourdboat_dev;
\q
EOF

# 验证数据库连接
echo "验证数据库连接..."
PGPASSWORD=postgrespw psql -h localhost -p 55000 -U postgres -d gourdboat_dev -c "SELECT version();"

if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL开发环境设置成功！"
    echo ""
    echo "数据库配置信息："
    echo "  数据库地址: localhost:55000"
    echo "  数据库名称: gourdboat_dev"
    echo "  用户名: postgres"
    echo "  密码: postgrespw"
    echo ""
    echo "您现在可以启动应用程序了！"
else
    echo "❌ 数据库连接失败，请检查配置"
    exit 1
fi 