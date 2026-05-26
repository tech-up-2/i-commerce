package com.example.i_commerce.domain.cart.repository;

import com.example.i_commerce.domain.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @Query("""
        SELECT c
        FROM Cart c
        LEFT JOIN FETCH c.cartItems ci
        WHERE c.userId = :userId
    """)
    Optional<Cart> findByUserIdWithItems(Long userId);
}
