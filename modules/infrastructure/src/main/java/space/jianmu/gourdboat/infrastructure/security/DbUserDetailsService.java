package space.jianmu.gourdboat.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import space.jianmu.gourdboat.domain.account.AccountRepository;
import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.account.security.AccountUserDetails;

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public DbUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 默认使用PASSWORD提供商进行查询（最常见的登录方式）
        return loadUserByProviderAndIdentifier(AuthProvider.PASSWORD, identifier);
    }

    /**
     * 根据认证提供商和标识符加载用户（利用组合索引优化查询性能）
     */
    public UserDetails loadUserByProviderAndIdentifier(AuthProvider provider, String identifier) 
            throws UsernameNotFoundException {
        return accountRepository.findByProviderAndIdentifier(provider, identifier)
                .map(AccountUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("账号不存在: provider=%s, identifier=%s", provider, identifier)));
    }
} 