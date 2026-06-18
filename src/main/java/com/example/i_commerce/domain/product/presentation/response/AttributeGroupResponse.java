package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.Attribute;
import java.util.List;
import lombok.Builder;

@Builder
public record AttributeGroupResponse(
    String key,
    List<AttributeValueResponse> values
) {
    public static AttributeGroupResponse of(String key, List<Attribute> attributes) {
        return AttributeGroupResponse.builder()
            .key(key)
            .values(attributes.stream()
                .map(AttributeValueResponse::from)
                .toList())
            .build();
    }

    @Builder
    public record AttributeValueResponse(
        Long id,
        String value
    ) {
        public static AttributeValueResponse from(Attribute attribute) {
            return AttributeValueResponse.builder()
                .id(attribute.getId())
                .value(attribute.getValue())
                .build();
        }
    }
}
