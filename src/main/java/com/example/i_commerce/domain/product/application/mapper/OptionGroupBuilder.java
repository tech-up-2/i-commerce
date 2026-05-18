package com.example.i_commerce.domain.product.application.mapper;

import com.example.i_commerce.domain.product.application.dto.ProductOptionGroupDto;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionType;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;


@Component
public class OptionGroupBuilder {

    public List<ProductOptionGroupDto> build(
        ProductOptionType optionType,
        List<ProductOptionValue> allOptions,
        List<ProductItem> allItems,
        ProductItem selectedItem
    ) {
        return switch (optionType) {
            case NONE   -> Collections.emptyList();
            case SINGLE -> buildSingleOptionGroups(allOptions, allItems, selectedItem);
            case DOUBLE -> buildDoubleOptionGroups(allOptions, allItems, selectedItem);
        };
    }

    private List<ProductOptionGroupDto> buildSingleOptionGroups(
        List<ProductOptionValue> allOptions,
        List<ProductItem> allItems,
        ProductItem selectedItem
    ) {
        Map<Long, List<ProductItem>> itemsByOptionValue = allItems.stream()
            .filter(item -> item.getOptionValue1() != null)
            .collect(Collectors.groupingBy(item -> item.getOptionValue1().getId()));

        Set<Long> selectedOptionIds = Optional.ofNullable(selectedItem.getOptionValue1())
            .map(optionValue -> Set.of(optionValue.getId()))
            .orElse(Collections.emptySet());

        return List.of(
            ProductOptionGroupDto.of(allOptions, selectedOptionIds, itemsByOptionValue)
        );
    }

    private List<ProductOptionGroupDto> buildDoubleOptionGroups(
        List<ProductOptionValue> allOptions,
        List<ProductItem> allItems,
        ProductItem selectedItem
    ) {

        Map<Long, List<ProductItem>> itemsByOptionValue1 = new LinkedHashMap<>();
        Map<Long, List<ProductItem>> itemsByOptionValue2 = new LinkedHashMap<>();

        for (ProductItem item : allItems) {
            if (item.getOptionValue1() != null) {
                itemsByOptionValue1
                    .computeIfAbsent(item.getOptionValue1().getId(), k -> new ArrayList<>())
                    .add(item);
            }
            if (item.getOptionValue2() != null) {
                itemsByOptionValue2
                    .computeIfAbsent(item.getOptionValue2().getId(), k -> new ArrayList<>())
                    .add(item);
            }
        }

        Set<Long> selectedOptionIds = findDoubleSelectedOptionIds(selectedItem);

        Map<Integer, Map<Long, List<ProductItem>>> itemMapByOrder = new LinkedHashMap<>();
        itemMapByOrder.put(1, itemsByOptionValue1);
        itemMapByOrder.put(2, itemsByOptionValue2);

        Map<Integer, List<ProductOptionValue>> optionValuesByOrder = allOptions.stream()
            .collect(Collectors.groupingBy(
                ProductOptionValue::getOptionOrder,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        return optionValuesByOrder.entrySet().stream().map(
            entry -> ProductOptionGroupDto.of(
                entry.getValue(),
                selectedOptionIds,
                itemMapByOrder.get(entry.getKey())
            ))
            .collect(Collectors.toList());
    }

    private Set<Long> findDoubleSelectedOptionIds(ProductItem selectedItem) {
        return Stream.of(selectedItem.getOptionValue1(), selectedItem.getOptionValue2())
            .filter(Objects::nonNull)
            .map(ProductOptionValue::getId)
            .collect(Collectors.toUnmodifiableSet());
    }

}
