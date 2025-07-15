package space.jianmu.gourdboat.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.jianmu.gourdboat.application.auth.OidcAccountService;
import space.jianmu.gourdboat.application.oidc.dto.OidcAuthResult;
import space.jianmu.gourdboat.domain.account.*;
import space.jianmu.gourdboat.domain.user.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OIDC账号服务实现
 * 负责处理OIDC用户的查找、创建和绑定逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OidcAccountServiceImpl implements OidcAccountService {
    
    private final AccountRepository accountRepository;
    
    @Override
    public Account findOrCreateAccount(OidcAuthResult oidcResult) {
        log.info("查找或创建OIDC账号: provider={}, openId={}", oidcResult.getProvider(), oidcResult.getOpenId());
        
        try {
            // 1. 构建认证提供商
            AuthProvider provider = AuthProvider.of(oidcResult.getProvider().toUpperCase());
            
            // 2. 使用openId作为identifier查找现有账号
            String identifier = oidcResult.getOpenId();
            Optional<Account> existingAccount = accountRepository.findByProviderAndIdentifier(provider, identifier);
            
            if (existingAccount.isPresent()) {
                log.info("找到现有OIDC账号: provider={}, identifier={}", provider.getValue(), identifier);
                return existingAccount.get();
            }
            
            // 3. 如果账号不存在，创建新账号（待绑定状态）
            log.info("创建新OIDC账号: provider={}, identifier={}", provider.getValue(), identifier);
            Account newAccount = createNewOidcAccount(oidcResult, provider, identifier, null);
            
            // 4. 保存新账号到数据库
            Account savedAccount = accountRepository.save(newAccount);
            log.info("OIDC账号创建成功: provider={}, identifier={}, accountId={}", 
                    provider.getValue(), identifier, savedAccount.getId().getValue());
            
            return savedAccount;
            
        } catch (Exception e) {
            log.error("查找或创建OIDC账号失败: provider={}, openId={}", oidcResult.getProvider(), oidcResult.getOpenId(), e);
            throw new RuntimeException("OIDC账号处理失败", e);
        }
    }
    
    @Override
    public Account bindOidcToExistingUser(OidcAuthResult oidcResult, String existingIdentifier) {
        log.info("绑定OIDC账号到现有用户: provider={}, openId={}, existingIdentifier={}", 
                oidcResult.getProvider(), oidcResult.getOpenId(), existingIdentifier);
        
        try {
            // 1. 查找现有账号（通常是密码认证的账号）
            Optional<Account> existingAccount = accountRepository.findByProviderAndIdentifier(
                AuthProvider.of(AuthProvider.PASSWORD), existingIdentifier);
            
            if (existingAccount.isEmpty()) {
                throw new RuntimeException("现有账号不存在: " + existingIdentifier);
            }
            
            // 2. 创建OIDC账号并关联到同一个用户
            AuthProvider oidcProvider = AuthProvider.of(oidcResult.getProvider().toUpperCase());
            Account oidcAccount = createNewOidcAccount(oidcResult, oidcProvider, oidcResult.getOpenId(), existingAccount.get().getUserId());
            
            // 3. 保存OIDC账号到数据库
            Account savedOidcAccount = accountRepository.save(oidcAccount);
            
            log.info("OIDC账号绑定成功: provider={}, identifier={}, userId={}, accountId={}", 
                    oidcProvider.getValue(), oidcResult.getOpenId(), existingAccount.get().getUserId(), savedOidcAccount.getId().getValue());
            
            return savedOidcAccount;
            
        } catch (Exception e) {
            log.error("绑定OIDC账号失败: provider={}, openId={}, existingIdentifier={}", 
                    oidcResult.getProvider(), oidcResult.getOpenId(), existingIdentifier, e);
            throw new RuntimeException("OIDC账号绑定失败", e);
        }
    }
    
    /**
     * 创建新的OIDC账号
     * 根据userId是否为null自动决定账号状态
     */
    private Account createNewOidcAccount(OidcAuthResult oidcResult, AuthProvider provider, String identifier, UserId userId) {
        // 根据userId是否为null决定账号状态
        AccountStatus status = (userId == null) ? AccountStatus.PENDING_BIND : AccountStatus.ACTIVE;
        
        return Account.reconstruct()
                .id(AccountId.generate())
                .userId(userId) // 可能为null（待绑定）或有值（已绑定）
                .type(AccountType.EXTERNAL)
                .provider(provider)
                .identifier(identifier)
                .password(null) // OIDC账号不需要密码
                .status(status) // 根据userId动态决定状态
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    @Override
    public Account bindToPhoneNumber(String accountId, PhoneNumber phoneNumber, String nickname) {
        log.info("绑定OIDC账号到手机号: accountId={}, phoneNumber={}", accountId, phoneNumber);
        
        try {
            // 1. 查找待绑定的账号
            Optional<Account> accountOpt = findAccountById(accountId);
            if (accountOpt.isEmpty()) {
                throw new RuntimeException("账号不存在: " + accountId);
            }
            
            Account account = accountOpt.get();
            if (!account.isPendingBind()) {
                throw new RuntimeException("账号不是待绑定状态: " + accountId);
            }
            
            // 2. 根据手机号查找或创建用户
            User user = findOrCreateUserByPhoneNumber(phoneNumber, nickname);
            
            // 3. 绑定账号到用户
            Account boundAccount = account.bindUser(user.getId());
            
            // 4. 保存更新后的账号
            Account savedAccount = accountRepository.save(boundAccount);
            
            log.info("OIDC账号绑定成功: accountId={}, phoneNumber={}, userId={}", 
                    accountId, phoneNumber, user.getId());
            
            return savedAccount;
            
        } catch (Exception e) {
            log.error("绑定OIDC账号到手机号失败: accountId={}, phoneNumber={}", accountId, phoneNumber, e);
            throw new RuntimeException("绑定失败", e);
        }
    }
    
    /**
     * 根据账号ID查找账号
     */
    private Optional<Account> findAccountById(String accountId) {
        return accountRepository.findById(AccountId.of(accountId));
    }
    
    /**
     * 根据手机号查找或创建用户
     * TODO: 需要实现UserRepository和UserService
     */
    private User findOrCreateUserByPhoneNumber(PhoneNumber phoneNumber, String nickname) {
        // 临时实现：需要实现用户管理逻辑
        // 1. 根据手机号查找现有用户
        // 2. 如果不存在，创建新用户
        
        // 目前先创建新用户
        Nickname userNickname = Nickname.of(nickname);
        return User.create(phoneNumber, userNickname);
    }
} 