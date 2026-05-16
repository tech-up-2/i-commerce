package com.example.i_commerce.domain.cart.repository;

import com.example.i_commerce.domain.cart.entity.CartItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("""
        SELECT ci
        FROM CartItem ci
        WHERE ci.cart.id = :cartId
        ORDER BY ci.createdAt DESC
    """)
    List<CartItem> findAllByCartId(@Param("cartId") Long cartId);

}
