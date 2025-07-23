package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.user.PhoneNumber;

import java.util.Optional;

/**
 * OIDC账号服务接口
 * 负责处理OIDC用户的查找、创建和绑定逻辑
 */
public interface OidcAccountService {
    
    /**
     * 根据OIDC认证结果查找账号
     * @param provider OIDC提供商代码
     * @param configId 配置ID
     * @param identifier 用户标识符（通常是openId）
     * @return 可能存在的账号
     */
    Optional<Account> findAccount(String provider, String configId, String identifier);
    
    /**
     * 创建新的OIDC账号
     * @param oidcResult OIDC认证结果（必须包含userInfo）
     * @param configId 配置ID
     * @return 新创建的账号（PENDING_BIND状态）
     * @throws IllegalArgumentException 如果oidcResult中不包含userInfo
     */
    Account createAccount(OidcAuthResult oidcResult, String configId);
    
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