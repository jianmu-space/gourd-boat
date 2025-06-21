# OIDC动态架构设计

## 概述

本文档描述了GourdBoat项目中支持多个OIDC服务商的动态架构设计，包括微信小程序、微信公众号、淘宝、钉钉等多个服务商，并且每个服务商可以对接多个实例。采用**动态注册机制**，支持在运行时添加新的服务商而无需修改代码。

## 架构设计

### 1. 核心组件

#### 1.1 领域层 (Domain Layer)
- `AuthProvider`: 认证服务商字符串常量，定义支持的服务商类型
- `OidcConfig`: OIDC配置实体，包含服务商的配置信息
- `OidcConfigRepository`: OIDC配置仓储接口
- `ProviderRegistry`: 服务商注册表，支持动态注册
- `OidcErrorCode`: 错误码枚举，定义标准错误码
- `OidcAuthenticationException`: 认证异常类，统一异常处理

#### 1.2 应用层 (Application Layer)
- `OidcService`: OIDC服务接口，定义OIDC认证的核心方法
- `OidcConfigService`: OIDC配置管理服务
- `OidcConfigValidator`: 配置验证器，验证配置完整性和有效性
- DTO类：`OidcAuthResult`, `OidcUserInfo`, `OidcTokenValidationResult`

#### 1.3 基础设施层 (Infrastructure Layer)
- `OidcServiceImpl`: OIDC服务实现
- `OidcProviderStrategy`: 服务商策略接口
- 具体策略实现：`WechatMiniAppStrategy`, `WechatMpStrategy`等
- `DynamicOidcStrategyFactory`: 动态策略工厂，基于Spring Bean管理
- `EncryptionService`: 加密服务，AES加密敏感信息
- `EncryptedStringConverter`: JPA加密转换器，自动加密/解密数据库字段

#### 1.4 接口层 (Interface Layer)
- `OidcController`: OIDC认证控制器，提供REST API
- `ProviderManagementController`: 服务商管理控制器
- `GlobalExceptionHandler`: 全局异常处理器，统一异常响应

### 2. 支持的服务商

| 服务商 | 代码 | 说明 | 支持多实例 | 分组 |
|--------|------|------|------------|------|
| 微信小程序 | WECHAT_MINIAPP | 微信小程序登录 | ✅ | WECHAT |
| 微信公众号 | WECHAT_MP | 微信公众号OAuth | ✅ | WECHAT |
| 企业微信 | WECOM | 企业微信登录 | ✅ | WECHAT |
| 淘宝 | TAOBAO | 淘宝开放平台 | ✅ | ALIBABA |
| 钉钉 | DINGTALK | 钉钉开放平台 | ✅ | ALIBABA |
| 支付宝 | ALIPAY | 支付宝开放平台 | ✅ | ALIBABA |
| QQ | QQ | QQ互联 | ✅ | TENCENT |
| 微博 | WEIBO | 微博开放平台 | ✅ | SINA |
| 抖音 | DOUYIN | 抖音开放平台 | ✅ | BYTEDANCE |
| 字节跳动 | BYTEDANCE | 字节跳动开放平台 | ✅ | BYTEDANCE |
| Google | GOOGLE | Google OAuth | ✅ | GOOGLE |
| GitHub | GITHUB | GitHub OAuth | ✅ | GITHUB |

## 安全架构

### 1. 敏感信息加密

#### 1.1 加密服务
```java
@Service
public class EncryptionService {
    @Value("${app.encryption.key}")
    private String encryptionKey;
    
    /**
     * AES加密敏感信息
     */
    public String encrypt(String plaintext) {
        // AES加密实现
    }
    
    /**
     * AES解密敏感信息
     */
    public String decrypt(String ciphertext) {
        // AES解密实现
    }
}
```

#### 1.2 自动加密字段
```java
@Entity
@Table(name = "boat_oidc_provider_config")
public class OidcProviderConfigEntity {
    
    @Column(name = "client_secret", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String clientSecret; // 自动加密存储
}
```

#### 1.3 配置方式
```yaml
app:
  encryption:
    key: ${ENCRYPTION_KEY:default-encryption-key-32-chars-long-for-dev}
```

### 2. 错误处理机制

