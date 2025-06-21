package space.jianmu.gourdboat.infrastructure.auth;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.application.auth.OidcService;
import space.jianmu.gourdboat.application.auth.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.auth.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.application.auth.dto.OidcUserInfo;
import space.jianmu.gourdboat.domain.account.AuthProvider;
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
    
    @Override
    public String generateAuthorizationUrl(String provider, String configId, String state, String redirectUri) {
        OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
        OidcProviderStrategy strategy = getProviderStrategy(provider);
        return strategy.generateAuthorizationUrl(config, state, redirectUri);
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(String provider, String configId, String code, String state, String redirectUri) {
        OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
        OidcProviderStrategy strategy = getProviderStrategy(provider);
        return strategy.handleAuthorizationCode(config, code, state, redirectUri);
    }
    
    @Override
    public OidcTokenValidationResult validateIdToken(String provider, String configId, String idToken) {
        OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
        OidcProviderStrategy strategy = getProviderStrategy(provider);
        return strategy.validateIdToken(config, idToken);
    }
    
    @Override
    public OidcUserInfo getUserInfo(String provider, String configId, String accessToken) {
        OidcProviderConfig config = getConfig(AuthProvider.of(provider), configId);
        OidcProviderStrategy strategy = getProviderStrategy(provider);
        return strategy.getUserInfo(config, accessToken);
    }
    
    private OidcProviderConfig getConfig(AuthProvider provider, String configId) {
        Optional<OidcProviderConfig> configOpt = oidcProviderConfigRepository.findByConfigId(configId);
        if (configOpt.isEmpty()) {
            throw new IllegalArgumentException("OIDC配置不存在: " + configId);
        }
        
        OidcProviderConfig config = configOpt.get();
        if (!config.getProviderCode().equals(provider.getValue())) {
            throw new IllegalArgumentException("配置与服务商不匹配: " + configId + " vs " + provider.getValue());
        }
        
        if (!config.getEnabled()) {
            throw new IllegalStateException("OIDC配置已禁用: " + configId);
        }
        
        return config;
    }
    
    private OidcProviderStrategy getProviderStrategy(String providerCode) {
        return strategyFactory.getStrategy(providerCode);
    }
} 