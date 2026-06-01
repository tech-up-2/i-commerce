package com.example.i_commerce.domain.product.presentation;


import com.example.i_commerce.domain.product.application.service.CategoryAttributeService;
import com.example.i_commerce.domain.product.presentation.request.AddCategoryAttributeRequest;
import com.example.i_commerce.domain.product.presentation.response.AddCategoryAttributeResponse;
import com.example.i_commerce.domain.product.presentation.response.CategoryAttributeResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Category Attribute API", description = "카테고리 제공 속성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories/{categoryId}/attributes")
public class CategoryAttributeController {

    private final CategoryAttributeService categoryAttributeService;

    @Operation(summary = "카테고리 제공 속성 조회", description = "특정 카테고리에서 제공하는 속성을 조회합니다.")
    @GetMapping
    public ApiResponse<CategoryAttributeResponse> getCategoryAttributes(
        @PathVariable Long categoryId
    ){
        CategoryAttributeResponse res =
            categoryAttributeService.getAttributesByCategory(categoryId);
        return ApiResponse.success(res);
    }

    @Operation(summary = "카테고리 제공 속성 추가", description = "특정 카테고리에서 제공할 속성을 추가합니다.")
    @PreAuthorize("@authChecker.canManageCategory()")
    @PostMapping
    public ApiResponse<AddCategoryAttributeResponse> addCategoryAttribute(
        @PathVariable Long categoryId,
        @Valid @RequestBody AddCategoryAttributeRequest request
    ){
        AddCategoryAttributeResponse res =
            categoryAttributeService.addCategoryAttribute(categoryId, request);
        return ApiResponse.success(res);
    }


    @Operation(summary = "카테고리 제공 속성 제거", description = "특정 카테고리에서 제공할 속성을 제거합니다.")
    @PreAuthorize("@authChecker.canManageCategory()")
    @DeleteMapping("/{categoryAttributeId}")
    public ApiResponse<Void> deleteCategoryAttribute(
        @PathVariable Long categoryId,
        @PathVariable Long categoryAttributeId
    ) {
        categoryAttributeService.deleteCategoryAttribute(categoryId, categoryAttributeId);
        return ApiResponse.success();
    }

}
