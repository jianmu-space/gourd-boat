package space.jianmu.gourdboat.infrastructure.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA加密字符串转换器
 * 用于自动加密/解密数据库中的敏感字段
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    private static EncryptionService encryptionService;
    
    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptedStringConverter.encryptionService = encryptionService;
    }
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        
        // 如果已经是加密的，直接返回
        if (encryptionService.isEncrypted(attribute)) {
            return attribute;
        }
        
        return encryptionService.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        
        // 如果是加密的，解密后返回
        if (encryptionService.isEncrypted(dbData)) {
            return encryptionService.decrypt(dbData);
        }
        
        return dbData;
    }
} 