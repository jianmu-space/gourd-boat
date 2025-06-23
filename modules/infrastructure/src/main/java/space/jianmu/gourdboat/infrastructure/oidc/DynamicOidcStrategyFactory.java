package space.jianmu.gourdboat.infrastructure.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import space.jianmu.gourdboat.domain.account.AuthProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态OIDC策略工厂
 * 支持运行时注册和管理OIDC策略
 */
@Slf4j
@Component
public class DynamicOidcStrategyFactory {
    
    private final Map<String, OidcProviderStrategy> strategies = new ConcurrentHashMap<>();
    
    /**
     * 注册策略
     */
    public void registerStrategy(String providerCode, OidcProviderStrategy strategy) {
        strategies.put(providerCode, strategy);
        log.info("注册OIDC策略: {} -> {}", providerCode, strategy.getClass().getSimpleName());
    }
    
    /**
     * 获取策略
     */
    public OidcProviderStrategy getStrategy(String providerCode) {
        OidcProviderStrategy strategy = strategies.get(providerCode);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerCode);
        }
        return strategy;
    }
    
    /**
     * 获取策略（使用AuthProvider）
     */
    public OidcProviderStrategy getStrategy(AuthProvider provider) {
        return getStrategy(provider.getValue());
    }
    
    /**
     * 检查策略是否存在
     */
    public boolean hasStrategy(String providerCode) {
        return strategies.containsKey(providerCode);
    }
    
    /**
     * 移除策略
     */
    public void removeStrategy(String providerCode) {
        OidcProviderStrategy removed = strategies.remove(providerCode);
        if (removed != null) {
            log.info("移除OIDC策略: {} -> {}", providerCode, removed.getClass().getSimpleName());
        }
    }
    
    /**
     * 获取所有已注册的策略
     */
    public Map<String, OidcProviderStrategy> getAllStrategies() {
        return new ConcurrentHashMap<>(strategies);
    }
    
    /**
     * 获取策略数量
     */
    public int getStrategyCount() {
        return strategies.size();
    }
} 