package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record ProductOptionGroupResponse(
    String optionName,
    Integer optionOrder,
    List<ProductOptionValueResponse> values
) {

    public static ProductOptionGroupResponse of(
        List<ProductOptionValue> groupValues,
        Set<Long> selectedOptionIds,
        Map<Long, List<ProductItem>> optionItemMap
    ) {
        ProductOptionValue first = groupValues.getFirst();

        List<ProductOptionValueResponse> valueResponses = groupValues.stream()
            .sorted(Comparator.comparingInt(ProductOptionValue::getDisplayOrder))
            .map(v -> {
                boolean selected = selectedOptionIds.contains(v.getId());

                List<ProductItem> linkedItems = optionItemMap.getOrDefault(v.getId(), List.of());

                boolean available = linkedItems.stream().anyMatch(item ->
                    item.getStatus() == ProductItemStatus.ON_SALE
                );

                return ProductOptionValueResponse.of(v, selected, available);
            })
            .collect(Collectors.toList());

        return ProductOptionGroupResponse.builder()
            .optionName(first.getOptionName())
            .optionOrder(first.getOptionOrder())
            .values(valueResponses)
            .build();
    }
}
