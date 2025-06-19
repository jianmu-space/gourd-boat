package space.jianmu.gourdboat.domain.account;

import lombok.Value;
import lombok.Builder;
import space.jianmu.gourdboat.domain.user.UserId;

import java.time.LocalDateTime;

/**
 * 账号实体
 * 支持内部账号(密码认证)和外部账号(Google、GitHub、微信等)
 * 每个账号关联到一个用户(通过手机号)
 */
@Value
public class Account {
    AccountId id;                // 账号ID
    UserId userId;               // 关联的用户ID
    AccountType type;            // 账号类型(内部/外部)
    AuthProvider provider;       // 认证服务商
    String identifier;           // 账号标识(如Google subject、GitHub ID等)
    String password;           // 账号密码（加密存储，仅内部账号使用）
    AccountStatus status;        // 账号状态
    LocalDateTime createdAt;     // 创建时间
    LocalDateTime updatedAt;     // 更新时间
    
    @Builder(builderMethodName = "reconstruct")
    private Account(AccountId id, UserId userId, AccountType type,
                   AuthProvider provider, String identifier, String password, AccountStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.provider = provider;
        this.identifier = identifier;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 更新账号状态
     * 返回新的账号实例
     */
    public Account withStatus(AccountStatus newStatus) {
        return new Account(
            id,
            userId,
            type,
            provider,
            identifier,
            password,
            newStatus,
            createdAt,
            LocalDateTime.now()
        );
    }
} 