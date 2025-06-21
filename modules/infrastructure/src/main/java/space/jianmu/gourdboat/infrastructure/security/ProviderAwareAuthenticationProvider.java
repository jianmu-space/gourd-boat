package space.jianmu.gourdboat.infrastructure.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import space.jianmu.gourdboat.domain.account.AuthProvider;

/**
 * 支持多认证提供商的认证提供者
 * 优先使用组合索引查询，提升认证性能
 */
@Component
public class ProviderAwareAuthenticationProvider implements AuthenticationProvider {

    private final DbUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public ProviderAwareAuthenticationProvider(DbUserDetailsService userDetailsService, 
                                              PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String identifier = authentication.getName();
        String password = (String) authentication.getCredentials();

        // 对于用户名密码认证，优先使用PASSWORD提供商和组合索引查询
        UserDetails userDetails = userDetailsService.loadUserByProviderAndIdentifier(
            AuthProvider.of(AuthProvider.PASSWORD), identifier);
        
        // 验证密码
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        // 返回认证成功的token
        return new UsernamePasswordAuthenticationToken(
            userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 