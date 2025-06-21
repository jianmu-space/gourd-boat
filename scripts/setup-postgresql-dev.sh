#!/bin/bash

# PostgreSQL开发环境设置脚本
echo "🚀 正在设置PostgreSQL开发环境..."

# 检查是否使用Docker
if command -v docker &> /dev/null && docker ps &> /dev/null; then
    echo "🐳 检测到Docker，使用Docker运行PostgreSQL..."
    
    # 检查PostgreSQL容器是否已运行
    if docker ps | grep -q "postgres-dev"; then
        echo "✅ PostgreSQL容器已在运行"
    else
        echo "📦 启动PostgreSQL容器..."
        
        # 检查是否有PostgreSQL镜像
        if ! docker images | grep -q "postgres"; then
            echo "📥 需要下载PostgreSQL镜像..."
            echo "⏳ 正在下载postgres:15镜像（可能需要几分钟）..."
            
            # 尝试下载镜像
            if ! docker pull postgres:15; then
                echo "❌ 无法下载PostgreSQL镜像，可能是网络问题"
                echo ""
                echo "💡 替代方案："
                echo "1. 检查网络连接"
                echo "2. 使用本地PostgreSQL安装"
                echo "3. 手动下载镜像：docker pull postgres:15"
                echo "4. 使用其他PostgreSQL版本：docker pull postgres:14"
                echo ""
                echo "是否继续使用本地PostgreSQL安装？(y/n)"
                read -r response
                if [[ "$response" =~ ^[Yy]$ ]]; then
                    echo "💻 切换到本地PostgreSQL安装..."
                else
                    echo "❌ 设置取消"
                    exit 1
                fi
            fi
        fi
        
        # 启动容器
        if docker run -d \
            --name postgres-dev \
            -e POSTGRES_PASSWORD=postgrespw \
            -e POSTGRES_DB=gourdboat_dev \
            -p 55000:5432 \
            postgres:15; then
            
            # 等待容器启动
            echo "⏳ 等待PostgreSQL容器启动..."
            sleep 5
        else
            echo "❌ 启动PostgreSQL容器失败"
            echo "💡 可能的原因："
            echo "  - 端口55000已被占用"
            echo "  - 容器名称postgres-dev已存在"
            echo "  - Docker权限问题"
            echo ""
            echo "尝试清理并重新启动..."
            docker rm -f postgres-dev 2>/dev/null
            docker run -d \
                --name postgres-dev \
                -e POSTGRES_PASSWORD=postgrespw \
                -e POSTGRES_DB=gourdboat_dev \
                -p 55000:5432 \
                postgres:15
            sleep 5
        fi
    fi
    
    # 验证数据库连接
    echo "🔍 验证数据库连接..."
    if docker exec postgres-dev psql -U postgres -d gourdboat_dev -c "SELECT version();" 2>/dev/null; then
        echo "✅ PostgreSQL开发环境设置成功！"
        echo ""
        echo "数据库配置信息："
        echo "  数据库地址: localhost:55000"
        echo "  数据库名称: gourdboat_dev"
        echo "  用户名: postgres"
        echo "  密码: postgrespw"
        echo ""
        echo "🐳 Docker容器管理："
        echo "  停止容器: docker stop postgres-dev"
        echo "  启动容器: docker start postgres-dev"
        echo "  删除容器: docker rm postgres-dev"
        echo ""
        echo "您现在可以启动应用程序了！"
    else
        echo "❌ 数据库连接失败，请检查Docker容器状态"
        echo ""
        echo "🔍 调试信息："
        docker ps -a | grep postgres-dev
        docker logs postgres-dev --tail 10
        exit 1
    fi
    
else
    # 本地PostgreSQL安装
    echo "💻 使用本地PostgreSQL安装..."
    
    # 检查PostgreSQL是否已安装
    if ! command -v psql &> /dev/null; then
        echo "❌ PostgreSQL未安装。请先安装PostgreSQL："
        echo "  macOS: brew install postgresql"
        echo "  Ubuntu: sudo apt-get install postgresql postgresql-contrib"
        echo "  CentOS/RHEL: sudo yum install postgresql-server postgresql-contrib"
        echo ""
        echo "或者使用Docker运行PostgreSQL："
        echo "  docker run -d --name postgres-dev -e POSTGRES_PASSWORD=postgrespw -e POSTGRES_DB=gourdboat_dev -p 55000:5432 postgres:15"
        exit 1
    fi

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

    # 创建开发数据库
    echo "📝 创建开发数据库..."
    PGPASSWORD=postgrespw psql -h localhost -p 55000 -U postgres -c "CREATE DATABASE gourdboat_dev;" 2>/dev/null || echo "数据库可能已存在"

    # 验证数据库连接
    echo "🔍 验证数据库连接..."
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
        echo ""
        echo "💡 提示：确保PostgreSQL配置为监听端口55000"
        echo "  编辑 postgresql.conf 文件，设置: port = 55000"
        echo "  编辑 pg_hba.conf 文件，允许本地连接"
        exit 1
    fi
fi 