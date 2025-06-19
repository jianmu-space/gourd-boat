# 数据库索引优化说明

## 概述

为了提升账号认证查询的性能，我们对数据库索引和相应的代码进行了彻底优化，移除了低效的查询方法。

## 数据库改进

### 新增索引

在 `boat_account` 表中新增了组合索引：

```sql
CREATE INDEX IF NOT EXISTS idx_boat_account_provider_identifier ON boat_account (provider, identifier);
```

### 索引优势

1. **登录查询优化**：根据认证提供商和标识符的组合查询账号时，性能显著提升
2. **查询路径明确**：避免了在不同认证提供商之间的数据混淆
3. **索引覆盖**：所有认证相关查询都充分利用这个组合索引

## 代码改进

### 1. Repository 层简化

- **AccountRepository**: 只保留高效的 `findByProviderAndIdentifier()` 方法
- **AccountJpaRepository**: 对应的JPA查询方法
- **AccountRepositoryImpl**: 实现高效查询逻辑

### 2. 认证服务优化

- **DbUserDetailsService**: 统一使用组合索引查询，提升认证性能
- **ProviderAwareAuthenticationProvider**: 支持多认证提供商的认证提供者
- **JwtAuthenticationFilter**: JWT验证也使用高效查询方式
- **SecurityConfig**: 配置使用新的认证提供者

### 3. 应用服务更新

- **LoginCommand**: 支持指定认证提供商
- **Application 模块**: 添加对 Domain 模块的依赖

## 性能提升

1. **查询效率**：所有查询都使用组合索引，从全表扫描优化为索引查找
2. **并发处理**：减少数据库锁竞争，提升并发登录性能
3. **扩展性**：为未来支持OAuth2等第三方认证奠定基础

## 架构简化

移除了冗余的查询方法，简化了代码架构：

- 移除 `findByIdentifier()` 方法，避免性能陷阱
- 统一使用 `findByProviderAndIdentifier()` 进行精确查询
- 所有查询都充分利用数据库索引

## 使用示例

### 精确查询（唯一方式）

```java
// 使用组合索引的高效查询
Optional<Account> account = accountRepository.findByProviderAndIdentifier(
    AuthProvider.PASSWORD, "user@example.com");

// 其他认证方式
Optional<Account> googleAccount = accountRepository.findByProviderAndIdentifier(
    AuthProvider.GOOGLE, "google-user-id");
``` 