package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Attribute;
import java.util.List;

public class AttributeFixture {

    public static Attribute.AttributeBuilder defaultAttribute() {
        return Attribute.builder()
            .key("소재")
            .value("면");
    }

    public static Attribute createAttributeWithId(Long id, String key, String value) {
        return defaultAttribute()
            .id(id)
            .key(key)
            .value(value)
            .build();
    }

    public static List<Attribute> defaultAttributes() {
        Attribute attribute1 = defaultAttribute()
            .value("면")
            .build();
        Attribute attribute2 = defaultAttribute()
            .value("폴리에스터")
            .build();
        return List.of(attribute1, attribute2);
    }

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
