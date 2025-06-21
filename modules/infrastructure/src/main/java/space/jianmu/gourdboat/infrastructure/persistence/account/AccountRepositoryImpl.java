package space.jianmu.gourdboat.infrastructure.persistence.account;

import org.springframework.stereotype.Repository;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.account.AccountRepository;
import space.jianmu.gourdboat.domain.account.AuthProvider;

import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountJpaRepository jpaRepository;

    public AccountRepositoryImpl(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Account> findByProviderAndIdentifier(AuthProvider provider, String identifier) {
        return jpaRepository.findByProviderAndIdentifier(provider.getValue(), identifier)
                .map(AccountEntityMapper::toDomain);
    }
} 