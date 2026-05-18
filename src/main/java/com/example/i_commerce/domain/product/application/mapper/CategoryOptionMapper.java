package com.example.i_commerce.domain.product.application.mapper;


import com.example.i_commerce.domain.product.application.dto.CategoryOptionGroupDto;
import com.example.i_commerce.domain.product.application.dto.CategoryOptionGroupDto.CategoryOptionValueResponse;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CategoryOptionMapper {


    public List<CategoryOptionGroupDto> toGroupedResponseList(
        List<CategoryOptionProjection> projections
    ) {
        if (projections.isEmpty()) {
            return List.of();
        }

        return projections.stream()
            .collect(Collectors.groupingBy(
                CategoryOptionProjection::getOptionType,
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(this::toGroupResponse)
            .toList();
    }

    private CategoryOptionGroupDto toGroupResponse(
        Entry<String, List<CategoryOptionProjection>> entry
    ) {
        return CategoryOptionGroupDto.builder()
            .optionType(entry.getKey())
            .optionValues(entry.getValue().stream()
                .map(p -> CategoryOptionValueResponse.builder()
                    .categoryOptionId(p.getCategoryOptionId())
                    .optionId(p.getOptionId())
                    .value(p.getOptionValue())
                    .inputType(p.getInputType())
                    .required(p.getRequired())
                    .build()
                ).toList()
            ).build();
    }


}
