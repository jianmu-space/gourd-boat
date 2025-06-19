package space.jianmu.gourdboat.domain.verification;

import lombok.Value;
import java.time.LocalDateTime;

/**
 * 验证码值对象
 * 用于各种需要验证码的场景(注册、重置密码等)
 */
@Value
public class VerificationCode {
    String target;               // 验证目标(如手机号、邮箱等)
    String code;                 // 验证码
    LocalDateTime createdAt;     // 创建时间
    LocalDateTime expiredAt;     // 过期时间
    int attempts;                // 验证尝试次数
    
    private static final int MAX_ATTEMPTS = 5;          // 最大尝试次数
    private static final int EXPIRY_MINUTES = 5;        // 有效期(分钟)
    
    private VerificationCode(String target, String code, 
                           LocalDateTime createdAt, int attempts) {
        this.target = target;
        this.code = code;
        this.createdAt = createdAt;
        this.expiredAt = createdAt.plusMinutes(EXPIRY_MINUTES);
        this.attempts = attempts;
    }
    
    /**
     * 创建新的验证码
     * @param target 验证目标(如手机号、邮箱等)
     * @param code 验证码内容
     */
    public static VerificationCode create(String target, String code) {
        return new VerificationCode(target, code, LocalDateTime.now(), 0);
    }
    
    /**
     * 验证码是否有效
     */
    public boolean isValid() {
        return attempts < MAX_ATTEMPTS && !isExpired();
    }
    
    /**
     * 验证码是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }
    
    /**
     * 验证码验证
     * 返回新的验证码实例,包含更新后的尝试次数
     * 如果验证码无效,返回当前实例
     */
    public VerificationCode verify(String verifyCode) {
        if (!isValid()) {
            return this;
        }
        if (!code.equals(verifyCode)) {
            return new VerificationCode(target, code, createdAt, attempts + 1);
        }
        return this;
    }
} 