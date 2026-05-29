package com.example.i_commerce.domain.product.presentation;


import com.example.i_commerce.domain.product.application.service.ProductService;
import com.example.i_commerce.domain.product.application.service.ProductUpdateService;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest;
import com.example.i_commerce.domain.product.presentation.request.UpdateProductRequest;
import com.example.i_commerce.domain.product.presentation.request.UpdateProductStatusRequest;
import com.example.i_commerce.domain.product.presentation.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.presentation.response.UpdateProductStatusResponse;
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

@Tag(name = "Product Command API", description = "상품 관리 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductCommandController {

    private final ProductService productService;
    private final ProductUpdateService productUpdateService;

    @Operation(summary = "상품 생성", description = "요청 정보를 바탕으로 상품을 생성합니다.")
    @PostMapping
    public ApiResponse<CreatedProductResponse> createProduct(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Validated CreateProductRequest request
    ) {
        return ApiResponse.success(productService.createProduct(principal.getId(), request));
    }

    @Operation(summary = "상품 기본 정보 수정", description = "상품의 기본 정보를 수정합니다.")
    @PatchMapping("/{productId}")
    public ApiResponse<Void> updateProduct(
        @PathVariable Long productId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        productUpdateService.updateBasicInfo(productId, principal.getId(), request);
        return ApiResponse.success();
    }


    @Operation(summary = "상품 상태 변경", description = "상품 상태를 변경합니다.")
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
