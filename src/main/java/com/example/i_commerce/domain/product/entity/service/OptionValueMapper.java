package com.example.i_commerce.domain.product.entity.service;

import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import java.util.HashMap;
import java.util.Map;

public class OptionValueMapper {

    private final Map<OptionKey, ProductOptionValue> map = new HashMap<>();

    public static OptionValueMapper empty() {
        return new OptionValueMapper();
    }

    public void put(Integer optionOrder, String value, ProductOptionValue optionValue) {
        map.put(new OptionKey(optionOrder, value), optionValue);
    }

    public ProductOptionValue get(Integer optionOrder, String value) {
        ProductOptionValue optionValue = map.get(new OptionKey(optionOrder, value));
        if (optionValue == null) {
            throw new AppException(ErrorCode.OPTION_NOT_FOUND);
        }
        return optionValue;
    }

    private record OptionKey(
        Integer optionOrder,
        String value
    ) {}

}
