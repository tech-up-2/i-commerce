package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Attribute;
import java.util.List;

public class AttributeFixture {
    public static Attribute colorAttribute() {
        return Attribute.builder()
            .id(1L)
            .key("색상")
            .value("빨강")
            .build();
    }

    public static Attribute materialAttribute() {
        return Attribute.builder()
            .id(2L)
            .key("재질")
            .value("면")
            .build();
    }

    public static Attribute volumeAttribute1() {
        return Attribute.builder()
            .id(3L)
            .key("용량")
            .value("256GB")
            .build();
    }

    public static Attribute volumeAttribute2() {
        return Attribute.builder()
            .id(4L)
            .key("용량")
            .value("512GB")
            .build();
    }

    public static List<Attribute> basicAttribute() {
        return List.of(colorAttribute(), materialAttribute());
    }

    public static Attribute createAttribute(Long id, String name, String code) {
        return Attribute.builder()
            .id(id)
            .key(name)
            .value(code)
            .build();
    }
}