#### 2.1 错误码体系
```java
public enum OidcErrorCode {
    // 认证相关错误 (OIDC_001-OIDC_099)
    INVALID_TOKEN("OIDC_001", "无效的访问令牌"),
    TOKEN_EXPIRED("OIDC_002", "访问令牌已过期"),
    TOKEN_SIGNATURE_INVALID("OIDC_003", "令牌签名无效"),
    INVALID_CLIENT("OIDC_004", "无效的客户端"),
    INVALID_CLIENT_SECRET("OIDC_005", "无效的客户端密钥"),
    
    // 服务商相关错误 (OIDC_101-OIDC_199)
    PROVIDER_NOT_FOUND("OIDC_101", "OIDC服务商未找到"),
    PROVIDER_DISABLED("OIDC_102", "OIDC服务商已禁用"),
    PROVIDER_CONFIG_INVALID("OIDC_103", "OIDC服务商配置无效"),
    
    // 网络相关错误 (OIDC_201-OIDC_299)
    NETWORK_ERROR("OIDC_201", "网络连接错误"),
    TIMEOUT_ERROR("OIDC_202", "请求超时"),
    SERVICE_UNAVAILABLE("OIDC_203", "服务不可用"),
    
    // 系统内部错误 (OIDC_500-OIDC_599)
    INTERNAL_ERROR("OIDC_500", "系统内部错误"),
    CONFIGURATION_ERROR("OIDC_501", "配置错误"),
    STRATEGY_NOT_FOUND("OIDC_502", "认证策略未找到");
}
```

#### 2.2 认证异常
```java
public class OidcAuthenticationException extends RuntimeException {
    private final OidcErrorCode errorCode;
    private final String provider;
    private final String details;
    
    public OidcAuthenticationException(OidcErrorCode errorCode, String provider) {
        super(String.format("[%s] %s: %s", provider, errorCode.getCode(), errorCode.getMessage()));
        this.errorCode = errorCode;
        this.provider = provider;
        this.details = null;
    }
}
```

#### 2.3 全局异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(OidcAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOidcAuthenticationException(OidcAuthenticationException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", getHttpStatus(e.getErrorCode()).value());
        response.put("error", "OIDC Authentication Error");
        response.put("errorCode", e.getErrorCodeString());
        response.put("message", e.getErrorMessage());
        response.put("provider", e.getProvider());
        
        if (e.getDetails() != null) {
            response.put("details", e.getDetails());
        }
        
        return ResponseEntity.status(getHttpStatus(e.getErrorCode())).body(response);
    }
}
```

### 3. 配置验证

#### 3.1 配置验证器
```java
@Component
public class OidcConfigValidator {
    
