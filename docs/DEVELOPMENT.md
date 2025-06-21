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

### 2. PostgreSQL 数据库设置

开发环境需要PostgreSQL数据库，可以使用以下方式之一：

#### 方式一：使用Docker（推荐）
```bash
./scripts/setup-postgresql-dev.sh
```
这个脚本会：
- 自动检测Docker环境
- 启动PostgreSQL容器（端口55000）
- 创建开发数据库 `gourdboat_dev`
- 验证数据库连接

**注意**：如果网络连接有问题无法下载Docker镜像，脚本会自动提示使用本地安装。

#### 方式二：本地PostgreSQL安装（网络受限时推荐）
```bash
./scripts/setup-postgresql-dev-simple.sh
```
这个脚本会：
- 检查本地PostgreSQL安装
- 启动PostgreSQL服务
- 创建开发数据库和用户
- 自动检测端口配置

#### 方式三：手动设置
1. 安装PostgreSQL
2. 配置监听端口55000
3. 创建数据库 `gourdboat_dev`
4. 运行设置脚本验证连接

### 3. 手动设置

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
| `SPRING_DATASOURCE_URL` | 数据库连接URL | jdbc:postgresql://localhost:55000/gourdboat_dev |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | postgres |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | postgrespw |
| `SERVER_PORT` | 服务器端口 | 8080 |
| `SPRING_PROFILES_ACTIVE` | Spring激活的profile | dev |

## 🔧 开发配置特性

### PostgreSQL 数据库
- 开发环境使用PostgreSQL数据库
- 默认连接：`jdbc:postgresql://localhost:55000/gourdboat_dev`
- 用户名：`postgres`
- 密码：`postgrespw`
- 使用 `schema-postgresql.sql` 创建表结构
- 使用 `data-dev.sql` 初始化测试数据

### 日志配置
- 应用日志级别: DEBUG
- Spring Security日志: DEBUG
- SQL日志: 启用，包含参数绑定

### 数据库配置
- 使用PostgreSQL数据库
- 表结构通过SQL脚本初始化
- 显示格式化的SQL语句
- 支持SQL注释显示

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
4. **数据库安全**：确保PostgreSQL数据库有适当的访问控制

## 🐛 常见问题

### Q: JWT token验证失败
A: 检查 `.env.local` 中的 `JWT_SECRET` 是否正确设置

### Q: 数据库连接失败
A: 检查PostgreSQL服务是否运行，确认连接参数是否正确

### Q: Docker无法下载PostgreSQL镜像
A: 
1. 检查网络连接
2. 使用本地PostgreSQL安装：`./scripts/setup-postgresql-dev-simple.sh`
3. 手动下载镜像：`docker pull postgres:15`
4. 使用其他PostgreSQL版本：`docker pull postgres:14`

### Q: 表结构初始化失败
A: 确认 `db/schema/schema-postgresql.sql` 文件存在且语法正确

### Q: PostgreSQL端口冲突
A: 
1. 检查端口55000是否被占用：`lsof -i :55000`
2. 使用默认端口5432，并更新 `application-dev.yml` 中的数据库URL
3. 停止占用端口的服务或更换端口

## 📝 开发工作流

1. 运行环境设置脚本：`./scripts/setup-dev-env.sh`
2. 设置PostgreSQL数据库：`./scripts/setup-postgresql-dev.sh`
3. 启动应用：`./gradlew bootRun`
4. 使用数据库客户端连接PostgreSQL查看数据
5. 使用API测试工具测试接口
6. 查看日志调试问题

## 🔄 重新初始化环境

如果需要重新生成配置：
```bash
rm .env.local
./scripts/setup-dev-env.sh
```

## 📊 测试环境说明

**注意**：测试环境使用H2内存数据库，与开发环境不同：
- 测试环境：H2内存数据库（`application-test.yml`）
- 开发环境：PostgreSQL数据库（`application-dev.yml`） 