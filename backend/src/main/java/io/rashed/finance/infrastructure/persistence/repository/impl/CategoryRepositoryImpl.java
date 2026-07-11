package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.infrastructure.persistence.entity.CategoryEntity;
import io.rashed.finance.infrastructure.persistence.mapper.CategoryEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.CategoryJpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryImpl
        implements CategoryRepository {

    private final CategoryJpaRepository repository;

    public CategoryRepositoryImpl(
            CategoryJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {

        CategoryEntity entity =
                CategoryEntityMapper.toEntity(category);

        CategoryEntity saved =
                repository.save(entity);

        return CategoryEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {

        return repository.findById(id.getValue())
                .map(CategoryEntityMapper::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {

        return repository.findByName(name)
                .map(CategoryEntityMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {

        return repository.findAll()
                .stream()
                .map(CategoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByCategoryType(
            CategoryType categoryType
    ) {

        return repository.findByCategoryType(categoryType)
                .stream()
                .map(CategoryEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {

        return repository.existsByName(name);
    }

    @Override
    public void delete(CategoryId id) {

        repository.deleteById(id.getValue());
    }
}