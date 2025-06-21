package space.jianmu.gourdboat.domain.account;

/**
 * OIDC认证异常
 */
public class OidcAuthenticationException extends RuntimeException {
    
    private final OidcErrorCode errorCode;
    private final String provider;
    private final String details;
    
    public OidcAuthenticationException(OidcErrorCode errorCode, String provider) {
        super(String.format("[%s] %s: %s", provider, errorCode.getCode(), errorCode.getMessage()));
        this.errorCode = errorCode;
        this.provider = provider;
        this.details = null;
    }
    
    public OidcAuthenticationException(OidcErrorCode errorCode, String provider, String details) {
        super(String.format("[%s] %s: %s - %s", provider, errorCode.getCode(), errorCode.getMessage(), details));
        this.errorCode = errorCode;
        this.provider = provider;
        this.details = details;
    }
    
    public OidcAuthenticationException(OidcErrorCode errorCode, String provider, Throwable cause) {
        super(String.format("[%s] %s: %s", provider, errorCode.getCode(), errorCode.getMessage()), cause);
        this.errorCode = errorCode;
        this.provider = provider;
        this.details = null;
    }
    
    public OidcAuthenticationException(OidcErrorCode errorCode, String provider, String details, Throwable cause) {
        super(String.format("[%s] %s: %s - %s", provider, errorCode.getCode(), errorCode.getMessage(), details), cause);
        this.errorCode = errorCode;
        this.provider = provider;
        this.details = details;
    }
    
    public OidcErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public String getDetails() {
        return details;
    }
    
    public String getErrorCodeString() {
        return errorCode.getCode();
    }
    
    public String getErrorMessage() {
        return errorCode.getMessage();
    }
} 