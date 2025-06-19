-- 账号表：存储所有用户的认证账号信息（支持多种认证方式）
CREATE TABLE IF NOT EXISTS boat_account (
    id           VARCHAR(36)  PRIMARY KEY,
    user_id      VARCHAR(36)  NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    provider     VARCHAR(20)  NOT NULL,
    identifier   VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255),
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 为user_id创建索引
CREATE INDEX IF NOT EXISTS idx_boat_account_user_id ON boat_account (user_id);

-- 为provider和identifier组合创建索引（用于登录查询优化）
CREATE INDEX IF NOT EXISTS idx_boat_account_provider_identifier ON boat_account (provider, identifier);

-- 为PostgreSQL表添加注释
COMMENT ON TABLE boat_account IS '账号表，支持内部和外部多种认证方式，每个账号唯一标识一个登录方式';
COMMENT ON COLUMN boat_account.id IS '账号ID，UUID';
COMMENT ON COLUMN boat_account.user_id IS '关联的用户ID，UUID';
COMMENT ON COLUMN boat_account.type IS '账号类型（INTERNAL-内部账号，EXTERNAL-外部账号）';
COMMENT ON COLUMN boat_account.provider IS '认证服务商（如PASSWORD、GOOGLE、GITHUB、WECHAT等）';
COMMENT ON COLUMN boat_account.identifier IS '账号标识（如邮箱、手机号、第三方ID等，唯一）';
COMMENT ON COLUMN boat_account.password IS '账号密码（BCrypt等加密存储，仅内部账号使用）';
COMMENT ON COLUMN boat_account.status IS '账号状态（如ACTIVE、INACTIVE、LOCKED等）';
COMMENT ON COLUMN boat_account.created_at IS '创建时间';
COMMENT ON COLUMN boat_account.updated_at IS '更新时间'; 