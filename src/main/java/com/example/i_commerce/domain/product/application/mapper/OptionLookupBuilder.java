package com.example.i_commerce.domain.product.application.mapper;


import com.example.i_commerce.domain.product.application.dto.OptionItemLookupDto;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OptionLookupBuilder {

    public OptionItemLookupDto build(
        ProductOptionType optionType,
        List<ProductItem> allItems
    ) {
        return switch (optionType) {
            case NONE   -> OptionItemLookupDto.ofNone();
            case SINGLE -> buildSingleLookup(allItems);
            case DOUBLE -> buildDoubleLookup(allItems);
        };
    }

    private OptionItemLookupDto buildSingleLookup(List<ProductItem> allItems) {
        Map<Long, Long> lookup = new LinkedHashMap<>();
        for (ProductItem item : allItems) {
            if (item.getOptionValue1() != null) {
                lookup.put(item.getOptionValue1().getId(), item.getId());
            }
        }
        return OptionItemLookupDto.ofSingle(lookup);
    }

    private OptionItemLookupDto buildDoubleLookup(List<ProductItem> allItems) {
        Map<Long, Map<Long, Long>> lookup = new LinkedHashMap<>();
        for (ProductItem item : allItems) {
            if (item.getOptionValue1() != null && item.getOptionValue2() != null) {
                lookup.computeIfAbsent(item.getOptionValue1().getId(), k -> new LinkedHashMap<>())
                    .put(item.getOptionValue2().getId(), item.getId());
            }
        }
        return OptionItemLookupDto.ofDouble(lookup);
    }
}
