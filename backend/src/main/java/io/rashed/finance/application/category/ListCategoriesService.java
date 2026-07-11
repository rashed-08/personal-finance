package io.rashed.finance.application.category;

import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCategoriesService {

    private final CategoryRepository repository;

    public ListCategoriesService(
            CategoryRepository repository
    ) {
        this.repository = repository;
    }

    public List<Category> execute() {

        return repository.findAll();
    }

    public List<Category> execute(
            CategoryType categoryType
    ) {

        return repository.findByCategoryType(categoryType);
    }
}