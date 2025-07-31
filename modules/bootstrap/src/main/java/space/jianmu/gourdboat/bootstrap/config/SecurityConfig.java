package space.jianmu.gourdboat.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import space.jianmu.gourdboat.infrastructure.security.JwtAuthenticationFilter;
import space.jianmu.gourdboat.infrastructure.security.JwtTokenProvider;
import space.jianmu.gourdboat.infrastructure.security.ProviderAwareAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            ProviderAwareAuthenticationProvider authenticationProvider) throws Exception {
        
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 登录相关的API - 允许匿名访问
                .requestMatchers("/api/login/**").permitAll()
                // 账号绑定相关的API - 需要认证（JWT验证）
                .requestMatchers("/api/account-binding/**").authenticated()
                // 公共API
                .requestMatchers("/api/public/**").permitAll()
                // 开发环境数据库控制台
                .requestMatchers("/h2-console/**").permitAll()
                // 健康检查
                .requestMatchers("/actuator/health").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(config -> config.disable()))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(ProviderAwareAuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

} 