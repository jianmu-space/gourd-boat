package space.jianmu.gourdboat.domain.account;

/**
 * OIDC错误码枚举
 */
public enum OidcErrorCode {
    
    // 认证相关错误
    INVALID_TOKEN("OIDC_001", "无效的访问令牌"),
    TOKEN_EXPIRED("OIDC_002", "访问令牌已过期"),
    TOKEN_SIGNATURE_INVALID("OIDC_003", "令牌签名无效"),
    INVALID_CLIENT("OIDC_004", "无效的客户端"),
    INVALID_CLIENT_SECRET("OIDC_005", "无效的客户端密钥"),
    
    // 服务商相关错误
    PROVIDER_NOT_FOUND("OIDC_101", "OIDC服务商未找到"),
    PROVIDER_DISABLED("OIDC_102", "OIDC服务商已禁用"),
    PROVIDER_CONFIG_INVALID("OIDC_103", "OIDC服务商配置无效"),
    
    // 网络相关错误
    NETWORK_ERROR("OIDC_201", "网络连接错误"),
    TIMEOUT_ERROR("OIDC_202", "请求超时"),
    SERVICE_UNAVAILABLE("OIDC_203", "服务不可用"),
    
    // 用户信息相关错误
    USER_INFO_NOT_FOUND("OIDC_301", "用户信息未找到"),
    USER_INFO_INVALID("OIDC_302", "用户信息无效"),
    
    // 系统内部错误
    INTERNAL_ERROR("OIDC_500", "系统内部错误"),
    CONFIGURATION_ERROR("OIDC_501", "配置错误"),
    STRATEGY_NOT_FOUND("OIDC_502", "认证策略未找到");
    
    private final String code;
    private final String message;
    
    OidcErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", code, message);
    }
} 