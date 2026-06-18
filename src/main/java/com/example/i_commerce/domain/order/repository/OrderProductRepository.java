package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.OrderProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrderId(Long id);

    @Query("SELECT op FROM OrderProduct op JOIN FETCH op.order WHERE op.id = :orderProductId")
    Optional<OrderProduct> findByIdWithOrder(@Param("orderProductId") Long orderProductId);
}
