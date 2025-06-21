package space.jianmu.gourdboat.domain.account;

import java.util.List;
import java.util.Optional;

/**
 * OIDC服务商实例配置仓储接口
 */
public interface OidcProviderConfigRepository {
    
    /**
     * 保存OIDC配置
     */
    OidcProviderConfig save(OidcProviderConfig config);
    
    /**
     * 根据配置ID查找
     */
    Optional<OidcProviderConfig> findByConfigId(String configId);
    
    /**
     * 根据服务商类型查找所有配置
     */
    List<OidcProviderConfig> findByProvider(AuthProvider provider);
    
    /**
     * 查找所有启用的配置
     */
    List<OidcProviderConfig> findAllEnabled();
    
    /**
     * 根据服务商类型查找启用的配置
     */
    List<OidcProviderConfig> findEnabledByProvider(AuthProvider provider);
    
    /**
     * 删除配置
     */
    void deleteByConfigId(String configId);
    
    /**
     * 检查配置ID是否存在
     */
    boolean existsByConfigId(String configId);
} 