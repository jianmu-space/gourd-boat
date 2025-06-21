package space.jianmu.gourdboat.domain.account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * 服务商注册表
 * 支持动态注册和管理OIDC服务商
 */
public class ProviderRegistry {
    
    private static final Map<String, ProviderInfo> providers = new ConcurrentHashMap<>();
    
    static {
        // 注册预定义的服务商
        registerProvider(AuthProvider.WECHAT_MINIAPP, "微信小程序", "微信小程序登录", "WECHAT");
        registerProvider(AuthProvider.WECHAT_MP, "微信公众号", "微信公众号OAuth", "WECHAT");
        registerProvider(AuthProvider.WECOM, "企业微信", "企业微信登录", "WECHAT");
        registerProvider(AuthProvider.TAOBAO, "淘宝", "淘宝开放平台", "ALIBABA");
        registerProvider(AuthProvider.DINGTALK, "钉钉", "钉钉开放平台", "ALIBABA");
        registerProvider(AuthProvider.ALIPAY, "支付宝", "支付宝开放平台", "ALIBABA");
        registerProvider(AuthProvider.QQ, "QQ", "QQ互联", "TENCENT");
        registerProvider(AuthProvider.WEIBO, "微博", "微博开放平台", "SINA");
        registerProvider(AuthProvider.DOUYIN, "抖音", "抖音开放平台", "BYTEDANCE");
        registerProvider(AuthProvider.BYTEDANCE, "字节跳动", "字节跳动开放平台", "BYTEDANCE");
        registerProvider(AuthProvider.GOOGLE, "Google", "Google OAuth", "GOOGLE");
        registerProvider(AuthProvider.GITHUB, "GitHub", "GitHub OAuth", "GITHUB");
    }
    
    /**
     * 注册新的服务商
     */
    public static void registerProvider(String providerCode, String providerName, String description, String group) {
        providers.put(providerCode, new ProviderInfo(providerCode, providerName, description, group));
    }
    
    /**
     * 获取服务商信息
     */
    public static ProviderInfo getProvider(String providerCode) {
        return providers.get(providerCode);
    }
    
    /**
     * 检查服务商是否存在
     */
    public static boolean hasProvider(String providerCode) {
        return providers.containsKey(providerCode);
    }
    
    /**
     * 获取所有服务商代码
     */
    public static Set<String> getAllProviders() {
        return providers.keySet();
    }
    
    /**
     * 根据分组获取服务商
     */
    public static Set<String> getProvidersByGroup(String group) {
        return providers.entrySet().stream()
            .filter(entry -> group.equals(entry.getValue().getGroup()))
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 获取所有分组
     */
    public static Set<String> getAllGroups() {
        return providers.values().stream()
            .map(ProviderInfo::getGroup)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 服务商信息
     */
    public static class ProviderInfo {
        private final String code;
        private final String name;
        private final String description;
        private final String group;
        
        public ProviderInfo(String code, String name, String description, String group) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.group = group;
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getGroup() { return group; }
        
        @Override
        public String toString() {
            return String.format("ProviderInfo{code='%s', name='%s', group='%s'}", code, name, group);
        }
    }
} 