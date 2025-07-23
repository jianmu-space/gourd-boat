package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.user.PhoneNumber;

/**
 * 账号绑定服务
 * 专门负责处理各种账号绑定业务逻辑
 * 
 * 核心业务：将处于PENDING_BIND状态的账号绑定到用户
 * 绑定方式包括：手机号验证、邮箱验证、密码验证等
 */
public interface AccountBindingService {
    
    /**
     * 通过手机号验证绑定账号到用户
     * 这是最常见的绑定方式，通过手机号验证码确认用户身份
     * 
     * @param accountId 待绑定的账号ID（必须处于PENDING_BIND状态）
     * @param phoneNumber 用户手机号（用于查找/创建用户）
     * @param verificationCode 手机号验证码
     * @param userNickname 用户昵称（用户不存在时创建新用户使用）
     * @return 绑定完成的账号（状态已更新为ACTIVE）
     * @throws IllegalStateException 如果账号不是PENDING_BIND状态
     * @throws RuntimeException 如果验证码验证失败或其他绑定错误
     */
    Account bindAccountByPhoneVerification(String accountId, PhoneNumber phoneNumber, 
                                         String verificationCode, String userNickname);
    
    /**
     * 通过密码验证绑定账号到现有用户
     * 适用于用户已有密码账号，希望绑定第三方账号的场景
     * 
     * @param pendingAccountId 待绑定的账号ID（PENDING_BIND状态）
     * @param existingUserIdentifier 现有用户的标识符（通常是用户名或邮箱）
     * @param password 用户密码
     * @return 绑定完成的账号
     */
    Account bindAccountByPasswordVerification(String pendingAccountId, 
                                            String existingUserIdentifier, String password);
    
    /**
     * 验证账号是否可以绑定
     * 检查账号状态、权限等前置条件
     * 
     * @param accountId 账号ID
     * @return 验证结果
     */
    BindingValidationResult validateAccountBinding(String accountId);
    
    /**
     * 绑定验证结果
     */
    record BindingValidationResult(
        boolean canBind,
        String reason,
        String accountStatus
    ) {}
} 