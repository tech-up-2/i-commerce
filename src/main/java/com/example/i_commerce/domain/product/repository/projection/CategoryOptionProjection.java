package com.example.i_commerce.domain.product.repository.projection;

public interface CategoryOptionProjection {
    Long getCategoryOptionId();
    Boolean getRequired();
    Long getOptionId();
    String getOptionType();
    String getOptionValue();
    String getDisplayName();
    String getInputType();

}
