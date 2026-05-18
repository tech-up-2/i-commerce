package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.CategoryAttributeService;
import com.example.i_commerce.domain.product.controller.request.AddCategoryAttributeRequest;
import com.example.i_commerce.domain.product.controller.response.AddCategoryAttributeResponse;
import com.example.i_commerce.domain.product.controller.response.CategoryAttributeResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories/{categoryId}/attributes")
public class CategoryAttributeController {

    private final CategoryAttributeService categoryAttributeService;

    @GetMapping
    public ApiResponse<CategoryAttributeResponse> getCategoryAttributes(
        @PathVariable Long categoryId
    ){
        CategoryAttributeResponse res =
            categoryAttributeService.getAttributesByCategory(categoryId);
        return ApiResponse.success(res);
    }


    @PostMapping
    public ApiResponse<AddCategoryAttributeResponse> addCategoryAttribute(
        @PathVariable Long categoryId,
        @Valid @RequestBody AddCategoryAttributeRequest request
    ){
        AddCategoryAttributeResponse res =
            categoryAttributeService.addCategoryAttribute(categoryId, request);
        return ApiResponse.success(res);
    }

}
