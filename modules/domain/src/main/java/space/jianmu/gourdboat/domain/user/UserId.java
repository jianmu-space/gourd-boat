package space.jianmu.gourdboat.domain.user;

import lombok.Value;

/**
 * 用户ID值对象
 */
@Value
public class UserId {
    String value;
    
    private UserId(String value) {
        this.value = value;
    }
    
    /**
     * 生成新的用户ID
     */
    public static UserId generate() {
        return new UserId(java.util.UUID.randomUUID().toString());
    }
    
    /**
     * 从字符串创建用户ID
     */
    public static UserId of(String value) {
        return new UserId(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
} 