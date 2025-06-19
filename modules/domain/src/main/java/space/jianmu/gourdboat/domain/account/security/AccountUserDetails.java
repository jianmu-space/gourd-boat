package space.jianmu.gourdboat.domain.account.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.account.AccountStatus;

import java.util.Collection;
import java.util.Collections;

public class AccountUserDetails implements UserDetails {
    private final Account account;

    public AccountUserDetails(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回默认用户角色
        return Collections.singletonList(() -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        // 返回账号密码用于认证
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getIdentifier();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !account.getStatus().equals(AccountStatus.LOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.getStatus().equals(AccountStatus.ACTIVE);
    }

    public Account getAccount() {
        return account;
    }
} 