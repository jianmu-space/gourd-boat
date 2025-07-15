# 统一JWT方案：所有登录方式的完整统一

## 设计理念

为什么要为不同的登录方式设计不同的JWT？我们完全可以设计**统一的JWT TOKEN**：
- 传统密码登录生成统一格式的JWT
- OIDC授权成功后直接生成相同格式的JWT
- 绑定完成后只需要更新JWT中的用户信息
- 前端始终使用相同的token格式，无论何种登录方式

### 1. **用户体验统一**
- ✅ 前端始终使用相同格式的JWT
- ✅ 无需处理两种不同的token类型
- ✅ 绑定流程对用户透明

### 2. **系统设计简洁**
- ✅ 只有一种JWT格式
- ✅ 统一的token验证逻辑
- ✅ 减少了系统复杂性

### 3. **开发维护便利**
- ✅ 前端逻辑统一
- ✅ 后端验证逻辑统一
- ✅ 更少的边界条件处理

### 4. **安全性保障**
- 🔒 JWT自带签名验证
- 🔒 通过`userStatus`字段控制用户状态
- 🔒 绑定完成后自动更新用户信息

## 登录方式对比

### 之前的不统一方案 ❌

#### 传统密码登录
```java
// AuthServiceImpl - 基础JWT
String token = tokenProvider.generateToken(authentication);
// 返回的JWT: { "sub": "user123", "iat": ..., "exp": ... }
```

#### OIDC登录
```java
// AuthController - 丰富的JWT
String token = jwtTokenProvider.generateTokenWithClaims(claims);
// 返回的JWT: { "sub": "wx_123", "accountId": "...", "provider": "...", "userStatus": "..." }
```

**问题**：两种登录方式生成的JWT格式完全不同，前端需要处理两套逻辑。

### 现在的统一方案 ✅

#### 所有登录方式
```java
// 统一使用UnifiedJwtService
LoginResult result = unifiedJwtService.generateJwtFromAccount(account);
// 返回的JWT: { "sub": "identifier", "accountId": "...", "provider": "...", "userStatus": "...", "userId": "..." }
```

**优势**：无论密码登录还是OIDC登录，前端都收到相同格式的JWT。

## 技术实现

### 1. 统一JWT服务

```java
@Service
public class UnifiedJwtServiceImpl implements UnifiedJwtService {
    
    public LoginResult generateJwtFromAccount(Account account) {
        // 所有登录方式都使用这个统一的生成逻辑
        String token = jwtTokenProvider.generateUnifiedToken(
            account.getIdentifier(),                           // identifier
            account.getId().getValue(),                        // accountId
            account.getProvider().getValue(),                  // provider
            account.getType().name(),                          // accountType
            account.isPendingBind() ? "PENDING_BIND" : "ACTIVE", // userStatus
            account.isBound() ? account.getUserId().getValue() : null // userId
        );
        
        return new LoginResult(token, account.getIdentifier(), 
                              account.isPendingBind() ? "PENDING_BIND" : "USER");
    }
}
```

### 2. 传统登录改造

```java
// AuthServiceImpl - 现在也使用统一JWT
@Override
public LoginResult login(LoginCommand command) {
    // 1. 根据identifier查找Account
    Account account = findAccountByIdentifier(identifier);
    
    if (account != null) {
        // 2. 使用统一JWT服务
        return unifiedJwtService.generateJwtFromAccount(account);
    } else {
        // 3. 降级处理（保持兼容性）
        return generateTraditionalJwt(authentication);
    }
}
```

### 3. OIDC登录改造

```java
// AuthController - 现在也使用统一JWT
@GetMapping("/oidc/callback/{provider}")
public ResponseEntity<LoginResult> handleOidcCallback(...) {
    // 1. 处理OIDC授权
    Account account = oidcAccountService.findOrCreateAccount(oidcResult);
    
    // 2. 使用统一JWT服务
    LoginResult result = unifiedJwtService.generateJwtFromAccount(account);
    
    return ResponseEntity.ok(result);
}
```

### 4. 统一JWT生成器

