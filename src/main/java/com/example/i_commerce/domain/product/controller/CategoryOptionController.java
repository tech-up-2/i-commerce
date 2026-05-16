package com.example.i_commerce.domain.product.controller;

import com.example.i_commerce.domain.product.application.service.CategoryOptionService;
import com.example.i_commerce.domain.product.controller.response.CategoryOptionGroupResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories/{categoryId}/options")
public class CategoryOptionController {

    private final CategoryOptionService categoryOptionService;

    @GetMapping
    public ApiResponse<List<CategoryOptionGroupResponse>> getCategoryOptions(
        @PathVariable Long categoryId
    ) {
        return ApiResponse.success(categoryOptionService.getOptionsByCategory(categoryId));
    }

}
