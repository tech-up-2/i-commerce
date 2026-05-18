package com.example.i_commerce.domain.product.application.dto;

import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeProjection;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryAttributeGroupDto(
    String attributeKey,
    List<CategoryAttributeValueDto> attributeValues

) {

    public static CategoryAttributeGroupDto of(
        String attributeKey,
        List<CategoryAttributeValueDto> attributeValues
    ) {
        return CategoryAttributeGroupDto.builder()
            .attributeKey(attributeKey)
            .attributeValues(attributeValues)
            .build();
    }

    @Builder
    public record CategoryAttributeValueDto(
        Long categoryAttributeId,
        Long attributeId,
        String value,
        Boolean required
    ) {

        public static CategoryAttributeValueDto from(
            CategoryAttributeProjection projection
        ) {
            return CategoryAttributeValueDto.builder()
                .categoryAttributeId(projection.categoryAttributeId())
                .attributeId(projection.attributeId())
                .value(projection.attributeValue())
                .required(projection.required())
                .build();
        }
    }

}
