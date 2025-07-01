package space.jianmu.gourdboat.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import space.jianmu.gourdboat.application.auth.AuthService;
import space.jianmu.gourdboat.application.auth.command.LoginCommand;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;
import space.jianmu.gourdboat.infrastructure.security.JwtTokenProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final JwtTokenProvider tokenProvider;
    
    @Override
    public LoginResult login(LoginCommand command) {
        log.info("用户登录请求: {}", command);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // 生成 JWT token
            String token = tokenProvider.generateToken(authentication);
            
            // 返回登录结果
            LoginResult result = new LoginResult(
                token,
                authentication.getName(), // 这里的getName()语义为identifier
                authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
            );
            log.info("用户登录成功: identifier={}", authentication.getName());
            return result;
        } catch (Exception e) {
            log.error("用户登录失败: {}", command, e);
            throw e;
        }
    }
} 