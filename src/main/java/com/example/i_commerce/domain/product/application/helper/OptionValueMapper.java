package com.example.i_commerce.domain.product.application.helper;

import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionValueRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OptionValueMapper {

    private final Map<OptionKey, ProductOptionValue> map;
    private record OptionKey(Integer optionOrder, String value) {}

    private OptionValueMapper(Map<OptionKey, ProductOptionValue> map) {
        this.map = Map.copyOf(map);
    }

    public static OptionValueMapper from(
        Product product,
        List<OptionRequest> optionRequests
    ) {

        if (optionRequests == null || optionRequests.isEmpty()) {
            return new OptionValueMapper(Map.of());
        }

        Map<OptionKey, ProductOptionValue> map = new HashMap<>();

        for (OptionRequest option : optionRequests) {
            for (OptionValueRequest valueReq : option.values()) {
                ProductOptionValue optionValue = ProductOptionValue.of(
                    option.optionOrder(),
                    option.name(),
                    valueReq.value(),
                    valueReq.displayOrder()
                );
                product.addOptionValue(optionValue);
                map.put(new OptionKey(option.optionOrder(), valueReq.value()), optionValue);
            }
        }

        return new OptionValueMapper(map);
    }

    public ProductOptionValue getOrThrow(Integer optionOrder, String value) {
        ProductOptionValue optionValue = map.get(new OptionKey(optionOrder, value));
        if (optionValue == null) {
            throw new AppException(ProductErrorCode.OPTION_NOT_FOUND);
        }
        return optionValue;
    }

}
