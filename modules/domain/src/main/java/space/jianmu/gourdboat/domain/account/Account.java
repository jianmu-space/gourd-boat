package space.jianmu.gourdboat.domain.account;

import lombok.Value;
import lombok.Builder;
import space.jianmu.gourdboat.domain.user.UserId;

import java.time.LocalDateTime;

/**
 * 账号实体
 * 支持内部账号(密码认证)和外部账号(Google、GitHub、微信等)
 * 每个账号关联到一个用户(通过手机号)
 * 
 * 对于OIDC账号，可能存在两种状态：
 * 1. PENDING_BIND - 刚完成OIDC授权，userId为null，待绑定手机号
 * 2. ACTIVE - 已绑定手机号，userId不为null
 */
@Value
public class Account {
    AccountId id;                // 账号ID
    UserId userId;               // 关联的用户ID（OIDC待绑定状态时可能为null）
    AccountType type;            // 账号类型(内部/外部)
    AuthProvider provider;       // 认证服务商
    String identifier;           // 账号标识(如Google subject、GitHub ID、openId等)
    String password;           // 账号密码（加密存储，仅内部账号使用）
    AccountStatus status;        // 账号状态
    String configId;             // OIDC配置ID（用于区分同一provider的不同配置实例）
    String unionId;              // UnionId（微信等平台的联合用户标识）
    LocalDateTime createdAt;     // 创建时间
    LocalDateTime updatedAt;     // 更新时间
    
    @Builder(builderMethodName = "reconstruct")
    private Account(AccountId id, UserId userId, AccountType type,
                   AuthProvider provider, String identifier, String password, AccountStatus status,
                   String configId, String unionId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.provider = provider;
        this.identifier = identifier;
        this.password = password;
        this.status = status;
        this.configId = configId;
        this.unionId = unionId;
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
            configId,
            unionId,
            createdAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 绑定用户ID，将状态从PENDING_BIND更新为ACTIVE
     * 返回新的账号实例
     */
    public Account bindUser(UserId newUserId) {
        if (status != AccountStatus.PENDING_BIND) {
            throw new IllegalStateException("只有待绑定状态的账号才能绑定用户");
        }
        if (newUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        return new Account(
            id,
            newUserId,
            type,
            provider,
            identifier,
            password,
            AccountStatus.ACTIVE,
            configId,
            unionId,
            createdAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 检查是否为待绑定状态
     */
    public boolean isPendingBind() {
        return status == AccountStatus.PENDING_BIND;
    }
    
    /**
     * 检查是否已绑定用户
     */
    public boolean isBound() {
        return userId != null && status == AccountStatus.ACTIVE;
    }
} 