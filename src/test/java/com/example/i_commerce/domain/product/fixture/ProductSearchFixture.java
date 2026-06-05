package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.presentation.request.SearchProductRequest;
import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import java.util.List;

public class ProductSearchFixture {

    public static SearchProductRequest.SearchProductRequestBuilder defaultRequest() {
        return SearchProductRequest
            .builder();
    }

    public static SearchProductRequest defaultSearchRequest() {
        return defaultRequest().build();
    }

    public static SearchProductRequest createRequestWithKeyword(String keyword) {
        return defaultRequest()
            .keyword(keyword)
            .build();
    }

    public static SearchProductRequest createRequestWithCategory(Long categoryId) {
        return defaultRequest()
            .categoryId(categoryId)
            .build();
    }

    public static SearchProductRequest createRequestWithSortType(ProductSortType sortType) {
        return defaultRequest()
            .sortType(sortType)
            .build();
    }

    public static SearchProductRequest createFullRequest(
        String keyword,
        Long categoryId,
        Integer minPrice,
        Integer maxPrice,
        List<Long> attributeIds,
        ProductSortType sortType
    ) {
        return new SearchProductRequest(
            keyword, categoryId, minPrice, maxPrice, attributeIds, sortType
        );
    }
}
