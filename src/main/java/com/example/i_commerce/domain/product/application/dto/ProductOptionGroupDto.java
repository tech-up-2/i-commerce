package com.example.i_commerce.domain.product.application.dto;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record ProductOptionGroupDto(
    String optionName,
    Integer optionOrder,
    List<ProductOptionValueDto> values
) {

    public static ProductOptionGroupDto of(
        List<ProductOptionValue> groupValues,
        Set<Long> selectedOptionIds,
        Map<Long, List<ProductItem>> optionItemMap
    ) {
        ProductOptionValue first = groupValues.getFirst();

        List<ProductOptionValueDto> valueResponses = groupValues.stream()
            .sorted(Comparator.comparingInt(ProductOptionValue::getDisplayOrder))
            .map(v -> {
                boolean selected = selectedOptionIds.contains(v.getId());

                List<ProductItem> linkedItems = optionItemMap.getOrDefault(v.getId(), List.of());

                boolean available = linkedItems.stream().anyMatch(item ->
                    item.getStatus() == ProductItemStatus.ON_SALE
                );

                return ProductOptionValueDto.of(v, selected, available);
            })
            .collect(Collectors.toList());

        return ProductOptionGroupDto.builder()
            .optionName(first.getOptionName())
            .optionOrder(first.getOptionOrder())
            .values(valueResponses)
            .build();
    }
}
