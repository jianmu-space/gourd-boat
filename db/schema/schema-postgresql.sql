-- 账号表：存储所有用户的认证账号信息（支持多种认证方式）
CREATE TABLE IF NOT EXISTS boat_account (
    id           VARCHAR(36)  PRIMARY KEY,
    user_id      VARCHAR(36)  NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    provider     VARCHAR(20)  NOT NULL,
    identifier   VARCHAR(100) NOT NULL,
    password     VARCHAR(255) NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 为PostgreSQL表添加注释
COMMENT ON TABLE boat_account IS '账号表，支持内部和外部多种认证方式，每个账号唯一标识一个登录方式';
COMMENT ON COLUMN boat_account.id IS '账号ID，UUID';
COMMENT ON COLUMN boat_account.user_id IS '关联的用户ID，UUID（逻辑外键）';
COMMENT ON COLUMN boat_account.type IS '账号类型（INTERNAL-内部账号，EXTERNAL-外部账号）';
COMMENT ON COLUMN boat_account.provider IS '认证服务商（如PASSWORD、GOOGLE、GITHUB、WECHAT等）';
COMMENT ON COLUMN boat_account.identifier IS '账号标识（如邮箱、手机号、第三方ID等，唯一）';
COMMENT ON COLUMN boat_account.password IS '账号密码（BCrypt等加密存储，仅内部账号使用）';
COMMENT ON COLUMN boat_account.status IS '账号状态（如ACTIVE、INACTIVE、LOCKED等）';
COMMENT ON COLUMN boat_account.created_at IS '创建时间';
COMMENT ON COLUMN boat_account.updated_at IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_boat_account_user_id ON boat_account(user_id);
CREATE INDEX IF NOT EXISTS idx_boat_account_provider_identifier ON boat_account(provider, identifier);

-- OIDC服务商注册表（支持动态注册）
CREATE TABLE IF NOT EXISTS boat_oidc_provider_registry (
    provider_code VARCHAR(50) PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    provider_group VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE boat_oidc_provider_registry IS 'OIDC服务商注册表，支持动态注册和管理服务商';
COMMENT ON COLUMN boat_oidc_provider_registry.provider_code IS '服务商代码';
COMMENT ON COLUMN boat_oidc_provider_registry.provider_name IS '服务商名称';
COMMENT ON COLUMN boat_oidc_provider_registry.description IS '描述（支持500字符）';
COMMENT ON COLUMN boat_oidc_provider_registry.provider_group IS '服务商分组';
COMMENT ON COLUMN boat_oidc_provider_registry.enabled IS '是否启用';
COMMENT ON COLUMN boat_oidc_provider_registry.created_at IS '创建时间';
COMMENT ON COLUMN boat_oidc_provider_registry.updated_at IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_oidc_provider_registry_group_enabled ON boat_oidc_provider_registry(provider_group, enabled);

-- OIDC服务商配置表（支持动态服务商，使用逻辑外键）
CREATE TABLE IF NOT EXISTS boat_oidc_provider_config (
    config_id VARCHAR(100) PRIMARY KEY,
    provider_code VARCHAR(50) NOT NULL,
    provider_name VARCHAR(100) NOT NULL,
    client_id VARCHAR(200) NOT NULL,
    client_secret VARCHAR(500) NOT NULL,
    issuer_url VARCHAR(500),
    authorization_endpoint VARCHAR(500),
    token_endpoint VARCHAR(500),
    user_info_endpoint VARCHAR(500),
    jwks_uri VARCHAR(500),
    scope VARCHAR(200),
    enabled BOOLEAN NOT NULL DEFAULT true,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE boat_oidc_provider_config IS 'OIDC服务商配置表，支持动态服务商配置，使用逻辑外键保证数据一致性';
COMMENT ON COLUMN boat_oidc_provider_config.config_id IS '配置ID';
COMMENT ON COLUMN boat_oidc_provider_config.provider_code IS '关联的服务商代码（逻辑外键）';
COMMENT ON COLUMN boat_oidc_provider_config.provider_name IS '服务商名称';
COMMENT ON COLUMN boat_oidc_provider_config.client_id IS '客户端ID';
COMMENT ON COLUMN boat_oidc_provider_config.client_secret IS '客户端密钥';
COMMENT ON COLUMN boat_oidc_provider_config.issuer_url IS '发行者URL';
COMMENT ON COLUMN boat_oidc_provider_config.authorization_endpoint IS '授权端点';
COMMENT ON COLUMN boat_oidc_provider_config.token_endpoint IS '令牌端点';
COMMENT ON COLUMN boat_oidc_provider_config.user_info_endpoint IS '用户信息端点';
COMMENT ON COLUMN boat_oidc_provider_config.jwks_uri IS 'JWKS URI';
COMMENT ON COLUMN boat_oidc_provider_config.scope IS '授权范围';
COMMENT ON COLUMN boat_oidc_provider_config.enabled IS '是否启用';
COMMENT ON COLUMN boat_oidc_provider_config.description IS '配置描述（支持500字符）';
COMMENT ON COLUMN boat_oidc_provider_config.created_at IS '创建时间';
COMMENT ON COLUMN boat_oidc_provider_config.updated_at IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_oidc_provider_config_provider_enabled ON boat_oidc_provider_config(provider_code, enabled);

-- 插入预定义的OIDC服务商
INSERT INTO boat_oidc_provider_registry (
    provider_code, 
    provider_name, 
    description, 
    provider_group, 
    enabled
) VALUES 
('WECHAT_MINIAPP', '微信小程序', '微信小程序登录', 'WECHAT', true),
('WECHAT_MP', '微信公众号', '微信公众号OAuth', 'WECHAT', true)
ON CONFLICT (provider_code) DO UPDATE SET
    provider_name = EXCLUDED.provider_name,
    description = EXCLUDED.description,
    provider_group = EXCLUDED.provider_group,
    enabled = EXCLUDED.enabled,
    updated_at = CURRENT_TIMESTAMP;

-- 插入示例OIDC服务商配置
INSERT INTO boat_oidc_provider_config (
    config_id, 
    provider_code, 
    provider_name, 
    client_id, 
    client_secret, 
    enabled, 
    description
) VALUES 
('wechat_miniapp_001', 'WECHAT_MINIAPP', '微信小程序1', 'wx1234567890abcdef', 'secret1234567890abcdef', true, '主要微信小程序'),
('wechat_mp_001', 'WECHAT_MP', '微信公众号1', 'wx1234567890abcdef', 'secret1234567890abcdef', true, '主要微信公众号')
ON CONFLICT (config_id) DO UPDATE SET
    provider_code = EXCLUDED.provider_code,
    provider_name = EXCLUDED.provider_name,
    client_id = EXCLUDED.client_id,
    client_secret = EXCLUDED.client_secret,
    enabled = EXCLUDED.enabled,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP; 