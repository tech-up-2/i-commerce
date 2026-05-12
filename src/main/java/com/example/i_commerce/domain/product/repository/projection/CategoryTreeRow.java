package com.example.i_commerce.domain.product.repository.projection;

public interface CategoryTreeRow {
    Long getId();
    Long getParentId();
    String getName();
    Integer getDepth();
}
