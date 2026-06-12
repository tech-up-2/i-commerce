package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {

    @Query("""
        SELECT new com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection(
                pi.id, p.name, p.storeId, pi.price, pi.displayOptionName, s.quantity, pi.status
        )
        FROM ProductItem pi
        JOIN pi.product p
        JOIN pi.stock s
        WHERE pi.id = :itemId
        """)
    Optional<ProductItemInfoProjection> findItemInfoById(@Param("itemId") Long itemId);

    @Query("""
        SELECT new com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection(
                pi.id, p.name, p.storeId, pi.price, pi.displayOptionName, s.quantity, pi.status
        )
        FROM ProductItem pi
        JOIN pi.product p
        JOIN pi.stock s
        WHERE pi.id IN :ids
        """)
    List<ProductItemInfoProjection> findAllItemInfoByIdIn(@Param("ids") Set<Long> ids);

    List<ProductItem> findAllByProductId(Long productId);

}
