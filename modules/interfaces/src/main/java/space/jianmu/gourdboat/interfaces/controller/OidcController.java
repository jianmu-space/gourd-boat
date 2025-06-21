package space.jianmu.gourdboat.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import space.jianmu.gourdboat.application.auth.OidcService;
import space.jianmu.gourdboat.application.auth.dto.OidcAuthResult;
import space.jianmu.gourdboat.application.auth.dto.OidcTokenValidationResult;

import java.util.Map;

/**
 * OIDC认证控制器
 */
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
            @PathVariable String provider,
            @RequestParam String configId,
            @RequestParam String state,
            @RequestParam String redirectUri) {
        
        String authUrl = oidcService.generateAuthorizationUrl(provider, configId, state, redirectUri);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }
    
    /**
     * 处理授权回调
     */
    @GetMapping("/callback/{provider}")
    public ResponseEntity<OidcAuthResult> handleCallback(
            @PathVariable String provider,
            @RequestParam String configId,
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam String redirectUri) {
        
        OidcAuthResult result = oidcService.handleAuthorizationCode(provider, configId, code, state, redirectUri);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 验证ID Token
     */
    @PostMapping("/validate/{provider}")
    public ResponseEntity<Map<String, Object>> validateToken(
            @PathVariable String provider,
            @RequestParam String configId,
            @RequestBody Map<String, String> request) {
        
        String idToken = request.get("idToken");
        OidcTokenValidationResult result = oidcService.validateIdToken(provider, configId, idToken);
        return ResponseEntity.ok(Map.of("valid", result.isValid(), "error", result.getError()));
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo/{provider}")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @PathVariable String provider,
            @RequestParam String configId,
            @RequestHeader("Authorization") String authorization) {
        
        String accessToken = authorization.replace("Bearer ", "");
        var userInfo = oidcService.getUserInfo(provider, configId, accessToken);
        return ResponseEntity.ok(Map.of("userInfo", userInfo));
    }
} 