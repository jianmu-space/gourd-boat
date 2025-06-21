package space.jianmu.gourdboat.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import space.jianmu.gourdboat.bootstrap.config.TestSecurityConfig;
import space.jianmu.gourdboat.domain.account.Account;
import space.jianmu.gourdboat.domain.account.AccountRepository;
import space.jianmu.gourdboat.domain.account.AuthProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestSecurityConfig.class)
public class AccountRepositoryIntegrationTest {
    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testFindByProviderAndIdentifier() {
        Optional<Account> accountOpt = accountRepository.findByProviderAndIdentifier(
            AuthProvider.of(AuthProvider.PASSWORD), "testuser");
        assertTrue(accountOpt.isPresent(), "Account should be found");
        Account account = accountOpt.get();
        assertEquals("testuser", account.getIdentifier());
        assertEquals(AuthProvider.PASSWORD, account.getProvider().getValue());
        assertEquals("$2a$10$7QJ8QwQwQwQwQwQwQwQwQeQwQwQwQwQwQwQwQwQwQwQwQwQw", account.getPassword());
    }
} 