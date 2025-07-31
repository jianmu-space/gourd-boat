package space.jianmu.gourdboat.infrastructure.persistence.user;

import org.springframework.stereotype.Repository;
import space.jianmu.gourdboat.domain.user.PhoneNumber;
import space.jianmu.gourdboat.domain.user.User;
import space.jianmu.gourdboat.domain.user.UserId;
import space.jianmu.gourdboat.domain.user.UserRepository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByPhoneNumber(PhoneNumber phoneNumber) {
        return jpaRepository.findByPhoneNumber(phoneNumber.getFullNumber())
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return jpaRepository.findById(userId.getValue())
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntityMapper.toEntity(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return UserEntityMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return jpaRepository.existsByPhoneNumber(phoneNumber.getFullNumber());
    }
} 