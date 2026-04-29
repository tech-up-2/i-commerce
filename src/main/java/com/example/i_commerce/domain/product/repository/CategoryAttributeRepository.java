package com.example.i_commerce.domain.product.repository;


import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {

    @Query("SELECT ca FROM CategoryAttribute ca "
        + "JOIN FETCH ca.attribute "
        + "WHERE ca.category.id = :categoryId ")
    List<CategoryAttribute> findByCategoryIdWithAttribute(@Param("categoryId") Long categoryId);


}
