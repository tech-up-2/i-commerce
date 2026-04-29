package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Category;

public class CategoryFixture {

    public static Category devicesCategory() {
        return Category.builder()
            .id(1L)
            .name("전자기기")
            .depth(0)
            .build();
    }

    public static Category clothCategory() {
        return Category.builder()
            .id(2L)
            .name("의류")
            .depth(0)
            .build();
    }

    public static Category createCategory(Long id, String name, String code) {
        return Category.builder()
            .id(id)
            .name(name)
            .depth(0)
            .build();
    }

}
