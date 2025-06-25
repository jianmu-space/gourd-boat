-- 开发环境测试数据
-- 添加测试账号 (密码: password123)

INSERT INTO boat_account (id, user_id, type, provider, identifier, password, status, created_at, updated_at)
VALUES (
    'dev-test-account-1', 
    'dev-test-user-1', 
    'INTERNAL', 
    'PASSWORD', 
    'testuser', 
    '$2a$10$BOdCPbr/qouAbXbvWfuY2el7zVp9d.VdP1A9Z9DRjHLbaHnaai/9e', -- password123
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

INSERT INTO boat_account (id, user_id, type, provider, identifier, password, status, created_at, updated_at)
VALUES (
    'dev-test-account-2', 
    'dev-test-user-2', 
    'INTERNAL', 
    'PASSWORD', 
    'admin', 
    '$2a$10$BOdCPbr/qouAbXbvWfuY2el7zVp9d.VdP1A9Z9DRjHLbaHnaai/9e', -- password123
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

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
    description,
    created_at,
    updated_at
) VALUES 
('wechat_miniapp_001', 'WECHAT_MINIAPP', '微信小程序1', 'wx1234567890abcdef', 'secret1234567890abcdef', true, '主要微信小程序', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('wechat_mp_001', 'WECHAT_MP', '微信公众号1', 'wx1234567890abcdef', 'secret1234567890abcdef', true, '主要微信公众号', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (config_id) DO UPDATE SET
    provider_code = EXCLUDED.provider_code,
    provider_name = EXCLUDED.provider_name,
    client_id = EXCLUDED.client_id,
    client_secret = EXCLUDED.client_secret,
    enabled = EXCLUDED.enabled,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP; 