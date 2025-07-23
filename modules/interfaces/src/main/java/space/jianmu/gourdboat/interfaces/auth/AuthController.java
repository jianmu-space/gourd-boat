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
import space.jianmu.gourdboat.application.verification.VerificationCodeService;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.user.PhoneNumber;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final OidcService oidcService;
    private final OidcAccountService oidcAccountService;
    private final VerificationCodeService verificationCodeService;
    private final UnifiedJwtService unifiedJwtService;

    /**
     * 传统账号密码登录
     * 
     * 专门用于处理用户名/密码认证，不涉及任何第三方OIDC认证
     * OIDC认证请使用 /api/auth/oidc/callback/{provider} 接口
     * 
     * @param command 登录命令，包含用户名和密码
     * @return 登录结果，包含JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody LoginCommand command) {
        log.info("传统账号密码登录请求: identifier={}", command.identifier());
        // 使用 Spring Security 的认证机制进行用户名密码验证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.identifier(), command.password())
        );
        
        // 设置认证信息到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成 JWT token（仅限PASSWORD类型的账号）
        LoginResult result = authService.login(command);
        log.info("传统账号密码登录成功: identifier={}", command.identifier());
        return ResponseEntity.ok(result);
    }

/**
     * 生成OIDC授权URL
     * 用于前端跳转到第三方认证页面
     */
    @GetMapping("/oidc/auth/{provider}")
    public ResponseEntity<Map<String, String>> generateOidcAuthUrl(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestParam("state") String state) {
        log.info("生成OIDC授权URL: provider={}, configId={}, state={}", provider, configId, state);
        String authUrl = oidcService.generateAuthorizationUrl(provider, configId, state);
        log.debug("OIDC授权URL生成结果: {}", authUrl);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    /**
     * OIDC授权回调处理
     * 负责将OIDC认证结果转换为系统内部的JWT认证
     */
    @GetMapping("/oidc/callback/{provider}")
    public ResponseEntity<LoginResult> handleOidcCallback(
            @PathVariable("provider") String provider,
            @RequestParam("configId") String configId,
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        
        log.info("OIDC授权回调: provider={}, configId={}, state={}", provider, configId, state);
        
        try {
            // 1. 处理授权码，获取认证结果
            OidcAuthResult oidcResult = oidcService.handleAuthorizationCode(provider, configId, code, state);
            if (!oidcResult.isSuccess()) {
                log.error("OIDC授权失败: provider={}, error={}", provider, oidcResult.getError());
                throw new RuntimeException("OIDC授权失败: " + oidcResult.getError());
            }
            
            // 2. 查找或创建账号
            Account account = findOrCreateAccount(provider, configId, oidcResult);
            
            // 3. 设置认证信息并生成JWT
            Authentication authentication = createOidcAuthentication(account, oidcResult);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            LoginResult result = unifiedJwtService.generateJwtFromAccount(account);
            
            log.info("OIDC认证成功: provider={}, identifier={}, accountId={}, userStatus={}", 
                    provider, account.getIdentifier(), account.getId().getValue(), 
                    account.isPendingBind() ? "PENDING_BIND" : "ACTIVE");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("OIDC回调处理失败: provider={}, configId={}", provider, configId, e);
            throw new RuntimeException("OIDC登录失败", e);
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
            log.info("找到现有OIDC账号: provider={}, identifier={}, accountId={}", 
                provider, oidcResult.getOpenId(), account.getId().getValue());
            return account;
        }
        
        // 创建新账号前检查用户信息
        if (oidcResult.getUserInfo() == null) {
            log.error("OIDC认证结果中缺少用户信息: provider={}, openId={}", 
                provider, oidcResult.getOpenId());
            throw new RuntimeException("OIDC认证结果中缺少用户信息，请检查提供商配置");
        }
        
        Account newAccount = oidcAccountService.createAccount(oidcResult, configId);
        log.info("创建新OIDC账号: provider={}, identifier={}, accountId={}", 
            provider, oidcResult.getOpenId(), newAccount.getId().getValue());
        
        return newAccount;
    }
    
    /**
     * 发送手机号验证码
     * 用于OIDC账号绑定流程中的手机号验证
     */
    @PostMapping("/oidc/send-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody SendCodeRequest request) {
        log.info("发送验证码请求: phoneNumber={}", request.phoneNumber());
        
        try {
            // 1. 验证绑定令牌
            String accountId = unifiedJwtService.validateBindToken(request.bindToken());
            
            // 2. 创建手机号对象
            PhoneNumber phoneNumber = PhoneNumber.of(request.countryCode(), request.phoneNumber());
            
            // 3. 发送验证码
            VerificationCodeService.SendCodeResult result = verificationCodeService.sendCode(phoneNumber.getFullNumber(), "BIND_ACCOUNT");
            
            log.info("验证码发送结果: phoneNumber={}, success={}", phoneNumber, result.success());
            
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "message", result.message(),
                "remainingAttempts", result.remainingAttempts()
            ));
            
        } catch (Exception e) {
            log.error("发送验证码失败: phoneNumber={}", request.phoneNumber(), e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "发送验证码失败: " + e.getMessage(),
                "remainingAttempts", 0
            ));
        }
    }
    
    /**
     * 绑定手机号到OIDC账号
     * 用于OIDC授权成功后的用户绑定流程
     * 需要提供验证码进行安全验证
     */
    @PostMapping("/oidc/bind-phone")
    public ResponseEntity<LoginResult> bindPhoneNumber(@RequestBody PhoneBindRequest request) {
        log.info("绑定手机号请求: bindToken={}, phoneNumber={}", request.bindToken(), request.phoneNumber());
        
        try {
            // 1. 验证绑定令牌
            String accountId = unifiedJwtService.validateBindToken(request.bindToken());
            
            // 2. 创建手机号对象
            PhoneNumber phoneNumber = PhoneNumber.of(request.countryCode(), request.phoneNumber());
            
            // 3. 验证手机号验证码
            VerificationCodeService.VerifyCodeResult codeResult = verificationCodeService.verifyCode(
                phoneNumber.getFullNumber(), 
                request.verificationCode(), 
                "BIND_ACCOUNT"
            );
            
            if (!codeResult.success()) {
                throw new RuntimeException("验证码验证失败: " + codeResult.message());
            }
            
            // 4. 绑定手机号到账号
            Account boundAccount = oidcAccountService.bindToPhoneNumber(
                accountId, 
                phoneNumber, 
                request.nickname()
            );
            
            // 5. 创建认证对象并设置到SecurityContext
            Authentication authentication = createOidcAuthentication(boundAccount, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 6. 使用统一的JWT服务生成更新后的token
            LoginResult result = unifiedJwtService.generateJwtFromAccount(boundAccount);
       
            log.info("手机号绑定成功: accountId={}, phoneNumber={}, userId={}", 
                    accountId, phoneNumber, boundAccount.getUserId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("手机号绑定失败: bindToken={}, phoneNumber={}", request.bindToken(), request.phoneNumber(), e);
            throw new RuntimeException("手机号绑定失败", e);
        }
    }
    


    /**
     * 创建OIDC认证对象
     * 基于Account信息创建Spring Security的Authentication对象
     */
    private Authentication createOidcAuthentication(Account account, OidcAuthResult oidcResult) {
        // 创建认证对象，使用账号标识符作为principal
        // 这里可以根据需要扩展，添加更多的用户信息和权限
        return new UsernamePasswordAuthenticationToken(
            account.getIdentifier(), 
            null, // OIDC认证不需要密码
            java.util.Collections.singletonList(() -> "ROLE_USER") // 默认用户角色，可以根据账号类型动态设置
        );
    }
    
    /**
     * 发送验证码请求
     */
    public record SendCodeRequest(
        String bindToken,
        String countryCode,
        String phoneNumber
    ) {}
    
    /**
     * 手机号绑定请求
     */
    public record PhoneBindRequest(
        String bindToken,
        String countryCode,
        String phoneNumber,
        String nickname,
        String verificationCode
    ) {}
} 