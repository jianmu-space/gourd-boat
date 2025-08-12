package space.jianmu.gourdboat.infrastructure.verification;

import space.jianmu.gourdboat.application.verification.VerificationCodeService;

import lombok.extern.slf4j.Slf4j;

/**
 * 模拟验证码服务实现
 * 用于开发测试，使用固定验证码"123456"
 */
@Slf4j
public class MockVerificationCodeService implements VerificationCodeService {

    private static final String FIXED_CODE = "123456";
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public SendCodeResult sendCode(String phoneNumber, String purpose) {
        log.info("模拟发送验证码到 {}，用途: {}", phoneNumber, purpose);
        return new SendCodeResult(
            true,
            "验证码已发送（模拟）",
            MAX_ATTEMPTS
        );
    }

    @Override
    public VerifyCodeResult verifyCode(String phoneNumber, String code, String purpose) {
        log.info("模拟验证验证码 {} 对于 {}，用途: {}", code, phoneNumber, purpose);
        
        boolean success = FIXED_CODE.equals(code);
        return new VerifyCodeResult(
            success,
            success ? "验证成功" : "验证码错误",
            success ? 0 : MAX_ATTEMPTS - 1
        );
    }
} 