package space.jianmu.gourdboat.infrastructure.persistence.account;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "boat_account")
public class AccountEntity {
    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false, unique = true)
    private String identifier;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private String password; // 账号密码（BCrypt等加密存储，仅内部账号使用）

    // getter/setter略，可用lombok @Data
}