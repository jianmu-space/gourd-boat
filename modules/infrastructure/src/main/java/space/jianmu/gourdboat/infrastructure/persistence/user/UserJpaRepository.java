package space.jianmu.gourdboat.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    
    /**
     * 根据手机号查找用户
     */
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    
    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhoneNumber(String phoneNumber);
} 