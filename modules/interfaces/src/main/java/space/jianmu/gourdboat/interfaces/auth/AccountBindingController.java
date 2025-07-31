package space.jianmu.gourdboat.interfaces.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jianmu.gourdboat.application.auth.AccountBindingService;
import space.jianmu.gourdboat.application.auth.UnifiedJwtService;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;
import space.jianmu.gourdboat.application.verification.VerificationCodeService;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.user.PhoneNumber;
import space.jianmu.gourdboat.infrastructure.security.JwtTokenProvider;

/**
 * 账号绑定控制器
 * 专门处理账号与用户绑定的业务：将PENDING_BIND状态的账号绑定到用户
 * 
 * 设计原则：
 * - 完全依赖Spring Security的JWT认证
 * - 从SecurityContext获取当前用户的认证信息
 * - 不需要在请求参数中重复传递token
 * 
 * 核心场景：
 * - OIDC登录后，账号处于PENDING_BIND状态，需要绑定到具体用户
 * - 支持多种绑定方式：手机号验证、密码验证等
 * - 绑定完成后，账号状态从PENDING_BIND变为ACTIVE
 * 
 * 安全机制：
 * - Spring Security自动验证JWT有效性
 * - 从JWT Claims中提取账号信息
 * - 验证用户状态必须为PENDING_BIND
 */
@Slf4j
@RestController
@RequestMapping("/api/account-binding")
@RequiredArgsConstructor
public class AccountBindingController {