```java
// OIDC授权成功后，直接生成统一格式的JWT
Map<String, Object> claims = new HashMap<>();
claims.put("accountId", account.getId().getValue());
claims.put("provider", provider);
claims.put("accountType", account.getType().name());
claims.put("userStatus", account.isPendingBind() ? "PENDING_BIND" : "ACTIVE");

// 如果已绑定，添加用户信息
if (account.isBound()) {
    claims.put("userId", account.getUserId().getValue());
}

String token = jwtTokenProvider.generateTokenWithClaims(
    account.getIdentifier(),  // subject使用账号标识符
    claims,                   // 统一的claims格式
    24 * 60 * 60 * 1000L     // 24小时过期时间
);
```

### 2. 统一JWT验证

```java
private String validateBindJwt(String token) {
    // 1. 验证JWT有效性
    if (!jwtTokenProvider.validateToken(token)) {
        throw new RuntimeException("无效的JWT令牌");
    }
    
    // 2. 获取claims并验证用户状态
    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
    String userStatus = claims.get("userStatus", String.class);
    
    if (!"PENDING_BIND".equals(userStatus)) {
        throw new RuntimeException("用户状态不正确，当前状态: " + userStatus);
    }
    
    // 3. 返回账号ID
    return claims.get("accountId", String.class);
}
```

### 3. 绑定完成后更新JWT

```java
// 绑定完成后，生成包含完整用户信息的JWT
Map<String, Object> claims = new HashMap<>();
claims.put("accountId", boundAccount.getId().getValue());
claims.put("provider", boundAccount.getProvider().getValue());
claims.put("accountType", boundAccount.getType().name());
claims.put("userStatus", "ACTIVE");  // 更新状态为ACTIVE
claims.put("userId", boundAccount.getUserId().getValue());  // 添加用户ID

String token = jwtTokenProvider.generateTokenWithClaims(
    boundAccount.getIdentifier(),
    claims,
    24 * 60 * 60 * 1000L
);
```

### 3. 新增的JwtTokenProvider方法

```java
/**
 * 生成包含自定义claims的JWT token
 */
public String generateTokenWithClaims(String subject, Map<String, Object> claims, long expirationMs) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
            .setSubject(subject)
            .addClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
}

/**
 * 获取JWT token中的所有claims
 */
public Claims getClaimsFromToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
}
```

## 安全流程

### 1. OIDC授权成功
```
用户完成微信授权 → 系统创建PENDING_BIND账号 → 生成统一格式的JWT（userStatus: PENDING_BIND）
```

### 2. 发送验证码
```
前端调用 /api/auth/oidc/send-code
→ 验证JWT有效性和userStatus
→ 提取账号信息
→ 发送短信验证码
```

### 3. 绑定手机号
```
前端调用 /api/auth/oidc/bind-phone
→ 验证JWT有效性和userStatus
→ 验证手机号验证码
→ 绑定账号到用户
→ 生成更新后的JWT（userStatus: ACTIVE，包含userId）
```

## 安全特性

### 1. **JWT的内置安全性**
- 签名验证防止篡改
- 过期时间自动处理
- 自包含，无需查询数据库

### 2. **类型验证**
- 通过`type: "PENDING_BIND"`确保token只用于绑定
- 防止普通JWT被误用

### 3. **短期有效性**
- 10分钟有效期，减少攻击窗口
- 自动过期，无需手动清理

### 4. **双重验证**
- JWT验证确保来源合法
- 验证码验证确保手机号归属

## 与原方案的对比

| 特性 | BindToken方案 | JWT方案 |
|------|---------------|---------|
| 数据库依赖 | 需要额外表 | 无需额外表 |
| 清理任务 | 需要定期清理 | 自动过期 |
| 系统复杂度 | 较高 | 较低 |
| 安全性 | 依赖数据库验证 | JWT内置验证 |
| 性能 | 需要数据库查询 | 无需查询 |
| 扩展性 | 需要额外管理 | 自然扩展 |

## API变化

