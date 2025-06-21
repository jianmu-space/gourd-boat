package space.jianmu.gourdboat.application.auth.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import space.jianmu.gourdboat.domain.account.AuthProvider;

public record LoginCommand(
    @NotBlank(message = "账号标识不能为空")
    @Size(min = 3, max = 50, message = "账号标识长度必须在3-50之间")
    String identifier,
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6位")
    String password,
    
    // 认证提供商，默认为PASSWORD（用户名密码认证）
    AuthProvider provider
) {
    // 构造方法，提供默认的provider
    public LoginCommand(String identifier, String password) {
        this(identifier, password, AuthProvider.of(AuthProvider.PASSWORD));
    }
} 