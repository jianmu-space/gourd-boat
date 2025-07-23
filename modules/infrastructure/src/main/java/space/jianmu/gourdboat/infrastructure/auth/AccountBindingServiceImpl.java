package space.jianmu.gourdboat.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.jianmu.gourdboat.application.auth.AccountBindingService;
import space.jianmu.gourdboat.application.verification.VerificationCodeService;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.account.AccountId;
import space.jianmu.gourdboat.domain.account.AccountRepository;
import space.jianmu.gourdboat.domain.account.AccountStatus;
import space.jianmu.gourdboat.domain.user.PhoneNumber;
import space.jianmu.gourdboat.domain.user.User;
import space.jianmu.gourdboat.domain.user.Nickname;

import java.util.Optional;

/**
 * 账号绑定服务实现
 * 统一处理各种账号绑定业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBindingServiceImpl implements AccountBindingService {
    
    private final AccountRepository accountRepository;
    private final VerificationCodeService verificationCodeService;
    // TODO: 添加 UserRepository userRepository;
    
    @Override
    public Account bindAccountByPhoneVerification(String accountId, PhoneNumber phoneNumber, 
                                                String verificationCode, String userNickname) {
        log.info("开始通过手机号验证绑定账号: accountId={}, phoneNumber={}", accountId, phoneNumber);
        
        try {
            // 1. 验证账号状态
            BindingValidationResult validation = validateAccountBinding(accountId);
            if (!validation.canBind()) {
                throw new IllegalStateException("账号无法绑定: " + validation.reason());
            }
            
            // 2. 验证手机号验证码
            VerificationCodeService.VerifyCodeResult codeResult = verificationCodeService.verifyCode(
                phoneNumber.getFullNumber(), 
                verificationCode, 
                "BIND_ACCOUNT"
            );
            
            if (!codeResult.success()) {
                throw new RuntimeException("验证码验证失败: " + codeResult.message());
            }
            
            // 3. 获取待绑定账号
            Account account = findAccountById(accountId);
            
            // 4. 查找或创建用户
            User user = findOrCreateUserByPhoneNumber(phoneNumber, userNickname, account.getTempAvatar());
            
            // 5. 执行绑定
            Account boundAccount = account.bindUser(user.getId());
            Account savedAccount = accountRepository.save(boundAccount);
            
            log.info("账号绑定成功: accountId={}, phoneNumber={}, userId={}", 
                    accountId, phoneNumber, user.getId());
            
            return savedAccount;
            
        } catch (Exception e) {
            log.error("账号绑定失败: accountId={}, phoneNumber={}", accountId, phoneNumber, e);
            throw new RuntimeException("账号绑定失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Account bindAccountByPasswordVerification(String pendingAccountId, 
                                                   String existingUserIdentifier, String password) {
        log.info("开始通过密码验证绑定账号: pendingAccountId={}, existingUserIdentifier={}", 
                pendingAccountId, existingUserIdentifier);
        
        // TODO: 实现密码验证绑定逻辑
        // 1. 验证账号状态
        // 2. 验证现有用户的密码
        // 3. 将待绑定账号关联到现有用户
        
        throw new UnsupportedOperationException("密码验证绑定功能待实现");
    }
    
    @Override
    public BindingValidationResult validateAccountBinding(String accountId) {
        log.debug("验证账号绑定条件: accountId={}", accountId);
        
        try {
            Optional<Account> accountOpt = accountRepository.findById(AccountId.of(accountId));
            
            if (accountOpt.isEmpty()) {
                return new BindingValidationResult(false, "账号不存在", null);
            }
            
            Account account = accountOpt.get();
            AccountStatus status = account.getStatus();
            
            if (!account.isPendingBind()) {
                return new BindingValidationResult(false, 
                    "账号状态不正确，当前状态: " + status, status.name());
            }
            
            return new BindingValidationResult(true, "可以绑定", status.name());
            
        } catch (Exception e) {
            log.error("验证账号绑定条件失败: accountId={}", accountId, e);
            return new BindingValidationResult(false, "验证失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 根据账号ID查找账号
     */
    private Account findAccountById(String accountId) {
        return accountRepository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new RuntimeException("账号不存在: " + accountId));
    }
    
    /**
     * 根据手机号查找或创建用户
     * TODO: 需要实现UserRepository和UserService
     */
    private User findOrCreateUserByPhoneNumber(PhoneNumber phoneNumber, String nickname, String avatar) {
        // 临时实现：需要实现用户管理逻辑
        // 1. 根据手机号查找现有用户
        // 2. 如果不存在，创建新用户
        
        // 目前先创建新用户，使用传入的用户信息
        Nickname userNickname = Nickname.of(nickname);
        
        if (avatar != null && !avatar.trim().isEmpty()) {
            log.info("创建用户，包含头像信息: phoneNumber={}, nickname={}, avatar={}", 
                    phoneNumber, nickname, avatar);
            return User.create(phoneNumber, userNickname, avatar);
        } else {
            log.info("创建用户，无头像信息: phoneNumber={}, nickname={}", 
                    phoneNumber, nickname);
            return User.create(phoneNumber, userNickname);
        }
    }
} 