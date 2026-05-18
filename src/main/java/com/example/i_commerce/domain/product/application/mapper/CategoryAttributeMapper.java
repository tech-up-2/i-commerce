package com.example.i_commerce.domain.product.application.mapper;

import com.example.i_commerce.domain.product.application.dto.CategoryAttributeGroupDto;
import com.example.i_commerce.domain.product.application.dto.CategoryAttributeGroupDto.CategoryAttributeValueDto;
import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeProjection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CategoryAttributeMapper {

    public List<CategoryAttributeGroupDto> toGroupResponse(
        List<CategoryAttributeProjection> projections
    ) {
        return projections.stream()
            .collect(Collectors.groupingBy(
                CategoryAttributeProjection::attributeKey,
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(this::toGroupResponse)
            .toList();
    }

    private CategoryAttributeGroupDto toGroupResponse(
        Entry<String, List<CategoryAttributeProjection>> entry
    ) {
        return CategoryAttributeGroupDto.of(
            entry.getKey(),
            entry.getValue().stream()
                .map(CategoryAttributeValueDto::from)
                .toList()
        );
    }

}