    private final AccountBindingService accountBindingService;
    private final VerificationCodeService verificationCodeService;
    private final UnifiedJwtService unifiedJwtService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 检查当前账号绑定状态
     * 从Spring Security上下文中获取当前用户信息
     * 
     * @return 绑定状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkBindingStatus() {
        log.info("检查当前账号绑定状态");
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证账号绑定条件
            AccountBindingService.BindingValidationResult validation = 
                accountBindingService.validateAccountBinding(accountInfo.accountId());
            
            return ResponseEntity.ok(Map.of(
                "canBind", validation.canBind(),
                "reason", validation.reason(),
                "accountStatus", validation.accountStatus(),
                "accountId", accountInfo.accountId(),
                "provider", accountInfo.provider(),
                "userStatus", accountInfo.userStatus()
            ));
            
        } catch (Exception e) {
            log.error("检查账号绑定状态失败", e);
            return ResponseEntity.ok(Map.of(
                "canBind", false,
                "reason", "状态检查失败: " + e.getMessage(),
                "accountStatus", null,
                "accountId", null
            ));
        }
    }

    /**
     * 发送手机验证码
     * 利用当前认证用户的信息，无需传递token
     * 
     * @param request 发送验证码请求
     * @return 发送结果
     */
    @PostMapping("/phone/send-code")
    public ResponseEntity<Map<String, Object>> sendPhoneVerificationCode(@RequestBody SendPhoneCodeRequest request) {
        log.info("发送手机验证码请求: phoneNumber={}", request.phoneNumber());
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证用户状态必须为PENDING_BIND
            if (!"PENDING_BIND".equals(accountInfo.userStatus())) {
                throw new IllegalStateException("当前账号状态不支持绑定操作，状态: " + accountInfo.userStatus());
            }
            
            // 3. 创建手机号对象
            PhoneNumber phoneNumber = PhoneNumber.of(request.countryCode(), request.phoneNumber());
            
            // 4. 发送验证码
            VerificationCodeService.SendCodeResult result = verificationCodeService.sendCode(
                phoneNumber.getFullNumber(), "BIND_ACCOUNT");
            
            log.info("手机验证码发送结果: accountId={}, phoneNumber={}, success={}", 
                    accountInfo.accountId(), phoneNumber, result.success());
            
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "message", result.message(),
                "remainingAttempts", result.remainingAttempts()
            ));
            
        } catch (Exception e) {
            log.error("发送手机验证码失败: phoneNumber={}", request.phoneNumber(), e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "发送验证码失败: " + e.getMessage(),
                "remainingAttempts", 0
            ));
        }
    }
    
    /**
     * 通过手机号绑定账号
     * 利用当前认证信息，无需传递token参数
     * 
     * @param request 手机号绑定请求
     * @return 绑定成功后的登录结果
     */
    @PostMapping("/phone/bind")
    public ResponseEntity<LoginResult> bindAccountByPhone(@RequestBody BindByPhoneRequest request) {
        log.info("通过手机号绑定账号: phoneNumber={}", request.phoneNumber());
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证用户状态必须为PENDING_BIND
            if (!"PENDING_BIND".equals(accountInfo.userStatus())) {
                throw new IllegalStateException("当前账号状态不支持绑定操作，状态: " + accountInfo.userStatus());
            }
            
            // 3. 创建手机号对象
            PhoneNumber phoneNumber = PhoneNumber.of(request.countryCode(), request.phoneNumber());
            
            // 4. 使用绑定服务进行绑定（包含验证码验证）
            Account boundAccount = accountBindingService.bindAccountByPhoneVerification(
                accountInfo.accountId(), 
                phoneNumber, 
                request.verificationCode(),
                request.nickname()
            );
            
            // 5. 更新SecurityContext中的认证信息
            Authentication newAuthentication = createAuthentication(boundAccount);
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            
            // 6. 生成更新后的JWT
            LoginResult result = unifiedJwtService.generateJwtFromAccount(boundAccount);
       
            log.info("账号绑定成功: accountId={}, phoneNumber={}, userId={}", 
                    accountInfo.accountId(), phoneNumber, boundAccount.getUserId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("账号绑定失败: phoneNumber={}", request.phoneNumber(), e);
            throw new RuntimeException("账号绑定失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过密码绑定账号
     * 利用当前认证信息，无需传递token参数
     * 
     * @param request 密码绑定请求
     * @return 绑定成功后的登录结果
     */
    @PostMapping("/password/bind")
    public ResponseEntity<LoginResult> bindAccountByPassword(@RequestBody BindByPasswordRequest request) {
        log.info("通过密码绑定账号: existingUserIdentifier={}", request.existingUserIdentifier());
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证用户状态必须为PENDING_BIND
            if (!"PENDING_BIND".equals(accountInfo.userStatus())) {
                throw new IllegalStateException("当前账号状态不支持绑定操作，状态: " + accountInfo.userStatus());
            }
            
            // 3. 使用绑定服务进行密码验证绑定
            Account boundAccount = accountBindingService.bindAccountByPasswordVerification(
                accountInfo.accountId(), 
                request.existingUserIdentifier(),
                request.password()
            );
            
            // 4. 更新SecurityContext中的认证信息
            Authentication newAuthentication = createAuthentication(boundAccount);
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            
            // 5. 生成更新后的JWT
            LoginResult result = unifiedJwtService.generateJwtFromAccount(boundAccount);
       
            log.info("账号绑定成功: accountId={}, existingUserIdentifier={}, userId={}", 
                    accountInfo.accountId(), request.existingUserIdentifier(), boundAccount.getUserId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("账号绑定失败: existingUserIdentifier={}", request.existingUserIdentifier(), e);
            throw new RuntimeException("账号绑定失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从Spring Security上下文获取当前账号信息
     * 
     * @return 当前账号信息
     * @throws IllegalStateException 如果无法获取有效的认证信息
     */
    private CurrentAccountInfo getCurrentAccountInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("未找到有效的认证信息");
        }

        try {
            // 从当前请求中提取JWT token
            String token = getCurrentJwtToken();
            if (token == null) {
                throw new IllegalStateException("未找到JWT令牌");
            }
            
            // 解析JWT中的claims
            var claims = jwtTokenProvider.getClaimsFromToken(token);
            
            String accountId = claims.get("accountId", String.class);
            String provider = claims.get("provider", String.class);
            String userStatus = claims.get("userStatus", String.class);
            
            if (accountId == null) {
                throw new IllegalStateException("JWT中缺少accountId信息");
            }
            
            return new CurrentAccountInfo(accountId, provider, userStatus);
            
        } catch (Exception e) {
            log.error("获取当前账号信息失败", e);
            throw new IllegalStateException("无法解析当前用户信息: " + e.getMessage());
        }
    }

    /**
     * 获取当前请求中的JWT token
     * 从Authorization header中提取Bearer token
     */
    private String getCurrentJwtToken() {
        // 从当前请求上下文获取HttpServletRequest
        try {
            // 使用Spring的RequestContextHolder获取当前请求
            var requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                var servletRequestAttributes = (org.springframework.web.context.request.ServletRequestAttributes) requestAttributes;
                var request = servletRequestAttributes.getRequest();
                
                String bearerToken = request.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("获取JWT token失败", e);
            return null;
        }
    }

    /**
     * 创建认证对象
     * 为绑定成功的账号创建Spring Security的Authentication对象
     */
    private Authentication createAuthentication(Account account) {
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            account.getIdentifier(), 
            null, // 绑定后不需要密码
            java.util.Collections.singletonList(() -> "ROLE_USER")
        );
    }
    
    /**
     * 当前账号信息
     */
    public record CurrentAccountInfo(
        String accountId,   // 账号ID
        String provider,    // 认证提供商
        String userStatus   // 用户状态
    ) {}
    
    /**
     * 发送手机验证码请求
     */
    public record SendPhoneCodeRequest(
        String countryCode,    // 国家代码，如 "86"
        String phoneNumber     // 手机号
    ) {}
    
    /**
     * 通过手机号绑定请求
     */
    public record BindByPhoneRequest(
        String countryCode,      // 国家代码
        String phoneNumber,      // 手机号
        String nickname,         // 用户昵称
        String verificationCode  // 验证码
    ) {}
    
    /**
     * 通过密码绑定请求
     */
    public record BindByPasswordRequest(
        String existingUserIdentifier,  // 现有用户标识符（用户名/邮箱）
        String password                 // 用户密码
    ) {}
} 