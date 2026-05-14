package com.example.i_commerce.domain.cart.infrastructure;


import com.example.i_commerce.domain.product.facade.ProductQueryFacade;
import com.example.i_commerce.domain.product.facade.dto.ProductItemInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final ProductQueryFacade productQueryFacade;

    public ProductItemInfo getProductItem(Long productItemId) {
        ProductItemInfoResponse res = productQueryFacade.getProductItemInfo(productItemId);

        return ProductItemInfo.builder()
            .productItemId(res.productItemId())
            .productName(res.productName())
            .price(res.price())
            .displayOptionName(res.displayOptionName())
            .stockQuantity(res.stockQuantity())
            .isAvailable(res.onSale())
            .build();
    }

}
