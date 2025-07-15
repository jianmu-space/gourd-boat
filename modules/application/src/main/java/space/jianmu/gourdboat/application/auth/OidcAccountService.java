package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.user.PhoneNumber;

/**
 * OIDC账号服务接口
 * 负责处理OIDC用户的查找、创建和绑定逻辑
 */
public interface OidcAccountService {
    
    /**
     * 根据OIDC认证结果查找或创建账号
     * 如果是新用户，返回PENDING_BIND状态的Account
     * @param oidcResult OIDC认证结果
     * @return 系统账号（可能是待绑定状态）
     */
    Account findOrCreateAccount(OidcAuthResult oidcResult);
    
    /**
     * 绑定OIDC账号到现有用户
     * @param oidcResult OIDC认证结果
     * @param existingIdentifier 现有用户标识符
     * @return 绑定后的账号
     */
    Account bindOidcToExistingUser(OidcAuthResult oidcResult, String existingIdentifier);
    
    /**
     * 将待绑定状态的OIDC账号绑定到手机号
     * @param accountId 账号ID
     * @param phoneNumber 手机号
     * @param nickname 用户昵称
     * @return 绑定后的账号
     */
    Account bindToPhoneNumber(String accountId, PhoneNumber phoneNumber, String nickname);
} 