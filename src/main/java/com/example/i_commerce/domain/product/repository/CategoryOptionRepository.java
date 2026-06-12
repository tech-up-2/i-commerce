package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionKey;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryOptionRepository extends JpaRepository<CategoryOption, Long> {

    Optional<CategoryOption> findByIdAndCategoryId(Long id, Long categoryId);

    @Query("""
    SELECT co.id
    FROM CategoryOption co
    WHERE co.category.id = :categoryId
    AND co.option.id IN :optionIds
    """)
    List<Long> findAllIdsByCategoryIdAndOptionIds(Long categoryId, List<Long> optionIds);

    @Query("""
    SELECT new com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection(
        co.id, co.required, o.id, o.name, o.inputType
    )
    FROM CategoryOption co
    JOIN co.option o
    WHERE co.category.id = :categoryId
    ORDER BY o.name
    """)
    List<CategoryOptionProjection> findOptionsByCategoryId(
        @Param("categoryId") Long categoryId
    );

    @Query("""
    SELECT new com.example.i_commerce.domain.product.repository.projection.CategoryOptionKey(
        co.category.id,
        co.option.id
    )
    FROM CategoryOption co
    WHERE co.category.id IN :categoryIds
    AND co.option.id IN :optionIds
    """)
    List<CategoryOptionKey> findExistingKeys(
        @Param("categoryIds") List<Long> categoryIds,
        @Param("optionIds") List<Long> optionIds
    );
}
