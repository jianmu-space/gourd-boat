package space.jianmu.gourdboat.domain.account;

/**
 * 账号类型
 * 定义不同的账号类型
 */
public enum AccountType {
    INTERNAL,   // 内部账号(使用密码认证)
    EXTERNAL    // 外部账号(使用OIDC、微信等认证)
} 