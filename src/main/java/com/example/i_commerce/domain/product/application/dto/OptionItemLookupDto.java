package com.example.i_commerce.domain.product.application.dto;

import com.example.i_commerce.domain.product.entity.ProductOptionType;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;

@Builder
public record OptionItemLookupDto(
    String lookupType,
    Map<Long, Long> singleMap,
    Map<Long, Map<Long, Long>> doubleMap
) {

    private static OptionItemLookupDtoBuilder defaultOptionItemLookup() {
        return OptionItemLookupDto.builder()
            .singleMap(Collections.emptyMap())
            .doubleMap(Collections.emptyMap());
    }

    public static OptionItemLookupDto ofNone() {
        return defaultOptionItemLookup()
            .lookupType(ProductOptionType.NONE.name())
            .build();
    }

    public static OptionItemLookupDto ofSingle(Map<Long, Long> singleMap) {
        return defaultOptionItemLookup()
            .lookupType(ProductOptionType.SINGLE.name())
            .singleMap(singleMap)
            .build();
    }

    public static OptionItemLookupDto ofDouble(Map<Long, Map<Long, Long>> doubleMap) {
        return defaultOptionItemLookup()
            .lookupType(ProductOptionType.DOUBLE.name())
            .doubleMap(doubleMap)
            .build();
    }
}
