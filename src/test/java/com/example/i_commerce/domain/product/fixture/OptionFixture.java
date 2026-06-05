package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import java.util.List;

public class OptionFixture {

    public static Option.OptionBuilder defaultOption() {
        return Option.builder()
            .name("색상")
            .inputType(OptionInputType.RADIO);
    }

    public static Option createWithIdAndName(Long id, String name) {
        return defaultOption()
            .id(id)
            .name(name)
            .build();
    }

    public static List<Option> defaultOptions() {
        Option option1 = defaultOption()
            .name("용량")
            .inputType(OptionInputType.RADIO)
            .build();
        Option option2 = defaultOption()
            .name("크기")
            .inputType(OptionInputType.SELECT)
            .build();
        return List.of(option1, option2);
    }

}
