package space.jianmu.gourdboat.domain.account;

/**
 * 账号状态
 */
public enum AccountStatus {
    ACTIVE,     // 正常
    INACTIVE,   // 未激活
    LOCKED,     // 已锁定
    DELETED     // 已删除
} 