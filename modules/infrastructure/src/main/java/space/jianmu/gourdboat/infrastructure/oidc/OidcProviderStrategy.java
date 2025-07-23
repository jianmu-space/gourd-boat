package space.jianmu.gourdboat.infrastructure.oidc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.oidc.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.domain.oidc.OidcProviderConfig;

/**
 * OIDC服务商策略接口
 * 定义不同服务商的具体实现
 */
public interface OidcProviderStrategy {
    
    /**
     * 生成授权URL
     */
    String generateAuthorizationUrl(OidcProviderConfig config, String state);
    
    /**
     * 处理授权码回调
     */
    OidcAuthResult handleAuthorizationCode(OidcProviderConfig config, String code, String state);
    
    /**
     * 验证ID Token
     */
    OidcTokenValidationResult validateIdToken(OidcProviderConfig config, String idToken);
    
    /**
     * URL编码工具方法
     */
    default String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    
    /**
     * URL参数拼接工具方法
     * @param baseUrl 基础URL
     * @param paramName 参数名
     * @param paramValue 参数值
     * @return 拼接后的URL
     */
    default String appendUrlParam(String baseUrl, String paramName, String paramValue) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return baseUrl;
        }
        
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + paramName + "=" + paramValue;
    }
} 