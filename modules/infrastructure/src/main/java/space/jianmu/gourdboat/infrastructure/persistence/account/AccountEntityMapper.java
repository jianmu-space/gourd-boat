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
                .userId(UserId.of(entity.getUserId()))
                .type(AccountType.valueOf(entity.getType()))
                .provider(AuthProvider.of(entity.getProvider()))
                .identifier(entity.getIdentifier())
                .password(entity.getPassword())
                .status(AccountStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