    /**
     * 验证OIDC配置的完整性和有效性
     */
    public ValidationResult validate(OidcProviderConfig config) {
        List<String> errors = new ArrayList<>();
        
        // 基本信息验证
        validateBasicInfo(config, errors);
        
        // URL格式验证
        validateUrls(config, errors);
        
        // 端点验证
        validateEndpoints(config, errors);
        
        // 范围验证
        validateScopes(config, errors);
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * 验证配置并抛出异常（如果验证失败）
     */
    public void validateAndThrow(OidcProviderConfig config) {
        ValidationResult result = validate(config);
        if (!result.isValid()) {
            throw new OidcAuthenticationException(
                OidcErrorCode.PROVIDER_CONFIG_INVALID,
                config.getProviderCode(),
                String.join("; ", result.getErrors())
            );
        }
    }
}
```

#### 3.2 验证内容
- **基本信息验证**: provider_code、client_id、client_secret等必填字段
- **URL格式验证**: 验证所有URL的格式正确性
- **端点安全性验证**: 生产环境强制使用HTTPS
- **Scope格式验证**: 验证授权范围的格式正确性

### 3. 数据库设计

#### 3.1 服务商注册表
```sql
CREATE TABLE IF NOT EXISTS boat_oidc_provider_registry (
    provider_code VARCHAR(50) PRIMARY KEY,      -- 服务商代码
    provider_name VARCHAR(100) NOT NULL,        -- 服务商名称
    description VARCHAR(500),                   -- 描述（支持500字符）
    provider_group VARCHAR(50) NOT NULL,        -- 分组
    enabled BOOLEAN NOT NULL DEFAULT true,      -- 是否启用
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_oidc_provider_registry_group (provider_group),
    INDEX idx_oidc_provider_registry_enabled (enabled)
) COMMENT='OIDC服务商注册表，支持动态注册和管理服务商';
```

#### 3.2 配置表结构
```sql
CREATE TABLE IF NOT EXISTS boat_oidc_provider_config (
    config_id VARCHAR(100) PRIMARY KEY,         -- 配置ID
    provider_code VARCHAR(50) NOT NULL,         -- 关联的服务商代码（逻辑外键）
    provider_name VARCHAR(100) NOT NULL,        -- 服务商名称
    client_id VARCHAR(200) NOT NULL,            -- 客户端ID
    client_secret VARCHAR(500) NOT NULL,        -- 客户端密钥（加密存储）
    issuer_url VARCHAR(500),                    -- 发行者URL
    authorization_endpoint VARCHAR(500),        -- 授权端点
    token_endpoint VARCHAR(500),                -- 令牌端点
    user_info_endpoint VARCHAR(500),            -- 用户信息端点
    jwks_uri VARCHAR(500),                      -- JWKS URI
    scope VARCHAR(200),                         -- 授权范围
    enabled BOOLEAN NOT NULL DEFAULT true,      -- 是否启用
    description VARCHAR(500),                   -- 配置描述（支持500字符）
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_oidc_config_provider (provider_code),
    INDEX idx_oidc_config_enabled (enabled),
    INDEX idx_oidc_config_provider_enabled (provider_code, enabled)
    -- 注意：使用逻辑外键，无物理外键约束
) COMMENT='OIDC配置表，支持动态服务商配置，使用逻辑外键保证数据一致性';
```

#### 3.3 配置示例
```sql
-- 微信小程序配置
INSERT INTO boat_oidc_provider_config (
    config_id, 
    provider_code, 
    provider_name, 
    client_id, 
    client_secret, 
    enabled, 
    description
) VALUES (
    'wechat_miniapp_001', 
    'WECHAT_MINIAPP', 
    '微信小程序1', 
    'wx1234567890abcdef', 
    'secret1234567890abcdef', 
    true, 
    '主要微信小程序'
);

-- 微信公众号配置
INSERT INTO boat_oidc_provider_config (
    config_id, 
    provider_code, 
    provider_name, 
    client_id, 
    client_secret, 
    enabled, 
    description
) VALUES (
    'wechat_mp_001', 
    'WECHAT_MP', 
    '微信公众号1', 
    'wx1234567890abcdef', 
    'secret1234567890abcdef', 
    true, 
    '主要微信公众号'
);
```

### 4. 策略模式实现

#### 4.1 策略接口
```java
public interface OidcProviderStrategy {
    String getProviderCode();  // 返回服务商代码
    String generateAuthorizationUrl(OidcConfig config, String state, String redirectUri);
    OidcAuthResult handleAuthorizationCode(OidcConfig config, String code, String state, String redirectUri);
    OidcTokenValidationResult validateIdToken(OidcConfig config, String idToken);
    OidcUserInfo getUserInfo(OidcConfig config, String accessToken);
}
```

#### 4.2 Spring驱动的策略工厂
```java
@Component
public class DynamicOidcStrategyFactory {
    
    @Autowired
    private Map<String, OidcProviderStrategy> strategyMap;
    
    // 根据provider_code自动获取策略
    public OidcProviderStrategy getStrategy(String providerCode) {
        OidcProviderStrategy strategy = strategyMap.get(providerCode);
        if (strategy == null) {
            throw new OidcAuthenticationException(
                OidcErrorCode.STRATEGY_NOT_FOUND,
                providerCode,
                "策略未找到: " + providerCode
            );
        }
        return strategy;
    }
    
