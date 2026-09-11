package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.infrastructure.persistence.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

    /**
     * Categories with this name, oldest first. A list, not an
     * {@code Optional}: categories.name has no unique index, so a duplicate
     * (a hand-added "Groceries" next to the seeded one, say) would make an
     * {@code Optional} return throw NonUniqueResultException rather than
     * answering. Callers take the first — the original row.
     */
    List<CategoryEntity> findByNameOrderByCreatedAtAsc(String name);

    List<CategoryEntity> findByCategoryType(CategoryType categoryType);

    boolean existsByName(String name);
}