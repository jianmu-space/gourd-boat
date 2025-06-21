package space.jianmu.gourdboat.domain.account;

/**
 * 认证服务商
 * 使用字符串类型，支持动态扩展
 */
public class AuthProvider {
    
    // 预定义的服务商常量
    public static final String PASSWORD = "PASSWORD";           // 密码认证
    public static final String GOOGLE = "GOOGLE";               // Google账号
    public static final String GITHUB = "GITHUB";               // GitHub账号
    public static final String WECHAT_MINIAPP = "WECHAT_MINIAPP";     // 微信小程序
    public static final String WECHAT_MP = "WECHAT_MP";          // 微信公众号
    public static final String WECOM = "WECOM";                  // 企业微信
    public static final String TAOBAO = "TAOBAO";                // 淘宝
    public static final String DINGTALK = "DINGTALK";            // 钉钉
    public static final String ALIPAY = "ALIPAY";                // 支付宝
    public static final String QQ = "QQ";                        // QQ
    public static final String WEIBO = "WEIBO";                  // 微博
    public static final String DOUYIN = "DOUYIN";                // 抖音
    public static final String BYTEDANCE = "BYTEDANCE";          // 字节跳动
    
    // 服务商类型分组
    public static final String WECHAT_GROUP = "WECHAT";          // 微信系
    public static final String ALIBABA_GROUP = "ALIBABA";        // 阿里系
    public static final String BYTEDANCE_GROUP = "BYTEDANCE";    // 字节系
    
    private final String value;
    
    public AuthProvider(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AuthProvider that = (AuthProvider) obj;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    /**
     * 检查是否为微信系服务商
     */
    public boolean isWechatGroup() {
        return WECHAT_MINIAPP.equals(value) || 
               WECHAT_MP.equals(value) || 
               WECOM.equals(value);
    }
    
    /**
     * 检查是否为阿里系服务商
     */
    public boolean isAlibabaGroup() {
        return TAOBAO.equals(value) || 
               ALIPAY.equals(value);
    }
    
    /**
     * 检查是否为字节系服务商
     */
    public boolean isBytedanceGroup() {
        return DOUYIN.equals(value) || 
               BYTEDANCE.equals(value);
    }
    
    /**
     * 从字符串创建AuthProvider
     */
    public static AuthProvider of(String value) {
        return new AuthProvider(value);
    }
} 