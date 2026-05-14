package com.example.i_commerce.domain.product.facade;

import com.example.i_commerce.domain.product.facade.dto.ProductItemInfoResponse;
import java.util.List;
import java.util.Set;

public interface ProductQueryFacade {
    ProductItemInfoResponse getProductItemInfo(Long productItem);
    List<ProductItemInfoResponse> getProductItemInfos(Set<Long> productItemIds);
}
