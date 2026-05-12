package com.example.i_commerce.domain.cart.service;


import com.example.i_commerce.domain.cart.controller.request.AddCartItemRequest;
import com.example.i_commerce.domain.cart.controller.response.AddCartItemResponse;
import com.example.i_commerce.domain.cart.entity.Cart;
import com.example.i_commerce.domain.cart.entity.CartItem;
import com.example.i_commerce.domain.cart.exception.CartErrorCode;
import com.example.i_commerce.domain.cart.infrastructure.ProductClient;
import com.example.i_commerce.domain.cart.infrastructure.ProductItemInfo;
import com.example.i_commerce.domain.cart.repository.CartItemRepository;
import com.example.i_commerce.domain.cart.repository.CartRepository;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public AddCartItemResponse addCartItem(Long userId, AddCartItemRequest request) {

        ProductItemInfo productItemInfo =
            productClient.getProductItem(request.productItemId());
        if(!productItemInfo.isAvailable()) {
            throw new AppException(CartErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> cartRepository.save(Cart.create(userId)));

        CartItem cartItem = cart.findCartItem(request.productItemId())
            .map(existingItem -> {
                existingItem.increaseQuantity(
                    request.quantity(), productItemInfo.stockQuantity()
                );
                return existingItem;
            }).orElseGet(() -> {
                CartItem newItem = CartItem.of(
                    cart,
                    productItemInfo.productItemId(),
                    productItemInfo.productName(),
                    productItemInfo.price(),
                    productItemInfo.displayOptionName(),
                    request.quantity()
                );
                return cartItemRepository.save(newItem);
            });

        return AddCartItemResponse.from(cartItem);
    }

}
