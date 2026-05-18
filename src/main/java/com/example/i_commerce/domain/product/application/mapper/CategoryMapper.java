package com.example.i_commerce.domain.product.application.mapper;


import com.example.i_commerce.domain.product.controller.response.CategoryResponse;
import com.example.i_commerce.domain.product.repository.projection.CategoryTreeRow;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public List<CategoryResponse> toHierarchy(List<CategoryTreeRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<CategoryTreeRow>> childrenMap = groupByParentId(rows);

        return rows.stream()
            .filter(row -> row.getParentId() == null)
            .map(root -> buildNode(root, childrenMap))
            .toList();
    }

    public CategoryResponse toTree(List<CategoryTreeRow> rows) {

        Map<Long, List<CategoryTreeRow>> childrenMap = groupByParentId(rows);

        return buildNode(rows.getFirst(), childrenMap);
    }

    private Map<Long, List<CategoryTreeRow>> groupByParentId(
        List<CategoryTreeRow> rows
    ) {
        return rows.stream()
            .filter(row -> row.getParentId() != null)
            .collect(Collectors.groupingBy(
                CategoryTreeRow::getParentId,
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    private CategoryResponse buildNode(
        CategoryTreeRow row,
        Map<Long, List<CategoryTreeRow>> childrenByParentId
    ) {
        List<CategoryResponse> children = childrenByParentId
            .getOrDefault(row.getId(), Collections.emptyList())
            .stream()
            .map(child -> buildNode(child, childrenByParentId))
            .toList();

        return CategoryResponse.builder()
            .id(row.getId())
            .parentId(row.getParentId())
            .name(row.getName())
            .depth(row.getDepth())
            .children(children)
            .build();
    }

}
