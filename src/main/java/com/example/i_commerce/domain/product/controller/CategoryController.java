package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.CategoryService;
import com.example.i_commerce.domain.product.controller.response.CategoryResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories(
        @RequestParam(required = false) Integer maxDepth
    ) {
        return ApiResponse.success(categoryService.getAllCategories(maxDepth));
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategories(
        @PathVariable Long categoryId
    ) {
        return ApiResponse.success(categoryService.getCategoryById(categoryId));
    }

}
