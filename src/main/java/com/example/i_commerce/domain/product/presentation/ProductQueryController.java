package com.example.i_commerce.domain.product.presentation;


import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.product.application.service.ProductSearchService;
import com.example.i_commerce.domain.product.presentation.request.SearchProductRequest;
import com.example.i_commerce.domain.product.presentation.response.ProductDetailResponse;
import com.example.i_commerce.domain.product.presentation.response.ProductItemSearchResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.common.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Product Query API", description = "상품 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductQueryController {
    private final ProductQueryService productQueryService;
    private final ProductSearchService productSearchService;

    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(
        @PathVariable Long productId,
        @RequestParam(required = false) Long itemId
    ) {
        ProductDetailResponse response =
            productQueryService.getProductDetail(productId, itemId);
        return ApiResponse.success(response);
    }


    @Operation(summary = "상품 검색", description = "요청 파라미터를 바탕으로 상품을 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<SliceResponse<ProductItemSearchResponse>> search(
        @Valid @ModelAttribute SearchProductRequest request,
        @AuthenticationPrincipal UserDetails userDetails,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        boolean isAuthenticated = (userDetails != null);

        Slice<ProductItemSearchResponse> result =
            productSearchService.search(request, pageable, isAuthenticated);

        return ApiResponse.success(SliceResponse.of(result));
    }

}

