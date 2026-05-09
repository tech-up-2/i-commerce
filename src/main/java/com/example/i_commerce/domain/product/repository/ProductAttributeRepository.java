package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.ProductAttribute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    @Query("""
    SELECT pa FROM ProductAttribute pa
    WHERE pa.productItem.id = :itemId
    ORDER BY pa.displayOrder ASC
    """)
    List<ProductAttribute> findByItemIdOrdered(@Param("itemId") Long itemId);

}
