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

#### 1.2 应用层 (Application Layer)
- `OidcService`: OIDC服务接口，定义OIDC认证的核心方法
- `OidcConfigService`: OIDC配置管理服务
- DTO类：`OidcAuthResult`, `OidcUserInfo`, `OidcTokenValidationResult`

#### 1.3 基础设施层 (Infrastructure Layer)
- `OidcServiceImpl`: OIDC服务实现
- `OidcProviderStrategy`: 服务商策略接口
- 具体策略实现：`WechatMiniAppStrategy`, `WechatMpStrategy`等
- `DynamicOidcStrategyFactory`: 动态策略工厂，基于Spring Bean管理

#### 1.4 接口层 (Interface Layer)
- `OidcController`: OIDC认证控制器，提供REST API
- `ProviderManagementController`: 服务商管理控制器

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
    client_secret VARCHAR(500) NOT NULL,        -- 客户端密钥
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
        return strategyMap.get(providerCode);
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
GET /api/providers                    # 获取所有服务商
GET /api/providers/group/{group}      # 获取指定分组服务商
POST /api/providers/register          # 注册新服务商
GET /api/providers/{code}/exists      # 检查服务商是否存在
```

### 6. 使用示例

#### 6.1 微信小程序登录
```java
// 1. 前端获取code
String code = "wx_code_from_miniapp";

// 2. 后端处理登录
OidcAuthResult result = oidcService.handleAuthorizationCode(
    "WECHAT_MINIAPP", 
    "wechat_miniapp_001", 
    code, 
    "state", 
    "redirect_uri"
);

// 3. 获取用户信息
OidcUserInfo userInfo = result.getUserInfo();
```

#### 6.2 微信公众号登录
```java
// 1. 生成授权URL
String authUrl = oidcService.generateAuthorizationUrl(
    "WECHAT_MP", 
    "wechat_mp_001", 
    "state", 
    "https://example.com/callback"
);

// 2. 用户访问授权URL，获取code

// 3. 处理回调
OidcAuthResult result = oidcService.handleAuthorizationCode(
    "WECHAT_MP", 
    "wechat_mp_001", 
    code, 
    "state", 
    "https://example.com/callback"
);
```

### 7. 扩展性设计

#### 7.1 添加新服务商（无需代码修改）
```sql
-- 1. 注册新服务商
INSERT INTO boat_oidc_provider_registry (
    provider_code, 
    provider_name, 
    description, 
    provider_group, 
    enabled
) VALUES (
    'CUSTOM_PROVIDER', 
    '自定义服务商', 
    '自定义OIDC服务商', 
    'CUSTOM_GROUP', 
    true
);

-- 2. 添加配置
INSERT INTO boat_oidc_provider_config (
    config_id, 
    provider_code, 
    provider_name, 
    client_id, 
    client_secret, 
    enabled, 
    description
) VALUES (
    'custom_provider_001', 
    'CUSTOM_PROVIDER', 
    '自定义服务商1', 
    'client_id', 
    'client_secret', 
    true, 
    '自定义服务商配置'
);
```

#### 7.2 实现新策略（需要代码）
```java
@Component
public class CustomProviderStrategy implements OidcProviderStrategy {
    
    @Override
    public String getProviderCode() {
        return "CUSTOM_PROVIDER";
    }
    
    @Override
    public String generateAuthorizationUrl(OidcConfig config, String state, String redirectUri) {
        // 实现自定义授权URL生成逻辑
        return "https://custom-provider.com/oauth/authorize?...";
    }
    
    // 其他方法实现...
}
```

#### 7.3 添加新实例
1. 在数据库中添加新的配置记录
2. 使用不同的`configId`区分实例
3. 前端根据业务需求选择对应的配置

### 8. 架构优势

#### 8.1 动态扩展
- **无需修改代码**: 添加新服务商不需要修改枚举
- **运行时注册**: 支持应用运行时注册新服务商
- **配置驱动**: 通过数据库管理服务商

#### 8.2 Spring集成
- **自动Bean管理**: 策略类自动注册为Spring Bean
- **依赖注入**: 利用Spring的IoC容器管理策略
- **类型安全**: 编译时检查，避免运行时错误

#### 8.3 高性能设计
- **逻辑外键**: 无物理外键约束，提高性能
- **索引优化**: 复合索引支持高效查询
- **分库分表**: 支持大型项目架构需求

### 9. 安全考虑

1. **配置安全**: 客户端密钥等敏感信息需要加密存储
2. **状态验证**: 使用state参数防止CSRF攻击
3. **Token验证**: 验证ID Token的签名和有效期
4. **权限控制**: 不同配置实例可以有不同的权限范围
5. **数据验证**: 应用层验证保证逻辑外键的数据一致性

## 总结

这个架构设计提供了以下优势：

1. **灵活性**: 支持多个服务商和多个实例
2. **可扩展性**: 易于添加新的服务商和实例
3. **可维护性**: 清晰的层次结构和职责分离
4. **可测试性**: 接口和实现分离，便于单元测试
5. **安全性**: 完善的配置管理和安全措施 