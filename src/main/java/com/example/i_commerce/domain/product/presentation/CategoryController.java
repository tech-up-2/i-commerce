package com.example.i_commerce.domain.product.presentation;


import com.example.i_commerce.domain.product.application.service.CategoryService;
import com.example.i_commerce.domain.product.presentation.request.CreateCategoryRequest;
import com.example.i_commerce.domain.product.presentation.response.CategoryResponse;
import com.example.i_commerce.domain.product.presentation.response.CreateCategoryResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Category API", description = "카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성", description = "제공할 카테고리를 생성합니다.")
    @PreAuthorize("@authChecker.canManageCategory()")
    @PostMapping
    public ApiResponse<CreateCategoryResponse> createCategory(
        @Valid @RequestBody CreateCategoryRequest request
    ) {
        CreateCategoryResponse res = categoryService.createCategory(request);
        return ApiResponse.success(res);
    }

    @Operation(summary = "전체 카테고리 조회", description = "존재하는 모든 카테고리를 조회합니다.")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories(
        @RequestParam(required = false) Integer maxDepth
    ) {
        List<CategoryResponse> res = categoryService.getAllCategories(maxDepth);
        return ApiResponse.success(res);
    }

    @Operation(summary = "특정 카테고리 조회", description = "특정 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategories(@PathVariable Long categoryId) {
        CategoryResponse res = categoryService.getCategory(categoryId);
        return ApiResponse.success(res);
    }

    @Operation(summary = "특정 카테고리 삭제", description = "특정 카테고리를 삭제합니다.")
    @PreAuthorize("@authChecker.canManageCategory()")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.success();
    }


}
