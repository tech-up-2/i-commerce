package com.example.i_commerce.domain.product.entity.policy;

public final class CategoryPolicy {
    public static final int MAX_DEPTH = 5;
    public static final int DEFAULT_TREE_DEPTH = 3;
    public static final int RECURSIVE_DEPTH_LIMIT = 5;

    private CategoryPolicy() {
        throw new AssertionError("should not be instantiated");
    }
}
