package com.example.i_commerce.domain.product.application.service.query;


import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import java.util.List;
import lombok.Builder;



@Builder
public record ProductSearchQuery(
    String keyword,
    Integer minPrice,
    Integer maxPrice,
    List<Long> categoryIds,
    List<Long> attributeIds,
    ProductSortType sortType,
    boolean isAuthenticated
) {

    public ProductSearchQuery {
        categoryIds = categoryIds != null ? categoryIds : List.of();
        attributeIds = attributeIds != null ? attributeIds : List.of();
    }

}
