package com.example.i_commerce.domain.cart.controller;


import com.example.i_commerce.domain.cart.controller.request.AddCartItemRequest;
import com.example.i_commerce.domain.cart.controller.request.DeleteCartItemRequest;
import com.example.i_commerce.domain.cart.controller.response.AddCartItemResponse;
import com.example.i_commerce.domain.cart.controller.response.CartResponse;
import com.example.i_commerce.domain.cart.service.CartQueryService;
import com.example.i_commerce.domain.cart.service.CartService;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;
    private final CartQueryService cartQueryService;


    @PostMapping("/items")
    public ApiResponse<AddCartItemResponse> addCartItem(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid AddCartItemRequest request
    ) {
        AddCartItemResponse response = cartService.addCartItem(principal.getId(), request);
        return ApiResponse.success(response);
    }


    @GetMapping
    public ApiResponse<CartResponse> getCart(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CartResponse response = cartQueryService.getCartItems(principal.getId());
        return ApiResponse.success(response);
    }


    @DeleteMapping("/items")
    public ApiResponse<Void> deleteCartItems(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid DeleteCartItemRequest request
    ) {
        cartService.deleteItems(principal.getId(), request);
        return ApiResponse.success();
    }

}
