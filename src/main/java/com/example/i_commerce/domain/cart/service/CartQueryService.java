package com.example.i_commerce.domain.cart.service;


import com.example.i_commerce.domain.cart.controller.response.CartItemResponse;
import com.example.i_commerce.domain.cart.controller.response.CartResponse;
import com.example.i_commerce.domain.cart.controller.response.CartStoreGroupResponse;
import com.example.i_commerce.domain.cart.entity.Cart;
import com.example.i_commerce.domain.cart.entity.CartItem;
import com.example.i_commerce.domain.product.application.dto.ProductItemInfo;
import com.example.i_commerce.domain.cart.repository.CartItemRepository;
import com.example.i_commerce.domain.cart.repository.CartRepository;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductQueryService productQueryService;

    public CartResponse getCartItems(Long userId) {

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if(cartOpt.isEmpty()) {
            return CartResponse.empty(null);
        }

        Cart cart = cartOpt.get();

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
        if(cartItems.isEmpty()) {
            return CartResponse.empty(cart.getId());
        }

        Set<Long> productItemIds = cartItems.stream()
            .map(CartItem::getProductItemId)
            .collect(Collectors.toSet());

        Map<Long, ProductItemInfo> productItemInfoMap =
            productQueryService.getProductItemInfosByIds(productItemIds).stream()
                .collect(Collectors.toMap(
                    ProductItemInfo::productItemId,
                    Function.identity()
                ));

        Map<Long, StoreAccumulator> storeMap = new LinkedHashMap<>();

        for(CartItem item : cartItems) {
            ProductItemInfo info = productItemInfoMap.getOrDefault(
                item.getProductItemId(),
                ProductItemInfo.unavailable(item)
            );
            CartItemResponse response = CartItemResponse.of(item, info);
            storeMap
                .computeIfAbsent(item.getStoreId(), _ -> new StoreAccumulator())
                .add(response);
        }

        List<CartStoreGroupResponse> storeGroups = storeMap.entrySet().stream()
            .map(entry -> CartStoreGroupResponse.of(
                entry.getKey(),
                entry.getValue().items,
                entry.getValue().priceSum
            ))
            .toList();

        int totalCheckedPrice = storeMap.values().stream()
            .mapToInt(acc -> acc.priceSum)
            .sum();

        return CartResponse.of(cart, storeGroups, totalCheckedPrice);
    }

    private static class StoreAccumulator {

        private final List<CartItemResponse> items = new ArrayList<>();
        private int priceSum = 0;

        void add(CartItemResponse response) {
            items.add(response);
            if (response.isChecked() && response.isOnSale()) {
                priceSum += response.itemTotalPrice();
            }
        }
    }

}


