package space.jianmu.gourdboat.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGeneratorTest {
    
    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        
        System.out.println("============================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("============================");
        
        // 验证密码
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + matches);
        
        // 测试现有哈希
        String existingHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        boolean matchesExisting = encoder.matches(password, existingHash);
        System.out.println("现有哈希验证: " + matchesExisting);
    }
} 