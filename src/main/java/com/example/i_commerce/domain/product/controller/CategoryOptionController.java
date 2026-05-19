package com.example.i_commerce.domain.product.controller;

import com.example.i_commerce.domain.product.application.service.CategoryOptionService;
import com.example.i_commerce.domain.product.controller.request.AddCategoryOptionRequest;
import com.example.i_commerce.domain.product.controller.response.AddCategoryOptionResponse;
import com.example.i_commerce.domain.product.controller.response.CategoryOptionResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category Option API", description = "카테고리 제공 옵션 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories/{categoryId}/options")
public class CategoryOptionController {

    private final CategoryOptionService categoryOptionService;

    @Operation(summary = "카테고리 제공 옵션 조회", description = "특정 카테고리에서 제공하는 옵션을 조회합니다.")
    @GetMapping
    public ApiResponse<CategoryOptionResponse> getCategoryOptions(
        @PathVariable Long categoryId
    ) {
        return ApiResponse.success(categoryOptionService.getOptionsByCategory(categoryId));
    }

    @Operation(summary = "카테고리 제공 옵션 추가", description = "특정 카테고리에서 제공할 옵션을 추가합니다.")
    @PostMapping
    public ApiResponse<AddCategoryOptionResponse> addCategoryOption(
        @PathVariable Long categoryId,
        @Valid @RequestBody AddCategoryOptionRequest request
    ) {
        AddCategoryOptionResponse res =
            categoryOptionService.addCategoryOptions(categoryId, request);
        return ApiResponse.success(res);
    }

}
