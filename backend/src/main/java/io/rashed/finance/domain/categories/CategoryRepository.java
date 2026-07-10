package io.rashed.finance.domain.categories;

import io.rashed.finance.common.enums.CategoryType;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(CategoryId id);

    Optional<Category> findByName(String name);

    List<Category> findAll();

    List<Category> findByCategoryType(CategoryType categoryType);

    boolean existsByName(String name);

    void delete(CategoryId id);
}