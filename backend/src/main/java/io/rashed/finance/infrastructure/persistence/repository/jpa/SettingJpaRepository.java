package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.infrastructure.persistence.entity.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettingJpaRepository extends JpaRepository<SettingEntity, UUID> {

    Optional<SettingEntity> findBySettingKey(String settingKey);

    List<SettingEntity> findAllByOrderBySettingKeyAsc();
}
