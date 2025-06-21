package space.jianmu.gourdboat.infrastructure.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OidcProviderConfigJpaRepository extends JpaRepository<OidcProviderConfigEntity, String> {
    
    @Query("SELECT o FROM OidcProviderConfigEntity o WHERE o.providerCode = :providerCode")
    List<OidcProviderConfigEntity> findByProviderCode(@Param("providerCode") String providerCode);
    
    @Query("SELECT o FROM OidcProviderConfigEntity o WHERE o.enabled = true")
    List<OidcProviderConfigEntity> findAllEnabled();
    
    @Query("SELECT o FROM OidcProviderConfigEntity o WHERE o.providerCode = :providerCode AND o.enabled = true")
    List<OidcProviderConfigEntity> findEnabledByProviderCode(@Param("providerCode") String providerCode);
} 