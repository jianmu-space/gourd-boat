package space.jianmu.gourdboat.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.jianmu.gourdboat.application.auth.AuthService;
import space.jianmu.gourdboat.application.auth.command.LoginCommand;
import space.jianmu.gourdboat.application.auth.dto.LoginResult;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody LoginCommand command) {
        // 使用 Spring Security 的认证机制
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(command.identifier(), command.password())
        );
        
        // 设置认证信息到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成 token
        LoginResult result = authService.login(command);
        return ResponseEntity.ok(result);
    }
} 