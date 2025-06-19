package space.jianmu.gourdboat.domain.account;

import java.util.Optional;

public interface AccountRepository {
    // 根据认证提供商和标识符查找账号（利用组合索引优化）
    Optional<Account> findByProviderAndIdentifier(AuthProvider provider, String identifier);
} 