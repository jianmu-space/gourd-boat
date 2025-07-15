package space.jianmu.gourdboat.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.jianmu.gourdboat.application.auth.UnifiedJwtService;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;

/**
 * 统一JWT服务实现
 * 为所有登录方式提供统一的JWT生成逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedJwtServiceImpl implements UnifiedJwtService {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public LoginResult generateJwtFromAccount(Account account) {
        return generateJwtFromAccount(account, null);
    }
    
    @Override
    public LoginResult generateJwtFromAccount(Account account, String customRole) {
        log.info("生成统一JWT: accountId={}, provider={}, userStatus={}", 
                account.getId().getValue(), account.getProvider().getValue(), 
                account.isPendingBind() ? "PENDING_BIND" : "ACTIVE");
        
        try {
            // 生成统一格式的JWT
            String token = jwtTokenProvider.generateUnifiedToken(
                account.getIdentifier(),                           // identifier
                account.getId().getValue(),                        // accountId
                account.getProvider().getValue(),                  // provider
                account.getType().name(),                          // accountType
                account.isPendingBind() ? "PENDING_BIND" : "ACTIVE", // userStatus
                account.isBound() ? account.getUserId().getValue() : null // userId
            );
            
            // 确定用户角色
            String role = customRole != null ? customRole : 
                         account.isPendingBind() ? "PENDING_BIND" : "USER";
            
            LoginResult result = new LoginResult(
                token,
                account.getIdentifier(),
                role
            );
            
            log.info("统一JWT生成成功: identifier={}, role={}", 
                    account.getIdentifier(), role);
            return result;
            
        } catch (Exception e) {
            log.error("生成统一JWT失败: account={}", account, e);
            throw new RuntimeException("JWT生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public String validateBindToken(String token) {
        log.info("验证绑定令牌");
        try {
            // 1. 验证JWT有效性
            if (!jwtTokenProvider.validateToken(token)) {
                throw new RuntimeException("无效的JWT令牌");
            }
            
            // 2. 获取claims
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            
            // 3. 验证用户状态
            String userStatus = claims.get("userStatus", String.class);
            if (!"PENDING_BIND".equals(userStatus)) {
                throw new RuntimeException("用户状态不正确，当前状态: " + userStatus);
            }
            
            // 4. 获取账号ID
            String accountId = claims.get("accountId", String.class);
            if (accountId == null || accountId.isEmpty()) {
                throw new RuntimeException("JWT中缺少账号信息");
            }
            
            log.info("绑定JWT验证成功: accountId={}", accountId);
            return accountId;
            
        } catch (Exception e) {
            log.error("验证绑定JWT失败", e);
            throw new RuntimeException("JWT验证失败: " + e.getMessage());
        }
    }
} 