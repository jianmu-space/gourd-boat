-- 账号表：存储所有用户的认证账号信息（支持多种认证方式）
CREATE TABLE IF NOT EXISTS boat_account (
    id           VARCHAR(36)  PRIMARY KEY COMMENT '账号ID，UUID',
    user_id      VARCHAR(36)  NOT NULL COMMENT '关联的用户ID，UUID（逻辑外键）',
    type         VARCHAR(20)  NOT NULL COMMENT '账号类型（INTERNAL-内部账号，EXTERNAL-外部账号）',
    provider     VARCHAR(20)  NOT NULL COMMENT '认证服务商（如PASSWORD、GOOGLE、GITHUB、WECHAT等）',
    identifier   VARCHAR(100) NOT NULL UNIQUE COMMENT '账号标识（如邮箱、手机号、第三方ID等，唯一）',
    password     VARCHAR(255) NULL COMMENT '账号密码（BCrypt等加密存储，仅内部账号使用）',
    status       VARCHAR(20)  NOT NULL COMMENT '账号状态（如ACTIVE、INACTIVE、LOCKED等）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_boat_account_user_id (user_id),
    INDEX idx_boat_account_provider_identifier (provider, identifier)
) COMMENT='账号表，支持内部和外部多种认证方式，每个账号唯一标识一个登录方式';

-- OIDC服务商注册表（支持动态注册）
CREATE TABLE IF NOT EXISTS boat_oidc_provider_registry (
    provider_code VARCHAR(50) PRIMARY KEY COMMENT '服务商代码',
    provider_name VARCHAR(100) NOT NULL COMMENT '服务商名称',
    description VARCHAR(500) COMMENT '描述（支持500字符）',
    provider_group VARCHAR(50) NOT NULL COMMENT '服务商分组',
    enabled BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_oidc_provider_registry_group_enabled (provider_group, enabled)
) COMMENT='OIDC服务商注册表，支持动态注册和管理服务商';

-- OIDC服务商配置表（支持动态服务商，使用逻辑外键）
CREATE TABLE IF NOT EXISTS boat_oidc_provider_config (
    config_id VARCHAR(100) PRIMARY KEY COMMENT '配置ID',
    provider_code VARCHAR(50) NOT NULL COMMENT '关联的服务商代码（逻辑外键）',
    provider_name VARCHAR(100) NOT NULL COMMENT '服务商名称',
    client_id VARCHAR(200) NOT NULL COMMENT '客户端ID',
    client_secret VARCHAR(500) NOT NULL COMMENT '客户端密钥',
    issuer_url VARCHAR(500) COMMENT '发行者URL',
    authorization_endpoint VARCHAR(500) COMMENT '授权端点',
    token_endpoint VARCHAR(500) COMMENT '令牌端点',
    user_info_endpoint VARCHAR(500) COMMENT '用户信息端点',
    jwks_uri VARCHAR(500) COMMENT 'JWKS URI',
    scope VARCHAR(200) COMMENT '授权范围',
    enabled BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用',
    description VARCHAR(500) COMMENT '配置描述（支持500字符）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_oidc_provider_config_provider_enabled (provider_code, enabled)
    -- 注意：使用逻辑外键，无物理外键约束
) COMMENT='OIDC服务商配置表，支持动态服务商配置，使用逻辑外键保证数据一致性';

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
ON DUPLICATE KEY UPDATE
    provider_name = VALUES(provider_name),
    description = VALUES(description),
    provider_group = VALUES(provider_group),
    enabled = VALUES(enabled),
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
ON DUPLICATE KEY UPDATE
    provider_code = VALUES(provider_code),
    provider_name = VALUES(provider_name),
    client_id = VALUES(client_id),
    client_secret = VALUES(client_secret),
    enabled = VALUES(enabled),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;
