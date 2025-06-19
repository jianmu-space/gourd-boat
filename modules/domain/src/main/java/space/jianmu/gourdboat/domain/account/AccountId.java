package space.jianmu.gourdboat.domain.account;

import lombok.Value;

/**
 * 账号ID值对象
 */
@Value
public class AccountId {
    String value;
    
    private AccountId(String value) {
        this.value = value;
    }
    
    /**
     * 生成新的账号ID
     */
    public static AccountId generate() {
        return new AccountId(java.util.UUID.randomUUID().toString());
    }
    
    /**
     * 从字符串创建账号ID
     */
    public static AccountId of(String value) {
        return new AccountId(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
} 