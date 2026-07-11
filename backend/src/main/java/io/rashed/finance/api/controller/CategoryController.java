package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.category.*;
import io.rashed.finance.application.category.*;
import io.rashed.finance.common.enums.CategoryType;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CreateCategoryService createCategoryService;
    private final GetCategoryService getCategoryService;
    private final ListCategoriesService listCategoriesService;
    private final UpdateCategoryService updateCategoryService;
    private final DeactivateCategoryService deactivateCategoryService;
    private final DeleteCategoryService deleteCategoryService;

    public CategoryController(
            CreateCategoryService createCategoryService,
            GetCategoryService getCategoryService,
            ListCategoriesService listCategoriesService,
            UpdateCategoryService updateCategoryService,
            DeactivateCategoryService deactivateCategoryService,
            DeleteCategoryService deleteCategoryService
    ) {

        this.createCategoryService = createCategoryService;
        this.getCategoryService = getCategoryService;
        this.listCategoriesService = listCategoriesService;
        this.updateCategoryService = updateCategoryService;
        this.deactivateCategoryService = deactivateCategoryService;
        this.deleteCategoryService = deleteCategoryService;
    }

    @PostMapping
    public CategoryResponse createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        Category category =
                createCategoryService.create(

                        CategoryDtoMapper.toCommand(

                                request.name(),
                                request.categoryType(),
                                request.systemDefined(),
                                request.description()

                        )

                );

        return CategoryDtoMapper.toResponse(category);
    }

    @GetMapping
    public List<CategoryResponse> listCategories(
            @RequestParam(required = false)
            CategoryType categoryType
    ) {

        List<Category> categories =
                categoryType == null
                        ? listCategoriesService.execute()
                        : listCategoriesService.execute(categoryType);

        return categories.stream()
                .map(CategoryDtoMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(
            @PathVariable UUID id
    ) {

        return CategoryDtoMapper.toResponse(

                getCategoryService.execute(

                        CategoryId.of(id)

                )

        );
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(

            @PathVariable UUID id,

            @Valid
            @RequestBody UpdateCategoryRequest request

    ) {

        Category category =
                updateCategoryService.execute(

                        CategoryId.of(id),

                        request.name(),

                        request.categoryType(),

                        request.description()

                );

        return CategoryDtoMapper.toResponse(category);
    }

    @PatchMapping("/{id}/deactivate")
    public CategoryResponse deactivateCategory(
            @PathVariable UUID id
    ) {

        Category category =
                deactivateCategoryService.execute(
                        CategoryId.of(id)
                );

        return CategoryDtoMapper.toResponse(category);
    }

    @PatchMapping("/{id}/activate")
    public CategoryResponse activateCategory(
            @PathVariable UUID id
    ) {

        Category category =
                deactivateCategoryService.executeActivate(
                        CategoryId.of(id)
                );

        return CategoryDtoMapper.toResponse(category);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(
            @PathVariable UUID id
    ) {

        deleteCategoryService.execute(
                CategoryId.of(id)
        );
    }
}