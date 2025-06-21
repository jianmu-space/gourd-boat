package space.jianmu.gourdboat.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * 加密服务
 * 用于加密敏感信息如client_secret等
 */
@Service
public class EncryptionService {
    
    @Value("${app.encryption.key:default-encryption-key-32-chars-long}")
    private String encryptionKey;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 加密字符串
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            SecretKeySpec secretKey = generateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    /**
     * 解密字符串
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        
        try {
            SecretKeySpec secretKey = generateKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
    
    /**
     * 生成密钥
     */
    private SecretKeySpec generateKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        byte[] key = Arrays.copyOf(hash, 16); // AES需要16字节密钥
        return new SecretKeySpec(key, ALGORITHM);
    }
    
    /**
     * 检查字符串是否已加密
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            // 尝试Base64解码，如果成功且长度合理，可能是加密的
            byte[] decoded = Base64.getDecoder().decode(text);
            return decoded.length > 0 && decoded.length % 16 == 0; // AES加密后的长度是16的倍数
        } catch (Exception e) {
            return false;
        }
    }
} 