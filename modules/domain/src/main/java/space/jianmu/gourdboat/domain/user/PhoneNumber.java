package space.jianmu.gourdboat.domain.user;

import lombok.Value;

/**
 * 手机号值对象
 * 作为用户的唯一标识
 * 支持国际手机号格式
 */
@Value
public class PhoneNumber {
    String countryCode;  // 国家/地区代码
    String number;      // 手机号码
    
    private PhoneNumber(String countryCode, String number) {
        if (!isValidCountryCode(countryCode)) {
            throw new IllegalArgumentException("Invalid country code: " + countryCode);
        }
        if (!isValidNumber(number)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.countryCode = countryCode;
        this.number = number;
    }
    
    /**
     * 创建手机号
     * @param countryCode 国家/地区代码(如: 86, 1, 44等)
     * @param number 手机号码
     */
    public static PhoneNumber of(String countryCode, String number) {
        return new PhoneNumber(normalizeCountryCode(countryCode), normalizeNumber(number));
    }
    
    /**
     * 获取完整手机号(包含国家代码)
     */
    public String getFullNumber() {
        return "+" + countryCode + number;
    }
    
    private static boolean isValidCountryCode(String countryCode) {
        // 国家代码验证: 1-3位数字
        return countryCode != null && countryCode.matches("^[1-9]\\d{0,2}$");
    }
    
    private static boolean isValidNumber(String number) {
        // 基本格式验证: 5-15位数字
        // 具体的号段验证应该由外部服务处理
        return number != null && number.matches("^\\d{5,15}$");
    }
    
    private static String normalizeCountryCode(String countryCode) {
        // 移除可能的+号前缀
        return countryCode.replace("+", "");
    }
    
    private static String normalizeNumber(String number) {
        // 移除所有非数字字符
        return number.replaceAll("\\D", "");
    }
    
    @Override
    public String toString() {
        return getFullNumber();
    }
} 