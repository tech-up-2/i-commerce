package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
      SELECT p FROM Product p
      LEFT JOIN FETCH p.items i
      LEFT JOIN FETCH i.stock
      WHERE p.id = :id
    """)
    Optional<Product> findByIdWithItemsAndStock(@Param("id") Long id);

    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1 FROM Product p
            WHERE p.category.id IN :categoryIds
        ) THEN true ELSE false END
    """)
    boolean existsActiveByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

}
