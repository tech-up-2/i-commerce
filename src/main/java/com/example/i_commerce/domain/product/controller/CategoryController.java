package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.CategoryService;
import com.example.i_commerce.domain.product.controller.request.CreateCategoryRequest;
import com.example.i_commerce.domain.product.controller.response.CategoryResponse;
import com.example.i_commerce.domain.product.controller.response.CreateCategoryResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    @PostMapping
    public ApiResponse<CreateCategoryResponse> createCategory(
        @Valid @RequestBody CreateCategoryRequest request
    ) {
        return ApiResponse.success(categoryService.createCategory(request));
    }

    @Operation(summary = "전체 카테고리 조회", description = "존재하는 모든 카테고리를 조회합니다.")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories(
        @RequestParam(required = false) Integer maxDepth
    ) {
        return ApiResponse.success(categoryService.getAllCategories(maxDepth));
    }

    @Operation(summary = "특정 카테고리 조회", description = "특정 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategories(
        @PathVariable Long categoryId
    ) {
        return ApiResponse.success(categoryService.getCategoryById(categoryId));
    }

}
