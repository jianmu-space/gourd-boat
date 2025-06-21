package space.jianmu.gourdboat.infrastructure.persistence.account;

import org.springframework.stereotype.Component;
import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.account.OidcProviderConfig;

@Component
public class OidcProviderConfigEntityMapper {
    
    public OidcProviderConfig toDomain(OidcProviderConfigEntity entity) {
        return OidcProviderConfig.builder()
            .configId(entity.getConfigId())
            .providerCode(entity.getProviderCode())
            .providerName(entity.getProviderName())
            .clientId(entity.getClientId())
            .clientSecret(entity.getClientSecret())
            .issuerUrl(entity.getIssuerUrl())
            .authorizationEndpoint(entity.getAuthorizationEndpoint())
            .tokenEndpoint(entity.getTokenEndpoint())
            .userInfoEndpoint(entity.getUserInfoEndpoint())
            .jwksUri(entity.getJwksUri())
            .scope(entity.getScope())
            .enabled(entity.getEnabled())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    public OidcProviderConfigEntity toEntity(OidcProviderConfig config) {
        OidcProviderConfigEntity entity = new OidcProviderConfigEntity();
        entity.setConfigId(config.getConfigId());
        entity.setProviderCode(config.getProviderCode());
        entity.setProviderName(config.getProviderName());
        entity.setClientId(config.getClientId());
        entity.setClientSecret(config.getClientSecret());
        entity.setIssuerUrl(config.getIssuerUrl());
        entity.setAuthorizationEndpoint(config.getAuthorizationEndpoint());
        entity.setTokenEndpoint(config.getTokenEndpoint());
        entity.setUserInfoEndpoint(config.getUserInfoEndpoint());
        entity.setJwksUri(config.getJwksUri());
        entity.setScope(config.getScope());
        entity.setEnabled(config.getEnabled());
        entity.setDescription(config.getDescription());
        entity.setCreatedAt(config.getCreatedAt());
        entity.setUpdatedAt(config.getUpdatedAt());
        return entity;
    }
} 