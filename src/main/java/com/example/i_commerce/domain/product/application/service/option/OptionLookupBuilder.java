package com.example.i_commerce.domain.product.application.service.option;


import com.example.i_commerce.domain.product.controller.response.OptionItemLookupResponse;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OptionLookupBuilder {

    public OptionItemLookupResponse build(
        ProductOptionType optionType,
        List<ProductItem> allItems
    ) {
        return switch (optionType) {
            case NONE   -> OptionItemLookupResponse.ofNone();
            case SINGLE -> buildSingleLookup(allItems);
            case DOUBLE -> buildDoubleLookup(allItems);
        };
    }

    private OptionItemLookupResponse buildSingleLookup(List<ProductItem> allItems) {
        Map<Long, Long> lookup = new LinkedHashMap<>();
        for (ProductItem item : allItems) {
            if (item.getOptionValue1() != null) {
                lookup.put(item.getOptionValue1().getId(), item.getId());
            }
        }
        return OptionItemLookupResponse.ofSingle(lookup);
    }

    private OptionItemLookupResponse buildDoubleLookup(List<ProductItem> allItems) {
        Map<Long, Map<Long, Long>> lookup = new LinkedHashMap<>();
        for (ProductItem item : allItems) {
            if (item.getOptionValue1() != null && item.getOptionValue2() != null) {
                lookup.computeIfAbsent(item.getOptionValue1().getId(), k -> new LinkedHashMap<>())
                    .put(item.getOptionValue2().getId(), item.getId());
            }
        }
        return OptionItemLookupResponse.ofDouble(lookup);
    }
}
