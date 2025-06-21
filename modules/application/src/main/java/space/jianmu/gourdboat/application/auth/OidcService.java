package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.application.auth.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.auth.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.application.auth.dto.OidcUserInfo;

/**
 * OIDC服务接口
 * 定义OIDC认证的核心方法
 */
public interface OidcService {
    
    /**
     * 生成授权URL
     * @param provider 认证服务商
     * @param configId 配置ID（用于区分同一服务商的多个实例）
     * @param state 状态参数
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    String generateAuthorizationUrl(String provider, String configId, String state, String redirectUri);
    
    /**
     * 处理授权码回调
     * @param provider 认证服务商
     * @param configId 配置ID
     * @param code 授权码
     * @param state 状态参数
     * @param redirectUri 回调地址
     * @return OIDC认证结果
     */
    OidcAuthResult handleAuthorizationCode(String provider, String configId, String code, String state, String redirectUri);
    
    /**
     * 验证ID Token
     * @param provider 认证服务商
     * @param configId 配置ID
     * @param idToken ID Token
     * @return 验证结果
     */
    OidcTokenValidationResult validateIdToken(String provider, String configId, String idToken);
    
    /**
     * 获取用户信息
     * @param provider 认证服务商
     * @param configId 配置ID
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    OidcUserInfo getUserInfo(String provider, String configId, String accessToken);
} 