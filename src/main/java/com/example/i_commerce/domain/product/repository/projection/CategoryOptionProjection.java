package com.example.i_commerce.domain.product.repository.projection;

import com.example.i_commerce.domain.product.entity.OptionInputType;

public interface CategoryOptionProjection {
    Long getCategoryOptionId();
    Boolean getRequired();
    Long getOptionId();
    String getOptionType();
    String getOptionValue();
    OptionInputType getInputType();

}
