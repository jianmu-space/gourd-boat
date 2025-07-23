package space.jianmu.gourdboat.interfaces.auth;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.application.auth.AuthService;
import space.jianmu.gourdboat.application.auth.OidcAccountService;
import space.jianmu.gourdboat.application.auth.UnifiedJwtService;
import space.jianmu.gourdboat.application.auth.command.LoginCommand;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;
import space.jianmu.gourdboat.application.oidc.OidcService;
import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.domain.account.Account;

/**
 * 登录控制器
 * 专门处理用户登录相关的业务：各种登录方式的统一入口
 * 
 * 核心职责：
 * 1. 传统用户名密码登录
 * 2. OIDC第三方登录（微信、GitHub等）
 * 3. 生成第三方授权URL
 * 4. 处理第三方登录回调
 * 
 * 注意：账号绑定相关的API在 AccountLinkingController 中处理
 */
@Slf4j
@RestController
@RequestMapping("/api/login") 
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final OidcService oidcService;
    private final OidcAccountService oidcAccountService;
    private final UnifiedJwtService unifiedJwtService;

    /**
     * 用户名密码登录
     * 处理传统的用户名密码认证方式
     * 
     * @param command 登录命令，包含用户名和密码
     * @return 登录结果，包含JWT令牌
     */
    @PostMapping("/password")
    public ResponseEntity<LoginResult> loginByPassword(@RequestBody LoginCommand command) {
        log.info("用户名密码登录请求: identifier={}", command.identifier());
        
        // 使用 Spring Security 的认证机制进行用户名密码验证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.identifier(), command.password())
        );
        
        // 设置认证信息到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成 JWT token（仅限PASSWORD类型的账号）
        LoginResult result = authService.login(command);
        log.info("用户名密码登录成功: identifier={}", command.identifier());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取第三方登录授权URL
     * 生成第三方认证服务的授权链接，供前端跳转使用
     * 
     * @param provider 第三方服务商标识（如：wechat_mp, github等）
     * @param configId 配置实例ID
     * @param state 状态参数，用于防止CSRF攻击
     * @return 包含授权URL的响应
     */
    @GetMapping("/oauth/{provider}/authorize-url")
    public ResponseEntity<Map<String, String>> getOAuthAuthorizeUrl(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestParam("state") String state) {
        log.info("获取OAuth授权URL: provider={}, configId={}, state={}", provider, configId, state);
        
        String authUrl = oidcService.generateAuthorizationUrl(provider, configId, state);
        log.debug("OAuth授权URL生成成功: {}", authUrl);
        
        return ResponseEntity.ok(Map.of(
            "authUrl", authUrl,
            "provider", provider,
            "configId", configId
        ));
    }

    /**
     * 处理第三方登录回调
     * 接收第三方认证服务的回调，完成登录流程
     * 
     * 处理流程：
     * 1. 验证授权码，获取用户信息
     * 2. 查找或创建账号
     * 3. 生成统一格式的JWT
     * 
     * 注意：如果返回的role为"PENDING_BIND"，前端需要引导用户进行账号绑定
     * 
     * @param provider 第三方服务商标识
     * @param configId 配置实例ID  
     * @param code 授权码
     * @param state 状态参数
     * @return 登录结果，可能需要进一步绑定
     */
    @GetMapping("/oauth/{provider}/callback")
    public ResponseEntity<LoginResult> handleOAuthCallback(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        
        log.info("处理OAuth回调: provider={}, configId={}, state={}", provider, configId, state);
        
        try {
            // 1. 处理授权码，获取认证结果
            OidcAuthResult oidcResult = oidcService.handleAuthorizationCode(provider, configId, code, state);
            if (!oidcResult.isSuccess()) {
                log.error("OAuth认证失败: provider={}, error={}", provider, oidcResult.getError());
                throw new RuntimeException("OAuth认证失败: " + oidcResult.getError());
            }
            
            // 2. 查找或创建账号
            Account account = findOrCreateAccount(provider, configId, oidcResult);
            
            // 3. 设置认证信息并生成JWT
            Authentication authentication = createOAuthAuthentication(account, oidcResult);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            LoginResult result = unifiedJwtService.generateJwtFromAccount(account);
            
            log.info("OAuth登录成功: provider={}, identifier={}, accountId={}, userStatus={}", 
                    provider, account.getIdentifier(), account.getId().getValue(), 
                    account.isPendingBind() ? "PENDING_BIND" : "ACTIVE");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("OAuth回调处理失败: provider={}, configId={}", provider, configId, e);
            throw new RuntimeException("OAuth登录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查找或创建OIDC账号
     */
    private Account findOrCreateAccount(String provider, String configId, OidcAuthResult oidcResult) {
        // 先尝试查找现有账号
        Optional<Account> existingAccount = oidcAccountService.findAccount(
            provider, configId, oidcResult.getOpenId());
        
        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            log.info("找到现有OAuth账号: provider={}, identifier={}, accountId={}", 
                provider, oidcResult.getOpenId(), account.getId().getValue());
            return account;
        }
        
        // 创建新账号前检查用户信息
        if (oidcResult.getUserInfo() == null) {
            log.error("OAuth认证结果中缺少用户信息: provider={}, openId={}", 
                provider, oidcResult.getOpenId());
            throw new RuntimeException("OAuth认证结果中缺少用户信息，请检查提供商配置");
        }
        
        Account newAccount = oidcAccountService.createAccount(oidcResult, configId);
        log.info("创建新OAuth账号: provider={}, identifier={}, accountId={}", 
            provider, oidcResult.getOpenId(), newAccount.getId().getValue());
        
        return newAccount;
    }

    /**
     * 创建OAuth认证对象
     * 基于Account信息创建Spring Security的Authentication对象
     */
    private Authentication createOAuthAuthentication(Account account, OidcAuthResult oidcResult) {
        // 创建认证对象，使用账号标识符作为principal
        return new UsernamePasswordAuthenticationToken(
            account.getIdentifier(), 
            null, // OAuth认证不需要密码
            java.util.Collections.singletonList(() -> "ROLE_USER")
        );
    }
} 