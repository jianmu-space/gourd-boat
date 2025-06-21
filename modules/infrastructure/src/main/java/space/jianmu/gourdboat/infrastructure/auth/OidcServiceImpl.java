package space.jianmu.gourdboat.infrastructure.auth;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.application.auth.OidcConfigValidator;
import space.jianmu.gourdboat.application.auth.OidcService;
import space.jianmu.gourdboat.application.auth.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.auth.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.application.auth.dto.OidcUserInfo;
import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.account.OidcErrorCode;
import space.jianmu.gourdboat.domain.account.OidcAuthenticationException;
import space.jianmu.gourdboat.domain.account.OidcProviderConfig;
import space.jianmu.gourdboat.domain.account.OidcProviderConfigRepository;

/**
 * OIDC服务实现
 * 支持多个服务商的OIDC认证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OidcServiceImpl implements OidcService {
    
    private final OidcProviderConfigRepository oidcProviderConfigRepository;
    private final DynamicOidcStrategyFactory strategyFactory;
    private final OidcConfigValidator configValidator;
    
    @Override
    public String generateAuthorizationUrl(String provider, String configId, String state, String redirectUri) {
        try {
            OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
            OidcProviderStrategy strategy = getProviderStrategy(provider);
            return strategy.generateAuthorizationUrl(config, state, redirectUri);
        } catch (OidcAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成授权URL失败: provider={}, configId={}", provider, configId, e);
            throw new OidcAuthenticationException(OidcErrorCode.INTERNAL_ERROR, provider, e);
        }
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(String provider, String configId, String code, String state, String redirectUri) {
        try {
            OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
            OidcProviderStrategy strategy = getProviderStrategy(provider);
            return strategy.handleAuthorizationCode(config, code, state, redirectUri);
        } catch (OidcAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理授权码失败: provider={}, configId={}", provider, configId, e);
            throw new OidcAuthenticationException(OidcErrorCode.INTERNAL_ERROR, provider, e);
        }
    }
    
    @Override
    public OidcTokenValidationResult validateIdToken(String provider, String configId, String idToken) {
        try {
            OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
            OidcProviderStrategy strategy = getProviderStrategy(provider);
            return strategy.validateIdToken(config, idToken);
        } catch (OidcAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证ID Token失败: provider={}, configId={}", provider, configId, e);
            throw new OidcAuthenticationException(OidcErrorCode.INTERNAL_ERROR, provider, e);
        }
    }
    
    @Override
    public OidcUserInfo getUserInfo(String provider, String configId, String accessToken) {
        try {
            OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
            OidcProviderStrategy strategy = getProviderStrategy(provider);
            return strategy.getUserInfo(config, accessToken);
        } catch (OidcAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息失败: provider={}, configId={}", provider, configId, e);
            throw new OidcAuthenticationException(OidcErrorCode.INTERNAL_ERROR, provider, e);
        }
    }
    
    private OidcProviderConfig getConfig(AuthProvider provider, String configId) {
        Optional<OidcProviderConfig> configOpt = oidcProviderConfigRepository.findByConfigId(configId);
        if (configOpt.isEmpty()) {
            throw new OidcAuthenticationException(
                OidcErrorCode.PROVIDER_NOT_FOUND, 
                provider.getValue(), 
                "配置不存在: " + configId
            );
        }
        
        OidcProviderConfig config = configOpt.get();
        
        // 验证配置与服务商匹配
        if (!config.getProviderCode().equals(provider.getValue())) {
            throw new OidcAuthenticationException(
                OidcErrorCode.PROVIDER_CONFIG_INVALID,
                provider.getValue(),
                "配置与服务商不匹配: " + configId + " vs " + provider.getValue()
            );
        }
        
        // 验证配置是否启用
        if (!config.getEnabled()) {
            throw new OidcAuthenticationException(
                OidcErrorCode.PROVIDER_DISABLED,
                provider.getValue(),
                "配置已禁用: " + configId
            );
        }
        
        // 验证配置的完整性和有效性
        configValidator.validateAndThrow(config);
        
        return config;
    }
    
    private OidcProviderStrategy getProviderStrategy(String providerCode) {
        try {
            return strategyFactory.getStrategy(providerCode);
        } catch (Exception e) {
            log.error("获取OIDC策略失败: provider={}", providerCode, e);
            throw new OidcAuthenticationException(
                OidcErrorCode.STRATEGY_NOT_FOUND,
                providerCode,
                "策略未找到: " + providerCode
            );
        }
    }
} 