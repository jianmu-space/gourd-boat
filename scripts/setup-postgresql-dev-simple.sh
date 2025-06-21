#!/bin/bash

# PostgreSQL开发环境设置脚本（简化版）
echo "🚀 设置PostgreSQL开发环境（简化版）..."

# 检查PostgreSQL是否已安装
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL未安装"
    echo ""
    echo "📦 安装PostgreSQL："
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "  macOS: brew install postgresql"
        echo "  然后运行: brew services start postgresql"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "  Ubuntu: sudo apt-get install postgresql postgresql-contrib"
        echo "  CentOS/RHEL: sudo yum install postgresql-server postgresql-contrib"
        echo "  然后运行: sudo systemctl start postgresql"
    fi
    echo ""
    echo "🐳 或者使用Docker（如果网络正常）："
    echo "  docker run -d --name postgres-dev -e POSTGRES_PASSWORD=postgrespw -e POSTGRES_DB=gourdboat_dev -p 55000:5432 postgres:15"
    exit 1
fi

echo "✅ PostgreSQL已安装"

# 启动PostgreSQL服务
echo "🔧 启动PostgreSQL服务..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    brew services start postgresql
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    sudo systemctl start postgresql
fi

# 等待服务启动
echo "⏳ 等待PostgreSQL服务启动..."
sleep 3

# 检查PostgreSQL是否在运行
if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
    echo "❌ PostgreSQL服务未启动"
    echo "💡 请手动启动PostgreSQL服务"
    exit 1
fi

echo "✅ PostgreSQL服务正在运行"

# 创建用户（如果不存在）
echo "👤 创建数据库用户..."
psql -h localhost -U postgres -c "CREATE USER postgres WITH PASSWORD 'postgrespw' SUPERUSER;" 2>/dev/null || echo "用户可能已存在"

# 创建开发数据库
echo "📝 创建开发数据库..."
psql -h localhost -U postgres -c "CREATE DATABASE gourdboat_dev OWNER postgres;" 2>/dev/null || echo "数据库可能已存在"

# 配置PostgreSQL监听端口55000
echo "🔧 配置PostgreSQL监听端口55000..."
PG_CONFIG_DIR=$(psql -h localhost -U postgres -c "SHOW config_file;" -t | xargs dirname)
echo "PostgreSQL配置文件目录: $PG_CONFIG_DIR"

# 检查是否已经在监听55000端口
if netstat -an | grep -q ":55000 "; then
    echo "✅ PostgreSQL已在端口55000监听"
else
    echo "⚠️  PostgreSQL默认在端口5432监听"
    echo "💡 要使用端口55000，请编辑postgresql.conf文件："
    echo "  设置: port = 55000"
    echo "  然后重启PostgreSQL服务"
    echo ""
    echo "🔗 当前连接信息："
    echo "  数据库地址: localhost:5432"
    echo "  数据库名称: gourdboat_dev"
    echo "  用户名: postgres"
    echo "  密码: postgrespw"
fi

# 验证数据库连接
echo "🔍 验证数据库连接..."
if psql -h localhost -U postgres -d gourdboat_dev -c "SELECT version();" >/dev/null 2>&1; then
    echo "✅ PostgreSQL开发环境设置成功！"
    echo ""
    echo "数据库配置信息："
    if netstat -an | grep -q ":55000 "; then
        echo "  数据库地址: localhost:55000"
    else
        echo "  数据库地址: localhost:5432"
    fi
    echo "  数据库名称: gourdboat_dev"
    echo "  用户名: postgres"
    echo "  密码: postgrespw"
    echo ""
    echo "💡 如果使用端口5432，请更新application-dev.yml中的数据库URL"
    echo ""
    echo "您现在可以启动应用程序了！"
else
    echo "❌ 数据库连接失败"
    echo "💡 请检查："
    echo "  - PostgreSQL服务是否启动"
    echo "  - 用户名密码是否正确"
    echo "  - 数据库是否创建成功"
    exit 1
fi 