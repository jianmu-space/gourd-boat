package space.jianmu.gourdboat.interfaces.oidc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import space.jianmu.gourdboat.application.oidc.OidcService;
import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.oidc.dto.OidcTokenValidationResult;

import java.util.Map;

/**
 * OIDC认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/oidc")
@RequiredArgsConstructor
public class OidcController {
    
    private final OidcService oidcService;
    
    /**
     * 生成授权URL
     */
    @GetMapping("/auth/{provider}")
    public ResponseEntity<Map<String, String>> generateAuthUrl(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestParam("state") String state) {
        log.info("生成授权URL, provider={}, configId={}, state={}", provider, configId, state);
        String authUrl = oidcService.generateAuthorizationUrl(provider, configId, state);
        log.debug("授权URL生成结果: {}", authUrl);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }
    
    /**
     * 处理授权回调
     */
    @GetMapping("/callback/{provider}")
    public ResponseEntity<OidcAuthResult> handleCallback(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        log.info("处理授权回调, provider={}, configId={}, code={}, state={}", provider, configId, code != null ? code.substring(0, 6) + "..." : null, state);
        OidcAuthResult result = oidcService.handleAuthorizationCode(provider, configId, code, state);
        log.debug("授权回调处理结果: {}", result);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 验证ID Token
     */
    @PostMapping("/validate/{provider}")
    public ResponseEntity<Map<String, Object>> validateToken(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        log.info("验证ID Token, provider={}, configId={}, idToken={}...", provider, configId, idToken != null ? idToken.substring(0, 8) : null);
        OidcTokenValidationResult result = oidcService.validateIdToken(provider, configId, idToken);
        log.debug("ID Token验证结果: {}", result);
        return ResponseEntity.ok(Map.of("valid", result.isValid(), "error", result.getError()));
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo/{provider}")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        log.info("获取用户信息, provider={}, configId={}, accessToken={}...", provider, configId, accessToken != null ? accessToken.substring(0, 8) : null);
        var userInfo = oidcService.getUserInfo(provider, configId, accessToken);
        log.debug("用户信息获取结果: {}", userInfo);
        return ResponseEntity.ok(Map.of("userInfo", userInfo));
    }
} 