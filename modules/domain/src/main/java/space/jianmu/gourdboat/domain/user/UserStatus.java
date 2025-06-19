package space.jianmu.gourdboat.domain.user;

/**
 * 用户状态
 * 定义用户的生命周期状态
 */
public enum UserStatus {
    ACTIVE,     // 正常状态
    INACTIVE,   // 未激活(注册后未完成验证)
    LOCKED,     // 账号锁定(多次验证失败)
    DELETED     // 已删除
} 