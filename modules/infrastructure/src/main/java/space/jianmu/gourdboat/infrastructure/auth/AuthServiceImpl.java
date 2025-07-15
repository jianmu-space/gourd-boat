package space.jianmu.gourdboat.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import space.jianmu.gourdboat.application.auth.AuthService;
import space.jianmu.gourdboat.application.auth.UnifiedJwtService;
import space.jianmu.gourdboat.application.auth.command.LoginCommand;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.account.AccountRepository;
import space.jianmu.gourdboat.domain.account.AuthProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UnifiedJwtService unifiedJwtService;
    private final AccountRepository accountRepository;
    
    @Override
    public LoginResult login(LoginCommand command) {
        log.info("传统账号密码登录请求: {}", command);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String identifier = authentication.getName();
            
            // 只查找密码认证类型的账号（明确区分传统登录和OIDC登录）
            var accountOpt = accountRepository.findByProviderAndIdentifier(
                new AuthProvider(AuthProvider.PASSWORD), identifier);
            
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                // 使用统一的JWT服务生成token
                log.info("使用统一JWT服务生成token: identifier={}", identifier);
                return unifiedJwtService.generateJwtFromAccount(account);
            } else {
                // 账号不存在，登录失败
                log.error("未找到PASSWORD类型的账号: identifier={}", identifier);
                throw new RuntimeException("账号不存在或认证类型不匹配");
            }
        } catch (Exception e) {
            log.error("传统账号密码登录失败: {}", command, e);
            throw e;
        }
    }
} 