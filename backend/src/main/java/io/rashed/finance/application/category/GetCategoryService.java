package io.rashed.finance.application.category;

import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class GetCategoryService {

    private final CategoryRepository repository;

    public GetCategoryService(
            CategoryRepository repository
    ) {
        this.repository = repository;
    }

    public Category execute(CategoryId id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + id.getValue()
                        )
                );
    }
}