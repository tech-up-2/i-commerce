package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Attribute;
import java.util.List;

public class AttributeFixture {

    public static Attribute createAttribute(String key, String value) {
        return Attribute.builder()
            .key(key)
            .value(value)
            .build();
    }

    public static List<Attribute> createAttributes(
        String key,
        List<String> values
    ) {
        return values.stream()
            .map(value -> createAttribute(key, value))
            .toList();
    }

}
