package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.OrderProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrderId(Long id);
}
