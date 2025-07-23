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
 * 账号关联控制器
 * 专门处理账号与用户关联的业务：将PENDING_BIND状态的账号关联到用户
 * 
 * 设计原则：
 * - 完全依赖Spring Security的JWT认证
 * - 从SecurityContext获取当前用户的认证信息
 * - 不需要在请求参数中重复传递token
 * 
 * 核心场景：
 * - OIDC登录后，账号处于PENDING_BIND状态，需要关联到具体用户
 * - 支持多种关联方式：手机号验证、密码验证等
 * - 关联完成后，账号状态从PENDING_BIND变为ACTIVE
 * 
 * 安全机制：
 * - Spring Security自动验证JWT有效性
 * - 从JWT Claims中提取账号信息
 * - 验证用户状态必须为PENDING_BIND
 */
@Slf4j
@RestController
@RequestMapping("/api/account-linking")
@RequiredArgsConstructor
public class AccountLinkingController {

    private final AccountBindingService accountBindingService;
    private final VerificationCodeService verificationCodeService;
    private final UnifiedJwtService unifiedJwtService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 检查当前账号关联状态
     * 从Spring Security上下文中获取当前用户信息
     * 
     * @return 关联状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkLinkingStatus() {
        log.info("检查当前账号关联状态");
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证账号关联条件
            AccountBindingService.BindingValidationResult validation = 
                accountBindingService.validateAccountBinding(accountInfo.accountId());
            
            return ResponseEntity.ok(Map.of(
                "canLink", validation.canBind(),
                "reason", validation.reason(),
                "accountStatus", validation.accountStatus(),
                "accountId", accountInfo.accountId(),
                "provider", accountInfo.provider(),
                "userStatus", accountInfo.userStatus()
            ));
            
        } catch (Exception e) {
            log.error("检查账号关联状态失败", e);
            return ResponseEntity.ok(Map.of(
                "canLink", false,
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
                throw new IllegalStateException("当前账号状态不支持关联操作，状态: " + accountInfo.userStatus());
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
     * 通过手机号关联账号
     * 利用当前认证信息，无需传递token参数
     * 
     * @param request 手机号关联请求
     * @return 关联成功后的登录结果
     */
    @PostMapping("/phone/link")
    public ResponseEntity<LoginResult> linkAccountByPhone(@RequestBody LinkByPhoneRequest request) {
        log.info("通过手机号关联账号: phoneNumber={}", request.phoneNumber());
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证用户状态必须为PENDING_BIND
            if (!"PENDING_BIND".equals(accountInfo.userStatus())) {
                throw new IllegalStateException("当前账号状态不支持关联操作，状态: " + accountInfo.userStatus());
            }
            
            // 3. 创建手机号对象
            PhoneNumber phoneNumber = PhoneNumber.of(request.countryCode(), request.phoneNumber());
            
            // 4. 使用关联服务进行关联（包含验证码验证）
            Account linkedAccount = accountBindingService.bindAccountByPhoneVerification(
                accountInfo.accountId(), 
                phoneNumber, 
                request.verificationCode(),
                request.nickname()
            );
            
            // 5. 更新SecurityContext中的认证信息
            Authentication newAuthentication = createAuthentication(linkedAccount);
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            
            // 6. 生成更新后的JWT
            LoginResult result = unifiedJwtService.generateJwtFromAccount(linkedAccount);
       
            log.info("账号关联成功: accountId={}, phoneNumber={}, userId={}", 
                    accountInfo.accountId(), phoneNumber, linkedAccount.getUserId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("账号关联失败: phoneNumber={}", request.phoneNumber(), e);
            throw new RuntimeException("账号关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过密码关联账号
     * 利用当前认证信息，无需传递token参数
     * 
     * @param request 密码关联请求
     * @return 关联成功后的登录结果
     */
    @PostMapping("/password/link")
    public ResponseEntity<LoginResult> linkAccountByPassword(@RequestBody LinkByPasswordRequest request) {
        log.info("通过密码关联账号: existingUserIdentifier={}", request.existingUserIdentifier());
        
        try {
            // 1. 从Spring Security上下文获取账号信息
            CurrentAccountInfo accountInfo = getCurrentAccountInfo();
            
            // 2. 验证用户状态必须为PENDING_BIND
            if (!"PENDING_BIND".equals(accountInfo.userStatus())) {
                throw new IllegalStateException("当前账号状态不支持关联操作，状态: " + accountInfo.userStatus());
            }
            
            // 3. 使用关联服务进行密码验证关联
            Account linkedAccount = accountBindingService.bindAccountByPasswordVerification(
                accountInfo.accountId(), 
                request.existingUserIdentifier(),
                request.password()
            );
            
            // 4. 更新SecurityContext中的认证信息
            Authentication newAuthentication = createAuthentication(linkedAccount);
            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            
            // 5. 生成更新后的JWT
            LoginResult result = unifiedJwtService.generateJwtFromAccount(linkedAccount);
       
            log.info("账号关联成功: accountId={}, existingUserIdentifier={}, userId={}", 
                    accountInfo.accountId(), request.existingUserIdentifier(), linkedAccount.getUserId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("账号关联失败: existingUserIdentifier={}", request.existingUserIdentifier(), e);
            throw new RuntimeException("账号关联失败: " + e.getMessage(), e);
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
     * 为关联成功的账号创建Spring Security的Authentication对象
     */
    private Authentication createAuthentication(Account account) {
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            account.getIdentifier(), 
            null, // 关联后不需要密码
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
     * 发送手机验证码请求（移除了linkToken参数）
     */
    public record SendPhoneCodeRequest(
        String countryCode,    // 国家代码，如 "86"
        String phoneNumber     // 手机号
    ) {}
    
    /**
     * 通过手机号关联请求（移除了linkToken参数）
     */
    public record LinkByPhoneRequest(
        String countryCode,      // 国家代码
        String phoneNumber,      // 手机号
        String nickname,         // 用户昵称
        String verificationCode  // 验证码
    ) {}
    
    /**
     * 通过密码关联请求（移除了linkToken参数）
     */
    public record LinkByPasswordRequest(
        String existingUserIdentifier,  // 现有用户标识符（用户名/邮箱）
        String password                 // 用户密码
    ) {}
} 