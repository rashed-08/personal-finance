package io.rashed.finance.application.category;

import java.util.Objects;

import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryRepository;

public class CreateCategoryService {

    private final CategoryRepository repository;

    public CreateCategoryService(CategoryRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Category create(CreateCategoryCommand command) {

        Objects.requireNonNull(command);

        if (repository.existsByName(command.name())) {
            throw new IllegalArgumentException("Category already exists: " + command.name());
        }

        Category category = Category.create(command.name(), command.categoryType(), command.systemDefined(), command.description());

        return repository.save(category);
    }
}