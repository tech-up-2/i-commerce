package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Category;

public class CategoryFixture {

    public static Category.CategoryBuilder defaultCategory() {
        return Category.builder()
            .name("기본 카테고리명")
            .depth(0);
    }

    public static Category createRootWithId(Long id) {
        return defaultCategory()
            .id(id)
            .build();
    }

    public static Category rootCategory() {
        return defaultCategory()
            .build();
    }

    public static Category createChild(Category category, String name) {
        return defaultCategory()
            .parent(category)
            .name(name)
            .depth(category.getDepth() + 1)
            .build();
    }

}
