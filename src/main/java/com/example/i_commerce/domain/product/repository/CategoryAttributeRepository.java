package com.example.i_commerce.domain.product.repository;


import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {

    @Query("""
    SELECT new com.example.i_commerce.domain.product.repository.projection.CategoryAttributeKey(
        ca.category.id,
        ca.attribute.id
    )
    FROM CategoryAttribute ca
    WHERE ca.category.id IN :categoryIds
    AND ca.attribute.id IN :attributeIds
    """)
    List<CategoryAttributeKey> findExistingKeys(
        @Param("categoryIds") List<Long> categoryIds,
        @Param("attributeIds") List<Long> attributeIds
    );


    @Query("SELECT ca FROM CategoryAttribute ca "
        + "JOIN FETCH ca.attribute "
        + "WHERE ca.category.id = :categoryId ")
    List<CategoryAttribute> findByCategoryIdWithAttribute(@Param("categoryId") Long categoryId);

}
