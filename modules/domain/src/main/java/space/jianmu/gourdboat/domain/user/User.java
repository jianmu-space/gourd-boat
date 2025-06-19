package space.jianmu.gourdboat.domain.user;

import lombok.Value;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 以手机号作为唯一标识
 * 不包含认证相关的信息(密码、OIDC等)
 */
@Value
public class User {
    UserId id;                    // 用户ID
    PhoneNumber phoneNumber;      // 手机号(唯一标识)
    Nickname nickname;            // 用户昵称(用于社交展示)
    UserStatus status;            // 用户状态
    LocalDateTime createdAt;      // 创建时间
    LocalDateTime updatedAt;      // 更新时间
    
    private User(UserId id, PhoneNumber phoneNumber, Nickname nickname, UserStatus status, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 通过手机号和昵称创建用户
     */
    public static User create(PhoneNumber phoneNumber, Nickname nickname) {
        return new User(
            UserId.generate(),
            phoneNumber,
            nickname,
            UserStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
    
    /**
     * 更新用户状态
     * 返回新的用户实例
     */
    public User withStatus(UserStatus newStatus) {
        return new User(
            id,
            phoneNumber,
            nickname,
            newStatus,
            createdAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 更新用户昵称
     * 返回新的用户实例
     */
    public User withNickname(Nickname newNickname) {
        return new User(
            id,
            phoneNumber,
            newNickname,
            status,
            createdAt,
            LocalDateTime.now()
        );
    }
} 