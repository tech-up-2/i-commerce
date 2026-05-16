package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.ProductService;
import com.example.i_commerce.domain.product.application.service.ProductUpdateService;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.request.UpdateProductStatusRequest;
import com.example.i_commerce.domain.product.controller.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.controller.response.UpdateProductStatusResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product API", description = "상품 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductSellerController {

    private final ProductService productService;
    private final ProductUpdateService productUpdateService;

    @Operation(summary = "상품 생성", description = "상품을 생성한다.")
    @PostMapping
    public ApiResponse<CreatedProductResponse> createProduct(
        Long sellerId,
        @RequestBody @Validated CreateProductRequest request
    ) {
        return ApiResponse.success(productService.createProduct(sellerId, request));
    }

    @PatchMapping("/{productId}/status")
    public ApiResponse<UpdateProductStatusResponse> changeStatus(
        @PathVariable Long productId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        UpdateProductStatusResponse res =
            productUpdateService.changeProductStatus(productId, principal.getId(), request);
        return ApiResponse.success(res);
    }

}
