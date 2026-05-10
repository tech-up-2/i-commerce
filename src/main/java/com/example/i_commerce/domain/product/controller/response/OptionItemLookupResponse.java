package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.ProductOptionType;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;

@Builder
public record OptionItemLookupResponse(
    String lookupType,
    Map<Long, Long> singleMap,
    Map<Long, Map<Long, Long>> doubleMap
) {

    private static OptionItemLookupResponseBuilder defaultOptionItemLookup() {
        return OptionItemLookupResponse.builder()
            .singleMap(Collections.emptyMap())
            .doubleMap(Collections.emptyMap());
    }

    public static OptionItemLookupResponse ofNone() {
        return defaultOptionItemLookup()
            .lookupType(ProductOptionType.NONE.name())
            .build();
    }

    public static OptionItemLookupResponse ofSingle(Map<Long, Long> singleMap) {
        return defaultOptionItemLookup()
            .lookupType(ProductOptionType.SINGLE.name())
            .singleMap(singleMap)
            .build();
    }

    public static OptionItemLookupResponse ofDouble(Map<Long, Map<Long, Long>> doubleMap) {
        return defaultOptionItemLookup()
            .lookupType(ProductOptionType.DOUBLE.name())
            .doubleMap(doubleMap)
            .build();
    }
}
