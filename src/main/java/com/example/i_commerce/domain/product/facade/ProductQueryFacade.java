package com.example.i_commerce.domain.product.facade;

import com.example.i_commerce.domain.product.facade.dto.ProductItemInfoResponse;

public interface ProductQueryFacade {
    ProductItemInfoResponse getProductItemInfo(Long productItem);
}
