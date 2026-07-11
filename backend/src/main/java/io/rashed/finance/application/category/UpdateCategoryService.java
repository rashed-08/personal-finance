package io.rashed.finance.application.category;

import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateCategoryService {

    private final CategoryRepository repository;

    public UpdateCategoryService(
            CategoryRepository repository
    ) {
        this.repository = repository;
    }

    public Category execute(
            CategoryId id,
            String name,
            CategoryType categoryType,
            String description
    ) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + id.getValue()
                        )
                );

        Category updated = category.update(
                name,
                categoryType,
                description
        );

        return repository.save(updated);
    }
}