package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.account.OidcProviderConfig;

import java.util.List;

/**
 * OIDC配置管理服务
 */
public interface OidcProviderConfigService {
    
    /**
     * 创建OIDC配置
     */
    OidcProviderConfig createConfig(OidcProviderConfig config);
    
    /**
     * 更新OIDC配置
     */
    OidcProviderConfig updateConfig(String configId, OidcProviderConfig config);
    
    /**
     * 获取OIDC配置
     */
    OidcProviderConfig getConfig(String configId);
    
    /**
     * 根据服务商获取所有配置
     */
    List<OidcProviderConfig> getConfigsByProvider(AuthProvider provider);
    
    /**
     * 获取所有启用的配置
     */
    List<OidcProviderConfig> getAllEnabledConfigs();
    
    /**
     * 启用/禁用配置
     */
    OidcProviderConfig toggleConfig(String configId, boolean enabled);
    
    /**
     * 删除配置
     */
    void deleteConfig(String configId);
    
    /**
     * 验证配置
     */
    boolean validateConfig(OidcProviderConfig config);
} 