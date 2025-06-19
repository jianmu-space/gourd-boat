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