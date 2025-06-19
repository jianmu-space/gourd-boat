-- 账号表：存储所有用户的认证账号信息（支持多种认证方式）
CREATE TABLE boat_account (
    id           VARCHAR(36)  PRIMARY KEY COMMENT '账号ID，UUID',
    user_id      VARCHAR(36)  NOT NULL COMMENT '关联的用户ID，UUID',
    type         VARCHAR(20)  NOT NULL COMMENT '账号类型（INTERNAL-内部账号，EXTERNAL-外部账号）',
    provider     VARCHAR(20)  NOT NULL COMMENT '认证服务商（如PASSWORD、GOOGLE、GITHUB、WECHAT等）',
    identifier   VARCHAR(100) NOT NULL UNIQUE COMMENT '账号标识（如邮箱、手机号、第三方ID等，唯一）',
    password     VARCHAR(255) NULL COMMENT '账号密码（BCrypt等加密存储，仅内部账号使用）',
    status       VARCHAR(20)  NOT NULL COMMENT '账号状态（如ACTIVE、INACTIVE、LOCKED等）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_boat_account_user_id (user_id)
) COMMENT='账号表，支持内部和外部多种认证方式，每个账号唯一标识一个登录方式';
