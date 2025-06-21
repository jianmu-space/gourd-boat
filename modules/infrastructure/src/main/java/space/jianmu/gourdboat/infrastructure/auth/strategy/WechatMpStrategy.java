package space.jianmu.gourdboat.infrastructure.auth.strategy;

import java.util.Map;

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
 * 微信公众号OIDC策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMpStrategy implements OidcProviderStrategy {
    
    @Override
    public String generateAuthorizationUrl(OidcProviderConfig config, String state, String redirectUri) {
        String clientId = config.getClientId();
        String scope = config.getScope() != null ? config.getScope() : "snsapi_userinfo";
        
        return String.format(
            "https://open.weixin.qq.com/connect/oauth2/authorize" +
            "?appid=%s" +
            "&redirect_uri=%s" +
            "&response_type=code" +
            "&scope=%s" +
            "&state=%s" +
            "#wechat_redirect",
            clientId,
            redirectUri,
            scope,
            state
        );
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(OidcProviderConfig config, String code, String state, String redirectUri) {
        // 1. 获取access_token
        WechatAccessTokenResponse tokenResponse = getAccessToken(config, code);
        
        if (tokenResponse.getErrcode() != null && tokenResponse.getErrcode() != 0) {
            return OidcAuthResult.builder()
                    .success(false)
                    .error("获取access_token失败: " + tokenResponse.getErrmsg())
                    .build();
        }
        
        // 2. 获取用户信息
        OidcUserInfo userInfo = getUserInfo(config, tokenResponse.getAccessToken());
        
        return OidcAuthResult.builder()
                .success(true)
                .accessToken(tokenResponse.getAccessToken())
                .openId(tokenResponse.getOpenid())
                .unionId(tokenResponse.getUnionid())
                .expiresIn(tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn().longValue() : null)
                .provider(AuthProvider.WECHAT_MP)
                .build();
    }
    
    @Override
    public OidcTokenValidationResult validateIdToken(OidcProviderConfig config, String idToken) {
        // 微信公众号使用access_token验证，这里简化处理
        return OidcTokenValidationResult.builder()
                .valid(true)
                .claims(Map.of("openid", idToken))
                .build();
    }
    
    @Override
    public OidcUserInfo getUserInfo(OidcProviderConfig config, String accessToken) {
        // 这里需要根据具体业务逻辑实现
        return OidcUserInfo.builder()
                .provider(AuthProvider.WECHAT_MP)
                .build();
    }
    
    private WechatAccessTokenResponse getAccessToken(OidcProviderConfig config, String code) {
        // 实现微信access_token接口调用
        // 这里需要集成微信SDK或使用HTTP客户端
        return new WechatAccessTokenResponse();
    }
    
    // 微信access_token响应
    private static class WechatAccessTokenResponse {
        private String accessToken;
        private String openid;
        private Integer expiresIn;
        private String refreshToken;
        private Integer errcode;
        private String errmsg;
        private String unionid;
        
        // getters and setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        
        public Integer getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public Integer getErrcode() { return errcode; }
        public void setErrcode(Integer errcode) { this.errcode = errcode; }
        
        public String getErrmsg() { return errmsg; }
        public void setErrmsg(String errmsg) { this.errmsg = errmsg; }
        
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
    }
} 