package space.jianmu.gourdboat.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import space.jianmu.gourdboat.domain.account.ProviderRegistry;
import space.jianmu.gourdboat.infrastructure.auth.DynamicOidcStrategyFactory;

import java.util.Map;
import java.util.Set;

/**
 * 服务商管理控制器
 * 提供动态注册和管理OIDC服务商的API
 */
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderManagementController {
    
    private final DynamicOidcStrategyFactory strategyFactory;
    
    /**
     * 获取所有已注册的服务商
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProviders() {
        Set<String> providers = ProviderRegistry.getAllProviders();
        Set<String> groups = ProviderRegistry.getAllGroups();
        
        return ResponseEntity.ok(Map.of(
            "providers", providers,
            "groups", groups,
            "strategyCount", strategyFactory.getStrategyCount()
        ));
    }
    
    /**
     * 获取指定分组的所有服务商
     */
    @GetMapping("/group/{group}")
    public ResponseEntity<Set<String>> getProvidersByGroup(@PathVariable String group) {
        Set<String> providers = ProviderRegistry.getProvidersByGroup(group);
        return ResponseEntity.ok(providers);
    }
    
    /**
     * 获取服务商详细信息
     */
    @GetMapping("/{providerCode}")
    public ResponseEntity<ProviderRegistry.ProviderInfo> getProviderInfo(@PathVariable String providerCode) {
        ProviderRegistry.ProviderInfo info = ProviderRegistry.getProvider(providerCode);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }
    
    /**
     * 注册新的服务商
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerProvider(@RequestBody RegisterProviderRequest request) {
        try {
            ProviderRegistry.registerProvider(
                request.getProviderCode(),
                request.getProviderName(),
                request.getDescription(),
                request.getGroup()
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "服务商注册成功",
                "providerCode", request.getProviderCode()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "服务商注册失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 检查服务商是否存在
     */
    @GetMapping("/{providerCode}/exists")
    public ResponseEntity<Map<String, Boolean>> checkProviderExists(@PathVariable String providerCode) {
        boolean exists = ProviderRegistry.hasProvider(providerCode);
        boolean hasStrategy = strategyFactory.hasStrategy(providerCode);
        
        return ResponseEntity.ok(Map.of(
            "exists", exists,
            "hasStrategy", hasStrategy
        ));
    }
    
    /**
     * 获取所有已注册的策略
     */
    @GetMapping("/strategies")
    public ResponseEntity<Map<String, Object>> getAllStrategies() {
        Map<String, Object> strategies = strategyFactory.getAllStrategies().entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getClass().getSimpleName()
            ));
        
        return ResponseEntity.ok(Map.of(
            "strategies", strategies,
            "count", strategyFactory.getStrategyCount()
        ));
    }
    
    /**
     * 注册服务商请求
     */
    public static class RegisterProviderRequest {
        private String providerCode;
        private String providerName;
        private String description;
        private String group;
        
        // getters and setters
        public String getProviderCode() { return providerCode; }
        public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
        public String getProviderName() { return providerName; }
        public void setProviderName(String providerName) { this.providerName = providerName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
    }
} 