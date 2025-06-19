-- H2数据库兼容的账号表结构 (测试专用)
CREATE TABLE boat_account (
    id           VARCHAR(36)  PRIMARY KEY,
    user_id      VARCHAR(36)  NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    provider     VARCHAR(20)  NOT NULL,
    identifier   VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_boat_account_user_id ON boat_account (user_id); 