# OIDC多服务商架构设计

## 概述

本文档描述了如何在项目中支持多个OIDC服务商（如微信小程序、微信公众号、淘宝、钉钉等），每个服务商可能有多个实例的架构设计方案。

## 核心概念

### 领域模型
- `AuthProvider`: 认证服务商枚举，支持动态扩展
- `OidcConfig`: OIDC配置实体，包含服务商的配置信息
- `OidcConfigRepository`: OIDC配置仓储接口
- `ProviderRegistry`: 服务商注册表，管理可用的服务商

### 应用服务
- `OidcService`: OIDC认证服务接口
- `OidcConfigService`: OIDC配置管理服务

### 基础设施
- 策略模式：`OidcProviderStrategy`接口和具体实现
- 动态策略工厂：`DynamicOidcStrategyFactory`
- 数据持久化：`OidcConfigEntity`, `OidcConfigRepositoryImpl`

## 数据库设计

### 服务商注册表 (boat_oidc_provider_registry)

```sql
CREATE TABLE IF NOT EXISTS boat_oidc_provider_registry (
    provider_code VARCHAR(50) PRIMARY KEY COMMENT '服务商代码',
    provider_name VARCHAR(100) NOT NULL COMMENT '服务商名称',
    provider_group VARCHAR(50) COMMENT '服务商分组',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    description VARCHAR(500) COMMENT '服务商描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_oidc_provider_registry_group_enabled (provider_group, enabled)
) COMMENT='OIDC服务商注册表';
```

### OIDC配置表 (boat_oidc_provider_config)

```sql
CREATE TABLE IF NOT EXISTS boat_oidc_provider_config (
    config_id VARCHAR(100) PRIMARY KEY COMMENT '配置ID',
    provider_code VARCHAR(50) NOT NULL COMMENT '服务商代码',
    provider_name VARCHAR(100) NOT NULL COMMENT '服务商名称（如"微信公众号1"）',
    client_id VARCHAR(200) NOT NULL COMMENT '客户端ID',
    client_secret VARCHAR(500) NOT NULL COMMENT '客户端密钥',
    issuer_url VARCHAR(500) COMMENT '发行者URL',
    authorization_endpoint VARCHAR(500) COMMENT '授权端点',
    token_endpoint VARCHAR(500) COMMENT '令牌端点',
    user_info_endpoint VARCHAR(500) COMMENT '用户信息端点',
    jwks_uri VARCHAR(500) COMMENT 'JWKS URI',
    scope VARCHAR(200) COMMENT '授权范围',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    description VARCHAR(500) COMMENT '配置描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_oidc_provider_config_provider_enabled (provider_code, enabled)
) COMMENT='OIDC服务商配置表';
```

### 示例数据

```sql
-- 注册微信系服务商
INSERT INTO boat_oidc_provider_registry (provider_code, provider_name, provider_group, description) VALUES
('WECHAT_MINIAPP', '微信小程序', 'WECHAT', '微信小程序OIDC认证'),
('WECHAT_MP', '微信公众号', 'WECHAT', '微信公众号OIDC认证'),
('WECOM', '企业微信', 'WECHAT', '企业微信OIDC认证');

-- 配置微信小程序实例
INSERT INTO boat_oidc_provider_config (
    config_id, provider_code, provider_name, client_id, client_secret, 
    authorization_endpoint, token_endpoint, user_info_endpoint, scope, description
) VALUES (
    'wechat_miniapp_001', 'WECHAT_MINIAPP', '微信小程序-测试环境', 
    'wx1234567890abcdef', 'secret1234567890abcdef', 
    'https://api.weixin.qq.com/sns/jscode2session', 
    'https://api.weixin.qq.com/sns/jscode2session', 
    'https://api.weixin.qq.com/sns/userinfo', 
    'snsapi_userinfo', '微信小程序测试环境配置'
);

-- 配置微信公众号实例
INSERT INTO boat_oidc_provider_config (
    config_id, provider_code, provider_name, client_id, client_secret, 
    authorization_endpoint, token_endpoint, user_info_endpoint, scope, description
) VALUES (
    'wechat_mp_001', 'WECHAT_MP', '微信公众号-生产环境', 
    'wx9876543210fedcba', 'secret9876543210fedcba', 
    'https://open.weixin.qq.com/connect/oauth2/authorize', 
    'https://api.weixin.qq.com/sns/oauth2/access_token', 
    'https://api.weixin.qq.com/sns/userinfo', 
    'snsapi_userinfo', '微信公众号生产环境配置'
);
```

