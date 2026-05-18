package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.application.dto.ProductSearchQuery;
import com.example.i_commerce.domain.product.controller.response.ProductItemSearchResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductSearchRepositoryCustom {

    Slice<ProductItemSearchResponse> search(ProductSearchQuery query, Pageable pageable);
}
