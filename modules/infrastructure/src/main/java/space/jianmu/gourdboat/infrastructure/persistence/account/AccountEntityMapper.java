package space.jianmu.gourdboat.infrastructure.persistence.account;

import space.jianmu.gourdboat.domain.account.*;
import space.jianmu.gourdboat.domain.user.UserId;

public class AccountEntityMapper {
    private AccountEntityMapper() {
        // 工具类，防止实例化
    }
    
    public static Account toDomain(AccountEntity entity) {
        return Account.reconstruct()
                .id(AccountId.of(entity.getId()))
                .userId(entity.getUserId() != null ? UserId.of(entity.getUserId()) : null)
                .type(AccountType.valueOf(entity.getType()))
                .provider(AuthProvider.of(entity.getProvider()))
                .identifier(entity.getIdentifier())
                .password(entity.getPassword())
                .status(AccountStatus.valueOf(entity.getStatus()))
                .configId(entity.getConfigId())
                .unionId(entity.getUnionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public static AccountEntity toEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setId(account.getId().getValue());
        entity.setUserId(account.getUserId() != null ? account.getUserId().getValue() : null);
        entity.setType(account.getType().name());
        entity.setProvider(account.getProvider().getValue());
        entity.setIdentifier(account.getIdentifier());
        entity.setPassword(account.getPassword());
        entity.setStatus(account.getStatus().name());
        entity.setConfigId(account.getConfigId());
        entity.setUnionId(account.getUnionId());
        entity.setCreatedAt(account.getCreatedAt());
        entity.setUpdatedAt(account.getUpdatedAt());
        return entity;
    }
}
