package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.infrastructure.persistence.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByName(String name);

    List<CategoryEntity> findByCategoryType(CategoryType categoryType);

    boolean existsByName(String name);
}