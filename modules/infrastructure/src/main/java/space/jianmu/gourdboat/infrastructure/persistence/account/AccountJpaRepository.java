package space.jianmu.gourdboat.infrastructure.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {
    // 根据认证提供商和标识符查找账号（利用组合索引）
    Optional<AccountEntity> findByProviderAndIdentifier(String provider, String identifier);
}
