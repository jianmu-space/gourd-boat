package space.jianmu.gourdboat.interfaces.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import space.jianmu.gourdboat.domain.account.OidcAuthenticationException;
import space.jianmu.gourdboat.domain.account.OidcErrorCode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理OIDC认证异常
     */
    @ExceptionHandler(OidcAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOidcAuthenticationException(OidcAuthenticationException e) {
        log.warn("OIDC认证异常: provider={}, errorCode={}, message={}", 
                e.getProvider(), e.getErrorCodeString(), e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", getHttpStatus(e.getErrorCode()).value());
        response.put("error", "OIDC Authentication Error");
        response.put("errorCode", e.getErrorCodeString());
        response.put("message", e.getErrorMessage());
        response.put("provider", e.getProvider());
        
        if (e.getDetails() != null) {
            response.put("details", e.getDetails());
        }
        
        return ResponseEntity.status(getHttpStatus(e.getErrorCode())).body(response);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("未处理的异常", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "系统内部错误");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 根据错误码获取HTTP状态码
     */
    private HttpStatus getHttpStatus(OidcErrorCode errorCode) {
        switch (errorCode) {
            case INVALID_TOKEN:
            case TOKEN_EXPIRED:
            case TOKEN_SIGNATURE_INVALID:
            case INVALID_CLIENT:
            case INVALID_CLIENT_SECRET:
                return HttpStatus.UNAUTHORIZED;
                
            case PROVIDER_NOT_FOUND:
            case PROVIDER_DISABLED:
            case PROVIDER_CONFIG_INVALID:
                return HttpStatus.BAD_REQUEST;
                
            case NETWORK_ERROR:
            case TIMEOUT_ERROR:
            case SERVICE_UNAVAILABLE:
                return HttpStatus.SERVICE_UNAVAILABLE;
                
            case USER_INFO_NOT_FOUND:
            case USER_INFO_INVALID:
                return HttpStatus.NOT_FOUND;
                
            case INTERNAL_ERROR:
            case CONFIGURATION_ERROR:
            case STRATEGY_NOT_FOUND:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
} 