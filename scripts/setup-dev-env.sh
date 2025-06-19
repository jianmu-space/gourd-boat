#!/bin/bash

# 开发环境设置脚本
# 使用方法: ./scripts/setup-dev-env.sh

echo "🚀 设置开发环境..."

# 创建 scripts 目录（如果不存在）
mkdir -p scripts

# 生成新的JWT Secret
JWT_SECRET=$(openssl rand -base64 32)
echo "✅ 生成JWT Secret: $JWT_SECRET"

# 创建 .env.local 文件
cat > .env.local << EOF
# 开发环境配置文件
# 注意：此文件不会被提交到版本控制，包含敏感信息

# JWT 配置
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=86400000

# 数据库配置 (开发环境使用 H2)
SPRING_DATASOURCE_URL=jdbc:h2:mem:devdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# 日志级别
LOGGING_LEVEL_SPACE_JIANMU_GOURDBOAT=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# H2 控制台
SPRING_H2_CONSOLE_ENABLED=true

# JPA 配置
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop

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

# 数据库配置
SPRING_DATASOURCE_URL=jdbc:h2:mem:devdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# 日志级别
LOGGING_LEVEL_SPACE_JIANMU_GOURDBOAT=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# H2 控制台
SPRING_H2_CONSOLE_ENABLED=true

# JPA 配置
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop

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
echo "1. 运行应用: ./gradlew bootRun"
echo "2. 访问 H2 控制台: http://localhost:8080/h2-console"
echo "3. API 端点: http://localhost:8080/api/auth/login"
echo ""
echo "⚠️  注意：.env.local 文件包含敏感信息，不会被提交到版本控制" 