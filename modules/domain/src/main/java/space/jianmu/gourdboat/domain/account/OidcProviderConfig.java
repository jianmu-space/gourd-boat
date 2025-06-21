package space.jianmu.gourdboat.domain.account;

import lombok.Value;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * OIDC服务商实例配置实体
 * 用于管理不同服务商的OIDC配置信息
 */
@Value
@Builder
public class OidcProviderConfig {
    String configId;                    // 配置ID
    String providerCode;                // 服务商代码
    String providerName;                // 服务商名称（如"微信公众号1"）
    String clientId;                    // 客户端ID
    String clientSecret;                // 客户端密钥
    String issuerUrl;                   // 发行者URL
    String authorizationEndpoint;       // 授权端点
    String tokenEndpoint;               // 令牌端点
    String userInfoEndpoint;            // 用户信息端点
    String jwksUri;                     // JWKS URI
    String scope;                       // 授权范围
    Boolean enabled;                    // 是否启用
    String description;                 // 配置描述
    LocalDateTime createdAt;            // 创建时间
    LocalDateTime updatedAt;            // 更新时间
} 