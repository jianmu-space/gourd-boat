#!/bin/bash

# 开发环境设置脚本
# 使用方法: ./scripts/setup-dev-env.sh

echo "🚀 设置开发环境..."

# 创建 scripts 目录（如果不存在）
mkdir -p scripts

# 生成新的JWT Secret
JWT_SECRET=$(openssl rand -base64 32)
echo "✅ 生成JWT Secret: $JWT_SECRET"

# 生成新的ENCRYPTION_KEY（32位）
ENCRYPTION_KEY=$(openssl rand -base64 24 | cut -c1-32)
echo "✅ 生成ENCRYPTION_KEY: $ENCRYPTION_KEY"

# 创建 .env.local 文件
cat > .env.local << EOF
# 开发环境配置文件
# 注意：此文件不会被提交到版本控制，包含敏感信息

# JWT 配置
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=86400000

# 加密密钥
ENCRYPTION_KEY=$ENCRYPTION_KEY

# 数据库配置 (开发环境使用 PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:55000/gourdboat_dev
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgrespw

# 日志级别
LOGGING_LEVEL_SPACE_JIANMU_GOURDBOAT=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# JPA 配置
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_HIBERNATE_DDL_AUTO=none

# 服务器配置
SERVER_PORT=8080

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
EOF

echo "✅ 创建 .env.local 文件"

# 创建 .env.example 模板文件
cat > .env.example << EOF
# 环境变量配置模板
# 复制此文件为 .env.local 并填入实际值

# JWT 配置
JWT_SECRET=your-jwt-secret-here-minimum-32-characters
JWT_EXPIRATION=86400000

# 数据库配置 (PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:55000/gourdboat_dev
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgrespw

# 日志级别
LOGGING_LEVEL_SPACE_JIANMU_GOURDBOAT=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# JPA 配置
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_HIBERNATE_DDL_AUTO=none

# 服务器配置
SERVER_PORT=8080

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
EOF

echo "✅ 创建 .env.example 模板文件"

echo ""
echo "🎉 开发环境设置完成！"
echo ""
echo "📋 下一步操作："
echo "1. 确保 PostgreSQL 服务运行在端口 55000"
echo "2. 创建数据库: gourdboat_dev"
echo "3. 运行应用: ./gradlew bootRun"
echo "4. API 端点: http://localhost:8080/api/auth/login"
echo ""
echo "⚠️  注意：.env.local 文件包含敏感信息，不会被提交到版本控制"
echo ""
echo "💡 提示：如果使用 Docker 运行 PostgreSQL，可以参考 scripts/setup-postgresql-dev.sh" 