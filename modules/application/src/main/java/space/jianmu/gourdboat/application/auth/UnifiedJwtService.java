package space.jianmu.gourdboat.application.auth;

import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;

/**
 * 统一JWT服务接口
 * 为所有登录方式提供统一的JWT生成逻辑
 */
public interface UnifiedJwtService {
    
    /**
     * 根据账号信息生成统一格式的JWT
     * @param account 账号信息
     * @return 登录结果
     */
    LoginResult generateJwtFromAccount(Account account);
    
    /**
     * 根据账号信息生成统一格式的JWT
     * @param account 账号信息
     * @param customRole 自定义角色（如果为null则根据账号状态自动判断）
     * @return 登录结果
     */
    LoginResult generateJwtFromAccount(Account account, String customRole);
    
    /**
     * 验证绑定令牌
     * 验证JWT有效性并确保用户状态为PENDING_BIND
     * @param token JWT令牌
     * @return 账号ID（未来可能返回Account对象）
     * @throws RuntimeException 如果JWT无效或用户状态不正确
     */
    String validateBindToken(String token);
} 