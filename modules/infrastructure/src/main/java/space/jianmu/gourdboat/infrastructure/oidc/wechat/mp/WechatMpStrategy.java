package space.jianmu.gourdboat.infrastructure.oidc.wechat.mp;

import java.util.Map;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.oidc.dto.OidcTokenValidationResult;
import space.jianmu.gourdboat.application.oidc.dto.OidcUserInfo;
import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.oidc.OidcProviderConfig;
import space.jianmu.gourdboat.infrastructure.oidc.OidcProviderStrategy;

/**
 * 微信公众号OIDC策略实现
 * 通过注入WechatMpApiClient实现API调用，便于维护和扩展
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMpStrategy implements OidcProviderStrategy {
    private final WechatMpApiClient wechatMpApiClient;
    
    @Override
    public String generateAuthorizationUrl(OidcProviderConfig config, String state) {
        String clientId = config.getClientId();
        String scope = config.getScope() != null ? config.getScope() : "snsapi_userinfo";
        String baseRedirectUri = config.getRedirectUri();
        
        if (baseRedirectUri == null || baseRedirectUri.trim().isEmpty()) {
            throw new IllegalArgumentException("配置中缺少redirectUri");
        }
        
        // 在回调地址中拼接config_id参数
        String redirectUri = appendUrlParam(baseRedirectUri, "configId", config.getConfigId());
        
        // URL编码参数
        String encodedRedirectUri = urlEncode(redirectUri);
        String encodedState = urlEncode(state);
        
        return String.format(
            "https://open.weixin.qq.com/connect/oauth2/authorize" +
            "?appid=%s" +
            "&redirect_uri=%s" +
            "&response_type=code" +
            "&scope=%s" +
            "&state=%s" +
            "#wechat_redirect",
            clientId,
            encodedRedirectUri,
            scope,
            encodedState
        );
    }
    
    @Override
    public OidcAuthResult handleAuthorizationCode(OidcProviderConfig config, String code, String state) {
        // 1. 获取access_token
        WechatMpApiClient.WechatAccessTokenResponse tokenResponse = wechatMpApiClient.getSnsAccessToken(config, code);
        if (tokenResponse.getErrcode() != null && tokenResponse.getErrcode() != 0) {
            return OidcAuthResult.builder()
                    .success(false)
                    .error("获取access_token失败: " + tokenResponse.getErrmsg())
                    .build();
        }
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
        // TODO: 需根据accessToken查找openId，或由上游传递/存储
        String openId = null; // 这里需后续补充查找逻辑
        log.info("获取微信用户信息, configId={}, openId={}, accessToken={}", config.getConfigId(), openId, accessToken != null ? accessToken.substring(0, 8) + "..." : null);
        if (openId == null) {
            log.warn("未能获取openId，无法获取用户信息");
            return OidcUserInfo.builder().provider(AuthProvider.WECHAT_MP).nickname("ERROR: openId缺失").build();
        }
        WechatMpApiClient.WechatUserInfoResponse resp = wechatMpApiClient.getUserInfo(accessToken, openId);
        if (resp == null || (resp.getErrcode() != null && resp.getErrcode() != 0)) {
            log.warn("获取微信用户信息失败: {}", resp != null ? resp.getErrmsg() : "无响应");
            return OidcUserInfo.builder()
                    .provider(AuthProvider.WECHAT_MP)
                    .nickname("ERROR: " + (resp != null ? resp.getErrmsg() : "获取用户信息失败"))
                    .build();
        }
        log.info("获取微信用户信息成功, openId={}, nickname={}", resp.getOpenid(), resp.getNickname());
        return OidcUserInfo.builder()
                .provider(AuthProvider.WECHAT_MP)
                .openId(resp.getOpenid())
                .unionId(resp.getUnionid())
                .nickname(resp.getNickname())
                .picture(resp.getHeadimgurl())
                .build();
    }

    // 微信用户信息响应
    private static class WechatUserInfoResponse {
        private String openid;
        private String nickname;
        private String headimgurl;
        private String unionid;
        private Integer errcode;
        private String errmsg;
        // getters and setters
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getHeadimgurl() { return headimgurl; }
        public void setHeadimgurl(String headimgurl) { this.headimgurl = headimgurl; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
        public Integer getErrcode() { return errcode; }
        public void setErrcode(Integer errcode) { this.errcode = errcode; }
        public String getErrmsg() { return errmsg; }
        public void setErrmsg(String errmsg) { this.errmsg = errmsg; }
    }
} 