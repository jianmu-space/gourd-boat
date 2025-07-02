package space.jianmu.gourdboat.infrastructure.oidc.wechat.mp;

import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import space.jianmu.gourdboat.domain.oidc.OidcProviderConfig;

/**
 * 微信公众号API Client
 */
@Slf4j
@Component
public class WechatMpApiClient {
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理微信API HTTP状态码错误
     */
    private Function<ClientResponse, Mono<? extends Throwable>> handleWechatApiStatusError(String apiName) {
        return response -> response.bodyToMono(String.class)
            .flatMap(body -> {
                log.error("微信{} API HTTP错误, status={}, body={}", apiName, response.statusCode(), body);
                return Mono.error(new RuntimeException("微信" + apiName + " API调用失败: " + response.statusCode()));
            });
    }

    /**
     * 解析JSON响应字符串为对象
     */
    private <T> T parseJsonResponse(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            log.error("JSON解析失败: {}", jsonString, e);
            throw new RuntimeException("JSON解析失败: " + e.getMessage());
        }
    }

    /**
     * 通过网页授权code换取access_token（OAuth2.0流程，sns接口）
     * https://developers.weixin.qq.com/doc/service/guide/h5/auth.html
     * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#0
     */
    public WechatAccessTokenResponse getSnsAccessToken(OidcProviderConfig config, String code) {
        log.info("请求微信sns access_token, appid={}, code={}", config.getClientId(), code);
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
                    + "?appid=" + config.getClientId()
                    + "&secret=" + config.getClientSecret()
                    + "&code=" + code
                    + "&grant_type=authorization_code";
            
            // 先获取字符串响应，然后手动解析JSON
            String rawResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.isError(), handleWechatApiStatusError("sns access_token"))
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("微信API原始响应: {}", rawResponse);
            
            // 手动解析JSON
            WechatAccessTokenResponse resp = parseJsonResponse(rawResponse, WechatAccessTokenResponse.class);
            log.debug("微信sns access_token返回: {}", resp);
            return resp;
        } catch (Exception e) {
            log.error("获取微信sns access_token失败, appid={}, code={}, err={}", config.getClientId(), code, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取微信用户基本信息
     * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#4
     */
    public WechatUserInfoResponse getUserInfo(String accessToken, String openId) {
        log.info("请求微信用户信息, accessToken={}, openId={}", accessToken != null ? accessToken.substring(0, 8) + "..." : null, openId);
        try {
            String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";
            
            // 先获取字符串响应，然后手动解析JSON
            String rawResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.isError(), handleWechatApiStatusError("用户信息"))
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("微信用户信息原始响应: {}", rawResponse);
            
            // 手动解析JSON
            WechatUserInfoResponse resp = parseJsonResponse(rawResponse, WechatUserInfoResponse.class);
            log.debug("微信用户信息返回: {}", resp);
            return resp;
        } catch (Exception e) {
            log.error("获取微信用户信息异常", e);
            return null;
        }
    }

    @Data
    public static class WechatAccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("expires_in")
        private Integer expiresIn;
        @JsonProperty("refresh_token")
        private String refreshToken;
        @JsonProperty("openid")
        private String openid;
        @JsonProperty("scope")
        private String scope;
        @JsonProperty("unionid")
        private String unionid;
        @JsonProperty("errcode")
        private Integer errcode;
        @JsonProperty("errmsg")
        private String errmsg;
    }

    // 微信用户信息响应
    public static class WechatUserInfoResponse {
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