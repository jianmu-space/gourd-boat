package space.jianmu.gourdboat.application.auth.dto;

import lombok.Value;
import lombok.Builder;

/**
 * OIDC认证结果
 */
@Value
@Builder
public class OidcAuthResult {
    String accessToken;         // 访问令牌
    String idToken;            // ID令牌
    String refreshToken;       // 刷新令牌
    String tokenType;          // 令牌类型
    Long expiresIn;            // 过期时间（秒）
    String scope;              // 授权范围
    String openId;             // 用户唯一标识
    String unionId;            // 用户联合标识
    String provider;           // 服务商代码
    OidcUserInfo userInfo;     // 用户信息
    boolean success;           // 是否成功
    String error;              // 错误信息
} 