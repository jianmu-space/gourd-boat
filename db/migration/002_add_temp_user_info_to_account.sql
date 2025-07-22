-- 002_add_temp_user_info_to_account.sql
-- 为boat_account表添加临时存储用户信息的字段
-- 创建boat_user表

-- 为boat_account表添加临时用户信息字段
ALTER TABLE boat_account 
ADD COLUMN temp_nickname VARCHAR(100) NULL COMMENT '临时存储的昵称（OIDC账号待绑定状态时使用）',
ADD COLUMN temp_avatar VARCHAR(500) NULL COMMENT '临时存储的头像URL（OIDC账号待绑定状态时使用）';

-- 创建boat_user表
CREATE TABLE IF NOT EXISTS boat_user (
    id           VARCHAR(36)  PRIMARY KEY COMMENT '用户ID，UUID',
    phone_number VARCHAR(20)  NOT NULL COMMENT '手机号（唯一标识）',
    nickname     VARCHAR(100) NOT NULL COMMENT '用户昵称',
    avatar       VARCHAR(500) NULL COMMENT '头像URL',
    status       VARCHAR(20)  NOT NULL COMMENT '用户状态（ACTIVE、INACTIVE等）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_boat_user_phone_number (phone_number)
) COMMENT='用户表，存储用户基本信息，以手机号作为唯一标识'; 