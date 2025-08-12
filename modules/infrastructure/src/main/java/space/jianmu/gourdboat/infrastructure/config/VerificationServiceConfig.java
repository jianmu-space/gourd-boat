package space.jianmu.gourdboat.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import space.jianmu.gourdboat.application.verification.VerificationCodeService;
import space.jianmu.gourdboat.infrastructure.verification.MockVerificationCodeService;

/**
 * 验证码服务配置
 * 根据环境注入不同实现
 */
@Configuration
public class VerificationServiceConfig {

    @Bean
    @Profile("dev")
    public VerificationCodeService mockVerificationCodeService() {
        return new MockVerificationCodeService();
    }

    // TODO: 添加生产环境的真实实现
    // @Bean
    // @Profile("prod")
    // public VerificationCodeService realVerificationCodeService() {
    //     return new RealVerificationCodeService();
    // }
} 