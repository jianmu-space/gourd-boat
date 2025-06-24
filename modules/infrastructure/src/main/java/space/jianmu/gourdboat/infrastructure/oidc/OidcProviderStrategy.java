package space.jianmu.gourdboat.infrastructure.oidc;

import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.oidc.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.application.oidc.dto.OidcUserInfo;
import space.jianmu.gourdboat.domain.oidc.OidcProviderConfig;

/**
 * OIDC服务商策略接口
 * 定义不同服务商的具体实现
 */
public interface OidcProviderStrategy {
    
    /**
     * 生成授权URL
     */
    String generateAuthorizationUrl(OidcProviderConfig config, String state, String redirectUri);
    
    /**
     * 处理授权码回调
     */
    OidcAuthResult handleAuthorizationCode(OidcProviderConfig config, String code, String state, String redirectUri);
    
    /**
     * 验证ID Token
     */
    OidcTokenValidationResult validateIdToken(OidcProviderConfig config, String idToken);
    
    /**
     * 获取用户信息
     */
    OidcUserInfo getUserInfo(OidcProviderConfig config, String accessToken);
} 