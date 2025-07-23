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
        log.info("处理微信授权码: configId={}, code={}", config.getConfigId(), code);
        
        // 1. 获取access_token
        WechatMpApiClient.WechatAccessTokenResponse tokenResponse = wechatMpApiClient.getSnsAccessToken(config, code);
        if (tokenResponse.getErrcode() != null && tokenResponse.getErrcode() != 0) {
            log.error("获取微信access_token失败: {}", tokenResponse.getErrmsg());
            return OidcAuthResult.builder()
                    .success(false)
                    .error("获取access_token失败: " + tokenResponse.getErrmsg())
                    .build();
        }
        
        // 2. 如果scope包含用户信息权限，直接获取用户信息
        OidcUserInfo userInfo = null;
        String scope = tokenResponse.getScope();
        if (scope != null && scope.contains("snsapi_userinfo")) {
            log.info("检测到snsapi_userinfo权限，直接获取用户信息");
            try {
                WechatMpApiClient.WechatUserInfoResponse userInfoResp = wechatMpApiClient.getUserInfo(
                    tokenResponse.getAccessToken(), 
                    tokenResponse.getOpenid()
                );
                
                if (userInfoResp != null && (userInfoResp.getErrcode() == null || userInfoResp.getErrcode() == 0)) {
                    userInfo = OidcUserInfo.builder()
                        .provider(AuthProvider.WECHAT_MP)
                        .openId(userInfoResp.getOpenid())
                        .unionId(userInfoResp.getUnionid())
                        .nickname(userInfoResp.getNickname())
                        .picture(userInfoResp.getHeadimgurl())
                        .build();
                    log.info("获取微信用户信息成功: openId={}, nickname={}", 
                        userInfoResp.getOpenid(), userInfoResp.getNickname());
                } else {
                    log.warn("获取微信用户信息失败: {}", 
                        userInfoResp != null ? userInfoResp.getErrmsg() : "无响应");
                }
            } catch (Exception e) {
                log.warn("获取微信用户信息异常: {}", e.getMessage(), e);
            }
        } else {
            log.info("当前scope不包含用户信息权限: {}", scope);
        }
        
        return OidcAuthResult.builder()
                .success(true)
                .accessToken(tokenResponse.getAccessToken())
                .openId(tokenResponse.getOpenid())
                .unionId(tokenResponse.getUnionid())
                .expiresIn(tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn().longValue() : null)
                .provider(AuthProvider.WECHAT_MP)
                .userInfo(userInfo) // 直接包含用户信息
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
} 