## 核心接口设计

### OIDC服务商策略接口

```java
public interface OidcProviderStrategy {
    /**
     * 生成授权URL
     */
    String generateAuthorizationUrl(OidcConfig config, String state, String redirectUri);
    
    /**
     * 处理授权码
     */
    OidcAuthResult handleAuthorizationCode(OidcConfig config, String code, String state, String redirectUri);
    
    /**
     * 验证ID Token
     */
    OidcTokenValidationResult validateIdToken(OidcConfig config, String idToken);
    
    /**
     * 获取用户信息
     */
    OidcUserInfo getUserInfo(OidcConfig config, String accessToken);
}
```

### 动态策略工厂

```java
@Component
public class DynamicOidcStrategyFactory {
    
    private final Map<String, OidcProviderStrategy> strategies;
    
    public DynamicOidcStrategyFactory(List<OidcProviderStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                    strategy -> strategy.getClass().getSimpleName().replace("Strategy", "").toUpperCase(),
                    strategy -> strategy
                ));
    }
    
    public OidcProviderStrategy getStrategy(String providerCode) {
        OidcProviderStrategy strategy = strategies.get(providerCode);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerCode);
        }
        return strategy;
    }
}
```

## 实现示例

### 微信小程序策略实现

```java
@Component
public class WechatMiniAppStrategy implements OidcProviderStrategy {
    
    @Override
    public String generateAuthorizationUrl(OidcConfig config, String state, String redirectUri) {
        // 微信小程序使用code2session，不需要生成授权URL
        throw new UnsupportedOperationException("微信小程序不支持授权URL生成");
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(OidcConfig config, String code, String state, String redirectUri) {
        // 调用微信code2session接口
        WechatCode2SessionResponse response = callCode2Session(config, code);
        
        return OidcAuthResult.builder()
                .accessToken(response.getSessionKey())
                .openId(response.getOpenid())
                .unionId(response.getUnionid())
                .expiresIn(response.getExpiresIn())
                .build();
    }
    
    @Override
    public OidcTokenValidationResult validateIdToken(OidcConfig config, String idToken) {
        // 微信小程序使用session_key验证，这里简化处理
        return OidcTokenValidationResult.builder()
                .valid(true)
                .claims(Map.of("openid", idToken))
                .build();
    }
    
    @Override
    public OidcUserInfo getUserInfo(OidcConfig config, String accessToken) {
        // 微信小程序需要用户主动授权获取信息
        return OidcUserInfo.builder()
                .openId(accessToken) // 这里accessToken实际是openid
                .build();
    }
    
    private WechatCode2SessionResponse callCode2Session(OidcConfig config, String code) {
        // 实现微信code2session调用
        // ...
    }
}
```

## API接口设计

### 认证接口

```http
POST /api/auth/oidc/authorize
Content-Type: application/json

{
    "provider": "WECHAT_MINIAPP",
    "configId": "wechat_miniapp_001",
    "code": "wx_code_123",
    "state": "state_123"
}
```

### 配置管理接口

```http
GET /api/oidc/configs?provider=WECHAT_MINIAPP
GET /api/oidc/configs/wechat_miniapp_001
POST /api/oidc/configs
PUT /api/oidc/configs/wechat_miniapp_001
DELETE /api/oidc/configs/wechat_miniapp_001
```

## 扩展新服务商

### 1. 实现策略类

```java
@Component
public class TaobaoStrategy implements OidcProviderStrategy {
    // 实现淘宝OIDC认证逻辑
}
```

### 2. 注册服务商

```sql
INSERT INTO boat_oidc_provider_registry (provider_code, provider_name, provider_group, description) 
VALUES ('TAOBAO', '淘宝', 'ALIBABA', '淘宝OIDC认证');
```

### 3. 添加配置

```sql
INSERT INTO boat_oidc_provider_config (config_id, provider_code, provider_name, client_id, client_secret, ...) 
VALUES ('taobao_001', 'TAOBAO', '淘宝-测试环境', 'app_key', 'app_secret', ...);
```

## 优势

1. **灵活性**: 支持动态添加新服务商，无需修改代码
2. **可扩展性**: 每个服务商可以有多个配置实例
3. **可维护性**: 策略模式使代码结构清晰
4. **性能**: 支持配置级别的启用/禁用
5. **安全性**: 配置信息加密存储，支持密钥轮换 