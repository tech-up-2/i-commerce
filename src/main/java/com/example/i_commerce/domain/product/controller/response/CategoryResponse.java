package com.example.i_commerce.domain.product.controller.response;


import java.util.List;
import lombok.Builder;

@Builder
public record CategoryResponse(
    Long id,
    Long parentId,
    String name,
    Integer depth,
    List<CategoryResponse> children
) {

}
