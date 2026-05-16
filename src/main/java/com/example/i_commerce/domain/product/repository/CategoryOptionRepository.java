package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryOptionRepository extends JpaRepository<CategoryOption, Long> {

    Optional<CategoryOption> findByCategoryId(Long categoryId);

    @Query("""
    SELECT
        co.id AS categoryOptionId,
        co.required AS required,
        o.id AS optionId,
        o.type AS optionType,
        o.value AS optionValue,
        o.inputType AS inputType
    FROM CategoryOption co
    JOIN co.option o
    WHERE co.category.id = :categoryId
    ORDER BY o.type, o.value
    """)
    List<CategoryOptionProjection> findOptionsByCategoryId(
        @Param("categoryId") Long categoryId
    );
}
