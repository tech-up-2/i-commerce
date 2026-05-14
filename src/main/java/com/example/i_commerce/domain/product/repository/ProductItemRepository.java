package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {

    @Query("""
        SELECT
            pi.id as productItemId,
            p.name as productName,
            pi.price as price,
            pi.displayOptionName as displayOptionName,
            s.quantity as stockQuantity,
            pi.status as status
        FROM ProductItem pi
        JOIN pi.product p
        JOIN pi.stock s
        WHERE pi.id = :itemId
        """)
    Optional<ProductItemInfoProjection> findItemInfoById(@Param("itemId") Long itemId);
}
