package space.jianmu.gourdboat.application.auth;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import space.jianmu.gourdboat.domain.account.OidcProviderConfig;
import space.jianmu.gourdboat.domain.account.OidcErrorCode;
import space.jianmu.gourdboat.domain.account.OidcAuthenticationException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * OIDC配置验证器
 */
@Component
public class OidcConfigValidator {
    
    /**
     * 验证OIDC配置的完整性和有效性
     */
    public ValidationResult validate(OidcProviderConfig config) {
        List<String> errors = new ArrayList<>();
        
        // 基本信息验证
        validateBasicInfo(config, errors);
        
        // URL格式验证
        validateUrls(config, errors);
        
        // 端点验证
        validateEndpoints(config, errors);
        
        // 范围验证
        validateScopes(config, errors);
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * 验证基本信息
     */
    private void validateBasicInfo(OidcProviderConfig config, List<String> errors) {
        if (!StringUtils.hasText(config.getProviderCode())) {
            errors.add("provider_code不能为空");
        }
        
        if (!StringUtils.hasText(config.getProviderName())) {
            errors.add("provider_name不能为空");
        }
        
        if (!StringUtils.hasText(config.getClientId())) {
            errors.add("client_id不能为空");
        }
        
        if (!StringUtils.hasText(config.getClientSecret())) {
            errors.add("client_secret不能为空");
        }
    }
    
    /**
     * 验证URL格式
     */
    private void validateUrls(OidcProviderConfig config, List<String> errors) {
        validateUrl("issuer_url", config.getIssuerUrl(), errors);
        validateUrl("authorization_endpoint", config.getAuthorizationEndpoint(), errors);
        validateUrl("token_endpoint", config.getTokenEndpoint(), errors);
        validateUrl("user_info_endpoint", config.getUserInfoEndpoint(), errors, true);
        validateUrl("jwks_uri", config.getJwksUri(), errors, true);
    }
    
    /**
     * 验证单个URL
     */
    private void validateUrl(String fieldName, String url, List<String> errors) {
        validateUrl(fieldName, url, errors, false);
    }
    
    /**
     * 验证单个URL（可选）
     */
    private void validateUrl(String fieldName, String url, List<String> errors, boolean optional) {
        if (!StringUtils.hasText(url)) {
            if (!optional) {
                errors.add(fieldName + "不能为空");
            }
            return;
        }
        
        try {
            new URI(url);
        } catch (URISyntaxException e) {
            errors.add(fieldName + "格式无效: " + e.getMessage());
        }
    }
    
    /**
     * 验证端点
     */
    private void validateEndpoints(OidcProviderConfig config, List<String> errors) {
        // 验证authorization_endpoint必须是HTTPS（生产环境）
        if (StringUtils.hasText(config.getAuthorizationEndpoint()) && 
            !config.getAuthorizationEndpoint().startsWith("https://")) {
            errors.add("authorization_endpoint在生产环境中必须使用HTTPS");
        }
        
        // 验证token_endpoint必须是HTTPS（生产环境）
        if (StringUtils.hasText(config.getTokenEndpoint()) && 
            !config.getTokenEndpoint().startsWith("https://")) {
            errors.add("token_endpoint在生产环境中必须使用HTTPS");
        }
    }
    
    /**
     * 验证范围
     */
    private void validateScopes(OidcProviderConfig config, List<String> errors) {
        if (StringUtils.hasText(config.getScope())) {
            String[] scopes = config.getScope().split("\\s+");
            for (String scope : scopes) {
                if (!isValidScope(scope)) {
                    errors.add("无效的scope: " + scope);
                }
            }
        }
    }
    
    /**
     * 验证scope格式
     */
    private boolean isValidScope(String scope) {
        if (!StringUtils.hasText(scope)) {
            return false;
        }
        
        // scope只能包含字母、数字、点、下划线、连字符
        return scope.matches("^[a-zA-Z0-9._-]+$");
    }
    
    /**
     * 验证配置并抛出异常（如果验证失败）
     */
    public void validateAndThrow(OidcProviderConfig config) {
        ValidationResult result = validate(config);
        if (!result.isValid()) {
            throw new OidcAuthenticationException(
                OidcErrorCode.PROVIDER_CONFIG_INVALID,
                config.getProviderCode(),
                String.join("; ", result.getErrors())
            );
        }
    }
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
} 