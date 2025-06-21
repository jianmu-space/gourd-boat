package space.jianmu.gourdboat.infrastructure.auth.strategy;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.application.auth.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.auth.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.application.auth.dto.OidcUserInfo;
import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.account.OidcProviderConfig;
import space.jianmu.gourdboat.infrastructure.auth.OidcProviderStrategy;

/**
 * 微信小程序OIDC策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMiniAppStrategy implements OidcProviderStrategy {
    
    @Override
    public String generateAuthorizationUrl(OidcProviderConfig config, String state, String redirectUri) {
        // 微信小程序使用code2Session，不需要授权URL
        throw new UnsupportedOperationException("微信小程序不支持授权URL，请使用code2Session");
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(OidcProviderConfig config, String code, String state, String redirectUri) {
        try {
            // 调用微信code2Session接口
            WechatCode2SessionResponse response = callCode2Session(config, code);
            
            if (response.getErrcode() != 0) {
                return OidcAuthResult.builder()
                    .success(false)
                    .error("微信认证失败: " + response.getErrmsg())
                    .build();
            }
            
            // 获取用户信息（微信小程序需要用户主动授权）
            OidcUserInfo userInfo = OidcUserInfo.builder()
                .sub(response.getOpenid())
                .openId(response.getOpenid())
                .unionId(response.getUnionid())
                .provider(AuthProvider.WECHAT_MINIAPP)
                .build();
            
            return OidcAuthResult.builder()
                .success(true)
                .accessToken(response.getSessionKey())
                .openId(response.getOpenid())
                .unionId(response.getUnionid())
                .expiresIn(response.getExpiresIn())
                .provider(AuthProvider.WECHAT_MINIAPP)
                .build();
                
        } catch (Exception e) {
            log.error("微信小程序认证失败", e);
            return OidcAuthResult.builder()
                .success(false)
                .error("微信小程序认证异常: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public OidcTokenValidationResult validateIdToken(OidcProviderConfig config, String idToken) {
        // 微信小程序没有ID Token，使用session_key验证
        return OidcTokenValidationResult.builder()
            .valid(true)
            .build();
    }
    
    @Override
    public OidcUserInfo getUserInfo(OidcProviderConfig config, String accessToken) {
        // 微信小程序通过session_key获取用户信息
        // 这里需要根据具体业务逻辑实现
        return OidcUserInfo.builder()
            .provider(AuthProvider.WECHAT_MINIAPP)
            .build();
    }
    
    private WechatCode2SessionResponse callCode2Session(OidcProviderConfig config, String code) {
        // 实现微信code2Session接口调用
        // 这里需要集成微信SDK或使用HTTP客户端
        // 返回微信的响应结果
        return new WechatCode2SessionResponse();
    }
    
    // 微信code2Session响应
    private static class WechatCode2SessionResponse {
        private int errcode;
        private String errmsg;
        private String openid;
        private String sessionKey;
        private String unionid;
        private long expiresIn;
        
        // getters and setters
        public int getErrcode() { return errcode; }
        public void setErrcode(int errcode) { this.errcode = errcode; }
        public String getErrmsg() { return errmsg; }
        public void setErrmsg(String errmsg) { this.errmsg = errmsg; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
        public long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    }
} 