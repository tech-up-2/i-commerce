package com.example.i_commerce.domain.product.controller;

import com.example.i_commerce.domain.product.application.service.CategoryOptionService;
import com.example.i_commerce.domain.product.controller.request.AddCategoryOptionRequest;
import com.example.i_commerce.domain.product.controller.response.AddCategoryOptionResponse;
import com.example.i_commerce.domain.product.application.dto.CategoryOptionGroupDto;
import com.example.i_commerce.domain.product.controller.response.CategoryOptionResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories/{categoryId}/options")
public class CategoryOptionController {

    private final CategoryOptionService categoryOptionService;

    @GetMapping
    public ApiResponse<CategoryOptionResponse> getCategoryOptions(
        @PathVariable Long categoryId
    ) {
        return ApiResponse.success(categoryOptionService.getOptionsByCategory(categoryId));
    }

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
