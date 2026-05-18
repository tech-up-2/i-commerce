package com.example.i_commerce.domain.product.repository.projection;

public record CategoryAttributeProjection(
    Long categoryAttributeId,
    Boolean required,
    Long attributeId,
    String attributeKey,
    String attributeValue
) {

}
