package io.rashed.finance.application.category;

import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteCategoryService {

    private final CategoryRepository repository;

    public DeleteCategoryService(
            CategoryRepository repository
    ) {
        this.repository = repository;
    }

    public void execute(CategoryId id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + id.getValue()
                        )
                );

        if (category.isSystemDefined()) {
            throw new IllegalStateException(
                    "System-defined categories cannot be deleted."
            );
        }

        repository.delete(id);
    }
}