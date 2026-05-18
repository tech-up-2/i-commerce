package com.example.i_commerce.domain.cart.controller;


import com.example.i_commerce.domain.cart.controller.request.AddCartItemRequest;
import com.example.i_commerce.domain.cart.controller.request.DeleteCartItemRequest;
import com.example.i_commerce.domain.cart.controller.response.AddCartItemResponse;
import com.example.i_commerce.domain.cart.controller.response.CartResponse;
import com.example.i_commerce.domain.cart.service.CartQueryService;
import com.example.i_commerce.domain.cart.service.CartService;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Cart API", description = "장바구니 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;
    private final CartQueryService cartQueryService;

    @Operation(summary = "상품 추가", description = "장바구니에 특정 상품을 추가합니다.")
    @PostMapping("/items")
    public ApiResponse<AddCartItemResponse> addCartItem(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid AddCartItemRequest request
    ) {
        AddCartItemResponse response = cartService.addCartItem(principal.getId(), request);
        return ApiResponse.success(response);
    }


    @Operation(summary = "장바구니 조회", description = "장바구니를 조회합니다.")
    @GetMapping
    public ApiResponse<CartResponse> getCart(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CartResponse response = cartQueryService.getCartItems(principal.getId());
        return ApiResponse.success(response);
    }

    @Operation(summary = "상품 제거", description = "장바구니에서 특정 상품들을 제거합니다.")
    @DeleteMapping("/items")
    public ApiResponse<Void> deleteCartItems(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid DeleteCartItemRequest request
    ) {
        cartService.deleteItems(principal.getId(), request);
        return ApiResponse.success();
    }

}
