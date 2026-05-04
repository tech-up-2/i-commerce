package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.application.service.ProductService;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/seller/products")
public class ProductSellerController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<CreatedProductResponse> createProduct(
        Long sellerId,
        @RequestBody @Validated CreateProductRequest request
    ) {
        return ApiResponse.success(productService.createProduct(sellerId, request));
    }

}
