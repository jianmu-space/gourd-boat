package space.jianmu.gourdboat.domain.account;

/**
 * 认证服务商
 * 定义不同的身份提供商
 */
public enum AuthProvider {
    PASSWORD,   // 密码认证
    GOOGLE,     // Google账号
    GITHUB,     // GitHub账号
    WECHAT,     // 微信账号
    WECOM       // 企业微信账号
} 