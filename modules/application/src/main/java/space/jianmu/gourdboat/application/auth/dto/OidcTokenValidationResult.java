package space.jianmu.gourdboat.application.auth.dto;

import lombok.Value;
import lombok.Builder;
import java.util.Map;

/**
 * OIDC令牌验证结果
 */
@Value
@Builder
public class OidcTokenValidationResult {
    boolean valid;              // 是否有效
    String error;               // 错误信息
    OidcUserInfo userInfo;      // 用户信息（如果验证成功）
    String issuer;              // 发行者
    String audience;            // 受众
    Long issuedAt;              // 签发时间
    Long expiresAt;             // 过期时间
    Map<String, Object> claims; // 令牌声明
} 