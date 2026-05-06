package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.product.controller.response.ProductDetailResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductQueryService productQueryService;

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(
        @PathVariable Long productId,
        @RequestParam(required = false) Long itemId
    ) {
        ProductDetailResponse response = productQueryService.getProductDetail(productId, itemId);
        return ApiResponse.success(response);
    }
}
