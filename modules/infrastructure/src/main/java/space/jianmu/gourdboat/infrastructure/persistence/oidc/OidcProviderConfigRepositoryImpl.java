package space.jianmu.gourdboat.infrastructure.persistence.oidc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import space.jianmu.gourdboat.domain.account.AuthProvider;
import space.jianmu.gourdboat.domain.oidc.OidcProviderConfig;
import space.jianmu.gourdboat.domain.oidc.OidcProviderConfigRepository;

@Repository
public class OidcProviderConfigRepositoryImpl implements OidcProviderConfigRepository {
    private final OidcProviderConfigJpaRepository jpaRepository;
    private final OidcProviderConfigEntityMapper mapper;
    public OidcProviderConfigRepositoryImpl(OidcProviderConfigJpaRepository jpaRepository, OidcProviderConfigEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    @Override
    public OidcProviderConfig save(OidcProviderConfig config) {
        OidcProviderConfigEntity entity = mapper.toEntity(config);
        OidcProviderConfigEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    @Override
    public Optional<OidcProviderConfig> findByConfigId(String configId) {
        return jpaRepository.findById(configId).map(mapper::toDomain);
    }
    @Override
    public List<OidcProviderConfig> findByProvider(AuthProvider provider) {
        return jpaRepository.findByProviderCode(provider.getValue())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    @Override
    public List<OidcProviderConfig> findAllEnabled() {
        return jpaRepository.findAllEnabled()
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    @Override
    public List<OidcProviderConfig> findEnabledByProvider(AuthProvider provider) {
        return jpaRepository.findEnabledByProviderCode(provider.getValue())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
    @Override
    public void deleteByConfigId(String configId) {
        jpaRepository.deleteById(configId);
    }
    @Override
    public boolean existsByConfigId(String configId) {
        return jpaRepository.existsById(configId);
    }
} 