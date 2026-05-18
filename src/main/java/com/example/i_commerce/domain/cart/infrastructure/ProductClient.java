package com.example.i_commerce.domain.cart.infrastructure;


import com.example.i_commerce.domain.product.facade.ProductQueryFacade;
import com.example.i_commerce.domain.product.facade.dto.ProductItemInfoResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final ProductQueryFacade productQueryFacade;

    public ProductItemInfo getProductItemInfo(Long productItemId) {
        ProductItemInfoResponse res = productQueryFacade.getProductItemInfo(productItemId);

        return ProductItemInfo.from(res);
    }

    public Map<Long, ProductItemInfo> getProductItemInfos(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        List<ProductItemInfoResponse> res = productQueryFacade.getProductItemInfos(productIds);
        return res.stream()
            .collect(Collectors.toMap(
                ProductItemInfoResponse::productItemId,
                ProductItemInfo::from
            ));
    }

}
