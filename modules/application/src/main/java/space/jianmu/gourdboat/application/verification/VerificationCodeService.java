package space.jianmu.gourdboat.application.verification;

/**
 * 验证码服务接口
 * 用于手机号验证码的生成、发送和验证
 */
public interface VerificationCodeService {
    
    /**
     * 发送手机号验证码
     * @param phoneNumber 手机号（格式：+86xxxxxxxxxx）
     * @param purpose 验证码用途（如：BIND_ACCOUNT）
     * @return 发送结果
     */
    SendCodeResult sendCode(String phoneNumber, String purpose);
    
    /**
     * 验证手机号验证码
     * @param phoneNumber 手机号
     * @param code 验证码
     * @param purpose 验证码用途
     * @return 验证结果
     */
    VerifyCodeResult verifyCode(String phoneNumber, String code, String purpose);
    
    /**
     * 发送验证码结果
     */
    record SendCodeResult(
        boolean success,
        String message,
        int remainingAttempts    // 剩余发送次数
    ) {}
    
    /**
     * 验证码验证结果
     */
    record VerifyCodeResult(
        boolean success,
        String message,
        int remainingAttempts    // 剩余验证次数
    ) {}
} 