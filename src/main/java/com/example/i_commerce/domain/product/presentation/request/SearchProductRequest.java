package com.example.i_commerce.domain.product.presentation.request;

import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;


@Builder
public record SearchProductRequest(
    @Size(min = 2, message = "검색어는 2글자 이상이어야 합니다.")
    String keyword,

    Long categoryId,

    @Min(value = 0)
    Integer minPrice,

    @Min(value = 0)
    Integer maxPrice,

    List<Long> attributeIds,

    ProductSortType sortType
) {

}
