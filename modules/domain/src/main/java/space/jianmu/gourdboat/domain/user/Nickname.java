package space.jianmu.gourdboat.domain.user;

import lombok.Value;

/**
 * 用户昵称值对象
 * 用于社交展示，可以包含emoji等特殊字符
 */
@Value
public class Nickname {
    String value;
    
    private Nickname(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (value.length() > 30) {
            throw new IllegalArgumentException("昵称长度不能超过30个字符");
        }
        // 去除首尾空白字符
        this.value = value.trim();
    }
    
    /**
     * 创建昵称
     * @param value 昵称值
     * @return 昵称对象
     */
    public static Nickname of(String value) {
        return new Nickname(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
} 