    // 获取所有可用策略
    public Map<String, OidcProviderStrategy> getAllStrategies() {
        return new HashMap<>(strategyMap);
    }
}
```

#### 4.3 具体策略实现
- `WechatMiniAppStrategy`: 微信小程序策略（使用code2Session）
- `WechatMpStrategy`: 微信公众号策略（使用OAuth2.0）
- `TaobaoStrategy`: 淘宝策略
- `DingtalkStrategy`: 钉钉策略

### 5. API接口

#### 5.1 生成授权URL
```
GET /api/oidc/auth/{provider}?configId={configId}&state={state}&redirectUri={redirectUri}
```

#### 5.2 处理授权回调
```
GET /api/oidc/callback/{provider}?configId={configId}&code={code}&state={state}&redirectUri={redirectUri}
```

#### 5.3 验证ID Token
```
POST /api/oidc/validate/{provider}?configId={configId}
Content-Type: application/json

{
    "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 5.4 获取用户信息
```
GET /api/oidc/userinfo/{provider}?configId={configId}
Authorization: Bearer {accessToken}
```

#### 5.5 服务商管理
```
GET /api/oidc/providers - 获取所有服务商
POST /api/oidc/providers - 注册新服务商
PUT /api/oidc/providers/{code} - 更新服务商
DELETE /api/oidc/providers/{code} - 删除服务商
```

#### 5.6 错误响应格式
```json
{
    "timestamp": "2025-06-21T17:30:00",
    "status": 401,
    "error": "OIDC Authentication Error",
    "errorCode": "OIDC_001",
    "message": "无效的访问令牌",
    "provider": "WECHAT_MP",
    "details": "Token signature verification failed"
}
```

### 6. 扩展机制

#### 6.1 添加新的OIDC服务商

1. **定义服务商常量**
```java
public class AuthProvider {
    public static final String NEW_PROVIDER = "NEW_PROVIDER";
}
```

2. **实现策略类**
```java
@Component
public class NewProviderStrategy implements OidcProviderStrategy {
    @Override
    public String getProviderCode() {
        return AuthProvider.NEW_PROVIDER;
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(OidcConfig config, String code, String state, String redirectUri) {
        try {
            // 实现具体的认证逻辑
            return result;
        } catch (Exception e) {
            throw new OidcAuthenticationException(
                OidcErrorCode.INTERNAL_ERROR,
                config.getProviderCode(),
                "处理授权码失败: " + e.getMessage(),
                e
            );
        }
    }
    
    // 实现其他方法...
}
```

3. **注册策略**
```java
@Configuration
public class OidcStrategyConfig {
    
    @PostConstruct
    public void registerStrategies() {
        // 策略会自动注册到Spring容器中
        log.info("OIDC策略注册完成，共注册 {} 个策略", strategyFactory.getAllStrategies().size());
    }
}
```

4. **添加数据库配置**
```sql
-- 注册服务商
INSERT INTO boat_oidc_provider_registry (
    provider_code, provider_name, description, provider_group, enabled
) VALUES (
    'NEW_PROVIDER', 'New Provider', '新服务商描述', 'CUSTOM', true
);

-- 添加配置
INSERT INTO boat_oidc_provider_config (
    config_id, provider_code, provider_name, client_id, client_secret,
    authorization_endpoint, token_endpoint, enabled
) VALUES (
    'new-config-123', 'NEW_PROVIDER', 'New Provider Config', 'client-id', 'client-secret',
    'https://provider.com/oauth/authorize', 'https://provider.com/oauth/token', true
);
```

### 7. 监控和日志

#### 7.1 日志记录
- OIDC认证过程的详细日志
- 错误异常记录
- 配置变更日志
- 策略注册日志

#### 7.2 错误监控
- 详细的错误码统计
- 服务商级别的错误分析
- 异常堆栈信息记录
- 性能监控指标

### 8. 部署配置

#### 8.1 环境变量配置
```bash
# 加密密钥（生产环境必须设置）
export ENCRYPTION_KEY="your-secure-encryption-key-32-chars-long"

# JWT密钥
export JWT_SECRET="your-jwt-secret-key"

# 数据库配置
export DB_URL="jdbc:postgresql://localhost:5432/gourdboat"
export DB_USERNAME="gourdboat"
export DB_PASSWORD="your-db-password"
```

#### 8.2 配置文件
```yaml
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000
  encryption:
    key: ${ENCRYPTION_KEY:default-encryption-key-32-chars-long-for-dev}

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 9. 安全最佳实践

#### 9.1 密钥管理
- 使用环境变量管理敏感密钥
- 定期轮换加密密钥
- 不同环境使用不同的密钥

#### 9.2 配置安全
- 敏感配置信息加密存储
- 配置验证确保完整性
- 生产环境强制使用HTTPS

#### 9.3 错误处理
- 不泄露敏感信息在错误消息中
- 统一的错误响应格式
- 详细的错误日志记录

#### 9.4 访问控制
- 验证客户端身份
- 检查访问权限
- 监控异常访问行为

## 总结

动态OIDC架构具有以下优势：

1. **高扩展性**: 支持运行时动态添加新的OIDC服务商
2. **强安全性**: 敏感信息加密、配置验证、统一错误处理
3. **易维护性**: 策略模式、分层架构、模块化设计
4. **高性能**: 缓存策略、连接池、异步处理
5. **可监控**: 详细日志、错误统计、性能指标

通过这种设计，系统能够安全、高效、灵活地支持多个OIDC服务商的认证需求，同时保持良好的可维护性和扩展性。 