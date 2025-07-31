package space.jianmu.gourdboat.infrastructure.persistence.user;

import space.jianmu.gourdboat.domain.user.*;

/**
 * 用户实体映射器
 * 负责领域对象与持久化实体之间的转换
 */
public class UserEntityMapper {
    
    /**
     * 将领域用户对象转换为持久化实体
     */
    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId().getValue());
        entity.setPhoneNumber(user.getPhoneNumber().getFullNumber());
        entity.setNickname(user.getNickname().getValue());
        entity.setAvatar(user.getAvatar());
        entity.setStatus(user.getStatus().name());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
    
    /**
     * 将持久化实体转换为领域用户对象
     */
    public static User toDomain(UserEntity entity) {
        // 解析手机号（从完整格式中提取国家代码和号码）
        PhoneNumber phoneNumber = parsePhoneNumber(entity.getPhoneNumber());
        
        return User.reconstruct(
                UserId.of(entity.getId()),
                phoneNumber,
                Nickname.of(entity.getNickname()),
                entity.getAvatar(),
                UserStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * 解析完整手机号格式（+86xxxxxxxxxxxx）为PhoneNumber对象
     */
    private static PhoneNumber parsePhoneNumber(String fullPhoneNumber) {
        if (fullPhoneNumber == null || !fullPhoneNumber.startsWith("+")) {
            throw new IllegalArgumentException("无效的手机号格式: " + fullPhoneNumber);
        }
        
        // 移除前缀+号
        String withoutPlus = fullPhoneNumber.substring(1);
        
        // 简单解析：前1-3位作为国家代码，其余作为号码
        // 中国(86)、美国(1)、英国(44)等
        String countryCode;
        String number;
        
        if (withoutPlus.startsWith("86") && withoutPlus.length() > 2) {
            countryCode = "86";
            number = withoutPlus.substring(2);
        } else if (withoutPlus.startsWith("1") && withoutPlus.length() > 1) {
            countryCode = "1";
            number = withoutPlus.substring(1);
        } else if (withoutPlus.startsWith("44") && withoutPlus.length() > 2) {
            countryCode = "44";
            number = withoutPlus.substring(2);
        } else {
            // 默认假设国家代码是前2位
            if (withoutPlus.length() > 2) {
                countryCode = withoutPlus.substring(0, 2);
                number = withoutPlus.substring(2);
            } else {
                throw new IllegalArgumentException("无法解析手机号: " + fullPhoneNumber);
            }
        }
        
        return PhoneNumber.of(countryCode, number);
    }
} 