package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.Category;
import com.topwatch.back_topwatch.domain.enums.Type;
import com.topwatch.back_topwatch.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category catalog endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Get categories",
            description = "Returns every category, or only the ones matching the given type (BOOKS, SERIES, MOVIES, MUSIC) when the type query param is provided",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping
    public List<Category> getCategories(@RequestParam(required = false) Type type) {
        return type != null
                ? categoryService.findCategoriesByType(type)
                : categoryService.findAllCategories();
    }

}
