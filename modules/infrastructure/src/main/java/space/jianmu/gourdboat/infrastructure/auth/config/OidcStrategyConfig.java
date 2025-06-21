package space.jianmu.gourdboat.infrastructure.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.infrastructure.auth.DynamicOidcStrategyFactory;
import space.jianmu.gourdboat.infrastructure.auth.strategy.WechatMiniAppStrategy;
import space.jianmu.gourdboat.infrastructure.auth.strategy.WechatMpStrategy;

/**
 * OIDC策略自动注册配置
 * 在应用启动时自动注册所有OIDC策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OidcStrategyConfig implements CommandLineRunner {
    
    private final DynamicOidcStrategyFactory strategyFactory;
    private final WechatMiniAppStrategy wechatMiniAppStrategy;
    private final WechatMpStrategy wechatMpStrategy;
    
    @Override
    public void run(String... args) throws Exception {
        // 注册预定义的策略
        registerPredefinedStrategies();
        
        // 注册自定义策略（如果有的话）
        registerCustomStrategies();
        
        log.info("OIDC策略注册完成，共注册 {} 个策略", strategyFactory.getStrategyCount());
    }
    
    /**
     * 注册预定义的策略
     */
    private void registerPredefinedStrategies() {
        // 微信系策略
        strategyFactory.registerStrategy(AuthProvider.WECHAT_MINIAPP, wechatMiniAppStrategy);
        strategyFactory.registerStrategy(AuthProvider.WECHAT_MP, wechatMpStrategy);
        
        // 可以继续注册其他策略...
        // strategyFactory.registerStrategy(AuthProvider.TAOBAO, taobaoStrategy);
        // strategyFactory.registerStrategy(AuthProvider.DINGTALK, dingtalkStrategy);
    }
    
    /**
     * 注册自定义策略
     * 这里可以通过配置文件或数据库动态加载策略
     */
    private void registerCustomStrategies() {
        // 示例：从配置文件或数据库加载自定义策略
        // 这里可以实现动态策略加载逻辑
    }
} 