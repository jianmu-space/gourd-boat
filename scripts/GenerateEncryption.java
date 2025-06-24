import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class GenerateEncryption {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("用法: java GenerateEncryption <明文> <密钥>");
            return;
        }
        String plaintext = args[0];
        String key = args[1];
        // 生成16字节AES密钥
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        byte[] aesKey = Arrays.copyOf(hash, 16);
        SecretKeySpec secretKey = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("加密后密文: " + encrypted);
    }
} 