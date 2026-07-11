package io.rashed.finance.application.category;

import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class DeactivateCategoryService {

    private final CategoryRepository repository;

    public DeactivateCategoryService(
            CategoryRepository repository
    ) {
        this.repository = repository;
    }

    public Category execute(CategoryId id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + id.getValue()
                        )
                );

        Category deactivated = category.deactivate();

        return repository.save(deactivated);
    }

    public Category executeActivate(CategoryId id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + id.getValue()
                        )
                );

        Category activated = category.activate();

        return repository.save(activated);
    }
}