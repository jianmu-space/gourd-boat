package space.jianmu.gourdboat.infrastructure.persistence.oidc;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OidcProviderConfigJpaRepository extends JpaRepository<OidcProviderConfigEntity, String> {
    
    @Query("SELECT o FROM OidcProviderConfigEntity o WHERE o.providerCode = :providerCode")
    List<OidcProviderConfigEntity> findByProviderCode(@Param("providerCode") String providerCode);
    
    @Query("SELECT o FROM OidcProviderConfigEntity o WHERE o.enabled = true")
    List<OidcProviderConfigEntity> findAllEnabled();
    
    @Query("SELECT o FROM OidcProviderConfigEntity o WHERE o.providerCode = :providerCode AND o.enabled = true")
    List<OidcProviderConfigEntity> findEnabledByProviderCode(@Param("providerCode") String providerCode);
} 