### 请求参数
```json
// 发送验证码
{
  "bindToken": "eyJhbGciOiJIUzM4NCJ9...",  // JWT格式
  "countryCode": "86",
  "phoneNumber": "13800138000"
}

// 绑定手机号
{
  "bindToken": "eyJhbGciOiJIUzM4NCJ9...",  // JWT格式
  "countryCode": "86",
  "phoneNumber": "13800138000",
  "nickname": "用户昵称",
  "verificationCode": "123456"
}
```

### 响应格式

#### OIDC授权成功后（PENDING_BIND状态）
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",  // 统一格式的JWT
  "identifier": "wx_openid_123456",
  "role": "PENDING_BIND"
}
```

#### JWT Claims结构（PENDING_BIND状态）
```json
{
  "sub": "wx_openid_123456",
  "accountId": "account-uuid-123",
  "provider": "WECHAT_MP",
  "accountType": "EXTERNAL",
  "userStatus": "PENDING_BIND",
  "iat": 1234567890,
  "exp": 1234654290
}
```

#### 绑定完成后（ACTIVE状态）
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",  // 更新后的JWT
  "identifier": "wx_openid_123456",
  "role": "USER"
}
```

#### JWT Claims结构（ACTIVE状态）
```json
{
  "sub": "wx_openid_123456",
  "accountId": "account-uuid-123",
  "provider": "WECHAT_MP",
  "accountType": "EXTERNAL",
  "userStatus": "ACTIVE",
  "userId": "user-uuid-456",
  "iat": 1234567890,
  "exp": 1234654290
}
```

## 统一效果对比

### 登录方式的JWT格式

无论使用哪种登录方式，前端都收到相同格式的JWT：

#### 密码登录
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "identifier": "user@example.com",
  "role": "USER"
}
```

#### 微信OIDC登录（已绑定）
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "identifier": "wx_openid_123456",
  "role": "USER"
}
```

#### 微信OIDC登录（待绑定）
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "identifier": "wx_openid_123456",
  "role": "PENDING_BIND"
}
```

### JWT Claims结构（所有登录方式统一）

```json
{
  "sub": "identifier",
  "accountId": "account-uuid-123",
  "provider": "PASSWORD|WECHAT_MP|WECHAT_MINIAPP|GOOGLE|GITHUB",
  "accountType": "INTERNAL|EXTERNAL",
  "userStatus": "ACTIVE|PENDING_BIND",
  "userId": "user-uuid-456",  // 仅当userStatus为ACTIVE时存在
  "iat": 1234567890,
  "exp": 1234654290
}
```

## 总结

**完整统一JWT方案**是最优雅的解决方案，核心优势：

### 🎯 **用户体验优势**
1. **前端逻辑统一** - 无论何种登录方式，始终使用相同格式的JWT
2. **无缝的状态转换** - 绑定前后用户无感知
3. **一致的API响应** - 所有登录接口返回相同格式

### 🏗️ **系统架构优势**
1. **单一JWT格式** - 避免多种不同token的复杂性
2. **统一验证逻辑** - 减少代码分支和维护成本
3. **统一服务设计** - UnifiedJwtService处理所有登录方式
4. **状态驱动设计** - 通过`userStatus`字段优雅控制流程

### 🔒 **安全性优势**
1. **JWT内置验证** - 签名保护，防篡改
2. **状态精确控制** - 通过`userStatus`字段精确控制用户状态
3. **信息递进式补全** - 绑定后自动添加完整用户信息
4. **统一安全策略** - 所有登录方式使用相同的安全机制

### 💡 **核心设计思想**
> **为什么要为不同登录方式搞不同的token？为什么不设计统一的JWT TOKEN？**

这个方案完美体现了软件设计的核心原则：
- **统一性** - 一种JWT格式处理所有登录场景
- **简洁性** - 减少不必要的复杂性
- **可维护性** - 更少的代码，更清晰的逻辑
- **可扩展性** - 新增登录方式时无需修改前端

**统一JWT方案是所有登录方式的最佳实践**，不仅解决了安全问题，还实现了：
- 前端开发的简化
- 后端代码的统一
- 用户体验的一致性
- 系统维护的便利性

这是一个真正**以用户为中心**的设计方案！ 