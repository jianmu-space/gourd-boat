#!/bin/bash

# API测试脚本
echo "🧪 开始API测试..."

# 等待应用启动
echo "⏳ 等待应用启动..."
sleep 10

BASE_URL="http://localhost:8080"

# 测试健康检查
echo "🔍 测试应用状态..."
if curl -s "$BASE_URL" > /dev/null; then
    echo "✅ 应用正在运行"
else
    echo "❌ 应用未启动"
    exit 1
fi

# 测试H2控制台
echo "🔍 测试H2控制台..."
if curl -s "$BASE_URL/h2-console" | grep -q "H2"; then
    echo "✅ H2控制台可访问: $BASE_URL/h2-console"
else
    echo "⚠️  H2控制台可能需要配置"
fi

# 测试登录API（期待401因为没有用户）
echo "🔍 测试登录API..."
RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/login_response.json \
    -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"identifier":"testuser","password":"password123"}')

echo "响应状态码: $RESPONSE"
echo "响应内容:"
cat /tmp/login_response.json
echo ""

if [ "$RESPONSE" = "401" ]; then
    echo "✅ 登录API正常工作（返回401表示认证失败，这是预期的）"
elif [ "$RESPONSE" = "403" ]; then
    echo "⚠️  返回403，可能是安全配置问题"
elif [ "$RESPONSE" = "200" ]; then
    echo "✅ 登录成功！"
else
    echo "❌ 意外的响应状态码: $RESPONSE"
fi

echo ""
echo "🎯 测试总结:"
echo "- 应用访问: $BASE_URL"
echo "- H2控制台: $BASE_URL/h2-console"
echo "- 登录API: $BASE_URL/api/auth/login"
echo ""
echo "📝 注意: 如需测试登录，请先在H2控制台中添加测试用户数据" 