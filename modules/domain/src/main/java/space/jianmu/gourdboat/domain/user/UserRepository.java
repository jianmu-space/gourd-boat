package space.jianmu.gourdboat.domain.user;

import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    
    /**
     * 根据手机号查找用户
     * @param phoneNumber 手机号
     * @return 用户（如果存在）
     */
    Optional<User> findByPhoneNumber(PhoneNumber phoneNumber);
    
    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户（如果存在）
     */
    Optional<User> findById(UserId userId);
    
    /**
     * 保存用户
     * @param user 用户
     * @return 保存后的用户
     */
    User save(User user);
    
    /**
     * 检查手机号是否已存在
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
} 