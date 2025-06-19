# 开发环境配置指南

## 🚀 快速开始

### 1. 自动设置（推荐）

运行设置脚本来自动配置开发环境：

```bash
./scripts/setup-dev-env.sh
```

这个脚本会：
- 生成安全的JWT secret
- 创建 `.env.local` 配置文件
- 创建 `.env.example` 模板文件

### 2. 手动设置

如果你需要手动配置，可以：

1. 复制环境变量模板：
```bash
cp .env.example .env.local
```

2. 生成JWT secret：
```bash
openssl rand -base64 32
```

3. 编辑 `.env.local` 文件，填入生成的JWT secret

## 📋 环境变量说明

| 变量名 | 说明 | 默认值 |
|-------|------|-------|
| `JWT_SECRET` | JWT签名密钥（必须至少32字符） | 无 |
| `JWT_EXPIRATION` | JWT过期时间（毫秒） | 86400000 (24小时) |
| `SPRING_DATASOURCE_URL` | 数据库连接URL | jdbc:h2:mem:devdb |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | sa |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | 空 |
| `SERVER_PORT` | 服务器端口 | 8080 |
| `SPRING_PROFILES_ACTIVE` | Spring激活的profile | dev |

## 🔧 开发配置特性

### H2 数据库控制台
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:devdb`
- 用户名: `sa`
- 密码: 空

### 日志配置
- 应用日志级别: DEBUG
- Spring Security日志: DEBUG
- SQL日志: 启用，包含参数绑定

### 数据库配置
- 使用内存H2数据库
- 每次启动时重建表结构 (create-drop)
- 显示格式化的SQL语句

## 🏃‍♂️ 运行应用

### 使用Gradle
```bash
./gradlew bootRun
```

### 使用Gradle（显示指定环境）
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 使用IDE
1. 确保已安装JDK 17+
2. 设置环境变量 `SPRING_PROFILES_ACTIVE=dev`
3. 运行 `GourdBoatApplication.main()`

## 🧪 测试API

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

### 登录接口
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "password": "password123"
  }'
```

## 🔒 安全注意事项

1. **不要提交环境文件**：`.env.local` 已被添加到 `.gitignore`
2. **定期更换JWT secret**：建议每个项目使用不同的secret
3. **生产环境配置**：生产环境必须使用强密码和安全配置

## 🐛 常见问题

### Q: JWT token验证失败
A: 检查 `.env.local` 中的 `JWT_SECRET` 是否正确设置

### Q: H2控制台无法访问
A: 确认 `SPRING_H2_CONSOLE_ENABLED=true` 且应用使用dev profile

### Q: 数据库连接失败
A: 检查 `SPRING_DATASOURCE_URL` 配置是否正确

## 📝 开发工作流

1. 运行设置脚本：`./scripts/setup-dev-env.sh`
2. 启动应用：`./gradlew bootRun`
3. 访问H2控制台查看数据：http://localhost:8080/h2-console
4. 使用API测试工具测试接口
5. 查看日志调试问题

## 🔄 重新初始化环境

如果需要重新生成配置：
```bash
rm .env.local
./scripts/setup-dev-env.sh
``` 