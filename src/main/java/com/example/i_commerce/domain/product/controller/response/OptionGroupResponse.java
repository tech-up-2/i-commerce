package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.entity.OptionInputType;
import java.util.List;
import lombok.Builder;

@Builder
public record OptionGroupResponse(
    String type,
    OptionInputType inputType,
    List<OptionValueResponse> values
) {
    public static OptionGroupResponse from(String type, List<Option> options) {
        return OptionGroupResponse.builder()
            .type(type)
            .inputType(options.getFirst().getInputType())
            .values(options.stream()
                .map(OptionValueResponse::from)
                .toList()
            )
            .build();
    }

    @Builder
    public record OptionValueResponse(
        Long id,
        String value
    ){
        public static OptionValueResponse from(Option option) {
            return OptionValueResponse.builder()
                .id(option.getId())
                .value(option.getValue())
                .build();
        }
    }
}
